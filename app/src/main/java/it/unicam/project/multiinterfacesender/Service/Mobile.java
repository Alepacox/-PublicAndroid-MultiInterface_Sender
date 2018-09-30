package it.unicam.project.multiinterfacesender.Service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import it.unicam.project.multiinterfacesender.IService_App_to_Mobile;
import it.unicam.project.multiinterfacesender.IService_Mobile_to_App;

public class Mobile extends Service {

    protected IService_Mobile_to_App iService_mobile_to_app;

    private ConnectivityManager connectivityManager;
    private InetAddress serverAddr;
    private int PORT;
    private NetworkRequest.Builder builder;
    private Socket socket;

    private boolean keepAlive = false;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private byte[] toSend;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IService_App_to_Mobile.Stub() {
            @Override
            public void register(IService_Mobile_to_App activity) {
                Mobile.this.iService_mobile_to_app = activity;
            }

            @Override
            public void createConnection(String ip, int port) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                try {
                    serverAddr = InetAddress.getByName(ip);
                } catch (UnknownHostException ignored) {
                }
                PORT = port;

                builder = new NetworkRequest.Builder();
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        ConnectivityManager.setProcessDefaultNetwork(network);
                        try {
                            socket = new Socket(serverAddr, PORT);
                            //network.bindSocket(socket);
                        } catch (IOException ignored) {
                        }
                    }
                });
                connectionCreated();
            }

            @Override
            public void connect() {
                new Thread(() -> {
                    keepAlive = true;
                    while (objectOutputStream == null || objectInputStream == null) {
                        if (objectOutputStream == null && socket != null) {
                            try {
                                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            } catch (Exception ignored) {
                            }
                        }
                        if (objectInputStream == null && socket != null) {
                            try {
                                objectInputStream = new ObjectInputStream(socket.getInputStream());
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    connectionEstablished();

                    while (keepAlive) {
                        try {
                            Thread.sleep(30000);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    connectionClosed();
                }).start();
            }

            @Override
            public void disconnect() {
                keepAlive = false;
                try {
                    socket.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }

            @Override
            public void setupPackage(byte[] data) {
                if (toSend == null) {
                    toSend = data;
                } else {
                    byte[] res = new byte[toSend.length + data.length];
                    System.arraycopy(toSend, 0, res, 0, toSend.length);
                    System.arraycopy(data, 0, res, toSend.length, data.length);
                    toSend = res;
                }
            }

            @Override
            public void sendPackage() {
                if (objectOutputStream != null) {
                    try {
                        //objectOutputStream.writeUnshared(deSerialize(toSend));
                        objectOutputStream.writeUnshared(toSend);
                        toSend = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        objectOutputStream.reset();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private void connectionCreated() {
        final Handler handler = new Handler(Mobile.this.getMainLooper());
        handler.post(() -> {
            try {
                Mobile.this.iService_mobile_to_app.connection_Created();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionEstablished() {
        final Handler handler = new Handler(Mobile.this.getMainLooper());
        handler.post(() -> {
            try {
                Mobile.this.iService_mobile_to_app.connection_Established();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionClosed() {
        final Handler handler = new Handler(Mobile.this.getMainLooper());
        handler.post(() -> {
            try {
                Mobile.this.iService_mobile_to_app.connection_Closed();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            stopSelf();
        });
    }
}

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

import it.unicam.project.multiinterfacesender.DirectlyConnect;
import it.unicam.project.multiinterfacesender.IService_App_to_Mobile;
import it.unicam.project.multiinterfacesender.IService_Mobile_to_App;

public class MobileSend extends Service {

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
    //Aidl stuff
    public final int MOBILE_STATE_CONNECTED = 11;
    public final int MOBILE_STATE_ESTABILISHED = 12;
    public final int MOBILE_STATE_NOT_FOUND = 13;
    public final int MOBILE_STATE_CLOSED = 14;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IService_App_to_Mobile.Stub() {
            @Override
            public void register(IService_Mobile_to_App activity) throws RemoteException {
                MobileSend.this.iService_mobile_to_app = activity;
                MobileSend.this.iService_mobile_to_app.getProcessID(android.os.Process.myPid());
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
                                connectionCreated();
                            } catch (IOException ignored) {
                                connectionRefused();
                            }
                    }
                });
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
                            if(objectInputStream.read()==-1){
                                connectionClosed();
                                disconnect();
                                keepAlive=false;
                            }
                        } catch (IOException e) {
                            connectionClosed();
                            disconnect();
                            keepAlive=false;
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }).start();
            }

            @Override
            public void disconnect() {
                keepAlive = false;
                try {
                    if(socket!=null){
                        socket.close();
                    }
                    stopSelf();
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
        final Handler handler = new Handler(MobileSend.this.getMainLooper());
        handler.post(() -> {
            try {
                MobileSend.this.iService_mobile_to_app.mobileHandler(MOBILE_STATE_CONNECTED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionEstablished() {
        final Handler handler = new Handler(MobileSend.this.getMainLooper());
        handler.post(() -> {
            try {
                MobileSend.this.iService_mobile_to_app.mobileHandler(MOBILE_STATE_ESTABILISHED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionRefused() {
        final Handler handler = new Handler(MobileSend.this.getMainLooper());
        handler.post(() -> {
            try {
                MobileSend.this.iService_mobile_to_app.mobileHandler(MOBILE_STATE_NOT_FOUND);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionClosed() {
        final Handler handler = new Handler(MobileSend.this.getMainLooper());
        handler.post(() -> {
            try {
                MobileSend.this.iService_mobile_to_app.mobileHandler(MOBILE_STATE_CLOSED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}

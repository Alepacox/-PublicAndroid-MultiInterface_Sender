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
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import it.unicam.project.multiinterfacesender.IService_App_to_Wifi;
import it.unicam.project.multiinterfacesender.IService_Wifi_to_App;
import it.unicam.project.multiinterfacesender.Receive.SendedData;

public class WifiReceive extends Service {

    protected IService_Wifi_to_App iService_wifi_to_app;

    private ConnectivityManager connectivityManager;
    private InetAddress serverAddr;
    private int PORT;
    private NetworkRequest.Builder builder;
    private Socket socket;

    private boolean keepAlive = false;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private byte[] toSend;
    public final int WIFI_STATE_CONNECTED = 11;
    public final int WIFI_STATE_ESTABILISHED = 12;
    public final int WIFI_STATE_CLOSED=14;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IService_App_to_Wifi.Stub() {
            @Override
            public void register(IService_Wifi_to_App service) throws RemoteException {
                WifiReceive.this.iService_wifi_to_app = service;
                WifiReceive.this.iService_wifi_to_app.getProcessID(android.os.Process.myPid());
            }

            @Override
            public void createConnection(String ip, int port) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                PORT = port;

                builder = new NetworkRequest.Builder();
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        ConnectivityManager.setProcessDefaultNetwork(network);
                            try {
                                ServerSocket hostServer = new ServerSocket(PORT);
                                socket = hostServer.accept();
                                while (!socket.isBound()){

                                }
                                connectionCreated();
                            } catch (IOException ignored) {
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
                            sendRecievedData((SendedData) objectInputStream.readUnshared());
                            /*if(objectInputStream.read()==-1){
                                connectionClosed();
                                disconnect();
                                keepAlive=false;
                            }*/
                        } catch (IOException e) {
                            connectionClosed();
                            disconnect();
                            keepAlive = false;
                        } catch (ClassNotFoundException | RemoteException e) {
                            e.printStackTrace();
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
        final Handler handler = new Handler(WifiReceive.this.getMainLooper());
        handler.post(() -> {
            try {
                WifiReceive.this.iService_wifi_to_app.wifiHandler(WIFI_STATE_CONNECTED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionEstablished() {
        final Handler handler = new Handler(WifiReceive.this.getMainLooper());
        handler.post(() -> {
            try {
                WifiReceive.this.iService_wifi_to_app.wifiHandler(WIFI_STATE_ESTABILISHED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectionClosed() {
        final Handler handler = new Handler(WifiReceive.this.getMainLooper());
        handler.post(() -> {
            try {
                WifiReceive.this.iService_wifi_to_app.wifiHandler(WIFI_STATE_CLOSED);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }


    private void sendRecievedData(SendedData data) throws RemoteException {
        ArrayList<byte[]> back = splitByteArray(serialize(data), 399860);
        for (byte[] b : back)
            WifiReceive.this.iService_wifi_to_app.setupPackage(b);
        WifiReceive.this.iService_wifi_to_app.packageComplete();
    }

    public ArrayList<byte[]> splitByteArray(byte[] input, int chunkSize) {
        int chunkCount = 0;
        ArrayList<byte[]> res = new ArrayList<>();
        while ((chunkCount * chunkSize) < input.length) {
            byte[] chunk;
            if (((chunkCount * chunkSize) + (chunkSize)) > input.length) {
                chunk = new byte[input.length - (chunkCount * chunkSize)];
                System.arraycopy(input, chunkCount * chunkSize, chunk, 0, input.length - (chunkCount * chunkSize));
            } else {
                chunk = new byte[chunkSize];
                System.arraycopy(input, chunkCount * chunkSize, chunk, 0, chunkSize);
            }
            res.add(chunk);
            chunkCount++;
        }
        return res;
    }

    public byte[] serialize(SendedData in) {
        byte[] out = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput objOut;
        try {
            objOut = new ObjectOutputStream(bos);
            objOut.writeObject(in);
            objOut.flush();
            out = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return out;
    }

    public SendedData deSerialize(byte[] in) {
        SendedData out = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        ObjectInput objIn = null;
        try {
            objIn = new ObjectInputStream(bis);
            out = (SendedData) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objIn != null) {
                    objIn.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return out;
    }
}

package it.unicam.project.multiinterfacesender.Receive;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import it.unicam.project.multiinterfacesender.IService_App_to_Mobile;
import it.unicam.project.multiinterfacesender.IService_App_to_Wifi;
import it.unicam.project.multiinterfacesender.IService_Mobile_to_App;
import it.unicam.project.multiinterfacesender.IService_Wifi_to_App;
import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.Service.BluetoothReceive;
import it.unicam.project.multiinterfacesender.Service.MobileReceive;
import it.unicam.project.multiinterfacesender.Service.WifiReceive;

import static it.unicam.project.multiinterfacesender.Service.BluetoothReceive.BT_RECEIVE_MESSAGE_STATE_CHANGE;


public class Receive_loading extends AppCompatActivity {
    private TextView localAddress;
    private TextView bluetoothAddress;
    private TextView localAddressText;
    private TextView bluetoothAddressText;
    private TextView textGeneratedCode;
    private TextView generatedCode;
    private TextView shareSession;
    private TextView downloadingFileText;
    private TextView downloadingFile;
    private TextView mobileStatus;
    private TextView mobileStatusText;
    private boolean blocked;
    private int numberOfUsedInterface = 0;
    private boolean usingWifi;
    private boolean usingMobile;
    private boolean usingBluetooth;
    private String wifiIp;
    private String bluetoothName;
    //AIDL stuff
    private final String serverIp = "35.180.118.235";
    private final int mobileport = 3306;
    private int port = 50000;
    private ServiceConnection wifiServiceConnection;
    private IService_App_to_Wifi iService_app_to_wifi;
    private IService_Wifi_to_App iService_wifi_to_app;
    private ServiceConnection mobileServiceConnection;
    private IService_App_to_Mobile iService_app_to_mobile;
    private IService_Mobile_to_App iService_mobile_to_app;
    private volatile boolean wifi_connectionCreated = false;
    private volatile boolean wifi_connectionEstablished = false;
    private volatile boolean wifi_connectionRefused = false;
    private volatile boolean mobile_connectionCreated = false;
    private volatile boolean mobile_connectionEstablished = false;
    private volatile boolean mobile_connectionRefused = false;
    private int wifiProcessID;
    private int mobileProcessID;

    private String received_FileName;
    private ArrayList<CacheElement> chunks = new ArrayList<>();
    private boolean bt_trans_done = true, wf_trans_done = true, mo_trans_done = true;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_RECEIVE_MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothReceive.BT_RECEIVE_STATE_CONNECTED:
                            Log.e("BT STATE", "CONNECTED");
                            setMessage("Connessione Bluetooth stabilita");
                            break;
                        case BluetoothReceive.BT_RECEIVE_STATE_ERROR:
                            setMessage("C'è stato un problema con la connessione Bluetooth");
                            Log.e("BT STATE", "ERROR");
                            lostInterface();
                            break;
                        case BluetoothReceive.BT_RECEIVE_STATE_LOST_CONNECTION:
                            lostInterface();
                            Log.e("BT STATE", "LOST CONNECTION");
                            setMessage("Ho perso la connessione con il dispositivo Bluetooth");
                            break;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
        }
        Intent intent = getIntent();
        boolean manual = intent.getBooleanExtra("receivingManual", true);
        wifiIp = intent.getStringExtra("wifiIp");
        bluetoothName = intent.getStringExtra("bluetoothName");
        usingMobile = Boolean.valueOf(intent.getStringExtra("mobileIp"));
        if (usingMobile) numberOfUsedInterface += 1;
        localAddress = findViewById(R.id.downloading_ip_local);
        bluetoothAddress = findViewById(R.id.downloading_bluetooth);
        mobileStatus = findViewById(R.id.downloading_remote_status);
        localAddressText = findViewById(R.id.downloading_ip_local_text);
        bluetoothAddressText = findViewById(R.id.downloading_bluetooth_text);
        mobileStatusText = findViewById(R.id.downloading_mobile_text);
        textGeneratedCode = findViewById(R.id.session_code_text);
        shareSession = findViewById(R.id.text_share_session);
        generatedCode = findViewById(R.id.session_code);
        downloadingFileText = findViewById(R.id.downloading_filename_text);
        downloadingFile = findViewById(R.id.downloading_filename);
        blocked = true;
        if (!wifiIp.equals("null")) {
            usingWifi = true;
            numberOfUsedInterface += 1;
        } else usingWifi = false;
        if (!bluetoothName.equals("null")) {
            usingBluetooth = true;
            numberOfUsedInterface += 1;
        } else usingBluetooth = false;

        if (manual) {
            setManualView();
        } else {
            String sessionCode = intent.getStringExtra("sessioncode");
            setAutoView(sessionCode);
        }
        if (usingWifi) {
            wf_trans_done = false;
            startReceivingOnWifi();
        }
        if (usingMobile) {
            mo_trans_done = false;
            startReceivingOnMobile();
        }
        if (usingBluetooth) {
            bt_trans_done = false;
            BluetoothReceive btReceive = new BluetoothReceive(mHandler, new BluetoothReceive.OnMessageReceived() {
                @Override
                public void messageReceived(SendedData message) {
                    if (message.fileName != null && message.fileName != "")
                        received_FileName = message.fileName;

                    //non abbiamo ram infinita,  quindi c'è da scrivere sti pezzi di file a parte e salvarsi solo il ref,
                    //poi alla scrittura si prende un pezzo alla volta e si mette nel file finale eliminando sti pezzi uno a uno una volta scritti
                    if (message.fileChunk != new byte[0] && message.fileChunk != null)
                        chunks.add(new CacheElement(
                                Arrays.copyOfRange(message.fileChunk, 0, message.fileChunk.length - 2),
                                Arrays.copyOfRange(message.fileChunk, message.fileChunk.length - 2, message.fileChunk.length)
                        ));

                    if (message.fileChunk == new byte[0]) {
                        bt_trans_done = true;
                        tryWriteFile();
                    }
                }
            });
            btReceive.connect();
        }
    }


    private byte[] wifiTMP;
    public void startReceivingOnWifi() {
        Intent wifiIntent = new Intent(this, WifiReceive.class);
        wifiServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iService_app_to_wifi = IService_App_to_Wifi.Stub.asInterface(iBinder);
                iService_wifi_to_app = new IService_Wifi_to_App.Stub() {
                    @Override
                    public void wifiHandler(int code) throws RemoteException {
                        switch (code) {
                            case 11:
                                wifi_connectionCreated = true;
                                break;
                            case 12:
                                wifi_connectionEstablished = true;
                                break;
                            case 14:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        wifi_connectionRefused = true;
                                        lostInterface();
                                        setMessage("Ho perso la connessione con il dispositivo in WiFi");
                                    }
                                });
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        wifiProcessID = code;
                    }

                    @Override
                    public void setupPackage(byte[] data) throws RemoteException {
                        if (wifiTMP == null) {
                            wifiTMP = data;
                        } else {
                            byte[] res = new byte[wifiTMP.length + data.length];
                            System.arraycopy(wifiTMP, 0, res, 0, wifiTMP.length);
                            System.arraycopy(data, 0, res, wifiTMP.length, data.length);
                            wifiTMP = res;
                        }
                    }

                    @Override
                    public void packageComplete() throws RemoteException {
                        SendedData message = deSerialize(wifiTMP);
                        wifiTMP = null;

                        if (message.fileName != null && message.fileName != "")
                            received_FileName = message.fileName;

                        //non abbiamo ram infinita,  quindi c'è da scrivere sti pezzi di file a parte e salvarsi solo il ref,
                        //poi alla scrittura si prende un pezzo alla volta e si mette nel file finale eliminando sti pezzi uno a uno una volta scritti
                        if (message.fileChunk != new byte[0] && message.fileChunk != null)
                            chunks.add(new CacheElement(
                                    Arrays.copyOfRange(message.fileChunk, 0, message.fileChunk.length - 2),
                                    Arrays.copyOfRange(message.fileChunk, message.fileChunk.length - 2, message.fileChunk.length)
                            ));

                        if (message.fileChunk == new byte[0]) {
                            wf_trans_done = true;
                            tryWriteFile();
                        }

                    }
                };
                try {
                    iService_app_to_wifi.register(iService_wifi_to_app);
                    new Thread(() -> {
                        try {
                            iService_app_to_wifi.createConnection(null, port);
                            while (!wifi_connectionCreated) {
                                if (wifi_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            iService_app_to_wifi.connect();
                            while (!wifi_connectionEstablished) {
                                if (wifi_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (wifi_connectionRefused) return;
                                    setMessage("Connesso al dispositivo in WiFi");
                                }
                            });
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(wifiIntent, wifiServiceConnection, Context.BIND_IMPORTANT);
        startService(wifiIntent);
    }

    private byte[] mobileTMP;
    public void startReceivingOnMobile() {
        Intent mobileIntent = new Intent(this, MobileReceive.class);
        mobileServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iService_app_to_mobile = IService_App_to_Mobile.Stub.asInterface(iBinder);
                iService_mobile_to_app = new IService_Mobile_to_App.Stub() {
                    @Override
                    public void mobileHandler(int code) throws RemoteException {
                        switch (code) {
                            case 11:
                                mobile_connectionCreated = true;
                                break;
                            case 12:
                                mobile_connectionEstablished = true;
                                break;
                            case 13:
                                mobile_connectionRefused = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lostInterface();
                                        setMessage("Non sono riuscito a connettermi al server");
                                    }
                                });
                                break;
                            case 14:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mobile_connectionRefused = true;
                                        lostInterface();
                                        setMessage("Ho perso la connessione con il dispositivo sulla rete mobile");
                                    }
                                });
                                break;
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        mobileProcessID = code;
                    }

                    @Override
                    public void setupPackage(byte[] data) throws RemoteException {
                        if (mobileTMP == null) {
                            mobileTMP = data;
                        } else {
                            byte[] res = new byte[mobileTMP.length + data.length];
                            System.arraycopy(mobileTMP, 0, res, 0, mobileTMP.length);
                            System.arraycopy(data, 0, res, mobileTMP.length, data.length);
                            mobileTMP = res;
                        }
                    }

                    @Override
                    public void packageComplete() throws RemoteException {
                        SendedData message = deSerialize(mobileTMP);
                        mobileTMP = null;

                        if (message.fileName != null && message.fileName != "")
                            received_FileName = message.fileName;

                        //non abbiamo ram infinita,  quindi c'è da scrivere sti pezzi di file a parte e salvarsi solo il ref,
                        //poi alla scrittura si prende un pezzo alla volta e si mette nel file finale eliminando sti pezzi uno a uno una volta scritti
                        if (message.fileChunk != new byte[0] && message.fileChunk != null)
                            chunks.add(new CacheElement(
                                    Arrays.copyOfRange(message.fileChunk, 0, message.fileChunk.length - 2),
                                    Arrays.copyOfRange(message.fileChunk, message.fileChunk.length - 2, message.fileChunk.length)
                            ));

                        if (message.fileChunk == new byte[0]) {
                            mo_trans_done = true;
                            tryWriteFile();
                        }

                    }
                };
                try {
                    iService_app_to_mobile.register(iService_mobile_to_app);
                    new Thread(() -> {
                        try {
                            iService_app_to_mobile.createConnection(serverIp, mobileport);
                            while (!mobile_connectionCreated) {
                                if (mobile_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            iService_app_to_mobile.connect();
                            while (!mobile_connectionEstablished) {
                                if (mobile_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mobile_connectionRefused) return;
                                    setMessage("Connesso al dispositivo attraverso la rete mobile.");
                                }
                            });
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(mobileIntent, mobileServiceConnection, Context.BIND_IMPORTANT);
        startService(mobileIntent);
    }

    public void setMessage(String message) {
        MainActivity.snackBarNav(this, R.id.container_receive_loading, message, Snackbar.LENGTH_LONG, 1);
    }

    public void setAutoView(final String sessionCode) {
        textGeneratedCode.setVisibility(View.VISIBLE);
        generatedCode.setVisibility(View.VISIBLE);
        shareSession.setVisibility(View.VISIBLE);
        generatedCode.setText(sessionCode);
        shareSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "MultiInterface Sender: il mio codice di connessione è " + sessionCode);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    public void lostInterface() {
        numberOfUsedInterface -= 1;
        if (numberOfUsedInterface == 0) {
            doubleSkip = true;
            onBackPressed();
        }
    }

    public void setDownloadingView(boolean comesFromManual, String downloadingFileName) {
        //Making downloading file visible
        if (comesFromManual) {
            localAddress.setVisibility(View.INVISIBLE);
            bluetoothAddress.setVisibility(View.INVISIBLE);
            localAddressText.setVisibility(View.INVISIBLE);
            bluetoothAddressText.setVisibility(View.INVISIBLE);
            mobileStatus.setVisibility(View.INVISIBLE);
            mobileStatusText.setVisibility(View.INVISIBLE);
        } else {
            textGeneratedCode.setVisibility(View.INVISIBLE);
            generatedCode.setVisibility(View.INVISIBLE);
            shareSession.setVisibility(View.INVISIBLE);
        }
        downloadingFile.setVisibility(View.VISIBLE);
        downloadingFileText.setVisibility(View.VISIBLE);
        downloadingFile.setText(downloadingFileName);
    }

    public void setManualView() {
        if (usingWifi) {
            localAddress.setText(wifiIp);
        }
        if (usingBluetooth) {
            bluetoothAddress.setText(bluetoothName);
        }
        if (usingMobile) {
            mobileStatus.setText("On");
        }
        localAddress.setVisibility(View.VISIBLE);
        bluetoothAddress.setVisibility(View.VISIBLE);
        localAddressText.setVisibility(View.VISIBLE);
        bluetoothAddressText.setVisibility(View.VISIBLE);
        mobileStatus.setVisibility(View.VISIBLE);
        mobileStatusText.setVisibility(View.VISIBLE);
    }

    boolean doubleBackToExitPressedOnce = false;
    boolean doubleSkip = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || doubleSkip) {
            if (usingWifi) {
                try {
                    iService_app_to_wifi.disconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(wifiServiceConnection);
                android.os.Process.killProcess(wifiProcessID);
            }
            if (usingMobile) {
                try {
                    iService_app_to_mobile.disconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(mobileServiceConnection);
                android.os.Process.killProcess(mobileProcessID);
            }
            if (usingBluetooth) {
                BluetoothReceive.clear();
            }
            if (doubleSkip) {
                Toast.makeText(this, "Tutte le interfacce si sono disconnesse. Connessione annullata", Toast.LENGTH_SHORT).show();
            }
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        MainActivity.snackBarNav(this, R.id.container_receive_loading,
                "Premi ancora indietro per chiudere la connessione", Snackbar.LENGTH_SHORT, 1);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    private void tryWriteFile() {
        if (bt_trans_done && wf_trans_done && mo_trans_done) {
            bt_trans_done = wf_trans_done = mo_trans_done = false;
            //SCRIVI FILE SU DISCO

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                chunks.sort(new CacheElementSorter());
            }else{
                //sortalo per chi ha android merda < N
            }
            FileOutputStream writer;
            try {
                writer = new FileOutputStream(received_FileName);
                for (CacheElement chunk : chunks) {
                    writer.write(chunk.getFileChunk());
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            chunks.clear();
            received_FileName = null;
        }
    }

    static class CacheElement {
        private byte[] fileChunk;
        private byte[] index;

        CacheElement(byte[] fileChunk, byte[] index) {
            this.fileChunk = fileChunk;
            this.index = index;
        }

        byte[] getFileChunk() {
            return fileChunk;
        }

        byte[] getIndex() {
            return index;
        }
    }

    public static class CacheElementSorter implements Comparator<CacheElement> {

        public int compare(CacheElement one, CacheElement another) {
            int returnVal = 0;

            if (one.getIndex()[0] < another.getIndex()[0])
                returnVal = -1;
            else if (one.getIndex()[0] > another.getIndex()[0])
                returnVal = 1;
            else if (one.getIndex()[0] == another.getIndex()[0]) {
                if (one.getIndex()[1] < another.getIndex()[1])
                    returnVal = -1;
                else if (one.getIndex()[1] > another.getIndex()[1])
                    returnVal = 1;
            }
            return returnVal;

        }
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

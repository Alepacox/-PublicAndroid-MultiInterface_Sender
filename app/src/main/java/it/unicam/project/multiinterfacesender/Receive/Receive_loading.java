package it.unicam.project.multiinterfacesender.Receive;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import it.unicam.project.multiinterfacesender.IService_App_to_Mobile;
import it.unicam.project.multiinterfacesender.IService_App_to_Wifi;
import it.unicam.project.multiinterfacesender.IService_Mobile_to_App;
import it.unicam.project.multiinterfacesender.IService_Wifi_to_App;
import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.Service.MobileReceive;
import it.unicam.project.multiinterfacesender.Service.WifiReceive;


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
    private int numberOfUsedInterface=0;
    private boolean usingWifi;
    private boolean usingMobile;
    private boolean usingBluetooth;
    private String wifiIp;
    private String bluetoothName;
    //AIDL stuff
    private final String serverIp="35.180.118.235";
    private final int mobileport=3306;
    private int port= 50000;
    private ServiceConnection wifiServiceConnection;
    private IService_App_to_Wifi iService_app_to_wifi;
    private IService_Wifi_to_App iService_wifi_to_app;
    private ServiceConnection mobileServiceConnection;
    private IService_App_to_Mobile iService_app_to_mobile;
    private IService_Mobile_to_App iService_mobile_to_app;
    private volatile boolean wifi_connectionCreated=false;
    private volatile boolean wifi_connectionEstablished=false;
    private volatile boolean wifi_connectionRefused=false;
    private volatile boolean mobile_connectionCreated=false;
    private volatile boolean mobile_connectionEstablished=false;
    private volatile boolean mobile_connectionRefused=false;
    private int wifiProcessID;
    private int mobileProcessID;

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
        boolean manual= intent.getBooleanExtra("receivingManual", true);
        wifiIp = intent.getStringExtra("wifiIp");
        bluetoothName = intent.getStringExtra("bluetoothName");
        usingMobile = Boolean.valueOf(intent.getStringExtra("mobileIp"));
        if(usingMobile) numberOfUsedInterface+=1;
        localAddress = findViewById(R.id.downloading_ip_local);
        bluetoothAddress = findViewById(R.id.downloading_bluetooth);
        mobileStatus = findViewById(R.id.downloading_remote_status);
        localAddressText = findViewById(R.id.downloading_ip_local_text);
        bluetoothAddressText = findViewById(R.id.downloading_bluetooth_text);
        mobileStatusText = findViewById(R.id.downloading_mobile_text);
        textGeneratedCode= findViewById(R.id.session_code_text);
        shareSession= findViewById(R.id.text_share_session);
        generatedCode= findViewById(R.id.session_code);
        downloadingFileText = findViewById(R.id.downloading_filename_text);
        downloadingFile = findViewById(R.id.downloading_filename);
        blocked=true;
        if (!wifiIp.equals("null")) {
            usingWifi=true;
            numberOfUsedInterface+=1;
        } else usingWifi=false;
        if (!bluetoothName.equals("null")) {
            usingBluetooth=true;
            numberOfUsedInterface+=1;
        }else usingBluetooth=false;

        if(manual) {
            setManualView();
        } else {
            String sessionCode= intent.getStringExtra("sessioncode");
            setAutoView(sessionCode);
        }
        if(usingWifi){
            startReceivingOnWifi();
        }
        if(usingMobile){
            startReceivingOnMobile();
        }
    }

    public void startReceivingOnWifi(){
        Intent wifiIntent= new Intent(this, WifiReceive.class);
        wifiServiceConnection= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iService_app_to_wifi = IService_App_to_Wifi.Stub.asInterface(iBinder);
                iService_wifi_to_app = new IService_Wifi_to_App.Stub() {
                    @Override
                    public void wifiHandler(int code) throws RemoteException {
                        switch (code){
                            case 11:
                                wifi_connectionCreated=true;
                                break;
                            case 12:
                                wifi_connectionEstablished=true;
                                break;
                            case 14:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        wifi_connectionRefused=true;
                                        setMessage("Ho perso la connessione con il dispositivo in WiFi");
                                    }
                                });
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        wifiProcessID=code;
                    }
                };
                try {
                    iService_app_to_wifi.register(iService_wifi_to_app);
                    new Thread(() -> {
                        try {
                            iService_app_to_wifi.createConnection(null, port);
                            while (!wifi_connectionCreated) {
                                if(wifi_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            iService_app_to_wifi.connect();
                            while (!wifi_connectionEstablished) {
                                if(wifi_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(wifi_connectionRefused) return;
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

    public void startReceivingOnMobile(){
        Intent mobileIntent= new Intent(this, MobileReceive.class);
        mobileServiceConnection= new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                iService_app_to_mobile = IService_App_to_Mobile.Stub.asInterface(iBinder);
                iService_mobile_to_app = new IService_Mobile_to_App.Stub() {
                    @Override
                    public void mobileHandler(int code) throws RemoteException {
                        switch (code){
                            case 11:
                                mobile_connectionCreated=true;
                                break;
                            case 12:
                                mobile_connectionEstablished=true;
                                break;
                            case 13:
                                mobile_connectionRefused=true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setMessage("Non sono riuscito a connettermi con il server");
                                    }
                                });
                                break;
                            case 14:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mobile_connectionRefused=true;
                                        setMessage("Ho perso la connessione con il dispositivo sulla rete mobile");
                                    }
                                });
                                break;
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        mobileProcessID=code;
                    }
                };
                try {
                    iService_app_to_mobile.register(iService_mobile_to_app);
                    new Thread(() -> {
                        try {
                            iService_app_to_mobile.createConnection(serverIp, mobileport);
                            while (!mobile_connectionCreated) {
                                if(mobile_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            iService_app_to_mobile.connect();
                            while (!mobile_connectionEstablished) {
                                if(mobile_connectionRefused) return;
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mobile_connectionRefused) return;
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

    public void setMessage(String message){
        MainActivity.snackBarNav(this, R.id.container_receive_loading, message, Snackbar.LENGTH_LONG, 1);
    }
    public void setAutoView(final String sessionCode){
        textGeneratedCode.setVisibility(View.VISIBLE);
        generatedCode.setVisibility(View.VISIBLE);
        shareSession.setVisibility(View.VISIBLE);
        generatedCode.setText(sessionCode);
        shareSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "MultiInterface Sender: il mio codice di connessione è "+ sessionCode);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    public void setDownloadingView(boolean comesFromManual, String downloadingFileName){
        //Making downloading file visible
        if(comesFromManual){
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
    public void setManualView(){
        if (usingWifi) {
            localAddress.setText(wifiIp);
        }
        if (usingBluetooth) {
            bluetoothAddress.setText(bluetoothName);
        }
        if (usingMobile){
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            if(usingWifi){
                try {
                    iService_app_to_wifi.disconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(wifiServiceConnection);
                android.os.Process.killProcess(wifiProcessID);
            }
            if(usingMobile){
                try {
                    iService_app_to_mobile.disconnect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                unbindService(mobileServiceConnection);
                android.os.Process.killProcess(mobileProcessID);
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
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

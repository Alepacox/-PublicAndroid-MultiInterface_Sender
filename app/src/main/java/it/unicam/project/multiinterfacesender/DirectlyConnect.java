package it.unicam.project.multiinterfacesender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import it.unicam.project.multiinterfacesender.Send.Send_loading;
import it.unicam.project.multiinterfacesender.Send.Send_step_2;
import it.unicam.project.multiinterfacesender.Service.BluetoothSend;
import it.unicam.project.multiinterfacesender.Service.MobileSend;
import it.unicam.project.multiinterfacesender.Service.WifiSend;

import static it.unicam.project.multiinterfacesender.Service.BluetoothSend.*;

public class DirectlyConnect {
    private Activity myActivity;
    private Fragment currentFragment;
    private String btname;
    private String wifiip;
    private String wifiSSID;
    private boolean mobileconnection;
    private TextView stepmessage;
    private TextView retryText;
    private TextView ignoreText;
    private ProgressBar progress;
    private FragmentManager fm;
    private AlertDialog alertdialog;
    //Connection
    public static BluetoothSend bluetoothSendService;
    private WifiManager wifiManager;
    private boolean foundBTdevice;
    private int currentStep;
    private int skippableInterfaces;
    private boolean alreadyPaired;
    private final String serverIp="35.180.118.235";
    private final int mobileport=3306;
    private final int wifiport = 50000;
    private boolean wifiHandlerRegistered=false;
    private boolean btHandlerRegistered=false;
    public static boolean usingWifi;
    public static boolean usingBluetooth;
    public static boolean usingMobile;
    //AIDL stuff
    public static ServiceConnection wifiServiceConnection;
    public static ServiceConnection mobileServiceConnection;
    public static int wifiProcessID;
    public static int mobileProcessID;
    public static IService_App_to_Wifi iService_app_to_wifi;
    private IService_Wifi_to_App iService_wifi_to_app;
    public static IService_App_to_Mobile iService_app_to_mobile;
    private IService_Mobile_to_App iService_mobile_to_app;
    private volatile boolean wifi_connectionCreated=false;
    private volatile boolean wifi_connectionEstablished=false;
    private volatile boolean wifi_connectionRefused=false;
    private volatile boolean mobile_connectionCreated=false;
    private volatile boolean mobile_connectionEstablished=false;
    private volatile boolean mobile_connectionRefused=false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_SEND_MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothSend.BT_SEND_STATE_CONNECTED:
                            btHandlerRegistered=false;
                            checkStep3();
                            break;
                        case BluetoothSend.BT_SEND_STATE_NOT_FOUND:
                            handleSomethingWrong("Disposito bluetooth non raggiungibile");
                            break;
                        case BluetoothSend.BT_SEND_STATE_LOST_CONNECTION:
                            if(btHandlerRegistered)myActivity.unregisterReceiver(bluetoothReceiver);
                            skippableInterfaces-=1;
                            if(skippableInterfaces==0){
                                //Send_loading.mHandler.sendMessage()
                                currentFragment.getActivity().getSupportFragmentManager().popBackStack();
                                MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la con il dispositivo bluetooth. " +
                                        "Non ci sono altre interfacce da utilizzare. Connessione annullata", Snackbar.LENGTH_LONG, 0);
                            } else MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la con il dispositivo bluetooth. " +
                                    "Sto utilizzando le interfacce rimanenti.", Snackbar.LENGTH_LONG, 0);
                    }
                    break;
            }
        }
    };
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            myActivity.unregisterReceiver(this);
            wifiHandlerRegistered=false;
            boolean foundNetwork = false;
            for (ScanResult scanResult : results) {
                if (wifiSSID.equals("\"" + scanResult.SSID + "\"")) {
                    foundNetwork = true;
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    boolean foundConfiguration = false;
                    for (WifiConfiguration i : list) {
                        if (i.SSID != null && i.SSID.equals(wifiSSID)) {
                            foundConfiguration = true;
                            WifiInfo info = wifiManager.getConnectionInfo();
                            int id = info.getNetworkId();
                            wifiManager.disconnect();
                            wifiManager.disableNetwork(id);
                            wifiManager.enableNetwork(i.networkId, true);
                            wifiManager.reconnect();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    connectToDeviceOnWifi();
                                }
                            }, 1500);
                            break;
                        }
                    }
                    if (!foundConfiguration) {
                        wifiManager.disconnect();
                        Toast.makeText(myActivity, "Inserisci le credenziali per connetterti a " + wifiSSID, Toast.LENGTH_LONG).show();
                        myActivity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        stepmessage.setText("Hai inserito le credenziali? Riprova per connetterti");
                    }
                }
            }
            if (!foundNetwork) {
                stepmessage.setText("Non sono riuscito a trovare la rete " + wifiSSID);
                progress.setVisibility(View.INVISIBLE);
                retryText.setVisibility(View.VISIBLE);
                retryText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkStep1();
                    }
                });
                ignoreText.setVisibility(View.VISIBLE);
                ignoreText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(avoidConnection()) return;
                        checkStep2();
                    }
                });
            }
        }
    };
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().equals(btname)) {
                    foundBTdevice = true;
                    myActivity.registerReceiver(pairedReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                    Class class1 = null;
                    try {
                        class1 = Class.forName("android.bluetooth.BluetoothDevice");
                        Method createBondMethod = class1.getMethod("createBond");
                        createBondMethod.invoke(device);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !foundBTdevice) {
                handleSomethingWrong("Non sono stato in grado di trovare " + btname);
            }
        }
    };
    private BroadcastReceiver pairedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                myActivity.unregisterReceiver(this);
                btHandlerRegistered=false;
                bluetoothSendService.connect(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                stepmessage.setText("Accoppiamento in corso");
            }
            else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                handleSomethingWrong("Ho bisogno di accoppiarmi con il dispositivo per comunicare con lui");
            }
        }
    };

    public DirectlyConnect(Fragment currentFragment, String btname,
                           String wifiip, String wifiSSID, boolean mobileconnection) {
        this.myActivity = currentFragment.getActivity();
        this.btname = btname;
        this.wifiip=wifiip;
        this.wifiSSID = wifiSSID;
        this.mobileconnection = mobileconnection;
        this.fm = currentFragment.getActivity().getSupportFragmentManager();
        this.currentFragment= currentFragment;
        this.bluetoothSendService =new BluetoothSend(mHandler);
    }

    public void startDirectylyConnection() {
        LayoutInflater li = LayoutInflater.from(myActivity);
        View toInflate = li.inflate(R.layout.connecting_step_layout, null);
        stepmessage = toInflate.findViewById(R.id.connecting_step_message);
        retryText = toInflate.findViewById(R.id.connecting_step_retry);
        ignoreText = toInflate.findViewById(R.id.connecting_step_ignore);
        progress = toInflate.findViewById(R.id.connecting_step_prog);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                myActivity);
        alertDialogBuilder.setView(toInflate);
        alertDialogBuilder.setCancelable(false);
        alertdialog = alertDialogBuilder.create();
        alertdialog.show();
        this.skippableInterfaces=3;
        for (int i=0; i<3; i++){
            switch (i){
                case 0: if(btname==null) skippableInterfaces-=1;
                break;
                case 1: if(wifiip==null) skippableInterfaces-=1;
                break;
                case 2: if(!mobileconnection) skippableInterfaces-=1;
            }
        }
        Log.e("SKIPPABLE", String.valueOf(skippableInterfaces));
        checkStep1();
    }

    public void checkStep1() {
        //WifiSend
        if (wifiSSID != null) {
            usingWifi=true;
            currentStep=1;
            progress.setVisibility(View.VISIBLE);
            retryText.setVisibility(View.INVISIBLE);
            ignoreText.setVisibility(View.INVISIBLE);
            stepmessage.setText("Sto controllando la connessione Wi-Fi");
            wifiManager = (WifiManager) myActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager connectionManager = (ConnectivityManager) myActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifiCheck.isConnected()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiSSID.equals(wifiInfo.getSSID())) {
                            connectToDeviceOnWifi();
                        } else {
                            stepmessage.setText("Mi sto connettendo alla rete " + wifiSSID);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                                    wifiHandlerRegistered=true;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            currentFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 5489);
                                        } else wifiManager.startScan();
                                    } else wifiManager.startScan();
                                }
                            }, 1000);
                        }
                    } else {
                        stepmessage.setText("Mi sto connettendo alla rete " + wifiSSID);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                myActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                                wifiHandlerRegistered=true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        currentFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 5489);
                                    } else wifiManager.startScan();
                                } else wifiManager.startScan();
                            }
                        }, 1000);
                    }
                }
            }, 1500);
        } else checkStep2();

    }

    public void connectToDeviceOnWifi(){
        stepmessage.setText("Sto cercando di trovare il dispositivo sulla wifi");
        Intent wifiIntent= new Intent(myActivity, WifiSend.class);
        MainActivity.wifiServiceIntent=wifiIntent;
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
                            case 13:
                                wifi_connectionRefused=true;
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleSomethingWrong("Non sono stato in grado di connettermi al dispositivo");
                                    }
                                });
                                break;
                            case 14:
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    wifi_connectionRefused=true;
                                    skippableInterfaces-=1;
                                    if(skippableInterfaces ==0){
                                        currentFragment.getActivity().getSupportFragmentManager().popBackStack();
                                        MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la con il dispositivo in wifi. " +
                                                "Non ci sono altre interfacce da utilizzare. Connessione annullata", Snackbar.LENGTH_LONG, 0);
                                    } else MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la con il dispositivo in wifi. " +
                                            "Sto utilizzando le interfacce rimanenti.", Snackbar.LENGTH_LONG, 0);
                                }
                            });
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        wifiProcessID=code;
                    }

                    @Override
                    public void setupPackage(byte[] data) throws RemoteException {

                    }

                    @Override
                    public void packageComplete() throws RemoteException {

                    }
                };
                try {
                    iService_app_to_wifi.register(iService_wifi_to_app);
                    new Thread(() -> {
                        try {
                            iService_app_to_wifi.createConnection(wifiip, wifiport);
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
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(wifi_connectionRefused) return;
                                    checkStep2();
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
        myActivity.bindService(wifiIntent, wifiServiceConnection, Context.BIND_IMPORTANT);
        myActivity.startService(wifiIntent);
    }
    public static void disconnectWifi(){
        try {
            iService_app_to_wifi.disconnect();
        } catch (RemoteException e) {
        }
    }

    public void checkStep2() {
        //BluetoothSend
        if (btname != null) {
            currentStep=2;
            usingBluetooth=true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            myActivity.registerReceiver(bluetoothReceiver, filter);
            btHandlerRegistered=true;
            BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> bondedDevice = btadapter.getBondedDevices();
            alreadyPaired = false;
            BluetoothDevice pairedDevice= null;
            for (BluetoothDevice i : bondedDevice) {
                if (btname.equals(i.getName())) {
                    alreadyPaired = true;
                    pairedDevice=i;
                }
            }
            progress.setVisibility(View.VISIBLE);
            retryText.setVisibility(View.INVISIBLE);
            ignoreText.setVisibility(View.INVISIBLE);
            stepmessage.setText("Sto cercando il dispositivo bluetooth " + btname);
            foundBTdevice = false;
            if (!btadapter.isEnabled()) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                currentFragment.startActivityForResult(discoverableIntent, 5452);
            } else if (!alreadyPaired) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            currentFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 5489);
                        } else btadapter.startDiscovery();
                    } else btadapter.startDiscovery();
                } else {
                BluetoothDevice finalPairedDevice = pairedDevice;
                new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothSendService.connect(finalPairedDevice);
                        }
                    }, 1500);
                }
        } else checkStep3();
    }

    public void handleGuaranteedPermission(boolean bluetooth) {
        if(bluetooth){
            if (!alreadyPaired) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        currentFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 5489);
                    } else {
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        myActivity.registerReceiver(bluetoothReceiver, filter);
                        btHandlerRegistered=true;
                        BluetoothAdapter.getDefaultAdapter().startDiscovery();
                    }
                } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
            } else {
                myActivity.unregisterReceiver(bluetoothReceiver);
                btHandlerRegistered=false;
                //CREATE SOCKET CONNECTION
                checkStep3();
            }
        } else {
            if(currentStep==1){
                myActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiHandlerRegistered=true;
                wifiManager.startScan();
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                myActivity.registerReceiver(bluetoothReceiver, filter);
                btHandlerRegistered=true;
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            }
        }
    }

    public void handleSomethingWrong(String message) {
        stepmessage.setText(message);
        progress.setVisibility(View.INVISIBLE);
        retryText.setVisibility(View.VISIBLE);
        ignoreText.setVisibility(View.VISIBLE);
        ignoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentStep==1){
                    usingWifi=false;
                    if(wifiHandlerRegistered) myActivity.unregisterReceiver(wifiReceiver);
                    if(avoidConnection()) return;
                    checkStep2();
                } else if(currentStep==2) {
                    usingBluetooth=false;
                    if(btHandlerRegistered) myActivity.unregisterReceiver(bluetoothReceiver);
                    if(avoidConnection()) return;
                    checkStep3();
                } else {
                    usingMobile=false;
                    if(avoidConnection()) return;
                    alertdialog.dismiss();
                    fm.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                            .replace(R.id.send_container, new Send_step_2(), "Send_step_2")
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        retryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentStep==1){
                    checkStep1();
                } else checkStep2();

            }
        });
    }

    public void checkStep3() {
        //MobileSend
        if (mobileconnection) {
            currentStep=3;
            usingMobile=true;
            progress.setVisibility(View.VISIBLE);
            stepmessage.setText("Sto verificando che i tuoi dati mobili siano attivi ");
            retryText.setVisibility(View.INVISIBLE);
            ignoreText.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager cm = (ConnectivityManager) myActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    try {
                        boolean isDataEnabled = false;
                        try {
                            Class<?> c = Class.forName(cm.getClass().getName());
                            Method m = c.getDeclaredMethod("getMobileDataEnabled");
                            m.setAccessible(true);
                            isDataEnabled = (Boolean) m.invoke(cm);
                        } catch (NoSuchMethodException e) {
                            isDataEnabled = Settings.Global.getInt(myActivity.getContentResolver(), "mobile_data", 0) == 1;
                        }
                        if (!isDataEnabled) {
                            Toast.makeText(myActivity, "Attiva la rete mobile per procedere", Toast.LENGTH_LONG).show();
                            myActivity.startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity")));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stepmessage.setText("Sei hai attivato i dati, riprova la connessione");
                                    progress.setVisibility(View.INVISIBLE);
                                    retryText.setVisibility(View.VISIBLE);
                                    retryText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            checkStep3();
                                        }
                                    });
                                    ignoreText.setVisibility(View.VISIBLE);
                                    ignoreText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(avoidConnection()) return;
                                            usingMobile=false;
                                            alertdialog.dismiss();
                                            fm.beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                                    .replace(R.id.send_container, new Send_step_2(), "Send_step_2")
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    });
                                }
                            }, 2000);
                        } else {
                            connectToServerOnMobile();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1500);
        } else {
            alertdialog.dismiss();
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.send_container, new Send_step_2(), "Send_step_2")
                    .addToBackStack(null)
                    .commit();
        }
    }
    public void connectToServerOnMobile(){
        stepmessage.setText("Sto cercando di connettermi al server attraverso la rete mobile");
        Intent mobileIntent= new Intent(myActivity, MobileSend.class);
        MainActivity.mobileServiceIntent=mobileIntent;
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
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleSomethingWrong("Non sono stato in grado di connettermi al dispositivo attraverso il server");
                                    }
                                });
                                break;
                            case 14:
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mobile_connectionRefused=true;
                                        skippableInterfaces-=1;
                                        if(skippableInterfaces ==0){
                                            currentFragment.getActivity().getSupportFragmentManager().popBackStack();
                                            MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la connessione con il server. " +
                                                    "Non ci sono altre interfacce da utilizzare. Connessione annullata", Snackbar.LENGTH_LONG, 0);
                                        } else MainActivity.snackBarNav(myActivity, R.id.send_container, "Persa la connessione con il server. " +
                                                "Sto utilizzando interfacce rimanenti.", Snackbar.LENGTH_LONG, 0);
                                    }
                                });
                                break;
                        }
                    }

                    @Override
                    public void getProcessID(int code) throws RemoteException {
                        mobileProcessID=code;
                    }

                    @Override
                    public void setupPackage(byte[] data) throws RemoteException {

                    }

                    @Override
                    public void packageComplete() throws RemoteException {

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
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mobile_connectionRefused) return;
                                    alertdialog.dismiss();
                                    fm.beginTransaction()
                                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                            .replace(R.id.send_container, new Send_step_2(), "Send_step_2")
                                            .addToBackStack(null)
                                            .commit();
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
        myActivity.bindService(mobileIntent, mobileServiceConnection, Context.BIND_IMPORTANT);
        myActivity.startService(mobileIntent);
    }

    public static void disconnectMobile(){
        try {
            iService_app_to_mobile.disconnect();
        } catch (RemoteException e) {
        }
    }
    private boolean avoidConnection(){
        skippableInterfaces-=1;
        if(skippableInterfaces ==0){
            alertdialog.dismiss();
            MainActivity.snackBarNav(myActivity, R.id.send_container, "Connessione annullata", Snackbar.LENGTH_SHORT, 0);
            return true;
        } else return false;
    }
}

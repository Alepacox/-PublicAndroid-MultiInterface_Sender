package it.unicam.project.multiinterfacesender;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
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

import it.unicam.project.multiinterfacesender.Send.Send_step_2;

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
    private WifiManager wifiManager;
    private boolean foundBTdevice;
    private int currentStep;
    private boolean alreadyPaired;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            myActivity.unregisterReceiver(this);
            boolean foundNetwork = false;
            for (ScanResult scanResult : results) {
                Log.e("Found: ", scanResult.BSSID);
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
                            //CREATE SOCKET CONNECTION
                            checkStep2();
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
                //CREATE SOCKET CONNECTION
                checkStep3();
                myActivity.unregisterReceiver(this);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                handleSomethingWrong("Accoppiamento in corso");
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
        checkStep1();
    }

    public void checkStep1() {
        //Wifi
        if (wifiSSID != null) {
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
                            //CREATE SOCKET CONNECTION
                            checkStep2();
                        } else {
                            stepmessage.setText("Mi sto connettendo alla rete " + wifiSSID);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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

    public void checkStep2() {
        //Bluetooth
        if (btname != null) {
            currentStep=2;
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            myActivity.registerReceiver(bluetoothReceiver, filter);
            BluetoothAdapter btadapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> bondedDevice = btadapter.getBondedDevices();
            alreadyPaired = false;
            for (BluetoothDevice i : bondedDevice) {
                if (btname.equals(i.getName())) alreadyPaired = true;
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
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myActivity.unregisterReceiver(bluetoothReceiver);
                            //CREATE SOCKET CONNECTION
                            checkStep3();
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
                        BluetoothAdapter.getDefaultAdapter().startDiscovery();
                    }
                } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
            } else {
                myActivity.unregisterReceiver(bluetoothReceiver);
                //CREATE SOCKET CONNECTION
                checkStep3();
            }
        } else {
            if(currentStep==1){
                myActivity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                myActivity.registerReceiver(bluetoothReceiver, filter);
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
                    myActivity.unregisterReceiver(wifiReceiver);
                } else {
                    myActivity.unregisterReceiver(bluetoothReceiver);
                    checkStep3();
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
        //Mobile
        if (mobileconnection) {
            currentStep=3;
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
                                            alertdialog.dismiss();
                                            fm.beginTransaction()
                                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                                    .replace(R.id.send_container, new Send_step_2(), "")
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    });
                                }
                            }, 2000);
                        } else {
                            alertdialog.dismiss();
                            fm.beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.send_container, new Send_step_2(), "")
                                    .addToBackStack(null)
                                    .commit();
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
                    .replace(R.id.send_container, new Send_step_2(), "")
                    .addToBackStack(null)
                    .commit();
        }
    }
}
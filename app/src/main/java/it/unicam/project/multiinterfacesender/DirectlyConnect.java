package it.unicam.project.multiinterfacesender;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
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

import it.unicam.project.multiinterfacesender.Send.Send_step_2;

public class DirectlyConnect {
    private Activity myActivity;
    private String btname;
    private String wifiSSID;
    private boolean mobileconnection;
    private boolean found = false;
    private TextView stepmessage;
    private TextView retryText;
    private TextView ignoreText;
    private ProgressBar progress;
    private FragmentManager fm;
    private AlertDialog alertdialog;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName()!=null && device.getName().equals(btname)) {
                    found = true;
                    Class class1 = null;
                    try {
                        class1 = Class.forName("android.bluetooth.BluetoothDevice");
                        Method createBondMethod = class1.getMethod("createBond");
                        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                        if (!returnValue) {
                            handleSomethingWrongOnStep2("È necessario accoppiare i dispositivi per continuare");
                        } else {
                            myActivity.unregisterReceiver(mReceiver);
                            checkStep3();
                        }
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
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !found) {
                handleSomethingWrongOnStep2("Non sono stato in grado di trovare " +btname);
            }
        }
    };

    public DirectlyConnect(Activity myActivity, FragmentManager supportFragmentManager, String btname, String wifiSSID, boolean mobileconnection) {
        this.myActivity = myActivity;
        this.btname = btname;
        this.wifiSSID = wifiSSID;
        this.mobileconnection = mobileconnection;
        this.fm= supportFragmentManager;
    }

    public void startDirectylyConnection() {
        LayoutInflater li = LayoutInflater.from(myActivity);
        View toInflate = li.inflate(R.layout.connecting_step_layout, null);
        stepmessage = toInflate.findViewById(R.id.connecting_step_message);
        retryText = toInflate.findViewById(R.id.connecting_step_retry);
        ignoreText = toInflate.findViewById(R.id.connecting_step_ignore);
        progress = toInflate.findViewById(R.id.connecting_step_prog);
        if (btname != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            myActivity.registerReceiver(mReceiver, filter);
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                myActivity);
        alertDialogBuilder.setView(toInflate);
        alertDialogBuilder.setCancelable(false);
        alertdialog= alertDialogBuilder.create();
        alertdialog.show();
        checkStep1();
    }

    public void checkStep1() {
        //Wifi
        if (wifiSSID != null) {
            progress.setVisibility(View.VISIBLE);
            retryText.setVisibility(View.INVISIBLE);
            ignoreText.setVisibility(View.INVISIBLE);
            stepmessage.setText("Ti sto connettendo alla rete " + wifiSSID);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ConnectivityManager connectionManager = (ConnectivityManager) myActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    WifiManager wifiManager = (WifiManager) myActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(true);
                    }
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiSSID.equals(wifiInfo.getSSID())) {
                        checkStep2();
                    } else {
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        boolean found = false;
                        for (WifiConfiguration i : list) {
                            if (i.SSID != null && i.SSID.equals(wifiSSID)) {
                                found = true;
                                wifiManager.disconnect();
                                wifiManager.enableNetwork(i.networkId, true);
                                wifiManager.reconnect();
                                checkStep2();
                            }
                        }
                        if (!found) {
                            stepmessage.setText("Non ho trovato la rete " + wifiSSID);
                            progress.setVisibility(View.INVISIBLE);
                            retryText.setVisibility(View.VISIBLE);
                            ignoreText.setVisibility(View.VISIBLE);
                            ignoreText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    checkStep2();
                                }
                            });
                            retryText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    checkStep1();
                                }
                            });
                        }
                    }
                }
            }, 1500);
        } else checkStep2();

    }

    public void checkStep2() {
        //Bluetooth
        if (btname != null) {
            progress.setVisibility(View.VISIBLE);
            retryText.setVisibility(View.INVISIBLE);
            ignoreText.setVisibility(View.INVISIBLE);
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                myActivity.startActivityForResult(discoverableIntent, 5452);
            }
            stepmessage.setText("Sto cercando il dispositivo bluetooth " + btname);
            found = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    myActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 5489);
                } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
            } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
        } else checkStep3();
    }

    public void handleSomethingWrongOnStep2(String message) {
        stepmessage.setText(message);
        progress.setVisibility(View.INVISIBLE);
        retryText.setVisibility(View.VISIBLE);
        ignoreText.setVisibility(View.VISIBLE);
        ignoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myActivity.unregisterReceiver(mReceiver);
                checkStep3();
            }
        });
        retryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkStep2();
            }
        });
    }

    public void checkStep3() {
        //Mobile
        if (mobileconnection) {
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

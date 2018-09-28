package it.unicam.project.multiinterfacesender.Send;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;

public class Send_step_1_manual extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final int ACTION_BT_ENABLE = 875;
    private DataCommunication mListener;
    private CheckBox bluetoothSwitch;
    private CheckBox mobileSwitch;
    private CheckBox wifiSwitch;
    private boolean[] interfaces = {false, false, false};
    private ExpandableLayout interfaceExapandableLayout;
    private ExpandableLayout paramsExapandableLayout;
    private ProgressDialog connectingDialog;
    private String bluetoothName;
    private boolean found = false;
    private int MY_PERMISSION_REQUEST_COARSE_LOCATION = 542;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equals(bluetoothName)) {
                    found = true;
                    Class class1 = null;
                    try {
                        class1 = Class.forName("android.bluetooth.BluetoothDevice");
                        Method createBondMethod = class1.getMethod("createBond");
                        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                        if (!returnValue) {
                            if (connectingDialog.isShowing()) {
                                connectingDialog.cancel();
                                Snackbar.make(getActivity().findViewById(R.id.send_container),
                                        "Si è verificato un errore nella connessione con il dispositivo Bluetooth", Snackbar.LENGTH_LONG).show();
                            }
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
                if (connectingDialog.isShowing()) {
                    connectingDialog.cancel();
                    MainActivity.snackBarNav(getActivity(), R.id.send_container,
                            "Non è stato trovato il dispositivo Bluetooth", Snackbar.LENGTH_LONG, 0);
                }
            }
        }
    };


    public interface DataCommunication {
        public void setInterfaces(boolean[] value);

        public boolean isTheFirstTimeManual();
    }

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Send_step_1_manual() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send_step_1_manual.
     */
    // TODO: Rename and change types and number of parameters
    public static Send_step_1_manual newInstance(String param1, String param2) {
        Send_step_1_manual fragment = new Send_step_1_manual();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        connectingDialog = new ProgressDialog(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(mReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_step_1_manual, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mListener.isTheFirstTimeManual()) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle("Modalità manuale");
            alertDialog.setMessage("Sincronizza le interfacce da utilizzare " +
                    "con quelle del ricevente ed inserisci i suoi parametri visibili" +
                    " sullo schermo una volta in ascolto");

            alertDialog.setIcon(R.mipmap.ic_launcher);

            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
        CardView interfaceCard = getActivity().findViewById(R.id.cardInterfaces);
        interfaceExapandableLayout = getActivity().findViewById(R.id.expandable_interfaces);
        interfaceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!interfaceExapandableLayout.isExpanded()) {
                    interfaceExapandableLayout.expand();
                }
                if (paramsExapandableLayout.isExpanded()) {
                    paramsExapandableLayout.collapse();
                }
            }
        });
        bluetoothSwitch = getActivity().findViewById(R.id.checkBox_bluetooth_send);
        bluetoothSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(discoverableIntent, ACTION_BT_ENABLE);
                    } else interfaces[2] = true;
                } else interfaces[2] = false;
            }
        }));
        mobileSwitch = getActivity().findViewById(R.id.checkBox_mobile_send);
        mobileSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConnectivityManager cm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    try {
                        boolean isDataEnabled = false;
                        try {
                            Class<?> c = Class.forName(cm.getClass().getName());
                            Method m = c.getDeclaredMethod("getMobileDataEnabled");
                            m.setAccessible(true);
                            isDataEnabled = (Boolean) m.invoke(cm);
                        } catch (NoSuchMethodException e) {
                            isDataEnabled = Settings.Global.getInt(getContext().getContentResolver(), "mobile_data", 0) == 1;
                        }
                        if (!isDataEnabled) {
                            Toast.makeText(getActivity(), "Attiva la rete mobile per procedere", Toast.LENGTH_LONG).show();
                            startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity")));
                            mobileSwitch.setChecked(false);
                            interfaces[1] = false;
                        } else interfaces[1] = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else interfaces[1] = false;
            }
        }));
        wifiSwitch = getActivity().findViewById(R.id.checkBox_wifi_send);
        wifiSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (!wifiCheck.isConnectedOrConnecting()) {
                        Toast.makeText(getActivity(), "Connettiti ad una wifi per procedere", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        wifiSwitch.setChecked(false);
                        interfaces[0] = false;
                    } else interfaces[0] = true;
                } else interfaces[0] = false;
            }
        }));
        CardView paramsCard = getActivity().findViewById(R.id.cardParams);
        paramsExapandableLayout = getActivity().findViewById(R.id.expandable_params);
        paramsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!paramsExapandableLayout.isExpanded()) {
                    if (interfaces[0] || interfaces[1] || interfaces[2]) {
                        if (interfaceExapandableLayout.isExpanded()) {
                            interfaceExapandableLayout.collapse();
                        }
                        updateTextInputByChoosenInterfaces();
                        paramsExapandableLayout.expand();
                    } else MainActivity.snackBarNav(getActivity(), R.id.send_container,
                            "Seleziona almeno un'interfaccia per continuare", Snackbar.LENGTH_LONG, 0);
                }
            }
        });
        Button buttonConnect = getActivity().findViewById(R.id.button_next);
        buttonConnect.setText("CONNETTI");
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((interfaces[0] || interfaces[1] || interfaces[2])) {
                    if (interfaces[2]) {
                        TextInputEditText bluetoothInput = getActivity().findViewById(R.id.input_bluetooth_name);
                        bluetoothName = bluetoothInput.getText().toString();
                        if (!bluetoothName.matches("")) {
                            connectingDialog.setTitle("");
                            connectingDialog.setMessage("Connessione in corso");
                            connectingDialog.setIndeterminate(true);
                            connectingDialog.show();
                            startBluetoothDiscovery();
                        } else {
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                    .replace(R.id.send_container, new Send_step_2(), "SEND_STEP_2")
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                } else MainActivity.snackBarNav(getActivity(), R.id.send_container,
                        "Seleziona almeno un'interfaccia per continuare", Snackbar.LENGTH_LONG, 0);
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_BT_ENABLE) {
            if (resultCode == getActivity().RESULT_CANCELED) {
                MainActivity.snackBarNav(getActivity(), R.id.send_container, "Acconsenti per continuare", Snackbar.LENGTH_SHORT, 0);
                Switch bluetoothSwitch = getActivity().findViewById(R.id.checkBox_bluetooth_send);
                bluetoothSwitch.setChecked(false);
                interfaces[2] = false;
            } else {
                interfaces[2] = true;
            }
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataCommunication) {
            mListener = (DataCommunication) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataCommunication");
        }
    }

    public void updateTextInputByChoosenInterfaces() {
        TextInputLayout bluetoothLayout = getActivity().findViewById(R.id.input_layout_bluetooth_name);
        TextInputLayout wifiLayout = getActivity().findViewById(R.id.input_layout_wifi_ip);
        Resources r = getActivity().getResources();
        int[] highs = new int[]{(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 59, r.getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 119, r.getDisplayMetrics())};
        int count = 0;
        if (interfaces[0]) {
            FrameLayout.LayoutParams parameter = (FrameLayout.LayoutParams) wifiLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            wifiLayout.setLayoutParams(parameter);
            count++;
            wifiLayout.setVisibility(View.VISIBLE);
        } else wifiLayout.setVisibility(View.INVISIBLE);
        if (interfaces[2]) {
            FrameLayout.LayoutParams parameter = (FrameLayout.LayoutParams) bluetoothLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            bluetoothLayout.setLayoutParams(parameter);
            bluetoothLayout.setVisibility(View.VISIBLE);
        } else bluetoothLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }
        getContext().unregisterReceiver(mReceiver);
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            } else {
                if (connectingDialog.isShowing()) {
                    connectingDialog.cancel();
                }
                MainActivity.snackBarNav(getActivity(), R.id.send_container,
                        "La localizzazione è necessaria per trovare gli altri dispositivi Bluetooth", Snackbar.LENGTH_LONG, 0);
            }
        }
    }

    public void startBluetoothDiscovery() {
        found = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_COARSE_LOCATION);
            } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
        } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }
}

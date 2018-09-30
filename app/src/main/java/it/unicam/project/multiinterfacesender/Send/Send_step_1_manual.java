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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unicam.project.multiinterfacesender.DirectlyConnect;
import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;

public class Send_step_1_manual extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private DataCommunication mListener;
    private CheckBox bluetoothSwitch;
    private CheckBox wifiSwitch;
    private boolean usingWifi;
    private boolean usingBluetooth;
    private ExpandableLayout interfaceExapandableLayout;
    private ExpandableLayout paramsExapandableLayout;
    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");
    private DirectlyConnect dc;
    private TextInputEditText wifipiText;
    private TextInputLayout wifipiLayout;
    private TextInputEditText bluetoothText;
    private TextInputLayout bluetoothLayout;
    private boolean configuringInterfaces;
    private int ACTION_BT_ENABLE = 5452;
    private int MY_PERMISSION_REQUEST_COARSE_LOCATION = 5489;

    public interface DataCommunication {
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
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
                    "con quelle del ricevente ed inserisci i parametri visibili" +
                    "sul suo schermo una volta in ascolto. \nL'uso della rete mobile" +
                    " è disabilitato.");

            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();
        }
        configuringInterfaces=true;
        wifipiText = getActivity().findViewById(R.id.input_wifi_ip);
        wifipiLayout = getActivity().findViewById(R.id.input_layout_wifi_ip);
        wifipiText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (wifipiLayout.isErrorEnabled()) {
                    wifipiLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        bluetoothLayout = getActivity().findViewById(R.id.input_layout_bluetooth_name);
        bluetoothText = getActivity().findViewById(R.id.input_bluetooth_name);
        bluetoothText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (bluetoothLayout.isErrorEnabled()) {
                    bluetoothLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final CardView interfaceCard = getActivity().findViewById(R.id.cardInterfaces);
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
                    } else usingBluetooth = true;
                } else usingBluetooth = false;
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
                        usingWifi = false;
                    } else usingWifi = true;
                } else usingWifi = false;
            }
        }));
        final CardView paramsCard = getActivity().findViewById(R.id.cardParams);
        paramsExapandableLayout = getActivity().findViewById(R.id.expandable_params);
        paramsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!paramsExapandableLayout.isExpanded()) {
                    if (usingWifi || usingBluetooth) {
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
        final Fragment currentFragment = this;
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usingWifi || usingBluetooth) {
                    if(interfaceExapandableLayout.isExpanded()) {
                        interfaceExapandableLayout.collapse();
                        updateTextInputByChoosenInterfaces();
                        paramsExapandableLayout.expand();
                    } else {
                        String btname = null;
                        String wifiSSID = null;
                        String wifiip = null;
                        configuringInterfaces=false;
                        boolean errorInField=false;
                        if (usingWifi) {
                            ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (wifiCheck.isConnected()) {
                                String insertedWifiIp = wifipiText.getText().toString();
                                if (insertedWifiIp.length() == 0) {
                                    wifipiText.setError("Questo campo non può essere vuoto");
                                    errorInField=true;
                                } else if (!IP_ADDRESS.matcher(insertedWifiIp).matches()) {
                                    wifipiText.setError("Inserisci un indirizzo ip valido");
                                    errorInField=true;
                                } else {
                                    WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    wifiSSID = wifiManager.getConnectionInfo().getSSID();
                                    wifiip = insertedWifiIp;
                                }
                            } else {
                                wifiSwitch.setChecked(false);
                                MainActivity.snackBarNav(getActivity(), R.id.send_container,
                                        "Ti sei disconnesso dalla wifi, riconnettiti", Snackbar.LENGTH_LONG, 0);
                                return;
                            }
                        }
                        if (usingBluetooth) {
                            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                                String insertedBluetoothName = bluetoothText.getText().toString();
                                if (insertedBluetoothName.length() == 0) {
                                    bluetoothText.setError("Questo campo non può essere vuoto");
                                    errorInField=true;
                                } else btname = insertedBluetoothName;
                            } else {
                                bluetoothSwitch.setChecked(false);
                                MainActivity.snackBarNav(getActivity(), R.id.send_container,
                                        "Hai disattivato il bluetooh", Snackbar.LENGTH_LONG, 0);
                                return;
                            }
                        }
                        if(errorInField){
                            return;
                        } else{
                            dc = new DirectlyConnect(currentFragment, btname, wifiip, wifiSSID, false);
                            dc.startDirectylyConnection();
                        }
                    }
                } else MainActivity.snackBarNav(getActivity(), R.id.send_container,
                        "Seleziona almeno un'interfaccia per continuare", Snackbar.LENGTH_LONG, 0);
            }
        });
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dc.handleGuaranteedPermission(true);
            } else {
                dc.handleSomethingWrong("La localizzazione è necessaria per effettuare la connessione");
            }
        }
    }

    public void updateTextInputByChoosenInterfaces() {
        TextInputLayout bluetoothLayout = getActivity().findViewById(R.id.input_layout_bluetooth_name);
        TextInputLayout wifiLayout = getActivity().findViewById(R.id.input_layout_wifi_ip);
        Resources r = getActivity().getResources();
        int[] highs = new int[]{(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, r.getDisplayMetrics())};
        int count = 0;
        if (usingWifi) {
            FrameLayout.LayoutParams parameter = (FrameLayout.LayoutParams) wifiLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            wifiLayout.setLayoutParams(parameter);
            count++;
            wifiLayout.setVisibility(View.VISIBLE);
        } else wifiLayout.setVisibility(View.INVISIBLE);
        if (usingBluetooth) {
            FrameLayout.LayoutParams parameter = (FrameLayout.LayoutParams) bluetoothLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            bluetoothLayout.setLayoutParams(parameter);
            bluetoothLayout.setVisibility(View.VISIBLE);
        } else bluetoothLayout.setVisibility(View.INVISIBLE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_BT_ENABLE) {
            if (resultCode == getActivity().RESULT_CANCELED) {
                if (configuringInterfaces) {
                    CheckBox bluetoothSwitch = getActivity().findViewById(R.id.checkBox_bluetooth_send);
                    bluetoothSwitch.setChecked(false);
                    MainActivity.snackBarNav(getActivity(), R.id.send_container, "Acconsenti per continuare", Snackbar.LENGTH_SHORT, 0);
                } else dc.handleSomethingWrong("Attivare il bluetooth per continuare");
                usingBluetooth = false;
            } else {
                usingBluetooth = true;
            }
        }

    }

}

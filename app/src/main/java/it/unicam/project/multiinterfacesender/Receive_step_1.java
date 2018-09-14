package it.unicam.project.multiinterfacesender;


import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link } interface
 * to handle interaction events.
 * Use the {@link Receive_step_1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Receive_step_1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mobileIp;
    private String wifiIp;
    private String bluetoothName;
    private int ACTION_BT= 56;

    public interface DataCommunication {
        public void setInterfacesDetails(String mobileIp, String wifiIp, String bluetoothName);
        public boolean getNoLoginMode();
    }

    private DataCommunication mListener;

    public Receive_step_1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Receive_step_1.
     */
    // TODO: Rename and change types and number of parameters
    public static Receive_step_1 newInstance(String param1, String param2) {
        Receive_step_1 fragment = new Receive_step_1();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive_step_1, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final CheckBox bluetoothCheckbox= getActivity().findViewById(R.id.checkBox_bluetooth);
        bluetoothCheckbox.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        if (BluetoothAdapter.getDefaultAdapter().getScanMode() !=
                                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                            startActivityForResult(discoverableIntent, ACTION_BT);
                        } else bluetoothName=BluetoothAdapter.getDefaultAdapter().getName();
                    } else bluetoothName=null;
                }
            }));
        final CheckBox mobileCheckbox= getActivity().findViewById(R.id.checkBox_mobile);
        mobileCheckbox.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifiCheck.isConnected()) {
                        MainActivity.snackBarNav(getActivity(), R.id.receive_container,
                                "Seleziona questa spunta prima di connetterti ad una wifi", Snackbar.LENGTH_LONG, 1);
                        mobileCheckbox.setChecked(false);
                    } else {
                        ConnectivityManager cm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mobileCheck = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                        if (!mobileCheck.isConnected()) {
                            try {
                                Class cmClass = Class.forName(cm.getClass().getName());
                                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                                method.setAccessible(true); // Make the method callable
                                if (!(Boolean) method.invoke(cm)) {
                                    Toast.makeText(getActivity(), "Attiva la rete mobile per procedere", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity")));
                                    mobileCheckbox.setChecked(false);
                                }
                            } catch (Exception e) {
                                // TODO do whatever error handling you want here
                            }
                        } else {
                            mobileIp=PublicIP();
                        }
                    }
                } else {
                    mobileIp=null;
                }
        }}));
        final CheckBox wifiCheckbox= getActivity().findViewById(R.id.checkBox_wifi);
        wifiCheckbox.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (!wifiCheck.isConnected()) {
                        Toast.makeText(getActivity(), "Assicurati di connetterti alla stessa wifi dell'altro", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        wifiCheckbox.setChecked(false);
                    } else {
                        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        int ipAddress = wifiInfo.getIpAddress();
                        String ip= String.format(Locale.getDefault(), "%d.%d.%d.%d",
                                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                        wifiIp=ip;
                    }
                } else {
                    wifiIp=null;
                }
            }
        }));
        FloatingActionButton doneButton= getActivity().findViewById(R.id.button_receive_interf_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiCheckbox.isChecked() || mobileCheckbox.isChecked() || bluetoothCheckbox.isChecked()) {
                    mListener.setInterfacesDetails(mobileIp, wifiIp, bluetoothName);
                    if(!mListener.getNoLoginMode()) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                .replace(R.id.receive_container, new Receive_step_2(), "")
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Intent myIntent = new Intent(getActivity(), Receive_loading.class);
                        myIntent.putExtra("mobileIp", mobileIp);
                        myIntent.putExtra("wifiIp", wifiIp);
                        myIntent.putExtra("bluetoothName", bluetoothName);
                        myIntent.putExtra("receivingManual", true);
                        getActivity().startActivity(myIntent);
                    }
                } else MainActivity.snackBarNav(getActivity(), R.id.receive_container,
                        "Seleziona almeno un'interfaccia per continuare", Snackbar.LENGTH_LONG, 1);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_BT){
            if(resultCode == getActivity().RESULT_CANCELED) {
                MainActivity.snackBarNav(getActivity(), R.id.receive_container,
                        "Acconsenti per continuare", Snackbar.LENGTH_SHORT, 1);
                CheckBox bluetoothCheckbox= getActivity().findViewById(R.id.checkBox_bluetooth);
                bluetoothCheckbox.setChecked(false);
            } else bluetoothName=BluetoothAdapter.getDefaultAdapter().getName();
        }
    }

    public String PublicIP(){
        boolean error= false;
        try {
            String output = new GetPublicIP().execute().get();
            if(output!=null){
                JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                return object.getString("ip");
            } else error=true;
        } catch (InterruptedException e) {
            error=true;
            e.printStackTrace();
        } catch (ExecutionException e) {
            error=true;
            e.printStackTrace();
        } catch (JSONException e) {
            error=true;
            e.printStackTrace();
        }
        if (error) {
            Snackbar.make(getActivity().findViewById(R.id.receive_container),
                    "C'è stato un problema", Snackbar.LENGTH_LONG).
                    setAction("Riprova", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PublicIP();
                        }
                    }).show();
            return null;
        }
        return null;
    }
    public static class GetPublicIP extends AsyncTask<Void, Void, String> {

        public GetPublicIP() {
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL("https://api.ipify.org?format=json");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    if (urlConnection.getResponseCode()==200){
                        InputStreamReader streamReader = new
                                InputStreamReader(urlConnection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(streamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } else return null;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                return null;
            }
        }
    }
}

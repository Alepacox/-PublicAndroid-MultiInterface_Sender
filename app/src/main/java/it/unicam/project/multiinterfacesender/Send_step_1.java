package it.unicam.project.multiinterfacesender;


import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.lang.reflect.Method;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Send_step_1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Send_step_1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Send_step_1 extends Fragment implements BlockingStep {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final int ACTION_BT_ENABLE= 875;
    private DataCommunication mListener;
    private Switch bluetoothSwitch;
    private Switch mobileSwitch;
    private Switch wifiSwitch;

    public interface DataCommunication {
        public void setUsingWifi(boolean value);
        public void setUsingMobile(boolean value);
        public void setUsingBluetooth(boolean value);
    }

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Send_step_1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send_step_1.
     */
    // TODO: Rename and change types and number of parameters
    public static Send_step_1 newInstance(String param1, String param2) {
        Send_step_1 fragment = new Send_step_1();
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
        return inflater.inflate(R.layout.fragment_send_step_1, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        bluetoothSwitch= getActivity().findViewById(R.id.switch_bluetooth);
        bluetoothSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(discoverableIntent, ACTION_BT_ENABLE);
                    } else mListener.setUsingBluetooth(true);
                } else mListener.setUsingBluetooth(false);
            }
        }));
        mobileSwitch= getActivity().findViewById(R.id.switch_mobile);
        mobileSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ConnectivityManager cm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo mobileCheck = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                        try {
                            boolean isDataEnabled=false;
                            try {
                                Class<?> c = Class.forName(cm.getClass().getName());
                                Method m = c.getDeclaredMethod("getMobileDataEnabled");
                                m.setAccessible(true);
                                isDataEnabled= (Boolean)m.invoke(cm);
                            } catch (NoSuchMethodException e) {
                                isDataEnabled = Settings.Global.getInt(getContext().getContentResolver(), "mobile_data", 0) == 1;
                            }

                            if(!isDataEnabled) {
                                Toast.makeText(getActivity(), "Attiva la rete mobile per procedere", Toast.LENGTH_LONG).show();
                                startActivity(new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity")));
                                mobileSwitch.setChecked(false);
                                mListener.setUsingMobile(false);
                            } else mListener.setUsingMobile(true);
                        } catch(Exception e){
                            e.printStackTrace();
                        }


                } else mListener.setUsingMobile(false);
            }
        }));
        wifiSwitch= getActivity().findViewById(R.id.switch_wifi);
        wifiSwitch.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ConnectivityManager connectionManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (!wifiCheck.isConnected()) {
                        Toast.makeText(getActivity(), "Connettiti ad una wifi per procedere", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        wifiSwitch.setChecked(false);
                        mListener.setUsingWifi(false);
                    } else mListener.setUsingWifi(true);
                } else mListener.setUsingWifi(false);
            }
        }));

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_BT_ENABLE ) {
            if(resultCode == getActivity().RESULT_CANCELED) {
                MainActivity.snackBarNav(getActivity(), R.id.send_container,"Acconsenti per continuare", Snackbar.LENGTH_SHORT, 0);
                Switch bluetoothSwitch = getActivity().findViewById(R.id.switch_bluetooth);
                bluetoothSwitch.setChecked(false);
                mListener.setUsingBluetooth(false);
            } else {
                mListener.setUsingBluetooth(true);
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

    @Override
    public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
        if(!bluetoothSwitch.isChecked() && !mobileSwitch.isChecked() && !wifiSwitch.isChecked()){
            MainActivity.snackBarNav(getActivity(), R.id.send_container,
                    "Seleziona almeno un'interfaccia per continuare", Snackbar.LENGTH_LONG, 0);
        } else {
            Send_step_2.updateTextInputByChoosenInterfaces(getActivity());
            callback.goToNextStep();
        }
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

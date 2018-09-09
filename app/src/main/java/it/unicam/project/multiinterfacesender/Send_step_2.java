package it.unicam.project.multiinterfacesender;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.poovam.pinedittextfield.LinePinField;
import com.stepstone.stepper.BlockingStep;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Send_step_2.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Send_step_2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Send_step_2 extends Fragment implements BlockingStep {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public interface DataCommunication {
        public boolean[] getChoosenInterface();
    }
    public static DataCommunication mListener;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ExpandableLayout usernameExapandableLayout;
    private ExpandableLayout paramsExapandableLayout;
    private ProgressDialog connectingDialog;
    private String bluetoothName;
    private boolean firstTime=true;
    private boolean found=false;
    private int MY_PERMISSION_REQUEST_COARSE_LOCATION= 542;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().equals(bluetoothName)){
                    found=true;
                    Class class1 = null;
                    try {
                        class1 = Class.forName("android.bluetooth.BluetoothDevice");
                        Method createBondMethod = class1.getMethod("createBond");
                        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                        if(!returnValue){
                            if(connectingDialog.isShowing()){
                                connectingDialog.cancel();
                                Snackbar.make(getActivity().findViewById(R.id.send_container),
                                        "Si è verificato un errore nel connettersi al dispositivo Bluetooth", Snackbar.LENGTH_LONG).show();
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
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !found){
                if(connectingDialog.isShowing()){
                    connectingDialog.cancel();
                    Snackbar.make(getActivity().findViewById(R.id.send_container),
                            "Non è stato trovato il dispositivo Bluetooth", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    };

    public Send_step_2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send_step_2.
     */
    // TODO: Rename and change types and number of parameters
    public static Send_step_2 newInstance(String param1, String param2) {
        Send_step_2 fragment = new Send_step_2();
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
        connectingDialog= new ProgressDialog(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(mReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_step_2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        CardView userCard = getActivity().findViewById(R.id.cardUsername);
        CardView paramsCard = getActivity().findViewById(R.id.cardParams);
        usernameExapandableLayout = getActivity().findViewById(R.id.expandable_username);
        paramsExapandableLayout = getActivity().findViewById(R.id.expandable_params);
        userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameExapandableLayout.expand();
                if (paramsExapandableLayout.isExpanded()) {
                    paramsExapandableLayout.collapse();
                }
            }
        });
        paramsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstTime){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Modalità manuale");
                    alertDialog.setMessage("Inserisci i parametri visualizzati sullo schermo del ricevente visibili " +
                            "una volta configurate le interfacce in modalità manuale");

                    alertDialog.setIcon(R.mipmap.ic_launcher);

                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    firstTime=false;
                    alertDialog.show();
                }
                paramsExapandableLayout.expand();
                if (usernameExapandableLayout.isExpanded()) {
                    usernameExapandableLayout.collapse();
                }
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
    public void onDestroy() {
        super.onDestroy();
        if(BluetoothAdapter.getDefaultAdapter().isDiscovering()){
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }
        getContext().unregisterReceiver(mReceiver);
        mListener = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onNextClicked(final StepperLayout.OnNextClickedCallback callback) {
        if(usernameExapandableLayout.isExpanded()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle("Codice di sicurezza");
            alertDialog.setMessage("Inserisci il codice di sicurezza visualizzato sullo schermo del ricevente");

            final LinePinField pinInput= new LinePinField(getActivity());
            pinInput.setNumberOfFields(6);
            pinInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            pinInput.setHighlightPaintColor(getActivity().getResources().getColor(R.color.sendPrimaryColor));
            FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pinInput.setLayoutParams(params);
            alertDialog.setView(pinInput);
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setPositiveButton("Vai",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            callback.goToNextStep();
                        }
                    });

            alertDialog.setNegativeButton("Annulla",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();

        } else if (paramsExapandableLayout.isExpanded()){

            TextInputEditText bluetoothInput= getActivity().findViewById(R.id.input_bluetooth_name);
            bluetoothName= bluetoothInput.getText().toString();
            if (!bluetoothName.matches("")) {
                connectingDialog.setTitle("");
                connectingDialog.setMessage("Connessione in corso");
                connectingDialog.setIndeterminate(true);
                connectingDialog.show();
                StartBluetoothDiscovery();
            } else callback.goToNextStep();
        } else MainActivity.snackBarNav(getActivity(), R.id.send_container,
                "Seleziona ed inserisci i parametri per una modalità", Snackbar.LENGTH_SHORT, 0);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(requestCode==MY_PERMISSION_REQUEST_COARSE_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            } else {
                if(connectingDialog.isShowing()){
                    connectingDialog.cancel();
                }
                MainActivity.snackBarNav(getActivity(), R.id.send_container,
                        "La localizzazione è necessaria per trovare gli altri dispositivi Bluetooth", Snackbar.LENGTH_LONG, 0);
            }
        }
    }

    @Override
    public void onCompleteClicked(StepperLayout.OnCompleteClickedCallback callback) {

    }

    @Override
    public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
        callback.goToPrevStep();
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

    public void StartBluetoothDiscovery(){
        found=false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_COARSE_LOCATION);
            } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
        } else BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public static void updateTextInputByChoosenInterfaces(Activity activity){
        TextInputLayout bluetoothLayout= activity.findViewById(R.id.input_layout_bluetooth_name);
        TextInputLayout mobileLayout= activity.findViewById(R.id.input_layout_remote_ip);
        TextInputLayout wifiLayout= activity.findViewById(R.id.input_layout_wifi_ip);
        boolean[] interfaces= mListener.getChoosenInterface();
        Resources r = activity.getResources();
        int[] highs= new int[]{(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 63, r.getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 123, r.getDisplayMetrics())};
        int count=0;
        if(interfaces[0]){
            FrameLayout.LayoutParams parameter =  (FrameLayout.LayoutParams) wifiLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            wifiLayout.setLayoutParams(parameter);
            count++;
            wifiLayout.setVisibility(View.VISIBLE);
        } else wifiLayout.setVisibility(View.INVISIBLE);
        if(interfaces[1]){
            FrameLayout.LayoutParams parameter =  (FrameLayout.LayoutParams) mobileLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            mobileLayout.setLayoutParams(parameter);
            count++;
            mobileLayout.setVisibility(View.VISIBLE);
        } else mobileLayout.setVisibility(View.INVISIBLE);
        if(interfaces[2]){
            FrameLayout.LayoutParams parameter =  (FrameLayout.LayoutParams) bluetoothLayout.getLayoutParams();
            parameter.setMargins(parameter.leftMargin, highs[count], parameter.rightMargin, parameter.bottomMargin);
            bluetoothLayout.setLayoutParams(parameter);
            bluetoothLayout.setVisibility(View.VISIBLE);
        } else bluetoothLayout.setVisibility(View.INVISIBLE);
    }
}

package it.unicam.project.multiinterfacesender.Receive;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import it.unicam.project.multiinterfacesender.Login;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.SyncToServerTasks;


public class Receive_step_2 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Thread serverTask;
    private Button buttonManual;
    private String sessioncode;
    private String mobileIp;
    private String wifiIp;
    private String wifiSSID;
    private String bluetoothName;
    private String uToken;
    private String dToken;
    private String devicename;
    private Intent myIntent;
    //View
    private TextView textCode;
    private ProgressBar progGenerateCode;

    public interface DataCommunication {
        public String[] getInterfacesDetails();
        public String getDeviceName();
        public String getUToken();
        public String getDToken();
        public void setSwipable(boolean locked);
    }

    private DataCommunication mListener;

    public Receive_step_2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Receive_step_2.
     */
    // TODO: Rename and change types and number of parameters
    public static Receive_step_2 newInstance(String param1, String param2) {
        Receive_step_2 fragment = new Receive_step_2();
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
        uToken= mListener.getUToken();
        dToken=mListener.getDToken();
        devicename= mListener.getDeviceName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive_step_2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final String[] interfacesDetails = mListener.getInterfacesDetails();
        myIntent = new Intent(getActivity(), Receive_loading.class);
        mobileIp= String.valueOf(interfacesDetails[0]);
        wifiIp= String.valueOf(interfacesDetails[1]);
        wifiSSID= String.valueOf(interfacesDetails[2]);
        bluetoothName= String.valueOf(interfacesDetails[3]);
        myIntent.putExtra("mobileIp", mobileIp);
        myIntent.putExtra("wifiIp", wifiIp);
        myIntent.putExtra("wifiSSID", wifiSSID);
        myIntent.putExtra("bluetoothName", bluetoothName);
        textCode= getActivity().findViewById(R.id.text_generate_code);
        progGenerateCode= getActivity().findViewById(R.id.prog_generate_code);
        buttonManual = getActivity().findViewById(R.id.button_receive_manual);
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myIntent.putExtra("receivingManual", true);
                getActivity().startActivity(myIntent);
            }
        });
        TextView devicenameText= getActivity().findViewById(R.id.id_device);
        devicenameText.setText(devicename);
        final ImageView buttonStart = getActivity().findViewById(R.id.button_generate_code);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateSessionCode();
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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.setSwipable(true);
        mListener = null;
        if (serverTask!=null && serverTask.isAlive()) {
            serverTask.interrupt();
        }
    }

    public void generateSessionCode(){
        textCode.setVisibility(View.INVISIBLE);
        progGenerateCode.setVisibility(View.VISIBLE);
        serverTask= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String output = new SyncToServerTasks.GenerateSessionCodeTask(uToken, dToken,
                            new String[]{mobileIp, wifiIp, wifiSSID, bluetoothName}).execute().get();
                    if (output != null) {
                        JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                        if (!object.getString("message").equals("unauthorized")) {
                            sessioncode = object.getString("sessioncode");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        mListener.setSwipable(false);
                                        myIntent.putExtra("receivingManual", false);
                                        myIntent.putExtra("sessioncode", sessioncode);
                                        getActivity().startActivity(myIntent);
                                        textCode.setText("Genera codice sessione");
                                        textCode.setVisibility(View.VISIBLE);
                                        progGenerateCode.setVisibility(View.INVISIBLE);
                                    } catch (NullPointerException e){
                                    }
                                }
                            });
                        } else if (object.getString("cause").equals("user token expired")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(getActivity(), Login.class);
                                    Toast.makeText(getActivity(), R.string.session_expired, Toast.LENGTH_LONG).show();
                                    startActivity(i);
                                    getActivity().finish();
                                }
                            });
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    resetView();
                                    Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                                } catch (NullPointerException e){}
                            }
                        });
                    }
                } catch (InterruptedException e) {

                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                resetView();
                                Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                            } catch (NullPointerException e){}
                        }
                    });
                }
            }
        });
        serverTask.start();
    }

    public void resetView(){
        textCode.setText("Riprova");
        textCode.setVisibility(View.VISIBLE);
        progGenerateCode.setVisibility(View.INVISIBLE);
    }
}

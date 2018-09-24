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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import it.unicam.project.multiinterfacesender.R;


public class Receive_step_2 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String sessionCode;

    public interface DataCommunication {
        public String[] getInterfacesDetails();
        public String getDeviceName();
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
        final Intent myIntent = new Intent(getActivity(), Receive_loading.class);
        myIntent.putExtra("mobileIp", interfacesDetails[0]);
        myIntent.putExtra("wifiIp", interfacesDetails[1]);
        myIntent.putExtra("bluetoothName", interfacesDetails[2]);
        TextView devicename= getActivity().findViewById(R.id.id_device);
        devicename.setText(mListener.getDeviceName());
        final Button buttonManual = getActivity().findViewById(R.id.button_receive_manual);
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myIntent.putExtra("receivingManual", true);
                getActivity().startActivity(myIntent);
            }
        });
        final ImageView buttonStart = getActivity().findViewById(R.id.button_generate_code);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout relativeLayout= getActivity().findViewById(R.id.receive_generate_layout);
                relativeLayout.setVisibility(View.INVISIBLE);
                buttonManual.setVisibility(View.INVISIBLE);
                final Random rm = new Random();
                final TextView code = getActivity().findViewById(R.id.session_code);
                sessionCode = String.valueOf(rm.nextLong());
                code.setText(sessionCode);
                CardView sessionCard = getActivity().findViewById(R.id.cardview_session_code);
                sessionCard.setVisibility(View.VISIBLE);
                TextView shareSession = getActivity().findViewById(R.id.text_share_session);
                shareSession.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "MultiInterface Sender: il mio codice di connessione è ");
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                    }
                });
                FloatingActionButton goNextPage = getActivity().findViewById(R.id.button_session_next);
                goNextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myIntent.putExtra("receivingManual", false);
                        getActivity().startActivity(myIntent);
                    }
                });
                FloatingActionButton refresh = getActivity().findViewById(R.id.button_session_refresh);
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sessionCode = String.valueOf(rm.nextLong());
                        code.setText(sessionCode);
                    }
                });
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
        mListener = null;
    }
}

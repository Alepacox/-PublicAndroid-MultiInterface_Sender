package it.unicam.project.multiinterfacesender.Send;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.poovam.pinedittextfield.LinePinField;

import it.unicam.project.multiinterfacesender.R;


public class Send_step_1_auto extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String choosenDeviceName;
    private EditText inputID;
    private TextInputLayout inputIDLayout;

    public interface DataCommunication {
        public void setChoosenDevice(String ID);
    }

    private DataCommunication mListener;

    public Send_step_1_auto() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send_step_1_auto.
     */
    // TODO: Rename and change types and number of parameters
    public static Send_step_1_auto newInstance(String param1, String param2) {
        Send_step_1_auto fragment = new Send_step_1_auto();
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
        return inflater.inflate(R.layout.fragment_send_step_1_auto, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        inputID= getActivity().findViewById(R.id.input_id);
        inputID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputIDLayout.isErrorEnabled()){
                    inputIDLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputIDLayout= getActivity().findViewById(R.id.input_layout_id);
        Button buttonManual= getActivity().findViewById(R.id.button_send_manual);
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.send_container, new Send_step_1_manual(),"")
                        .addToBackStack(null)
                        .commit();
            }
        });
        Button buttonMyDevices= getActivity().findViewById(R.id.button_send_your_devices);
        buttonMyDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.send_container, new Send_device_list(),"")
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button buttonConnect= getActivity().findViewById(R.id.button_next);
        buttonConnect.setText("CONNETTI");
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputID.getText().toString().length()==0){
                    inputID.setError(getResources().getString(R.string.field_empty));
                    return;
                } else mListener.setChoosenDevice(inputID.getText().toString());
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
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                        .replace(R.id.send_container, new Send_step_2(),"")
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });

                alertDialog.setNegativeButton("Annulla",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
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

}

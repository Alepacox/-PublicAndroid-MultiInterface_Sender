package it.unicam.project.multiinterfacesender.Send;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.poovam.pinedittextfield.LinePinField;

import org.json.JSONObject;
import org.json.JSONTokener;

import it.unicam.project.multiinterfacesender.Login;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.Registration;
import it.unicam.project.multiinterfacesender.SetupDeviceName;
import it.unicam.project.multiinterfacesender.SyncToServerTasks;


public class Send_step_1_auto extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String choosenDeviceName;
    private LinePinField pinField;
    private String uToken;
    private String dToken;
    private Thread serverTask;

    public interface DataCommunication {
        public String getUToken();
        public String getDToken();
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
        uToken = mListener.getUToken();
        dToken = mListener.getDToken();
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
        pinField = getActivity().findViewById(R.id.pin_input);
        Button buttonManual = getActivity().findViewById(R.id.button_send_manual);
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.send_container, new Send_step_1_manual(), "")
                        .addToBackStack(null)
                        .commit();
            }
        });
        Button buttonMyDevices = getActivity().findViewById(R.id.button_send_your_devices);
        buttonMyDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.send_container, new Send_device_list(), "")
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button buttonConnect = getActivity().findViewById(R.id.button_next);
        buttonConnect.setText("CONNETTI");
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sessionCode = pinField.getText().toString();
                if (sessionCode.length() < 6) {
                    pinField.setText("");
                    Toast.makeText(getActivity(), "Inserisci un PIN valido", Toast.LENGTH_SHORT).show();
                    return;
                }
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Connessione in corso");
                progressDialog.show();
                serverTask = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String output = new SyncToServerTasks.SessionConnectTask(uToken, dToken, sessionCode).execute().get();
                            if (output != null) {
                                JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                                if (!object.getString("message").equals("unauthorized")) {
                                    String btName = object.getString("btname");
                                    String wifiip = object.getString("wifiip");
                                    boolean mobileip = object.getBoolean("mobileip");
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                                            .replace(R.id.send_container, new Send_step_2(), "")
                                            .addToBackStack(null)
                                            .commit();
                                } else if (object.getString("cause").equals("invalid session code")) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.cancel();
                                            pinField.setText("");
                                            Toast.makeText(getActivity(), "Nessuna sessione valida per questo PIN", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else if (object.getString("cause").equals("same device")) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.cancel();
                                            pinField.setText("");
                                            Toast.makeText(getActivity(), "Non puoi comunicare con te stesso", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (InterruptedException e) {

                        } catch (Exception e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                serverTask.start();
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
        if (serverTask.isAlive()) {
            serverTask.interrupt();
        }
    }

}

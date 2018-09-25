package it.unicam.project.multiinterfacesender.Send;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.unicam.project.multiinterfacesender.Login;
import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.SyncToServerTasks;


public class Send_device_list extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout deviceListLayout;
    protected LayoutInflater inflater;
    private String uToken;
    private String choosenDeviceName;
    protected ArrayList<Device> deviceList= new ArrayList<>();
    private Thread serverTask;
    private SwipeRefreshLayout refreshDevices;

    public interface DataCommunication {
        public void setChoosenDevice(String ID);
        public String getUToken();
    }

    private DataCommunication mListener;

    public Send_device_list() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send_device_list.
     */
    // TODO: Rename and change types and number of parameters
    public static Send_device_list newInstance(String param1, String param2) {
        Send_device_list fragment = new Send_device_list();
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
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_device_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        refreshDevices= getActivity().findViewById(R.id.swipe_devices);
        refreshDevices.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncCollectionToServer();
            }
        });
        syncCollectionToServer();
        deviceListLayout= getActivity().findViewById(R.id.device_list);
        Button buttonConnect= getActivity().findViewById(R.id.button_next);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(choosenDeviceName==null){
                    MainActivity.snackBarNav(getActivity(), R.id.send_container,
                            "Seleziona prima un dispositivo", Snackbar.LENGTH_SHORT, 0);
                } else {

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if(serverTask.isAlive()){
            serverTask.interrupt();
        }
    }

    public class Device {
        private String name;
        private boolean pc;
        private boolean status;
        private View view;
        public Device(String name, boolean pc, boolean status) {
            this.name=name;
            this.pc=pc;
            this.status= status;
            deviceList.add(this);
        }
        public void setView(View view){ this.view=view; }
        public String getName() { return name; }
        public boolean isPc() { return pc; }
        public void setName(String name) { this.name = name; }
        public void setPc(boolean pc) { this.pc = pc; }
    }

    public void syncCollectionToServer(){
        refreshDevices.setRefreshing(true);
        serverTask= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String output = new SyncToServerTasks.GetDeviceListTask(uToken).execute().get();
                    if (output != null) {
                        if(!output.equals("unauthorized")){
                            JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                            JSONArray devices = object.getJSONArray("devices");
                            deviceList.clear();
                            for(int i=0; i<devices.length(); i++){
                                JSONObject device= (JSONObject) devices.get(i);
                                String name= device.getString("Name");
                                boolean isPc= device.getBoolean("Is_Pc");
                                boolean isActive= device.getBoolean("Status");
                                new Device(name, isPc, isActive);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    syncCollectionToView();
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(getActivity(), Login.class);
                                    Toast.makeText(getActivity(), R.string.session_expired, Toast.LENGTH_LONG).show();
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                }
                            });
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshDevices.setRefreshing(false);
                                Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (InterruptedException e){

                }
                catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshDevices.setRefreshing(false);
                            Toast.makeText(getActivity(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        serverTask.start();
    }

    public void syncCollectionToView(){
        deviceListLayout.removeAllViews();
        for(int i=0; i<deviceList.size(); i++){
            final Device temp= deviceList.get(i);
            final View tempView= inflater.inflate(R.layout.device_list_item, null);
            temp.view=tempView;
            final TextView deviceName= tempView.findViewById(R.id.device_name);
            deviceName.setText(String.valueOf(temp.getName()));
            ImageView deviceImageType= tempView.findViewById(R.id.image_device_type);
            if(temp.pc){
                deviceImageType.setImageResource(R.drawable.send_icon_pc);
            } else deviceImageType.setImageResource(R.drawable.send_icon_mobile);
            final CardView deviceCard= (CardView) tempView.findViewById(R.id.base_device);
            deviceCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deviceCard.setCardBackgroundColor(getResources().getColor(R.color.card_selected_background));
                    choosenDeviceName= temp.getName();
                    for(int i=0; i<deviceList.size(); i++){
                        Device temp2= deviceList.get(i);
                        if(temp!=temp2){
                            CardView tempCard= (CardView) temp2.view.findViewById(R.id.base_device);
                            tempCard.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                        }
                    }
                }
            });
            refreshDevices.setRefreshing(false);
            deviceListLayout.addView(tempView);
        }
    }
}

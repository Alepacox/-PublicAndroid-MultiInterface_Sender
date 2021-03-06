package it.unicam.project.multiinterfacesender.Send;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unicam.project.multiinterfacesender.R;

public class Send extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public interface DataCommunication {
        public boolean getNoLoginMode();
    }
    private DataCommunication mListener;

    public Send() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Send.
     */
    // TODO: Rename and change types and number of parameters
    public static Send newInstance(String param1, String param2) {
        Send fragment = new Send();
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
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(!mListener.getNoLoginMode()) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.send_container, new Send_step_1_auto(), "")
                    .commit();
        } else getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.send_container, new Send_step_1_manual(), "")
                .commit();
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

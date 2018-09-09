package it.unicam.project.multiinterfacesender;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Send.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Send#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Send extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private StepperLayout mStepperLayout;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStepperLayout = (StepperLayout) getActivity().findViewById(R.id.stepperLayout);
        mStepperLayout.setAdapter(new MyStepperAdapter(getActivity().getSupportFragmentManager(), getContext()));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public static class MyStepperAdapter extends AbstractFragmentStepAdapter {
        private String CURRENT_STEP_POSITION_KEY= "Key";

        public MyStepperAdapter(FragmentManager fm, Context context) {
            super(fm, context);
        }
        @Override
        public Step createStep(int position) {
            if(position==0){
                Send_step_1 step = new Send_step_1();
                Bundle b = new Bundle();
                b.putInt(CURRENT_STEP_POSITION_KEY, position);
                step.setArguments(b);
                return step;
            } else if (position==1){
                Send_step_2 step2 = new Send_step_2();
                Bundle b2 = new Bundle();
                b2.putInt(CURRENT_STEP_POSITION_KEY, position);
                step2.setArguments(b2);
                return step2;
            } else {
                Send_step_3 step3 = new Send_step_3();
                Bundle b2 = new Bundle();
                b2.putInt(CURRENT_STEP_POSITION_KEY, position);
                step3.setArguments(b2);
                return step3;
            }

        }

        @Override
        public int getCount() {
            return 3;
        }

        @NonNull
        @Override
        public StepViewModel getViewModel(@IntRange(from = 0) int position) {
            //Override this method to set Step title for the Tabs, not necessary for other stepper types
            StepViewModel.Builder builder = new StepViewModel.Builder(context);
            if(position==0){
                builder
                        .setEndButtonLabel("Avanti")
                        .setBackButtonLabel("Indietro");
            } else if (position==1) {
                builder
                        .setEndButtonLabel("Connetti")
                        .setBackButtonLabel("Indietro");
            } else {
                builder
                        .setEndButtonLabel("Invia")
                        .setBackButtonLabel("Indietro");
            }
            return builder.create();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}

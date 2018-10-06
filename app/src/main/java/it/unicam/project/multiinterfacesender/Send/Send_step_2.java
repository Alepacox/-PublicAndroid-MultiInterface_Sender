package it.unicam.project.multiinterfacesender.Send;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;

public class Send_step_2 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PICKFILE_REQUEST_CODE = 126;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DataCommunication mListener;
    private Uri choosenFileUri;
    private String choosenFileName;

    public interface DataCommunication {
        public void setSwipable(boolean locked);
    }
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_step_2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton pickFileButton= getActivity().findViewById(R.id.pick_file_button);
        pickFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            }
        });
        Button buttonSend= getActivity().findViewById(R.id.button_next);
        buttonSend.setText("INVIA");
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(choosenFileUri!=null){
                    Intent myIntent = new Intent(getActivity(), Send_loading.class);
                    myIntent.putExtra("choosenFileName", choosenFileName);
                    //myIntent.putExtra("choosenFileUri", choosenFileUri);
                    getActivity().startActivity(myIntent);
                } else {
                    MainActivity.snackBarNav(getActivity(), R.id.send_container,
                            "Seleziona un file un file da inviare", Snackbar.LENGTH_SHORT, 0);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                try {
                    choosenFileUri = data.getData();
                    choosenFileName = null;
                    if (choosenFileUri.getScheme().equals("content")) {
                        Cursor cursor = getActivity().getContentResolver().query(choosenFileUri, null, null, null, null);
                        try {
                            if (cursor != null && cursor.moveToFirst()) {
                                choosenFileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                    if (choosenFileName == null) {
                        choosenFileName = choosenFileUri.getPath();
                        int cut = choosenFileName.lastIndexOf('/');
                        if (cut != -1) {
                            choosenFileName = choosenFileName.substring(cut + 1);
                        }
                    }
                    TextView fileNameText= getActivity().findViewById(R.id.file_name);
                    fileNameText.setText(choosenFileName);
                    fileNameText.setVisibility(View.VISIBLE);
                    TextView fileNameTitle= getActivity().findViewById(R.id.file_name_text);
                    fileNameTitle.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataCommunication) {
            mListener = (DataCommunication) context;
            mListener.setSwipable(false);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataCommunication");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.setSwipable(true);
        mListener = null;
    }
}

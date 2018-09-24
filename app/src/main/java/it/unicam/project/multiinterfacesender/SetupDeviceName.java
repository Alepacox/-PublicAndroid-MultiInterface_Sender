package it.unicam.project.multiinterfacesender;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutionException;

public class SetupDeviceName extends AppCompatActivity {
    private TextInputLayout idDeviceLayout;
    private EditText idDevice;
    private LoadingButton setupDoneButton;
    private String username;
    private String uToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_device_name);
        Intent myIntent= getIntent();
        username = myIntent.getStringExtra("username");
        uToken= myIntent.getStringExtra("uToken");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(SetupDeviceName.this,
                    android.R.color.black));
            getWindow().setNavigationBarColor(ContextCompat.getColor(SetupDeviceName.this,
                    android.R.color.black));
        }
        idDeviceLayout=findViewById(R.id.setup_input_layout_id);
        idDevice= findViewById(R.id.setup_input_id);
        idDevice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(idDeviceLayout.isErrorEnabled()){
                    idDeviceLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        setupDoneButton= findViewById(R.id.setup_done_button);
        setupDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String devicename= idDevice.getText().toString();
                if(devicename.length()==0){
                    idDevice.setError(getResources().getString(R.string.field_empty));
                    idDevice.setEnabled(false);
                } else {
                    setupDoneButton.setClickable(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            addNewDevice(devicename);
                            finish();
                        }
                    }).start();
                }
            }
        });

    }
    public void addNewDevice(String devicename){
        try {
            String output = new SyncToServerTasks.NewDeviceTask(uToken, devicename).execute().get();
            if (output != null) {
                if (!output.equals("unauthorized")) {
                    if (!output.equals("duplicate")) {
                        JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                        String dToken = object.getString("devicetoken");
                        Intent i = new Intent(SetupDeviceName.this, MainActivity.class);
                        i.putExtra("nologin", false);
                        i.putExtra("username", username);
                        i.putExtra("devicename", devicename);
                        i.putExtra("uToken", uToken);
                        i.putExtra("dToken", dToken);
                        startActivity(i);
                    } else resetView(true);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.session_expired), Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent i= new Intent(SetupDeviceName.this, Login.class);
                    startActivity(i);
                }
            } else resetView(false);
        } catch (Exception e) {
            e.printStackTrace();
            resetView(false);
        }
    }

    public void resetView(final boolean alreadyTakenDeviceName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                idDevice.setEnabled(true);
                setupDoneButton.setClickable(true);
                if(alreadyTakenDeviceName){
                    idDevice.setError(getResources().getString(R.string.devicename_already_picked));
                } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

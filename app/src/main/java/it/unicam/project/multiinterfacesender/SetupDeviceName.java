package it.unicam.project.multiinterfacesender;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.dx.dxloadingbutton.lib.LoadingButton;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class SetupDeviceName extends AppCompatActivity {
    private TextInputLayout idDeviceLayout;
    private EditText idDevice;
    private LoadingButton setupDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_device_name);
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
                writeNameToStorage();
            }
        });
    }

    public void writeNameToStorage() {
        if(idDevice.length()==0){
            idDevice.setError(getResources().getString(R.string.field_empty));
            return;
        }
        try {
            FileOutputStream fileout=openFileOutput("deviceID", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(idDevice.getText().toString());
            outputWriter.close();
            Intent i = new Intent(SetupDeviceName.this,MainActivity.class);
            i.putExtra("nologin", false);
            i.putExtra("deviceID", idDevice.getText().toString());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

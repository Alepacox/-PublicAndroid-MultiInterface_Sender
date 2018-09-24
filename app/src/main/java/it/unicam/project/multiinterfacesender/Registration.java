package it.unicam.project.multiinterfacesender;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.concurrent.ExecutionException;

public class Registration extends AppCompatActivity {
    private TextInputLayout inputUsernameLayout;
    private TextInputLayout inputPasswordLayout;
    private TextInputLayout inputEmailLayout;
    private TextInputLayout inputPasswordConfirmLayout;
    private EditText inputUsername;
    private EditText inputPassword;
    private EditText inputEmail;
    private EditText inputConfirmPassword;
    private ProgressBar progRegistrationButton;
    private FloatingActionButton registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        progRegistrationButton = findViewById(R.id.registration_progressBar);
        inputUsernameLayout = findViewById(R.id.reg_input_layout_username);
        inputPasswordLayout = findViewById(R.id.reg_input_layout_password);
        inputPasswordConfirmLayout = findViewById(R.id.reg_input_layout_confirm_password);
        inputEmailLayout = findViewById(R.id.reg_input_layout_email);
        inputUsername = findViewById(R.id.reg_input_username);
        inputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputUsernameLayout.isErrorEnabled()) {
                    inputUsernameLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputPassword = findViewById(R.id.reg_input_password);
        inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputPasswordLayout.isErrorEnabled()) {
                    inputPasswordLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEmail = findViewById(R.id.reg_input_email);
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputEmailLayout.isErrorEnabled()) {
                    inputEmailLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputConfirmPassword = findViewById(R.id.reg_input_confirm_password);
        inputConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputPasswordConfirmLayout.isErrorEnabled()) {
                    inputPasswordConfirmLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        registrationButton = findViewById(R.id.go_registration_button);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = inputUsername.getText().toString();
                final String password = inputPassword.getText().toString();
                String confirmPassword = inputConfirmPassword.getText().toString();
                final String email = inputEmail.getText().toString();
                if (userName.length() == 0 || password.length() == 0 ||
                        confirmPassword.length() == 0 || email.length() == 0 || !password.equals(confirmPassword)) {
                    if (userName.length() == 0) {
                        inputUsername.setError(getResources().getString(R.string.field_empty));
                    }
                    if (password.length() == 0) {
                        inputPassword.setError(getResources().getString(R.string.field_empty));
                    }
                    if (confirmPassword.length() == 0) {
                        inputConfirmPassword.setError(getResources().getString(R.string.field_empty));
                    }
                    if (email.length() == 0) {
                        inputEmail.setError(getResources().getString(R.string.field_empty));
                    }
                    if (!password.equals(confirmPassword)) {
                        inputConfirmPassword.setError(getResources().getString(R.string.psws_not_matching));
                    }
                    return;
                }
                progRegistrationButton.setVisibility(View.VISIBLE);
                inputUsername.setEnabled(false);
                inputPassword.setEnabled(false);
                inputConfirmPassword.setEnabled(false);
                inputEmail.setEnabled(false);
                registrationButton.setClickable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        registration(userName, password, email);
                    }
                }).start();
            }
        });
    }

    public void registration(String userName, String password, String email) {
        try {
            String output = new SyncToServerTasks.RegistationTask(userName, password, email).execute().get();
            if (output != null) {
                if(!output.equals("duplicate")){
                    JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                    String uToken= object.getString("usertoken");
                    final Intent i = new Intent(Registration.this, SetupDeviceName.class);
                    i.putExtra("nologin", false);
                    i.putExtra("username", userName);
                    i.putExtra("uToken", uToken);
                    startActivity(i);
                    finish();
                } else resetView(true);
            } else resetView(false);
        } catch (Exception e) {
            e.printStackTrace();
            resetView(false);
        }
    }

    public void resetView(final boolean usernameAlreadyTaken){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputUsername.setEnabled(true);
                inputPassword.setEnabled(true);
                inputConfirmPassword.setEnabled(true);
                inputEmail.setEnabled(true);
                registrationButton.setClickable(true);
                progRegistrationButton.setVisibility(View.INVISIBLE);
                if(usernameAlreadyTaken){
                    inputUsername.setError(getResources().getString(R.string.user_already_picked));
                } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

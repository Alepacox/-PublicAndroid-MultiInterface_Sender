package it.unicam.project.multiinterfacesender;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        progRegistrationButton= findViewById(R.id.registration_progressBar);
        inputUsernameLayout = findViewById(R.id.reg_input_layout_username);
        inputPasswordLayout= findViewById(R.id.reg_input_layout_password);
        inputPasswordConfirmLayout= findViewById(R.id.reg_input_layout_confirm_password);
        inputEmailLayout= findViewById(R.id.reg_input_layout_email);
        inputUsername = findViewById(R.id.reg_input_username);
        inputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputUsernameLayout.isErrorEnabled()){
                    inputUsernameLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputPassword= findViewById(R.id.reg_input_password);
        inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputPasswordLayout.isErrorEnabled()){
                    inputPasswordLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEmail= findViewById(R.id.reg_input_email);
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputEmailLayout.isErrorEnabled()){
                    inputEmailLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputConfirmPassword= findViewById(R.id.reg_input_confirm_password);
        inputConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputPasswordConfirmLayout.isErrorEnabled()){
                    inputPasswordConfirmLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        FloatingActionButton registrationButton= findViewById(R.id.go_registration_button);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });
    }
    public void registration(){
        String userName = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();
        String email = inputEmail.getText().toString();
        if(userName.length()== 0 || password.length()==0 || confirmPassword.length()==0 || email.length()==0){
            if(userName.length()== 0){
                inputUsername.setError(getResources().getString(R.string.field_empty));
            }
            if(password.length()== 0){
                inputPassword.setError(getResources().getString(R.string.field_empty));
            }
            if(confirmPassword.length()== 0){
                inputConfirmPassword.setError(getResources().getString(R.string.field_empty));
            }
            if(email.length()== 0){
                inputEmail.setError(getResources().getString(R.string.field_empty));
            }
            return;
        }
        progRegistrationButton.setVisibility(View.VISIBLE);
    }
}

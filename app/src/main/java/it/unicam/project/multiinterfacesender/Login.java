package it.unicam.project.multiinterfacesender;

import android.animation.Animator;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.AnimationType;
import com.dx.dxloadingbutton.lib.LoadingButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Login extends AppCompatActivity {
    private LoadingButton loginButton;
    private View animateView;
    private EditText inputName;
    private EditText inputPassword;
    private TextInputLayout inputNameLayout;
    private TextInputLayout inputPasswordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputNameLayout = findViewById(R.id.input_layout_name);
        inputPasswordLayout = findViewById(R.id.input_layout_password);
        animateView= findViewById(R.id.animate_view);
        inputName= findViewById(R.id.input_name);
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(inputNameLayout.isErrorEnabled()){
                    inputNameLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputPassword= findViewById(R.id.input_password);
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
        loginButton= findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
        Button noLoginButton= findViewById(R.id.no_login_button);
        noLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                i.putExtra("nologin", true);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        });
        LoadingButton registrationButton= findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Registration.class);
                startActivity(i);
            }
        });
    }

    private void checkLogin(){

        final String userName = inputName.getText().toString();
        final String password = inputPassword.getText().toString();


        if(userName.length() == 0 || password.length() == 0){
            if(userName.length() == 0) {
                inputName.setError(getResources().getString(R.string.field_empty));
            }
            if (password.length() == 0) {
                inputPassword.setError(getResources().getString(R.string.field_empty));
            }
            return;
        }

        loginButton.startLoading();

        //demo
        inputName.setEnabled(false);
        inputPassword.setEnabled(false);
        loginButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                inputName.setEnabled(true);
                inputPassword.setEnabled(true);
                if("admin".endsWith(userName) && "admin".equals(password)){
                    //login success
                    loginButton.loadingSuccessful();
                    loginButton.setAnimationEndAction(new Function1<AnimationType, Unit>() {
                        @Override
                        public Unit invoke(AnimationType animationType) {
                            toNextPage();
                            return Unit.INSTANCE;
                        }
                    });
                }else{
                    loginButton.loadingFailed();
                    Toast.makeText(getApplicationContext(),"Nome utente o password errati",Toast.LENGTH_SHORT).show();
                }
            }
        },3000);
    }

    private void toNextPage(){

        int cx = (loginButton.getLeft() + loginButton.getRight()) / 2;
        int cy = (loginButton.getTop() + loginButton.getBottom()) / 2;

        Animator animator = ViewAnimationUtils.createCircularReveal(animateView,cx,cy,0,getResources().getDisplayMetrics().heightPixels * 1.2f);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateInterpolator());
        animateView.setVisibility(View.VISIBLE);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    FileInputStream devicefile = openFileInput("deviceID");
                    InputStreamReader InputRead= new InputStreamReader(devicefile);

                    char[] inputBuffer= new char[100];
                    String deviceID="";
                    int charRead;

                    while ((charRead=InputRead.read(inputBuffer))>0) {
                        // char to string conversion
                        String readstring=String.copyValueOf(inputBuffer,0,charRead);
                        deviceID +=readstring;
                    }
                    InputRead.close();
                    Intent i = new Intent(Login.this,MainActivity.class);
                    i.putExtra("nologin", false);
                    i.putExtra("deviceID", deviceID);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } catch (FileNotFoundException e) {
                    Intent i = new Intent(Login.this, SetupDeviceName.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }
}

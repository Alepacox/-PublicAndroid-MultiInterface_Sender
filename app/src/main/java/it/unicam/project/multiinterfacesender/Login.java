package it.unicam.project.multiinterfacesender;

import android.animation.Animator;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.AnimationType;
import com.dx.dxloadingbutton.lib.LoadingButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Login extends AppCompatActivity {
    private LoadingButton loginButton;
    private View animateView;
    private EditText inputName;
    private EditText inputPassword;
    private TextInputLayout inputNameLayout;
    private TextInputLayout inputPasswordLayout;
    private String dToken;
    private Button noLoginButton;
    private LoadingButton registrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RWStorage rw= new RWStorage(this);
        dToken= rw.readDeviceToken();
        setContentView(R.layout.activity_login);
        inputNameLayout = findViewById(R.id.input_layout_name);
        inputPasswordLayout = findViewById(R.id.input_layout_password);
        animateView = findViewById(R.id.animate_view);
        inputName = findViewById(R.id.input_name);
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (inputNameLayout.isErrorEnabled()) {
                    inputNameLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputPassword = findViewById(R.id.input_password);
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
        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = inputName.getText().toString();
                final String password = inputPassword.getText().toString();


                if (username.length() == 0 || password.length() == 0) {
                    if (username.length() == 0) {
                        inputName.setError(getResources().getString(R.string.field_empty));
                    }
                    if (password.length() == 0) {
                        inputPassword.setError(getResources().getString(R.string.field_empty));
                    }
                    return;
                }
                loginButton.startLoading();
                loginButton.setEnabled(false);
                registrationButton.setEnabled(false);
                noLoginButton.setEnabled(false);
                inputName.setEnabled(false);
                inputPassword.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkLogin(username, password);
                    }
                }).start();
            }
        });
        noLoginButton = findViewById(R.id.no_login_button);
        noLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("nologin", true);
                startActivity(i);
                finish();
            }
        });
        registrationButton = findViewById(R.id.registration_button);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Registration.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void checkLogin(String username, String password) {
        try {
            if (dToken ==null) {
                String output = new SyncToServerTasks.LoginTask(username, password).execute().get();
                if (output != null) {
                    if(!output.equals("unauthorized")){
                        JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                        final String uToken = object.getString("usertoken");
                        final Intent i = new Intent(Login.this, SetupDeviceName.class);
                        i.putExtra("username", username);
                        i.putExtra("uToken", uToken);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginButton.loadingSuccessful();
                                loginButton.setAnimationEndAction(new Function1<AnimationType, Unit>() {
                                    @Override
                                    public Unit invoke(AnimationType animationType) {
                                        toNextPage(i);
                                        return Unit.INSTANCE;
                                    }
                                });
                            }
                        });
                    } else resetView(true);
                } else resetView(false);
            } else {
                String output = new SyncToServerTasks.LoginTask(username, password, dToken).execute().get();
                if (output != null) {
                    if (!output.equals("unauthorized")) {
                        JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                        final String uToken = object.getString("usertoken");
                        final String devicename = object.getString("devicename");
                        if(devicename.equals("null")){
                            final Intent i = new Intent(Login.this, SetupDeviceName.class);
                            i.putExtra("username", username);
                            i.putExtra("uToken", uToken);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginButton.loadingSuccessful();
                                    loginButton.setAnimationEndAction(new Function1<AnimationType, Unit>() {
                                        @Override
                                        public Unit invoke(AnimationType animationType) {
                                            toNextPage(i);
                                            return Unit.INSTANCE;
                                        }
                                    });
                                }
                            });

                        } else {
                            final Intent i = new Intent(Login.this, MainActivity.class);
                            i.putExtra("nologin", false);
                            i.putExtra("username", username);
                            i.putExtra("devicename", devicename);
                            i.putExtra("uToken", uToken);
                            i.putExtra("dToken", dToken);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginButton.loadingSuccessful();
                                    loginButton.setAnimationEndAction(new Function1<AnimationType, Unit>() {
                                        @Override
                                        public Unit invoke(AnimationType animationType) {
                                            toNextPage(i);
                                            return Unit.INSTANCE;
                                        }
                                    });
                                }
                            });
                        }
                    } else resetView(true);
                }else resetView(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resetView(false);
        }
    }

    public void resetView(final boolean wrongCredential){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputName.setEnabled(true);
                inputPassword.setEnabled(true);
                loginButton.setEnabled(true);
                registrationButton.setEnabled(true);
                noLoginButton.setEnabled(true);
                loginButton.loadingFailed();
                if(wrongCredential){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_user_psw), Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void toNextPage(final Intent i) {

        int cx = (loginButton.getLeft() + loginButton.getRight()) / 2;
        int cy = (loginButton.getTop() + loginButton.getBottom()) / 2;

        Animator animator = ViewAnimationUtils.createCircularReveal(animateView, cx, cy, 0, getResources().getDisplayMetrics().heightPixels * 1.2f);
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
                startActivity(i);
                finish();
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

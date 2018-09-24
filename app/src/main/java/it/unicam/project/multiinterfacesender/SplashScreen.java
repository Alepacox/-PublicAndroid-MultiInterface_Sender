package it.unicam.project.multiinterfacesender;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class SplashScreen extends AppCompatActivity {
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show the splash screen
        setContentView(R.layout.activity_splash_screen);
        mProgress = (ProgressBar) findViewById(R.id.splash_progressBar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(SplashScreen.this,
                    android.R.color.black));
            getWindow().setNavigationBarColor(ContextCompat.getColor(SplashScreen.this,
                    android.R.color.black));
        }
        // Start lengthy operation in a background thread
        RWStorage rw = new RWStorage(this);
        final String uToken = rw.readUserToken();
        final String dToken = rw.readDeviceToken();
        if (uToken != null && dToken != null) {
            mProgress.postOnAnimationDelayed(new Runnable() {
                @Override
                public void run() {
                    getInfo(uToken, dToken);
                    finish();
                }
            }, 1000);
        } else {
            Intent i = new Intent(SplashScreen.this, Login.class);
            startActivity(i);
            finish();
        }
    }

    private void getInfo(String uToken, String dToken) {
        try {
            String output = new SyncToServerTasks.GetInfoTask(uToken, dToken).execute().get();
            if (output != null) {
                JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                if (!object.getString("message").equals("unauthorized")) {
                    String username = object.getString("username");
                    String devicename = object.getString("devicename");
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    i.putExtra("nologin", false);
                    i.putExtra("username", username);
                    i.putExtra("devicename", devicename);
                    i.putExtra("uToken", uToken);
                    i.putExtra("dToken", dToken);
                    startActivity(i);
                } else {
                    Intent i = new Intent(SplashScreen.this, Login.class);
                    Toast.makeText(getApplicationContext(), R.string.session_expired, Toast.LENGTH_LONG);
                    startActivity(i);
                }
            } else {
                Intent i = new Intent(SplashScreen.this, Login.class);
                startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Intent i = new Intent(SplashScreen.this, Login.class);
            startActivity(i);
        }
    }


}

package it.unicam.project.multiinterfacesender.Receive;


import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import it.unicam.project.multiinterfacesender.MainActivity;
import it.unicam.project.multiinterfacesender.R;


public class Receive_loading extends AppCompatActivity {
    private TextView localAddress;
    private TextView bluetoothAddress;
    private TextView localAddressText;
    private TextView bluetoothAddressText;
    private TextView textGeneratedCode;
    private TextView generatedCode;
    private TextView shareSession;
    private TextView downloadingFileText;
    private TextView downloadingFile;
    private boolean blocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
        }
        Intent intent = getIntent();
        boolean manual= intent.getBooleanExtra("receivingManual", true);
        localAddress = findViewById(R.id.downloading_ip_local);
        bluetoothAddress = findViewById(R.id.downloading_bluetooth);
        localAddressText = findViewById(R.id.downloading_ip_local_text);
        bluetoothAddressText = findViewById(R.id.downloading_bluetooth_text);
        textGeneratedCode= findViewById(R.id.session_code_text);
        shareSession= findViewById(R.id.text_share_session);
        generatedCode= findViewById(R.id.session_code);
        downloadingFileText = findViewById(R.id.downloading_filename_text);
        downloadingFile = findViewById(R.id.downloading_filename);
        blocked=true;

        if(manual) {
            String wifiIp = intent.getStringExtra("wifiIp");
            String bluetoothName = intent.getStringExtra("bluetoothName");
            setManualView(wifiIp, bluetoothName);
        } else {
            String sessionCode= intent.getStringExtra("sessioncode");
            setAutoView(sessionCode);
        }
    }

    public void setAutoView(final String sessionCode){
        textGeneratedCode.setVisibility(View.VISIBLE);
        generatedCode.setVisibility(View.VISIBLE);
        shareSession.setVisibility(View.VISIBLE);
        generatedCode.setText(sessionCode);
        shareSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "MultiInterface Sender: il mio codice di connessione è "+ sessionCode);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    public void setDownloadingView(boolean comesFromManual, String downloadingFileName){
        //Making downloading file visible
        if(comesFromManual){
            localAddress.setVisibility(View.INVISIBLE);
            bluetoothAddress.setVisibility(View.INVISIBLE);
            localAddressText.setVisibility(View.INVISIBLE);
            bluetoothAddressText.setVisibility(View.INVISIBLE);
        } else {
            textGeneratedCode.setVisibility(View.INVISIBLE);
            generatedCode.setVisibility(View.INVISIBLE);
            shareSession.setVisibility(View.INVISIBLE);
        }
        downloadingFile.setVisibility(View.VISIBLE);
        downloadingFileText.setVisibility(View.VISIBLE);
        downloadingFile.setText(downloadingFileName);
    }
    public void setManualView(String wifiIp, String bluetoothName){
        if (!wifiIp.equals("null")) {
            localAddress.setText(wifiIp);
        }
        if (!bluetoothName.equals("null")) {
            bluetoothAddress.setText(bluetoothName);
        }
        localAddress.setVisibility(View.VISIBLE);
        bluetoothAddress.setVisibility(View.VISIBLE);
        localAddressText.setVisibility(View.VISIBLE);
        bluetoothAddressText.setVisibility(View.VISIBLE);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        MainActivity.snackBarNav(this, R.id.container_receive_loading,
                "Premi ancora indietro per chiudere la connessione", Snackbar.LENGTH_SHORT, 1);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

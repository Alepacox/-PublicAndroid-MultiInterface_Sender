package it.unicam.project.multiinterfacesender;


import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class Receive_loading extends AppCompatActivity {

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
        TextView localAddress = findViewById(R.id.downloading_ip_local);
        TextView remoteAddress = findViewById(R.id.downloading_ip_remote);
        TextView bluetoothAddress = findViewById(R.id.downloading_bluetooth);
        if(manual) {
            String wifiIp = intent.getStringExtra("wifiIp");
            String mobileIp = intent.getStringExtra("mobileIp");
            String bluetoothName = intent.getStringExtra("bluetoothName");
            if (wifiIp != null) {
                localAddress.setText(wifiIp);
            }
            if (mobileIp != null) {
                remoteAddress.setText(mobileIp);
            }
            if (bluetoothName != null) {
                bluetoothAddress.setText(bluetoothName);
            }
        } else {
            setDownloadingView("This is the file name");
        }
    }

    public void setDownloadingView(String downloadingFileName){
        //Making invisible parameters
        TextView localAddress = findViewById(R.id.downloading_ip_local);
        TextView remoteAddress = findViewById(R.id.downloading_ip_remote);
        TextView bluetoothAddress = findViewById(R.id.downloading_bluetooth);
        TextView localAddressText = findViewById(R.id.downloading_ip_local_text);
        TextView remoteAddressText = findViewById(R.id.downloading_ip_remote_text);
        TextView bluetoothAddressText = findViewById(R.id.downloading_bluetooth_text);
        localAddress.setVisibility(View.INVISIBLE);
        remoteAddress.setVisibility(View.INVISIBLE);
        bluetoothAddress.setVisibility(View.INVISIBLE);
        localAddressText.setVisibility(View.INVISIBLE);
        remoteAddressText.setVisibility(View.INVISIBLE);
        bluetoothAddressText.setVisibility(View.INVISIBLE);
        //Making downloading file visible
        TextView downloadingFileText = findViewById(R.id.downloading_filename_text);
        TextView downloadingFile = findViewById(R.id.downloading_filename);
        downloadingFile.setVisibility(View.VISIBLE);
        downloadingFileText.setVisibility(View.VISIBLE);
        downloadingFile.setText(downloadingFileName);
    }
}

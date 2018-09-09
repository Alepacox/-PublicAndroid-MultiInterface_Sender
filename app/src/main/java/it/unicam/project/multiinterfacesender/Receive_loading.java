package it.unicam.project.multiinterfacesender;


import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class Receive_loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String wifiIp = intent.getStringExtra("wifiIp");
        String mobileIp = intent.getStringExtra("mobileIp");
        String bluetoothName = intent.getStringExtra("bluetoothName");
        setContentView(R.layout.activity_receive_loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this,
                    R.color.receivePrimaryColor));
        }
        if(wifiIp!=null){
            TextView localAddress= findViewById(R.id.downloading_ip_local);
            localAddress.setText(wifiIp);
        }
        if(mobileIp!=null){
            TextView remoteAddress= findViewById(R.id.downloading_ip_remote);
            remoteAddress.setText(mobileIp);
        }
        if(bluetoothName!=null){
            TextView bluetoothAddress= findViewById(R.id.downloading_bluetooth);
            bluetoothAddress.setText(bluetoothName);
        }

    }
}

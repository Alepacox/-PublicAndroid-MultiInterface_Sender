package it.unicam.project.multiinterfacesender;


import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Send_loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_loading);
        Intent intent= getIntent();
        String choosenFileName = intent.getStringExtra("choosenFileName");
//        Uri choosenFileUri = Uri.parse(intent.getStringExtra("choosenFileUri"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,
                    R.color.sendPrimaryColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this,
                    R.color.sendPrimaryColor));
        }
        TextView filenameTextView= findViewById(R.id.sending_text_filename);
        filenameTextView.setText(choosenFileName);

    }
}

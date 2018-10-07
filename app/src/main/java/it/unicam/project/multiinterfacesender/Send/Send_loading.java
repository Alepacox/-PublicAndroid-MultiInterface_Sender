package it.unicam.project.multiinterfacesender.Send;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

import it.unicam.project.multiinterfacesender.DirectlyConnect;
import it.unicam.project.multiinterfacesender.R;
import it.unicam.project.multiinterfacesender.Receive.SendedData;
import it.unicam.project.multiinterfacesender.Streamer;
import it.unicam.project.multiinterfacesender.StreamerRes;

public class Send_loading extends AppCompatActivity {
    static boolean active = false;
    private Streamer fileSource;
    private String choosenFileName;
    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 52: //READ PERCENTAGE
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_loading);
        Intent intent = getIntent();
        choosenFileName = intent.getStringExtra("choosenFileName");
        String stringUri = intent.getStringExtra("choosenFileUri");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this,
                    R.color.sendPrimaryColor));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this,
                    R.color.sendPrimaryColor));
        }
        TextView filenameTextView = findViewById(R.id.sending_text_filename);
        filenameTextView.setText(choosenFileName);
        Uri choosenFileUri = Uri.parse(stringUri);
        try {
            fileSource = new Streamer(getContentResolver().openInputStream(choosenFileUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    private void Bluetooth_Sender() {
        new Thread(() -> {
            try {
                //endregion
                while (fileSource.available() > 0) {
                    StreamerRes res;
                    if (fileSource.available() >= 131072)
                        res = fileSource.read(new byte[131072]);
                    else
                        res = fileSource.read(new byte[fileSource.available()]);

                    DirectlyConnect.bluetoothSendService.write(new SendedData(res.Message, choosenFileName));
                }

                DirectlyConnect.bluetoothSendService.write(new SendedData(new byte[0]));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void Mobile_Sender() {
        new Thread(() -> {
            try {
                ArrayList<byte[]> toSend;
                while (fileSource.available() > 0) {
                    StreamerRes res;
                    if (fileSource.available() >= 131072)
                        res = fileSource.read(new byte[131072]);
                    else
                        res = fileSource.read(new byte[fileSource.available()]);

                    toSend = splitByteArray(serialize(new SendedData(res.Message, choosenFileName)), 399860);
                    for (byte[] bytes : toSend) {
                        DirectlyConnect.iService_app_to_mobile.setupPackage(bytes);
                    }
                    DirectlyConnect.iService_app_to_mobile.sendPackage();
                }

                toSend = splitByteArray(serialize(new SendedData(new byte[0])), 399860);
                for (byte[] bytes : toSend) {
                    DirectlyConnect.iService_app_to_mobile.setupPackage(bytes);
                }
                DirectlyConnect.iService_app_to_mobile.sendPackage();
            } catch (RemoteException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void Wifi_Sender() {
        new Thread(() -> {
            try {
                ArrayList<byte[]> toSend;
                while (fileSource.available() > 0) {
                    StreamerRes res;
                    if (fileSource.available() >= 131072)
                        res = fileSource.read(new byte[131072]);
                    else
                        res = fileSource.read(new byte[fileSource.available()]);

                    toSend = splitByteArray(serialize(new SendedData(res.Message, choosenFileName)), 399860);
                    for (byte[] bytes : toSend) {
                        DirectlyConnect.iService_app_to_wifi.setupPackage(bytes);
                    }
                    DirectlyConnect.iService_app_to_wifi.sendPackage();
                }

                toSend = splitByteArray(serialize(new SendedData(new byte[0])), 399860);
                for (byte[] bytes : toSend) {
                    DirectlyConnect.iService_app_to_wifi.setupPackage(bytes);
                }
                DirectlyConnect.iService_app_to_wifi.sendPackage();
            } catch (RemoteException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public ArrayList<byte[]> splitByteArray(byte[] input, int chunkSize){
        int chunkCount = 0;
        ArrayList<byte[]> res = new ArrayList<>();
        while ((chunkCount * chunkSize) < input.length) {
            byte[] chunk;
            if (((chunkCount * chunkSize) + (chunkSize)) > input.length) {
                chunk = new byte[input.length - (chunkCount * chunkSize)];
                System.arraycopy(input, chunkCount * chunkSize, chunk, 0, input.length - (chunkCount * chunkSize));
            } else {
                chunk = new byte[chunkSize];
                System.arraycopy(input, chunkCount * chunkSize, chunk, 0, chunkSize);
            }
            res.add(chunk);
            chunkCount++;
        }
        return res;
    }

    public byte[] serialize(SendedData in) {
        byte[] out = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput objOut;
        try {
            objOut = new ObjectOutputStream(bos);
            objOut.writeObject(in);
            objOut.flush();
            out = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return out;
    }

    public SendedData deSerialize(byte[] in) {
        SendedData out = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        ObjectInput objIn = null;
        try {
            objIn = new ObjectInputStream(bis);
            out = (SendedData) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objIn != null) {
                    objIn.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return out;
    }
}

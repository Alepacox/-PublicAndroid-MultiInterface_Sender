package it.unicam.project.multiinterfacesender;

import android.os.Message;

import java.io.IOException;
import java.io.InputStream;

import it.unicam.project.multiinterfacesender.Send.Send_loading;

public class Streamer {
    private InputStream inputStream;
    private int size;
    private byte chunk_ID0 = -128;
    private byte chunk_ID1 = -128;

    public Streamer(InputStream inputStream) {
        this.inputStream = inputStream;
        try {
            size = this.inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized StreamerRes read(byte[] buffer) throws IOException {
        inputStream.read(buffer);
        StreamerRes res = new StreamerRes(new byte[]{chunk_ID1, chunk_ID0}, buffer);
        if (chunk_ID0 == 127) {
            chunk_ID0 = -128;
            chunk_ID1++;
        } else {
            chunk_ID0++;
        }
        Send_loading.mHandler.obtainMessage(52, (size/100*(size-available())), -1).sendToTarget();
        return res;
    }

    public int getSize() {
        return size;
    }

    public int available() {
        int out;
        try {
            out = inputStream.available();
        } catch (IOException e) {
            out = 0;
        }
        return out;
    }
}

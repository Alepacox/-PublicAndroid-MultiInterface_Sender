package it.unicam.project.multiinterfacesender.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import it.unicam.project.multiinterfacesender.Receive.SendedData;

public class BluetoothReceive {
    private static final UUID MY_UUID = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    private OnMessageReceived mMessageListener;
    private int mState;
    private Handler mHandler;
    //private BluetoothDevice mSavedDevice;
    //private int mConnectionLostCount;

    // Constants that indicate the current connection state
    public final static int BT_RECEIVE_MESSAGE_STATE_CHANGE =3;
    public final static int BT_RECEIVE_STATE_CONNECTED =32;
    public final static int BT_RECEIVE_STATE_ERROR =33;
    public final static int BT_RECEIVE_STATE_LOST_CONNECTION =34;

    // Constants that indicate command to computer
    private static final int EXIT_CMD = -1;

    public BluetoothReceive(Handler handler, OnMessageReceived listener) {
        this.mHandler= handler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mMessageListener = listener;
    }

    public synchronized void connect() {
        clear();
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    private void connected(BluetoothSocket socket) throws IOException {
        //clear();
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

    }

    public static synchronized void clear() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            if(mConnectThread.isAlive()){
                mConnectThread.interrupt();
            }
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            if(mConnectedThread.isAlive()){
                mConnectedThread.interrupt();
            }
            mConnectedThread = null;
        }
    }
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BT_RECEIVE_STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        if(mHandler!=null){
            mHandler.obtainMessage(BT_RECEIVE_MESSAGE_STATE_CHANGE, BT_RECEIVE_STATE_LOST_CONNECTION, -1).sendToTarget();
        }
        /*mConnectionLostCount++;
        if (mConnectionLostCount < 3) {
        	// Send a reconnect message back to the Activity
	        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
	        Bundle bundle = new Bundle();
	        bundle.putString(MainActivity.TOAST, "Device connection was lost. Reconnecting...");
	        msg.setData(bundle);
	        mHandler.sendMessage(msg);

        	connect(mSavedDevice);
        } else {*/
        //setState(STATE_LISTEN);
        /*// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.BT_MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        }*/
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private BluetoothSocket socket;

        ConnectThread() {
            BluetoothSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("MultiInterfaceSender", MY_UUID);
            } catch (IOException e) {
                if(mHandler!=null){
                    mHandler.obtainMessage(BT_RECEIVE_MESSAGE_STATE_CHANGE, BT_RECEIVE_STATE_ERROR, -1).sendToTarget();
                }
            }
        }

        public void run() {
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (socket != null) {
                    try {
                        connected(socket);
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("BT connection", "estabilished");
                    break;
                }
            }

        }

        void cancel() {
            try {
                mHandler=null;
                if(socket!=null)socket.close();
            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            Log.e("I'm here", "Inside connected thread");
            // Get the input and output streams; using temp objects because
            // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                    Log.e("I'm here", "Inside io allocation");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TAG", "Error occurred when creating input stream", e);
                }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            if(mHandler!=null){
                mHandler.obtainMessage(BT_RECEIVE_MESSAGE_STATE_CHANGE, BT_RECEIVE_STATE_CONNECTED, -1).sendToTarget();
            }
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectedThread");
            // Keep listening to the InputStream while connected
            try {
                objectOutputStream= new ObjectOutputStream(mmOutStream);
                objectInputStream=new ObjectInputStream(mmInStream);
            } catch (IOException e) {
                connectionLost();
                e.printStackTrace();
            }
            while (true) {
                try {
                    SendedData s;
                    if (objectOutputStream == null) {
                        return;
                    }
                    Object o = objectInputStream.readUnshared();
                    if (o == null)
                        return;
                    else
                        s = (SendedData) o;
                    mMessageListener.messageReceived(s);
                    //objectOutputStream.reset();

                } catch (Exception e2) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        void write(byte[] buffer) {
            try {
                objectOutputStream.writeUnshared(buffer);
                objectOutputStream.flush();
                objectOutputStream.reset();
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
//                Log.e(TAG, "Exception during write", e);
            }
        }

        void cancel() {
            try {
                mHandler=null;
                objectOutputStream.write(EXIT_CMD);
                if(mmSocket!=null){
                    mmSocket.close();
                }
            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public interface OnMessageReceived {
        void messageReceived(SendedData message);
    }
}

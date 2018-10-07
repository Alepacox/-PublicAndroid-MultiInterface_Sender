package it.unicam.project.multiinterfacesender.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

public class BluetoothSend {
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
    public final static int BT_SEND_MESSAGE_STATE_CHANGE =2;
    public final static int BT_SEND_STATE_CONNECTED =21;
    public final static int BT_SEND_STATE_NOT_FOUND =22;
    public final static int BT_SEND_STATE_LOST_CONNECTION =23;

    // Constants that indicate command to computer
    private static final int EXIT_CMD = -1;

    public BluetoothSend(Handler handler) {
        this.mHandler= handler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void connect(BluetoothDevice device) {
        clear();
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }


    private void connected(BluetoothSocket socket) {
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
    public void write(SendedData out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        if(mHandler!=null){
        mHandler.obtainMessage(BT_SEND_MESSAGE_STATE_CHANGE, BT_SEND_STATE_NOT_FOUND, -1).sendToTarget();
        }
        //setState(BT_STATE_LISTEN);

        /*// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(0);//MainActivity.BT_MESSAGE_TOAST
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        if(mHandler!=null){
            mHandler.obtainMessage(BT_SEND_MESSAGE_STATE_CHANGE, BT_SEND_STATE_LOST_CONNECTION, -1).sendToTarget();
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
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(mHandler!=null){
                    mHandler.obtainMessage(BT_SEND_MESSAGE_STATE_CHANGE, BT_SEND_STATE_NOT_FOUND, -1).sendToTarget();
                }
            }
            mmSocket = tmp;
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            if(mAdapter.isDiscovering()) mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
//                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                clear();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSend.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        void cancel() {
            try {
                mHandler=null;
                mmSocket.close();
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

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            long t= System.currentTimeMillis();
            long end = t+10000;
            while (objectOutputStream == null || objectInputStream == null) {
                if(System.currentTimeMillis() >= end) {
                    cancel();
                    break;
                }
                if (objectOutputStream == null && socket != null) {
                    try {
                        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
                if (objectInputStream == null && socket != null) {
                    Log.e("LOOP", "here");
                    try {
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
            Log.e("Out of while", "true");
            if(mHandler!=null){
                mHandler.obtainMessage(BT_SEND_MESSAGE_STATE_CHANGE, BT_SEND_STATE_CONNECTED, -1).sendToTarget();
            }
        }

        public void run() {
//            Log.i(TAG, "BEGIN mConnectedThread");
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    byte[] s;
                    if (objectInputStream == null) {
                        System.out.println("objectInputStream == null");
                        return;
                    }
                    Object o = objectInputStream.readUnshared();
                    if (o == null)
                        return;
                    else
                        s = (byte[]) o;
                    mMessageListener.messageReceived(s);
                    //objectInputStream.reset();

                } catch (Exception e) {
//                    Log.e(TAG, "disconnected", e);
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
        void write(SendedData buffer) {
            try {
                objectOutputStream.writeUnshared(buffer);
                objectOutputStream.flush();
                objectOutputStream.reset();
                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                return;
            }
        }

        void cancel() {
            try {
                mHandler=null;
                objectOutputStream.write(EXIT_CMD);
                mmSocket.close();
            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public interface OnMessageReceived {
        void messageReceived(byte[] message);
    }
}

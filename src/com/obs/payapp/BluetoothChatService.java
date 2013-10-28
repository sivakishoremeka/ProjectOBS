/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obs.payapp;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.obs.object.PrintInfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.widget.Toast;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothConnectivityService";
    private static final boolean D = true;
    // Member fields
    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    String cname;
    String name;
    private SharedPreferences mPrefs;
    protected final static String PREFS_FILE = "mifosAppPrefs";
    PrintInfo printinfo;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    InputStream inputStream = null;
    OutputStream outputStream = null;
    BluetoothSocket printerSocket=null;
    BluetoothDevice bluetoothDevice=null;
    Handler mHandler;
    byte[] fpDatabytes = null;
    byte ENROLL_ID= 0x21;
	byte ILV_OK=0x00;
    byte ILVSTS_OK=0x00;
    byte ISO_197942=(byte)0x6E;
    byte ILVSTS_HIT=0x01;
    byte ILVSTS_NO_HIT=0x02;
    
    Context ctx;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler=handler;
        ctx = context;
        mPrefs = ctx.getSharedPreferences(PREFS_FILE, 0);
        printinfo = new PrintInfo();
        printinfo.setName(mPrefs.getString("USER_ID", "user"));
        printinfo.setClientId(mPrefs.getString(PaymentActivity.CLIENT_ID, "clientId"));
        printinfo.setClientName(mPrefs.getString(PaymentActivity.CLIENT_NAME, "clientName"));
        printinfo.setPaymentCode(mPrefs.getString(PaymentActivity.PAYMENT_CODE, "paymentCode"));
        printinfo.setAmountPaid(mPrefs.getString(PaymentActivity.AMOUNT_PAID, "amountPaid"));
        //cname = mPrefs.getString("CNAME", "CNAME");
      
        name = mPrefs.getString("USER_ID", cname);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
        	/**
             * Start the ConnectThread to initiate a connection to a remote device.
             * @param device  The BluetoothDevice to connect
             */ if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Cancel all the thread those are done with their action part
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,String cname) {
        if (D) Log.d(TAG, "connected");
        printerSocket=socket;
        bluetoothDevice=device;
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to manage the connection and perform transmissions
        if(printerSocket==null){System.out.println("Socket is null in the enrollprinter");}else{System.out.println("Socket is active");}
        mConnectedThread = new ConnectedThread(this.printerSocket,this.bluetoothDevice,this,ctx);
        //mConnectedThread.start();
       // Toast.makeText(ctx, "Option"+BluetoothChatActivity.iOption, Toast.LENGTH_LONG).show();
        //mConnectedThread.MagCard();
        if(BluetoothChatActivity.iOption == 1)
        {
        	mConnectedThread.PrintdataFun(printinfo);
        	
        }
        else if(BluetoothChatActivity.iOption == 2)
        {
        	//Toast.makeText(ctx, "Second option", Toast.LENGTH_LONG).show();
        	mConnectedThread.MagCard();   
        }        
        setState(STATE_CONNECTED);
    }
   
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     */

   // public void readImage(){mConnectedThread.readImagedata();}
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
    	// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	// Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothChatActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }
	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
     private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
            	Method m = null;
				try {
					m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            	tmp = (BluetoothSocket) m.invoke(device, 1);
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                
            } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            mmSocket = tmp;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
				//mmSocket.getRemoteDevice().
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                e.printStackTrace();
                Log.e(this.toString(), "IOException " + e.getMessage());
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            //Start the connected thread
            connected(mmSocket, mmDevice,cname);
        }

        public void cancel() {
            try {
                mmSocket.close();
                Log.e(TAG, "Socket is closed");
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}

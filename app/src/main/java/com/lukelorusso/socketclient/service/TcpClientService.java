package com.lukelorusso.socketclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class TcpClientService extends Service {

    public interface TcpClientListener {
        void onMessageReceived(String message);
    }

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private TcpClientListener mListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    // used to get the service's instance
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setTcpClientListener(TcpClientListener listener) {
        mListener = listener;
    }

    public class LocalBinder extends Binder {
        public TcpClientService getServiceInstance(){
            return TcpClientService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        run();
        return START_STICKY;
    }

    private void run() {
        mRun = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    //here you must put your computer's IP address.
                    InetAddress serverAddr = InetAddress.getByName(TcpClientConfig.SERVER_IP);

                    //create a socket to make the connection with the server
                    Socket socket = new Socket(serverAddr, TcpClientConfig.SERVER_PORT);

                    try {
                        //sends the message to the server
                        mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        //receives the message which the server sends back
                        mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        //in this while the client listens for the messages sent by the server
                        while (mRun) {
                            mServerMessage = mBufferIn.readLine();

                            if (mServerMessage != null && mListener != null) {
                                //call the method messageReceived from MyActivity class
                                mListener.onMessageReceived(mServerMessage);
                            }
                        }

                    } catch (SocketException ignored) {
                    } catch (Exception e) {
                        Log.e("TCP", "S: Error", e);

                    } finally {
                        stopClient();
                    }

                } catch (Exception e) {
                    Log.e("TCP", "C: Error", e);
                }
            }
        }.start();
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        new Thread() {
            @Override
            public void run() {
                if (mBufferOut != null && !mBufferOut.checkError()) {
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        stopClient();
        super.onDestroy();
    }

    /**
     * Close the connection and release the members
     */
    private void stopClient() {
        mRun = false;
        mListener = null;
        mServerMessage = null;

        new Thread() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    mBufferOut.flush();
                    mBufferOut.close();
                }
                mBufferIn = null;
                mBufferOut = null;
            }
        }.start();
    }
}

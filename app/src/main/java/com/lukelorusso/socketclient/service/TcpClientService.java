package com.lukelorusso.socketclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static com.lukelorusso.socketclient.service.TcpClientConfig.*;

public class TcpClientService extends Service {

    public interface TcpClientListener {
        void onMessageReceived(String message);
        void onExceptionThrown(String message);
        void onServiceStarted();
        void onServiceStopped();
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

    private boolean mRememberToNotifyStart = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setTcpClientListener(TcpClientListener listener) {
        mListener = listener;
        if (mListener != null && mRememberToNotifyStart) {
            mListener.onServiceStarted();
            mRememberToNotifyStart = false;
        }
    }

    public class LocalBinder extends Binder {
        public TcpClientService getServiceInstance() {
            return TcpClientService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        run();
        return START_STICKY;
    }

    private void run() {
        new Thread() {
            @Override
            public void run() {
                try {
                    // creates a socket address from a hostname and a port number
                    SocketAddress address = new InetSocketAddress(
                            SERVER_HOST,
                            SERVER_PORT
                    );
                    Socket socket = new Socket();

                    try {
                        socket.connect(address, TIMEOUT_IN_MILLIS);
                        mRun = true;

                        // sends the message to the server
                        mBufferOut = new PrintWriter(
                                new BufferedWriter(
                                        new OutputStreamWriter(
                                                socket.getOutputStream()
                                        )
                                ),
                                true
                        );

                        // receives the message which the server sends back
                        mBufferIn = new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        );

                        // notify service started
                        if (mListener != null) {
                            mListener.onServiceStarted();
                        } else {
                            mRememberToNotifyStart = true;
                        }

                        // in this while the client listens for the messages sent by the server
                        while (mRun) {
                            mServerMessage = mBufferIn.readLine();

                            if (mServerMessage != null && mListener != null) {
                                // notify message received
                                mListener.onMessageReceived(mServerMessage);
                            }
                        }

                    } catch (SocketException exception) {
                        onExceptionThrown("SocketException - "
                                + SERVER_HOST + ":"
                                + SERVER_PORT + " "
                                + exception.getMessage()
                        );

                    } catch (SocketTimeoutException exception) {
                        onExceptionThrown("SocketTimeoutException - "
                                + SERVER_HOST + ":"
                                + SERVER_PORT + " "
                                + exception.getMessage()
                        );

                    } catch (IOException exception) {
                        onExceptionThrown("IOException - Unable to connect to "
                                + SERVER_HOST + ":"
                                + SERVER_PORT + " "
                                + exception.getMessage()
                        );

                    } catch (Exception exception) {
                        onExceptionThrown("ServerError - " + exception.getMessage());

                    } finally {
                        stopClient();
                    }

                } catch (Exception exception) {
                    onExceptionThrown("ClientError - " + exception.getMessage());
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
        new Thread() {
            @Override
            public void run() {
                mRun = false;
                if (mListener != null) {
                    // notify service stopped
                    mListener.onServiceStopped();
                    mListener = null;
                }
                mServerMessage = null;
                try {
                    if (mBufferOut != null) {
                        mBufferOut.flush();
                        mBufferOut.close();
                    }
                } catch (NullPointerException ignored) {}
                mBufferIn = null;
                mBufferOut = null;
            }
        }.start();
    }

    private void onExceptionThrown(String message) {
        Log.e(EXCEPTION_LOG_TAG, message);
        if (mListener != null) {
            mListener.onExceptionThrown(message);
        }
    }
}

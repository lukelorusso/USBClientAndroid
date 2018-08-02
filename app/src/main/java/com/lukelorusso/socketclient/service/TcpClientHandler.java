package com.lukelorusso.socketclient.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class TcpClientHandler {

    private static TcpClientHandler mInstance;
    private ServiceConnection mTcpServiceConnection;
    private TcpClientService mTcpClientService;
    private Intent mTcpServiceIntent;

    private TcpClientHandler() {
    }

    /**
     * If no instance of the handler exists, establish a connection and get the instance
     */
    public static TcpClientHandler getInstance(
            Context applicationContext,
            TcpClientService.TcpClientListener listener
    ) {
        if (mInstance == null) {
            mInstance = new TcpClientHandler();
            mInstance.start(applicationContext, listener);
            return mInstance;

        } else {
            mInstance.updateListener(listener);
            return mInstance;
        }
    }

    private void updateListener(TcpClientService.TcpClientListener listener) {
        mTcpClientService.setTcpClientListener(listener);
    }

    private void start(Context context, final TcpClientService.TcpClientListener listener) {
        mTcpServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                TcpClientService.LocalBinder binder = (TcpClientService.LocalBinder) iBinder;
                mTcpClientService = binder.getServiceInstance(); // getting service's instance
                mTcpClientService.setTcpClientListener(listener);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        mTcpServiceIntent = new Intent(context, TcpClientService.class);
        context.startService(mTcpServiceIntent);
        context.bindService(
                mTcpServiceIntent,
                mTcpServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    public TcpClientHandler reconnect(
            Context applicationContext,
            TcpClientService.TcpClientListener listener
    ) {
        this.stop(applicationContext);
        mInstance = new TcpClientHandler();
        mInstance.start(applicationContext, listener);
        return mInstance;
    }

    public void stop(Context applicationContext) {
        applicationContext.unbindService(mTcpServiceConnection);
        applicationContext.stopService(mTcpServiceIntent);
        mInstance = null;
    }

    public boolean isServiceActive() {
        return mTcpClientService != null;
    }

    public boolean send(String message) {
        if (isServiceActive()) {
            mTcpClientService.sendMessage(message);
            return true;
        }
        return false;
    }
}

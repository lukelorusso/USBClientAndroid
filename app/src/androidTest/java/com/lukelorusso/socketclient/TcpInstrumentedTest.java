package com.lukelorusso.socketclient;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.lukelorusso.socketclient.service.TcpClientHandler;
import com.lukelorusso.socketclient.service.TcpClientService;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TcpInstrumentedTest {

    private TcpClientHandler mTcpClientHandler;
    private String mHypotheticalMessages;

    private Context getApplicationContext() {
        return InstrumentationRegistry.getTargetContext().getApplicationContext();
    }

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    private void closeConnection() {
        mTcpClientHandler.stop(getApplicationContext());
        mTcpClientHandler = null;
    }

    @Test
    public void checkPackageName() {
        assertEquals("com.lukelorusso.socketclient", getContext().getPackageName());
    }

    @Test
    public void getTcpConnection() {
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {}
                }
        );
        assertNotNull(mTcpClientHandler);
        mTcpClientHandler.stop(getApplicationContext());
        mTcpClientHandler = null;
    }

    @Test
    public void sendMessage() {
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {}
                }
        );
        try {
            Thread.sleep(1000);
            mTcpClientHandler.send("Testing message sent");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(mTcpClientHandler);
        closeConnection();
    }

    @Test
    public void receiveMessage() {
        final int WAIT_FOR_TCP_MESSAGE_IN_MILLIS = 15000;

        mHypotheticalMessages = null;
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {
                        mHypotheticalMessages = message;
                    }
                }
        );
        try {
            Thread.sleep(WAIT_FOR_TCP_MESSAGE_IN_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(mHypotheticalMessages);
        closeConnection();
        mHypotheticalMessages = null;
    }

}

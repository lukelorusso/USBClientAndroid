package com.lukelorusso.socketclient;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.lukelorusso.socketclient.service.TcpClientHandler;
import com.lukelorusso.socketclient.service.TcpClientService;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.lukelorusso.socketclient.service.TcpClientConfig.*;
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

    /**
     * Start the connection with the server (if listening).
     * Failure is no service is running, success otherwise (will not have a connected confirmation).
     */
    @Test
    public void getTcpConnection() {
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {}

                    @Override
                    public void onExceptionThrown(String message) {}

                    @Override
                    public void onServiceStarted() {}

                    @Override
                    public void onServiceStopped() {}
                }
        );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(mTcpClientHandler);
        assertTrue(mTcpClientHandler.isServiceActive());
        mTcpClientHandler.stop(getApplicationContext());
        mTcpClientHandler = null;
    }

    /**
     * Send a message to the server.
     * Failure is no service is running, success otherwise (will not have a reception confirmation).
     */
    @Test
    public void sendMessage() {
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {}

                    @Override
                    public void onExceptionThrown(String message) {}

                    @Override
                    public void onServiceStarted() {}

                    @Override
                    public void onServiceStopped() {}
                }
        );
        boolean isServiceActive = false;
        try {
            Thread.sleep(1000);
            isServiceActive = mTcpClientHandler.send("Testing message sent");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            assertNotNull(mTcpClientHandler);
            assertTrue(isServiceActive);
            closeConnection();
        }
    }

    /**
     * Wait for "TIMEOUT_IN_MILLIS" to receive a message from the server.
     * Success if message received, failure otherwise.
     */
    @Test
    public void receiveMessage() {
        mHypotheticalMessages = null;
        mTcpClientHandler = TcpClientHandler.getInstance(
                getApplicationContext(),
                new TcpClientService.TcpClientListener() {
                    @Override
                    public void onMessageReceived(String message) {
                        mHypotheticalMessages = message;
                    }

                    @Override
                    public void onExceptionThrown(String message) {}

                    @Override
                    public void onServiceStarted() {}

                    @Override
                    public void onServiceStopped() {}
                }
        );
        long startTime = System.currentTimeMillis();
        long actualTime = startTime;
        while (actualTime - startTime < TIMEOUT_IN_MILLIS && mHypotheticalMessages == null) {
            actualTime = System.currentTimeMillis();
        }
        assertNotNull(mTcpClientHandler);
        assertTrue(mTcpClientHandler.isServiceActive());
        assertNotNull(mHypotheticalMessages);
        closeConnection();
    }

}

package com.lukelorusso.socketclient.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lukelorusso.socketclient.R;
import com.lukelorusso.socketclient.adapter.MessageListAdapter;
import com.lukelorusso.socketclient.service.TcpClientHandler;
import com.lukelorusso.socketclient.service.TcpClientService;

public class MainActivity extends Activity implements TcpClientService.TcpClientListener {

    private ArrayList<String> mMessageList;
    private MessageListAdapter mAdapter;
    private TcpClientHandler mTcpClientHandler;

    @Override
    public void onMessageReceived(final String message) {
        addToMessageList("[S] " + message);
    }

    @Override
    public void onServiceStarted() {
        addToMessageList(getString(R.string.service_started));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageList = new ArrayList<>();
        final EditText editText = findViewById(R.id.editText);
        Button send = findViewById(R.id.send_button);
        Button reconnect = findViewById(R.id.reconnect_button);

        //relate the listView from java to the one created in xml
        ListView listView = findViewById(R.id.list);
        mAdapter = new MessageListAdapter(this, mMessageList);
        listView.setAdapter(mAdapter);

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reconnectTcpClient();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString();

                // try to sends the message to the server...
                if (sendViaTcpClient(message)) {
                    // add the text in the mMessageList
                    addToMessageList("[C] " + message);
                } else {
                    // notify the problem
                    addToMessageList(getString(R.string.service_not_started));
                }

                editText.setText("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        stopTcpClient();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.retrieveTcpClientInstance();
    }

    private void addToMessageList(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageList.add(message);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void stopTcpClient() {
        this.retrieveTcpClientInstance();
        mTcpClientHandler.stop(getApplicationContext());
    }

    private boolean sendViaTcpClient(String message) {
        this.retrieveTcpClientInstance();
        return mTcpClientHandler.send(message);
    }

    private void reconnectTcpClient() {
        mTcpClientHandler = mTcpClientHandler.reconnect(getApplicationContext(), MainActivity.this);
    }

    private void retrieveTcpClientInstance() {
        if (mTcpClientHandler == null) {
            mTcpClientHandler = TcpClientHandler.getInstance(getApplicationContext(), this);
        }
    }
}

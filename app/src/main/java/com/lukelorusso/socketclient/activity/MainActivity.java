package com.lukelorusso.socketclient.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.lukelorusso.socketclient.R;
import com.lukelorusso.socketclient.adapter.ClientListAdapter;
import com.lukelorusso.socketclient.service.TcpClientHandler;
import com.lukelorusso.socketclient.service.TcpClientService;

public class MainActivity extends Activity implements TcpClientService.TcpClientListener {

    private ArrayList<String> mMessageList;
    private ClientListAdapter mAdapter;
    private TcpClientHandler mTcpClientHandler;

    @Override
    public void onMessageReceived(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageList.add("[S] " + message);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageList = new ArrayList<>();
        final EditText editText = findViewById(R.id.editText);
        Button send = findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        ListView listView = findViewById(R.id.list);
        mAdapter = new ClientListAdapter(this, mMessageList);
        listView.setAdapter(mAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the mMessageList
                mMessageList.add("[C] " + message);

                //sends the message to the server
                sendViaTcpClient(message);

                //refresh the list
                mAdapter.notifyDataSetChanged();
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

    private void stopTcpClient() {
        this.retrieveTcpClientInstance();
        mTcpClientHandler.stop(getApplicationContext());
    }

    private void sendViaTcpClient(String message) {
        this.retrieveTcpClientInstance();
        mTcpClientHandler.send(message);
    }

    private void retrieveTcpClientInstance() {
        if (mTcpClientHandler == null) {
            mTcpClientHandler = TcpClientHandler.getInstance(getApplicationContext(), this);
        }
    }
}

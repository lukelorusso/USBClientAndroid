package com.lukelorusso.socketclient.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lukelorusso.socketclient.R;
import com.lukelorusso.socketclient.adapter.MessageListAdapter;
import com.lukelorusso.socketclient.service.TcpClientHandler;
import com.lukelorusso.socketclient.service.TcpClientService;

/**
 * Legend:
 *     [C] = message from Client
 *     [S] = message from Server
 *     [I] = local Info
 *     [E] = local Exception
 */
public class MainActivity extends Activity implements TcpClientService.TcpClientListener {

    private static final String MESSAGE_LIST_KEY = "MESSAGE_LIST_KEY";

    private EditText mEditText;
    private ArrayList<String> mMessageList;
    private MessageListAdapter mAdapter;
    private TcpClientHandler mTcpClientHandler;
    private boolean mSavingInstanceState;

    @Override
    public void onMessageReceived(final String message) {
        addToMessageList("[S] " + message);
    }

    @Override
    public void onExceptionThrown(String message) {
        addToMessageList("[E] " + message);
    }

    @Override
    public void onServiceStarted() {
        addToMessageList("[I] " + getString(R.string.service_started));
    }

    @Override
    public void onServiceStopped() {
        addToMessageList("[I] " + getString(R.string.service_stopped));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(MESSAGE_LIST_KEY, mMessageList);
        mSavingInstanceState = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.editText);
        Button clear = findViewById(R.id.clear_button);
        Button reconnect = findViewById(R.id.reconnect_button);
        Button send = findViewById(R.id.send_button);

        if (savedInstanceState != null) {
            mMessageList = savedInstanceState.getStringArrayList(MESSAGE_LIST_KEY);
            mSavingInstanceState = false;
        } else {
            mMessageList = new ArrayList<>();
        }
        ListView listView = findViewById(R.id.list);
        mAdapter = new MessageListAdapter(this, mMessageList);
        listView.setAdapter(mAdapter);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageList.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reconnectTcpClient();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAction();
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendAction();
                    return true;
                }
                return false;
            }
        });

    }

    private void sendAction() {
        String message = mEditText.getText().toString();

        // try to sends the message to the server...
        if (sendViaTcpClient(message)) {
            // add the text in the mMessageList
            addToMessageList("[C] " + message);
        } else {
            // notify the problem
            addToMessageList("[I] " + getString(R.string.service_not_started));
        }

        mEditText.setText("");
    }

    @Override
    protected void onDestroy() {
        if (!mSavingInstanceState) {
            stopTcpClient();
        }
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

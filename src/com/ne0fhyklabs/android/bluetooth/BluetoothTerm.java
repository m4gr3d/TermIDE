/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ne0fhyklabs.android.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ne0fhyklabs.android.util.Constants;
import com.ne0fhyklabs.android.view.BluetoothTermEditor;
import com.spartacusrex.spartacuside.R;
import com.spartacusrex.spartacuside.Term;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothTerm extends Fragment {
    // Debugging
    private static final String TAG = BluetoothTerm.class.getName();
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_WRITE = 3;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private BluetoothTermEditor mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private Term mTermActivity;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        if ( !(activity instanceof Term) )
            throw new IllegalStateException("Activity " + activity + " should be instance of " +
                                            Term.class.getName());

        mTermActivity = (Term) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mTermActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ( mTermActivity == null )
            return null;

        if ( D )
            Log.e(TAG, "+++ ON CREATE VIEW +++");

        // Set up the window layout
        final View view = inflater.inflate(R.layout.bt_main, container, false);

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(mTermActivity, R.layout.message);
        mConversationView = (ListView) view.findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (BluetoothTermEditor) view.findViewById(R.id.edit_text_out);
        mOutEditText.setKeyEventListener(new KeyEvent.Callback() {

            @Override
            public boolean onKeyUp(int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
                if ( mTermActivity == null )
                    return false;

                for ( int i = 0; i < count; i++ )
                    mTermActivity.getBluetoothTermService().write(keyCode);
                return true;
            }

            @Override
            public boolean onKeyLongPress(int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if ( mTermActivity == null )
                    return false;

                mTermActivity.getBluetoothTermService().write(keyCode);
                return true;
            }
        });
        mOutEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ( mTermActivity == null )
                    return;

                if ( count > before ) {
                    char currentChar = s.charAt(s.length() - 1);
                    mTermActivity.getBluetoothTermService().write(String.valueOf(currentChar).getBytes());
					Log.d(TAG, "Term char: " + currentChar);
                }
                else if ( count < before ) {
                    mTermActivity.getBluetoothTermService().write(KeyEvent.KEYCODE_DEL);
					Log.d(TAG, "Del character");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        // mSendButton = (Button) view.findViewById(R.id.button_send);
        // mSendButton.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // // Send a message using content of the edit text widget
        // TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
        // String message = textView.getText().toString();
        // sendMessage(message);
        // }
        // });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if ( D )
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( D )
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( D )
            Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if ( mTermActivity == null )
            return;

        if ( D )
            Log.d(TAG, "ensure discoverable");

        if ( mTermActivity.getBluetoothAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    /**
     * Sends a message.
     *
     * @param message
     *            A string of text to send.
     */
    private void sendMessage(String message) {
        if ( mTermActivity == null )
            return;

        final BluetoothTermService btService = mTermActivity.getBluetoothTermService();
        // Check that we're actually connected before trying anything
        if ( btService.getState() != BluetoothTermService.STATE_CONNECTED ) {
            Toast.makeText(mTermActivity, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if ( message.length() > 0 ) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            btService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if ( actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP ) {
                        String message = view.getText().toString();
                        // sendMessage(message + "\n");
                        mConversationArrayAdapter.add("btTerm:  " + message);

                        // Reset out string buffer to zero and clear the edit text field
                        mOutStringBuffer.setLength(0);
                        mOutEditText.setText(mOutStringBuffer);
                    }
                    if ( D )
                        Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };

    private final void setStatus(int resId) {
        if ( mTermActivity == null )
            return;

        final ActionBar actionBar = mTermActivity.getActionBar();
        if ( actionBar != null )
            actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        if ( mTermActivity == null )
            return;

        final ActionBar actionBar = mTermActivity.getActionBar();
        if ( actionBar != null )
            actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if ( D )
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothTermService.STATE_CONNECTED:
                            String connectionStatus = getString(R.string.title_connected_to,
                                    mConnectedDeviceName);
                            setStatus(connectionStatus);
                            mConversationArrayAdapter.clear();
                            if ( mTermActivity != null )
                                Toast.makeText(mTermActivity, connectionStatus, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothTermService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothTermService.STATE_LISTEN:
                        case BluetoothTermService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("btTerm:  " + writeMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(mTermActivity.getApplicationContext(),
                            "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(mTermActivity.getApplicationContext(),
                            msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( D )
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if ( resultCode == Activity.RESULT_OK ) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if ( resultCode == Activity.RESULT_OK ) {
                    connectDevice(data, false);
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        if ( mTermActivity == null )
            return;

        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mTermActivity.getBluetoothAdapter().getRemoteDevice(address);
        // Attempt to connect to the device
        mTermActivity.getBluetoothTermService().connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Activity activity = getActivity();
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(activity, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(activity, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

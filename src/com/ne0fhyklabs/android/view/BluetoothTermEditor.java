/**
 * @author Fredia Huya-Kouadio
 * @date Sep 29, 2013
 */
package com.ne0fhyklabs.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class BluetoothTermEditor extends EditText {

    static final String TAG = BluetoothTermEditor.class.getName();

    private KeyEvent.Callback mKeyEventListener;

    public BluetoothTermEditor(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public BluetoothTermEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BluetoothTermEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setKeyEventListener(KeyEvent.Callback keyListener) {
        mKeyEventListener = keyListener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( mKeyEventListener != null && mKeyEventListener.onKeyDown(keyCode, event) )
            return true;

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if ( mKeyEventListener != null && mKeyEventListener.onKeyLongPress(keyCode, event) )
            return true;

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        if ( mKeyEventListener != null && mKeyEventListener.onKeyMultiple(keyCode, count, event) )
            return true;

        return super.onKeyMultiple(keyCode, count, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ( mKeyEventListener != null && mKeyEventListener.onKeyUp(keyCode, event) )
            return true;

        return super.onKeyUp(keyCode, event);
    }
}

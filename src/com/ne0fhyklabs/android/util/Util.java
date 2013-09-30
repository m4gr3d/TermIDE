/**
 * @author Fredia Huya-Kouadio
 * @date Sep 30, 2013
 */
package com.ne0fhyklabs.android.util;

public class Util {

    static final String TAG = Util.class.getName();

    public static String wrapKeyCode(int keyCode) {
        return new StringBuilder(Constants.KEY_CODE_PREFIX).append(keyCode).append(Constants.KEY_CODE_SUFFIX)
                .toString();
    }
}

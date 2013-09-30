/**
 * @author Fredia Huya-Kouadio
 * @date Sep 28, 2013
 */
package com.ne0fhyklabs.android.util;

import java.util.regex.Pattern;

public class Constants {

    public static final int REQUEST_ENABLE_BT = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
    public static final int MESSAGE_READ = 2;

    public static final String KEY_CODE_PREFIX = "\u03BB";
    public static final String KEY_CODE_SUFFIX = "\u03C8";
    public static final String KEY_CODE_REGEX = KEY_CODE_PREFIX + "(\\d+)" + KEY_CODE_SUFFIX;
    public static final Pattern KEY_CODE_PATTERN = Pattern.compile(KEY_CODE_REGEX);

}

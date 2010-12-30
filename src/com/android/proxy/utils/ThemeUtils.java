
package com.android.proxy.utils;

import java.io.File;

import android.util.Log;

public class ThemeUtils {

    private static final boolean DEBUG = false;
    private static final int LOG_LEVEL = 5;
    private static final String TAG = "ThemeUtils";

    public static final int getColor(final INIFile ini, final String section, final String property, final int defValue) {
        Long value = ini.getLongProperty(section, property);
        return (value != null) ? value.intValue() : defValue;
    }

    public static final String getText(final INIFile ini, final String section, final String property, final String defValue) {
        String value = ini.getStringProperty(section, property);
        return (value != null && value.length() != 0) ? value : defValue;
    }

    public static final CharSequence getText(final INIFile ini, final String section, final String property, final CharSequence defValue) {
        String value = ini.getStringProperty(section, property);
        return (value != null && value.length() != 0) ? value : defValue;
    }

    public static final boolean getBoolean(final INIFile ini, final String section, final String property, boolean defValue) {
        Boolean value = ini.getBooleanProperty(section, property);
        return (value != null) ? value : defValue;
    }

    public static final long getLong(final INIFile ini, final String section, final String property, final long defValue) {
        Long value = ini.getLongProperty(section, property);
        return (value != null) ? value : defValue;
    }

    public static final int getInteger(final INIFile ini, final String section, final String property, final int defValue) {
        Integer value = ini.getIntegerProperty(section, property);
        return (value != null) ? value : defValue;
    }

    public static final double getDouble(final INIFile ini, final String section, final String property, final double defValue) {
        Double value = ini.getDoubleProperty(section, property);
        return (value != null) ? value : defValue;
    }

    public static final float getFloat(final INIFile ini, final String section, final String property, final float defValue) {
        Float value = ini.getFloatProperty(section, property);
        return (value != null) ? value : defValue;
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base, final int defValue) {
        Double value = ini.getDoubleProperty(section, property);
        return (value != null) ? (int)(Math.round(value * base)) : defValue;
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base, final int defValue, final Double offset) {
        Double value = ini.getDoubleProperty(section, property);
        return (value != null) ? (int)(Math.round((value + offset)* base)) : (int)(defValue * (1 + offset));
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base, final float defValue) {
        Float value = ini.getFloatProperty(section, property);
        return Math.round(base * ((value != null) ? value : defValue));
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base, final float defValue, final double offset) {
        Float value = ini.getFloatProperty(section, property);
        return Math.round(base * (((value != null) ? value : defValue) + (float) offset));
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base, final double defValue) {
        Double value = ini.getDoubleProperty(section, property);
        return (int)Math.round(base * ((value != null) ? value : defValue));
    }

    public static final int getFraction(final INIFile ini, final String section, final String property, final int base) {
        return getFraction(ini, section, property, base, 0);
    }

    public static final String getFilePath(final String fileName) {
        File file = null;
        String path = null;
        try {
            file = new File(fileName);
            path = fileName.substring(0, file.getAbsolutePath().lastIndexOf(File.separator) + 1);
        } catch (Exception e) {
            // catch all exceptions here
        } finally {
            file = null;
        }
        return path;
    }

    private static final boolean checkFile(final String pstrFile) {
        boolean blnRet = false;
        File objFile = null;

        try {
            objFile = new File(pstrFile);
            blnRet = (objFile.exists() && objFile.isFile());
        } catch (Exception e) {
            blnRet = false;
        } finally {
            if (objFile != null) {
                objFile = null;
            }
        }
        return blnRet;
    }

    //------------------------------------------------------------------------------
    private static void LOGD(String text) {
        LOGD(text, 6);
    }
    
    private static void LOGD(String text, int loglevel) {
        if (DEBUG == true && loglevel >= LOG_LEVEL) {
            Log.d(TAG, text);
        }
    }

    private static void LOGE(String text) {
        Log.e(TAG, text);
    }
}


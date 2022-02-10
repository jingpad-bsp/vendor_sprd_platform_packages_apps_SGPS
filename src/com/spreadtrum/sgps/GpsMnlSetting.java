
package com.spreadtrum.sgps;

import android.os.SystemProperties;

import android.util.Log;

import java.util.ArrayList;

class GpsMnlSetting {

    private static final String TAG = "SGPS/Mnl_Setting";
    public static final String PROP_VALUE_0 = "0";
    public static final String PROP_VALUE_1 = "1";
    public static final String PROP_VALUE_2 = "2";

    private static final String KEY_DEBUG_DBG2SOCKET = "debug.dbg2socket";
    private static final String KEY_DEBUG_NMEA2SOCKET = "debug.nmea2socket";
    private static final String KEY_DEBUG_DBG2FILE = "debug.dbg2file";
    private static final String KEY_DEBUG_DEBUG_NMEA = "debug.debug_nmea";
    private static final String KEY_BEE_ENABLED = "BEE_enabled";
    public static final String KEY_TEST_MODE = "test.mode";
    private static final String KEY_SUPLLOG_ENABLED = "SUPPLOG_enabled";

    private static final String MNL_PROP_NAME = "persist.vendor.radio.mnl.prop";
   // private static final String GPS_CHIP_PROP = "gps.gps.version";

    private static final String DEFAULT_MNL_PROP = "0001100";
    private static ArrayList<String> sKeyList = null;

    public static void setMnlProp(String key, String value) {
        Log.v(TAG, "setMnlProp: " + key + " " + value);
        String prop = SystemProperties.get(MNL_PROP_NAME);
        if (null == sKeyList) {
            initList();
        }
        int index = sKeyList.indexOf(key);
        if (index != -1) {
            if (null == prop || prop.isEmpty()) {
                prop = DEFAULT_MNL_PROP;
            }
            if (prop.length() > index) {
                char[] charArray = prop.toCharArray();
                charArray[index] = value.charAt(0);
                String newProp = String.valueOf(charArray);
               // SystemProperties.set(MNL_PROP_NAME, newProp);
                Log.v(TAG, "setMnlProp newProp: " + newProp);
                /*Vendor
				String  cmd = "setprop "+MNL_PROP_NAME +" "+newProp;
                String atResponse = SocketUtils.sendCmdAndRecResult("ylog_cli_cmd",
                        LocalSocketAddress.Namespace.ABSTRACT, cmd);
				Log.d(TAG, "setMnlProp atResponse : " + atResponse);
				*/
                SystemProperties.set(MNL_PROP_NAME,newProp);
            }
        }
    }

    public static String getMnlProp(String key, String defaultValue) {
        String result;
        String prop = SystemProperties.get(MNL_PROP_NAME);
        if (null == sKeyList) {
            initList();
        }
        int index = sKeyList.indexOf(key);
        Log.v(TAG, "getMnlProp: " + prop);
        if (null == prop || prop.isEmpty() || -1 == index
                || index >= prop.length()) {
            result = defaultValue;
        } else {
            char c = prop.charAt(index);
            result = String.valueOf(c);
        }
        Log.v(TAG, "getMnlProp result: " + result);
        return result;
    }

    private static void initList() {
        sKeyList = new ArrayList<String>();
        sKeyList.add(KEY_DEBUG_DBG2SOCKET);
        sKeyList.add(KEY_DEBUG_NMEA2SOCKET);
        sKeyList.add(KEY_DEBUG_DBG2FILE);
        sKeyList.add(KEY_DEBUG_DEBUG_NMEA);
        sKeyList.add(KEY_BEE_ENABLED);
        sKeyList.add(KEY_TEST_MODE);
        sKeyList.add(KEY_SUPLLOG_ENABLED);
    }

}

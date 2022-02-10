package com.spreadtrum.sgps;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import android.util.Log;
import android.net.LocalSocketAddress.Namespace;

class SocketUtils {
    private static final String TAG = "SGPS/SocketUtils";
    private static LocalSocket mSocketClient = null;
    private static OutputStream mOutputStream;
    private static InputStream mInputStream;

    private static synchronized String sendCmdAndRecResult(String socketName,
                                                           Namespace namespace, String strcmd) {
        Log.d(TAG, socketName + " send cmd: " + strcmd);
        byte[] buf = new byte[255];
        String result;
        int count;

        try {
            mSocketClient = new LocalSocket();
            LocalSocketAddress mSocketAddress = new LocalSocketAddress(socketName, namespace);
            if (!mSocketClient.isConnected()) {
                Log.d(TAG, "isConnected...");
                mSocketClient.connect(mSocketAddress);
            }
            // mSocketClient.connect(mSocketAddress);
            Log.d(TAG,
                    "mSocketClient connect is " + mSocketClient.isConnected());
            mOutputStream = mSocketClient.getOutputStream();
            if (mOutputStream != null) {
                final String cmd = strcmd +
                        "\r\n";//'\0'
                mOutputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
                mOutputStream.flush();
            }
            mInputStream = mSocketClient.getInputStream();
            Log.d(TAG, strcmd + " result read beg...");
            //Thread.sleep(100);
            count = mInputStream.read(buf, 0, 255);
            Log.d(TAG, strcmd + " result read done");
            //Thread.sleep(100);
            result = new String(buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Failed get output stream: " + e.toString());
            return null;
        } finally {
            try {
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mSocketClient != null) {
                    mSocketClient.close();
                }
            } catch (IOException e1) {
                Log.d(TAG,  e1.getMessage());
            }
        }
        Log.d(TAG,  "result -> "+result);
        return result.substring(0, count);
    }

    public static synchronized String sendCommand(String command) {
        String result;
        result= sendCmdAndRecResult("hidl_common_socket", LocalSocketAddress.Namespace.ABSTRACT, "GNSS_LCS_SERVER "+command);

        return result;
    }
}


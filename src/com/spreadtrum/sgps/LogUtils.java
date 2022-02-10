package com.spreadtrum.sgps;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class LogUtils {
    private static final String TAG = "Sgps/LogUtils";
    private FileOutputStream mOutputLog = null;
    private final String fileType;


    public LogUtils(boolean isExcel) {
        if(isExcel){
            fileType=".xls";
        }else{
            fileType=".txt";
        }
    }
    public boolean openMyLog(String fileName) {
        File file;
        try {
            if (!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
                Log.d(TAG, "openMyLog Fail: Environment.getExternalStorageState()");
                return false;
            }
            file = new File(Environment.getExternalStorageDirectory(),fileName + fileType);
            mOutputLog = new FileOutputStream(file);
        } catch (Exception e) {
            Log.d(TAG, "openMyLog Fail:" + e.getMessage());
            return false;
        }

        return true;
    }

    public void writeMyLog(String mylog) {
        if(mOutputLog == null){
            Log.d(TAG, "writeMyLog Fail: mOutputLog == null" );
            return;
        }
        try {
            mOutputLog.write(mylog.getBytes(StandardCharsets.UTF_8), 0,mylog.getBytes(StandardCharsets.UTF_8).length);
            mOutputLog.flush();
            FileDescriptor fd = mOutputLog.getFD();
            fd.sync();
        } catch (IOException e) {
            Log.d(TAG, "writeMyLog Fail: " + e.getMessage());
        }

    }
    public void closeMyLog() {
        if(mOutputLog == null){
            Log.d(TAG, "closeMyLog Fail: mOutputAUTOTESTLog == null" );
            return;
        }
        try {
            mOutputLog.close();
            mOutputLog=null;
        } catch (IOException e) {
            Log.d(TAG, "closeMyLog Fail:"+ e.getMessage());
        }
    }
}

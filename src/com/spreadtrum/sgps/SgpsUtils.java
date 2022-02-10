
package com.spreadtrum.sgps;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import android.view.Gravity;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

public class SgpsUtils  {

    private static final String TAG = "Sgps/SgpsUtils";

    public static final String SGPS_VRESION = "v2.0.0_2019.08.30";

    public static final String FIRST_TIME = "first.time";
    public static final String START_MODE = "start.mode";
    public static final String UART_LOG_SWITCH = "uart.log.switch";

    public static final int ONE_SECOND = 1000;
    //add for noise scan
    public static final int COUNT_PRECISION = 500;

    public static final int HANDLE_COUNTER = 1000;
    public static final int HANDLE_COMMAND_OTHERS_UPDATE_RESULT_HINT = 1104;
    public static final int HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG = 1105;
    public static final int HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END = 1106;
    public static final int HANDLE_COMMAND_OTHERS_UPDATE_PROVIDER = 1107;
    public static final int HANDLE_AUTO_TRANSFER_START_BUTTON_UPDATE = 1008;
    public static final int HANDLE_AUTO_TRANSFER_UPDATE_CURRENT_MODE = 1010;
    public static final int HANDLE_CUSTOM_CURVECHART= 9999;

    private static final Object mAutoTransferTestRunningLock = new Object();// thread lock
    public static final int DIALOG_SLP_TEMPLATE = 100;
    public static final int DIALOG_SLP_ADDRESS = 101;
    public static final int DIALOG_SLP_PORT = 102;
    public static final int DIALOG_HORIZONTAL_ACCURACY = 103;
    public static final int DIALOG_VERTICAL_ACCURACY = 104;
    public static final int DIALOG_LOCATIONAGE = 105;
    public static final int DIALOG_DELAY = 106;
    public static final int DIALOG_CERTIFICATEVERIFICATION = 107;
    public static final int DIALOG_MSISDN = 108;
    public static final int DIALOG_3RDMSISDN = 109;
    public static final int DIALOG_POSMETHOD_SELECT = 110;
    public static final int DIALOG_PERODIC_MININTERVAL = 111;
    public static final int DIALOG_PERODIC_STARTTIME = 112;
    public static final int DIALOG_PERODIC_STOPTIME = 113;
    public static final int DIALOG_AREA_TYPE_SELECT = 114;
    public static final int DIALOG_AREA_MININTERVAL = 115;
    public static final int DIALOG_MAXNUM = 116;
    public static final int DIALOG_AREA_STARTTIME = 117;
    public static final int DIALOG_AREA_STOPTIME = 118;
    public static final int DIALOG_GEORADIUS = 119;
    public static final int DIALOG_LATITUDE = 120;
    public static final int DIALOG_LONGITUDE = 121;
    public static final int DIALOG_NI_DIALOG_TEST = 122;
    public static final int DIALOG_CUSTOMER_CMD = 123;

    private LoadingDialog hwndLoadingDialog=null;

    private static final String GNSSCHIP = SystemProperties.get("ro.vendor.gnsschip","ge");
    private static final String GPSCHIP = SystemProperties.get("ro.vendor.wcn.gpschip","ge");
    public static final boolean ISGe2 = GNSSCHIP.equalsIgnoreCase("ge2")||GPSCHIP.equalsIgnoreCase("ge2");
    public static final boolean ISMarlin3 =GNSSCHIP.equalsIgnoreCase("marlin3")||GPSCHIP.equalsIgnoreCase("marlin3");
    public static final boolean ISMarlin3lite =GNSSCHIP.equalsIgnoreCase("marlin3lite")||GPSCHIP.equalsIgnoreCase("marlin3lite");

    // GPS_EXTRA_DATA index = GPSGroupEnum index
    public static final String[] GPS_EXTRA_DATA= {
            "$PSPRD,00,3,1",//GPS_EXTRA_GPSONLY
            "$PSPRD,00,3,4",//GPS_EXTRA_GLONASS
            "$PSPRD,00,3,2",//GPS_EXTRA_BDSONLY
            "$PSPRD,00,3,5",//GPS_EXTRA_GLONASSGPS
            "$PSPRD,00,3,3",//GPS_EXTRA_GPSBDS
            "$PSPRD,00,3,7",//GPS_EXTRA_GPSBD2GLONASS
            "$PSPRD,00,3,13",//GPS_EXTRA_GPSB1CGLONASS
            "$PSPRD,00,3,25",//GPS_EXTRA_GPSB1CGalileo
            "$PSPRD,00,3,21",//GPS_EXTRA_GPSGLONASSGalileo
            "$PSPRD,00,3,",//GPSCUSTOM
            "$PSPRD,00,3,19",//GPS_EXTRA_GPSBD2Galileo
            "$PSPRD,00,a,a",//GPS_EXTRA_IMG_MODE_GL
            "$PSPRD,00,a,b",//GPS_EXTRA_IMG_MODE_BD
            "$PSPRD,00,a,c",//GPS_EXTRA_CP_MODE_GL_DEF
            "$PSPRD,00,a,d",//GPS_EXTRA_CP_MODE_BD_DEF
            "$PSPRD,00,a,e",//GPS_EXTRA_MINIPVT_TRUE
            "$PSPRD,00,a,f" //GPS_EXTRA_MINIPVT_FALSE
    };
    public enum GPSGroupEnum {
        GPS_EXTRA_GPSONLY ,
        GPS_EXTRA_GLONASS,
        GPS_EXTRA_BDSONLY,
        GPS_EXTRA_GLONASSGPS,
        GPS_EXTRA_GPSBDS,
        GPS_EXTRA_GPSBD2GLONASS,
        GPS_EXTRA_GPSB1CGLONASS,
        GPS_EXTRA_GPSB1CGalileo,
        GPS_EXTRA_GPSGLONASSGalileo,
        GPSCUSTOM,
        GPS_EXTRA_GPSBD2Galileo,
        GPS_EXTRA_IMG_MODE_GL ,
        GPS_EXTRA_IMG_MODE_BD,
        GPS_EXTRA_CP_MODE_GL_DEF,
        GPS_EXTRA_CP_MODE_BD_DEF,
        GPS_EXTRA_MINIPVT_TRUE,
        GPS_EXTRA_MINIPVT_FALSE,
    }

    public enum GPSModeEnum {
        Hot,
        Warm,
        Cold,
        Factory
    }

    /* @} */
    public static final String GPS_EXTRA_LOG_SWITCH_ON = "svdir";
    public static final String GPS_EXTRA_LOG_SWITCH_OFF = "sadata";

    public static final LogUtils mOutputRSSILog = new LogUtils(false);
    public static final LogUtils mOutputNMEALog = new LogUtils(false);
    public static final LogUtils mOutputAUTOTESTLog = new LogUtils(false);
    // for noise scan tab
    public int mCurrSCanPeriod = 0;
    public int mCurrScanTimes=3;
    public int mCurrSCanPeriodCount = 0;
    public int mCurrScanTimesCount=0;
    public LinearLayout mCurveChart = null;
    public final List<String> mltFirstData = new ArrayList<String>();
    public final List<String> mltSecondData = new ArrayList<String>();
    public final List<String> mltThirdData = new ArrayList<String>();
    //agps
    public String[] mSLPArray;
    public final ArrayList<String> mSLPNameList = new ArrayList<String>();
    public final ArrayList<String> mSLPValueList = new ArrayList<String>();
    public String[] mPosMethodArray;
    public String[] mPosMethodArrayValues;
    public String[] mAreaTypeArray;
    public String[] mAreaTypeArrayValues;
    public String[] mNiDialogTestArray;
    public String[] mNiDialogTestArrayValues;
    private PowerManager.WakeLock mScreenWakeLock = null;
    private boolean mEnterCn0FirstFlag = true;
    public boolean mTtffTimeoutFlag = false;
    private long mStartSerchTime = 0;
    private long mSerchFirstSateTime = 0;
    private boolean mSerchFirstSateFlag = false;
    private boolean mFirstFixFlag = false;
    private int mSatelliteTestCont = 0;
    private int[] mTotalInused = null;
    private int[] mTotalView = null;
    private int[] mGpsInUsed = null;
    private int[] mGpsView = null;
    private int[] mGlonassInUsed = null;
    private int[] mGlonassView = null;
    private int[] mBeidouInUsed = null;
    private int[] mBeidouView = null;
    private enum InUsedandViewEnum {
        TotalInUsed,
        TotalView,
        GpsInUsed,
        GpsView,
        GlonassInUsed,
        GlonassView,
        BeidouInUsed,
        BeidouView
    }
    private final int[] mSatelliteInUsedandViewMinValues=new int[8];
    private final int[] mSatelliteInUsedandViewMaxValues=new int[8];

    private int[] mSateTracking = null;
    public int mTimeoutValue = 999;
    public int mTTFFTimeoutCont = 0;
    public GPSModeEnum mCurrentMode = GPSModeEnum.Hot;
    private String mGe2Cn_Sr = null;
    public static final String GNSSBDMODEM = "GNSSBDMODEM";
    public static final String GNSSMODEM = "GNSSMODEM";

    private final int[] mUsedInFixMask = new int[SatelliteDataProvider.SATELLITES_MASK_SIZE];
    public int mTtffValue = 0;
    public int mDurationTime =0;
    private int mSatellites = 0;
    public int mCurrentTimes = 0;
    private final int[] mPrns = new int[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mSnrs = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mElevation = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mAzimuth = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final int[] mPrnsForCN0 = new int[5];
    private final float[] mSrnsForCN0 = new float[5];
    private final int[] mContForCN0 = new int[5];
    private int mTtffcont = 0;
    private float[] mTtff;
    public int mDistanceCont = 0;
    private float[] mDistance;
    private double[] mFirstFixLatitude;
    private double[] mFirstFixLongitude;
    public int mAutoTransferTotalTimes = 0;
    public int mTTFFInterval=0;
    public int mModeInterval = 0;

    public boolean mFirstFix = false;
    private boolean mIsAutoTransferTestRunning = false;
    public boolean mShowFirstFixLocate = true;
    private boolean locationWhenFirstFix = false;
    public double mTestLatitude = -1.0;
    public double mTestLongitude = -1.0;
    public int mLastTtffValue = 0;
    public float mTestTTFFSum = 0f;
    public float mTestDistanceSum = 0f;
    public String mProvider = "";
    public String mStatus = "";
    public LocationManager mLocationManager = null;
    public Location mLastLocationRefence = null;
    public GpsStatus mLastStatus = null;

    private static Context mContext;
    public SgpsUtils(Context context) {
        mContext = context;
        Arrays.fill(mPrnsForCN0,0);
        Arrays.fill(mSrnsForCN0,0);
        Arrays.fill(mContForCN0,0);
    }

    public void doMySwitchChange(int viewId,boolean isChecked){
        switch (viewId) {
            case R.id.spreadorbit_switch:
                SocketUtils.sendCommand(isChecked?"$PSPRD,00,4,1":"$PSPRD,00,4,2");
                break;
            case R.id.real_eph_switch: /* REALEPH-ENABLE */
                SocketUtils.sendCommand(isChecked?"$PSPRD,00,a,0":"$PSPRD,00,a,1");
                break;
            case R.id.gnss_log_switch: /* LOG-ENABLE */
                SocketUtils.sendCommand(isChecked?"$PSPRD,00,a,2":"$PSPRD,00,a,3");
                break;
            case R.id.single_satellite_switch:
                setAGPSInfo("PROPERTY","SINGLE-SATELLITE",
                        isChecked ? "TRUE" : "FALSE");
                break;
            case R.id.gnss_rtd_switch: /* RTD-SWITCH */
                SocketUtils.sendCommand(isChecked?"$PSPRD,00,a,6":"$PSPRD,00,a,7");
                break;
            case R.id.gnss_rtk_switch: /* RTK-SWITCH */
                SocketUtils.sendCommand(isChecked?"$PSPRD,00,a,8":"$PSPRD,00,a,9");
                break;
            default:
                Log.d(TAG, "viewId is not found!");
        }
    }

    public void domScrollToBottom(TextView tvNmeaLog, ScrollView tvScrollLog){
        int off = tvNmeaLog.getMeasuredHeight()
                - tvScrollLog.getMeasuredHeight();
        int viewpages = 5;
        if (off > viewpages * tvScrollLog.getMeasuredHeight()) {
            int scroll_y = tvScrollLog.getScrollY();
            int line_count_src = tvNmeaLog.getLineCount();
            int line_count_des;
            int line_height = tvNmeaLog.getLineHeight();
            int offset_x = 0;
            int lens_pre_page = tvScrollLog.getMeasuredHeight()
                    / line_height;

            int remove_line = line_count_src - viewpages * lens_pre_page;
            if (remove_line > 0) {
                for (int i = 0; i < remove_line; i++) {
                    offset_x = tvNmeaLog.getText().toString()
                            .indexOf("$", offset_x + 10);
                }

                if (offset_x < 0) {
                    offset_x = 10;// for exception
                }
                tvNmeaLog.setText(tvNmeaLog.getText().toString()
                        .substring(offset_x));// substring

                line_count_des = tvNmeaLog.getLineCount();

                Log.d(TAG, "offset = " + offset_x + " line_count_src : "
                        + line_count_src + " line_count_des : "
                        + line_count_des);

                scroll_y -= (line_count_src - line_count_des) * line_height;
                if (scroll_y > 0) {
                    tvScrollLog.scrollTo(0, scroll_y);// scroll to next line
                }
            }
        }
        if (off > 0
                && (tvNmeaLog.getMeasuredHeight()
                - tvScrollLog.getScrollY() < 2 * tvScrollLog
                .getMeasuredHeight())) {
            tvScrollLog.scrollTo(0, off);
        }

    }

    public void domLocListener(Location location,TextView averageCn0TextView){
        StringBuilder str_item = new StringBuilder();
        if (getmIsAutoTransferTestRunning()) {
            if (locationWhenFirstFix) {
                mFirstFixFlag = true;
                locationWhenFirstFix = false;
                float distance;
                distance = location.distanceTo(mLastLocationRefence);
                Log.d(TAG, "location la is " + location.getLatitude()
                        + " , lo is " + location.getLongitude() + " , distance is " + distance
                        + ", mDistanceCont is " + mDistanceCont + "\n");
                if (mDistanceCont < mCurrentTimes) {
                    mDistance[mDistanceCont] = distance;
                    mFirstFixLatitude[mDistanceCont] = location.getLatitude();
                    mFirstFixLongitude[mDistanceCont] = location.getLongitude();
                    mDistanceCont++;
                }
            }
        }
        if (mFirstFix && !mEnterCn0FirstFlag && !getmIsAutoTransferTestRunning()) {
            for (int i =0; i < mPrnsForCN0.length; i++) {
                if (mContForCN0[i] != 0) {
                    String  cn0=String.format("<%d,%d>",mPrnsForCN0[i],Math.round(mSrnsForCN0[i] / mContForCN0[i]));
                    str_item.append(cn0);
                }
            }
            averageCn0TextView.setText(str_item.toString());
        }else{
            averageCn0TextView.setText("N/A");
        }
    }

    public void doonGpsStatusChanged(int event,GpsStatus status){
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                mStartSerchTime = new Date().getTime();
                Log.d(TAG, "mStartSerchTime  is " + mStartSerchTime);
                if (getmIsAutoTransferTestRunning()) {
                    mFirstFixFlag = false;
                    locationWhenFirstFix = true;
                }
                mSerchFirstSateFlag = true;
                mEnterCn0FirstFlag = true;
                mStatus = mContext.getString(R.string.gps_status_started);
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.d(TAG, "GPS_EVENT_STOPPED");
                mStatus = mContext.getString(R.string.gps_status_stopped);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS is receive!!");
                if (mSerchFirstSateFlag && getmIsAutoTransferTestRunning()) {
                    mSerchFirstSateFlag = false;
                    mSerchFirstSateTime = new Date().getTime();
                }
                if (mFirstFix && !getmIsAutoTransferTestRunning())
                {
                    if (!mEnterCn0FirstFlag){
                        for(int i = 0; i < mPrnsForCN0.length; i++) {
                           if(mContForCN0[i]> 5) {
                               mSrnsForCN0[i] = 0;
                               mContForCN0[i] = 0;
                           }
                        }
                    }
                    satelliteStateCN0(status.getSatellites());
                }
                if (mFirstFixFlag) {
                    setSatelliteInusedOrTracking(status.getSatellites());
                }
                mLastStatus = status;
                break;
            default:
                break;
        }
    }

    public boolean isLocationFixed(Iterable<GpsSatellite> list) {
        boolean fixed = false;
        for (GpsSatellite sate : list) {
            if (sate.usedInFix()) {
                fixed = true;
                break;
            }
        }
        return fixed;
    }
    public void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
                                   float[] elevations, float[] azimuths, int ephemerisMask,
                                   int almanacMask, int[] usedInFixMask) {
        Log.v(TAG, "Enter setSatelliteStatus function parameter");
        emptyArray();
        mSatellites = svCount;
        System.arraycopy(prns, 0, mPrns, 0, mSatellites);
        System.arraycopy(snrs, 0, mSnrs, 0, mSatellites);
        System.arraycopy(elevations, 0, mElevation, 0, mSatellites);
        System.arraycopy(azimuths, 0, mAzimuth, 0, mSatellites);
        System.arraycopy(usedInFixMask, 0, mUsedInFixMask, 0,
                SatelliteDataProvider.SATELLITES_MASK_SIZE);
    }
    public void setSatelliteStatus(Iterable<GpsSatellite> list) {
        Log.v(TAG, "Enter setSatelliteStatus function");
        emptyArray();
        if (null != list) {
            int index = 0;
            for (GpsSatellite sate : list) {
                mPrns[index] = sate.getPrn();
                mSnrs[index] = sate.getSnr();
                mElevation[index] = sate.getElevation();
                mAzimuth[index] = sate.getAzimuth();
                if (sate.usedInFix()) {
                    int i = mPrns[index] - 1;
                    mUsedInFixMask[i
                            / SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH] |= (1 << (i % SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH));
                }
                index++;
            }
            mSatellites = index;
        }
        Log.v(TAG,
                "Found " + mSatellites + " Satellites:"
                        + toString(mPrns, mSatellites) + ","
                        + toString(mSnrs, mSatellites));
        for (int i = 0; i < mUsedInFixMask.length; i++) {
            Log.v(TAG,
                    "Satellites Masks " + i + ": 0x"
                            + Integer.toHexString(mUsedInFixMask[i]));
        }
    }
    private String getSatelliteStatus(Iterable<GpsSatellite> list) {
        Log.d(TAG, "Enter private getSatelliteStatus function");
        int[] mPrns_temp = new int[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
        float[] mSnrs_temp = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
        if (null == list) {
            return "0";
        } else {
            int index = 0;
            for (GpsSatellite sate : list) {
                mPrns_temp[index] = sate.getPrn();
                mSnrs_temp[index] = sate.getSnr();
                index++;
            }
            return toString(mPrns_temp, index) + ","
                    + toString(mSnrs_temp, index);
        }
    }

    public int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
                                  float[] azimuths, int ephemerisMask, int almanacMask,
                                  int[] usedInFixMask) {

        if (prns != null) {
            System.arraycopy(mPrns, 0, prns, 0, mSatellites);
        }
        if (snrs != null) {
            System.arraycopy(mSnrs, 0, snrs, 0, mSatellites);
        }
        if (azimuths != null) {
            System.arraycopy(mAzimuth, 0, azimuths, 0, mSatellites);
        }
        if (elevations != null) {
            System.arraycopy(mElevation, 0, elevations, 0, mSatellites);
        }
        if (usedInFixMask != null) {
            System.arraycopy(mUsedInFixMask, 0, usedInFixMask, 0,
                    SatelliteDataProvider.SATELLITES_MASK_SIZE);
        }
        return mSatellites;
    }
    public void setSatelliteStatusForGe2(List<com.spreadtrum.sgps.GpsSatellite> list){
        Log.d(TAG, "Enter private getSatelliteStatusForGe2 function");
        mGe2Cn_Sr= "0";
        if (null != list) {
            int index = 0;
            int[] mPrns_temp = new int[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
            float[] mSnrs_temp = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
            for (com.spreadtrum.sgps.GpsSatellite sate : list) {
                mPrns_temp[index] = sate.getPrn();
                mSnrs_temp[index] = sate.getSnr();
                index++;
            }
            mGe2Cn_Sr = toString(mPrns_temp, index) + ","
                    + toString(mSnrs_temp, index);
        }
    }
    public void updateSatelliteView(List<com.spreadtrum.sgps.GpsSatellite> list) {
        Log.v(TAG, "updateSatelliteView");
        emptyArray();
        if (null != list) {
            int index = 0;
            for (com.spreadtrum.sgps.GpsSatellite sate : list) {
                mPrns[index] = sate.getPrn();
                mSnrs[index] = sate.getSnr();
                mElevation[index] = sate.getElevation();
                mAzimuth[index] = sate.getAzimuth();
                if (sate.usedInFix()) {
                    int i = mPrns[index] - 1;
                    mUsedInFixMask[i
                            / SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH] |= (1 << (i % SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH));
                }
                index++;
            }
            mSatellites = index;
        }
    }

    //public static final String GPS_CONFIG_FILE = "/data/vendor/gnss/config/config.xml";//GPS_CSR_CONFIG_FIL_FOR_GE2
    //public static final String AGPS_CONFIG_FILE = "/data/vendor/gnss/supl/supl.xml";//GPS_CSR_GNSS_CONFIG_FILE
    public static String getGPSInfo(String elementName, String key) {
        Log.d(TAG, "ro.vendor.gnsschip =" + GNSSCHIP + ", ro.vendor.wcn.gpschip =" + GPSCHIP );
        Log.d(TAG, "ro.vendor.gnsschip =" + GNSSCHIP.equalsIgnoreCase("ge2") + ", ro.vendor.wcn.gpschip =" + GPSCHIP.equalsIgnoreCase("ge2") );
        return getValueFromXML(mContext.getString(R.string.gps_config), elementName, key);
    }
    public static boolean setGPSInfo(String elementName, String key, String newVal) {
        return setValueFromXML(mContext.getString(R.string.gps_config),elementName,key,newVal);
    }
    public static String getAGPSInfo(String elementName, String key) {
        return getValueFromXML(mContext.getString(R.string.agps_supl), elementName, key);
    }
    public static boolean setAGPSInfo(String elementName, String key, String newVal) {
        return setValueFromXML(mContext.getString(R.string.agps_supl),elementName,key,newVal);
    }
    /**
     * @param filepath the file to read from
     * @param key which to get
     * @return get string value by key
     */
    private static String getValueFromXML(String filepath, String elementName, String key) {
        String value = "UNKNOWN";
        Log.d(TAG, "filepath-> " + filepath + ", element name-> " + elementName + ", key-> " + key);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File gpsConfig = new File(filepath);
            Document doc = db.parse(gpsConfig);

            NodeList nodeList = doc.getElementsByTagName(elementName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getAttribute("NAME").equals(key)) {
                    value = element.getAttribute("VALUE");
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception ->" + e);
            e.printStackTrace();
        }
        return value;
    }
    /**
     * @param path the xml file entire path
     * @param newVal the new value will be set.
     * @return true if set attribute value successfully, false otherwise.
     */
    private static boolean setValueFromXML(String path, String elementName, String key, String newVal) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File gpsconfig = new File(path);
            Document doc = db.parse(gpsconfig);

            NodeList list = doc.getElementsByTagName(elementName);
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                if (element.getAttribute("NAME").equals(key)) {
                    element.setAttribute("VALUE", newVal);
                    break;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            StreamResult result = new StreamResult(new FileOutputStream(path));
            transformer.transform(domSource, result);

        } catch (Exception e) {
            Log.e(TAG, "Exception :", e);
            return false;
        }
        return true;
    }

    private float calcAveValue(int[] array, int nMax) {
        float sum = 0.0f;
        for (int i = 0; i < nMax; i++) {
                sum += array[i];
        }
        return sum/nMax;
    }
    public static String constructMylogFilename(String preFix){
        DateFormat dateformat = new SimpleDateFormat("yyyyMMddhhmmss");
        String strTime = dateformat.format(new Date(System.currentTimeMillis()));
        return String.format("%s%s",preFix,strTime);
    }

    private boolean isUsedInFix(int prn) {
        int innerPrn = prn;
        boolean result = false;
        if (0 >= innerPrn) {
            for (int mask : mUsedInFixMask) {
                if (0 != mask) {
                    result = true;
                    break;
                }
            }
        } else {
            innerPrn = innerPrn - 1;
            int index = innerPrn / SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH;
            int bit = innerPrn % SatelliteDataProvider.SATELLITES_MASK_BIT_WIDTH;
            result = (0 != (mUsedInFixMask[index] & (1 << bit)));
        }
        return result;
    }

    private static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                Log.d(TAG, "isNumeric false");
                return false;
            }
        }
        Log.d(TAG, "isNumeric true");
        return true;
    }
    private void setSatelliteInusedOrTracking(Iterable<android.location.GpsSatellite> list) {
        if (list != null) {
            int[] iNum=new int[8];
            Arrays.fill(iNum,0);
            int trackingNum = 0;
            for (android.location.GpsSatellite sate : list) {
                int prn = sate.getPrn();
                iNum[InUsedandViewEnum.TotalView.ordinal()]++;
                if (prn >= 1 && prn <= 32) {
                    iNum[InUsedandViewEnum.GpsView.ordinal()]++;
                } else if (prn >= 65 && prn <= 92) {
                    iNum[InUsedandViewEnum.GlonassView.ordinal()]++;
                } else if (prn >= 151 && prn <= 187) {
                    iNum[InUsedandViewEnum.BeidouView.ordinal()]++;
                }
                if (isUsedInFix(prn)) {
                    iNum[InUsedandViewEnum.TotalInUsed.ordinal()]++;
                    if (prn >= 1 && prn <= 32) {
                        iNum[InUsedandViewEnum.GpsInUsed.ordinal()]++;
                    } else if (prn >= 65 && prn <= 92) {
                        iNum[InUsedandViewEnum.GlonassInUsed.ordinal()]++;
                    } else if (prn >= 151 && prn <= 187) {
                        iNum[InUsedandViewEnum.BeidouInUsed.ordinal()]++;
                    }
                } else {
                    trackingNum++;
                }
            }
            Log.d(TAG, "setSatelliteInusedOrTracking\n totalInUsedNum= " + iNum[InUsedandViewEnum.TotalInUsed.ordinal()]
                    +"\ntotalViewNum=" + iNum[InUsedandViewEnum.TotalView.ordinal()] +
                    "\ngpsInUsedNum= "+ iNum[InUsedandViewEnum.GpsInUsed.ordinal()] +
                    "\ngpsViewNum=" + iNum[InUsedandViewEnum.GpsView.ordinal()] +
                    "\nglonassInUsedNum=" + iNum[InUsedandViewEnum.GlonassInUsed.ordinal()] +
                    "\nglonassViewNum=" + iNum[InUsedandViewEnum.GlonassView.ordinal()] +
                    "\nbeidouInUsedNum= " + iNum[InUsedandViewEnum.BeidouInUsed.ordinal()] +
                    "\nbeidouViewNum= " + iNum[InUsedandViewEnum.BeidouView.ordinal()]);

            adjustInUsedandViewMinMaxValue(iNum);
            if (mSatelliteTestCont < mCurrentTimes) {
                mTotalInused[mSatelliteTestCont] = iNum[InUsedandViewEnum.TotalInUsed.ordinal()];
                mTotalView[mSatelliteTestCont] = iNum[InUsedandViewEnum.TotalView.ordinal()];
                mGpsInUsed[mSatelliteTestCont] = iNum[InUsedandViewEnum.GpsInUsed.ordinal()];
                mGpsView[mSatelliteTestCont] = iNum[InUsedandViewEnum.GpsView.ordinal()];
                mGlonassInUsed[mSatelliteTestCont] = iNum[InUsedandViewEnum.GlonassInUsed.ordinal()];
                mGlonassView[mSatelliteTestCont] = iNum[InUsedandViewEnum.GlonassView.ordinal()];
                mBeidouInUsed[mSatelliteTestCont] = iNum[InUsedandViewEnum.BeidouInUsed.ordinal()];
                mBeidouView[mSatelliteTestCont] = iNum[InUsedandViewEnum.BeidouView.ordinal()];
                mSateTracking[mSatelliteTestCont] = trackingNum;
                mSatelliteTestCont++;
            }
            Log.d(TAG, "setSatelliteInusedOrTracking mSatelliteTestCont is "
                    + mSatelliteTestCont + " , mCurrentTimes is " + mCurrentTimes);
        }
        mFirstFixFlag = false;
    }

    private void adjustInUsedandViewMinMaxValue(int[] input) {
        for(int i=0;i<mSatelliteInUsedandViewMinValues.length;i++)
        {
            if(input[i]< mSatelliteInUsedandViewMinValues[i]){
                mSatelliteInUsedandViewMinValues[i]=input[i];
            }
            if(input[i]> mSatelliteInUsedandViewMaxValues[i]){
                mSatelliteInUsedandViewMaxValues[i]=input[i];
            }

        }
    }

    private void satelliteStateCN0(Iterable<android.location.GpsSatellite> list) {

        List<android.location.GpsSatellite> satelliteList=new ArrayList<android.location.GpsSatellite>();
        android.location.GpsSatellite backSate=null;
        for(android.location.GpsSatellite sate : list) {
            if ((backSate == null) || (backSate.getPrn() != sate.getPrn())) {
                satelliteList.add(sate);
            }
            backSate=sate;
        }
         if (mEnterCn0FirstFlag) {
            mEnterCn0FirstFlag = false;
            if (satelliteList.size() < mPrnsForCN0.length) {
                for(int i = 0; i < satelliteList.size(); i++) {
                    mPrnsForCN0[i] = satelliteList.get(i).getPrn();
                    mSrnsForCN0[i] = satelliteList.get(i).getSnr();
                    mContForCN0[i]++;
                }
            } else {
                for(int i = 0; i < mPrnsForCN0.length; i++) {
                    mPrnsForCN0[i] = satelliteList.get(i).getPrn();
                    mSrnsForCN0[i] = satelliteList.get(i).getSnr();
                    mContForCN0[i]++;
                }
            }
        } else {
            for(int i=0;i<satelliteList.size();i++) {
                for(int j = 0; j < mPrnsForCN0.length; j++) {
                    if (mPrnsForCN0[j] == satelliteList.get(i).getPrn() && satelliteList.get(i).getSnr() != 0) {
                        mSrnsForCN0[j] += satelliteList.get(i).getSnr();
                        mContForCN0[j]++;
                    }
                }
            }
        }
    }
    private String toString(float[] array, int count) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("(");
        for (int idx = 0; idx < count; idx++) {
            strBuilder.append(array[idx]);
            strBuilder.append(",");
        }
        strBuilder.append(")");
        return strBuilder.toString();
    }

    private String toString(int[] array, int count) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("(");
        for (int idx = 0; idx < count; idx++) {
            strBuilder.append(array[idx]);
            strBuilder.append(",");
        }
        strBuilder.append(")");
        return strBuilder.toString();
    }

    private void emptyArray() {
        mSatellites = 0;
        Arrays.fill(mPrns,0);
        Arrays.fill(mSnrs,0);
        Arrays.fill(mElevation,0);
        Arrays.fill(mAzimuth,0);
        Arrays.fill(mUsedInFixMask,0);
    }

    //Extraction of three values from RSSI
    //index[0]=Max,index[1]=Min,index[2]=ave
    private float[] extractValuesFromNoiseRssi(List<String> lsData) {
        float fCurRssi;
        float[] fRssi = new float[3];
        fRssi[0]=Float.parseFloat(lsData.get(0));
        fRssi[1]=Float.parseFloat(lsData.get(0));
        fRssi[2]=Float.parseFloat(lsData.get(0));
        for (int item = 1; item < lsData.size(); item++) {
            fCurRssi = Float.parseFloat(lsData.get(item));
            if(fRssi[0]< fCurRssi){
                fRssi[0]=fCurRssi;
            }
            if(fRssi[1]>fCurRssi){
                fRssi[1]=fCurRssi;
            }
            fRssi[2]+=fCurRssi;
        }
        fRssi[2]=fRssi[2]/lsData.size();
        return fRssi;
    }

    public void SetCustomModeSystem(String customedit){
        // parse
        String[] parsearray= customedit.split(" ");
        Log.d(TAG, "SetCustomModeSystem ->sModeSystemStr:" + customedit);

        if(("1".equals(parsearray[2]))&&(parsearray.length==3)){
            //miniPvt mode
            Log.d(TAG, "SetCustomModeSystem: miniPVT");
            String ret=SocketUtils.sendCommand(GPS_EXTRA_DATA[GPSGroupEnum.GPS_EXTRA_MINIPVT_TRUE.ordinal()]);
            if(ret.contains("OK")){
                Toast.makeText(mContext, "Set Custom Mode Success!" , Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(mContext, "Set Custom Mode Failure!" , Toast.LENGTH_LONG).show();
            }

        }else if(("0".equals(parsearray[2]))&&(parsearray.length==5)&&(("sys").equals(parsearray[3]))&&(isNumeric(parsearray[4]))){
            //Appvt mode
            int sys = Integer.parseInt(parsearray[4]);
            Log.d(TAG, "APPVT &System mode :" + sys);
            if ("TRUE".equals(getGPSInfo("PROPERTY", "MINIPVT"))) {
                SocketUtils.sendCommand(GPS_EXTRA_DATA[GPSGroupEnum.GPS_EXTRA_MINIPVT_FALSE.ordinal()]);
            }
            if(setCommandToProvider(GPSGroupEnum.GPSCUSTOM,Integer.toString(sys))){
                Toast.makeText(mContext, "Set Custom Mode Success!" , Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(mContext, "Set Custom Mode Failure!" , Toast.LENGTH_LONG).show();
            }

        }else{
            Log.d(TAG, "Please input correct Custom Mode System " );
            Toast.makeText(mContext, "Please input correct Custom Mode System" , Toast.LENGTH_LONG).show();
        }

    }

    /* SPRD:add for GE2 @{ */
    private boolean setCommandToProvider(GPSGroupEnum mode, String customParam){
        String type = GPS_EXTRA_DATA[mode.ordinal()];
        if(mode == GPSGroupEnum.GPSCUSTOM)
        {
            type=type + customParam;
        }
        Log.d(TAG, " setCommandToProvider type: " + type);
        String ret=SocketUtils.sendCommand(type);

        return (ret.contains("OK"))?true:false;
    }

    @SuppressLint("InvalidWakeLockTag")
    public void acquireScreenWakeLock() {
        Log.d(TAG, "Acquiring screen wake lock start");
        if (mScreenWakeLock != null) {
            return;
        }

        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);

        mScreenWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        // | PowerManager.ON_AFTER_RELEASE, TAG);
        mScreenWakeLock.acquire();
        Log.d(TAG, "Acquiring screen wake lock end");
    }
    public void release() {
        Log.d(TAG, "Releasing wake lock");
        if (mScreenWakeLock != null) {
            mScreenWakeLock.release();
            mScreenWakeLock = null;
        }
    }
    public boolean showSUPL2View(String version) {
        boolean show = false;
        if ("v2.0.0".equals(version)) {
            show = true;
        }
        return show;
    }

    public void setMOLATrigger(boolean on) {
        Log.d(TAG, "SettingsObserver selfChange is " + on);
        if(on){
            SocketUtils.sendCommand("$PSPRD,00,6,1");
        }else{
            SocketUtils.sendCommand("$PSPRD,00,6,2");
        }
    }
    public GPSGroupEnum getGNSSMode() {
        // bit0--gps,bit1--beidou,bit2--glonass, value = 1 is turn on, 0 is turn off. Default is gps+glonass
        // example : "101" is gps+glonass

        GPSGroupEnum gps_mode;
        String gpsModeValue = getGPSInfo("PROPERTY", "CP-MODE");
        Log.d(TAG, "gpsModeValue is " + gpsModeValue);
        if (ISMarlin3 ||ISMarlin3lite) {
            if (gpsModeValue.equalsIgnoreCase("0x7")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSBD2GLONASS;//GPSBD2GLONASS;
            } else if (gpsModeValue.equalsIgnoreCase("0xd")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSB1CGLONASS;//GPSB1CGLONASS;
            } else if (gpsModeValue.equalsIgnoreCase("0x19")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSB1CGalileo;//GPSB1CGalileo;
            } else if (gpsModeValue.equalsIgnoreCase("0x15")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSGLONASSGalileo;//GPSGLONASSGalileo;
            } else if (gpsModeValue.equalsIgnoreCase("0x13")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSBD2Galileo;//GPSBD2Galileo;
            } else {
                gps_mode = GPSGroupEnum.GPSCUSTOM;//GPSCUSTOM;
            }
        }else{
            /*if (gpsModeValue.equalsIgnoreCase("0x1")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSONLY;//GPS_ONLY;
            } else*/
            if (gpsModeValue.equalsIgnoreCase("0x4") ) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GLONASS;//GLONASS_ONLY;
            } else if (gpsModeValue.equalsIgnoreCase("0x2")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_BDSONLY;//BDS_ONLY;
            } else if (gpsModeValue.equalsIgnoreCase("0x5")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GLONASSGPS;//GLONASS_GPS;
            } else if (gpsModeValue.equalsIgnoreCase("0x3")) {
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSBDS;//GPS_BDS;
            }else{
                gps_mode = GPSGroupEnum.GPS_EXTRA_GPSONLY;//GPS_ONLY;
            }
        }
        Log.d(TAG, "gps_mode is " + gps_mode.toString());
        return gps_mode;
    }
    private Bundle perpareGpsMode(int start_mode) {
        Bundle bundleTemp = new Bundle();
        if (start_mode == GPSModeEnum.Hot.ordinal()) {
            // nothing should be put
            Log.v(TAG, "Radio Hot Start is selected");
            bundleTemp.putBoolean("rti", true);
        } else if (start_mode == GPSModeEnum.Cold.ordinal()) {
            Log.v(TAG, "Radio Cold Start is selected");
            bundleTemp.putBoolean("ephemeris", true);
            bundleTemp.putBoolean("position", true);
            bundleTemp.putBoolean("time", true);
            bundleTemp.putBoolean("iono", true);
            bundleTemp.putBoolean("utc", true);
            bundleTemp.putBoolean("health", true);
        } else if (start_mode == GPSModeEnum.Warm.ordinal()) {
            Log.v(TAG, "Radio Warm Start is selected");
            bundleTemp.putBoolean("ephemeris", true);
        } else {
            Log.v(TAG, "Radio Full Start is selected");
            bundleTemp.putBoolean("all", true);
        }

        return bundleTemp;
    }
    /* @}*/
    public Bundle StartGpsMode() {
        final SharedPreferences preferences = mContext
                .getSharedPreferences(START_MODE,
                        android.content.Context.MODE_PRIVATE);
        int start_mode = preferences.getInt(START_MODE, GPSModeEnum.Hot.ordinal());
        return perpareGpsMode(start_mode);
    }

    public void resetAGPSTextViewItemStatus(TextView item, String key, String value) {
        setAGPSInfo("PROPERTY", key, value);
        item.setText(value);
    }

    public void resetAGPSCheckBoxItemStatus(CheckBox item, String key, boolean value) {
        setAGPSInfo("PROPERTY", key, value ? "TRUE" : "FALSE");
        item.setChecked(value);
    }

    public void initAGPSTextViewItemStatus(TextView item, String key) {
        String value = getAGPSInfo("PROPERTY", key);
        item.setText(value);
    }
    public void initAGPSCheckBoxItemStatus(CheckBox item, String key) {
        String enable = getAGPSInfo("PROPERTY", key);
        if ("TRUE".equals(enable)) {
            item.setChecked(true);
        } else {
            item.setChecked(false);
        }
    }
    public void setCheckBoxListener(CheckBox item, final String key) {
        item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAGPSInfo("PROPERTY", key,(isChecked ? "TRUE" : "FALSE"));
            }
        });
    }

    // Change the interval
    public int modifyCountDown(int count) {
        int[] array = {
                -15, 15, -10, 10, -5, 5, 0
        };
        count += array[mCurrentTimes % 7];
        if (count < 0)
            count = 0;
        return count;
    }

    public boolean getmIsAutoTransferTestRunning() {
        synchronized (mAutoTransferTestRunningLock) {
            return mIsAutoTransferTestRunning;
        }
    }

    public void setmIsAutoTransferTestRunning(boolean isrunning) {
        synchronized (mAutoTransferTestRunningLock) {
            mIsAutoTransferTestRunning = isrunning;
        }
    }

    public boolean isGpsOpen() {
        LocationManager locationManager = (LocationManager) mContext.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        // Weather gps is open or not
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_HINT(TextView tvResultHint){
        if (!SgpsUtils.mOutputAUTOTESTLog.openMyLog(constructMylogFilename("Autotestlog" +mCurrentMode.toString()))) {
            Toast.makeText(mContext, R.string.toast_create_file_failed,Toast.LENGTH_LONG).show();
            return;
        }
        DateFormat dateformat = new SimpleDateFormat("yyyyMMddhhmmss");
        String test_head = String.format(mContext.getString(R.string.autotestlog_title),dateformat.format(new Date(System
                .currentTimeMillis())));

        test_head += String.format(mContext.getString(R.string.autotestlog_refence), mTestLatitude,mTestLongitude);

        test_head += mContext.getString(R.string.autotestlog_head);
        mOutputAUTOTESTLog.writeMyLog(test_head);

        if (null != tvResultHint) {
            tvResultHint.setText( mContext.getString(R.string.result_hint));
        }
    }

    public String doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END(boolean firstLonisEmpty,float aveTTFF,float aveDistance){
        String success_rate_text;
        if(firstLonisEmpty){
            success_rate_text=String.format(mContext.getString(R.string.success_rate_format),mTTFFTimeoutCont,mAutoTransferTotalTimes,0.0);
        }else {
            float rate=(float)(mAutoTransferTotalTimes - mTTFFTimeoutCont) / mAutoTransferTotalTimes;
            success_rate_text = String.format(mContext.getString(R.string.success_rate_format),
                    mTTFFTimeoutCont, mAutoTransferTotalTimes, rate);
        }
        Arrays.sort(mDistance);
        Arrays.sort(mTtff);
        float maxFirstDistance = 0;
        float maxTtff = 0;
        float m68FirstDistance = 0;
        float m68Ttff = 0;
        float m95FirstDistance = 0;
        float m95Ttff = 0;
        float totalInusedAve = 0;
        float totalViewAve = 0;
        float gpsInusedAve = 0;
        float gpsViewAve = 0;
        float gnssInusedAve = 0;
        float gnssViewAve = 0;
        float bdInusedAve = 0;
        float bdViewAve = 0;
        float sateTrackingAve = 0;
        if (mSatelliteTestCont > 0) {
            totalInusedAve = calcAveValue(mTotalInused,mSatelliteTestCont);
            totalViewAve = calcAveValue(mTotalView,mSatelliteTestCont);
            gpsInusedAve =calcAveValue(mGpsInUsed,mSatelliteTestCont);
            gpsViewAve = calcAveValue(mGpsView,mSatelliteTestCont);
            gnssInusedAve = calcAveValue(mGlonassInUsed,mSatelliteTestCont);
            gnssViewAve = calcAveValue(mGlonassView,mSatelliteTestCont);
            bdInusedAve = calcAveValue(mBeidouInUsed,mSatelliteTestCont);
            bdViewAve = calcAveValue(mBeidouView,mSatelliteTestCont);
            sateTrackingAve = calcAveValue(mSateTracking,mSatelliteTestCont);
        }else{
            Arrays.fill(mSatelliteInUsedandViewMinValues,0);
            Arrays.fill(mSatelliteInUsedandViewMaxValues,0);
        }
        if (mDistanceCont >= 2) {
            maxFirstDistance = mDistance[mDistanceCont - 1];
            m68FirstDistance = mDistance[(int) (mDistanceCont * 0.68 - 1)];
            m95FirstDistance = mDistance[(int) (mDistanceCont * 0.95 - 1)];
        }
        if (mTtffcont >= 2) {
            maxTtff = mTtff[mTtffcont - 1];
            m68Ttff = mTtff[(int) (mTtffcont * 0.68 - 1)];
            m95Ttff = mTtff[(int) (mTtffcont * 0.95 - 1)];
        }
        Log.v(TAG, "mSatelliteTestCont: " + mSatelliteTestCont + " totalInusedAve=" + totalInusedAve);
        String ttff_data =String.format(mContext.getString(R.string.ttff_data_format),  aveTTFF/ 1000, m68Ttff / 1000,m95Ttff / 1000 ,maxTtff / 1000);
        String distance_data =String.format(mContext.getString(R.string.distance_data_format), aveDistance,m68FirstDistance, m95FirstDistance,maxFirstDistance);
        String sate_status = String.format(mContext.getString(R.string.sate_status_format), totalInusedAve,sateTrackingAve);

        String sate_status_total_min_max_ave =String.format(mContext.getString(R.string.sate_status_total_format),mSatelliteInUsedandViewMinValues[InUsedandViewEnum.TotalInUsed.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.TotalInUsed.ordinal()], totalInusedAve, mSatelliteInUsedandViewMinValues[InUsedandViewEnum.TotalView.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.TotalView.ordinal()] ,totalViewAve);
        String sate_status_gps_min_max_ave = String.format(mContext.getString(R.string.sate_status_gps_format),mSatelliteInUsedandViewMinValues[InUsedandViewEnum.GpsInUsed.ordinal()],mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.GpsInUsed.ordinal()],gpsInusedAve,mSatelliteInUsedandViewMinValues[InUsedandViewEnum.GpsView.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.GpsView.ordinal()],gpsViewAve);
        String sate_status_gnss_min_max_ave = String.format(mContext.getString(R.string.sate_status_glo_format),mSatelliteInUsedandViewMinValues[InUsedandViewEnum.GlonassInUsed.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.GlonassInUsed.ordinal()],gnssInusedAve,mSatelliteInUsedandViewMinValues[InUsedandViewEnum.GlonassView.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.GlonassView.ordinal()],gnssViewAve);
        String sate_status_bd_min_max_ave =String.format(mContext.getString(R.string.sate_status_bds_format),mSatelliteInUsedandViewMinValues[InUsedandViewEnum.BeidouInUsed.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.BeidouInUsed.ordinal()], bdInusedAve,mSatelliteInUsedandViewMinValues[InUsedandViewEnum.BeidouView.ordinal()], mSatelliteInUsedandViewMaxValues[InUsedandViewEnum.BeidouView.ordinal()],bdViewAve);
        String sate_status_min_max_ave = sate_status_total_min_max_ave + sate_status_gps_min_max_ave + sate_status_gnss_min_max_ave + sate_status_bd_min_max_ave;

        return success_rate_text + ttff_data + distance_data + sate_status+ sate_status_min_max_ave;
    }

    public String doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG(GpsStatus lastStatusRecord){
        String test_item;
        String test_item_view;
        //Unisoc:Bug 1442605 fatal exception ArrayIndexOutOfBoundsException
        if (!mTtffTimeoutFlag && mDistanceCont >= 1) {
            if (mTtffcont<mCurrentTimes){
                mTtff[mTtffcont]=mLastTtffValue;
                mTtffcont++;
            }
            mTestDistanceSum += mDistance[mDistanceCont - 1];
            mTestTTFFSum += mTtff[mTtffcont-1];

            long mSFST = mSerchFirstSateTime - mStartSerchTime;
            test_item =  mCurrentTimes + " "
                    + String.format("%.6f" , ((float) mFirstFixLatitude[mDistanceCont - 1])) + " "
                    + String.format("%.6f" , ((float) mFirstFixLongitude[mDistanceCont - 1])) + " "
                    + String.format("%.2f" , mDistance[mDistanceCont - 1] );
            test_item_view=test_item+ " " + String.format("%.1f",(mLastTtffValue / 1000.0))+"\n";

            if (ISGe2 || ISMarlin3 || ISMarlin3lite) {
                test_item += " " + mGe2Cn_Sr;
            } else {
                test_item += " " + getSatelliteStatus(lastStatusRecord.getSatellites());
            }
            test_item += " " + mLastTtffValue / 1000.0 + " " + mSFST + " " + mCurrentMode.toString() + "\n";
        } else {
            test_item = mCurrentTimes + " N/A" + "\n";
            test_item_view=test_item;
        }
        // save data
        mOutputAUTOTESTLog.writeMyLog(test_item);

        return test_item_view;
    }

    public void doNoiseScan(String nmea,TextView[] tvResult) {
        if (mCurrScanTimesCount < mCurrScanTimes && mCurrSCanPeriodCount < mCurrSCanPeriod) {
            String rst;
            //get the first
            String[] str = nmea.split(",");
            Log.v(TAG, "nmea: " + nmea + "RSSI=" + Integer.parseInt(str[1].trim()));
            mOutputRSSILog.writeMyLog(nmea);
            double drssi = 20 * Math.log10(Double.parseDouble(str[1].trim()));
            mCurrSCanPeriodCount++;
            if(mCurrScanTimesCount == 0){
                mltFirstData.add(String.format("%.1f",drssi));
                if(mCurrSCanPeriodCount == mCurrSCanPeriod){
                    mCurrSCanPeriodCount=0;
                    mCurrScanTimesCount++;
                    float[] mfRssi=extractValuesFromNoiseRssi(mltFirstData);
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Data),"1st",mfRssi[0],mfRssi[1],mfRssi[2]);
                }else{
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Testing), "1st");
                }
                tvResult[0].setText(rst);
                if(mCurrScanTimes == 2){
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Waiting), "2st");
                    tvResult[1].setText(rst);
                }else if(mCurrScanTimes == 3){
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Waiting), "2st");
                    tvResult[1].setText(rst);
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Waiting), "3st");
                    tvResult[2].setText(rst);
                }
            }else if(mCurrScanTimesCount == 1){
                mltSecondData.add(String.format("%.1f",drssi));
                if(mCurrSCanPeriodCount == mCurrSCanPeriod){
                    mCurrSCanPeriodCount=0;
                    mCurrScanTimesCount++;
                    float[] mfRssi=extractValuesFromNoiseRssi(mltSecondData);
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Data),"2st",mfRssi[0],mfRssi[1],mfRssi[2]);
                }else{
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Testing), "2st");
                }
                tvResult[1].setText(rst);
                if(mCurrScanTimes == 3){
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Waiting),"3st");
                    tvResult[2].setText(rst);
                }
            }else{
                mltThirdData.add(String.format("%.1f",drssi));
                if(mCurrSCanPeriodCount == mCurrSCanPeriod){
                    mCurrSCanPeriodCount=0;
                    mCurrScanTimesCount++;
                    float[] mfRssi=extractValuesFromNoiseRssi(mltThirdData);
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Data),"3st",mfRssi[0],mfRssi[1],mfRssi[2]);
                }else{
                    rst=String.format(mContext.getString(R.string.NoiseScan_TestResult_Testing), "3st");
                }
                tvResult[2].setText(rst);
            }
        }
    }

    public Bundle initAutoCircleTestThread(GPSModeEnum mode,int ntotalTimes){
        mTTFFTimeoutCont = 0;
        mTtffcont = 0;
        mDistanceCont = 0;
        mSatelliteTestCont = 0;
        mLastTtffValue=0;
        mTestTTFFSum = 0;
        mTestDistanceSum = 0;
        mSateTracking = new int[ntotalTimes];
        mTtff = new float[ntotalTimes];
        mDistance = new float[ntotalTimes];
        mFirstFixLatitude = new double[ntotalTimes];
        mFirstFixLongitude = new double[ntotalTimes];
        mTotalInused = new int[ntotalTimes];
        mTotalView = new int[ntotalTimes];
        mGpsInUsed = new int[ntotalTimes];
        mGpsView = new int[ntotalTimes];
        mGlonassInUsed = new int[ntotalTimes];
        mGlonassView = new int[ntotalTimes];
        mBeidouInUsed = new int[ntotalTimes];
        mBeidouView = new int[ntotalTimes];
        Arrays.fill(mSatelliteInUsedandViewMinValues,100);
        Arrays.fill(mSatelliteInUsedandViewMaxValues,0);
        /* spreadst: switch mode. @{ */
        mCurrentMode = mode;
        return perpareGpsMode(mode.ordinal());
    }

    public void doMakeText(View aboutET,String info){
        int[] rt = new int[2];
        aboutET.getLocationInWindow(rt);
        Toast toast = Toast.makeText(mContext,info, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, rt[0],rt[1]);
        toast.show();
    }
    // Given the value of the disassembled into scale value of the x axis
    private String[] getxLabel(int maxValue) {
        String[] xLabel;
        int xMaxSize;
        int xStep;
        if (maxValue <= 0) {
            return null;
        }
        if (maxValue <= 20) {
            xStep = 1;
        } else if (maxValue <= 60) {
            xStep = 5;
        } else if (maxValue <= 300) {
            xStep = 10;
        } else {
            xStep = 50;
        }
        xMaxSize = 1 + maxValue / xStep;
        if (maxValue % xStep != 0) {
            xMaxSize++;
        }
        xLabel = new String[xMaxSize];
        for (int j = 0; j < xMaxSize; j++) {
            xLabel[j] = String.format("%d", xStep * j);
        }
        return xLabel;
    }

    // Given the value of the disassembled into y scale value
    private String[] getyLabel(float maxValue, boolean hasNegative) {
        String[] yLabel;
        int yMaxSize;
        float yStep;
        float curCeil = (float) Math.ceil(maxValue);
        if (maxValue <= 0) {
            return null;
        }

        if (maxValue <= 5) {
            yStep = 0.2f;
        } else if (maxValue <= 10) {
            yStep = 0.5f;
        } else if (maxValue <= 20) {
            yStep = 1.0f;
        } else if (maxValue <= 50) {
            yStep = 5.0f;
        } else if (maxValue <= 100) {
            yStep = 10.0f;
        } else {
            yStep = 50.0f;
        }
        yMaxSize = (int) (curCeil / yStep);
        if (curCeil % yStep != 0) {
            yMaxSize++;
        }
        Log.e(TAG, "getyLabel()-> hasNegative == " + hasNegative);
        if (hasNegative) {
            yLabel = new String[2 * yMaxSize + 1];
            for (int j = yMaxSize; j < (2 * yMaxSize + 1); j++) {
                yLabel[j] = String.format("%.1f", yStep * (j - yMaxSize));
            }
            for (int j = 0; j < yMaxSize; j++) {
                yLabel[j] = String.format("%.1f", yStep * (j - yMaxSize));
            }
        } else {
            yLabel = new String[yMaxSize + 1];
            for (int j = 0; j < (yMaxSize + 1); j++) {
                yLabel[j] = String.format("%.1f", yStep * j);
            }
        }
        return yLabel;
    }
    public void doNoiseScanCurveChart(){
        float[] fCur = extractValuesFromNoiseRssi(mltFirstData);
        float fMax = fCur[0];
        int xMaxSize=mltFirstData.size();
        if(mCurrScanTimes == 2){
            fCur = extractValuesFromNoiseRssi(mltSecondData);
            if(fMax < fCur[0]){
                fMax=fCur[0];
            }
            if(xMaxSize<mltSecondData.size()){
                xMaxSize=mltSecondData.size();
            }
        }else if(mCurrScanTimes == 3){
            fCur = extractValuesFromNoiseRssi(mltSecondData);
            if(fMax < fCur[0]){
                fMax=fCur[0];
            }
            if(xMaxSize<mltSecondData.size()){
                xMaxSize=mltSecondData.size();
            }
            fCur = extractValuesFromNoiseRssi(mltThirdData);
            if(fMax < fCur[0]){
                fMax=fCur[0];
            }
            if(xMaxSize<mltThirdData.size()){
                xMaxSize=mltThirdData.size();
            }
        }
        // String[]
        // xLabel={"0","5","10","15","20","25","30","35","40","45","50","55","60"};
        String[] xLabel = getxLabel(xMaxSize);
        // String[] yLabel= {"-25", "-20", "-15", "-10", "-5", "0", "5", "10","15", "20", "25"};
        String[] yLabel = getyLabel(fMax, false);

        List<float[]> data = new ArrayList<float[]>();
        List<Integer> color = new ArrayList<Integer>();
        if (mltFirstData.size() > 0) {
            float[] datappb = new float[mltFirstData.size()];
            for (int i = 0; i < mltFirstData.size(); i++) {
                datappb[i] = Float.parseFloat(mltFirstData.get(i));
            }
            data.add(datappb);
            color.add(R.color.color13);
        }
        if (mltSecondData.size() > 0) {
            float[] datappb = new float[mltSecondData.size()];
            for (int i = 0; i < mltSecondData.size(); i++) {
                datappb[i] = Float.parseFloat(mltSecondData.get(i));
            }
            data.add(datappb);
            color.add(R.color.color14);
        }
        if (mltThirdData.size() > 0) {
            float[] datappb = new float[mltThirdData.size()];
            for (int i = 0; i < mltThirdData.size(); i++) {
                datappb[i] = Float.parseFloat(mltThirdData.get(i));
            }
            data.add(datappb);
            color.add(R.color.color25);
        }
        mCurveChart.addView(new LineGraphicView(mContext, xLabel, yLabel,data, color));
    }

    public void showLoadingDialog(String info){
        LoadingDialog.Builder builder=new LoadingDialog.Builder(mContext)
                .setMessage(info)
                .setCancelable(false);
        hwndLoadingDialog=builder.create();
        hwndLoadingDialog.show();
    }
    public void closeLoadingDialog(){
        if(hwndLoadingDialog != null){
            hwndLoadingDialog.dismiss();
            hwndLoadingDialog=null;
        }
    }

    public int getTruePositionListDefaultIndex(){
        SharedPreferences preferences = mContext.getSharedPreferences("save.gps",android.content.Context.MODE_PRIVATE);
        String latitude= preferences.getString("save.gps.latitude", "");
        String longitude= preferences.getString("save.gps.longitude", "");
        String[] latitudeArr=mContext.getResources().getStringArray(R.array.true_position_Latitude_values);
        String[] longitudeArr=mContext.getResources().getStringArray(R.array.true_position_Longitude_values);
        if(latitude != null && longitude != null){
            for(int i=1;i<latitudeArr.length;i++){
                if(latitudeArr[i].equalsIgnoreCase(latitude.trim()) && longitudeArr[i].equalsIgnoreCase(longitude.trim())){
                    return i;
                }
            }
        }
        return 0;
    }
}

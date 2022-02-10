
package com.spreadtrum.sgps;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.OnNmeaMessageListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;
import android.graphics.Color;
import android.util.Log;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.ScrollingMovementMethod;
import android.location.BatchedLocationCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import android.provider.Settings;

public class SgpsActivity extends TabActivity implements SatelliteDataProvider, NmeaParser.LocalNmeaListener{
    private static final String TAG = "SGPS/Activity";
    private SgpsUtils mSgpsUtils=null;
    private Button mBtnNmeaStart = null;
    private Button mBtnNMEAStop = null;
    private Button mBtnGpsTestSave = null;
    private Button mBtnGpsTestAutoTransferStart = null;
    private Button mBtnGpsTestAutoTransferStop = null;
    private NumberPickerView mEtTestTimes = null;
    private NumberPickerView mEtTTFFTimeout = null;
    private NumberPickerView mEtTTFFInterval = null;
    private NumberPickerView mEtModeInterval = null;
    private CheckBox mTvUartLogSwitch = null;// tv_uart_log_switch
    private ScrollView mScrollLog = null;
    private final RadioButton[] mRadioBtnHWCF = new RadioButton[4];
    private Button mBtnStart = null;
    private Button mBtnStop = null;
    private boolean mbOpenSGPS=false;
    private RadioButton mRadioBtnGps = null;
    private RadioButton mRadioBtnGlonass = null;
    private RadioButton mRadioBtnBDS = null;
    private RadioButton mRadioBtnGlonassGps = null;
    private RadioButton mRadioBtnGpsBDS = null;
    private Switch mSpreadOrBitSwitch = null;
    private RadioButton  mRadioBtnGpsBd2Glonass = null;
    private RadioButton  mRadioBtnGpsBd2Galileo = null;
    private RadioButton  mRadioBtnGpsB1cGlonass =  null;
    private RadioButton  mRadioBtnGpsB1cGalileo=  null;
    private RadioButton  mRadioBtnGpsGlonassGalileo=null;
    private RadioButton  mRadioBtnCustom = null;
    private TextView mModeSystemCustomEdit = null;
    private Button  mBtnCustom = null;
    private Spinner true_position_Spinner = null;
    private Switch mRealEPHSwitch = null;
    private Switch mGNSSLogSwitch = null;
    private Switch mGNSSRtdSwitch = null;
    private Switch mGNSSRtkSwitch = null;
    // added end
    private TextView mTvResultHint = null;
    private TextView mTvResultLog = null;
    private EditText mEtTestLatitude = null;
    private EditText mEtTestLongitude = null;
    private AlertDialog.Builder gpsBuilder = null;
    private TextView mTvNmeaLog = null;
    private TextView mTvNMEAHint = null;
    private SatelliteSkyView mSatelliteView = null;
    private SatelliteSignalView mSignalView = null;
    private final CheckBox[] mCircAutoTestHCWF=new CheckBox[4];
    private String mCurrentTabTag="";
    private boolean mSgpsActivityonPause = false;
    private boolean mStartNmeaRecord = false;
    private boolean mIsRunInBg = true;// default can run in background mode
    private boolean IsStartNoiseScan = false;
    /* SPRD:add for GE2 @{ */
    private final OnClickListener mBtnGpsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mRadioBtnGps) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSONLY.ordinal()]);
            } else if (v == mRadioBtnGlonass) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASS.ordinal()]);
            } else if (v == mRadioBtnBDS) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_BDSONLY.ordinal()]);
            } else if (v == mRadioBtnGlonassGps) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASSGPS.ordinal()]);
            } else if (v == mRadioBtnGpsBDS) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBDS.ordinal()]);
            }
        }
    };
    /* SPRD:add for Marlin3 @{ */
    private final OnClickListener mBtnModeSystemConfigClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean bflag = false;
            if ("TRUE".equals(SgpsUtils.getGPSInfo("PROPERTY", "MINIPVT"))) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_MINIPVT_FALSE.ordinal()]);
                return;
            }
            if (v == mRadioBtnGpsBd2Glonass) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBD2GLONASS.ordinal()]);
            } else if (v == mRadioBtnGpsBd2Galileo) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBD2Galileo.ordinal()]);
            } else if (v == mRadioBtnGpsB1cGlonass) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSB1CGLONASS.ordinal()]);
            } else if (v == mRadioBtnGpsB1cGalileo) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSB1CGalileo.ordinal()]);
            } else if (v == mRadioBtnGpsGlonassGalileo) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSGLONASSGalileo.ordinal()]);
            } else if (v == mRadioBtnCustom) {
                bflag=true;
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPSCUSTOM.ordinal()]);
            }
            mModeSystemCustomEdit.setEnabled(bflag);
            mBtnCustom.setEnabled(bflag);
        }
    };
    // add agps test
    private int[] mRGPlaneSwitchItemId;
    private RadioGroup mRGMolrPositionMethod = null;
    private int[] mRGMolrPositionMethodItemId;
    private RadioGroup mRGSimSelection = null;
    private int[] mRGSimSelectionItemId;
    private CheckBox mExternalAddressCheckBox = null;
    private CheckBox mMLCNumberCheckBox = null;
    private CheckBox mAutoResetCheckBox = null;
    private EditText mExternalAddressEditText = null;
    private EditText mMLCNumberEditText = null;
    private Button mExternalAddressSaveButton = null;
    private Button mMLCNumberSaveButton = null;
    private Switch mMOLASwitch = null;
    private boolean resetToDefault = false;
    private Menu hNMEAtabMenu=null;
    // agps userplane settings
    private RadioGroup mAGPSModeRadioGroup = null;
    private int[] mAGPSModeRadioGroupItemId;
    private Button mSLPTemplateButton = null;
    private TextView mSLPAddressTextView = null;
    private Button mSLPAddressEditButton = null;
    private TextView mSLPPortTextView = null;
    private Button mSLPPortEditButton = null;
    private CheckBox mTLSEnableCheckBox = null;
    private RadioGroup mSetIDRadioGroup = null;
    private int[] mSetIDRadioGroupItemId;
    private RadioGroup mAccuracyUnitRadioGroup = null;
    private int[] mAccuracyUnitRadioGroupItemId;
    private TextView mHorizontalAccuracyTextView = null;
    private Button mHorizontalAccuracyEditButton = null;
    private TextView mVerticalAccuracyTextView = null;
    private Button mVerticalAccuracyEditButton = null;
    private TextView mLocationAgeTextView = null;
    private Button mLocationAgeEditButton = null;
    private TextView mDelayTextView = null;
    private Button mDelayEditButton = null;
    private CheckBox mCertificateVerificationCheckBox = null;
    private Button mCertificateVerificationEditButton = null;
    private CheckBox mEnableLabIOTTestCheckBox = null;
    private CheckBox mEnableeCIDCheckBox = null;
    // agps supl2.0
    private CheckBox mEnableSUPL2CheckBox = null;
    private LinearLayout mAGPSSUPL2Layout = null;
    private TextView mMSISDNTextView = null;
    private Button mMSISDNEditButton = null;
    private CheckBox mEnable3rdMSISDNCheckBox = null;
    private TextView m3rdMSISDNTextView = null;
    private Button m3rdMSISDNEditButton = null;
    private Button mTriggerStartButton = null;
    private Button mTriggerAbortButton = null;
    private RadioGroup mTriggerTypeRadioGroup = null;
    private int[] mTriggerTypeGroupItemId;
    private LinearLayout mPerodicTypeLayout = null;
    private LinearLayout mAreaTypeLayout = null;
    private TextView mPosMethodView = null;
    private Button mPosMethodSelectButton = null;
    private TextView mPerodicMinIntervalTextView = null;
    private Button mPerodicMinIntervalEditButton = null;
    private TextView mPerodicStartTimeTextView = null;
    private Button mPerodicStartTimeEditButton = null;
    private TextView mPerodicStopTimeTextView = null;
    private Button mPerodicStopTimeEditButton = null;
    private TextView mAreaTypeTextView = null;
    private Button mAreaTypeSelectButton = null;
    private TextView mAreaMinIntervalTextView = null;
    private Button mAreaMinIntervalEditButton = null;
    private TextView mMaxNumTextView = null;
    private Button mMaxNumEditButton = null;
    private TextView mAreaStartTimeTextView = null;
    private Button mAreaStartTimeEditButton = null;
    private TextView mAreaStopTimeTextView = null;
    private Button mAreaStopTimeEditButton = null;
    private TextView mGeographicTextView = null;
    private TextView mGeoRadiusTextView = null;
    private Button mGeoRadiusEditButton = null;
    private TextView mLatitudeTextView = null;
    private Button mLatitudeEditButton = null;
    private TextView mLongitudeTextView = null;
    private Button mLongitudeEditButton = null;
    private RadioGroup mSignRadioGroup = null;
    private int[] mSignRadioGroupItemId;
    // agps common
    private CheckBox mAllowNetworkInitiatedRequestCheckBox = null;
    private CheckBox mAllowEMNotificationCheckBox = null;
    private CheckBox mAllowRoamingCheckBox = null;
    private CheckBox mLogSUPLToFileCheckBox = null;
    private NumberPickerView mNotificationTimeoutSpinner = null;
    private NumberPickerView mVerificationTimeoutSpinner = null;
    private Button mResetToDefaultButton = null;
    private Button mNITestSelectButton = null;
    //for noise scan
    private Button mNoiseScanStart = null;
    private Button mNoiseScanStop = null;
    private NumberPickerView mSpinRepeattimes = null;
    private NumberPickerView mNoiseScanPeriod = null;
    private final TextView[] mtvNoiseScanResult=new TextView[3];

    private Switch mSingleSatelliteSwitch = null;
    private final TextView[] mCurrentTimeHWCF=new TextView[4];
    private TextView mAverageCn0TextView = null;
    private final TextView[] mLastTTFFHWCF=new TextView[4];
    private final TextView[] mAverageHWCF=new TextView[4];
    private final TextView[] mSuccessRateHWCF=new TextView[4];
    //add new feature for gps download bin
    private RadioButton mRadiobtnGpsBeidou = null;
    private RadioButton mRadiobtnGpsGlonass = null;
    private NmeaParser nmeaParser = null;

    private final CompoundButton.OnCheckedChangeListener mSwitchCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int viewId = buttonView.getId();
            mSgpsUtils.doMySwitchChange(viewId,isChecked);
        }
    };

    //add new feature for gps download bin by ansel.li
    private final OnClickListener mBtnGpsImgModeClickListener = new OnClickListener(){
        @Override
        public void onClick(View v) {
            if (v == mRadiobtnGpsBeidou) {
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_IMG_MODE_BD.ordinal()]);
                mRadioBtnGlonass.setEnabled(false);
                mRadioBtnGlonassGps.setEnabled(false);
                mRadioBtnBDS.setEnabled(true);
                mRadioBtnGpsBDS.setEnabled(true);
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_CP_MODE_BD_DEF.ordinal()]);
                startGPS(true);
            } else if(v == mRadiobtnGpsGlonass){
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_IMG_MODE_GL.ordinal()]);
                mRadioBtnBDS.setEnabled(false);
                mRadioBtnGpsBDS.setEnabled(false);
                mRadioBtnGlonass.setEnabled(true);
                mRadioBtnGlonassGps.setEnabled(true);
                SocketUtils.sendCommand(SgpsUtils.GPS_EXTRA_DATA[SgpsUtils.GPSGroupEnum.GPS_EXTRA_CP_MODE_GL_DEF.ordinal()]);
                startGPS(true);
            }
        }
    };
    private final Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            mSgpsUtils.domScrollToBottom(mTvNmeaLog,mScrollLog);
        }
    };
    /* @} */
    private long mLastTimestamp = -1;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("MissingPermission")
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage " + msg.what);
            switch (msg.what) {
                case SgpsUtils.HANDLE_COUNTER:
                    if (!mSgpsUtils.mFirstFix) {
                        mSgpsUtils.mTtffValue += SgpsUtils.COUNT_PRECISION;
                        TextView tvTtff = findViewById(R.id.tv_ttff);
                        tvTtff.setText(mSgpsUtils.mTtffValue+ getString(R.string.time_unit_ms));
                        tvTtff.setTextColor(Color.RED);
                    }
                    showGPSVersion();
                    mSgpsUtils.mDurationTime += SgpsUtils.COUNT_PRECISION;
                    TextView tvDurationtime = findViewById(R.id.tv_duration_time);
                    tvDurationtime.setText(mSgpsUtils.mDurationTime+ getString(R.string.time_unit_ms));
                    this.sendEmptyMessageDelayed(SgpsUtils.HANDLE_COUNTER,SgpsUtils.COUNT_PRECISION);
                    break;
                case SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_HINT:
                    mTvResultLog.setText("");
                    mSgpsUtils.doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_HINT(mTvResultHint);
                    break;
                case SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG:
                    if (!mSgpsUtils.mTtffTimeoutFlag && null == mSgpsUtils.mLastStatus){
                        break;
                    }
                    String result=mSgpsUtils.doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG(mSgpsUtils.mLastStatus);
                    mTvResultLog.append(result);

                    if(!mSgpsUtils.mTtffTimeoutFlag){
                        float aveTTFF=0.0f;
                        if(mSgpsUtils.mCurrentTimes > mSgpsUtils.mTTFFTimeoutCont){
                            aveTTFF=mSgpsUtils.mTestTTFFSum/(mSgpsUtils.mCurrentTimes - mSgpsUtils.mTTFFTimeoutCont);
                        }
                        mLastTTFFHWCF[mSgpsUtils.mCurrentMode.ordinal()].setText(mSgpsUtils.mLastTtffValue + getString(R.string.time_unit_ms));
                        mAverageHWCF[mSgpsUtils.mCurrentMode.ordinal()].setText(aveTTFF + getString(R.string.time_unit_ms));
                    }
                    break;
                case SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END:
                    float aveTTFF=0.0f;
                    float aveDistance=0.0f;
                    if(mSgpsUtils.mAutoTransferTotalTimes > mSgpsUtils.mTTFFTimeoutCont){
                        aveTTFF=mSgpsUtils.mTestTTFFSum/(mSgpsUtils.mAutoTransferTotalTimes - mSgpsUtils.mTTFFTimeoutCont);
                        aveDistance=mSgpsUtils.mTestDistanceSum/(mSgpsUtils.mAutoTransferTotalTimes - mSgpsUtils.mTTFFTimeoutCont);
                    }
                    String test_end = String.format(getString(R.string.test_end_format), aveDistance,aveTTFF / 1000.0);
                    TextView firstLon = findViewById(R.id.first_longtitude_text);
                    String strValue=firstLon.getText().toString().trim();
                    Log.d(TAG, "Enter HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END");
                    String restult=mSgpsUtils.doHANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END(TextUtils.isEmpty(strValue),aveTTFF,aveDistance);

                    mSuccessRateHWCF[mSgpsUtils.mCurrentMode.ordinal()].setText(restult);
                    SgpsUtils.mOutputAUTOTESTLog.writeMyLog( restult);
                    SgpsUtils.mOutputAUTOTESTLog.closeMyLog();
                    mTvResultLog.append(test_end);
                    break;
                case SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_PROVIDER:
                    mSgpsUtils.mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                    break;
                case SgpsUtils.HANDLE_AUTO_TRANSFER_START_BUTTON_UPDATE:
                    mSgpsUtils.closeLoadingDialog();
                    controlViewState(1 == msg.arg1);
                    break;
                case SgpsUtils.HANDLE_AUTO_TRANSFER_UPDATE_CURRENT_MODE:
                    String info=String.format("%d/%d",msg.arg2, mSgpsUtils.mAutoTransferTotalTimes);
                    mCurrentTimeHWCF[msg.arg1].setText(info);
                    if(msg.arg1 == SgpsUtils.GPSModeEnum.Hot.ordinal()){
                        info = "mode = Hot, runtime = " + msg.arg2;
                    }else if(msg.arg1 == SgpsUtils.GPSModeEnum.Cold.ordinal()){
                        info = "mode = Cold, runtime = " + msg.arg2;
                    }else if(msg.arg1 == SgpsUtils.GPSModeEnum.Warm.ordinal()){
                        info = "mode = Warm, runtime = " + msg.arg2;
                    }else{
                        info = "mode = Factory, runtime = " + msg.arg2;
                    }
                    Toast.makeText(SgpsActivity.this, info, Toast.LENGTH_SHORT).show();
                    break;
                case SgpsUtils.HANDLE_CUSTOM_CURVECHART:
                    mSgpsUtils.doNoiseScanCurveChart();
                    mSgpsUtils.mCurveChart.setVisibility(View.VISIBLE);
                    mSgpsUtils.mCurveChart.invalidate();
                    mNoiseScanStart.setEnabled(true);
                    mNoiseScanStop.setEnabled(false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private final LocationListener mLocListener = new LocationListener() {
        // @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Enter onLocationChanged function");
            if (!mSgpsUtils.mFirstFix) {
                Log.w(TAG, "mFirstFix is false, onLocationChanged");
                if (null != mSgpsUtils.mLastStatus) {// get prefix ttff
                    mSgpsUtils.mTtffValue = mSgpsUtils.mLastStatus.getTimeToFirstFix();
                    mSgpsUtils.mFirstFix = true;
                    TextView tvTtff = findViewById(R.id.tv_ttff);
                    tvTtff.setText(mSgpsUtils.mTtffValue+ getString(R.string.time_unit_ms));
                    tvTtff.setTextColor(Color.GREEN);
                }
            }
            Date d = new Date(location.getTime());
            String date = String.format("%s %+02d %04d/%02d/%02d", "GMT",
                    d.getTimezoneOffset(), d.getYear() + 1900,
                    d.getMonth() + 1, d.getDate());
            String time = String.format("%02d:%02d:%02d", d.getHours(),
                    d.getMinutes(), d.getSeconds());

            TextView tvTime = findViewById(R.id.tv_time);
            if (tvTime != null) {
                tvTime.setText(time);
            }

            TextView tvDate = findViewById(R.id.tv_date);
            tvDate.setText(date);

            if (mSgpsUtils.mShowFirstFixLocate) {
                mSgpsUtils.mShowFirstFixLocate = false;
                TextView firstLon = findViewById(R.id.first_longtitude_text);
                firstLon.setText(String.valueOf(location.getLongitude()));
                TextView firstLat = findViewById(R.id.first_latitude_text);
                firstLat.setText(String.valueOf(location.getLatitude()));
            }
            TextView tvLat = findViewById(R.id.tv_latitude);
            tvLat.setText(String.valueOf(location.getLatitude()));
            TextView tvLon = findViewById(R.id.tv_longitude);
            tvLon.setText(String.valueOf(location.getLongitude()));
            TextView tvAlt = findViewById(R.id.tv_altitude);
            tvAlt.setText(String.valueOf(location.getAltitude()));
            TextView tvAcc = findViewById(R.id.tv_accuracy);
            tvAcc.setText(String.valueOf(location.getAccuracy()));
            TextView tvBear = findViewById(R.id.tv_bearing);
            tvBear.setText(String.valueOf(location.getBearing()));
            TextView tvSpeed = findViewById(R.id.tv_speed);
            tvSpeed.setText(String.valueOf(location.getSpeed()));

            mSgpsUtils.domLocListener(location,mAverageCn0TextView);

            TextView tvTtff = findViewById(R.id.tv_ttff);
            tvTtff.setText(mSgpsUtils.mTtffValue + getString(R.string.time_unit_ms));
            tvTtff.setTextColor(Color.GREEN);
            TextView tvProvider = findViewById(R.id.tv_provider);
            tvProvider.setText(mSgpsUtils.mProvider);
            TextView tvStatus = findViewById(R.id.tv_status);
            tvStatus.setText(mSgpsUtils.mStatus);
        }

        // @Override
        public void onProviderDisabled(String provider) {
            Log.v(TAG, "Enter onProviderDisabled function");
            mSgpsUtils.mProvider = String.format(getString(
                    R.string.provider_status_disabled),
                    LocationManager.GPS_PROVIDER);
            TextView tvProvider = findViewById(R.id.tv_provider);
            tvProvider.setText(mSgpsUtils.mProvider);
        }

        // @Override
        public void onProviderEnabled(String provider) {
            Log.v(TAG, "Enter onProviderEnabled function");
            mSgpsUtils.mProvider = getString(
                    R.string.provider_status_enabled,
                    LocationManager.GPS_PROVIDER);
            TextView tvProvider = findViewById(R.id.tv_provider);
            tvProvider.setText(mSgpsUtils.mProvider);
            mSgpsUtils.mTtffValue = 0;
        }

        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.v(TAG, "Enter onStatusChanged function");
        }
    };
    /* @} */
    private final OnClickListener mBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBtnGpsTestSave){
                Log.d(TAG, "mBtnGPSTestSave is clicked");
                if (0 < mEtTestLatitude.getText().length()
                        && 0 < mEtTestLongitude.getText().length()) {
                    double mTestLatitude;
                    double mTestLongitude;
                    try {
                        mTestLatitude = Double.parseDouble(mEtTestLatitude.getText().toString());
                        mTestLongitude = Double.parseDouble(mEtTestLongitude.getText().toString());
                    } catch (Exception ex) {
                        mSgpsUtils.doMakeText(mEtTestLongitude,getString(R.string.toast_gps_input_range));
                        return;
                    }
                    if (0>Double.compare(mTestLatitude,-360.0) || 0<Double.compare(mTestLatitude,360.0)|| 0>Double.compare(mTestLongitude,-360.0) || 0<Double.compare(mTestLongitude,360.0)) {
                        mSgpsUtils.doMakeText(mEtTestLongitude,getString(R.string.toast_gps_input_range));
                        return;
                    }
                    final SharedPreferences preferences = SgpsActivity.this.getSharedPreferences("save.gps",android.content.Context.MODE_PRIVATE);
                    preferences.edit().putString("save.gps.latitude", Double.toString(mTestLatitude)).commit();
                    preferences.edit().putString("save.gps.longitude", Double.toString(mTestLongitude)).commit();
                    double mCheckLatitude= Double.parseDouble(Objects.requireNonNull(preferences.getString("save.gps.latitude", "360")));
                    double mCheckLongitude= Double.parseDouble(Objects.requireNonNull(preferences.getString("save.gps.longitude", "360")));
                    if( 0 != Double.compare(mTestLatitude,mCheckLatitude) ||  0 != Double.compare(mTestLongitude,mCheckLongitude)){
                        mSgpsUtils.doMakeText(mBtnGpsTestSave,getString(R.string.save_latitude_longitude_failure));
                    }else{
                        mSgpsUtils.doMakeText(mBtnGpsTestSave,getString(R.string.save_latitude_longitude_success));
					}
                }
            }else if(v == mBtnGpsTestAutoTransferStart) {
                mBtnGpsTestAutoTransferStart.setEnabled(false);
                mBtnGpsTestAutoTransferStart.refreshDrawableState();
                Log.d(TAG, "GPSTestAutoTransfer Start button is pressed");
                startGPSAutoTransferTest();
            }else if(v == mBtnGpsTestAutoTransferStop) {
                if (!mSgpsUtils.getmIsAutoTransferTestRunning()) {
                    return;
                }
                mSgpsUtils.setmIsAutoTransferTestRunning(false);
                mSgpsUtils.showLoadingDialog("Stopping...");
                mBtnGpsTestAutoTransferStop.setEnabled(false);
                mBtnGpsTestAutoTransferStop.refreshDrawableState();
                Log.d(TAG, "GPSTestAutoTransfer Stop button is pressed");

            } else if (v == mBtnNmeaStart) {
                Log.d(TAG, "NMEA Start button is pressed");
                if(!mbOpenSGPS && !mSgpsUtils.getmIsAutoTransferTestRunning()){
                    mSgpsUtils.doMakeText(mBtnNmeaStart,"Please open the information page SGPS ");
                    return;
                }
                if (!SgpsUtils.mOutputNMEALog.openMyLog(SgpsUtils.constructMylogFilename("Nmealog"))) {
                    Toast.makeText(SgpsActivity.this, R.string.toast_create_file_failed,Toast.LENGTH_SHORT).show();
                    return;
                }
                // set nmea hint
                mTvNMEAHint.setText((R.string.nmea_hint));
                mStartNmeaRecord = true;
                mBtnNmeaStart.setEnabled(false);
                mBtnNMEAStop.setEnabled(true);
            } else if (v == mBtnNMEAStop) {
                Log.d(TAG, "NMEA Stop button is pressed");
                mStartNmeaRecord = false;
                SgpsUtils.mOutputNMEALog.closeMyLog();
                mBtnNMEAStop.setEnabled(false);
                mBtnNmeaStart.setEnabled(true);
                mTvNMEAHint.setText(R.string.empty);
                mTvNmeaLog.setText(R.string.empty);
            } else if (v == mBtnStart) {
                Log.v(TAG, "mBtnStart Button is pressed");
                mbOpenSGPS=true;
                mSgpsUtils.mShowFirstFixLocate = true;
                Bundle extras = mSgpsUtils.StartGpsMode();
                resetParam(extras,500);
                if (SgpsUtils.ISGe2 || SgpsUtils.ISMarlin3 || SgpsUtils.ISMarlin3lite) {
                    mSgpsUtils.updateSatelliteView(null);
                } else {
                    mSgpsUtils.setSatelliteStatus(null);
                }
                if (mAverageCn0TextView != null) {
                    mAverageCn0TextView.setText("N/A");
                }
                clearInformationLayout();
                mBtnStop.setEnabled(true);
                mBtnStart.setEnabled(false);
            }else if (v == mBtnStop) {
                domBtnStop();
            }else if (v == mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()]) {
                final SharedPreferences preferences = SgpsActivity.this
                        .getSharedPreferences(SgpsUtils.START_MODE,
                                android.content.Context.MODE_PRIVATE);
                preferences.edit().putInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Hot.ordinal()).commit();
                Log.v(TAG, "mRadioBtnHot Button is pressed");
            } else if (v == mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()]) {
                final SharedPreferences preferences = SgpsActivity.this
                        .getSharedPreferences(SgpsUtils.START_MODE,
                                android.content.Context.MODE_PRIVATE);
                preferences.edit().putInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Cold.ordinal()).commit();
                Log.v(TAG, "mRadioBtnCold Button is pressed");
            } else if (v == mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()]) {
                final SharedPreferences preferences = SgpsActivity.this
                        .getSharedPreferences(SgpsUtils.START_MODE,
                                android.content.Context.MODE_PRIVATE);
                preferences.edit().putInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Warm.ordinal()).commit();
                Log.v(TAG, "mRadioBtnWarm Button is pressed");
            } else if (v == mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()]) {
                final SharedPreferences preferences = SgpsActivity.this
                        .getSharedPreferences(SgpsUtils.START_MODE,
                                android.content.Context.MODE_PRIVATE);
                preferences.edit().putInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Factory.ordinal()).commit();
                Log.v(TAG, "mRadioBtnFull Button is pressed");
            } else if (v == mTvUartLogSwitch) {
                setUartLogCheckBox(mTvUartLogSwitch.isChecked());
            }
            else if(v == mNoiseScanStart){
                if(!mbOpenSGPS){
                    mSgpsUtils.doMakeText(mNoiseScanStart,"Please open the information page SGPS ");
                    return;
                }

                if (! SgpsUtils.mOutputRSSILog.openMyLog(SgpsUtils.constructMylogFilename("Rssilog"))) {
                    Toast.makeText(SgpsActivity.this, R.string.toast_create_file_failed,Toast.LENGTH_SHORT).show();
                    return;
                }
                //start
                mSgpsUtils.mCurrSCanPeriod=mNoiseScanPeriod.getNumText();
                mSgpsUtils.mltFirstData.clear();
                mSgpsUtils.mltSecondData.clear();
                mSgpsUtils.mltThirdData.clear();
                mSgpsUtils.mCurrSCanPeriodCount = 0;
                mSgpsUtils.mCurrScanTimesCount = 0;
                IsStartNoiseScan = true;
                mNoiseScanStop.setEnabled(true);
                mNoiseScanStart.setEnabled(false);
                mSgpsUtils.mCurveChart.removeAllViews();
                mSgpsUtils.mCurveChart.setVisibility(View.GONE);
            }else if(v == mNoiseScanStop) {
                String rst=String.format(getString(R.string.NoiseScan_TestResult_Unknown), "1st");
                IsStartNoiseScan = false;
                SgpsUtils.mOutputRSSILog.closeMyLog();
                mNoiseScanStop.setEnabled(false);
                mNoiseScanStart.setEnabled(true);
                mtvNoiseScanResult[0].setText(rst);
                if(mSgpsUtils.mCurrScanTimes == 2){
                    rst=String.format(getString(R.string.NoiseScan_TestResult_Unknown), "2st");
                    mtvNoiseScanResult[1].setText(rst);
                }else if(mSgpsUtils.mCurrScanTimes == 3){
                    rst=String.format(getString(R.string.NoiseScan_TestResult_Unknown), "2st");
                    mtvNoiseScanResult[1].setText(rst);
                    rst=String.format(getString(R.string.NoiseScan_TestResult_Unknown), "3st");
                    mtvNoiseScanResult[2].setText(rst);
                }
                mSgpsUtils.mCurveChart.setVisibility(View.GONE);

            }else if(v == mBtnCustom) {
                Log.v(TAG, "Mode&System Custom Button is pressed");
                removeDialog(SgpsUtils.DIALOG_CUSTOMER_CMD);
                showDialog(SgpsUtils.DIALOG_CUSTOMER_CMD);
            }
        }
    };
    private void domBtnStop(){
        mbOpenSGPS=false;
        mSgpsUtils.mFirstFix = false;
        mSgpsUtils.mTtffValue = 0;
        mSgpsUtils.mDurationTime =0 ;
        mSgpsUtils.mLocationManager.removeUpdates(mLocListener);
        mHandler.removeMessages(SgpsUtils.HANDLE_COUNTER);
        ((TextView)findViewById(R.id.tv_ttff)).setText("");
        ((TextView)findViewById(R.id.tv_duration_time)).setText("");
        mBtnStart.setEnabled(true);
        mBtnStop.setEnabled(false);
    }
    private final GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            Log.v(TAG, "Enter onGpsStatusChanged function");
            GpsStatus status = mSgpsUtils.mLocationManager.getGpsStatus(null);
            mSgpsUtils.doonGpsStatusChanged(event,status);
            if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                mSgpsUtils.mStatus = getString(R.string.gps_status_first_fix);
                Log.v(TAG, "Enter onFirstFix function: ttff = " + status.getTimeToFirstFix());
                mSgpsUtils.mTtffValue = status.getTimeToFirstFix();
                mSgpsUtils.mFirstFix = true;
                Toast.makeText(SgpsActivity.this,String.format(getString(R.string.toast_first_fix), status.getTimeToFirstFix(),getString(R.string.time_unit_ms)),Toast.LENGTH_SHORT).show();
                TextView tvTtff = findViewById(R.id.tv_ttff);
                tvTtff.setText(mSgpsUtils.mTtffValue + getString(R.string.time_unit_ms));
                tvTtff.setTextColor(Color.GREEN);
                if (mSgpsUtils.getmIsAutoTransferTestRunning()) {
                    mSgpsUtils.mLastTtffValue = status.getTimeToFirstFix();
                }
            } else if(event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                if((!SgpsUtils.ISGe2) &&(!SgpsUtils.ISMarlin3)&&(!SgpsUtils.ISMarlin3lite)) {
                    setSatelliteStatus(status.getSatellites());
                    if (isLocationFixed(status.getSatellites())) {
                        clearInformationLayout();
                        mSgpsUtils.mStatus = getString(R.string.gps_status_unavailable);
                    } else {
                        mSgpsUtils.mStatus = getString(R.string.gps_status_available);
                    }
                }
            }
            TextView tvStatus = findViewById(R.id.tv_status);
            tvStatus.setText(mSgpsUtils.mStatus);
            Log.v(TAG, "onGpsStatusChanged:" + event + " Status:" + mSgpsUtils.mStatus);
        }
    };
    private final OnCheckedChangeListener mAGPSRadioGroupCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group == mAGPSModeRadioGroup) {
                if (checkedId == mAGPSModeRadioGroupItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-MODE", "standalone");
                } else if (checkedId == mAGPSModeRadioGroupItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-MODE", "msb");
                } else if (checkedId == mAGPSModeRadioGroupItemId[2]) {
                    SgpsUtils.setAGPSInfo( "PROPERTY","SUPL-MODE", "msa");
                }
            } else if (group == mSetIDRadioGroup) {
                if (checkedId == mSetIDRadioGroupItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SETID", "IPV4");
                } else if (checkedId == mSetIDRadioGroupItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SETID", "IMSI");
                }
            } else if (group == mAccuracyUnitRadioGroup) {
                if (checkedId == mAccuracyUnitRadioGroupItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "ACCURACY-UNIT", "KVALUE");
                } else if (checkedId == mAccuracyUnitRadioGroupItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "ACCURACY-UNIT", "METER");
                }
            } else if (group == mTriggerTypeRadioGroup) {
                String posmethod = "";
                if (checkedId == mTriggerTypeGroupItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "TRIGGER-TYPE", "PERIODIC");
                    posmethod = SgpsUtils.getAGPSInfo("PROPERTY", "PERIODIC-POSMETHOD");
                    mAreaTypeLayout.setVisibility(View.GONE);
                    mPerodicTypeLayout.setVisibility(View.VISIBLE);
                } else if (checkedId == mTriggerTypeGroupItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY","TRIGGER-TYPE", "AREA");
                    posmethod = SgpsUtils.getAGPSInfo("PROPERTY", "AREA-POSMETHOD");
                    mAreaTypeLayout.setVisibility(View.VISIBLE);
                    mPerodicTypeLayout.setVisibility(View.GONE);
                }
                if(posmethod == null){
                    return;
                }
                List<String> mPosMethodArrayValuesList = Arrays.asList(mSgpsUtils.mPosMethodArrayValues);
                mPosMethodView.setText(Arrays.asList(mSgpsUtils.mPosMethodArray).get(
                        mPosMethodArrayValuesList.indexOf(posmethod)));
            } else if (group == mSignRadioGroup) {
                if (checkedId == mSignRadioGroupItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SIGN", "NORTH");
                } else if (checkedId == mSignRadioGroupItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SIGN", "SOUTH");
                }
            }
        }
    };
    private final OnClickListener mAGPSBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mSLPTemplateButton) {
                removeDialog(SgpsUtils.DIALOG_SLP_TEMPLATE);
                showDialog(SgpsUtils.DIALOG_SLP_TEMPLATE);
            } else if (v == mSLPAddressEditButton) {
                removeDialog(SgpsUtils.DIALOG_SLP_ADDRESS);
                showDialog(SgpsUtils.DIALOG_SLP_ADDRESS);
            } else if (v == mSLPPortEditButton) {
                removeDialog(SgpsUtils.DIALOG_SLP_PORT);
                showDialog(SgpsUtils.DIALOG_SLP_PORT);
            } else if (v == mHorizontalAccuracyEditButton) {
                removeDialog(SgpsUtils.DIALOG_HORIZONTAL_ACCURACY);
                showDialog(SgpsUtils.DIALOG_HORIZONTAL_ACCURACY);
            } else if (v == mVerticalAccuracyEditButton) {
                removeDialog(SgpsUtils.DIALOG_VERTICAL_ACCURACY);
                showDialog(SgpsUtils.DIALOG_VERTICAL_ACCURACY);
            } else if (v == mLocationAgeEditButton) {
                removeDialog(SgpsUtils.DIALOG_LOCATIONAGE);
                showDialog(SgpsUtils.DIALOG_LOCATIONAGE);
            } else if (v == mDelayEditButton) {
                removeDialog(SgpsUtils.DIALOG_DELAY);
                showDialog(SgpsUtils.DIALOG_DELAY);
            } else if (v == mCertificateVerificationEditButton) {
                removeDialog(SgpsUtils.DIALOG_CERTIFICATEVERIFICATION);
                showDialog(SgpsUtils.DIALOG_CERTIFICATEVERIFICATION);
            } else if (v == mMSISDNEditButton) {
                removeDialog(SgpsUtils.DIALOG_MSISDN);
                showDialog(SgpsUtils.DIALOG_MSISDN);
            } else if (v == m3rdMSISDNEditButton) {
                removeDialog(SgpsUtils.DIALOG_3RDMSISDN);
                showDialog(SgpsUtils.DIALOG_3RDMSISDN);
            } else if (v == mTriggerStartButton) {
                SocketUtils.sendCommand("$PSPRD,00,7,1");
            } else if (v == mTriggerAbortButton) {
                SocketUtils.sendCommand("$PSPRD,00,7,2");
            } else if (v == mPosMethodSelectButton) {
                removeDialog(SgpsUtils.DIALOG_POSMETHOD_SELECT);
                showDialog(SgpsUtils.DIALOG_POSMETHOD_SELECT);
            } else if (v == mAreaTypeSelectButton) {
                removeDialog(SgpsUtils.DIALOG_AREA_TYPE_SELECT);
                showDialog(SgpsUtils.DIALOG_AREA_TYPE_SELECT);
            } else if (v == mPerodicMinIntervalEditButton) {
                removeDialog(SgpsUtils.DIALOG_PERODIC_MININTERVAL);
                showDialog(SgpsUtils.DIALOG_PERODIC_MININTERVAL);
            } else if (v == mPerodicStartTimeEditButton) {
                removeDialog(SgpsUtils.DIALOG_PERODIC_STARTTIME);
                showDialog(SgpsUtils.DIALOG_PERODIC_STARTTIME);
            } else if (v == mPerodicStopTimeEditButton) {
                removeDialog(SgpsUtils.DIALOG_PERODIC_STOPTIME);
                showDialog(SgpsUtils.DIALOG_PERODIC_STOPTIME);
            } else if (v == mAreaMinIntervalEditButton) {
                removeDialog(SgpsUtils.DIALOG_AREA_MININTERVAL);
                showDialog(SgpsUtils.DIALOG_AREA_MININTERVAL);
            } else if (v == mMaxNumEditButton) {
                removeDialog(SgpsUtils.DIALOG_MAXNUM);
                showDialog(SgpsUtils.DIALOG_MAXNUM);
            } else if (v == mAreaStartTimeEditButton) {
                removeDialog(SgpsUtils.DIALOG_AREA_STARTTIME);
                showDialog(SgpsUtils.DIALOG_AREA_STARTTIME);
            } else if (v == mAreaStopTimeEditButton) {
                removeDialog(SgpsUtils.DIALOG_AREA_STOPTIME);
                showDialog(SgpsUtils.DIALOG_AREA_STOPTIME);
            } else if (v == mGeoRadiusEditButton) {
                removeDialog(SgpsUtils.DIALOG_GEORADIUS);
                showDialog(SgpsUtils.DIALOG_GEORADIUS);
            } else if (v == mLatitudeEditButton) {
                removeDialog(SgpsUtils.DIALOG_LATITUDE);
                showDialog(SgpsUtils.DIALOG_LATITUDE);
            } else if (v == mLongitudeEditButton) {
                removeDialog(SgpsUtils.DIALOG_LONGITUDE);
                showDialog(SgpsUtils.DIALOG_LONGITUDE);
            } else if (v == mResetToDefaultButton) {
                resetToDefault = true;
                resetToDefault();
                resetToDefault = false;
            } else if (v == mNITestSelectButton) {
                removeDialog(SgpsUtils.DIALOG_NI_DIALOG_TEST);
                showDialog(SgpsUtils.DIALOG_NI_DIALOG_TEST);
            }
        }
    };
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Enter onPause function");
        mSgpsActivityonPause = true;
    }
    private void viewUartLogCheckBox() {
        SharedPreferences preferences = this.getSharedPreferences(SgpsUtils.UART_LOG_SWITCH, android.content.Context.MODE_PRIVATE);
        boolean uart_log_switch = preferences.getBoolean(SgpsUtils.UART_LOG_SWITCH, false);
        mTvUartLogSwitch.setChecked(uart_log_switch);
        setUartLogCheckBox(uart_log_switch);
    }

    private void setUartLogCheckBox(boolean bChecked) {
        SharedPreferences preferences = this.getSharedPreferences(SgpsUtils.UART_LOG_SWITCH, android.content.Context.MODE_PRIVATE);
        Bundle extras = new Bundle();
        extras.putBoolean(bChecked?SgpsUtils.GPS_EXTRA_LOG_SWITCH_ON:SgpsUtils.GPS_EXTRA_LOG_SWITCH_OFF, true);
        extras.putBoolean("time", true);
        preferences.edit().putBoolean(SgpsUtils.UART_LOG_SWITCH,bChecked ).commit();

        if (mSgpsUtils.mLocationManager != null)
            mSgpsUtils.mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data", extras);
    }
    private final NumberPickerView.OnInputNumberListener mSpinnerInputNumberListener=new NumberPickerView.OnInputNumberListener() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {  }
        @Override
        public void afterTextChanged(Editable editable) {
            Log.d(TAG,"mSpinnerInputNumberListener= "+editable.toString());
            mSgpsUtils.mCurrScanTimes = Integer.parseInt(editable.toString().trim());
            if (mSgpsUtils.mCurrScanTimes == 1) {
                TextView tv = findViewById(R.id.color_second);
                tv.setVisibility(View.GONE);
                tv = findViewById(R.id.tv_second);
                tv.setVisibility(View.GONE);
                tv = findViewById(R.id.color_third);
                tv.setVisibility(View.GONE);
                tv = findViewById(R.id.tv_third);
                tv.setVisibility(View.GONE);
            } else if (mSgpsUtils.mCurrScanTimes == 2) {
                TextView tv = findViewById(R.id.color_third);
                tv.setVisibility(View.GONE);
                tv = findViewById(R.id.tv_third);
                tv.setVisibility(View.GONE);
                tv = findViewById(R.id.color_second);
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.tv_second);
                tv.setVisibility(View.VISIBLE);

            }else{
                TextView tv = findViewById(R.id.color_second);
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.tv_second);
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.color_third);
                tv.setVisibility(View.VISIBLE);
                tv = findViewById(R.id.tv_third);
                tv.setVisibility(View.VISIBLE);
            }
        }
    };
    private final AdapterView.OnItemSelectedListener mSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
            if(true_position_Spinner == parent){
                if(position == 0){
                    SharedPreferences preferences = SgpsActivity.this.getSharedPreferences("save.gps",android.content.Context.MODE_PRIVATE);
                    String latitude= preferences.getString("save.gps.latitude", "");
                    String longitude= preferences.getString("save.gps.longitude", "");
                    mEtTestLatitude.setText(latitude != null ? latitude.trim() : "");
                    mEtTestLongitude.setText(longitude != null ? longitude.trim() : "");
                }else{
                    Resources res = getResources();
                    String[] true_position_latitude_values=res.getStringArray(R.array.true_position_Latitude_values);
                    String[] true_position_longitude_values=res.getStringArray(R.array.true_position_Longitude_values);
                    mEtTestLatitude.setText(true_position_latitude_values[position]);
                    mEtTestLongitude.setText(true_position_longitude_values[position]);
                }
                mEtTestLatitude.setSelection(mEtTestLatitude.getText().length());
                mEtTestLongitude.setSelection(mEtTestLongitude.getText().length());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {  }
    };
    /**
     * Component initial
     */
    private void setLayout() {
        showGPSVersion();
        mSatelliteView = findViewById(R.id.sky_view);
        mSatelliteView.setDataProvider(this);
        mSignalView = findViewById(R.id.signal_view);
        mSignalView.setDataProvider(this);
        mTvNmeaLog = findViewById(R.id.tv_nmea_log);
        mTvNMEAHint = findViewById(R.id.tv_nmea_hint);
        mBtnNmeaStart = findViewById(R.id.btn_nmea_start);
        mBtnNmeaStart.setOnClickListener(mBtnClickListener);
        mBtnNMEAStop = findViewById(R.id.btn_nmea_stop);
        mBtnNMEAStop.setOnClickListener(mBtnClickListener);
        mBtnNMEAStop.setEnabled(false);
        mBtnGpsTestSave = findViewById(R.id.btn_gps_test_save);
        mBtnGpsTestSave.setOnClickListener(mBtnClickListener);

        mBtnGpsTestAutoTransferStart = findViewById(R.id.btn_gps_test_start);
        mBtnGpsTestAutoTransferStart.setOnClickListener(mBtnClickListener);
        mBtnGpsTestAutoTransferStop = findViewById(R.id.btn_gps_test_stop);
        mBtnGpsTestAutoTransferStop.setOnClickListener(mBtnClickListener);
        mBtnGpsTestAutoTransferStop.setEnabled(false);
        mEtTestTimes = findViewById(R.id.et_gps_test_times);
        mEtTestTimes.setMaxValue(9999);
        mEtTestTimes.setMinValue(1);
        mEtTestTimes.setNumStep(10);
        mEtTestTimes.setCurrentValue(2);
        mEtTTFFTimeout = findViewById(R.id.gps_ttff_timeout);
        mEtTTFFTimeout.setMaxValue(999);
        mEtTTFFTimeout.setMinValue(1);
        mEtTTFFTimeout.setNumStep(10);
        mEtTTFFTimeout.setCurrentValue(60);
        mEtTTFFInterval = findViewById(R.id.gps_ttff_interval);
        mEtTTFFInterval.setMaxValue(100);
        mEtTTFFInterval.setMinValue(1);
        mEtTTFFInterval.setNumStep(5);
        mEtTTFFInterval.setCurrentValue(10);
        mEtModeInterval = findViewById(R.id.gps_mode_interval);
        mEtModeInterval.setMaxValue(100);
        mEtModeInterval.setMinValue(1);
        mEtModeInterval.setNumStep(5);
        mEtModeInterval.setCurrentValue(5);
        mCurrentTimeHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.tv_current_times_Hot);
        mCurrentTimeHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.tv_current_times_Warm);
        mCurrentTimeHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.tv_current_times_Cold);
        mCurrentTimeHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.tv_current_times_Factory);
        mLastTTFFHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.tv_last_ttff_Hot);
        mLastTTFFHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.tv_last_ttff_Warm);
        mLastTTFFHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.tv_last_ttff_Cold);
        mLastTTFFHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.tv_last_ttff_Factory);

        mAverageHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.tv_mean_ttff_Hot);
        mAverageHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.tv_mean_ttff_Warm);
        mAverageHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.tv_mean_ttff_Cold);
        mAverageHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.tv_mean_ttff_Factory);

        mTvUartLogSwitch = findViewById(R.id.tv_uart_log_switch);
        mTvUartLogSwitch.setOnClickListener(mBtnClickListener);

        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.check_auto_hot);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.check_auto_warm);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.check_auto_cold);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.check_auto_full);

        mSuccessRateHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.success_rate_hot);
        mSuccessRateHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.success_rate_warm);
        mSuccessRateHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.success_rate_cold);
        mSuccessRateHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.success_rate_full);

        mScrollLog = findViewById(R.id.tv_scroll_log);
        mTvResultLog = findViewById(R.id.tv_result_log);
        mTvResultHint = findViewById(R.id.tv_result_hint);
        mEtTestLatitude = findViewById(R.id.et_gps_test_latitude);
        mEtTestLongitude = findViewById(R.id.et_gps_test_longitude);
        // sgps version
        TextView mTvSGPSVersion = findViewById(R.id.tv_sgps_version);
        // SPRD:add ScrollingBar
        mTvSGPSVersion.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTvSGPSVersion.setText(SgpsUtils.SGPS_VRESION);

        mTvResultLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTvResultLog.setScrollbarFadingEnabled(false);

        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()] = findViewById(R.id.radio_hot);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()] = findViewById(R.id.radio_cold);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()] = findViewById(R.id.radio_warm);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()] = findViewById(R.id.radio_full);

        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStop.setEnabled(false);
        SharedPreferences preferences = this.getSharedPreferences(SgpsUtils.START_MODE,android.content.Context.MODE_PRIVATE);
        preferences.edit().putInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Cold.ordinal()).commit();// cold start
        // default.
        int start_mode = preferences.getInt(SgpsUtils.START_MODE, SgpsUtils.GPSModeEnum.Hot.ordinal());
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()].setChecked(start_mode == SgpsUtils.GPSModeEnum.Hot.ordinal());
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()].setChecked(start_mode == SgpsUtils.GPSModeEnum.Cold.ordinal());
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()].setChecked(start_mode == SgpsUtils.GPSModeEnum.Warm.ordinal());
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()].setChecked(start_mode == SgpsUtils.GPSModeEnum.Factory.ordinal());

        mSpreadOrBitSwitch = findViewById(R.id.spreadorbit_switch);
        mRealEPHSwitch = findViewById(R.id.real_eph_switch);
        if ("TRUE".equals(SgpsUtils.getGPSInfo("PROPERTY", "REALEPH-ENABLE"))) {
            mRealEPHSwitch.setChecked(true);
        } else {
            mRealEPHSwitch.setChecked(false);
        }
        mRealEPHSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);

        mGNSSLogSwitch = findViewById(R.id.gnss_log_switch);
        if ("TRUE".equals(SgpsUtils.getGPSInfo("PROPERTY", "LOG-ENABLE"))) {
            mGNSSLogSwitch.setChecked(true);
        } else {
            mGNSSLogSwitch.setChecked(false);
        }
        mGNSSLogSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);

        mGNSSRtdSwitch = findViewById(R.id.gnss_rtd_switch);
        if ("TRUE".equals(SgpsUtils.getGPSInfo("PROPERTY", "RTD-SWITCH"))) {
            mGNSSRtdSwitch.setChecked(true);
        } else {
            mGNSSRtdSwitch.setChecked(false);
        }
        mGNSSRtdSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);

        mGNSSRtkSwitch = findViewById(R.id.gnss_rtk_switch);
        if ("TRUE".equals(SgpsUtils.getGPSInfo( "PROPERTY","RTK-SWITCH"))) {
            mGNSSRtkSwitch.setChecked(true);
        } else {
            mGNSSRtkSwitch.setChecked(false);
        }
        mGNSSRtkSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);
        /* SPRD:add for GE2 @{ */
        /* SPRD:add for GE2 @{ */
        RadioGroup mGemodeRadioGroup = findViewById(R.id.ge_mode_system_list);
        mRadioBtnGps = findViewById(R.id.radio_gps);
        mRadioBtnGlonass = findViewById(R.id.radio_glonass);
        mRadioBtnBDS = findViewById(R.id.radio_bsd);
        mRadioBtnGlonassGps = findViewById(R.id.radio_glogps);
        mRadioBtnGpsBDS = findViewById(R.id.radio_gpsbds);
        /* SPRD:add for marlin3@{ */
        /* @} */
        /* SPRD:add for Marlin3 @{ */
        RadioGroup mMarlin3modeRadioGroup = findViewById(R.id.marlin3_mode_system_list);
        mRadioBtnGpsBd2Glonass = findViewById(R.id.radio_gps_bd2_glonass);
        mRadioBtnGpsBd2Galileo = findViewById(R.id.radio_gps_bd2_galileo);
        mRadioBtnGpsB1cGlonass = findViewById(R.id.radio_gps_b1c_glonass);
        mRadioBtnGpsB1cGalileo= findViewById(R.id.radio_gps_b1c_galileo);
        mRadioBtnGpsGlonassGalileo= findViewById(R.id.radio_gps_glonass_galileo);
        mRadioBtnCustom = findViewById(R.id.radio_custom);
        mModeSystemCustomEdit = findViewById(R.id.custom_mode_system_edit);
        mBtnCustom= findViewById(R.id.mode_custom_btn);

        if(SgpsUtils.ISMarlin3 || SgpsUtils.ISMarlin3lite){
            mMarlin3modeRadioGroup.setVisibility(View.VISIBLE);
            mGemodeRadioGroup.setVisibility(View.GONE);
            mBtnCustom.setVisibility(View.VISIBLE);
            mModeSystemCustomEdit.setVisibility(View.VISIBLE);

        }else{
            mGemodeRadioGroup.setVisibility(View.VISIBLE);
            mMarlin3modeRadioGroup.setVisibility(View.GONE);
            mBtnCustom.setVisibility(View.GONE);
            mModeSystemCustomEdit.setVisibility(View.GONE);
        }
        // add new feature for gps download bin by ansel.li
        TextView mGpsImgMode = findViewById(R.id.gps_img_mode);
        mRadiobtnGpsBeidou = findViewById(R.id.radio_gps_beidou);
        mRadiobtnGpsGlonass = findViewById(R.id.radio_gps_glonass);
        if((!SgpsUtils.ISGe2)){
            mGpsImgMode.setVisibility(View.GONE);
            mRadiobtnGpsBeidou.setVisibility(View.GONE);
            mRadiobtnGpsGlonass.setVisibility(View.GONE);
            mRadioBtnGlonass.setEnabled(false);
            mRadioBtnGlonassGps.setEnabled(false);
            mRadioBtnBDS.setEnabled(false);
            mRadioBtnGpsBDS.setEnabled(false);
        }
        getImgMode();
        mRadiobtnGpsBeidou.setOnClickListener(mBtnGpsImgModeClickListener);
        mRadiobtnGpsGlonass.setOnClickListener(mBtnGpsImgModeClickListener);
        SgpsUtils.GPSGroupEnum gnssMode = mSgpsUtils.getGNSSMode();
        Log.d(TAG, "gnssMode is " + gnssMode);
        mRadioBtnGps.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSONLY);
        mRadioBtnGlonass.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASS);
        mRadioBtnBDS.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_BDSONLY);
        mRadioBtnGlonassGps.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASSGPS);
        mRadioBtnGpsBDS.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBDS);
        mRadioBtnGps.setOnClickListener(mBtnGpsClickListener);
        mRadioBtnGlonass.setOnClickListener(mBtnGpsClickListener);
        mRadioBtnBDS.setOnClickListener(mBtnGpsClickListener);
        mRadioBtnGlonassGps.setOnClickListener(mBtnGpsClickListener);
        mRadioBtnGpsBDS.setOnClickListener(mBtnGpsClickListener);
        /*modify for marlin3 custom cmd*/
        if(SgpsUtils.ISMarlin3 || SgpsUtils.ISMarlin3lite) {
            if (SgpsUtils.ISMarlin3) {
                mRadioBtnGpsBd2Glonass.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBD2GLONASS);
                mRadioBtnGpsBd2Glonass.setOnClickListener(mBtnModeSystemConfigClickListener);
            }else {
                mRadioBtnGpsBd2Glonass.setEnabled(false);
                mRadioBtnGpsBd2Glonass.setChecked(false);
            }
            mRadioBtnGpsBd2Galileo.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBD2Galileo);
            mRadioBtnGpsB1cGlonass.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSB1CGLONASS);
            mRadioBtnGpsB1cGalileo.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSB1CGalileo);
            mRadioBtnGpsGlonassGalileo.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSGLONASSGalileo);
            mBtnCustom.setEnabled(gnssMode == SgpsUtils.GPSGroupEnum.GPSCUSTOM);
            mRadioBtnCustom.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPSCUSTOM);

            mModeSystemCustomEdit.setEnabled(false);
            mRadioBtnGpsBd2Galileo.setOnClickListener(mBtnModeSystemConfigClickListener);
            mRadioBtnGpsB1cGlonass.setOnClickListener(mBtnModeSystemConfigClickListener);
            mRadioBtnGpsB1cGalileo.setOnClickListener(mBtnModeSystemConfigClickListener);
            mRadioBtnGpsGlonassGalileo.setOnClickListener(mBtnModeSystemConfigClickListener);
            mRadioBtnCustom.setOnClickListener(mBtnModeSystemConfigClickListener);
            mBtnCustom.setOnClickListener(mBtnClickListener);
        }

        /* @}*/
        mBtnStart.setOnClickListener(mBtnClickListener);
        mBtnStop.setOnClickListener(mBtnClickListener);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Hot.ordinal()].setOnClickListener(mBtnClickListener);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Cold.ordinal()].setOnClickListener(mBtnClickListener);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Warm.ordinal()].setOnClickListener(mBtnClickListener);
        mRadioBtnHWCF[SgpsUtils.GPSModeEnum.Factory.ordinal()].setOnClickListener(mBtnClickListener);

        mTvNmeaLog.setMovementMethod(new ScrollingMovementMethod());
        mTvNmeaLog.setScrollbarFadingEnabled(true);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Hot.ordinal()].setChecked(true);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Cold.ordinal()].setChecked(true);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Warm.ordinal()].setChecked(true);
        mCircAutoTestHCWF[SgpsUtils.GPSModeEnum.Factory.ordinal()].setChecked(true);
        viewUartLogCheckBox();
        mAverageCn0TextView = findViewById(R.id.average_cn0);
        mAverageCn0TextView.setText("N/A");

        mSingleSatelliteSwitch = findViewById(R.id.single_satellite_switch);
        if ("TRUE".equals(SgpsUtils.getAGPSInfo("PROPERTY", "SINGLE-SATELLITE"))) {
            mSingleSatelliteSwitch.setChecked(true);
        } else {
            mSingleSatelliteSwitch.setChecked(false);
        }
        mSingleSatelliteSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);

        preferences = this.getSharedPreferences(SgpsUtils.FIRST_TIME,
                android.content.Context.MODE_PRIVATE);
        String ss = preferences.getString(SgpsUtils.FIRST_TIME, null);
        if (ss != null) {
            if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
                preferences.edit().putString(SgpsUtils.FIRST_TIME, GpsMnlSetting.PROP_VALUE_2).commit();
            }
        }
        // for noise scan tab
        mNoiseScanStart= findViewById(R.id.noise_btn_start);
        mNoiseScanStart.setOnClickListener(mBtnClickListener);
        mNoiseScanStop= findViewById(R.id.noise_btn_stop);
        mNoiseScanStop.setOnClickListener(mBtnClickListener);
        mNoiseScanStop.setEnabled(false);
        mNoiseScanPeriod= findViewById(R.id.et_PeriodPertestcase);
        mNoiseScanPeriod.setMaxValue(300);
        mNoiseScanPeriod.setMinValue(1);
        mNoiseScanPeriod.setNumStep(10);
        mNoiseScanPeriod.setCurrentValue(12);
        mSpinRepeattimes= findViewById(R.id.spn_Repeattimes);
        mSpinRepeattimes.setOnInputNumberListener(mSpinnerInputNumberListener);
        mSpinRepeattimes.setMaxValue(3);
        mSpinRepeattimes.setMinValue(1);
        mSpinRepeattimes.setCurrentValue(3);

        mSgpsUtils.mCurveChart = findViewById(R.id.customCurveChart);
        mSgpsUtils.mCurveChart.setVisibility(View.GONE);
        mtvNoiseScanResult[0]=findViewById(R.id.tv_first);
        mtvNoiseScanResult[1]=findViewById(R.id.tv_second);
        mtvNoiseScanResult[2]=findViewById(R.id.tv_third);
        //add for true position
        true_position_Spinner = findViewById(R.id.Spinner_true_position);
        true_position_Spinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);
        true_position_Spinner.setSelection(mSgpsUtils.getTruePositionListDefaultIndex());
    }
    /**
     * Clear location information
     */
    private void clearInformationLayout() {
        // clear all information in layout
        ((TextView) findViewById(R.id.tv_date)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_time)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_latitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_longitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_altitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_accuracy)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_bearing)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_speed)).setText(R.string.empty);
        if (mSgpsUtils.mShowFirstFixLocate) {
            ((TextView) findViewById(R.id.first_longtitude_text)).setText(R.string.empty);
            ((TextView) findViewById(R.id.first_latitude_text)).setText(R.string.empty);
        }
    }

    private void controlViewState(boolean bEnabled) {
        mBtnGpsTestAutoTransferStart.setEnabled(bEnabled);
        mBtnGpsTestAutoTransferStart.refreshDrawableState();

        mBtnGpsTestAutoTransferStop.setEnabled(!bEnabled);
        mBtnGpsTestAutoTransferStop.refreshDrawableState();

        mBtnGpsTestSave.setEnabled(bEnabled);

        mEtTestLatitude.setEnabled(bEnabled);
        mEtTestLatitude.setFocusableInTouchMode(bEnabled);
        mEtTestLatitude.refreshDrawableState();

        mEtTestLongitude.setEnabled(bEnabled);
        mEtTestLongitude.setFocusableInTouchMode(bEnabled);
        mEtTestLongitude.refreshDrawableState();

        mEtTestTimes.setEnabled(bEnabled);
        mEtTestTimes.setFocusableInTouchMode(bEnabled);
        mEtTestTimes.refreshDrawableState();

        mEtTTFFTimeout.setEnabled(bEnabled);
        mEtTTFFTimeout.setFocusableInTouchMode(bEnabled);
        mEtTTFFTimeout.refreshDrawableState();

        mEtTTFFInterval.setEnabled(bEnabled);
        mEtTTFFInterval.setFocusableInTouchMode(bEnabled);
        mEtTTFFInterval.refreshDrawableState();

        mEtModeInterval.setEnabled(bEnabled);
        mEtModeInterval.setFocusableInTouchMode(bEnabled);
        mEtModeInterval.refreshDrawableState();

        for (SgpsUtils.GPSModeEnum mode :SgpsUtils.GPSModeEnum.values()) {
            mCircAutoTestHCWF[mode.ordinal()].setEnabled(bEnabled);
            mRadioBtnHWCF[mode.ordinal()].setEnabled(bEnabled);
        }

        mGNSSLogSwitch.setEnabled(bEnabled);
        mSpreadOrBitSwitch.setEnabled(bEnabled);
        mRealEPHSwitch.setEnabled(bEnabled);
        mSingleSatelliteSwitch.setEnabled(bEnabled);
        true_position_Spinner.setEnabled(bEnabled);

        mGNSSRtdSwitch.setEnabled(bEnabled);
        mGNSSRtkSwitch.setEnabled(bEnabled);

        mRadioBtnGpsBd2Galileo.setEnabled(bEnabled);
        mRadioBtnGpsB1cGlonass.setEnabled(bEnabled);
        mRadioBtnGpsB1cGalileo.setEnabled(bEnabled);
        mRadioBtnGpsGlonassGalileo.setEnabled(bEnabled);
        mRadioBtnCustom.setEnabled(bEnabled);

        mBtnStart.setVisibility(bEnabled?View.VISIBLE:View.GONE);
        mBtnStop.setVisibility(bEnabled?View.VISIBLE:View.GONE);
        /* SPRD:add for GE2 @{ */
        mRadioBtnGps.setEnabled(bEnabled);
        mRadioBtnGlonass.setEnabled(bEnabled);
        mRadioBtnBDS.setEnabled(bEnabled);
        mRadioBtnGlonassGps.setEnabled(bEnabled);
        mRadioBtnGpsBDS.setEnabled(bEnabled);
        mRadiobtnGpsBeidou.setEnabled(bEnabled);
        mRadiobtnGpsGlonass.setEnabled(bEnabled);
        if(bEnabled) {
            clearInformationLayout();
            getImgMode();
        }
    }
    private void startGPSAutoTransferTest() {
        for (SgpsUtils.GPSModeEnum mode :SgpsUtils.GPSModeEnum.values()) {
            mSuccessRateHWCF[mode.ordinal()].setText(null);
        }

        mSgpsUtils.mAutoTransferTotalTimes = mEtTestTimes.getNumText();
        Log.d(TAG, "mAutoTransferTotalTimes->" + mSgpsUtils.mAutoTransferTotalTimes);
        mSgpsUtils.mTimeoutValue = mEtTTFFTimeout.getNumText();
        Log.d(TAG, "mTimeoutValue->" + mSgpsUtils.mTimeoutValue);
        mSgpsUtils.mTTFFInterval = mEtTTFFInterval.getNumText();
        mSgpsUtils.mModeInterval = mEtModeInterval.getNumText();

        // get latitude and longitude
        SharedPreferences preferences = this.getSharedPreferences("save.gps",android.content.Context.MODE_PRIVATE);
        mSgpsUtils.mTestLatitude = Double.valueOf(Objects.requireNonNull(preferences.getString("save.gps.latitude", "360")));
        mSgpsUtils.mTestLongitude = Double.valueOf(Objects.requireNonNull(preferences.getString("save.gps.longitude", "360")));

        if (null == mSgpsUtils.mLastLocationRefence)
            mSgpsUtils.mLastLocationRefence = new Location("refence");
        mSgpsUtils.mLastLocationRefence.setLatitude(mSgpsUtils.mTestLatitude);
        mSgpsUtils.mLastLocationRefence.setLongitude(mSgpsUtils.mTestLongitude);

        // start test now,start been pressed more times
        if (!mSgpsUtils.getmIsAutoTransferTestRunning()) {
            if(mbOpenSGPS){
                domBtnStop();
            }
            mBtnGpsTestAutoTransferStop.setEnabled(true);
            clearGpsCircleTestLayout();
            controlViewState(true);
            AutoCircleTestThread mAutoCircleTestThread = new AutoCircleTestThread();
            mSgpsUtils.setmIsAutoTransferTestRunning(true);
            mAutoCircleTestThread.start();
            Log.w(TAG, "new AutoCircleTestThread start!");

        } else {
            Log.w(TAG, "GpsAutoTransfer start button has been pushed.");
            mBtnGpsTestAutoTransferStart.refreshDrawableState();
            mBtnGpsTestAutoTransferStart.setEnabled(false);
        }
    }
    private void stopAutoTransferGPSAutoTest() {
        mSgpsUtils.mAutoTransferTotalTimes = 0;
        mSgpsUtils.mCurrentTimes = 0;
        mSgpsUtils.setmIsAutoTransferTestRunning(false);
        mHandler.removeMessages(SgpsUtils.HANDLE_COUNTER);
    }
    private void clearGpsCircleTestLayout() {
        for (SgpsUtils.GPSModeEnum mode :SgpsUtils.GPSModeEnum.values()) {
            mCurrentTimeHWCF[mode.ordinal()].setText(R.string.empty);
            mLastTTFFHWCF[mode.ordinal()].setText(R.string.empty);
            mAverageHWCF[mode.ordinal()].setText(R.string.empty);
        }
    }

    private void sendModeAndTimes(int nmode ,int ntime,String status) {
        Message msg = mHandler.obtainMessage(SgpsUtils.HANDLE_AUTO_TRANSFER_UPDATE_CURRENT_MODE);
        msg.arg1 = nmode;
        msg.arg2 = ntime;
        msg.obj=status;
        mHandler.sendMessage(msg);
    }

    private void setAutoTransferStartButtonEnable(boolean bEnable) {
        Message msg = mHandler.obtainMessage(SgpsUtils.HANDLE_AUTO_TRANSFER_START_BUTTON_UPDATE);
        msg.arg1 = bEnable ? 1 : 0;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Enter onCreate  function of Main Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_tabs);
        mSgpsUtils = new  SgpsUtils(this);
        setTitle("SGPS -> "+getString(R.string.agps_title));
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .build());
        TabHost tabHost = getTabHost();
        // config tab0
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.agps_title));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.agps_title))
                .setIndicator(view)
                .setContent(R.id.layout_agps));

        // tab1
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.satellites));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.satellites))
                .setIndicator(view)
                .setContent(R.id.layout_satellites));

        // tab2
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.information));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.information))
                .setIndicator(view)
                .setContent(R.id.layout_info));

        // tab3
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.nmea_log));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.nmea_log))
                .setIndicator(view)
                .setContent(R.id.layout_nmea));

        /* Spresdst: add a tab. @{ */
        // tab4
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.gps_circle_test));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.gps_circle_test))
                .setIndicator(view)
                .setContent(R.id.layout_auto_circle_test));
        /* @} */

        // tab5
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.gps_test));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.gps_test))
                .setIndicator(view)
                .setContent(R.id.layout_auto_test));

        // tab6
        view = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_tabwighet, null);
        ((TextView) view.findViewById(R.id.tv_title)).setText(this.getString(R.string.gps_noisescan));
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.gps_noisescan))
                .setIndicator(view)
                .setContent(R.id.layout_noise_scan));

        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                SgpsActivity.this.setTitle("SGPS -> "+tabId);
                Log.v(TAG, "Select: " + tabId );
                mCurrentTabTag=tabId;
                setMenuItemVisible(hNMEAtabMenu,R.id.btn_assert_text,tabId.equals(getString(R.string.nmea_log)));

                if (tabId.equals(getString(R.string.information))&& !mSgpsUtils.getmIsAutoTransferTestRunning()) {
                    getImgMode();
                }
            }
        });
        setLayout();
        initGpsConfigLayout();
        gpsBuilder = new AlertDialog.Builder(SgpsActivity.this);

        mSgpsUtils.acquireScreenWakeLock();
        try {
            mSgpsUtils.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mSgpsUtils.mLocationManager.addGpsStatusListener(mGpsListener);
            mSgpsUtils.mLocationManager.addNmeaListener(mNmeaListener);
            if (mSgpsUtils.mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mSgpsUtils.mProvider = String.format(getString(
                        R.string.provider_status_enabled),
                        LocationManager.GPS_PROVIDER);
            } else {
                mSgpsUtils.mProvider = String.format(getString(
                        R.string.provider_status_disabled),
                        LocationManager.GPS_PROVIDER);
            }
            mSgpsUtils.mStatus = getString(R.string.gps_status_unknown);

        } catch (SecurityException e) {
            Toast.makeText(this, "security exception", Toast.LENGTH_LONG)
                    .show();
            Log.w(TAG, "Exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }

        final SharedPreferences preferences = this.getSharedPreferences("RunInBG", android.content.Context.MODE_PRIVATE);
        mIsRunInBg = preferences.getBoolean("RunInBG", true);
        mSgpsUtils.mShowFirstFixLocate = true;
        //mSocketClient = new ClientSocket(this);
        TextView tvTtff = findViewById(R.id.tv_ttff);
        tvTtff.setText(0 + getString(R.string.time_unit_ms));
        TextView tvDurationtime = findViewById(R.id.tv_duration_time);
        tvDurationtime.setText(0+ getString(R.string.time_unit_ms));
        // blink issue
        if (SgpsUtils.ISGe2||SgpsUtils.ISMarlin3 || SgpsUtils.ISMarlin3lite) {
            nmeaParser = new NmeaParser(SgpsActivity.this);
            nmeaParser.setLocalNmeaListener(SgpsActivity.this);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean supRetVal = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.sgps, menu);
        hNMEAtabMenu=menu;
        setMenuItemVisible(hNMEAtabMenu,R.id.btn_assert_text,false);
        setMenuItemVisible(hNMEAtabMenu,R.id.btn_stop_batching,false);
        setMenuItemVisible(hNMEAtabMenu,R.id.btn_start_batching,false);
        return supRetVal;
    }

    private void setMenuItemVisible(Menu menu,int itemID,boolean isVisible) {
        MenuItem triggerassertItem=menu.findItem(itemID);
        triggerassertItem.setVisible(isVisible);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_assert_text://Bug 899123
                if ((!SgpsUtils.ISGe2) &&(!SgpsUtils.ISMarlin3) &&(!SgpsUtils.ISMarlin3lite)){
                    Toast.makeText(SgpsActivity.this, R.string.feature_not_support,
                            Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    SocketUtils.sendCommand("$PSPRD,00,5,1");
                }
                return true;
            case R.id.btn_start_batching:
                if(mSgpsUtils.mLocationManager.registerGnssBatchedLocationCallback(1000,true,mBatchedLocationCallback,null))
                {
                    setMenuItemVisible(hNMEAtabMenu,R.id.btn_stop_batching,true);
                    setMenuItemVisible(hNMEAtabMenu,R.id.btn_start_batching,false);
                }
                return true;
            case R.id.btn_stop_batching:
                if(mSgpsUtils.mLocationManager.unregisterGnssBatchedLocationCallback(mBatchedLocationCallback))
                {
                    setMenuItemVisible(hNMEAtabMenu,R.id.btn_stop_batching,false);
                    setMenuItemVisible(hNMEAtabMenu,R.id.btn_start_batching,true);
                }
                return true;
            default:
                break;
        }
        return false;
    }
    private final BatchedLocationCallback mBatchedLocationCallback = new BatchedLocationCallback() {
        /**
         * Called when a new batch of locations is ready
         * @param locations A list of all new locations (possibly zero of them.)
         */
        public void onLocationBatch(List<Location> locations) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Enter onResume function");
        startGPS(false);
        TextView tvProvider = findViewById(R.id.tv_provider);
        tvProvider.setText(mSgpsUtils.mProvider);
        TextView tvStatus = findViewById(R.id.tv_status);
        tvStatus.setText(mSgpsUtils.mStatus);
        //add new feature for gps download bin by ansel.li
        getImgMode();
        SgpsUtils.GPSGroupEnum gnssMode = mSgpsUtils.getGNSSMode();
        Log.d(TAG, "gnssMode is " + gnssMode);
        mRadioBtnGps.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSONLY);
        mRadioBtnGlonass.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASS);
        mRadioBtnBDS.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_BDSONLY);
        mRadioBtnGlonassGps.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GLONASSGPS);
        mRadioBtnGpsBDS.setChecked(gnssMode == SgpsUtils.GPSGroupEnum.GPS_EXTRA_GPSBDS);
        /* @}*/
        if (mSpreadOrBitSwitch != null) {
            mSpreadOrBitSwitch.setOnCheckedChangeListener(null);
            String value = SgpsUtils.getGPSInfo("PROPERTY", "SPREADORBIT-ENABLE");
            Log.d(TAG, "onResume set mSpreadOrBitSwitch state SPREADORBIT-ENABLE is " + value);
            if ("TRUE".equals(value)) {
                mSpreadOrBitSwitch.setChecked(true);
            } else {
                mSpreadOrBitSwitch.setChecked(false);
            }
            mSpreadOrBitSwitch.setOnCheckedChangeListener(mSwitchCheckedChangeListener);
        }
    }
    private void showGPSVersion() {
        if(mSgpsActivityonPause){
            return;
        }
        Log.d(TAG, "Enter show GPS version");
        TextView tvVersion = findViewById(R.id.tv_gps_version);
        tvVersion.setMovementMethod(ScrollingMovementMethod.getInstance());
        String sVersion = SgpsUtils.getGPSInfo("PROPERTY", "GE2-VERSION");
        Log.d(TAG, "GPS Version ->" + sVersion);
        tvVersion.setText(sVersion);

        tvVersion = findViewById(R.id.tv_agnss_version);
        tvVersion.setMovementMethod(ScrollingMovementMethod.getInstance());
        sVersion = SgpsUtils.getAGPSInfo("PROPERTY", "AGPS-VERSION");
        Log.d(TAG, "AGPS Version ->" + sVersion);
        tvVersion.setText(sVersion);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): mbRunInBG " + mIsRunInBg);
        if (!mIsRunInBg) {
            mSgpsUtils.mLocationManager.removeUpdates(mLocListener);
            Log.v(TAG, "removeUpdates(mLocListener)");
            mSgpsUtils.mLocationManager.removeGpsStatusListener(mGpsListener);
        }
    }

    private void startGPS(boolean bForceReStart) {
        Log.d(TAG, "GpsStatus->" + mSgpsUtils.isGpsOpen());
        if(!bForceReStart&&mSgpsUtils.isGpsOpen()){
            showGPSVersion();
        }else{
            gpsBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent gpsIntent = new Intent();
                    gpsIntent.setClassName("com.android.settings",
                            "com.android.settings.Settings$LocationSettingsActivity");
                    startActivity(gpsIntent);
                }
            });
            Resources res = getResources();
            AlertDialog alertDialog = gpsBuilder.create();
            alertDialog.setTitle(res.getString(R.string.start_gps_dialog_title));
            if(bForceReStart){
                Settings.Secure.setLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER, false);
                alertDialog.setMessage(res.getString(R.string.restart_gps_dialog_message));
            }else{
                alertDialog.setMessage( res.getString(R.string.start_gps_dialog_message));
            }
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onRestart() {
        Log.d(TAG, "Enter onRestart function");
        if (!mIsRunInBg) {
            mSgpsUtils.mFirstFix = false;
            if (mSgpsUtils.mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mSgpsUtils.mProvider = String.format(getString(
                        R.string.provider_status_enabled),
                        LocationManager.GPS_PROVIDER);
            } else {
                mSgpsUtils.mProvider = String.format(getString(
                        R.string.provider_status_disabled),
                        LocationManager.GPS_PROVIDER);
            }
            mSgpsUtils.mStatus = getString(R.string.gps_status_unknown);
            mSgpsUtils.mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
            Log.v(TAG, "request(mLocListener)");
            mSgpsUtils.mLocationManager.addGpsStatusListener(mGpsListener);
        }
        mSgpsUtils.acquireScreenWakeLock();
        mSgpsActivityonPause = false;
        super.onRestart();
    }
    private void getImgMode() {
        Log.d(TAG, "GPS-IMG-MODE is " + SgpsUtils.getGPSInfo( "PROPERTY","GPS-IMG-MODE"));
        if (SgpsUtils.GNSSBDMODEM.equals(SgpsUtils.getGPSInfo("PROPERTY", "GPS-IMG-MODE"))) {
            mRadioBtnBDS.setEnabled(true);
            mRadioBtnGpsBDS.setEnabled(true);
            mRadiobtnGpsBeidou.setChecked(true);
            mRadioBtnGlonass.setEnabled(false);
            mRadioBtnGlonassGps.setEnabled(false);
        } else if (SgpsUtils.GNSSMODEM.equals(SgpsUtils.getGPSInfo("PROPERTY", "GPS-IMG-MODE"))) {
            mRadioBtnGlonass.setEnabled(true);
            mRadioBtnGlonassGps.setEnabled(true);
            mRadiobtnGpsGlonass.setChecked(true);
            mRadioBtnBDS.setEnabled(false);
            mRadioBtnGpsBDS.setEnabled(false);
        } else {
            if (SgpsUtils.getGPSInfo("PROPERTY", "GE2-VERSION").contains("GPS_GLO")) {
                Log.d(TAG, "Running for GPS_GLO that is not native");
                mRadioBtnGlonass.setEnabled(true);
                mRadioBtnGlonassGps.setEnabled(true);
                mRadiobtnGpsGlonass.setChecked(true);
                mRadioBtnBDS.setEnabled(false);
                mRadioBtnGpsBDS.setEnabled(false);
            } else if (SgpsUtils.getGPSInfo("PROPERTY", "GE2-VERSION").contains("GPS_BDS")) {
                Log.d(TAG, "Running for GPS_BDS that is not native");
                mRadioBtnBDS.setEnabled(true);
                mRadioBtnGpsBDS.setEnabled(true);
                mRadiobtnGpsBeidou.setChecked(true);
                mRadioBtnGlonass.setEnabled(false);
                mRadioBtnGlonassGps.setEnabled(false);
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        Log.d(TAG, "enter onDestroy function");
        mSgpsUtils.mLocationManager.removeUpdates(mLocListener);
        Log.v(TAG, "removeUpdates(mLocListener)");
        mSgpsUtils.mLocationManager.removeGpsStatusListener(mGpsListener);
        mSgpsUtils.mLocationManager.removeNmeaListener(mNmeaListener);
        mHandler.removeMessages(SgpsUtils.HANDLE_COUNTER);
        mSgpsUtils.setmIsAutoTransferTestRunning(false);
        mStartNmeaRecord = false;
        SgpsUtils.mOutputNMEALog.closeMyLog();
        mBtnNMEAStop.setEnabled(false);
        mBtnNmeaStart.setEnabled(true);
        mTvNMEAHint.setText(R.string.empty);
        mTvNmeaLog.setText(R.string.empty);
        mSgpsUtils.release();
        //mSocketClient.endClient();
        final SharedPreferences preferences = this.getSharedPreferences(
                SgpsUtils.FIRST_TIME, android.content.Context.MODE_PRIVATE);
        String ss = preferences.getString(SgpsUtils.FIRST_TIME, null);
        if (ss != null && ss.equals(GpsMnlSetting.PROP_VALUE_2)) {
            GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_TEST_MODE,GpsMnlSetting.PROP_VALUE_0);
        }
        super.onDestroy();
    }

    private void resetParam(Bundle extras,int delay) {
        Log.d(TAG, "Enter resetParam function extras = " + extras);
        mSgpsUtils.mFirstFix = false;
        mSgpsUtils.mTtffValue = 0;
        mSgpsUtils.mDurationTime =0 ;   //clear duration time
        try {
            mSgpsUtils.mLocationManager.removeUpdates(mLocListener);
            if (extras != null) {
                Thread.sleep(delay);
                mSgpsUtils.mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data", extras);
                mHandler.sendEmptyMessage(SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_PROVIDER);
                //Unisoc: Bug 1441939 ttff is increase when test stop
                if (!mHandler.hasMessages(SgpsUtils.HANDLE_COUNTER)) {
                    mHandler.sendEmptyMessage(SgpsUtils.HANDLE_COUNTER);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "resetParam Exception: " + e.getMessage());
        }
    }

    private void initGpsConfigLayout() {
        // ControlPlane and User Plane switch
        RadioGroup mRGPlaneSwitch = findViewById(R.id.rg_plane_switch);
        final View mAgpsControlPlane = findViewById(R.id.layout_agps_control_plane);
        final View mAgpsUserPlane = findViewById(R.id.layout_agps_user_plane);
        String control_mode = SgpsUtils.getAGPSInfo( "PROPERTY","CONTROL-PLANE");
        Log.d(TAG, "control_mode"+control_mode);
        int mRGPlaneSwitchSize = mRGPlaneSwitch.getChildCount();
        mRGPlaneSwitchItemId = new int[mRGPlaneSwitchSize];
        for (int i = 0; i < mRGPlaneSwitchSize; i++) {
            mRGPlaneSwitchItemId[i] = mRGPlaneSwitch.getChildAt(i).getId();
        }
        initAGpsControlPlane(mAgpsControlPlane);
        initAGpsUserPlane(mAgpsUserPlane);
        initAGpsCommon();
        initAGPSConfigLayoutStatus();
        if ("TRUE".equals(control_mode)) {
            Log.d(TAG, "Control Pannel");
            mRGPlaneSwitch.check(mRGPlaneSwitchItemId[0]);
            mAgpsControlPlane.setVisibility(View.VISIBLE);
            mAgpsUserPlane.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "User Pannel");
            mRGPlaneSwitch.check(mRGPlaneSwitchItemId[1]);
            mAgpsControlPlane.setVisibility(View.GONE);
            mAgpsUserPlane.setVisibility(View.VISIBLE);
        }
        mRGPlaneSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mRGPlaneSwitchItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "CONTROL-PLANE", "TRUE");
                    mAgpsControlPlane.setVisibility(View.VISIBLE);
                    mAgpsUserPlane.setVisibility(View.GONE);
                } else if (checkedId == mRGPlaneSwitchItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "CONTROL-PLANE", "FALSE");
                    mAgpsControlPlane.setVisibility(View.GONE);
                    mAgpsUserPlane.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initAGpsControlPlane(View view) {
        // MOLR position method
        mRGMolrPositionMethod = view.findViewById(R.id.rg_molr_position_method);
        String molr_position_method = SgpsUtils.getAGPSInfo("PROPERTY", "MPM");
        int mRGMolrPositionMethodSize = mRGMolrPositionMethod.getChildCount();
        mRGMolrPositionMethodItemId = new int[mRGMolrPositionMethodSize];
        for (int i = 0; i < mRGMolrPositionMethodSize; i++) {
            mRGMolrPositionMethodItemId[i] = mRGMolrPositionMethod.getChildAt(i).getId();
        }
        if ("LOCATION".equals(molr_position_method)) {
            mRGMolrPositionMethod.check(mRGMolrPositionMethodItemId[0]);
        } else {
            mRGMolrPositionMethod.check(mRGMolrPositionMethodItemId[1]);
        }

        mRGMolrPositionMethod.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "plane_switch_rg checkedId is " + checkedId);
                if (checkedId == mRGMolrPositionMethodItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "MPM", "LOCATION");
                } else if (checkedId == mRGMolrPositionMethodItemId[1]) {
                    SgpsUtils.setAGPSInfo( "PROPERTY","MPM", "ASSISTANCE");
                }

            }
        });

        // External Address
        mExternalAddressCheckBox = view.findViewById(R.id.cb_external_address);
        mExternalAddressEditText = view.findViewById(R.id.et_external_address);
        mExternalAddressSaveButton = view.findViewById(R.id.bt_external_address_save);
        String mCheckBoxChecked = SgpsUtils.getAGPSInfo("PROPERTY", "ExtAddr Enable");
        if ("TRUE".equals(mCheckBoxChecked)) {
            mExternalAddressCheckBox.setChecked(true);
            mExternalAddressEditText.setEnabled(true);
            mExternalAddressSaveButton.setEnabled(true);
        } else {
            mExternalAddressCheckBox.setChecked(false);
            mExternalAddressEditText.setEnabled(false);
            mExternalAddressSaveButton.setEnabled(false);
        }
        mExternalAddressCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mExternalAddressEditText.setEnabled(isChecked);
                        mExternalAddressSaveButton.setEnabled(isChecked);
                        SgpsUtils.setAGPSInfo("PROPERTY", "ExtAddr Enable",isChecked ? "TRUE" : "FALSE");
                    }
                });
        mExternalAddressSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SgpsUtils.setAGPSInfo("PROPERTY", "EXTERNAL-ADDRESS",
                        mExternalAddressEditText.getText().toString().trim());
            }
        });
        mExternalAddressEditText.setText(SgpsUtils.getAGPSInfo("PROPERTY","EXTERNAL-ADDRESS"));
        Spannable eatext = mExternalAddressEditText.getText();
        if (eatext != null) {
            Selection.setSelection(eatext, eatext.length());
        }
        // MLC number
        mMLCNumberCheckBox = view.findViewById(R.id.cb_mlc_number);
        mMLCNumberEditText = view.findViewById(R.id.et_mlc_number);
        mMLCNumberSaveButton = view.findViewById(R.id.bt_mlc_number_save);

        mCheckBoxChecked = SgpsUtils.getAGPSInfo( "PROPERTY","MlcNum Enable");
        if ("TRUE".equals(mCheckBoxChecked)) {
            mMLCNumberCheckBox.setChecked(true);
            mMLCNumberEditText.setEnabled(true);
            mMLCNumberSaveButton.setEnabled(true);
        } else {
            mMLCNumberCheckBox.setChecked(false);
            mMLCNumberEditText.setEnabled(false);
            mMLCNumberSaveButton.setEnabled(false);
        }
        mMLCNumberCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMLCNumberEditText.setEnabled(isChecked);
                mMLCNumberSaveButton.setEnabled(isChecked);
                SgpsUtils.setAGPSInfo( "PROPERTY","MlcNum Enable",isChecked ? "TRUE" : "FALSE");

            }
        });
        mMLCNumberSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SgpsUtils.setAGPSInfo("PROPERTY", "MLC-NUMBER", mMLCNumberEditText.getText().toString().trim());
            }
        });
        mMLCNumberEditText.setText(SgpsUtils.getAGPSInfo("PROPERTY", "MLC-NUMBER"));
        Spannable mntext = mMLCNumberEditText.getText();
        if (mntext != null) {
            Selection.setSelection(mntext, mntext.length());
        }
        // CP auto reset
        mAutoResetCheckBox = view.findViewById(R.id.auto_reset);
        String IsAutoReset = SgpsUtils.getAGPSInfo("PROPERTY", "CP-AUTORESET");
        if ("TRUE".equals(IsAutoReset)) {
            mAutoResetCheckBox.setChecked(true);
        } else {
            mAutoResetCheckBox.setChecked(false);
        }
        mAutoResetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SgpsUtils.setAGPSInfo( "PROPERTY","CP-AUTORESET", isChecked?"TRUE":"FALSE");
            }
        });
        // SIM used
        mRGSimSelection = view.findViewById(R.id.rg_sim_selection);
        String sim = SgpsUtils.getAGPSInfo("PROPERTY", "SIM-PREFER");
        int mRGSimSelectionSize = mRGSimSelection.getChildCount();
        mRGSimSelectionItemId = new int[mRGSimSelectionSize];
        for (int i = 0; i < mRGSimSelectionSize; i++) {
            mRGSimSelectionItemId[i] = mRGSimSelection.getChildAt(i).getId();
        }
        if ("SIM1".equals(sim)) {
            mRGSimSelection.check(mRGSimSelectionItemId[0]);
        } else if ("SIM2".equals(sim)) {
            mRGSimSelection.check(mRGSimSelectionItemId[1]);
        }
        mRGSimSelection.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "plane_switch_rg checkedId is " + checkedId);
                if (checkedId == mRGSimSelectionItemId[0]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SIM-PREFER", "SIM1");
                } else if (checkedId == mRGSimSelectionItemId[1]) {
                    SgpsUtils.setAGPSInfo("PROPERTY", "SIM-PREFER", "SIM2");
                }

            }
        });
        mMOLASwitch = view.findViewById(R.id.mola_trigger);
        mMOLASwitch.setChecked(false);
        mMOLASwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged " + isChecked + ", resetToDefault " + resetToDefault);
                if (!resetToDefault) {
                    mSgpsUtils.setMOLATrigger(isChecked);
                }
            }
        });
    }

    private int[] getRadioGroupItemId(RadioGroup item) {
        int mItemRadioGroupSize = item.getChildCount();
        int[] mRadioGroupItemId = new int[mItemRadioGroupSize];
        for (int i = 0; i < mItemRadioGroupSize; i++) {
            mRadioGroupItemId[i] = item.getChildAt(i).getId();
        }
        return mRadioGroupItemId;
    }

    private void initAGpsUserPlane(View view) {
        // A-GPS Mode
        mAGPSModeRadioGroup = findViewById(R.id.rg_agps_mode);
        mAGPSModeRadioGroupItemId = getRadioGroupItemId(mAGPSModeRadioGroup);
        mAGPSModeRadioGroup.setOnCheckedChangeListener(mAGPSRadioGroupCheckedChangeListener);

        mSgpsUtils.mSLPArray = this.getResources().getStringArray(R.array.slp_array);
        for (int i = 0; i < mSgpsUtils.mSLPArray.length; i++) {
            int index = mSgpsUtils.mSLPArray[i].lastIndexOf(":");
            String name = mSgpsUtils.mSLPArray[i].substring(index + 1);
            mSgpsUtils.mSLPNameList.add(name);
            String values = mSgpsUtils.mSLPArray[i].substring(0, index);
            mSgpsUtils.mSLPValueList.add(values);
        }
        mSgpsUtils.mPosMethodArray = this.getResources().getStringArray(R.array.posmethod_array);
        mSgpsUtils.mPosMethodArrayValues = this.getResources().getStringArray(R.array.posmethod_array_values);
        mSgpsUtils.mAreaTypeArray = this.getResources().getStringArray(R.array.area_type_array);
        mSgpsUtils.mAreaTypeArrayValues = this.getResources().getStringArray(R.array.area_type_array_values);
        mSgpsUtils.mNiDialogTestArrayValues = this.getResources().getStringArray(
                R.array.ni_dialog_test_array_values);

        mSLPTemplateButton = findViewById(R.id.bt_slp_template);
        mSLPTemplateButton.setOnClickListener(mAGPSBtnClickListener);

        mSLPAddressTextView = findViewById(R.id.slp_address_text);
        mSLPAddressEditButton = findViewById(R.id.slp_address_edit);
        mSLPAddressEditButton.setOnClickListener(mAGPSBtnClickListener);

        mSLPPortTextView = findViewById(R.id.slp_port_text);
        mSLPPortEditButton = findViewById(R.id.slp_port_edit);
        mSLPPortEditButton.setOnClickListener(mAGPSBtnClickListener);

        mTLSEnableCheckBox = view.findViewById(R.id.tls_enable_checkbox);
        mSgpsUtils.setCheckBoxListener(mTLSEnableCheckBox, "TLS-ENABLE");

        mSetIDRadioGroup = findViewById(R.id.rg_set_id);
        mSetIDRadioGroupItemId = getRadioGroupItemId(mSetIDRadioGroup);
        mSetIDRadioGroup.setOnCheckedChangeListener(mAGPSRadioGroupCheckedChangeListener);

        mAccuracyUnitRadioGroup = findViewById(R.id.rg_accuracy_unit);
        mAccuracyUnitRadioGroupItemId = getRadioGroupItemId(mAccuracyUnitRadioGroup);
        mAccuracyUnitRadioGroup.setOnCheckedChangeListener(mAGPSRadioGroupCheckedChangeListener);

        mHorizontalAccuracyTextView = findViewById(R.id.horizontal_accuracy_text);
        mHorizontalAccuracyEditButton = findViewById(R.id.horizontal_accuracy_edit);
        mHorizontalAccuracyEditButton.setOnClickListener(mAGPSBtnClickListener);

        mVerticalAccuracyTextView = findViewById(R.id.vertical_accuracy_text);
        mVerticalAccuracyEditButton = findViewById(R.id.vertical_accuracy_edit);
        mVerticalAccuracyEditButton.setOnClickListener(mAGPSBtnClickListener);

        mLocationAgeTextView = findViewById(R.id.location_age_text);
        mLocationAgeEditButton = findViewById(R.id.location_age_edit);
        mLocationAgeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mDelayTextView = findViewById(R.id.delay_text);
        mDelayEditButton = findViewById(R.id.delay_edit);
        mDelayEditButton.setOnClickListener(mAGPSBtnClickListener);

        mCertificateVerificationCheckBox = view
                .findViewById(R.id.certificate_verification_checkbox);
        mCertificateVerificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SgpsUtils.setAGPSInfo( "PROPERTY","CER-VERIFY",(isChecked ? "TRUE" : "FALSE"));
                mCertificateVerificationEditButton.setEnabled(isChecked);
            }
        });

        mCertificateVerificationEditButton = findViewById(R.id.certificate_verification_edit);
        mCertificateVerificationEditButton.setOnClickListener(mAGPSBtnClickListener);

        mEnableLabIOTTestCheckBox = view.findViewById(R.id.enable_lab_iot_test_checkbox);
        mSgpsUtils.setCheckBoxListener(mEnableLabIOTTestCheckBox, "LAB-IOT");

        mEnableeCIDCheckBox = view.findViewById(R.id.enable_ecid_checkbox);
        mSgpsUtils.setCheckBoxListener(mEnableeCIDCheckBox, "ECID-ENABLE");
        // agps supl2
        mAGPSSUPL2Layout = view.findViewById(R.id.layout_supl2);
        mEnableSUPL2CheckBox = view.findViewById(R.id.supl2_enable_checkbox);
        mEnableSUPL2CheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mAGPSSUPL2Layout.setVisibility(View.VISIBLE);
                        } else {
                            mAGPSSUPL2Layout.setVisibility(View.GONE);
                        }
                        SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-VERSION",isChecked ? "v2.0.0" : "v1.0.0");
                    }
                });

        if (mSgpsUtils.showSUPL2View(SgpsUtils.getAGPSInfo("PROPERTY", "SUPL-VERSION"))) {
            mAGPSSUPL2Layout.setVisibility(View.VISIBLE);
            mEnableSUPL2CheckBox.setChecked(true);
        } else {
            mAGPSSUPL2Layout.setVisibility(View.GONE);
            mEnableSUPL2CheckBox.setChecked(false);
        }

        mMSISDNTextView = findViewById(R.id.msisdn_text);
        mMSISDNEditButton = findViewById(R.id.msisdn_edit);
        mMSISDNEditButton.setOnClickListener(mAGPSBtnClickListener);

        mEnable3rdMSISDNCheckBox = view.findViewById(R.id.enable_3rd_msisdn_checkbox);
        m3rdMSISDNTextView = findViewById(R.id.msisdn_3rd_text);
        m3rdMSISDNEditButton = findViewById(R.id.msisdn_3rd_edit);
        m3rdMSISDNEditButton.setOnClickListener(mAGPSBtnClickListener);

        mEnable3rdMSISDNCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SgpsUtils.setAGPSInfo("PROPERTY", "",(isChecked ? "TRUE" : "FALSE"));
                        m3rdMSISDNEditButton.setEnabled(isChecked);
                    }
                });

        mTriggerStartButton = findViewById(R.id.trigger_start);
        mTriggerStartButton.setOnClickListener(mAGPSBtnClickListener);
        mTriggerAbortButton = findViewById(R.id.trigger_abort);
        mTriggerAbortButton.setOnClickListener(mAGPSBtnClickListener);

        mAreaTypeLayout = findViewById(R.id.layout_area_type);
        mPerodicTypeLayout = findViewById(R.id.layout_perodic_type);

        mTriggerTypeRadioGroup = findViewById(R.id.rg_trigger_type);
        mTriggerTypeRadioGroup.setOnCheckedChangeListener(mAGPSRadioGroupCheckedChangeListener);
        mTriggerTypeGroupItemId = getRadioGroupItemId(mTriggerTypeRadioGroup);

        mPosMethodView = findViewById(R.id.posmethod_text);
        mPosMethodSelectButton = findViewById(R.id.posmethod_edit);
        mPosMethodSelectButton.setOnClickListener(mAGPSBtnClickListener);

        mPerodicMinIntervalTextView = findViewById(R.id.perodic_min_interval_text);
        mPerodicMinIntervalEditButton = findViewById(R.id.perodic_min_interval_edit);
        mPerodicMinIntervalEditButton.setOnClickListener(mAGPSBtnClickListener);

        mPerodicStartTimeTextView = findViewById(R.id.perodic_start_time_text);
        mPerodicStartTimeEditButton = findViewById(R.id.perodic_start_time_edit);
        mPerodicStartTimeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mPerodicStopTimeTextView = findViewById(R.id.perodic_stop_time_text);
        mPerodicStopTimeEditButton = findViewById(R.id.perodic_stop_time_edit);
        mPerodicStopTimeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mAreaTypeTextView = findViewById(R.id.area_type_text);
        mAreaTypeSelectButton = findViewById(R.id.area_type_edit);
        mAreaTypeSelectButton.setOnClickListener(mAGPSBtnClickListener);

        mAreaMinIntervalTextView = findViewById(R.id.min_interval_text);
        mAreaMinIntervalEditButton = findViewById(R.id.min_interval_edit);
        mAreaMinIntervalEditButton.setOnClickListener(mAGPSBtnClickListener);

        mMaxNumTextView = findViewById(R.id.max_num_text);
        mMaxNumEditButton = findViewById(R.id.max_num_edit);
        mMaxNumEditButton.setOnClickListener(mAGPSBtnClickListener);

        mAreaStartTimeTextView = findViewById(R.id.start_time_text);
        mAreaStartTimeEditButton = findViewById(R.id.start_time_edit);
        mAreaStartTimeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mAreaStopTimeTextView = findViewById(R.id.stop_time_text);
        mAreaStopTimeEditButton = findViewById(R.id.stop_time_edit);
        mAreaStopTimeEditButton.setOnClickListener(mAGPSBtnClickListener);
        mGeographicTextView = findViewById(R.id.geographic_text);

        mGeoRadiusTextView = findViewById(R.id.georadius_text);
        mGeoRadiusEditButton = findViewById(R.id.georadius_edit);
        mGeoRadiusEditButton.setOnClickListener(mAGPSBtnClickListener);

        mLatitudeTextView = findViewById(R.id.latitude_text);
        mLatitudeEditButton = findViewById(R.id.latitude_edit);
        mLatitudeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mLongitudeTextView = findViewById(R.id.longitude_text);
        mLongitudeEditButton = findViewById(R.id.longitude_edit);
        mLongitudeEditButton.setOnClickListener(mAGPSBtnClickListener);

        mSignRadioGroup = findViewById(R.id.rg_sign);
        mSignRadioGroup.setOnCheckedChangeListener(mAGPSRadioGroupCheckedChangeListener);
    }

    private void initAGpsCommon() {
        // agps common
        mAllowNetworkInitiatedRequestCheckBox = findViewById(R.id.allow_network_initiated_request);
        mSgpsUtils.setCheckBoxListener(mAllowNetworkInitiatedRequestCheckBox, "NI-ENABLE");

        mAllowEMNotificationCheckBox = findViewById(R.id.allow_em_notification);
        mSgpsUtils.setCheckBoxListener(mAllowEMNotificationCheckBox, "EM-NOTIFY");

        mAllowRoamingCheckBox = findViewById(R.id.allow_roaming);
        mSgpsUtils.setCheckBoxListener(mAllowRoamingCheckBox, "ROAMING-ENABLE");

        mLogSUPLToFileCheckBox = findViewById(R.id.log_supl_to_file);
        mSgpsUtils.setCheckBoxListener(mLogSUPLToFileCheckBox, "SUPLLOG-SAVE");

        mSgpsUtils.mNiDialogTestArray = this.getResources().getStringArray(R.array.ni_dialog_test_array);

        mNotificationTimeoutSpinner = findViewById(R.id.notification_timeout);
        mNotificationTimeoutSpinner.setMaxValue(20);
        mNotificationTimeoutSpinner.setMinValue(1);
        mNotificationTimeoutSpinner.setCurrentValue(8);
        mNotificationTimeoutSpinner.setOnInputNumberListener(new NumberPickerView.OnInputNumberListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {  }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG,"mNotificationTimeoutSpinner= "+editable.toString());
                SgpsUtils.setAGPSInfo("PROPERTY", "NOTIFY-TIMEOUT",editable.toString().trim());
            }
        });
        mVerificationTimeoutSpinner = findViewById(R.id.verification_timeout);
        mVerificationTimeoutSpinner.setMaxValue(20);
        mVerificationTimeoutSpinner.setMinValue(1);
        mVerificationTimeoutSpinner.setCurrentValue(8);
        mVerificationTimeoutSpinner.setOnInputNumberListener(new NumberPickerView.OnInputNumberListener() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {  }
            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG,"mNotificationTimeoutSpinner= "+editable.toString());
                SgpsUtils.setAGPSInfo("PROPERTY", "VERIFY-TIMEOUT",editable.toString().trim());
            }
        });

        mResetToDefaultButton = findViewById(R.id.bt_reset_to_default);
        mResetToDefaultButton.setOnClickListener(mAGPSBtnClickListener);
        mNITestSelectButton = findViewById(R.id.bt_ni_dialog_test);
        mNITestSelectButton.setOnClickListener(mAGPSBtnClickListener);
    }

    private void initAGPSConfigLayoutStatus() {
        // user plane
        String agpsMode = SgpsUtils.getAGPSInfo("PROPERTY", "SUPL-MODE");
        if ("standalone".equals(agpsMode)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[0]);
        } else if ("msb".equals(agpsMode)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[1]);
        } else if ("msa".equals(agpsMode)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[2]);
        }
        mSgpsUtils.initAGPSTextViewItemStatus(mSLPAddressTextView, "SERVER-ADDRESS");
        mSgpsUtils.initAGPSTextViewItemStatus(mSLPPortTextView, "SERVER-PORT");
        mSgpsUtils.initAGPSCheckBoxItemStatus(mTLSEnableCheckBox, "TLS-ENABLE");
        String setId = SgpsUtils.getAGPSInfo("PROPERTY", "SETID");
        if ("IPV4".equals(setId)) {
            mSetIDRadioGroup.check(mSetIDRadioGroupItemId[0]);
        } else if ("IMSI".equals(setId)) {
            mSetIDRadioGroup.check(mSetIDRadioGroupItemId[1]);
        }
        String accuracyUnit = SgpsUtils.getAGPSInfo("PROPERTY","ACCURACY-UNIT");
        if ("KVALUE".equals(accuracyUnit)) {
            mAccuracyUnitRadioGroup.check(mAccuracyUnitRadioGroupItemId[0]);
        } else if ("METER".equals(accuracyUnit)) {
            mAccuracyUnitRadioGroup.check(mAccuracyUnitRadioGroupItemId[1]);
        }
        mSgpsUtils.initAGPSTextViewItemStatus(mHorizontalAccuracyTextView, "HORIZON-ACCURACY");
        mSgpsUtils.initAGPSTextViewItemStatus(mVerticalAccuracyTextView, "VERTICAL-ACCURACY");
        mSgpsUtils.initAGPSTextViewItemStatus(mLocationAgeTextView, "LOC-AGE");
        mSgpsUtils.initAGPSTextViewItemStatus(mDelayTextView, "DELAY");

        String enable = SgpsUtils.getAGPSInfo("PROPERTY", "CER-VERIFY");
        if ("TRUE".equals(enable)) {
            mCertificateVerificationCheckBox.setChecked(true);
            mCertificateVerificationEditButton.setEnabled(true);
        } else {
            mCertificateVerificationCheckBox.setChecked(false);
            mCertificateVerificationEditButton.setEnabled(false);
        }

        mSgpsUtils.initAGPSCheckBoxItemStatus(mEnableLabIOTTestCheckBox, "LAB-IOT");
        mSgpsUtils.initAGPSCheckBoxItemStatus(mEnableeCIDCheckBox, "ECID-ENABLE");

        mSgpsUtils.initAGPSTextViewItemStatus(mMSISDNTextView, "LOCAL-MSISDN");
        mSgpsUtils.initAGPSTextViewItemStatus(m3rdMSISDNTextView, "3RD-MSISDN");

        if ("TRUE".equals(SgpsUtils.getAGPSInfo("PROPERTY", ""))) {
            mEnable3rdMSISDNCheckBox.setChecked(true);
            m3rdMSISDNEditButton.setEnabled(true);
        } else {
            mEnable3rdMSISDNCheckBox.setChecked(false);
            m3rdMSISDNEditButton.setEnabled(false);
        }

        mSgpsUtils.initAGPSTextViewItemStatus(mPerodicMinIntervalTextView, "PERIODIC-MIN-INTERVAL");
        mSgpsUtils.initAGPSTextViewItemStatus(mPerodicStartTimeTextView, "PERIODIC-START-TIME");
        mSgpsUtils.initAGPSTextViewItemStatus(mPerodicStopTimeTextView, "PERIODIC-STOP-TIME");

        String TriggerType = SgpsUtils.getAGPSInfo("PROPERTY", "TRIGGER-TYPE");
        if ("PERIODIC".equals(TriggerType)) {
            mTriggerTypeRadioGroup.check(mTriggerTypeGroupItemId[0]);
            mAreaTypeLayout.setVisibility(View.GONE);
            mPerodicTypeLayout.setVisibility(View.VISIBLE);
        } else if ("AREA".equals(TriggerType)) {
            mTriggerTypeRadioGroup.check(mTriggerTypeGroupItemId[1]);
            mAreaTypeLayout.setVisibility(View.VISIBLE);
            mPerodicTypeLayout.setVisibility(View.GONE);
        }

        String posmethod = "";
        if ("PERIODIC".equals(TriggerType)) {
            posmethod = SgpsUtils.getAGPSInfo( "PROPERTY","PERIODIC-POSMETHOD");
        } else if ("AREA".equals(TriggerType)) {
            posmethod = SgpsUtils.getAGPSInfo("PROPERTY", "AREA-POSMETHOD");
        }

        List<String> mPosMethodArrayValuesList = Arrays.asList(mSgpsUtils.mPosMethodArrayValues);
        if (!"".equals(posmethod) && posmethod != null) {
            mPosMethodView.setText(Arrays.asList(mSgpsUtils.mPosMethodArray).get(
                    mPosMethodArrayValuesList.indexOf(posmethod)));
        }

        String areaType = SgpsUtils.getAGPSInfo("PROPERTY", "AREA-TYPE");
        List<String> mAreaTypeArrayValuesList = Arrays.asList(mSgpsUtils.mAreaTypeArrayValues);
        if (areaType != null) {
            mAreaTypeTextView.setText(Arrays.asList(mSgpsUtils.mAreaTypeArray).get(mAreaTypeArrayValuesList.indexOf(areaType)));
        }
        mSgpsUtils.initAGPSTextViewItemStatus(mAreaMinIntervalTextView, "AREA-MIN-INTERVAL");
        mSgpsUtils.initAGPSTextViewItemStatus(mMaxNumTextView, "MAX-NUM");
        mSgpsUtils.initAGPSTextViewItemStatus(mAreaStartTimeTextView, "AREA-START-TIME");
        mSgpsUtils.initAGPSTextViewItemStatus(mAreaStopTimeTextView, "AREA-STOP-TIME");
        mSgpsUtils.initAGPSTextViewItemStatus(mGeographicTextView, "GEOGRAPHIC");
        mSgpsUtils.initAGPSTextViewItemStatus(mGeoRadiusTextView, "GEORADIUS");
        mSgpsUtils.initAGPSTextViewItemStatus(mLatitudeTextView, "GEO-LAT");
        mSgpsUtils.initAGPSTextViewItemStatus(mLongitudeTextView, "GEO-LON");
        mSignRadioGroupItemId = getRadioGroupItemId(mSignRadioGroup);
        String signType = SgpsUtils.getAGPSInfo( "PROPERTY","SIGN");
        if ("NORTH".equals(signType)) {
            mSignRadioGroup.check(mSignRadioGroupItemId[0]);
        } else if ("SOUTH".equals(signType)) {
            mSignRadioGroup.check(mSignRadioGroupItemId[1]);
        }
        // common
        mSgpsUtils.initAGPSCheckBoxItemStatus(mAllowNetworkInitiatedRequestCheckBox, "NI-ENABLE");
        mSgpsUtils.initAGPSCheckBoxItemStatus(mAllowEMNotificationCheckBox, "EM-NOTIFY");
        mSgpsUtils.initAGPSCheckBoxItemStatus(mAllowRoamingCheckBox, "ROAMING-ENABLE");
        mSgpsUtils.initAGPSCheckBoxItemStatus(mLogSUPLToFileCheckBox, "SUPLLOG-SAVE");

        int notifyIndex = Integer.parseInt(SgpsUtils.getAGPSInfo("PROPERTY","NOTIFY-TIMEOUT"));
        mNotificationTimeoutSpinner.setCurrentValue(notifyIndex);
        int verifyIndex = Integer.parseInt(SgpsUtils.getAGPSInfo("PROPERTY","VERIFY-TIMEOUT"));
        mVerificationTimeoutSpinner.setCurrentValue(verifyIndex);
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        switch (id) {
            case SgpsUtils.DIALOG_SLP_TEMPLATE:
                final ArrayList<String> SLPValueList = mSgpsUtils.mSLPValueList;
                final CharSequence[] items = mSgpsUtils.mSLPNameList.toArray(new CharSequence[0]);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] item = SLPValueList.get(which).split(":");
                        Log.d(TAG, "address = " + item[0] + ", port is " + item[1]);
                        SgpsUtils.setAGPSInfo("PROPERTY", "SERVER-ADDRESS", item[0]);
                        SgpsUtils.setAGPSInfo("PROPERTY", "SERVER-PORT", item[1]);
                        mSLPAddressTextView.setText(item[0]);
                        mSLPPortTextView.setText(item[1]);
                    }
                });
                return builder.create();
            case SgpsUtils.DIALOG_SLP_ADDRESS:
                setEditTextDialogBuilder(builder, "SERVER-ADDRESS", mSLPAddressTextView,
                        InputType.TYPE_CLASS_TEXT, "SERVER-ADDRESS", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_SLP_PORT:
                setEditTextDialogBuilder(builder, "SERVER-PORT", mSLPPortTextView,
                        InputType.TYPE_CLASS_NUMBER, "SERVER-PORT", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_HORIZONTAL_ACCURACY:
                setEditTextDialogBuilder(builder,"HORIZON-ACCURACY", mHorizontalAccuracyTextView,
                        InputType.TYPE_CLASS_NUMBER, "HORIZON-ACCURACY", "0123456789", 3, "0~100");
                return builder.create();
            case SgpsUtils.DIALOG_VERTICAL_ACCURACY:
                setEditTextDialogBuilder(builder,"VERTICAL-ACCURACY", mVerticalAccuracyTextView,
                        InputType.TYPE_CLASS_NUMBER, "VERTICAL-ACCURACY", "0123456789", 5,"0~99999");
                return builder.create();
            case SgpsUtils.DIALOG_LOCATIONAGE:
                setEditTextDialogBuilder(builder,"LOC-AGE", mLocationAgeTextView,
                        InputType.TYPE_CLASS_NUMBER, "LOC-AGE", "0123456789", 5,"0~99999");
                return builder.create();
            case SgpsUtils.DIALOG_DELAY:
                setEditTextDialogBuilder(builder,"DELAY", mDelayTextView,
                        InputType.TYPE_CLASS_NUMBER, "DELAY", "0123456789", 5,"0~99999");
                return builder.create();
            case SgpsUtils.DIALOG_CERTIFICATEVERIFICATION:
                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setText(SgpsUtils.getAGPSInfo( "PROPERTY","SUPL-CER"));
                builder.setTitle("SUPL-CER");
                Spannable text = et.getText();
                if (text != null) {
                    Selection.setSelection(text, text.length());
                }
                builder.setView(et);
                builder.setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, et.getText().toString());
                                String value = et.getText().toString();
                                SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-CER", value);
                            }
                        });
                builder.setNegativeButton(R.string.dlg_cancel, null);
                return builder.create();
            case SgpsUtils.DIALOG_MSISDN:
                setEditTextDialogBuilder(builder,"LOCAL-MSISDN", mMSISDNTextView,
                        InputType.TYPE_CLASS_NUMBER, "LOCAL-MSISDN", "0123456789", 20, null);
                return builder.create();
            case SgpsUtils.DIALOG_3RDMSISDN:
                setEditTextDialogBuilder(builder,"3RD-MSISDN", m3rdMSISDNTextView,
                        InputType.TYPE_CLASS_NUMBER, "3RD-MSISDN", "0123456789", 20, null);
                return builder.create();
            case SgpsUtils.DIALOG_POSMETHOD_SELECT:
                final CharSequence[] posmethoditems = mSgpsUtils.mPosMethodArray;
                builder.setItems(posmethoditems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String TriggerType = SgpsUtils.getAGPSInfo("PROPERTY","TRIGGER-TYPE");
                        if ("PERIODIC".equals(TriggerType)) {
                            SgpsUtils.setAGPSInfo( "PROPERTY","PERIODIC-POSMETHOD",mSgpsUtils.mPosMethodArrayValues[which]);
                        } else {
                            SgpsUtils.setAGPSInfo("PROPERTY", "AREA-POSMETHOD",mSgpsUtils.mPosMethodArrayValues[which]);
                        }
                        mPosMethodView.setText(posmethoditems[which]);
                    }
                });
                return builder.create();
            case SgpsUtils.DIALOG_AREA_TYPE_SELECT:
                final CharSequence[] areTypeitems = mSgpsUtils.mAreaTypeArray;
                builder.setItems(areTypeitems, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        SgpsUtils.setAGPSInfo("PROPERTY", "AREA-TYPE",mSgpsUtils.mAreaTypeArrayValues[which]);
                        mAreaTypeTextView.setText(areTypeitems[which]);
                    }
                });
                return builder.create();
            case SgpsUtils.DIALOG_PERODIC_MININTERVAL:
                setEditTextDialogBuilder(builder,"PERIODIC-MIN-INTERVAL", mPerodicMinIntervalTextView,
                        InputType.TYPE_CLASS_NUMBER, "PERIODIC-MIN-INTERVAL", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_PERODIC_STARTTIME:
               setEditTextDialogBuilder(builder,"PERIODIC-START-TIME", mPerodicStartTimeTextView,
                        InputType.TYPE_CLASS_NUMBER, "PERIODIC-START-TIME", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_PERODIC_STOPTIME:
                setEditTextDialogBuilder(builder,"PERIODIC-STOP-TIME", mPerodicStopTimeTextView,
                        InputType.TYPE_CLASS_NUMBER, "PERIODIC-STOP-TIME", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_AREA_MININTERVAL:
                setEditTextDialogBuilder(builder,"AREA-MIN-INTERVAL", mAreaMinIntervalTextView,
                        InputType.TYPE_CLASS_NUMBER, "AREA-MIN-INTERVAL", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_MAXNUM:
                setEditTextDialogBuilder(builder,"MAX-NUM", mMaxNumTextView,
                        InputType.TYPE_CLASS_NUMBER, "MAX-NUM", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_AREA_STARTTIME:
                setEditTextDialogBuilder(builder, "AREA-START-TIME",mAreaStartTimeTextView,
                        InputType.TYPE_CLASS_NUMBER, "AREA-START-TIME", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_AREA_STOPTIME:
                setEditTextDialogBuilder(builder,"AREA-STOP-TIME", mAreaStopTimeTextView,
                        InputType.TYPE_CLASS_NUMBER, "AREA-STOP-TIME", null, -1, null);
                builder.show();
                break;
            case SgpsUtils.DIALOG_GEORADIUS:
                setEditTextDialogBuilder(builder,"GEORADIUS", mGeoRadiusTextView,
                        InputType.TYPE_CLASS_TEXT, "GEORADIUS", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_LATITUDE:
                setEditTextDialogBuilder(builder, "GEO-LAT",mLatitudeTextView,
                        InputType.TYPE_CLASS_TEXT, "GEO-LAT", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_LONGITUDE:
                setEditTextDialogBuilder(builder,"GEO-LON", mLongitudeTextView,
                        InputType.TYPE_CLASS_TEXT, "GEO-LON", null, -1, null);
                return builder.create();
            case SgpsUtils.DIALOG_NI_DIALOG_TEST:
                final CharSequence[] niDialogTestitems = mSgpsUtils.mNiDialogTestArray;
                builder.setItems(niDialogTestitems, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SgpsUtils.setAGPSInfo("PROPERTY", "NI-TEST",mSgpsUtils.mNiDialogTestArrayValues[which]);
                    }
                });
                return builder.create();
            case SgpsUtils.DIALOG_CUSTOMER_CMD:
                final EditText cus = new EditText(this);
                cus.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setTitle("Custom Cmd");
                cus.setText(mModeSystemCustomEdit.getText().toString());
                Spannable cus_text =cus.getText();
                Selection.setSelection(cus_text, cus_text.length());
                builder.setView(cus);
                builder.setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, cus.getText().toString());
                                mModeSystemCustomEdit.setText(cus.getText().toString());
                                mSgpsUtils.SetCustomModeSystem(cus.getText().toString());
                            }
                        });
                builder.setNegativeButton(R.string.dlg_cancel, null);
                return builder.create();
        }
        return super.onCreateDialog(id);
    }

    private void resetToDefault() {
        // reset control plane
        String mDefault = getResources().getString(R.string.default_mpm);
        if ("LOCATION".equals(mDefault)) {
            mRGMolrPositionMethod.check(mRGMolrPositionMethodItemId[0]);
        } else {
            mRGMolrPositionMethod.check(mRGMolrPositionMethodItemId[1]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "MPM", mDefault);

        boolean isTure = getResources().getBoolean(R.bool.default_external_address_checkbox_checked);
        SgpsUtils.setAGPSInfo( "PROPERTY","ExtAddr Enable", isTure ? "TRUE" : "FALSE");

        mExternalAddressCheckBox.setChecked(isTure);
        mExternalAddressEditText.setEnabled(isTure);
        mExternalAddressSaveButton.setEnabled(isTure);

        mSgpsUtils.resetAGPSTextViewItemStatus(mExternalAddressEditText, "EXTERNAL-ADDRESS", getResources()
                .getString(R.string.default_external_address));

        isTure = getResources().getBoolean(R.bool.default_mlc_number_checkbox_checked);
        SgpsUtils.setAGPSInfo("PROPERTY", "MlcNum Enable",isTure ? "TRUE" : "FALSE");

        mMLCNumberCheckBox.setChecked(isTure);
        mMLCNumberEditText.setEnabled(isTure);
        mMLCNumberSaveButton.setEnabled(isTure);

        mSgpsUtils.resetAGPSTextViewItemStatus(mMLCNumberEditText, "MLC-NUMBER", getResources()
                .getString(R.string.default_mlc_number));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mAutoResetCheckBox, "CP-AUTORESET",getResources().getBoolean(R.bool.default_cp_autoreset));

        mDefault = getResources().getString(R.string.default_sim_prefer);
        if ("SIM1".equals(mDefault)) {
            mRGSimSelection.check(mRGSimSelectionItemId[0]);
        } else if ("SIM2".equals(mDefault)) {
            mRGSimSelection.check(mRGSimSelectionItemId[1]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "SIM-PREFER", mDefault);
        mMOLASwitch.setChecked(false);
        // reset user plane
        mDefault = getResources().getString(R.string.default_supl_mode);
        if ("standalone".equals(mDefault)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[0]);
        } else if ("msb".equals(mDefault)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[1]);
        } else if ("msa".equals(mDefault)) {
            mAGPSModeRadioGroup.check(mAGPSModeRadioGroupItemId[2]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-MODE", mDefault);
        mSgpsUtils.resetAGPSTextViewItemStatus(mSLPAddressTextView, "SERVER-ADDRESS", getResources()
                .getString(R.string.default_server_address));
        mSgpsUtils.resetAGPSTextViewItemStatus(mSLPPortTextView, "SERVER-PORT", getResources()
                .getString(R.string.default_server_port));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mAutoResetCheckBox, "TLS-ENABLE",getResources().getBoolean(R.bool.default_tls_enable));

        mDefault = getResources().getString(R.string.default_supl_version);
        SgpsUtils.setAGPSInfo( "PROPERTY","SUPL-VERSION", mDefault);
        if (mSgpsUtils.showSUPL2View(mDefault)) {
            mAGPSSUPL2Layout.setVisibility(View.VISIBLE);
            mEnableSUPL2CheckBox.setChecked(true);
        } else {
            mAGPSSUPL2Layout.setVisibility(View.GONE);
            mEnableSUPL2CheckBox.setChecked(false);
        }

        mDefault = getResources().getString(R.string.default_set_id);
        if ("IPV4".equals(mDefault)) {
            mSetIDRadioGroup.check(mSetIDRadioGroupItemId[0]);
        } else if ("IMSI".equals(mDefault)) {
            mSetIDRadioGroup.check(mSetIDRadioGroupItemId[1]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "SETID", mDefault);

        mDefault = getResources().getString(R.string.default_accuracy_unit);
        if ("KVALUE".equals(mDefault)) {
            mAccuracyUnitRadioGroup.check(mAccuracyUnitRadioGroupItemId[0]);
        } else if ("METER".equals(mDefault)) {
            mAccuracyUnitRadioGroup.check(mAccuracyUnitRadioGroupItemId[1]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "ACCURACY-UNIT", mDefault);
        mSgpsUtils.resetAGPSTextViewItemStatus(mHorizontalAccuracyTextView, "HORIZON-ACCURACY", getResources()
                .getString(R.string.default_horizon_accuracy));
        mSgpsUtils.resetAGPSTextViewItemStatus(mVerticalAccuracyTextView, "VERTICAL-ACCURACY", getResources()
                .getString(R.string.default_vertical_accuracy));
        mSgpsUtils.resetAGPSTextViewItemStatus(mLocationAgeTextView, "LOC-AGE", getResources()
                .getString(R.string.default_loc_age));
        mSgpsUtils.resetAGPSTextViewItemStatus(mDelayTextView, "DELAY", getResources()
                .getString(R.string.default_delay));
        isTure = getResources().getBoolean(R.bool.default_cer_verify);
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mCertificateVerificationCheckBox, "CER-VERIFY", isTure);
        mCertificateVerificationEditButton.setEnabled(isTure);
        SgpsUtils.setAGPSInfo("PROPERTY", "SUPL-CER",getResources().getString(R.string.default_supl_cer));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mEnableLabIOTTestCheckBox, "LAB-IOT",getResources().getBoolean(R.bool.default_lab_iot));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mEnableeCIDCheckBox, "ECID-ENABLE",getResources().getBoolean(R.bool.default_enable_ecid));

        mSgpsUtils.resetAGPSTextViewItemStatus(mMSISDNTextView, "LOCAL-MSISDN", getResources()
                .getString(R.string.default_local_msisdn));
        mSgpsUtils.resetAGPSTextViewItemStatus(m3rdMSISDNTextView, "3RD-MSISDN", getResources()
                .getString(R.string.default_3rd_msisdn));

        mDefault = getResources().getString(R.string.default_trigger_type);
        if ("PERIODIC".equals(mDefault)) {
            mTriggerTypeRadioGroup.check(mTriggerTypeGroupItemId[0]);
            mAreaTypeLayout.setVisibility(View.GONE);
            mPerodicTypeLayout.setVisibility(View.VISIBLE);
        } else if ("AREA".equals(mDefault)) {
            mTriggerTypeRadioGroup.check(mTriggerTypeGroupItemId[1]);
            mAreaTypeLayout.setVisibility(View.VISIBLE);
            mPerodicTypeLayout.setVisibility(View.GONE);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "TRIGGER-TYPE", mDefault);

        String periodic_posmethod = getResources().getString(R.string.default_periodic_posmethod);
        SgpsUtils.setAGPSInfo( "PROPERTY","PERIODIC-POSMETHOD", periodic_posmethod);
        String area_posmethod = getResources().getString(R.string.default_area_posmethod);
        SgpsUtils.setAGPSInfo( "PROPERTY","AREA-POSMETHOD", area_posmethod);

        List<String> mPosMethodArrayValuesList = Arrays.asList(mSgpsUtils.mPosMethodArrayValues);
        if ("PERIODIC".equals(mDefault)) {
            mPosMethodView.setText(Arrays.asList(mSgpsUtils.mPosMethodArray).get(mPosMethodArrayValuesList.indexOf(periodic_posmethod)));
        } else if ("AREA".equals(mDefault)) {
            mPosMethodView.setText(Arrays.asList(mSgpsUtils.mPosMethodArray).get(mPosMethodArrayValuesList.indexOf(area_posmethod)));
        }
        mSgpsUtils.resetAGPSTextViewItemStatus(mAreaTypeTextView, "AREA-TYPE", getResources()
                .getString(R.string.default_area_type));
        mSgpsUtils.resetAGPSTextViewItemStatus(mPerodicMinIntervalTextView, "PERIODIC-MIN-INTERVAL", getResources()
                .getString(R.string.default_periodic_min_interval));
        mSgpsUtils.resetAGPSTextViewItemStatus(mPerodicStartTimeTextView, "PERIODIC-START-TIME",getResources()
                .getString(R.string.default_periodic_start_time));
        mSgpsUtils.resetAGPSTextViewItemStatus(mPerodicStopTimeTextView, "PERIODIC-STOP-TIME", getResources()
                .getString(R.string.default_periodic_stop_time));

        mSgpsUtils.resetAGPSTextViewItemStatus(mAreaMinIntervalTextView, "AREA-MIN-INTERVAL", getResources()
                .getString(R.string.default_area_min_interval));
        mSgpsUtils.resetAGPSTextViewItemStatus(mMaxNumTextView, "MAX-NUM", getResources()
                .getString(R.string.default_max_num));
        mSgpsUtils.resetAGPSTextViewItemStatus(mAreaStartTimeTextView, "AREA-START-TIME", getResources()
                .getString(R.string.default_area_start_time));
        mSgpsUtils.resetAGPSTextViewItemStatus(mAreaStopTimeTextView, "AREA-STOP-TIME", getResources()
                .getString(R.string.default_area_stop_time));
        mSgpsUtils.resetAGPSTextViewItemStatus(mGeographicTextView, "GEOGRAPHIC", getResources()
                .getString(R.string.default_geographic));
        mSgpsUtils.resetAGPSTextViewItemStatus(mGeoRadiusTextView, "GEORADIUS", getResources()
                .getString(R.string.default_georadius));
        mSgpsUtils.resetAGPSTextViewItemStatus(mLatitudeTextView, "GEO-LAT", getResources()
                .getString(R.string.default_geo_lat));
        mSgpsUtils.resetAGPSTextViewItemStatus(mLongitudeTextView, "GEO-LON", getResources()
                .getString(R.string.default_geo_lon));

        mDefault = getResources().getString(R.string.default_sign);
        if ("NORTH".equals(mDefault)) {
            mSignRadioGroup.check(mSignRadioGroupItemId[0]);
        } else if ("SOUTH".equals(mDefault)) {
            mSignRadioGroup.check(mSignRadioGroupItemId[1]);
        }
        SgpsUtils.setAGPSInfo("PROPERTY", "SIGN", mDefault);
        // reset common
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mAllowNetworkInitiatedRequestCheckBox, "NI-ENABLE",getResources().getBoolean(R.bool.default_ni_enable));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mAllowEMNotificationCheckBox, "EM-NOTIFY",getResources().getBoolean(R.bool.default_em_notify));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mAllowRoamingCheckBox, "ROAMING-ENABLE",getResources().getBoolean(R.bool.default_roaming_enable));
        mSgpsUtils.resetAGPSCheckBoxItemStatus(mLogSUPLToFileCheckBox, "SUPLLOG-SAVE",getResources().getBoolean(R.bool.default_supllog_save));
        mDefault = getResources().getString(R.string.default_notify_timeout);
        SgpsUtils.setAGPSInfo("PROPERTY", "NOTIFY-TIMEOUT", mDefault);
        mNotificationTimeoutSpinner.setCurrentValue(Integer.parseInt(mDefault));
        mDefault = getResources().getString(R.string.default_verify_timeout);
        SgpsUtils.setAGPSInfo("PROPERTY", "VERIFY-TIMEOUT", mDefault);
        mVerificationTimeoutSpinner.setCurrentValue(Integer.parseInt(mDefault));
        SgpsUtils.setAGPSInfo("PROPERTY", "NI-TEST",getResources().getString(R.string.default_ni_test));

    }

    private void setEditTextDialogBuilder(AlertDialog.Builder builder,String title,
                                                         final TextView tv, int inputType,
                                                         final String key, String limitdigits, int maxlength, String hint) {
        final EditText et = new EditText(this);
        et.setInputType(inputType);
        et.setText(tv.getText());

        if (maxlength > 0) {
            et.setFilters(new InputFilter[] {
                    new InputFilter.LengthFilter(maxlength)
            });
        }
        if (limitdigits != null) {
            et.setKeyListener(DigitsKeyListener.getInstance(limitdigits));
        }
        if (hint != null) {
            et.setHint(hint);
        }
        builder.setTitle(title);
        Spannable text = et.getText();
        if (text != null) {
            Selection.setSelection(text, text.length());
        }
        builder.setView(et);
        builder.setPositiveButton(R.string.dlg_ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = et.getText().toString();
                        SgpsUtils.setAGPSInfo("PROPERTY", key, value);
                        tv.setText(value);
                    }
                });
        builder.setNegativeButton(R.string.dlg_cancel, null);
    }

    /* Spreadst: cold, warm, hot and factory switch. @{ */
    private class AutoCircleTestThread extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            setAutoTransferStartButtonEnable(false);
            try {
                int ndelay;
                for (SgpsUtils.GPSModeEnum mode :SgpsUtils.GPSModeEnum.values()) {
                    if (!mCircAutoTestHCWF[mode.ordinal()].isChecked()|| !mSgpsUtils.getmIsAutoTransferTestRunning()) {
                        continue;
                    }
                    Bundle extras=mSgpsUtils.initAutoCircleTestThread(mode,mSgpsUtils.mAutoTransferTotalTimes);
                    mHandler.sendEmptyMessage(SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_HINT);
                    /* @} */
                    for (int j = 0; j < mSgpsUtils.mAutoTransferTotalTimes && mSgpsUtils.getmIsAutoTransferTestRunning(); j++) {
                        resetParam(extras,SgpsUtils.ONE_SECOND*4);
                        mSgpsUtils.mCurrentTimes = j + 1;
                        mSgpsUtils.mTtffTimeoutFlag = false;
                        sendModeAndTimes(mode.ordinal(), j + 1,"<- Testing");
                        Log.d(TAG, "mode = " + mode.toString() + " times = " + j);
                        Long beginTime = Calendar.getInstance().getTime().getTime()/ SgpsUtils.ONE_SECOND;
                        Log.d(TAG, "beginTime[first]" + beginTime);
                        while(mSgpsUtils.getmIsAutoTransferTestRunning()) {
                            Long nowTime = Calendar.getInstance().getTime().getTime()/ SgpsUtils.ONE_SECOND;
                            if (mSgpsUtils.mFirstFix) {
                                break;
                            } else if (nowTime - beginTime > mSgpsUtils.mTimeoutValue) {
                                if (!mSgpsUtils.mTtffTimeoutFlag) {
                                    mSgpsUtils.mTtffTimeoutFlag = true;
                                    mSgpsUtils.mTTFFTimeoutCont++;
                                }
                                Log.d(TAG, "AutoCircleTestThread : updateTimeout");
                                break;
                            }
                            Thread.sleep(SgpsUtils.ONE_SECOND / 10);
                        }
                        mSgpsUtils.mShowFirstFixLocate = true;
                        ndelay=mSgpsUtils.modifyCountDown(mSgpsUtils.mTTFFInterval);
                        do{
                            ndelay--;
                            Thread.sleep(SgpsUtils.ONE_SECOND);
                        }while(ndelay>0 && mSgpsUtils.getmIsAutoTransferTestRunning());
                        //Unisoc:Bug1569503 autotestlog TTFF is incorrect
                        mHandler.sendEmptyMessage(SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG);
                    }
                    sendModeAndTimes(mode.ordinal(), mSgpsUtils.mAutoTransferTotalTimes,"<- Finish");
                    mHandler.removeMessages(SgpsUtils.HANDLE_COUNTER);
                    /* spreadst: send command to close file. one mode to save one file. @{ */
                    mHandler.sendEmptyMessage(SgpsUtils.HANDLE_COMMAND_OTHERS_UPDATE_RESULT_LOG_END);
                    //transfer Interval
                    ndelay=mSgpsUtils.mModeInterval;
                    do{
                        ndelay--;
                        Thread.sleep(1000);
                    }while(ndelay>0 && mSgpsUtils.getmIsAutoTransferTestRunning());
                }
                resetParam(null,0);
                // all complete!!!
                stopAutoTransferGPSAutoTest();
            } catch (InterruptedException ignored) {
            }
            setAutoTransferStartButtonEnable(true);
            interrupt();
        }
    }
    private final OnNmeaMessageListener mNmeaListener = new OnNmeaMessageListener() {
        @Override
        public void onNmeaMessage(String nmea,long timestamp) {
            if (SgpsUtils.ISGe2 ||SgpsUtils.ISMarlin3 || SgpsUtils.ISMarlin3lite) {
                if (nmeaParser == null) {
                    nmeaParser = new NmeaParser(SgpsActivity.this);
                    nmeaParser.setLocalNmeaListener(SgpsActivity.this);
                }
                nmeaParser.parserNmeaStatement(nmea);
            }

            if (timestamp - mLastTimestamp > SgpsUtils.ONE_SECOND) {
                mLastTimestamp = timestamp;
            }

            if (mStartNmeaRecord) {
                SgpsUtils.mOutputNMEALog.writeMyLog(nmea);
                if (mCurrentTabTag.equals(getString(R.string.nmea_log)) && !mSgpsActivityonPause) {
                    mTvNmeaLog.append(nmea);
                    mHandler.post(mScrollToBottom);
                }
            }
            //for noise scan
            if(nmea.contains("RD_RSSI") && IsStartNoiseScan) {
                mSgpsUtils.doNoiseScan(nmea,mtvNoiseScanResult);
                if (mSgpsUtils.mCurrScanTimesCount == mSgpsUtils.mCurrScanTimes) {
                    Log.v(TAG, "mNoiseScanFinished ");
                    IsStartNoiseScan = false;
                    SgpsUtils.mOutputRSSILog.closeMyLog();
                    mHandler.sendEmptyMessage(SgpsUtils.HANDLE_CUSTOM_CURVECHART);
                }
            }
        }
    };
    private boolean isLocationFixed(Iterable<GpsSatellite> list){
        synchronized (this) {
            return !mSgpsUtils.isLocationFixed(list);
        }
    }
    public void setSatelliteStatus(int svCount, int[] prns, float[] snrs,
                                   float[] elevations, float[] azimuths, int ephemerisMask,
                                   int almanacMask, int[] usedInFixMask) {
        synchronized (this) {
            mSgpsUtils.setSatelliteStatus(svCount,prns,snrs,elevations,azimuths,ephemerisMask,almanacMask,usedInFixMask);
        }
        mSatelliteView.postInvalidate();
        mSignalView.postInvalidate();
    }
    private void setSatelliteStatus(Iterable<GpsSatellite> list){
        synchronized (this) {
            mSgpsUtils.setSatelliteStatus(list);
        }
        mSatelliteView.postInvalidate();
        mSignalView.postInvalidate();
    }
    public int getSatelliteStatus(int[] prns, float[] snrs, float[] elevations,
                                  float[] azimuths, int ephemerisMask, int almanacMask,
                                  int[] usedInFixMask) {
        synchronized (this) {
            return mSgpsUtils.getSatelliteStatus(prns,snrs,elevations,azimuths,ephemerisMask,almanacMask,usedInFixMask);
        }
    }
    public void setSatelliteStatusForGe2(List<com.spreadtrum.sgps.GpsSatellite> list){
        mSgpsUtils.setSatelliteStatusForGe2(list);
    }
    public void updateSatelliteView(List<com.spreadtrum.sgps.GpsSatellite> list) {
        synchronized (this) {
            mSgpsUtils.updateSatelliteView(list);
        }
        mSatelliteView.postInvalidate();
        mSignalView.postInvalidate();
    }
    public void locationFixed(boolean available) {
        Log.d(TAG, "locationFixed available : " + available);
        if (available) {
            mSgpsUtils.mStatus = getString(R.string.gps_status_available);
        } else {
            clearInformationLayout();
            mSgpsUtils.mStatus = getString(R.string.gps_status_unavailable);
        }
    }
}

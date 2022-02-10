
package com.spreadtrum.sgps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import android.util.Log;

public class SatelliteSkyView extends View {
    private static final int DRAW_MARGIN = 12;
    private static final float GRID_WIDTH = 1.0f;
    private static final float TEXT_SIZE = 15.0f;
    private static final float THREE_QUARTER = 0.75f;
    private static final String TAG = "SGPS/SatelliteSkyView";
    private Paint mGridPaint = null;
    private Paint mTextPaint = null;
    private Paint mBackground = null;
    private Bitmap mSatelliteBitmapUsed = null;
    private Bitmap mSatelliteBitmapUnused = null;
    private Bitmap mSatelliteBitmapNoFix = null;

    private Bitmap mChinaFlag = null;
    private Bitmap mAmericaFlag = null;
    private Bitmap mRussiaFlag = null;
    private Bitmap mEuropeFlag = null;

    private SatelliteDataProvider mProvider = null;
    private int mSatellites = 0;
    private final int[] mPrns = new int[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mElevation = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mAzimuth = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final float[] mSnrs = new float[SatelliteDataProvider.MAX_SATELLITES_NUMBER];
    private final int[] mUsedInFixMask = new int[SatelliteDataProvider.SATELLITES_MASK_SIZE];

    public SatelliteSkyView(Context context) {
        this(context, null);
    }

    public SatelliteSkyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SatelliteSkyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources res = getResources();
        if (null != res) {
            mGridPaint = new Paint();
            mGridPaint.setColor(res.getColor(R.color.grid));
            mGridPaint.setAntiAlias(true);
            mGridPaint.setStyle(Style.STROKE);
            mGridPaint.setStrokeWidth(GRID_WIDTH);
            mBackground = new Paint();
            mBackground.setColor(res.getColor(R.color.skyview_background));

            mTextPaint = new Paint();
            mTextPaint.setColor(res.getColor(R.color.skyview_text_color));
            mTextPaint.setTextSize(TEXT_SIZE);
            mTextPaint.setTextAlign(Align.CENTER);
            /*add to for show flag*/
            BitmapDrawable satChina = (BitmapDrawable) res
                    .getDrawable(R.drawable.china);
            if (null != satChina) {
                mChinaFlag = satChina.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.china) failed");
            }
            BitmapDrawable satAmerica = (BitmapDrawable) res
                    .getDrawable(R.drawable.america);
            if (null != satAmerica) {
                mAmericaFlag = satAmerica.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.america)) failed");
            }
            BitmapDrawable satRussia = (BitmapDrawable) res
                    .getDrawable(R.drawable.russia);
            if (null != satRussia) {
                mRussiaFlag = satRussia.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.russia) failed");
            }
            BitmapDrawable satEurope = (BitmapDrawable) res
                    .getDrawable(R.drawable.europe);
            if (null != satEurope) {
                mEuropeFlag = satEurope.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.europe) failed");
            }
            /*end*/
            BitmapDrawable satgreen = (BitmapDrawable) res
                    .getDrawable(R.drawable.satgreen);
            if (null != satgreen) {
                mSatelliteBitmapUsed = satgreen.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.satgreen) failed");
            }
            BitmapDrawable satyellow = (BitmapDrawable) res
                    .getDrawable(R.drawable.satyellow);
            if (null != satyellow) {
                mSatelliteBitmapUnused = satyellow.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(R.drawable.satyellow)) failed");
            }
            BitmapDrawable satred = (BitmapDrawable) res
                    .getDrawable(R.drawable.satred);
            if (null != satred) {
                mSatelliteBitmapNoFix = satred.getBitmap();
            } else {
                Log.i(TAG, "get BitmapDrawable getDrawable(xxx) failed");
            }

        }
    }

    public void setDataProvider(SatelliteDataProvider provider) {
        mProvider = provider;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2;
        float centerX = getWidth() / 2;
        int radius;
        if (centerX > centerY) {
            radius = (getHeight() / 2) - DRAW_MARGIN;
        } else {
            radius = (getWidth() / 2) - DRAW_MARGIN;
        }
        final Paint gridPaint = mGridPaint;
        final Paint textPaint = mTextPaint;
        canvas.drawPaint(mBackground);
        canvas.drawCircle(centerX, centerY, radius, gridPaint);
        canvas.drawCircle(centerX, centerY, radius * THREE_QUARTER, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
        canvas.drawCircle(centerX, centerY, radius >> 2, gridPaint);
        canvas.drawLine(centerX, centerY - (radius >> 2), centerX, centerY
                - radius, gridPaint);
        canvas.drawLine(centerX, centerY + (radius >> 2), centerX, centerY
                + radius, gridPaint);
        canvas.drawLine(centerX - (radius >> 2), centerY, centerX - radius,
                centerY, gridPaint);
        canvas.drawLine(centerX + (radius >> 2), centerY, centerX + radius,
                centerY, gridPaint);
        if (mProvider != null) {
            mSatellites = mProvider.getSatelliteStatus(mPrns, mSnrs,
                    mElevation, mAzimuth, 0, 0, mUsedInFixMask);

        }
        for (int i = 0; i < mSatellites; ++i) {

            Log.v(TAG,
                    "Index: " +i + " mElevation:" + mElevation[i]  + " mAzimuth:"
                            +mAzimuth[i]   + " mPrns[i]:" + mPrns[i]);

            int x=(int)(centerX+(radius*mElevation[i]*Math.sin(Math.PI*mAzimuth[i]/180)/90));
            int y=(int)(centerY-(radius*mElevation[i]*Math.cos(Math.PI*mAzimuth[i]/180)/90));

            /**add for show flag**/
            if (mPrns[i] >= 1 && mPrns[i] <= 32) {
                canvas.drawBitmap(mAmericaFlag, x, y, gridPaint);
            } else if (mPrns[i] >= 65 && mPrns[i] <= 92) {
                canvas.drawBitmap(mRussiaFlag, x, y, gridPaint);
            } else if ((mPrns[i] >= 151 && mPrns[i] <= 187)|| (mPrns[i] >= 201 && mPrns[i] <= 237)) {
                canvas.drawBitmap(mChinaFlag, x, y, gridPaint);
            }
            else if (mPrns[i] >= 301 && mPrns[i] <= 336) {  //for Galileo
                canvas.drawBitmap(mEuropeFlag, x, y, gridPaint);}
            else {
                if (!isUsedInFix(0) || mSnrs[i] <= 0) {
                    canvas.drawBitmap(mSatelliteBitmapNoFix, x, y, gridPaint);
                } else if (isUsedInFix(mPrns[i])) {
                    canvas.drawBitmap(mSatelliteBitmapUsed, x, y, gridPaint);
                } else { // yellow
                    canvas.drawBitmap(mSatelliteBitmapUnused, x, y, gridPaint);
                }
            }
            /*end*/
            canvas.drawText(Integer.toString(mPrns[i]), x, y, textPaint);
        }
    }

}

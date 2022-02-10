
package com.spreadtrum.sgps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.Log;
import android.view.View;
import java.util.List;

public class LineGraphicView extends View {

    private static final String TAG = "SGPS/LineGraphicView";
    // Coordinate unit
    private final String[] xLabel;
    private final String[] yLabel;
    // The curve data
    private final List<float[]> dataList;
    private final List<Integer> colorList;
    // The default margins
    private final int margin = 20;
    // Distance on the left side of the offset
    private final int marginX = 30;
    // The origin of coordinates
    private int xPoint;
    private int yPoint;
    // X, Y axis per unit length
    private int xScale;
    private int yScale;
    // The brush
    private Paint paintAxes;
    private Paint paintCoordinate;
    private Paint paintTable;
    private Paint paintCurve;
    private Paint paintRectF;
    private Paint paintValue;
    private final Context mContext;

    public LineGraphicView(Context context, String[] xLabel,
                                String[] yLabel, List<float[]> dataList, List<Integer> colorList) {
        super(context);
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.dataList = dataList;
        this.colorList = colorList;
        this.mContext = context;
    }

    /**
     * Initialize the data values and brushes
     */
    private void init() {
        xPoint = margin + marginX;
        yPoint = this.getHeight() - margin;
        xScale = (this.getWidth() - 2 * margin - marginX) / (xLabel.length - 1);
        yScale = (this.getHeight() - 2 * margin) / (yLabel.length - 1);

        paintAxes = new Paint();
        paintAxes.setStyle(Paint.Style.STROKE);
        paintAxes.setAntiAlias(true);
        paintAxes.setDither(true);
        paintAxes.setColor(mContext.getResources().getColor(R.color.color14));
        paintAxes.setStrokeWidth(4);

        paintCoordinate = new Paint();
        paintCoordinate.setStyle(Paint.Style.STROKE);
        paintCoordinate.setDither(true);
        paintCoordinate.setAntiAlias(true);
        paintCoordinate.setColor(mContext.getResources().getColor(
                R.color.color14));
        paintCoordinate.setTextSize(15);

        paintTable = new Paint();
        paintTable.setStyle(Paint.Style.STROKE);
        paintTable.setAntiAlias(true);
        paintTable.setDither(true);
        paintTable.setColor(mContext.getResources().getColor(R.color.color4));
        paintTable.setStrokeWidth(2);

        paintCurve = new Paint();
        paintCurve.setStyle(Paint.Style.STROKE);
        paintCurve.setDither(true);
        paintCurve.setAntiAlias(true);
        paintCurve.setStrokeWidth(3);
        PathEffect pathEffect = new CornerPathEffect(25);
        paintCurve.setPathEffect(pathEffect);

        paintRectF = new Paint();
        paintRectF.setStyle(Paint.Style.FILL);
        paintRectF.setDither(true);
        paintRectF.setAntiAlias(true);
        paintRectF.setStrokeWidth(3);

        paintValue = new Paint();
        paintValue.setStyle(Paint.Style.STROKE);
        paintValue.setAntiAlias(true);
        paintValue.setDither(true);
        paintValue.setColor(mContext.getResources().getColor(R.color.color1));
        paintValue.setTextAlign(Paint.Align.CENTER);
        paintValue.setTextSize(15);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mContext.getResources().getColor(R.color.color1));
        init();
        drawTable(canvas, paintTable);
        drawAxesLine(canvas, paintAxes);
        drawCoordinate(canvas, paintCoordinate);
        for (int i = 0; i < dataList.size(); i++) {
            drawCurve(canvas, paintCurve, dataList.get(i), colorList.get(i));
        }
    }

    /**
     * Drawing axes
     */
    private void drawAxesLine(Canvas canvas, Paint paint) {
        // X
        canvas.drawLine(xPoint, yPoint, this.getWidth() - margin / 6, yPoint,
                paint);
        canvas.drawLine(this.getWidth() - margin / 6, yPoint, this.getWidth()
                - margin / 2, yPoint - margin / 3, paint);
        canvas.drawLine(this.getWidth() - margin / 6, yPoint, this.getWidth()
                - margin / 2, yPoint + margin / 3, paint);

        // Y
        canvas.drawLine(xPoint, yPoint, xPoint, margin / 6, paint);
        canvas.drawLine(xPoint, margin / 6, xPoint - margin / 3, margin / 2,
                paint);
        canvas.drawLine(xPoint, margin / 6, xPoint + margin / 3, margin / 2,
                paint);
    }

    /**
     * Draw table
     */
    private void drawTable(Canvas canvas, Paint paint) {
        Path path = new Path();
        // The horizontal line
        for (int i = 1; (yPoint - i * yScale) >= margin; i++) {
            int startX = xPoint;
            int startY = yPoint - i * yScale;
            int stopX = xPoint + (xLabel.length - 1) * xScale;
            path.moveTo(startX, startY);
            path.lineTo(stopX, startY);
            canvas.drawPath(path, paint);
        }

        // Vertical line
        for (int i = 1; i * xScale <= (this.getWidth() - margin); i++) {
            int startX = xPoint + i * xScale;
            int startY = yPoint;
            int stopY = yPoint - (yLabel.length - 1) * yScale;
            path.moveTo(startX, startY);
            path.lineTo(startX, stopY);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * Draw the calibration
     */
    private void drawCoordinate(Canvas canvas, Paint paint) {
        // The X coordinate
        for (int i = 0; i <= (xLabel.length - 1); i++) {
            paint.setTextAlign(Paint.Align.CENTER);
            int startX = xPoint + i * xScale;
            canvas.drawText(xLabel[i], startX, this.getHeight() - margin / 6,
                    paint);
        }

        // The Y coordinate
        for (int i = 0; i <= (yLabel.length - 1); i++) {
            paint.setTextAlign(Paint.Align.LEFT);
            int startY = yPoint - i * yScale;
            int offsetX;
            switch (yLabel[i].length()) {
                case 1:
                    offsetX = 28;
                    break;
                case 2:
                    offsetX = 20;
                    break;
                case 3:
                    offsetX = 12;
                    break;
                case 4:
                    offsetX = 5;
                    break;
                default:
                    offsetX = 0;
                    break;
            }
            int offsetY;
            if (i == 0) {
                offsetY = 0;
            } else {
                offsetY = margin / 5;
            }
            // X to the position of the string on the left of the screen by
            // default, y is the default string is a string of baseline position
            // on the screen
            canvas.drawText(yLabel[i], margin / 4 + offsetX, startY + offsetY,
                    paint);
        }
    }

    /**
     * Draw the curve
     */
    private void drawCurve(Canvas canvas, Paint paint, float[] data, int color) {
        paint.setColor(mContext.getResources().getColor(color));
        Path path = new Path();
        /*
         * for (int i = 0; i <= (data.length - 1); i++) { if (i == 0) {
         * path.moveTo(toX(0), toY(data[0])); } else { path.lineTo(toX(i),
         * toY(data[i])); }
         *
         * if (i == data.length - 1) { path.lineTo(toX(i), toY(data[i])); } }
         */
        for (int i = 1; i <= data.length; i++) {
            if (i == 1) {
                path.moveTo(toX(1), toY(data[0]));
            } else {
                path.lineTo(toX(i), toY(data[i - 1]));
            }

            if (i == data.length) {
                path.lineTo(toX(i), toY(data[i - 1]));
            }
        }
        canvas.drawPath(path, paint);
    }

    /**
     * Turn coordinate data in proportion
     */
    private float toY(float num) {
        float y;
        try {
            float scale = Float.parseFloat(yLabel[1])
                    - Float.parseFloat(yLabel[0]);
            float a = num / scale;
            if (0 > Float.parseFloat(yLabel[0])) {
                y = (float) yPoint - (float) ((yLabel.length / 2) * yScale) - a
                        * yScale;
            } else {
                y = (float) yPoint - a * yScale;
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
        return y;
    }

    private float toX(float num) {
        float x;
        try {
            float scale = Float.parseFloat(xLabel[1])
                    - Float.parseFloat(xLabel[0]);
            float a = num / scale;
            x = (float) xPoint + a * xScale;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return 0;
        }
        return x;
    }

}


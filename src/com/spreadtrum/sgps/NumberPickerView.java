package com.spreadtrum.sgps;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;



public class NumberPickerView extends LinearLayout implements View.OnClickListener, TextWatcher {


    private int maxNumValue = Integer.MAX_VALUE;
    private int minNumValue = 0;
    private int mNumStep = 1;

    //默认字体的大小
    private static final int textDefaultSize = 14;
    // 中间输入框的‘输入值
    private EditText mNumText;

    // 监听事件(负责警戒值回调)
    private OnClickInputListener onClickInputListener;
    // 监听输入框内容变化
    private OnInputNumberListener onInputNumberListener;

    public NumberPickerView(Context context) {
        super(context);
    }

    public NumberPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initNumberPickerView(context, attrs);
    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void initNumberPickerView(final Context context, AttributeSet attrs) {
        //加载定义好的布局文件
        LayoutInflater.from(context).inflate(R.layout.number_button, this);
        LinearLayout mRoot = findViewById(R.id.root);
        TextView subText = findViewById(R.id.button_sub);
        TextView addText = findViewById(R.id.button_add);
        mNumText = findViewById(R.id.middle_count);

        //添加监听事件
        addText.setOnClickListener(this);
        subText.setOnClickListener(this);
        mNumText.setOnClickListener(this);
        mNumText.addTextChangedListener(this);

        //获取自定义属性的相关内容
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NumberButton);
        // 背景
        int resourceId = typedArray.getResourceId(R.styleable.NumberButton_backgroud, R.drawable.bg_number_button);
        int addResourceId = typedArray.getResourceId(R.styleable.NumberButton_addBackground, R.drawable.bg_button_right);
        int subResourceId = typedArray.getResourceId(R.styleable.NumberButton_subBackground, R.drawable.bg_button_left);
        // 水平分割线
        Drawable dividerDrawable = typedArray.getDrawable(R.styleable.NumberButton_individer);
        //中间的编辑框是否可编辑
        boolean aBoolean = typedArray.getBoolean(R.styleable.NumberButton_editable, true);
        //+和-文本的宽度 geDiemension返回float getDimensionPixelSize四舍五入+  getDimensionPixeloffset四舍五入-
        int buttonWidth = typedArray.getDimensionPixelSize(R.styleable.NumberButton_buttonWidth, -1);
        //+和-文本的颜色
        int textColor = typedArray.getColor(R.styleable.NumberButton_textColor, 0xff000000);
        //+和-文本的字体大小
        int textSize = typedArray.getDimensionPixelSize(R.styleable.NumberButton_textSize, -1);
        // 中间显示数量的按钮宽度
        final int editextWidth = typedArray.getDimensionPixelSize(R.styleable.NumberButton_editextWidth, -1);
        //必须调用这个，因为自定义View会随着Activity创建频繁的创建array
        typedArray.recycle();

        //设置输入框是否可用
        setEditable(aBoolean);
        //初始化控件颜色
        mRoot.setBackgroundResource(resourceId);
        mRoot.setDividerDrawable(dividerDrawable);
        subText.setBackgroundResource(subResourceId);
        addText.setBackgroundResource(addResourceId);
        addText.setTextColor(textColor);
        subText.setTextColor(textColor);
        mNumText.setTextColor(textColor);

        //初始化字体,注意默认的是px单位，要转换
        if (textSize > 0) {
            mNumText.setTextSize(px2sp(context, textSize));
        } else {
            mNumText.setTextSize(textDefaultSize);
        }

        //设置文本框的宽高
        if (buttonWidth > 0) {
            LayoutParams layoutParams = new LayoutParams(buttonWidth, LayoutParams.MATCH_PARENT);
            addText.setLayoutParams(layoutParams);
            subText.setLayoutParams(layoutParams);
        } else {
            Log.d("NumPickerView", "文本采用默认的宽高");
        }
        //设置输入框的宽高
        if (editextWidth > 0) {
            LayoutParams layoutParams = new LayoutParams(editextWidth, LayoutParams.MATCH_PARENT);
            mNumText.setLayoutParams(layoutParams);
        } else {
            Log.d("NumPickerView", "编辑框采用默认的宽高");
        }
    }

    /**
     * @param editable 设置输入框是否可编辑
     */
    private void setEditable(boolean editable) {
        if (editable) {
            mNumText.setFocusable(true);
            mNumText.setKeyListener(new DigitsKeyListener());
        } else {
            mNumText.setFocusable(false);
            mNumText.setKeyListener(null);
        }
    }

    /**
     * @return 获取输入框的最终数字值
     */
    public int getNumText() {
        try {
            String textNum = mNumText.getText().toString().trim();
            return Integer.parseInt(textNum);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mNumText.setText(String.valueOf(minNumValue));
            return minNumValue;
        }
    }

    public NumberPickerView setNumStep(int iStep) {
        this.mNumStep = iStep;
        return this;
    }


    public NumberPickerView setMinValue(int minNumValue) {
        this.minNumValue = minNumValue;
        return this;
    }

    public NumberPickerView setMaxValue(int maxNumValue) {
        this.maxNumValue = maxNumValue;
        return this;
    }

    public NumberPickerView setCurrentValue(int currentNumValue) {
        if (currentNumValue > minNumValue) {
            if (currentNumValue > maxNumValue) {
                mNumText.setText(String.valueOf(maxNumValue));
            } else {
                mNumText.setText(String.valueOf(currentNumValue));
            }
        } else {
            mNumText.setText(String.valueOf(minNumValue));
        }
        return this;
    }

    public NumberPickerView setOnClickInputListener(OnClickInputListener mOnWarnListener) {
        this.onClickInputListener = mOnWarnListener;
        return this;
    }

    public NumberPickerView setOnInputNumberListener(OnInputNumberListener onInputNumberListener) {
        this.onInputNumberListener = onInputNumberListener;
        return this;
    }

    @Override
    public void onClick(View view) {
        int widgetId = view.getId();
        int numText = getNumText();
        if (widgetId == R.id.button_sub) {
            if (numText > minNumValue + mNumStep) {
                mNumText.setText(String.valueOf(numText - mNumStep));
            } else {
                mNumText.setText(String.valueOf(minNumValue));
            }
        } else if (widgetId == R.id.button_add) {
            if (numText < maxNumValue - mNumStep) {
                mNumText.setText(String.valueOf(numText + mNumStep));
            } else {
                mNumText.setText(String.valueOf(maxNumValue));

            }
        }
        mNumText.setSelection(mNumText.getText().toString().length());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        if (onInputNumberListener != null) {
            onInputNumberListener.beforeTextChanged(charSequence, start, count, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (onInputNumberListener != null) {
            onInputNumberListener.onTextChanged(charSequence, start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (onInputNumberListener != null) {
            onInputNumberListener.afterTextChanged(editable);
        }
        try {
            mNumText.removeTextChangedListener(this);
            String inputText = editable.toString().trim();
            if (!TextUtils.isEmpty(inputText)) {
                int inputNum = Integer.parseInt(inputText);
                if (inputNum < minNumValue) {
                    mNumText.setText(String.valueOf(minNumValue));
                } else if (inputNum > maxNumValue) {
                    mNumText.setText(String.valueOf(maxNumValue));
                }
            }
            mNumText.addTextChangedListener(this);
            mNumText.setSelection(mNumText.getText().toString().length());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }


    /**
     * 小于最小值回调
     * Warning for inventory.
     */
    private void warningForMinInput() {
        if (onClickInputListener != null)
            onClickInputListener.onWarningMinInput(minNumValue);
    }

    /**
     * 查过最大值值回调
     * Warning for inventory.
     */
    private void warningForMaxInput() {
        if (onClickInputListener != null)
            onClickInputListener.onWarningMaxInput(maxNumValue);
    }

    /**
     * 超过警戒值回调
     */
    public interface OnClickInputListener {

        void onWarningMinInput(int minValue);

        void onWarningMaxInput(int maxValue);
    }

    /**
     * 输入框数字内容监听
     */
    public interface OnInputNumberListener {

        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence charSequence, int start, int before, int count);

        void afterTextChanged(Editable editable);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue DisplayMetrics类中属性density）
     * @return
     */
    private static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    private static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    private static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
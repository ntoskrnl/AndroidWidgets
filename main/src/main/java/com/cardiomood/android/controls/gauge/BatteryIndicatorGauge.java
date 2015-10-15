package com.cardiomood.android.controls.gauge;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cardiomood.android.controls.R;

/**
 * Created by danon on 03.04.2014.
 */
public class BatteryIndicatorGauge extends View {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int DEFAULT_ORIENTATION = HORIZONTAL;

    public static final float DEFAULT_MAX = 100;
    public static final float DEFAULT_MIN = 0;
    public static final float DEFAULT_VALUE = 0;
    public static final ValueToColorConverter DEFAULT_CONVERTER = new ValueToColorConverter() {
        @Override
        public int getColorOf(BatteryIndicatorGauge view, float value) {
            float percentage = view.getPercentage();
            if (percentage < 10.0f) {
                return Color.RED;
            }
            if (percentage < 20.0f) {
                return Color.rgb(255, 127, 0);
            }
            if (percentage < 40.0f) {
                return Color.rgb(255, 191, 0);
            }
            if (percentage < 50.0f) {
                return Color.rgb(255, 216, 0);
            }
            if (percentage < 70.0f) {
                return Color.rgb(255, 247, 0);
            }
            return Color.rgb(102, 255, 0);
        }
    };

    private float max = DEFAULT_MAX;
    private float value = 0;
    private float min = DEFAULT_MIN;
    private int orientation = DEFAULT_ORIENTATION;
    private ValueToColorConverter valueToColorConverter;

    private float density = 1.0f;

    private Paint mPaint;
    private Paint bmpPaint;
    private Paint ovalPaint;
    private Bitmap batteryBitmap;

    public BatteryIndicatorGauge(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;

        init();
    }

    public BatteryIndicatorGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;

//        TypedArray attributes = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.BatteryIndicatorGauge,
//                0, 0);
//
//        try {
//            // read attributes
//            setMax(attributes.getFloat(R.styleable.BatteryIndicatorGauge_max, (float) DEFAULT_MAX));
//            setMin(attributes.getFloat(R.styleable.BatteryIndicatorGauge_min, (float) DEFAULT_MIN));
//            setValue(attributes.getFloat(R.styleable.BatteryIndicatorGauge_value, DEFAULT_VALUE));
//            setOrientation(attributes.getInt(R.styleable.BatteryIndicatorGauge_orientation, DEFAULT_ORIENTATION));
//        } finally {
//            attributes.recycle();
//        }

        init();
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        if (max < min) {
            throw new IllegalArgumentException("Illegal value: max < min");
        }

        this.max = max;
        invalidate();
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        if (max < min) {
            throw new IllegalArgumentException("Illegal value: min > max");
        }
        this.min = min;
        invalidate();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value <= getMin())
            value = getMin();
        if (value >= getMax())
            value = getMax();
        this.value = value;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setValue(float value, long duration, long delay) {
        if (value <= getMin())
            value = getMin();
        if (value >= getMax())
            value = getMax();

        ValueAnimator va = ValueAnimator.ofFloat(getValue(), value).setDuration(duration);
        va.setStartDelay(delay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                setValue(value);
            }
        });
        va.start();
        return va;
    }

    public ValueToColorConverter getValueToColorConverter() {
        return valueToColorConverter;
    }

    public void setValueToColorConverter(ValueToColorConverter valueToColorConverter) {
        this.valueToColorConverter = valueToColorConverter;
        invalidate();
    }

    private int getColorForValue(float value) {
        return valueToColorConverter == null ? DEFAULT_CONVERTER.getColorOf(this, value) : valueToColorConverter.getColorOf(this, value);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == VERTICAL || orientation == HORIZONTAL)
            this.orientation = orientation;
        else throw new IllegalArgumentException("Invalid orientation: " + orientation);

        if (getOrientation() == HORIZONTAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_horisontal);
        else
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_vertical);
    }

    public float getPercentage() {
        return 100 * getValue() / (getMax() - getMin());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            // it's ok
        } else if (width >= 0) {
            height = Math.round(width / getOriginalWidth() * getOriginalHeight());
        } else if (height >= 0) {
            width = Math.round(height / getOriginalHeight() * getOriginalWidth());
        } else {
            width = 0;
            height = 0;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        mPaint.setColor(Color.TRANSPARENT);
//        canvas.drawPaint(mPaint);

        if (getWidth() == 0 || getHeight() == 0)
            return;

        RectF rect = getRect(getWidth(), getHeight());
        float alpha = rect.width()/getOriginalWidth();
        mPaint.setColor(getColorForValue(getValue()));
        if (!isInEditMode())
            mPaint.setMaskFilter(new BlurMaskFilter(3*alpha, BlurMaskFilter.Blur.NORMAL));
        RectF contentRect = getBatteryContentRect(rect);
        float percentage = getPercentage();
        if (getOrientation() == HORIZONTAL) {
            contentRect.right = contentRect.left + contentRect.width() * percentage/100;
        } else {
            contentRect.top = contentRect.top + contentRect.height() * (100 - percentage)/100;
        }
        canvas.drawRect(contentRect, mPaint);

        if (percentage > 0.5f && percentage < 99.0f) {
            ovalPaint.setColor(getColorForValue(getValue()));
            if (getOrientation() == HORIZONTAL) {
                canvas.drawOval(new RectF(contentRect.right - alpha * 20, contentRect.top, contentRect.right + alpha * 20, contentRect.bottom), ovalPaint);
                ovalPaint.setColor(Color.argb(6, 0, 0, 0));
                canvas.drawOval(new RectF(contentRect.right - alpha * 20, contentRect.top, contentRect.right + alpha * 20, contentRect.bottom), ovalPaint);
            } else {
                canvas.drawOval(new RectF(contentRect.left, contentRect.top - alpha * 25, contentRect.right, contentRect.top + alpha * 25), ovalPaint);
                ovalPaint.setColor(Color.argb(6, 0, 0, 0));
                canvas.drawOval(new RectF(contentRect.left, contentRect.top - alpha * 25, contentRect.right, contentRect.top + alpha * 25), ovalPaint);
            }

            mPaint.setColor(getColorForValue(getValue()));
            if (getOrientation() == HORIZONTAL) {
                canvas.drawOval(new RectF(contentRect.right - alpha * 12, contentRect.top + alpha * 20, contentRect.right + alpha * 12, contentRect.bottom - alpha * 20), mPaint);
                mPaint.setColor(Color.argb(80, 255, 255, 255));
                if (!isInEditMode())
                    mPaint.setMaskFilter(new BlurMaskFilter(6 * alpha, BlurMaskFilter.Blur.NORMAL));
                canvas.drawOval(new RectF(contentRect.right - alpha * 12, contentRect.top + alpha * 20, contentRect.right + alpha * 12, contentRect.bottom - alpha * 20), mPaint);
            } else {
                canvas.drawOval(new RectF(contentRect.left + alpha * 20, contentRect.top - alpha * 12, contentRect.right - alpha * 20, contentRect.top + alpha * 12), mPaint);
                mPaint.setColor(Color.argb(80, 255, 255, 255));
                if (!isInEditMode())
                    mPaint.setMaskFilter(new BlurMaskFilter(6 * alpha, BlurMaskFilter.Blur.NORMAL));
                canvas.drawOval(new RectF(contentRect.left + alpha * 20, contentRect.top - alpha * 12, contentRect.right - alpha * 20, contentRect.top + alpha * 12), mPaint);
            }
        }

        Bitmap bmp = Bitmap.createScaledBitmap(batteryBitmap, (int) rect.width(), (int) rect.height(), true);
        canvas.drawBitmap(bmp, rect.left, rect.top, bmpPaint);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        bmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bmpPaint.setStyle(Paint.Style.FILL);

        ovalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ovalPaint.setStyle(Paint.Style.FILL);

        if (getOrientation() == HORIZONTAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_horisontal);
        else if (getOrientation() == VERTICAL)
            batteryBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_empty_battery_vertical);
        else throw new IllegalStateException("Invalid orientation value: " + getOrientation());
    }

    private float getOriginalWidth() {
        if (!isInEditMode()) {
            return batteryBitmap.getWidth();
        } else {
            return (getOrientation() == HORIZONTAL) ? 467 : 216;
        }
    }

    private float getOriginalHeight() {
        if (!isInEditMode()) {
            return batteryBitmap.getHeight();
        } else {
            return (getOrientation() == HORIZONTAL) ? 216 : 467;
        }
    }

    private RectF getRect(float w, float h) {
        float availableWidth = w - getPaddingLeft() - getPaddingRight();
        float availableHeight = h - getPaddingTop() - getPaddingBottom();
        if (availableWidth / availableHeight >= getOriginalWidth() / getOriginalHeight()) {
                // we have abundant space horizontally
                float alpha = availableHeight / getOriginalHeight();
                float x = getPaddingLeft() + (availableWidth - alpha*getOriginalWidth()) / 2.0f;
                float y = getPaddingTop();
                return new RectF(x, y, x + alpha*getOriginalWidth(), h - getPaddingBottom());
        } else {
            // too much space vertically
            float alpha = availableWidth / getOriginalWidth();
            float x = getPaddingLeft();
            float y = getPaddingTop() + (availableHeight - alpha*getOriginalHeight()) / 2.0f;
            return new RectF(x, y, w - getPaddingRight(), y + alpha*getOriginalHeight());
        }
    }

    private RectF getBatteryContentRect(RectF rect) {
        if (getOrientation() == HORIZONTAL) {
            float alpha = rect.width() / getOriginalWidth();
            return new RectF(
                    rect.left + 20*alpha,
                    rect.top + 3*alpha,
                    rect.right - 40*alpha,
                    rect.bottom - 3*alpha
            );
        } else if (getOrientation() == VERTICAL) {
            float alpha = rect.height() / getOriginalHeight();
            return new RectF(
                    rect.left + 4*alpha,
                    rect.top + 40*alpha,
                    rect.right - 4*alpha,
                    rect.bottom - 20*alpha
            );
        } else throw new IllegalStateException("Invalid orientation value: " + getOrientation());
    }

    public static interface ValueToColorConverter {

        int getColorOf(BatteryIndicatorGauge view, float value);

    }
}

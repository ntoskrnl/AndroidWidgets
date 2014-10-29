package com.cardiomood.android.controls.progress;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by danon on 01.03.14.
 */
public class CircularProgressBar extends View {

    public static final float DEFAULT_MAX = 100;
    public static final float DEFAULT_MIN = 0;
    public static final int DEFAULT_LINE_WIDTH = 60;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_TEXT_SIZE = 20;

    private float max = DEFAULT_MAX;
    private float progress = 0;
    private float min = DEFAULT_MIN;
    private float lineWidth = DEFAULT_LINE_WIDTH;
    private int color = DEFAULT_COLOR;
    private float textSize = 0;
    private int textColor = Color.BLACK;
    private LabelConverter labelConverter = null;

    private float density = 1.0f;

    private Paint mPaint;
    private Paint txtPaint;


    public CircularProgressBar(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        textSize = DEFAULT_TEXT_SIZE*density;
        init();
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        density = getResources().getDisplayMetrics().density;
        textSize = DEFAULT_TEXT_SIZE*density;

//        density = getResources().getDisplayMetrics().density;
//        TypedArray attributes = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.CircularProgressBar,
//                0, 0);
//
//        try {
//            // read attributes
//            setMax(attributes.getFloat(R.styleable.CircularProgressBar_max, (float) DEFAULT_MAX));
//            setMin(attributes.getFloat(R.styleable.CircularProgressBar_min, (float) DEFAULT_MIN));
//            setProgress(attributes.getFloat(R.styleable.CircularProgressBar_progress, 0));
//            setLineWidth(attributes.getDimensionPixelSize(R.styleable.CircularProgressBar_lineWidth, DEFAULT_LINE_WIDTH));
//            setColor(attributes.getColor(R.styleable.CircularProgressBar_color, DEFAULT_COLOR));
//            setTextSize(attributes.getDimensionPixelSize(R.styleable.CircularProgressBar_textSize, Math.round(DEFAULT_TEXT_SIZE * density)));
//        } finally {
//            attributes.recycle();
//        }

        init();
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        density = getResources().getDisplayMetrics().density;
        textSize = DEFAULT_TEXT_SIZE*density;

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

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        if (progress > max || progress < min) {
            throw new IllegalArgumentException("Illegal value: progress is outside range [min, max]");
        }
        this.progress = progress;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setProgress(float progress, long duration) {
        if (progress > max || progress < min) {
            throw new IllegalArgumentException("Illegal value: progress is outside range [min, max]");
        }
        ValueAnimator animator = ValueAnimator.ofFloat(getProgress(), progress).setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (Float) animation.getAnimatedValue();
                setProgress(f);
            }
        });
        animator.start();
        return animator;
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

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (lineWidth < 0) {
            throw new IllegalArgumentException("Illegal value: lineWidth < 0");
        }
        this.lineWidth = lineWidth;
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int color) {
        this.textColor = textColor;
        if (txtPaint != null)
            txtPaint.setColor(color);
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        if (txtPaint != null)
            txtPaint.setTextSize(textSize);
        invalidate();
    }

    public LabelConverter getLabelConverter() {
        return labelConverter;
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.labelConverter = labelConverter;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        RectF oval = getOval(getWidth(), getHeight(), 1);

        // background circle
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(Color.LTGRAY);
        canvas.drawArc(oval, 0, 360, false, mPaint);

        // current progress arc
        float angle = 360 * Math.abs((progress) / (max - min));
        mPaint.setColor(color);
        canvas.drawArc(oval, -90, angle, false, mPaint);

        if (labelConverter != null) {
            String text = labelConverter.getLabelFor(progress, max, txtPaint);

            if (text != null) {
//                Rect bounds = new Rect();
//                txtPaint.getTextBounds(text, 0, text.length(), bounds);
                canvas.drawText(text, oval.centerX(), oval.centerY() - (txtPaint.descent() + txtPaint.ascent()) / 2, txtPaint);
            }
        }
    }

    private RectF getOval(int w, int h, float factor) {
        RectF oval;
        final int canvasWidth = w - getPaddingLeft() - getPaddingRight() - (int) getLineWidth();
        final int canvasHeight = h - getPaddingTop() - getPaddingBottom() - (int) getLineWidth();

        if (canvasHeight >= canvasWidth) {
            oval = new RectF(0, 0, canvasWidth*factor, canvasWidth*factor);
        } else {
            oval = new RectF(0, 0, canvasHeight*factor, canvasHeight*factor);
        }

        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft()+getLineWidth()/2, (canvasHeight-oval.height())/2 + getPaddingTop()+getLineWidth()/2);

        return oval;
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setTextSize(textSize);
        txtPaint.setColor(textColor);
        txtPaint.setTextAlign(Paint.Align.CENTER);
    }

    public static interface LabelConverter {
        String getLabelFor(float progress, float max, Paint paint);
    }
}

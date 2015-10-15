package com.cardiomood.android.controls.gauge;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.cardiomood.android.controls.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Speedometer with needle.
 *
 * Created by danon on 26.02.14.
 * @version 1.0
 * @author Anton Danshin <a href="mailto:anton.danshin@frtk.ru">anton.danshin@frtk.ru</a>
 */
public class SpeedometerGauge extends View {

    private static final String TAG = SpeedometerGauge.class.getSimpleName();

    public static final double DEFAULT_MAX_SPEED = 100.0;
    public static final double DEFAULT_MAJOR_TICK_STEP = 20.0;
    public static final int DEFAULT_MINOR_TICKS = 1;
    public static final int DEFAULT_LABEL_TEXT_SIZE_DP = 12;
    public static final int DEFAULT_UNITS_TEXT_SIZE_DP = 24;

    private double maxSpeed = DEFAULT_MAX_SPEED;
    private double speed = 0;
    private int defaultColor = Color.rgb(180, 180, 180);
    private double majorTickStep = DEFAULT_MAJOR_TICK_STEP;
    private int minorTicks = DEFAULT_MINOR_TICKS;
    private LabelConverter labelConverter;
    private String unitsText = "km/h";

    private List<ColoredRange> ranges = new ArrayList<ColoredRange>();

    private Paint backgroundPaint;
    private Paint backgroundInnerPaint;
    private Paint maskPaint;
    private Paint needlePaint;
    private Paint ticksPaint;
    private Paint txtPaint;
    private Paint unitsPaint;
    private Paint colorLinePaint;
    private int labelTextSize;
    private int unitsTextSize;

    private Bitmap mMask;

    public SpeedometerGauge(Context context) {
        super(context);
        init();

        float density = getResources().getDisplayMetrics().density;
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density));
        setUnitsTextSize(Math.round(DEFAULT_UNITS_TEXT_SIZE_DP * density));
    }

    public SpeedometerGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        float density = getResources().getDisplayMetrics().density;
        setLabelTextSize(Math.round(DEFAULT_LABEL_TEXT_SIZE_DP * density));
        setUnitsTextSize(Math.round(DEFAULT_UNITS_TEXT_SIZE_DP * density));
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed <= 0)
            throw new IllegalArgumentException("Non-positive value specified as max speed.");
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException("Non-positive value specified as a speed.");
        if (speed > maxSpeed)
            speed = maxSpeed;
        this.speed = speed;
        invalidate();
    }

    public String getUnitsText() {
        return unitsText;
    }

    public void setUnitsText(String unitsText) {
        this.unitsText = unitsText;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, long duration, long startDelay) {
        if (progress < 0)
            throw new IllegalArgumentException("Negative value specified as a speed.");

        if (progress > maxSpeed)
            progress = maxSpeed;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, Double.valueOf(getSpeed()), Double.valueOf(progress));

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setSpeed(value);
            }
        });
        va.start();
        return va;
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, boolean animate) {
        return setSpeed(progress, 1500, 200);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        invalidate();
    }

    public double getMajorTickStep() {
        return majorTickStep;
    }

    public void setMajorTickStep(double majorTickStep) {
        if (majorTickStep <= 0)
            throw new IllegalArgumentException("Non-positive value specified as a major tick step.");
        this.majorTickStep = majorTickStep;
        invalidate();
    }

    public int getMinorTicks() {
        return minorTicks;
    }

    public void setMinorTicks(int minorTicks) {
        this.minorTicks = minorTicks;
        invalidate();
    }

    public LabelConverter getLabelConverter() {
        return labelConverter;
    }

    public void setLabelConverter(LabelConverter labelConverter) {
        this.labelConverter = labelConverter;
        invalidate();
    }

    public void clearColoredRanges() {
        ranges.clear();
        invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException("Incorrect number range specified!");
        if (begin < - 5.0/160* maxSpeed)
            begin = - 5.0/160* maxSpeed;
        if (end > maxSpeed * (5.0/160 + 1))
            end = maxSpeed * (5.0/160 + 1);
        ranges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    public int getLabelTextSize() {
        return labelTextSize;
    }

    public void setLabelTextSize(int labelTextSize) {
        this.labelTextSize = labelTextSize;
        if (txtPaint != null) {
            txtPaint.setTextSize(labelTextSize);
            invalidate();
        }
    }

    public int getUnitsTextSize() {
        return unitsTextSize;
    }

    public void setUnitsTextSize(int unitsTextSize) {
        this.unitsTextSize = unitsTextSize;
        if (unitsPaint != null) {
            unitsPaint.setTextSize(unitsTextSize);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT);

        // Draw Metallic Arc and background
        drawBackground(canvas);

        // Draw Ticks and colored arc
        drawTicks(canvas);

        // Draw Needle
        drawNeedle(canvas);
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
        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            //Must be this size
            width = widthSize;
        } else {
            width = -1;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            //Must be this size
            height = heightSize;
        } else {
            height = -1;
        }

        if (height >= 0 && width >= 0) {
            width = Math.min(height, width);
            height = width/2;
        } else if (width >= 0) {
            height = width/2;
        } else if (height >= 0) {
            width = height*2;
        } else {
            width = 0;
            height = 0;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void drawNeedle(Canvas canvas) {
        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.35f + 10;
        RectF smallOval = getOval(canvas, 0.2f);

        float angle = 10 + (float) (getSpeed()/ getMaxSpeed()*160);
        canvas.drawLine(
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * smallOval.width()*0.5f),
                (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius)),
                (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius)),
                needlePaint
        );


        canvas.drawArc(smallOval, 180, 180, true, backgroundPaint);
    }

    private void drawTicks(Canvas canvas) {
        float availableAngle = 160;
        float majorStep = (float) (majorTickStep/ maxSpeed *availableAngle);
        float minorStep = majorStep / (1 + minorTicks);

        float majorTicksLength = 30;
        float minorTicksLength = majorTicksLength/2;

        RectF oval = getOval(canvas, 1);
        float radius = oval.width()*0.35f;

        float currentAngle = 10;
        double curProgress = 0;
        while (currentAngle <= 170) {

            canvas.drawLine(
                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius-majorTicksLength/2)),
                    (float) (oval.centerX() + Math.cos((180 - currentAngle) / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180 * Math.PI)*(radius+majorTicksLength/2)),
                    ticksPaint
            );

            for (int i=1; i<=minorTicks; i++) {
                float angle = currentAngle + i*minorStep;
                if (angle >= 170 + minorStep/2) {
                    break;
                }
                canvas.drawLine(
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * radius),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * radius),
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180 * Math.PI) * (radius + minorTicksLength)),
                        (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI) * (radius + minorTicksLength)),
                        ticksPaint
                );
            }

            if (labelConverter != null) {

                canvas.save();
                canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
                float txtX = oval.centerX() + radius + majorTicksLength/2 + 8;
                float txtY = oval.centerY();
                canvas.rotate(+90, txtX, txtY);
                canvas.drawText(labelConverter.getLabelFor(curProgress, maxSpeed), txtX, txtY, txtPaint);
                canvas.restore();
            }

            currentAngle += majorStep;
            curProgress += majorTickStep;
        }

        RectF smallOval = getOval(canvas, 0.7f);
        colorLinePaint.setColor(defaultColor);
        canvas.drawArc(smallOval, 185, 170, false, colorLinePaint);

        for (ColoredRange range: ranges) {
            colorLinePaint.setColor(range.getColor());
            canvas.drawArc(smallOval, (float) (190 + range.getBegin()/ maxSpeed *160), (float) ((range.getEnd() - range.getBegin())/ maxSpeed *160), false, colorLinePaint);
        }
    }

    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        if (canvasHeight*2 >= canvasWidth) {
            oval = new RectF(0, 0, canvasWidth*factor, canvasWidth*factor);
        } else {
            oval = new RectF(0, 0, canvasHeight*2*factor, canvasHeight*2*factor);
        }

        oval.offset((canvasWidth-oval.width())/2 + getPaddingLeft(), (canvasHeight*2-oval.height())/2 + getPaddingTop());

        return oval;
    }

    private void drawBackground(Canvas canvas) {
        RectF oval = getOval(canvas, 1);
        canvas.drawArc(oval, 180, 180, true, backgroundPaint);

        RectF innerOval = getOval(canvas, 0.9f);
        canvas.drawArc(innerOval, 180, 180, true, backgroundInnerPaint);

        Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) (oval.width() * 1.1), (int) (oval.height() * 1.1) / 2, true);
        canvas.drawBitmap(mask, oval.centerX() - oval.width()*1.1f/2, oval.centerY()-oval.width()*1.1f/2, maskPaint);

        canvas.drawText(unitsText, oval.centerX(), oval.centerY()/1.5f, unitsPaint);
    }

    @SuppressWarnings("NewApi")
    private void init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.rgb(127, 127, 127));

        backgroundInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundInnerPaint.setStyle(Paint.Style.FILL);
        backgroundInnerPaint.setColor(Color.rgb(150, 150, 150));

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(labelTextSize);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setLinearText(true);

        unitsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unitsPaint.setColor(Color.WHITE);
        unitsPaint.setTextSize(unitsTextSize);
        unitsPaint.setTextAlign(Paint.Align.CENTER);
        unitsPaint.setLinearText(true);

        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.spot_mask);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight() / 2);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setDither(true);

        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(5);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(Color.argb(200, 255, 0, 0));
    }


    public static interface LabelConverter {

        String getLabelFor(double progress, double maxProgress);

    }

    public static class ColoredRange {

        private int color;
        private double begin;
        private double end;

        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public double getBegin() {
            return begin;
        }

        public void setBegin(double begin) {
            this.begin = begin;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }
    }

}

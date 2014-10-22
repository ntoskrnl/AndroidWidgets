package com.cardiomood.android.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by danon on 08.05.2014.
 */
public class CircledTextView extends TextView {

    private int circleColor = Color.LTGRAY;
    private float density = 1;

    public CircledTextView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
    }

    public CircledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        density = getResources().getDisplayMetrics().density;
    }

    public CircledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//
//        int measuredHeight = getMeasuredHeight();
//        int measuredWidth = getMeasuredWidth();
//
//        int w = Math.max(measuredHeight, measuredWidth);
//        setMeasuredDimension(w, w);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect rect = canvas.getClipBounds();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(circleColor);
        paint.setStrokeWidth(3*density);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(rect.centerX(), rect.centerY(), Math.max(rect.width(), rect.height())/2.0f - 1.5f*density, paint);
    }

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
        invalidate();
    }
}

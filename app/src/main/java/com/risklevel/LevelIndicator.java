package com.risklevel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class LevelIndicator extends LinearLayout implements View.OnTouchListener{

    private final static int COUNT = 10;
    private Paint mPaint;
    private Paint mPointerPaint;
    private RiskLevel[] mRiskLevels = new RiskLevel[COUNT];
    float padding = 25;
    float pointerY = 0;

    private float minY;
    private float maxY;

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        pointerY = event.getY();
        invalidate();
        return false;
    }

    private static final class RiskLevel {
        Context mContext;
        float mPadding;
        private int level;
        private float diameter;

        private RiskLevel(final Context context, final float padding, final int level) {
            mContext = context;
            mPadding = padding;
            this.level = level;
        }

        public void setDiameter(final float diameter) {
            this.diameter = diameter;
        }

        public float getX() {
            return mPadding;
        }

        public float getY() {
            return level * (diameter + mPadding + mPadding / (COUNT - 1));
        }

        public int getColor() {
            int res = mContext.getResources().getIdentifier("risk_" + String.valueOf(COUNT - level), "color", mContext.getPackageName());
            return mContext.getResources().getColor(res);
        }

        float getStartY() {
            return getY() - mPadding/2;
        }

        float getEndY() {
            return getY() + mPadding/2 + diameter;
        }
    }

    public LevelIndicator(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LevelIndicator(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LevelIndicator(final Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnTouchListener(this);
        setBackgroundColor(Color.TRANSPARENT);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mPointerPaint = new Paint();
        mPointerPaint.setAntiAlias(true);
        mPointerPaint.setStrokeWidth(2);
        mPointerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (int i = 0; i < COUNT; i++) {
            mRiskLevels[i] = new RiskLevel(getContext(), padding, i);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float diameter = height / COUNT - padding;
        float length = diameter * 4.3f;

        for (int i = 0; i < COUNT; i++) {
            RiskLevel riskLevel = mRiskLevels[i];
            riskLevel.setDiameter(diameter);

            mPaint.setColor(riskLevel.getColor());
            drawLevel(canvas, riskLevel.getX(), riskLevel.getY(), length, diameter);
        }

        minY = mRiskLevels[0].getY() + diameter / 2;
        maxY = mRiskLevels[COUNT - 1].getY() + diameter / 2;

        if (pointerY < minY) {
            pointerY = minY;
        }

        if (pointerY > maxY) {
            pointerY = maxY;
        }

        for (RiskLevel level : mRiskLevels) {
            if (level.getStartY() <= pointerY && level.getEndY() >= pointerY) {
                mPointerPaint.setColor(level.getColor());
                drawTriangle(canvas, mRiskLevels[0].getX() + length + padding + 2*diameter, pointerY, diameter);
            }
        }

    }

    private void drawLevel(Canvas canvas, float x, float y, float length, float diameter) {
        RectF leftOval = drawOval(x, y, diameter + x, diameter + y);
        canvas.drawArc(leftOval, 90, 180, false, mPaint);
        RectF rightOval = drawOval(x + length + diameter / 2, y, 3 * diameter / 2 + x + length, diameter + y);
        canvas.drawArc(rightOval, 90, -180, false, mPaint);
        canvas.drawRect(x + diameter / 2, y, x + length + diameter, y + diameter, mPaint);
    }

    private RectF drawOval(float left, float top, float right, float bottom) {
        return new RectF(left, top, right, bottom);
    }

    private void drawTriangle(Canvas canvas, float x, float y, float side) {
        Path path = new Path();
        path.moveTo(x, y);
        double x1 = x + side * Math.cos(Math.PI / 6);
        double y1 = y - side * Math.sin(Math.PI / 6);
        path.lineTo((float) x1, (float) y1);
        path.lineTo((float) x1, (float) (y1 + side));
        canvas.drawPath(path, mPointerPaint);
    }


}

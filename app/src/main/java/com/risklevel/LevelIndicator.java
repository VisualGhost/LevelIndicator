package com.risklevel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.math.BigDecimal;

public class LevelIndicator extends LinearLayout implements View.OnTouchListener {

    public final static int COUNT = 10;
    private Paint mPaint;
    private Paint mLargeTextPaint;
    private Paint mSmallTextPaint;
    private RiskLevel[] mRiskLevels = new RiskLevel[COUNT];
    public float padding = 25;
    float pointerY = 0;

    private float minY;
    private float maxY;
    private Rect rect;
    private int textHeight;
    private float diameter;
    private float currentValue;

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
        private float startY;

        private RiskLevel(final Context context, final float padding, final int level, final float startY) {
            mContext = context;
            mPadding = padding;
            this.level = level;
            this.startY = startY;
        }

        public void setDiameter(final float diameter) {
            this.diameter = diameter;
        }

        public float getX() {
            return mPadding;
        }

        public float getY() {
            return startY + level * (diameter + mPadding + mPadding / (COUNT - 1));
        }

        public int getColor() {
            int res = mContext.getResources().getIdentifier("risk_" + String.valueOf(COUNT - level), "color", mContext.getPackageName());
            return mContext.getResources().getColor(res);
        }

        float getMinLevel() {
            return COUNT - level - 1 + 0.5f;
        }

        float getMaxLevel() {
            return COUNT - level - 1 + 1.5f;
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

        int textSize = 60;

        mLargeTextPaint = new Paint();
        mLargeTextPaint.setAntiAlias(true);
        mLargeTextPaint.setTextSize(textSize);

        rect = new Rect();
        mLargeTextPaint.getTextBounds("0", 0, 1, rect);
        textHeight = rect.height();

        mSmallTextPaint = new Paint();
        mSmallTextPaint.setAntiAlias(true);
        mSmallTextPaint.setTextSize(textSize / 2);

        for (int i = 0; i < COUNT; i++) {
            mRiskLevels[i] = new RiskLevel(getContext(), padding, i, textHeight / 2);
        }
    }

    public int getValuePadding() {
        return textHeight / 2;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight() - textHeight;
        diameter = height / COUNT - padding;
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

        currentValue = getLevel(pointerY, minY, maxY, 1f, COUNT);

        for (RiskLevel level : mRiskLevels) {
            if (Float.compare(level.getMinLevel(), currentValue) <= 0 && Float.compare(level.getMaxLevel(), currentValue) > 0) {
                mLargeTextPaint.setColor(level.getColor());
                mSmallTextPaint.setColor(level.getColor());
                drawTriangle(canvas, mRiskLevels[0].getX() + length + padding + diameter, pointerY, diameter);
                break;
            }
        }

        BigDecimal bigDecimal = new BigDecimal(String.valueOf(currentValue));
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_CEILING);

        mLargeTextPaint.getTextBounds(bigDecimal.toString(), 0, bigDecimal.toString().length(), rect);

        setPadding(0, rect.height(), 0, 0);

        float textY = pointerY + rect.height() / 2;

        canvas.drawText(bigDecimal.toString(), 200, textY, mLargeTextPaint);
        canvas.drawText("/" + String.valueOf(COUNT), (200 + rect.width() + 5), textY, mSmallTextPaint);

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
        canvas.drawPath(path, mLargeTextPaint);
    }


    private float getLevel(float currentY, float minY, float maxY, float minValue, float maxValue) {
        return (maxValue - minValue) * (currentY - minY) / (minY - maxY) + maxValue;
    }

    static class SavedState extends BaseSavedState {
        float mCurrentY;

        SavedState(Parcelable superState) {
            super(superState);
        }

        protected SavedState(Parcel in) {
            super(in);
            mCurrentY = (float) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(mCurrentY);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();

        SavedState state = new SavedState(superState);
        state.mCurrentY = pointerY;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        pointerY = savedState.mCurrentY;
        invalidate();
    }

}

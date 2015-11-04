package com.risklevel;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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

    // todo not public
    public final static int MAX_RISK_LEVEL = 10;
    private final static int MIN_RISK_LEVEL = 1;
    private final static int DEFAULT_RISK_LEVEL = 5;

    private Paint mLevelPillPaint;
    private Paint mCurrentLevelTextPaint;
    private Paint mMaxLevelTextPaint;

    // "9.16/10" - "9.16" - is a current level; "/10" - max level

    // todo not public
    public float mVerticalDistanceBetweenPills;
    private float mCurrentLevelTextHeight; // the height of "9.16"
    private int mMinLevel;
    private int mMaxLevel;
    private RiskLevelPill[] mRiskLevelPills;
    private float mProportionBetweenWidthAndHeightOfPill;

    float pointerY = -1;
    private float minY;
    private float maxY;
    private float currentLevel;
    private Rect rect;

    public LevelIndicator(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public LevelIndicator(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LevelIndicator(final Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        float levelTextSize = 0;
        float horizontalPillMargin = 0;
        rect = new Rect();

        if (attrs != null) {
            TypedArray array = null;
            try {
                array = context.obtainStyledAttributes(attrs, R.styleable.Risk);
                mVerticalDistanceBetweenPills = array.getDimension(R.styleable.Risk_verticalPillDistance, 0);
                levelTextSize = array.getDimension(R.styleable.Risk_levelTextSize, 0);
                horizontalPillMargin = array.getDimension(R.styleable.Risk_horizontalPillMargin, 0);
                mMinLevel = array.getInteger(R.styleable.Risk_minLevel, MIN_RISK_LEVEL);
                mMaxLevel = array.getInteger(R.styleable.Risk_maxLevel, MAX_RISK_LEVEL);
                mProportionBetweenWidthAndHeightOfPill = array.getFloat(R.styleable.Risk_proportionBetweenWidthAndHeightOfPill, 1);
            } finally {
                if (array != null) {
                    array.recycle();
                }
            }
        }
        mRiskLevelPills = new RiskLevelPill[mMaxLevel];
        setOnTouchListener(this);
        setBackgroundColor(Color.TRANSPARENT);
        initPillPaint();
        initCurrentLevelPaint(levelTextSize);
        initMaxLevelPaint(levelTextSize);
        mCurrentLevelTextHeight = getCurrentLevelTextHeight();
        initLevelPills(horizontalPillMargin);
    }

    private void initPillPaint() {
        mLevelPillPaint = new Paint();
        mLevelPillPaint.setAntiAlias(true);
        mLevelPillPaint.setStrokeWidth(2);
        mLevelPillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void initCurrentLevelPaint(float textSize) {
        mCurrentLevelTextPaint = new Paint();
        mCurrentLevelTextPaint.setAntiAlias(true);
        mCurrentLevelTextPaint.setTextSize(textSize);
    }

    private void initMaxLevelPaint(float textSize) {
        mMaxLevelTextPaint = new Paint();
        mMaxLevelTextPaint.setAntiAlias(true);
        mMaxLevelTextPaint.setTextSize(textSize / 2);
    }

    private float getCurrentLevelTextHeight() {
        // The height is the same for all numbers
        return getTextHeight(rect, mCurrentLevelTextPaint, "0");
    }

    private float getCurrentLevelTextWidth(String text) {
        return getTextWidth(rect, mCurrentLevelTextPaint, text);
    }

    private float getTextHeight(Rect rect, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private float getTextWidth(Rect rect, Paint paint, String text) {
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    private void initLevelPills(float horizontalPillMargin) {
        for (int i = 0; i < mMaxLevel; i++) {
            mRiskLevelPills[i] = new RiskLevelPill(horizontalPillMargin,
                    mVerticalDistanceBetweenPills, i, mCurrentLevelTextHeight / 2, mMaxLevel);
        }
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        pointerY = event.getY();
        invalidate();
        return false;
    }

    private static final class RiskLevelPill {
        float horizontalMargin;
        float verticalMargin;
        float height;
        float startY;
        int index;
        int maxLevel;

        RiskLevelPill(final float horizontalMargin, final float verticalMargin, final int index, final float startY, final int maxLevel) {
            this.horizontalMargin = horizontalMargin;
            this.verticalMargin = verticalMargin;
            this.index = index;
            this.startY = startY;
            this.maxLevel = maxLevel;
        }

        /**
         * Sets the height of pill.
         */
        void setHeight(final float height) {
            this.height = height;
        }

        /**
         * @return The X-coordinate of pill.
         */
        float getX() {
            return horizontalMargin;
        }

        /**
         * @return The Y-coordinate of pill.
         */
        float getY() {
            return startY + index * (height + (verticalMargin * maxLevel) / (maxLevel - 1));
        }

        /**
         * @return The level of risk that this pill represents.
         */
        int getLevel() {
            return maxLevel - index;
        }

        /**
         * @return The minimum value of level that is represented by this pill.
         */
        float getMinEdge() {
            return getLevel() - 0.5f;
        }

        /**
         * @return The maximum value of level that is represented by this pill.
         */
        float getMaxEdge() {
            return getLevel() + 0.5f;
        }
    }

    public int getValuePadding() {
        return (int) (mCurrentLevelTextHeight / 2);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        float availableHeight = getHeight() - mCurrentLevelTextHeight;
        float pillHeight = availableHeight / mMaxLevel - mVerticalDistanceBetweenPills;
        float pillLength = pillHeight * mProportionBetweenWidthAndHeightOfPill;

        drawPills(canvas, pillHeight, pillLength);

        minY = mRiskLevelPills[0].getY() + pillHeight / 2;
        maxY = mRiskLevelPills[mMaxLevel - 1].getY() + pillHeight / 2;

        //todo
        if (Float.compare(pointerY, -1) == 0) {
            pointerY = getCurrentY(DEFAULT_RISK_LEVEL, minY, maxY, mMinLevel, mMaxLevel);
        }

        if (pointerY < minY) {
            pointerY = minY;
        }

        if (pointerY > maxY) {
            pointerY = maxY;
        }

        currentLevel = getLevel(pointerY, minY, maxY, mMinLevel, mMaxLevel);

        for (RiskLevelPill level : mRiskLevelPills) {
            if (Float.compare(level.getMinEdge(), currentLevel) <= 0 && Float.compare(level.getMaxEdge(), currentLevel) > 0) {
                mCurrentLevelTextPaint.setColor(getColor(level.index));
                mMaxLevelTextPaint.setColor(getColor(level.index));
                drawTrianglePointerToLevelPill(canvas, mRiskLevelPills[0].getX() + pillLength + mVerticalDistanceBetweenPills + pillHeight, pointerY, pillHeight);
                break;
            }
        }

        BigDecimal bigDecimal = new BigDecimal(String.valueOf(currentLevel));
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_CEILING);

        float textY = pointerY + mCurrentLevelTextHeight / 2;

        canvas.drawText(bigDecimal.toString(), 200, textY, mCurrentLevelTextPaint);
        canvas.drawText("/" + String.valueOf(mMaxLevel), (200 + getCurrentLevelTextWidth(bigDecimal.toString()) + 5), textY, mMaxLevelTextPaint);

    }

    private void drawPills(Canvas canvas, float pillHeight, float pillLength) {
        for (int i = 0; i < mMaxLevel; i++) {
            RiskLevelPill riskLevelPill = mRiskLevelPills[i];
            riskLevelPill.setHeight(pillHeight);
            mLevelPillPaint.setColor(getColor(i));
            drawLevelPill(canvas, riskLevelPill.getX(), riskLevelPill.getY(), pillLength, pillHeight);
        }
    }

    private int getColor(int level) {
        try {
            int res = getResources().getIdentifier("risk_" +
                    String.valueOf(mMaxLevel - level), "color", getContext().getPackageName());
            return getResources().getColor(res);
        } catch (Resources.NotFoundException e) {
            return Color.WHITE;
        }
    }

    private void drawLevelPill(Canvas canvas, float x, float y, float length, float height) {
        drawStartSegment(canvas, x, y, height);
        drawEndSegment(canvas, x, y, height, length);
        drawPillRectangle(canvas, x, y, length, height);
    }

    private void drawStartSegment(Canvas canvas, float x, float y, float height) {
        drawSegment(canvas, x, y, height + x, height + y, 90, 180);
    }

    private void drawEndSegment(Canvas canvas, float x, float y, float height, float length) {
        float left = x + length + height / 2;
        float right = 3 * height / 2 + x + length;
        float bottom = height + y;
        drawSegment(canvas, left, y, right, bottom, 90, -180);
    }

    private void drawSegment(Canvas canvas,
                             float left,
                             float top,
                             float right,
                             float bottom,
                             float startAngle,
                             float sweepAngle) {
        RectF rectF = new RectF(left, top, right, bottom);
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mLevelPillPaint);
    }

    private void drawPillRectangle(Canvas canvas, float x, float y, float length, float height) {
        float left = x + height / 2;
        float right = x + length + height;
        float bottom = y + height;
        canvas.drawRect(left, y, right, bottom, mLevelPillPaint);
    }

    /**
     * Draw the pointer "<|"
     *
     * @param canvas           The canvas.
     * @param x                The x-coordinate of pointer.
     * @param y                The y-coordinate of pointer.
     * @param lengthOfTriangle The length of triangle side.
     */
    private void drawTrianglePointerToLevelPill(Canvas canvas, float x, float y, float lengthOfTriangle) {
        Path path = new Path();
        path.moveTo(x, y);
        double angle = Math.PI / 6;
        double x1 = x + lengthOfTriangle * Math.cos(angle);
        double y1 = y - lengthOfTriangle * Math.sin(angle);
        path.lineTo((float) x1, (float) y1);
        path.lineTo((float) x1, (float) (y1 + lengthOfTriangle));
        canvas.drawPath(path, mCurrentLevelTextPaint);
    }

    /**
     * Calculates risk level based on y-coordinate
     *
     * @param currentY     The current y-coordinate where the pointer is.
     * @param minY         The min y-coordinate where the pointer can be.
     * @param maxY         The max y-coordinate where the pointer can be.
     * @param minRiskValue The min available risk level.
     * @param maxRiskValue The max available risk level.
     */
    private float getLevel(float currentY, float minY, float maxY, float minRiskValue, float maxRiskValue) {
        return (maxRiskValue - minRiskValue) * (currentY - minY) / (minY - maxY) + maxRiskValue;
    }

    /**
     * Calculates the y-coordinate of pointer based on given risk level.
     *
     * @param currentLevel The risk level.
     * @param minY         The min y-coordinate where the pointer can be.
     * @param maxY         The max y-coordinate where the pointer can be.
     * @param minRiskValue The min available risk level.
     * @param maxRiskValue The max available risk level.
     */
    private float getCurrentY(float currentLevel, float minY, float maxY, float minRiskValue, float maxRiskValue) {
        return (currentLevel - maxRiskValue) * (minY - maxY) / (maxRiskValue - minRiskValue) + minY;
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

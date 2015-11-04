package com.risklevel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class RiskLevelWidget extends FrameLayout {

    public RiskLevelWidget(final Context context) {
        super(context);
        init();
    }

    public RiskLevelWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RiskLevelWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // empty yet
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        View firstChild = getChildAt(0);
        int firstChildWidth = firstChild.getMeasuredWidth();
        int firstChildHeight = firstChild.getMeasuredHeight();
        View secondChild = getChildAt(1);
        secondChild.layout(firstChildWidth, 0, r, firstChildHeight);
        LevelIndicator indicator = (LevelIndicator) secondChild;
        firstChild.layout(0, 0, firstChildWidth, firstChildHeight);//todo
        float diameter = secondChild.getMeasuredHeight() / LevelIndicator.COUNT - indicator.padding;
        firstChild.setPadding(0, indicator.getValuePadding() - (int) diameter / 2, 0, indicator.getValuePadding() - (int) diameter / 2);
    }
}

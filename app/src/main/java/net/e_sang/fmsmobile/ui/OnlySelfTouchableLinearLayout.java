package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class OnlySelfTouchableLinearLayout extends LinearLayout {
    private float xDistance, yDistance, lastX, lastY;

    public OnlySelfTouchableLinearLayout(Context context) {
        super(context);
    }

    public OnlySelfTouchableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OnlySelfTouchableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;        // child view에 touch 전달 않됨
    }
}

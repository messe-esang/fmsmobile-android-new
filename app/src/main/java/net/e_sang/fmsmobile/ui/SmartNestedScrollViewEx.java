package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SmartNestedScrollViewEx extends NestedScrollViewEx {
    private float xDistance, yDistance, lastX, lastY;

    public SmartNestedScrollViewEx(Context context) {
        super(context);
    }

    public SmartNestedScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartNestedScrollViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                lastX = ev.getX();
                lastY = ev.getY();
                // This is very important line that fixes
                computeScroll();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                xDistance += Math.abs(curX - lastX);
                yDistance += Math.abs(curY - lastY);
                lastX = curX;
                lastY = curY;
                if (xDistance > yDistance) {        // 좌우(가로) 드래그시 child에 touch event 전달
                    return false;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}

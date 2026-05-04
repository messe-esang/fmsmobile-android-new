package net.e_sang.fmsmobile.ui;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class OnlySelfTouchableRecyclerView extends RecyclerView {

    public OnlySelfTouchableRecyclerView(Context context) {
        super(context);
    }

    public OnlySelfTouchableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OnlySelfTouchableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;        // child view에 touch 전달 않됨
    }
}

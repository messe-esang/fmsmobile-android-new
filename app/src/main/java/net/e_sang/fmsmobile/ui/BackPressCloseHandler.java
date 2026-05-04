package net.e_sang.fmsmobile.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import net.e_sang.fmsmobile.R;

public class BackPressCloseHandler {
    private long backKeyClickTime = 0;
    private Activity activity;
    private View mainLayout;

    public BackPressCloseHandler(Activity activity, View layout) {
        this.activity = activity;
        this.mainLayout = layout;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyClickTime + 2000) {
            backKeyClickTime = System.currentTimeMillis();
            showToast();
            return;
        }
        if (System.currentTimeMillis() <= backKeyClickTime + 2000) {
            activity.finish();
        }
    }

    public void showToast() {
        Snackbar snackbar = Snackbar.make(mainLayout, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.color_primary));
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, R.color.color_primary));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        View snackbarView = snackbar.getView();
        TextView snackbarText = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarText.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
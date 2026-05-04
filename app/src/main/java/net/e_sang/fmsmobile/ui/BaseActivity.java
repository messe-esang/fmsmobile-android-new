package net.e_sang.fmsmobile.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ContentFrameLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {
    private static ArrayList<Activity> mRemovableActivityList = new ArrayList<Activity>();
    private TextView mWatermarkView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE); // 화면 캡처 막기
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    protected void applyInsets() {
        View root = findViewById(R.id.main);

        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        String[] exceptClasses = {
                "ui.SplashActivity",
                "ui.LoginActivity",
        };
        String name = getLocalClassName();
        boolean isExcept = false;
        for (String exceptClass : exceptClasses) {
            if (exceptClass.equals(name)) {
                isExcept = true;
            }
        }
        if (isExcept == false) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            if (userInfo != null) {
                String text = "";
                if (userInfo.TEAM_NAME.equals("")) {
                    text = String.format("%s %s, 회사정보 유출 금지!!", userInfo.DEPT_NAME, userInfo.USER_NAME);
                } else {
                    text = String.format("%s %s, 회사정보 유출 금지!!", userInfo.TEAM_NAME, userInfo.USER_NAME);
                }

                if (PrefKit.getTestMode(this)) {
                    text = "[테스트 모드] " + text;
                }
                if (Kit.TOKEN_REGISTRATION_TO_SERVER == false) {
                    text = "[토큰갱신X] " + text;
                }
                showWatermarkView(text);
            }
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        //Kit.log(LogType.EVENT, "onStop");

        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            }
        }
    }

    protected void setStatusColor(int color, boolean isLight) {
        if (isLight) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(color);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(color);
            }
        }
    }

    protected void addRemovableActivity(Activity activity) {
        mRemovableActivityList.add(activity);
    }

    protected void finishRemovableActivities() {
        for (Activity activity : mRemovableActivityList) {
            activity.finish();
        }
        mRemovableActivityList.clear();
    }

    protected void showWatermarkView(final String text) {
        try {

            final String tag = "watermark";

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mWatermarkView == null) {
                        mWatermarkView = (TextView) View.inflate(BaseActivity.this, R.layout.layout_watermark, null);
                        mWatermarkView.setTag(tag);
                    }

                    mWatermarkView.setText(text);

                    View content = getWindow().getDecorView().findViewById(android.R.id.content);

                    if (content instanceof FrameLayout) {

                        FrameLayout frameLayout = (FrameLayout) content;

                        if (frameLayout.findViewWithTag(tag) == null) {

                            FrameLayout.LayoutParams params =
                                    new FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT
                                    );

                            int navBar = getNavigationBarHeight();
                            params.bottomMargin = navBar;

                            mWatermarkView.setLayoutParams(params);

                            frameLayout.addView(mWatermarkView);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNavigationBarHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? resources.getDimensionPixelSize(resourceId) : 0;
    }
}

package net.e_sang.fmsmobile;

import android.app.Application;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MyApplication extends Application {
    //
//    public static RequestOptions REQUEST_OPTIONS = new RequestOptions()
//            .error(R.drawable.ico_noimage)
//            .diskCacheStrategy(DiskCacheStrategy.ALL)   // default;
//            .skipMemoryCache(true);

    public static final int PAGE_VIEW_COUNT = 20;
    public static boolean AlertDialog_Check = false;
    public static boolean Notification_Check = false;
    public static FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}

/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.e_sang.fmsmobile.kit;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.appcompat.app.AlertDialog;

import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.*;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.ui.MainActivity;

import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.provider.Settings;
import android.widget.Toast;


public final class Kit {
    public static final String TAG = "fmsmobile";
    //	public static final boolean IsDEBUG = true;
    public static final boolean IsDEBUG = false;
    //    public static final boolean IsDevServer = false;
    public static final boolean IsDevServer = IsDEBUG;
    public static final String WEB_API_VER = "1";
    public static final boolean TOKEN_REGISTRATION_TO_SERVER = true;
//    public static final boolean TOKEN_REGISTRATION_TO_SERVER = false;

    public enum LogType {
        NONE,        // NONE, EVENT, VALUE, TEST 찍음
        ALL,        // 전부
        TEST,
        EVENT,
        AUTOMATA,
        LAYOUT,
        VALUE,
        TRACKING,
        TELKIT,
    }

    public static final LogType LOGTYPE = LogType.NONE;

    public static void log(String msg) {
        log(LogType.NONE, msg);
    }

    public static void log(LogType logType, String msg) {
        if (IsDEBUG) {
            if (LOGTYPE == LogType.ALL) {
                Log.i(TAG, msg);
            } else {
                if (LOGTYPE == LogType.NONE) {
                    if ((logType == LogType.NONE)
                            || (logType == LogType.EVENT)
                            || (logType == LogType.VALUE)
                            || (logType == LogType.TEST)
                            || (logType == LogType.TELKIT))
                        if ((logType == LogType.TEST)) {
                            Log.i(TAG, "========================================= " + msg);
                        } else {
                            Log.i(TAG, msg);
                        }
                } else {
                    if (LOGTYPE == logType) {
                        if ((logType == LogType.TEST)) {
                            Log.i(TAG, "========================================= " + msg);
                        } else {
                            Log.i(TAG, msg);
                        }
                    }
                }
            }
        }
    }

    public static void loglong(String msg, int max) {
        loglong(LogType.NONE, msg, max);
    }

    public static void loglong(LogType logType, String msg, int max) {
        for (int i = 0; i <= msg.length() / max; i++) {
            int start = i * max;
            int end = (i + 1) * max;
            end = end > msg.length() ? msg.length() : end;
            log(logType, msg.substring(start, end));
        }
    }

    public static void showAlertDialog(Context context, String title, String msg, String positiveButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public static void showAlertDialog(Context context, String msg, String positiveButtonText) {
        showAlertDialog(context, getAppName(context), msg, positiveButtonText);
    }

    public static void showAlertExitDialog(final Activity activity) {
        String appName = getAppName(activity);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setTitle(appName);
        alertBuilder.setMessage(appName + "을(를) 종료하시겠습니까?");
        alertBuilder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                        //System.exit(0);
                    }
                });
        alertBuilder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertBuilder.show();
    }

    public static void showAlertVersionDialog(Context context) {
        String appName = Kit.getAppName(context);
        String version = Kit.getPackageVersionName(context);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setIcon(R.mipmap.ic_launcher);
        alertBuilder.setTitle(appName);
        alertBuilder.setMessage(String.format("%s %s", appName, version));
        alertBuilder.setPositiveButton("닫기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertBuilder.show();
    }

    public static int getDipValue(Context context, int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, context.getResources().getDisplayMetrics());
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float pxToDp(Context context, int px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

//	static int pxToDp(Context context, float px) {
//		float density = context.getResources().getDisplayMetrics().density;
//		return Math.round(px / density);
//	}

    public static int dpToPx(Context context, float dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static int spToPx(Context context, float sp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static Map<String, String> getQueryMap(String query) {
        return getQueryMap(query, "&");
    }

    public static Map<String, String> getQueryMap(String query, String seperator) {
        if (query == null)
            return null;

        String[] params = query.split(seperator);
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = "";
            if (param.endsWith("=") == false) {
                value = param.split("=")[1];
            }
            map.put(name, value);
        }
        return map;
    }

    public static void execBrowser(Context context, String urlStr) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static void execBrowserEx(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    public static void startIntent(Context context, String uri, String dataAndType) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setDataAndType(Uri.parse(uri), dataAndType);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    public static void openPlayStore(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", getPackageName(context))));
        intent.setData(Uri.parse(String.format("market://details?id=%s", getPackageName(context))));
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);       // 이 플래그 적용시 구글플레이에서 [업데이트]가 아닌 [열기]가 나옴
        context.startActivity(intent);
    }

    // 특정 파일만 스캔
    public static void mediaScanFile(Context context, String uri) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uri)));
    }

    public static void mediaScanFile(Context context, final String[] filePath, final String[] mime) {
        MediaScannerConnection.scanFile(context, filePath, mime, null);
    }

    // 폴더 스캔
    public static void mediaScanDir(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }

    public static void showNotification(Context context, int icon, String tickerText, String title, String message, Intent intent) {
        showNotification(context, (int) Calendar.getInstance().getTimeInMillis(), icon, tickerText, title, message, intent);
    }

    public static void showNotification(Context context, int id, int icon, String tickerText, String title, String message, Intent intent) {
		/*
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // construct the Notification object.
        Notification notif = new Notification(icon, tickerText, System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(context, title, message, contentIntent);

        // We'll have this notification do the default sound, vibration, and led.
        // Note that if you want any of these behaviors, you should always have
        // a preference for the user to turn them off.
        //notif.defaults = Notification.DEFAULT_ALL;	// 오류 발생 (ICS 이상에서?)

        // 알림 메시지 터치시 자동 삭제
        notif.flags = Notification.FLAG_AUTO_CANCEL;

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(id, notif);
        */


        // look up the notification manager service
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        long when = System.currentTimeMillis();

        NotificationCompat.Builder builder = new Builder(context);
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(when)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo(title);

        Notification notif = builder.build();

        // 알림 메시지 터치시 자동 삭제
        notif.flags = Notification.FLAG_AUTO_CANCEL;

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(id, notif);
    }

    public static String getValidFileName(String fileName) {
        final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '#', '+', '[', ']', '\'', ' '};
        for (char c : ILLEGAL_CHARACTERS) {
            fileName = fileName.replace(c, '_');
        }

        return fileName;
    }

    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0 Byte(s)";

        final String[] units = new String[]{"Byte(s)", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        //return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    public static String readableKiloMegaGigaTera(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[]{"", "K", "M", "G", "T"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + units[digitGroups];
    }

    public static String readableTimeSec(long totalSec) {
        if (totalSec <= 0)
            return "00:00";

        int hours = (int) ((float) totalSec / (60.0 * 60.0));
        int minutes = (int) (((float) totalSec % (60.0 * 60.0)) / 60.0);
        int seconds = (int) ((float) totalSec % 60.0);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    public static String readableTimeSecShort(long totalSec) {
        if (totalSec <= 0)
            return "00:00";

        int minutes = (int) (((float) totalSec) / 60.0);
        int seconds = (int) ((float) totalSec % 60.0);

        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String readableTimeSecLong(long totalSec) {
        if (totalSec <= 0)
            return "00:00:00";

        int hours = (int) ((float) totalSec / (60.0 * 60.0));
        int minutes = (int) (((float) totalSec % (60.0 * 60.0)) / 60.0);
        int seconds = (int) ((float) totalSec % 60.0);

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("00:%02d:%02d", minutes, seconds);
    }

    // 화폐 단위 콤마 찍기
    public static final String convertCurrencyStr(long num) {
        DecimalFormat df = new DecimalFormat("#,##0");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(',');
        df.setGroupingSize(3);
        df.setDecimalFormatSymbols(dfs);

        String result = df.format(num).toString();
        return result;
    }

    // 화폐 단위 콤마 찍기
    public static final String convertCurrencyStr(double num) {
        DecimalFormat df = new DecimalFormat("#,##0");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator(',');
        df.setGroupingSize(3);
        df.setDecimalFormatSymbols(dfs);

        String result = df.format(num).toString();
        return result;
    }

    public static final String readAssetFile(Context context, String file) throws IOException {
        InputStream is = context.getAssets().open(file);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String text = new String(buffer);

        return text;
    }

    public static final String toHexStr(int color) {
        return String.format("%06X", 0xFFFFFF & color);
    }

    public static String getDownloadFolder(Context context) {
        String downloadFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Download";
        //log("getDownloadFolder::downloadFolder = " + downloadFolder);
        return downloadFolder;
    }

    public static String getPackageVersionName(Context context) {
        String name = "";

        // 패키지 정보
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            name = packageInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    public static String getPackageName(Context context) {
        String name = "";

        // 패키지 정보
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            name = packageInfo.packageName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    public static void hideStatusBar(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
    }

    public static void showStatusBar(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
    }

    public static String getAppName(Context context) {
        return context.getResources().getString(R.string.app_name);
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static int pathCanWritable(File path) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (path.canWrite()) {
                return 0;
            } else {
                log("Path not writable");
                return 1;
            }
        } else {
            log("Path not mounted");
            return 2;
        }
    }

    public static void copyAssets(Context context, String assetPath, String targetDir) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetPath);
        } catch (IOException e) {
            log(String.format("Failed to get asset file list (%s)", e.getLocalizedMessage()));
        }
        log("files.length = " + files.length);
        for (String filename : files) {
            Kit.log("copyAsset::filename = " + filename);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(targetDir, filename);
                out = new FileOutputStream(outFile);

                // copy file
                copyFile(in, out);
            } catch (IOException e) {
                log(String.format("Failed to copy asset file: %s (%s)", filename, e.getLocalizedMessage()));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    public static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        log(LogType.TEST, "copyAsset::fromAssetPath = " + fromAssetPath);
        log(LogType.TEST, "copyAsset::toPath = " + toPath);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void deleteAllFilesAndDirs(File file) throws SecurityException {
        File[] f = file.listFiles();

        for (int i = 0; i < f.length; i++) {
            if (f[i].isDirectory()) {
                deleteAllFilesAndDirs(f[i]);
            } else {
                f[i].delete();
            }
        }
    }

    public static void execChmod(String filepath, String code) {
        log("Trying to chmod '" + filepath + "' to: " + code);
        try {
            Runtime.getRuntime().exec("chmod " + code + " " + filepath);
            SystemClock.sleep(500);
        } catch (IOException e) {
            log(String.format("Error changing file permissions! (%s)", e.getLocalizedMessage()));
        }
    }

    public static String removeExtension(String name) {
        if (name == null)
            return null;
        return name.replaceAll("\\.[^\\.]+$", "");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blur(Context context, Bitmap sentBitmap, int radius) {
        if (VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius); //0.0f ~ 25.0f
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);

            return bitmap;
        } else {
            return null;
        }
    }

    public static void brandGlowEffect(Context context, int brandColor) {
        try {
            //glow
            int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            if (glowDrawableId > 0) {
                Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
                androidGlow.setColorFilter(brandColor, PorterDuff.Mode.MULTIPLY);
            }
            //edge
            int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            if (edgeDrawableId > 0) {
                Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
                androidEdge.setColorFilter(brandColor, PorterDuff.Mode.MULTIPLY);
            }
        } catch (Exception e) {
        }
    }

    public static boolean isGpsEnabled(Context context) {
        if (VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Secure.getString(context.getContentResolver(),
                    Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Secure.getInt(context.getContentResolver(),
                        Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {

                case Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Secure.LOCATION_MODE_BATTERY_SAVING:
                case Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }

    public static void effect1(View view) {
        ObjectAnimator animator1 = new ObjectAnimator();
        animator1.setTarget(view);
        animator1.setPropertyName("translationY");
        animator1.setFloatValues(0f, -100f);
        animator1.setDuration(1000);

        animator1.setInterpolator(new LinearInterpolator());
        //animator1.setEvaluator(EasingFunction.BOUNCE_OUT); //use `EasingFunction.BOUNCE_OUT` as `TypeEvaluator`

        animator1.start();
    }

    public static void effect2(Context context, View view, int animationResId) {
        Animation animation = AnimationUtils.loadAnimation(context, animationResId);
        view.startAnimation(animation);
    }

    public static Object convertViewToDrawable(View view) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(viewBmp);
    }

    public static String getDeviceInfoParam(Context context) {
//		&vername=0.2.4.2					Version Name:
//		&vercode=30							Version Code:
//		&locale=Ko-kr						Locale
//		&model=shv_e160k					모델명
//		&carrier=kt							통신사
//		&osver_code							OS Version Code
//		&display=1080x1920					해상도

        String versionName = "";
        int versionCode = 0;
        String locale = "";
        String model = "";
        String carrier = "";
        int osVersionCode = 0;
        String fingerPrint = "";

        // 패키지 정보
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // locale
        locale = context.getResources().getConfiguration().locale.toString();

        // 모델
        model = Build.MODEL;

        // 통신사
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        carrier = tm.getNetworkOperatorName();

        // OS Version Code
        osVersionCode = VERSION.SDK_INT;

        // 해상도
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        // Build Fingerprint
        fingerPrint = Build.FINGERPRINT;

        try {
            versionName = URLEncoder.encode(versionName, "utf-8");
            locale = URLEncoder.encode(locale, "utf-8");
            model = URLEncoder.encode(model, "utf-8");
            carrier = URLEncoder.encode(carrier, "utf-8");
            fingerPrint = URLEncoder.encode(fingerPrint, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String params = String.format("vername=%s&vercode=%d&locale=%s&model=%s&carrier=%s&osver_code=%d&display=%dx%d&fingerprint=%s",
                versionName,
                versionCode,
                locale,
                model,
                carrier,
                osVersionCode,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                fingerPrint);

        return params;
    }

    public static void setGlobalFont(Context context, View view, String fontFileName) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                int len = vg.getChildCount();
                for (int i = 0; i < len; i++) {
                    View v = vg.getChildAt(i);
                    if (v instanceof TextView) {
                        ((TextView) v).setTypeface(Typeface.createFromAsset(context.getAssets(), fontFileName));
                    }
                    setGlobalFont(context, v, fontFileName);
                }
            }
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String getClassName(Context context, String packageName) {
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);

        String className = "";
        Iterator<ResolveInfo> iterator = list.iterator();
        while (iterator.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) iterator.next();
            if (resolveInfo.activityInfo.packageName.equals(packageName)) {
                className = resolveInfo.activityInfo.name;
                break;
            }
        }

        return className;
    }

    public static boolean startActivityFromPackageName(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setClassName(packageName, getClassName(context, packageName));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            return false;
        }

        return true;
    }

    public static boolean isNotNullNotEmpty(String str) {
        return (str != null && !str.isEmpty());
    }

    public static void printHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(getPackageName(context), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                log(LogType.VALUE, "KeyHash:" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkEmailForm(String email) {
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(email);
        return matcher.matches();
    }

    public static boolean isXSSPreventSafeUrl(String url) {
        if (isNotNullNotEmpty(url)) {
            Matcher matcher = Patterns.WEB_URL.matcher(url);
            if (matcher.matches()) {
                Uri uri = Uri.parse(url);
                if (!uri.getScheme().equals("file")) {
                    return true;
                }
            }
        }

        return false;
    }

    // 외장메모리 sdcard 사용가능한지에 대한 여부 판단
    public static boolean isStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (!requireWriteAccess &&
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    // 사용가능한 외장 메모리 크기를 가져온다
    public static long getExternalMemorySize() {
        if (isStorage(true) == true) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    // 사용가능한 내장 메모리 크기를 가져온다
    public static long getInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public static boolean checkAvailableStorage(long limitedSize) {
        try {
            long size = getInternalMemorySize();
            if (size >= 0 && size < limitedSize) {
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return true;
    }

    public static boolean isNetworkConnected(Context context) {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addLinksPhoneNumbers(TextView txtview) {
        final String PHONE_REGEX = "(0|\\+?82[-,\\s.]*)1[016-9][-,\\s.]*(?:\\d{3}|\\d{4})[-,\\s.]*\\d{4}|(\\d{2}|\\d{3}|\\d{4})[)\\-. ]*?(\\d{3}|\\d{4})[\\-. ]*?(\\d{4})";
        Pattern pattern = Pattern.compile(PHONE_REGEX, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(txtview.getText());
        if (matcher.find()) {
            Linkify.addLinks(txtview, Linkify.PHONE_NUMBERS);
        }
    }

    public static String getPreviousDate() {
        try {
            long now = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(now);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, -1);

            return "* " + sdf.format(cal.getTime()) + " 기준";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getNewRate(String new_cnt, String total_cnt) {
        try {

            if (new_cnt.equals("0") || total_cnt.equals("0")) {
                return "0";
            }
            int iTotal_cnt = Integer.parseInt(total_cnt.replaceAll(" ", ""));
            int iNew_cnt = Integer.parseInt(new_cnt.replaceAll(" ", ""));
            String new_rate = String.valueOf(((double) iNew_cnt / (double) iTotal_cnt) * 100.0);
            int idx = new_rate.indexOf(".");
            return new_rate.substring(0, idx);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static class GoogleADIDHelper {

        public static void fetchAdIdAsync(Context context) {
            Context appContext = context.getApplicationContext();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler mainHandler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                String adId = "";
                try {
                    adId = AdvertisingIdClient.getAdvertisingIdInfo(appContext).getId();
                    Log.e("Kit", "GoogleADIDHelper adid : " + adId);
                } catch (IllegalStateException ex) {
                    Log.e("Kit", "GoogleADIDHelper IllegalStateException" + ex);
                } catch (GooglePlayServicesRepairableException ex) {
                    Log.e("Kit", "GoogleADIDHelper RepairableException" + ex);
                } catch (IOException ex) {
                    Log.e("Kit", "GoogleADIDHelper IOException" + ex);
                } catch (GooglePlayServicesNotAvailableException ex) {
                    Log.e("Kit", "GoogleADIDHelper NotAvailableException" + ex);
                } catch (Exception ex) {
                    Log.e("Kit", "GoogleADIDHelper Exception" + ex);
                }

                String finalAdId = adId;
                mainHandler.post(() -> {
                    if (isNotNullNotEmpty(finalAdId)) {
                        PrefKit.setUserAdId(appContext, finalAdId);
                        Log.e("Kit", "GoogleADIDHelper onPost adid : " + finalAdId);
                    }
                });
            });
        }
    }

    public static String getBase64encode(Context context, String content) throws UnsupportedEncodingException {
        String api_token;
        byte[] data = content.getBytes("UTF-8");
        api_token = Base64.encodeToString(data, Base64.DEFAULT).trim();
        PrefKit.setApiToken(context, api_token);
        return api_token;
    }

    public static class DeveloperOptionsChecker {
        //개발자 옵션 체크
        public static boolean isDeveloperOptionsEnabled(Context context) {
            try {
                int devOptions = Settings.Global.getInt(
                        context.getContentResolver(),
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
                );
                return devOptions == 1;
            } catch (Settings.SettingNotFoundException e) {
                Log.e("DevOptionsChecker", "Setting not found: " + e.getMessage());
                return false;
            }
        }
    }

    public static class ActivityManager {

        private static final List<Activity> activityList = new ArrayList<>();

        public static void register(Activity activity) {
            activityList.add(activity);
        }

        public static void unregister(Activity activity) {
            activityList.remove(activity);
        }

        public static void finishActivity(Class<?> cls) {
            for (Activity activity : new ArrayList<>(activityList)) {
                if (activity.getClass().equals(cls)) {
                    activity.finish();
                }
            }
        }

        public static void finishAllExcept(Class<?> cls) {
            for (Activity activity : new ArrayList<>(activityList)) {
                if (!activity.getClass().equals(cls)) {
                    activity.finish();
                }
            }
        }
    }

//    개발자 옵션 체크 사용방법
//    boolean isEnabled = Kit.DeveloperOptionsChecker.isDeveloperOptionsEnabled(MainActivity.this);
//        if (isEnabled) {
//        Log.e(TAG, "개발자 옵션이 켜져 있습니다.");
//        Toast.makeText(MainActivity.this, "개발자 옵션이 켜져 있습니다.", Toast.LENGTH_SHORT).show();
//    } else {
//        Log.e(TAG, "개발자 옵션이 꺼져 있습니다.");
//        Toast.makeText(MainActivity.this, "개발자 옵션이 꺼져 있습니다.", Toast.LENGTH_SHORT).show();
//    }

    public static boolean isContactExists(Context context, String name, String phone) {

        String normalizedInput = normalizePhone(phone);

        // 🔥 길이 필터 (핵심)
        if (normalizedInput.length() < 9) {
            return false;
        }

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String displayName = cursor.getString(0);
                String dbPhone = cursor.getString(1);

                String normalizedDb = normalizePhone(dbPhone);

                // 🔥 완전 일치만 허용
                if (normalizedDb.equals(normalizedInput)
                        && displayName != null
                        && displayName.trim().equalsIgnoreCase(name.trim())) {

                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }

        return false;
    }

    private static String normalizePhone(String phone) {

        if (phone == null) return "";

        // 숫자만 남김
        String number = phone.replaceAll("[^0-9]", "");

        // +82 → 0 변환
        if (number.startsWith("82")) {
            number = "0" + number.substring(2);
        }

        return number;
    }

    public abstract static class OnSingleClickListener implements View.OnClickListener {
        private static final long DEFAULT_INTERVAL = 1000; // 1초
        private long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime > DEFAULT_INTERVAL) {
                lastClickTime = currentTime;
                onSingleClick(v);
            }
        }

        public abstract void onSingleClick(View v);
    }
}

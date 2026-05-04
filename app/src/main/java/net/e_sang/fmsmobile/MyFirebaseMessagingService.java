package net.e_sang.fmsmobile;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.Kit.LogType;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.NoticeActivity;
import net.e_sang.fmsmobile.ui.SplashActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Kit.log(LogType.VALUE, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        Map<String, String> data = remoteMessage.getData();
        Kit.log(LogType.VALUE, "onMessageReceived::data: " + data);
        if (data.size() > 0) {
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
            }
        }

        // Check if message contains a notification payload.
        // 사용 안함, 헤드업 알림 동작 않됨
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Kit.log(LogType.VALUE, "onMessageReceived::notification: " + notification);
        if (notification != null) {
            Kit.log(LogType.VALUE, "onMessageReceived::getTitle: " + notification.getTitle());
            Kit.log(LogType.VALUE, "onMessageReceived::getBody: " + notification.getBody());
            Kit.log(LogType.VALUE, "onMessageReceived::getTitleLocalizationKey: " + notification.getTitleLocalizationKey());
            Kit.log(LogType.VALUE, "onMessageReceived::getBodyLocalizationKey: " + notification.getBodyLocalizationKey());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
//        showNotification(-1, notification.getTitle(), notification.getBody());
        String title = "", body = "", push_type = "";
        if (data != null) {
            title = data.get("title");
            body = data.get("body");
            push_type = data.get("push_type");
        }

        showNotification(-1, title, body, push_type);
    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Kit.log(LogType.VALUE, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            TelKit.tokenRegistrationToServer(this, token, userInfo.SYS_ID, userInfo.LOGIN_ID);
        }
    }

    public void showNotification(int notif_id, String title, String body, String push_type) {
        if (notif_id < 0)
            notif_id = (int) Calendar.getInstance().getTimeInMillis();

        Intent intent = null;
        if (isAppIsInBackground(this)) {
            intent = new Intent(this, SplashActivity.class);    // launcher 가 SplashActivity 이므로 SplashActivity 실행 후 열림
            intent.putExtra(Extra.KEY_NOTI_CHECK, true);
            MyApplication.Notification_Check = true;
        } else {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            if (userInfo != null) {
                intent = new Intent(this, NoticeActivity.class);
            } else {
                intent = new Intent(this, SplashActivity.class);    // launcher 가 SplashActivity 이므로 SplashActivity 실행 후 열림
                MyApplication.Notification_Check = true;
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // The PendingIntent to launch our activity if the user selects this
        // notification.  Note the use of FLAG_CANCEL_CURRENT so that, if there
        // is already an active matching pending intent, cancel it and replace
        // it with the new array of Intents.
        PendingIntent contentIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }else {
            contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        long when = System.currentTimeMillis();
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        int smallIcon = useWhiteIcon ? R.drawable.ic_notification : R.mipmap.ic_launcher;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.bigText(body);

        Notification.Builder builder;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Notification
            final String channel_id = getResources().getString(R.string.notification_channel_id);
            final String channel_name = getResources().getString(R.string.notification_channel_name);
            final String channel_description = getResources().getString(R.string.notification_channel_description);
            // 헤드업 알림을 트리거할 수 있는 조건
            // - 사용자 액티비티가 전체 화면 모드이거나(앱이 fullScreenIntent를 사용할 경우)
            // - 알림의 우선 순위가 높고 벨소리나 진동을 사용할 경우
//            final int importance = NotificationManager.IMPORTANCE_DEFAULT;
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            channel.setDescription(channel_description);
//            channel.enableLights(true);
//            channel.setLightColor(getResources().getColor(R.color.color_primary));
            channel.enableVibration(true);
//            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            builder = new Notification.Builder(this, channel.getId());
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(when)
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSound(defaultSoundUri)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(bigTextStyle)
                .setContentIntent(contentIntent);
//                .setContentInfo(title);       //  the large text at the right-hand side of the notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(getResources().getColor(R.color.color_primary));
        }

        Notification notif = builder.build();

        // 알림 메시지 터치시 자동 삭제
        notif.flags = Notification.FLAG_AUTO_CANCEL;

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(notif_id, notif);
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }

        return isInBackground;
    }
}

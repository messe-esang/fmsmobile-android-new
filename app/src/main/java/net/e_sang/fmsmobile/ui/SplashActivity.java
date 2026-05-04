package net.e_sang.fmsmobile.ui;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.e_sang.fmsmobile.BuildConfig;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.VersionDevKit;

import static net.e_sang.fmsmobile.kit.Kit.isNetworkConnected;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

public class SplashActivity extends BaseLoginActivity implements VersionDevKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    public static final int REQUEST_CODE_PERMISSION_INSTALL_PACKAGE = 1001;
    public static final int REQUEST_CODE_GET_UNKNOWN_APP_SOURCES = 1002;
    public static final int PERMISSION_REQUEST_CODE = 2296;
    private boolean NOTI_CHECK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_F8F8F8);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        applyInsets();
        Kit.GoogleADIDHelper.fetchAdIdAsync(SplashActivity.this);

        if (isNetworkConnected(SplashActivity.this)) {
            // 권한 확인
            String[] PermissionList = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PermissionList = new String[]{Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS};
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                PermissionList = new String[]{Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS};
            } else {
                PermissionList = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS};
            }
            checkPermissions(PermissionList);
        } else {
            if (!SplashActivity.this.isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                builder.show();
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    protected void checkPermissions(String[] PermissionList) {
        TedPermission.create()
                .setDeniedMessage("설정에서 앱 권한을 모두 허용해 주세요.")
                .setRationaleConfirmText("확인")
                .setDeniedCloseButtonText("취소")
                .setGotoSettingButtonText("설정")
                .setPermissionListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted() {
                        NextLevel();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(SplashActivity.this, "앱 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 일부서비스의 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
                        NextLevel();
                    }
                }).setPermissions(PermissionList)
                .check();
    }

    private void NextLevel() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestPermission();
            } else {
                new VersionDevKit(SplashActivity.this, SplashActivity.this).checkVersion();
            }
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        }
    }

    protected void next() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo == null) {
            // 메인화면으로 이동
            Intent intent = getIntent(); /*데이터 수신*/
            NOTI_CHECK = intent.getBooleanExtra(Extra.KEY_NOTI_CHECK, false);
            Intent intent_main = new Intent(this, LoginActivity.class);
            intent_main.putExtra(Extra.KEY_NOTI_CHECK, NOTI_CHECK);
            startActivity(intent_main);
            //Kit.startActivity(this, LoginActivity.class);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
//            if ("exco".equals(BuildConfig.APP_FLAVOR)) {
            tryUserAutoLogin(userInfo.LOGIN_ID, userInfo.LOGIN_PWD, "", null);
//            } else {
//                tryUserLogin(userInfo.LOGIN_ID, userInfo.LOGIN_PWD, "", null);
//            }
        }
    }

    // VersionDevKit.OnResultListener
    @Override
    public void onResult(boolean result) {
        if (result) {
            next();
        } else {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setMessage("앱 버전을 확인할 수 없습니다.\n앱 삭제 후 다시 다운로드 및 설치해 주세요.");
            builder.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.finishAffinity(SplashActivity.this);
                        }
                    });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_INSTALL_PACKAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                VersionDevKit.installAPK(this);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_GET_UNKNOWN_APP_SOURCES);
            }
        }
    }

    private void installAPK() {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                VersionDevKit.installAPK(this);
            } else {
                // '설치가 차단됨' 얼럿 X
                requestPermissions(new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, REQUEST_CODE_PERMISSION_INSTALL_PACKAGE);     // '설치가 차단됨' 얼럿 뜨지 않음
            }
        } else {
            VersionDevKit.installAPK(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_GET_UNKNOWN_APP_SOURCES:
                installAPK();
                break;
            case PERMISSION_REQUEST_CODE:
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // perform action when allow permission success
                        new VersionDevKit(SplashActivity.this, SplashActivity.this).checkVersion();
                    } else {
                        Toast.makeText(this, "업데이트를 설치할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        SplashActivity.this.finish();
                    }
                }
                break;

            default:
                break;
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        moveTaskToBack(true);
//        finishAffinity();
//        android.os.Process.killProcess(android.os.Process.myPid());
//    }
}

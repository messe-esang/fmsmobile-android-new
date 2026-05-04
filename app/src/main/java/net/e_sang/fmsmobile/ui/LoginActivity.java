package net.e_sang.fmsmobile.ui;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.e_sang.fmsmobile.R;

import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

public class LoginActivity extends BaseLoginActivity implements TelKit.OnResultListener {
    private EditText editID;
    private Button btnLogin;
    private int mManyTouchCount = 0;

    public static boolean LIMIT_MONTH = false;
    public static EditText editPW, editNewPW;

    private Handler mManyTouchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mManyTouchCount = 0;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_F8F8F8);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        applyInsets();
        editID = findViewById(R.id.editID);
        editPW = findViewById(R.id.editPW);
        editNewPW = findViewById(R.id.editNewPW);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kit.hideSoftKeyboard(LoginActivity.this);
                checkUserLogin();
            }
        });

        editPW.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Kit.hideSoftKeyboard(LoginActivity.this);
                    checkUserLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.imgLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManyTouchCount++;
                if (mManyTouchCount >= 10) {
                    mManyTouchHandler.removeMessages(0);
                    mManyTouchCount = 0;
                    boolean isTestMode = PrefKit.getTestMode(LoginActivity.this);
                    isTestMode = !isTestMode;
                    PrefKit.setTestMode(LoginActivity.this, isTestMode);

                    setResult(RESULT_OK);

                    String msg = String.format("테스트 모드가 [%s] 되었습니다.", isTestMode ? "활성화" : "비활성화");
                    Snackbar.make(findViewById(R.id.main), msg, Snackbar.LENGTH_LONG).show();
                } else {
                    mManyTouchHandler.removeMessages(0);
                    mManyTouchHandler.sendEmptyMessageDelayed(0, 2000);
                }
            }
        });
        Log.e("LoginActivity", "LIMIT_MONTH : " + LIMIT_MONTH);
        if (LIMIT_MONTH) {
            editNewPW.setVisibility(View.VISIBLE);
        } else {
            editNewPW.setVisibility(View.GONE);
        }

        editNewPW.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Kit.hideSoftKeyboard(LoginActivity.this);
                    checkUserLogin();
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                moveTaskToBack(true);
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    protected void checkUserLogin() {

        String id = editID.getText().toString();
        if (id.isEmpty()) {
            Kit.showAlertDialog(this, "", "아이디를 입력해주세요.", "확인");
            return;
        }

        String pw = editPW.getText().toString();
        if (pw.isEmpty()) {
            Kit.showAlertDialog(this, "", "비밀번호를 입력해주세요.", "확인");
            return;
        }

        String newpw = "";
        if (LIMIT_MONTH) {
            newpw = editNewPW.getText().toString();
            if (newpw.isEmpty()) {
                Kit.showAlertDialog(this, "", "신규 비밀번호를 입력해주세요.", "확인");
                return;
            }
        }

        tryUserLogin(id, pw, newpw, findViewById(R.id.progressBar_layout));
    }

    @Override
    protected void onDestroy() {
        LIMIT_MONTH = false;
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        if (LIMIT_MONTH) {
//            moveTaskToBack(true);
//            finishAffinity();
//            android.os.Process.killProcess(android.os.Process.myPid());
//        }
//    }
}

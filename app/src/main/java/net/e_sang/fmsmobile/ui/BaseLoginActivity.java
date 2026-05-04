package net.e_sang.fmsmobile.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static net.e_sang.fmsmobile.ui.LoginActivity.LIMIT_MONTH;
import static net.e_sang.fmsmobile.ui.LoginActivity.editNewPW;
import static net.e_sang.fmsmobile.ui.LoginActivity.editPW;

public class BaseLoginActivity extends BaseActivity implements TelKit.OnResultListener {

    private boolean NOTI_CHECK = false;

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_USER_LOGIN) || result.mRequestUrl.equals(TelKit.URL_API_USER_AUTO_LOGIN)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            JSONArray list = json.optJSONArray("list");
                            if (list != null && list.length() > 0) {
                                Object obj = list.get(0);
                                if (obj != null) {
                                    JSONObject userInfoObj = (JSONObject) obj;

                                    // 회원 정보 저장
                                    UserInfo userInfo = new UserInfo();
                                    userInfo.SYS_ID = userInfoObj.optString("SYS_ID");
                                    userInfo.LOGIN_ID = userInfoObj.optString("LOGIN_ID");
                                    userInfo.LOGIN_PWD = userInfoObj.optString("LOGIN_PWD");
                                    userInfo.USER_NAME = userInfoObj.optString("USER_NAME");
                                    userInfo.DEPT_NAME = userInfoObj.optString("DEPT_NAME");
                                    userInfo.DEPT_ID = userInfoObj.optString("DEPT_ID");
                                    userInfo.TEAM_NAME = userInfoObj.optString("TEAM_NAME");
                                    userInfo.TEAM_ID = userInfoObj.optString("TEAM_ID");
                                    userInfo.POSITION_NAME = userInfoObj.optString("POSITION_NAME");
                                    userInfo.POSITION_ID = userInfoObj.optString("POSITION_ID");
                                    PrefKit.setUserInfo(this, userInfo);

                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                                    // 메인화면으로 이동
                                    Intent intent = getIntent(); /*데이터 수신*/
                                    NOTI_CHECK = intent.getBooleanExtra(Extra.KEY_NOTI_CHECK, false);
                                    Intent intent_main = new Intent(this, MainActivity.class);
                                    intent_main.putExtra(Extra.KEY_NOTI_CHECK, NOTI_CHECK);
                                    startActivity(intent_main);
                                    finish();
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                } else {
                                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if ("fail2".equals(code)) {
                                LIMIT_MONTH = true;
                                editNewPW.setVisibility(View.VISIBLE);
                            } else {
                                LIMIT_MONTH = false;
                                editNewPW.setVisibility(View.GONE);
                            }
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            Intent intent = getIntent(); /*데이터 수신*/
                            NOTI_CHECK = intent.getBooleanExtra(Extra.KEY_NOTI_CHECK, false);
                            Intent intent_main = new Intent(this, LoginActivity.class);
                            intent_main.putExtra(Extra.KEY_NOTI_CHECK, NOTI_CHECK);
                            startActivity(intent_main);
                        }
                    } else {
                        Toast.makeText(this, json.optString("Message") + "\n다시 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        editPW.setText(null);
                        editNewPW.setText(null);
                        ReLoginStart();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic) + "\n다시 로그인을 진행해 주세요.", Toast.LENGTH_SHORT).show();
                    ReLoginStart();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.str_error_generic) + "\n다시 로그인을 진행해 주세요.", Toast.LENGTH_SHORT).show();
                ReLoginStart();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic) + "\n다시 로그인을 진행해 주세요.", Toast.LENGTH_SHORT).show();
            ReLoginStart();
        }
    }

    protected void tryUserLogin(String id, String pw, String newpw, View progressView) {
        if (Kit.isNotNullNotEmpty(id)) {
            HashMap<String, String> body = new HashMap<>();
            body.put("user_id", id);
            body.put("user_pw", pw);
            body.put("user_pw_confirm", newpw);
            new TelKit(this, this, progressView).request(TelKit.URL_API_USER_LOGIN, body);
        } else {
            Kit.showAlertDialog(this, "", getResources().getString(R.string.str_error_generic) + "\n다시 로그인을 진행해 주세요.", "확인");
            ReLoginStart();
        }
    }

    protected void tryUserAutoLogin(String id, String pw, String newpw, View progressView) {
        if (Kit.isNotNullNotEmpty(id)) {
            HashMap<String, String> body = new HashMap<>();
            body.put("user_id", id);
            body.put("user_pw", pw);
            body.put("user_pw_confirm", newpw);
            new TelKit(this, this, progressView).request(TelKit.URL_API_USER_AUTO_LOGIN, body);
        } else {
            Kit.showAlertDialog(this, "", getResources().getString(R.string.str_error_generic) + "\n다시 로그인을 진행해 주세요.", "확인");
            ReLoginStart();
        }
    }

    private void ReLoginStart() {
        PrefKit.setUserInfo(this, null);
        Intent intent = getIntent(); /*데이터 수신*/
        NOTI_CHECK = intent.getBooleanExtra(Extra.KEY_NOTI_CHECK, false);
        Intent intent_main = new Intent(this, LoginActivity.class);
        intent_main.putExtra(Extra.KEY_NOTI_CHECK, NOTI_CHECK);
        startActivity(intent_main);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

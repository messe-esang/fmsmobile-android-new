package net.e_sang.fmsmobile.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegStaffAddActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private LinearLayout mLinearLayout, layoutErrorManagers = null;
    private int mCompanyID = 0;
    private EditText edtStaffName;
    private EditText edtStaffPosition;
    private EditText edtStaffDept;
    private EditText edtStaffMobile;
    private EditText edtStaffEmail;
    private EditText edtStaffPhone;
    private CheckBox ckRole1;
    private CheckBox ckRole2;
    private CheckBox ckRole3;
    private CheckBox ckRole4;
    private CheckBox ckRole5;
    private CheckBox ckRole6;
    private CheckBox ckEmail;
    private CheckBox ckSMS;
    private CheckBox ckWork;
    private Button btnOK;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_staff_add);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_company));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_staff_add);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText(R.string.str_title_staff_add);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        layoutErrorManagers = findViewById(R.id.layoutErrorManagers);
        edtStaffName = findViewById(R.id.edtStaffName);
        edtStaffPosition = findViewById(R.id.edtStaffPosition);
        edtStaffDept = findViewById(R.id.edtStaffDept);
        edtStaffMobile = findViewById(R.id.edtStaffMobile);
        edtStaffEmail = findViewById(R.id.edtStaffEmail);
        edtStaffPhone = findViewById(R.id.edtStaffPhone);
        ckRole1 = findViewById(R.id.ckRole1);
        ckRole2 = findViewById(R.id.ckRole2);
        ckRole3 = findViewById(R.id.ckRole3);
        ckRole4 = findViewById(R.id.ckRole4);
        ckRole5 = findViewById(R.id.ckRole5);
        ckRole6 = findViewById(R.id.ckRole6);
        ckEmail = findViewById(R.id.ckEmail);
        ckSMS = findViewById(R.id.ckSMS);
        ckWork = findViewById(R.id.ckWork);
        btnOK = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);
        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            mCompanyID = intent.getIntExtra(Extra.KEY_COMPANY_ID, 0);
            Log.e(TAG, "COMPANY_ID : " + String.valueOf(mCompanyID));
        }

        edtStaffName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e(TAG, "onTextChanged count : " + count);
                if (edtStaffName.getText().length() == 0 || edtStaffName.getText().toString().trim().isEmpty()) {
                    layoutErrorManagers.setVisibility(View.VISIBLE);
                } else {
                    layoutErrorManagers.setVisibility(View.GONE);
                    if (edtStaffName.getText().length() == 1) {
                        if (s.toString().equals(" ")) {
                            edtStaffName.setText(null);
                            layoutErrorManagers.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_STAFF)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String resultStr = json.optString("result");
                    String resultList = json.optString("list");

                    JSONObject result_obj = new JSONObject(resultStr);
                    String code = result_obj.optString("code");
                    String msg = result_obj.optString("msg");

                    if ("ok".equals(code)) {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    protected void setStaffAdd() {
        UserInfo userInfo = PrefKit.getUserInfo(this);

        if (edtStaffName.getText().length() == 0 || TextUtils.isEmpty(edtStaffName.getText().toString())) {
            Kit.showAlertDialog(this, "", "이름을 입력해 주세요.", "확인");
            edtStaffName.setFocusable(true);
            return;
        }

        ArrayList<String> arrayRole = new ArrayList<>();
        arrayRole.clear();
        if (ckRole1.isChecked()) {
            //비즈매칭 6
            arrayRole.add("6");
        } else {
            arrayRole.remove("6");
        }

        if (ckRole2.isChecked()) {
            //세금 3
            arrayRole.add("3");
        } else {
            arrayRole.remove("3");
        }

        if (ckRole3.isChecked()) {
            //영업 2
            arrayRole.add("2");
        } else {
            arrayRole.remove("2");
        }

        if (ckRole4.isChecked()) {
            //온라인 4
            arrayRole.add("4");
        } else {
            arrayRole.remove("4");
        }

        if (ckRole5.isChecked()) {
            //총괄 1
            arrayRole.add("1");
        } else {
            arrayRole.remove("1");
        }

        if (ckRole6.isChecked()) {
            //현장 5
            arrayRole.add("5");
        } else {
            arrayRole.remove("5");
        }

        String fixRole = "";
        if (!arrayRole.isEmpty()) {
            fixRole = TextUtils.join(",", arrayRole);
        }

        String email = "";
        if (ckEmail.isChecked()) {
            email = "Y";
        } else {
            email = "N";
        }
        String sms = "";
        if (ckSMS.isChecked()) {
            sms = "Y";
        } else {
            sms = "N";
        }
        String work = "";
        if (ckWork.isChecked()) {
            work = "Y";
        } else {
            work = "N";
        }

        if (userInfo != null) {

            Log.e(TAG, "COMPANY_ID : " + String.valueOf(mCompanyID));
            Log.e(TAG, "STAFF_NAME : " + edtStaffName.getText().toString());
            Log.e(TAG, "STAFF_POSITION : " + edtStaffPosition.getText().toString());
            Log.e(TAG, "STAFF_DEPT : " + edtStaffDept.getText().toString());
            Log.e(TAG, "STAFF_MOBILE : " + edtStaffMobile.getText().toString());
            Log.e(TAG, "STAFF_EMAIL : " + edtStaffEmail.getText().toString());
            Log.e(TAG, "STAFF_PHONE : " + edtStaffPhone.getText().toString());
            Log.e(TAG, "ROLE : " + fixRole);
            Log.e(TAG, "FLAG_EMAIL : " + email);
            Log.e(TAG, "FLAG_SMS : " + sms);
            Log.e(TAG, "WORK_FLAG : " + work);
            Log.e(TAG, "CREATE_USER : " + userInfo.LOGIN_ID);

            HashMap<String, String> body = new HashMap<>();
            body.put("COMPANY_ID", String.valueOf(mCompanyID));
            body.put("STAFF_NAME", edtStaffName.getText().toString());
            body.put("STAFF_POSITION", edtStaffPosition.getText().toString());
            body.put("STAFF_DEPT", edtStaffDept.getText().toString());
            body.put("STAFF_MOBILE", edtStaffMobile.getText().toString());
            body.put("STAFF_EMAIL", edtStaffEmail.getText().toString());
            body.put("STAFF_PHONE", edtStaffPhone.getText().toString());
            body.put("ROLE", fixRole);
            body.put("FLAG_EMAIL", email);
            body.put("FLAG_SMS", sms);
            body.put("WORK_FLAG", work);
            body.put("CREATE_USER", userInfo.LOGIN_ID);
            new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_INSERT_STAFF, body);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnOK) {
            Log.e(TAG, "btnOK");
            setStaffAdd();
        } else if (id == R.id.btnCancel) {
            Log.e(TAG, "btnCancel");
            finish();
        }
    }
}

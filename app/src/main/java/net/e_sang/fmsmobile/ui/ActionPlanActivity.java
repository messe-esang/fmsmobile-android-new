package net.e_sang.fmsmobile.ui;

import android.app.DatePickerDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.WorkOut;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class ActionPlanActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private ArrayList<WorkOut> mItems = new ArrayList<>();
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private EditText mEditDate = null;
    private TextInputEditText mEditContent_ActionPlan = null;
    private Button mBtnRegister_ActionPlan = null;
    private TextView txtErrorContent = null;
    private String IDX = "";
    private String getDATE = "";
    private String Finish_Msg = "일정등록을 취소 하시겠습니까?";
    private Button delete_btn = null;
    private Space space_view = null;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mEditDate.getText().toString().trim().isEmpty()) {
                mBtnRegister_ActionPlan.setEnabled(false);
                txtErrorContent.setText("* 날짜를 선택해 주세요.");
                txtErrorContent.setVisibility(View.VISIBLE);
            } else if (mEditContent_ActionPlan.getText().toString().trim().isEmpty()) {
                mBtnRegister_ActionPlan.setEnabled(false);
                txtErrorContent.setText("* 일정 등록 내용을 입력해주세요.");
                txtErrorContent.setVisibility(View.VISIBLE);
            } else {
                mBtnRegister_ActionPlan.setEnabled(true);
                txtErrorContent.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_35A4F3);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_action_plan);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_35A4F3));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("일정등록");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        View root = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent != null) {
            IDX = intent.getExtras().getString("IDX");
            getDATE = intent.getExtras().getString("DATE");
            Log.e(TAG, "IDX : " + IDX);
            Log.e(TAG, "DATE : " + getDATE);
        }

        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        txtErrorContent = findViewById(R.id.txtErrorContent);
        mBtnRegister_ActionPlan = findViewById(R.id.btnRegister_ActionPlan);
        mEditContent_ActionPlan = findViewById(R.id.editContent_ActionPlan);
        mEditDate = findViewById(R.id.editDate);
        delete_btn = findViewById(R.id.delete_btn);
        space_view = findViewById(R.id.space_view);

        mBtnRegister_ActionPlan.setOnClickListener(this);
        mEditContent_ActionPlan.setOnClickListener(this);
        delete_btn.setOnClickListener(this);
        mEditDate.setOnClickListener(this);

        mEditDate.addTextChangedListener(mTextWatcher);
        mEditContent_ActionPlan.addTextChangedListener(mTextWatcher);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        if (!TextUtils.isEmpty(IDX) && !IDX.equals("0")) {
            delete_btn.setVisibility(View.VISIBLE);
            space_view.setVisibility(View.VISIBLE);
            getActionPlan();
        } else {
            delete_btn.setVisibility(View.GONE);
            space_view.setVisibility(View.GONE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ActionPlanActivity.this);
                alert_confirm.setMessage(Finish_Msg).setCancelable(false).setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });

                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
//        alert_confirm.setMessage(Finish_Msg).setCancelable(false).setPositiveButton("네",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                }).setNegativeButton("아니오",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        return;
//                    }
//                });
//
//        AlertDialog alert = alert_confirm.create();
//        alert.show();
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.editDate) {
            showDate();
        } else if (id == R.id.btnRegister_ActionPlan) {
            setActionPlan();
        } else if (id == R.id.delete_btn) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setMessage("등록된 일정을 삭제 하시겠습니까?").setCancelable(false).setPositiveButton("네",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'YES'
                            deleteActionPlan();
                        }
                    }).setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'No'
                            return;
                        }
                    });

            AlertDialog alert = alert_confirm.create();
            alert.show();
        }
    }

    protected void deleteActionPlan() {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("IDX", IDX);
        new TelKit(this, this, mProgressBar).request(TelKit.URL_API_DELETE_ACTION_PLAN, body);
    }

    protected void getActionPlan() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("IDX", IDX);
        body.put("USER_ID", userInfo.LOGIN_ID);
        new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_ACTION_PLAN_DETAIL, body);
    }

    protected void setActionPlan() {
        if (!TextUtils.isEmpty(mEditDate.getText().toString())) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            LinkedHashMap<String, String> body = new LinkedHashMap<>();
            body.put("IDX", IDX);
            body.put("COMMENT", mEditContent_ActionPlan.getText().toString());
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            body.put("WORK_DATE", getDATE);
            new TelKit(this, this, mProgressBar).request(TelKit.URL_API_INSERT_ACTION_PLAN, body);
        } else {
            Toast.makeText(ActionPlanActivity.this, "날짜가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setMessage(Finish_Msg).setCancelable(false).setPositiveButton("네",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

            AlertDialog alert = alert_confirm.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_ACTION_PLAN_DETAIL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            JSONObject data = json.optJSONObject("data");
                            Log.e(TAG, "getActionPlan onResult data : " + data);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            Date to = sdf.parse(data.optString("CREATE_DATE").replace("null", ""));
                            SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                            mEditDate.setText(transFormat.format(to));
                            mEditContent_ActionPlan.setText(data.optString("COMMENT").replace("null", ""));
                            mEditContent_ActionPlan.setSelection(mEditContent_ActionPlan.length());
                            mBtnRegister_ActionPlan.setText("수정");
                            mBtnRegister_ActionPlan.setEnabled(true);
                            Finish_Msg = "일정등록 수정을 취소 하시겠습니까?";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActionPlanActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_ACTION_PLAN)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            Toast.makeText(ActionPlanActivity.this, "일정등록이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActionPlanActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_DELETE_ACTION_PLAN)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            Toast.makeText(ActionPlanActivity.this, msg, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActionPlanActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ActionPlanActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActionPlanActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDate() {
        final Calendar cal = Calendar.getInstance();
        int year = -1;
        int month = -1;
        int date = -1;
        if (!TextUtils.isEmpty(IDX) && !IDX.equals("0") && !TextUtils.isEmpty(getDATE)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                Date getDate = sdf.parse(getDATE);
                Calendar c1 = Calendar.getInstance();
                c1.setTime(getDate);
                String y = new SimpleDateFormat("yyyy", Locale.getDefault()).format(c1.getTime());
                String m = new SimpleDateFormat("MM", Locale.getDefault()).format(c1.getTime());
                String d = new SimpleDateFormat("dd", Locale.getDefault()).format(c1.getTime());
                year = Integer.parseInt(y);
                month = Integer.parseInt(m);
                date = Integer.parseInt(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH) + 1;
            date = cal.get(Calendar.DATE);
        }

        DatePickerDialog dialog = new DatePickerDialog(ActionPlanActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String msg = String.format("%d-%d-%d", year, month + 1, date);
                    Date to = sdf.parse(msg);
                    mEditDate.setText(sdf.format(to));
                    getDATE = sdf.format(to).replace("-", "");
                    //Toast.makeText(ActionPlanActivity.this, sdf.format(to).replace("-", ""), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, year, month - 1, date);

        dialog.getDatePicker().setMinDate(new Date().getTime());
        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
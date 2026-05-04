package net.e_sang.fmsmobile.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class ReportActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private LinearLayout mLinearLayout = null;

    private TextView txtErrorContent = null;

    private RadioButton rg_btn1, rg_btn2, rg_btn3;
    private String report_type;

    private EditText editTitle, editContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_FFBB00);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_FFBB00));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("동향보고 등록");
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

        mLinearLayout = findViewById(R.id.progressBar_layout);
        editTitle = findViewById(R.id.editTitle);
        editContent = findViewById(R.id.editContent);
        txtErrorContent = findViewById(R.id.txtErrorContent);
        Button mBtnRegister = findViewById(R.id.btnRegister);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        rg_btn1 = findViewById(R.id.rg_btn1);
        rg_btn2 = findViewById(R.id.rg_btn2);
        rg_btn3 = findViewById(R.id.rg_btn3);

        mBtnRegister.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //txtErrorContent.setText("");
                if (editTitle.getText().length() == 0 || editTitle.getText().toString().trim().isEmpty()) {
                    txtErrorContent.setText("* 제목을 입력해주세요.");
                } else {
                    txtErrorContent.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editContent.getText().length() == 0 || editContent.getText().toString().trim().isEmpty()) {
                    txtErrorContent.setText("* 내용을 입력해주세요.");
                } else {
                    txtErrorContent.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                txtErrorContent.setText("");
                if (checkedId == R.id.rg_btn1) {
                    report_type = "1";
                } else if (checkedId == R.id.rg_btn2) {
                    report_type = "2";
                } else if (checkedId == R.id.rg_btn3) {
                    report_type = "3";
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FinishAlertDialog();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnRegister) {
            if (!rg_btn1.isChecked() && !rg_btn2.isChecked() && !rg_btn3.isChecked()) {
                txtErrorContent.setText("* 구분을 체크해 주세요");
            } else if (editTitle.getText().length() == 0 || editTitle.getText().toString().trim().isEmpty()) {
                txtErrorContent.setText("* 제목을 입력해주세요.");
            } else if (editContent.getText().length() == 0 || editContent.getText().toString().trim().isEmpty()) {
                txtErrorContent.setText("* 내용을 입력해주세요.");
            } else {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
                alert_confirm.setTitle("동향보고 등록 확인");
                alert_confirm.setMessage("확인을 누르시면 동양보고가 등록되며 FMS모바일 사용자에게 알림이 발송됩니다.\n등록 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        (dialog, which) -> {
                            // 'YES'
                            InsertReport();
                        }).setNegativeButton("취소",
                        (dialog, which) -> {
                            // 'No'
                            dialog.dismiss();
                        });

                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        }
    }

    protected void InsertReport() {

        try {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            if (!Objects.equals(userInfo.SYS_ID, "") && editTitle.getText().length() != 0 && editContent.getText().length() != 0) {
                HashMap<String, String> body = new HashMap<>();
                body.put("TITLE", editTitle.getText().toString());
                body.put("CONTENT", editContent.getText().toString());
                body.put("SYSTEM_ID", userInfo.SYS_ID);
                body.put("DBSTATUS", "A");
                body.put("CREATE_USER", userInfo.LOGIN_ID);
                body.put("BOARD_TYPE", "3");
                body.put("REPORT_TYPE", report_type);
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_INSERT_NOTICE, body);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FinishAlertDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_NOTICE)) {
            try {
                JSONObject json = new JSONObject(result.mResponse);
                JSONObject resultObj = json.optJSONObject("result");
                if (resultObj != null) {
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
                        alert_confirm.setTitle("동향보고 등록이 완료 되었습니다.");
                        alert_confirm.setMessage("확인을 누르시면 메인 화면으로 돌아갑니다.").setCancelable(false).setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'YES'
                                        finish();
                                    }
                                });

                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ReportActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ReportActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void FinishAlertDialog() {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        String finish_Msg = "동향보고 등록을 취소 하시겠습니까?";
        alert_confirm.setMessage(finish_Msg).setCancelable(false).setPositiveButton("확인",
                (dialog, which) -> {
                    // 'YES'
                    finish();
                }).setNegativeButton("취소",
                (dialog, which) -> {
                    // 'No'
                    dialog.dismiss();
                });

        AlertDialog alert = alert_confirm.create();
        alert.show();
    }
}
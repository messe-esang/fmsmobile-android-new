package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.BaseActivity;
import net.e_sang.fmsmobile.ui.RegSalesActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class NewCompanyActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();

    private TextView txtToolbarTitle, txt_name, txt_department, txt_phone, txt_tel, txt_email, txt_company;
    private LinearLayout mProgressBarLayout = null;
    private EditText edt_address, edt_homepage, edt_fax;// 데이터 수정 여부
    private ImageView image_view_crop;
    private NameCardList nameCardList = null;
    private Button btn_name_card_save, btn_camera;
    private String entry_path = "";
    private int mCompanyID = 0;
    private RadioGroup division_radioGroup;
    private String Division_Type = "N";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_company);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_new_company);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mProgressBarLayout = findViewById(R.id.progressBar_layout);

        image_view_crop = findViewById(R.id.image_view_crop);
        txt_name = findViewById(R.id.txt_name);
        txt_department = findViewById(R.id.txt_department);
        txt_phone = findViewById(R.id.txt_phone);
        txt_tel = findViewById(R.id.txt_tel);
        txt_email = findViewById(R.id.txt_email);
        txt_company = findViewById(R.id.txt_company);
        edt_address = findViewById(R.id.edt_address);
        edt_homepage = findViewById(R.id.edt_homepage);
        edt_fax = findViewById(R.id.edt_fax);

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(this);
        btn_name_card_save = findViewById(R.id.btn_name_card_save);
        btn_name_card_save.setOnClickListener(this);
        division_radioGroup = findViewById(R.id.division_radioGroup);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            nameCardList = getIntent().getParcelableExtra("NameCard");
            mCompanyID = intent.getIntExtra(Extra.KEY_COMPANY_ID, 0);
            Log.e(TAG, "COMPANY_ID : " + String.valueOf(mCompanyID));
            if (nameCardList != null) {
                Log.e(TAG, "companyName: " + nameCardList.company);
                Log.e(TAG, "companyId: " + nameCardList.companyId);
                Log.e(TAG, "image : " + nameCardList.image);
                Log.e(TAG, "memo : " + nameCardList.memo);
                entry_path = intent.getExtras().getString("entry_path");
                Log.e(TAG, "entry_path : " + entry_path);
                txt_name.setText(nameCardList.name);
                txt_department.setText(nameCardList.department + " | " + nameCardList.position);
                txt_phone.setText(nameCardList.mobile);
                txt_tel.setText(nameCardList.tel);
                txt_email.setText(nameCardList.email);
                txt_company.setText(nameCardList.company);

                edt_address.setText(nameCardList.address);
                edt_homepage.setText(nameCardList.homepage);
                edt_fax.setText(nameCardList.fax);
                if (Kit.isNotNullNotEmpty(nameCardList.image)) {
                    image_view_crop.setImageURI(Uri.parse(nameCardList.image));
//                    Glide.with(this)
//                            .load(new File(nameCardList.image.replace("file:", "")))
//                            .into(image_view_crop);
                }
            }
        }

        division_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioDomestic) {
                    // 국내 선택
                    Division_Type = "N";
                } else if (checkedId == R.id.radioGlobal) {
                    // 해외 선택
                    Division_Type = "Y";
                }
                Log.e(TAG, "onCheckedChanged : " + Division_Type);
            }
        });
        // 뒤로 가기 콜백 생성
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* 활성화 상태 */) {
            @Override
            public void handleOnBackPressed() {
                finishDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finishDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_camera) {
            Kit.startActivity(NewCompanyActivity.this, NameCardCameraActivity.class);
            finish();
        } else if (id == R.id.btn_name_card_save) {
            btn_name_card_save.setEnabled(false);
            uploadImage(nameCardList);
        }
    }

    private void finishDialog() {
        String type_mag = "신규 등록을 취소 하시겠습니까?";
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage(type_mag).setCancelable(false).setPositiveButton("아니오",
                (dialog, which) -> dialog.dismiss()).setNegativeButton("네",
                (dialog, which) -> finish());
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }

    protected String getTag(View view) {
        String tag = "";
        Object obj = view.getTag();
        if (obj != null) {
            tag = (String) obj;
        }
        return tag;
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_REG_COMPANY_INFO)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    Log.e(TAG, "onResult result : " + resultObj);
                    Log.e(TAG, "onResult code : " + code);
                    if ("ok".equals(code)) {
                        JSONObject data = json.optJSONObject("data");
                        Log.e(TAG, "onResult data : " + data);
                        if (data != null) {

                        }
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }

            } else if (result.mRequestUrl.equals(TelKit.URL_API_OCR_STAFF_DETAIL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    Log.e(TAG, "URL_API_OCR_STAFF_DETAIL json : " + json);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("success".equals(code)) {
                            JSONObject data = json.optJSONObject("data");
                            Log.e(TAG, "URL_API_OCR_STAFF_DETAIL data : " + data);
                            assert data != null;
                            showManagerCompleteDialog(data);
                        } else {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(NameCardList nameCardList) {
        new Thread(() -> {
            try {

                File file = new File(nameCardList.image.replace("file:", ""));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // mProgressBarLayout.setVisibility(View.VISIBLE);
                    }
                });
                UserInfo userInfo = PrefKit.getUserInfo(this);

                HashMap<String, String> body = new HashMap<>();
                body.put("COMPANY_ID", "");
                body.put("COMPANY_NAME", nameCardList.company);
                body.put("HOMEPAGE", nameCardList.homepage);
                body.put("ADDR", nameCardList.address);
                body.put("COMPANY_STAFF_MEMO", nameCardList.memo);
                body.put("COMPANY_STAFF_ID", "");
                body.put("STAFF_NAME", nameCardList.name);
                body.put("STAFF_EMAIL", nameCardList.email);
                body.put("STAFF_MOBILE", nameCardList.mobile);
                body.put("STAFF_PHONE", nameCardList.tel);
                body.put("STAFF_DEPT", nameCardList.department);
                body.put("STAFF_POSITION", nameCardList.position);
                body.put("USER_ID", userInfo.LOGIN_ID);
                body.put("FOREIGN_FLAG", Division_Type);

                new TelKit(this, result -> {
                    if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_OCR_STAFF)) {
                        try {
                            JSONObject json = new JSONObject(result.mResponse);
                            Log.e(TAG, "URL_API_INSERT_OCR_STAFF json : " + json);
                            JSONObject resultObj = json.optJSONObject("result");
                            Log.e(TAG, "URL_API_INSERT_OCR_STAFF result : " + resultObj);
                            if (resultObj != null) {
                                String code = resultObj.optString("code");
                                String msg = resultObj.optString("msg");
                                if ("success".equals(code)) {
                                    JSONObject data = json.optJSONObject("data");
                                    Log.e(TAG, "getActionPlan onResult data : " + data);
                                    assert data != null;
                                    getStaffDetail(data.optString("COMPANY_STAFF_ID"));
                                } else {
                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    // mProgressBarLayout.setVisibility(GONE);
                    btn_name_card_save.setEnabled(true);
                }).requestMultipart(
                        TelKit.URL_API_INSERT_OCR_STAFF,
                        body,
                        "namecard_image",
                        file,
                        "image/jpeg",
                        0
                );

            } catch (Exception e) {
                e.printStackTrace();
                btn_name_card_save.setEnabled(true);
            }
        }).start();
    }

    private void getStaffDetail(String companyStaffId) {
        Log.e(TAG, "getStaffDetail : " + companyStaffId);
        UserInfo userInfo = PrefKit.getUserInfo(this);
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_STAFF_ID", companyStaffId);
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        new TelKit(this, this).request(TelKit.URL_API_OCR_STAFF_DETAIL, body);
    }

    private void showManagerCompleteDialog(JSONObject data) {
        Log.e(TAG, "showManagerCompleteDialog data : " + data);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_manager_complete);

        // 바깥 투명 처리
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCancelable(false); // 바깥 터치 막기 (원하면 true)

        // ⭐ 90% width 설정
        dialog.show(); // show() 이후에 width 설정해야 적용됨

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = (int) (dm.widthPixels * 0.9); // 90%
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // View 연결
        TextView txtCompany = dialog.findViewById(R.id.txtCompany);
        TextView txtName = dialog.findViewById(R.id.txtName);
        TextView txtPhone = dialog.findViewById(R.id.txtPhone);
        TextView txtTel = dialog.findViewById(R.id.txtTel);
        TextView txtEmail = dialog.findViewById(R.id.txtEmail);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        LinearLayout staff_error_layout = dialog.findViewById(R.id.staff_error_layout);

        // 값 세팅 (동적으로 바꿀 수 있음)
        txtCompany.setText(data.optString("COMPANY_NAME"));
        String name = data.optString("STAFF_NAME");
        String dept = data.optString("STAFF_DEPT");
        String position = data.optString("STAFF_POSITION");

        StringBuilder sb = new StringBuilder();
        sb.append(name);

        if (!dept.isEmpty()) {
            sb.append("   ").append(dept);
        }

        if (!position.isEmpty()) {
            sb.append(" | ").append(position);
        }

        txtName.setText(sb.toString());

        txtPhone.setText(data.optString("STAFF_MOBILE"));
        txtTel.setText(data.optString("STAFF_PHONE"));
        txtEmail.setText(data.optString("STAFF_EMAIL"));

        CompanyInfo companyInfo_new = new CompanyInfo();
        companyInfo_new.COMPANY_ID = Integer.parseInt(data.optString("COMPANY_ID"));
        companyInfo_new.COMPANY_NAME = data.optString("COMPANY_NAME");

        if (data.optString("WORK_FLAG").equals("N")) {
            staff_error_layout.setVisibility(VISIBLE);
        } else {
            staff_error_layout.setVisibility(GONE);
        }
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Log.e(TAG, "COMPANY_STAFF_ID: " + data.optString("COMPANY_STAFF_ID"));
            if (data.optString("WORK_FLAG").equals("N")) {
                Intent intent = new Intent(this, NameCardViewActivity.class);
                intent.putExtra("COMPANY_STAFF_ID", data.optString("COMPANY_STAFF_ID"));
                intent.putExtra("WORK_FLAG", "N");
                intent.putExtra("EDIT_TYPE", "1");
                startActivity(intent);
                Kit.ActivityManager.finishActivity(NameCardSearchActivity.class);
                finish();
            } else {
                Intent intent = new Intent(this, RegSalesActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo_new);
                startActivity(intent);
                Kit.ActivityManager.finishActivity(NameCardSearchActivity.class);
                finish();
            }
        });
    }
}
package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;

import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.OCRNameCard;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.BaseActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NameCardEditActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private LinearLayout mProgressBarLayout, memo_layout = null;
    private ImageView image_view_crop;
    private EditText edt_name, edt_company, edt_department, edt_position, edt_phone, edt_tel, edt_email, edt_memo;
    private Button btn_name_card_save, btn_name_card_cancel, btn_camera, btn_add_contacts;
    boolean isDataChanged = true; // 데이터 수정 여부
    private TextView txt_name, txt_company;

    private String EDIT_TYPE = "0";
    private TextView txtToolbarTitle, txt_info;
    private NameCardList nameCardList = null;
    private Uri imageUri = null;
    private static final int REQ_INSERT_CONTACT = 1003;
    private String lastSavedPhoneNumber;

    private TextView tvCount;
    final int MAX_LENGTH = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_edit);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_name_card_edit);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mProgressBarLayout = findViewById(R.id.progressBar_layout);
        image_view_crop = findViewById(R.id.image_view_crop);
        edt_name = findViewById(R.id.edt_name);
        edt_company = findViewById(R.id.edt_company);
        edt_department = findViewById(R.id.edt_department);
        edt_position = findViewById(R.id.edt_position);
        edt_phone = findViewById(R.id.edt_phone);
        edt_tel = findViewById(R.id.edt_tel);
        edt_email = findViewById(R.id.edt_email);
        txt_name = findViewById(R.id.txt_name);
        txt_company = findViewById(R.id.txt_company);
        btn_name_card_save = findViewById(R.id.btn_name_card_save);
        btn_name_card_save.setOnClickListener(this);
        btn_name_card_cancel = findViewById(R.id.btn_name_card_cancel);
        btn_name_card_cancel.setOnClickListener(this);
        btn_camera = findViewById(R.id.btn_camera);
        txt_info = findViewById(R.id.txt_info);
        btn_add_contacts = findViewById(R.id.btn_add_contacts);
        btn_add_contacts.setOnClickListener(this);
        edt_memo = findViewById(R.id.edt_memo);
        tvCount = findViewById(R.id.tvCount);
        memo_layout = findViewById(R.id.memo_layout);

        // 데이터 수신
        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {

            String uriStr = intent.getExtras().getString("image_uri");
            EDIT_TYPE = intent.getExtras().getString("EDIT_TYPE");
            Log.e(TAG, "EDIT_TYPE : " + EDIT_TYPE);
            Log.e(TAG, "uriStr : " + uriStr);
            nameCardList = getIntent().getParcelableExtra("NameCard");
            if (EDIT_TYPE.equals("0") && uriStr != null && !uriStr.isEmpty()) {
                btn_name_card_save.setEnabled(false);
                image_view_crop.setImageResource(0);
                Log.e(TAG, "uriStr : " + uriStr);
                imageUri = Uri.parse(uriStr);
                Log.e(TAG, "imageUri : " + imageUri);
                uploadImage(imageUri);
            } else {
                if (nameCardList != null) {
                    setEditNameCard(nameCardList);
                }
            }

            if (EDIT_TYPE.equals("0")) {
                btn_name_card_save.setText("등록 여부 조회");
                txtToolbarTitle.setText("정보 확인");
                btn_camera.setVisibility(VISIBLE);
                edt_name.setVisibility(VISIBLE);
                edt_company.setVisibility(VISIBLE);
                txt_name.setVisibility(GONE);
                txt_company.setVisibility(GONE);
                txt_info.setVisibility(VISIBLE);
                btn_add_contacts.setVisibility(VISIBLE);
                memo_layout.setVisibility(GONE);
            } else {
                btn_name_card_save.setText("저장");
                txtToolbarTitle.setText("명함 수정");
                btn_camera.setVisibility(GONE);
                edt_name.setVisibility(GONE);
                edt_company.setVisibility(GONE);
                txt_name.setVisibility(VISIBLE);
                txt_company.setVisibility(VISIBLE);
                txt_info.setVisibility(INVISIBLE);
                btn_add_contacts.setVisibility(GONE);
                memo_layout.setVisibility(VISIBLE);
            }
        }

        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kit.startActivity(NameCardEditActivity.this, NameCardCameraActivity.class);
                finish();
            }
        });

        // 뒤로 가기 콜백 생성
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* 활성화 상태 */) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행할 커스텀 로직
                if (isDataChanged) { // 수정된 내용이 있다면
                    finishDialog();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }

            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        image_view_crop.setOnClickListener(v -> {
            if (nameCardList.image.isEmpty()) {
                Toast.makeText(this, "등록된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.namecard_image_alert_layout);
                dialog.setCancelable(true);

                PhotoView photoView = dialog.findViewById(R.id.popupPhotoView);
                photoView.setImageDrawable(image_view_crop.getDrawable());
                TextView btnClose = dialog.findViewById(R.id.btnClose);
                btnClose.setOnClickListener(view -> dialog.dismiss());

                dialog.show();
            }
        });

        edt_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCount.setText(s.length() + " / " + MAX_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        InputFilter lengthFilter = new InputFilter.LengthFilter(MAX_LENGTH);
        edt_memo.setFilters(new InputFilter[]{lengthFilter});
    }

    private File uriToFile(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        File file = new File(getCacheDir(), "upload.jpg");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        return file;
    }

//    private void uploadImage(Uri uri) {
//        new Thread(() -> {
//            try {
//                Log.e(TAG, "uploadImage uri : " + uri);
//                File file = uriToFile(uri);
//                Log.e(TAG, "uploadImage file : " + file);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mProgressBarLayout.setVisibility(VISIBLE);
//                        image_view_crop.setImageURI(uri);
//                        PrefKit.setNameCard(NameCardEditActivity.this, null);
//                    }
//                });
//                HashMap<String, String> body = new HashMap<>();
//
//                new TelKit(this, result -> {
//                    if (result.mRequestUrl.equals(TelKit.URL_API_PARSENAMECARD)) {
//                        try {
//                            JSONObject json = new JSONObject(result.mResponse);
//                            JSONObject resultObj = json.optJSONObject("result");
//                            if (resultObj != null) {
//                                String code = resultObj.optString("code");
//                                String msg = resultObj.optString("msg");
//                                Log.e(TAG, "uploadImage code : " + code);
//                                Log.e(TAG, "uploadImage msg : " + msg);
//                                if ("ok".equals(code)) {
//                                    JSONObject data = json.optJSONObject("data");
//                                    Log.e(TAG, "getActionPlan onResult data : " + data);
//                                    OCRNameCard namecard = new OCRNameCard();
//                                    namecard.name = data.optString("name");
//                                    namecard.company = data.optString("company");
//                                    namecard.department = data.optString("department");
//                                    namecard.position = data.optString("position");
//                                    namecard.mobile = data.optString("mobile");
//                                    namecard.tel = data.optString("tel");
//                                    namecard.email = data.optString("email");
//                                    namecard.address = data.optString("address");
//                                    namecard.homepage = data.optString("homepage");
//                                    namecard.fax = data.optString("fax");
//                                    namecard.image = file;
//                                    PrefKit.setNameCard(this, namecard);
//                                    setOCRNameCard(namecard);
//                                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//                                    btn_name_card_save.setEnabled(true);
//                                    btn_add_contacts.setEnabled(true);
//                                } else {
//                                    Toast.makeText(NameCardEditActivity.this, msg + " 다시 촬영해 주세요.", Toast.LENGTH_SHORT).show();
//                                    btn_add_contacts.setEnabled(false);
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(NameCardEditActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(NameCardEditActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                    }
//                    mProgressBarLayout.setVisibility(GONE);
//                }).requestMultipart(
//                        TelKit.URL_API_PARSENAMECARD,
//                        body,
//                        "namecard_image",
//                        file,
//                        "image/jpeg",
//                        0
//                );
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }

    private void uploadImage(Uri uri) {
        new Thread(() -> {
            File file = null;

            try {
                Log.e(TAG, "uploadImage uri : " + uri);

                file = uriToFile(uri);

                if (file == null || !file.exists()) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                NameCardEditActivity.this,
                                "이미지 파일을 불러올 수 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                    return;
                }

                File finalFile = file;

                runOnUiThread(() -> {
                    mProgressBarLayout.setVisibility(VISIBLE);
                    image_view_crop.setImageURI(uri);

                    btn_name_card_save.setEnabled(false);
                    btn_add_contacts.setEnabled(false);

                    PrefKit.setNameCard(NameCardEditActivity.this, null);
                });

                HashMap<String, String> body = new HashMap<>();

                new TelKit(this, result -> {

                    runOnUiThread(() -> {
                        try {

                            Log.e(TAG, "response : " + result.mResponse);

                            if (result == null) {
                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        "서버 응답이 없습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            if (!TelKit.URL_API_PARSENAMECARD.equals(result.mRequestUrl)) {
                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        "잘못된 요청입니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            if (result.mResponse == null || result.mResponse.isEmpty()) {
                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        "응답 데이터가 없습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            JSONObject json = new JSONObject(result.mResponse);

                            JSONObject resultObj = json.optJSONObject("result");

                            if (resultObj == null) {
                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        "응답 형식이 올바르지 않습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            String code = resultObj.optString("code");
                            String msg = resultObj.optString("msg");

                            Log.e(TAG, "uploadImage code : " + code);
                            Log.e(TAG, "uploadImage msg : " + msg);

                            if ("ok".equals(code)) {

                                JSONObject data = json.optJSONObject("data");

                                if (data == null) {
                                    Toast.makeText(
                                            NameCardEditActivity.this,
                                            "명함 데이터를 찾을 수 없습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                OCRNameCard namecard = new OCRNameCard();

                                namecard.name = data.optString("name");
                                namecard.company = data.optString("company");
                                namecard.department = data.optString("department");
                                namecard.position = data.optString("position");
                                namecard.mobile = data.optString("mobile");
                                namecard.tel = data.optString("tel");
                                namecard.email = data.optString("email");
                                namecard.address = data.optString("address");
                                namecard.homepage = data.optString("homepage");
                                namecard.fax = data.optString("fax");
                                namecard.image = finalFile;

                                PrefKit.setNameCard(
                                        NameCardEditActivity.this,
                                        namecard
                                );

                                setOCRNameCard(namecard);

                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        msg,
                                        Toast.LENGTH_SHORT
                                ).show();

                                btn_name_card_save.setEnabled(true);
                                btn_add_contacts.setEnabled(true);

                            } else {

                                btn_name_card_save.setEnabled(false);
                                btn_add_contacts.setEnabled(false);

                                Toast.makeText(
                                        NameCardEditActivity.this,
                                        msg + "\n다시 촬영해 주세요.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } catch (Exception e) {

                            Log.e(TAG, "parse error", e);

                            Toast.makeText(
                                    NameCardEditActivity.this,
                                    "데이터 처리 중 오류가 발생했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } finally {

                            mProgressBarLayout.setVisibility(GONE);
                        }
                    });

                }).requestMultipart(
                        TelKit.URL_API_PARSENAMECARD,
                        body,
                        "namecard_image",
                        file,
                        "image/jpeg",
                        0
                );

            } catch (Exception e) {

                Log.e(TAG, "uploadImage error", e);

                runOnUiThread(() -> {
                    mProgressBarLayout.setVisibility(GONE);

                    btn_name_card_save.setEnabled(false);
                    btn_add_contacts.setEnabled(false);

                    Toast.makeText(
                            NameCardEditActivity.this,
                            "이미지 업로드에 실패했습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        }).start();
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
        if (id == R.id.btn_name_card_save) {
            btn_name_card_save.setEnabled(false);
            Log.e(TAG, "btn_name_card_save EDIT_TYPE : " + EDIT_TYPE);
            if (EDIT_TYPE.equals("0")) {
                nameCardList.name = edt_name.getText().toString();
                nameCardList.company = edt_company.getText().toString();
            } else {
                nameCardList.name = txt_name.getText().toString();
                nameCardList.company = txt_company.getText().toString();
            }
            nameCardList.company = edt_company.getText().toString();
            nameCardList.department = edt_department.getText().toString();
            nameCardList.position = edt_position.getText().toString();
            nameCardList.mobile = edt_phone.getText().toString();
            nameCardList.tel = edt_tel.getText().toString();
            nameCardList.email = edt_email.getText().toString();
            nameCardList.memo = edt_memo.getText().toString();

            if (EDIT_TYPE.equals("0")) {
                Log.e(TAG, "btn_name_card_save EDIT_TYPE1 : " + EDIT_TYPE);
                Intent intent = new Intent(this, NameCardSearchActivity.class);
                intent.putExtra("NameCard", nameCardList);
                setResult(RESULT_OK, intent);
                startActivity(intent);
                finish();
            } else {
                SendNameCard(nameCardList);
            }
        } else if (id == R.id.btn_name_card_cancel) {
            finishDialog();
        } else if (id == R.id.btn_add_contacts) {
            if (edt_name.getText().toString().isEmpty() || edt_phone.getText().toString().isEmpty()) {
                Toast.makeText(this, "이름 또는 전화번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else if (!Kit.isContactExists(this, edt_name.getText().toString(), edt_phone.getText().toString())) {
                addContact(nameCardList);
                //saveContact(nameCardList);
            } else {
                Toast.makeText(this, "이미 연락처에 등록된 명함입니다.", Toast.LENGTH_SHORT).show();
//                btn_add_contacts.setText("내 휴대폰에 저장 완료");
//                btn_add_contacts.setEnabled(false);
            }
        }
    }

    private void setEditNameCard(NameCardList nameCardList) {
        if (nameCardList == null) return;


        if (!TextUtils.isEmpty(nameCardList.image)) {
            image_view_crop.setImageResource(0);
            Glide.with(image_view_crop.getContext())
                    .load("https://mfms.esfair.kr" + nameCardList.image)
                    .into(image_view_crop);

        }

        if (EDIT_TYPE.equals("1")) {
            txt_name.setText(nameCardList.name);
            txt_company.setText(nameCardList.company);
        }

        edt_name.setText(nameCardList.name);
        edt_company.setText(nameCardList.company);
        edt_phone.setText(nameCardList.mobile);

        edt_department.setText(nameCardList.department);
        edt_position.setText(nameCardList.position);
        edt_tel.setText(nameCardList.tel);
        edt_email.setText(nameCardList.email);
        edt_memo.setText(nameCardList.memo);
        if (!nameCardList.memo.isEmpty()) {
            tvCount.setText(edt_memo.length() + " / " + MAX_LENGTH);
        }
    }

    private void setOCRNameCard(OCRNameCard namecard) {
        if (namecard == null) return;

        if (EDIT_TYPE.equals("1")) {
            txt_name.setText(nameCardList.name);
            txt_company.setText(nameCardList.company);
        }
        edt_name.setText(namecard.name);
        edt_company.setText(namecard.company);
        edt_phone.setText(namecard.mobile);

        edt_department.setText(namecard.department);
        edt_position.setText(namecard.position);
        edt_tel.setText(namecard.tel);
        edt_email.setText(namecard.email);
        File image = namecard.image;

        Log.e(TAG, "File image : " + image);

        URI javaUri = namecard.image.toURI();
        nameCardList = null;
        nameCardList = new NameCardList();

        nameCardList.name = namecard.name;
        nameCardList.company = namecard.company;
        nameCardList.department = namecard.department;
        nameCardList.position = namecard.position;
        nameCardList.mobile = namecard.mobile;
        nameCardList.tel = namecard.tel;
        nameCardList.email = namecard.email;
        nameCardList.address = namecard.address;
        nameCardList.homepage = namecard.homepage;
        nameCardList.fax = namecard.fax;
        nameCardList.image = javaUri.toString();
        nameCardList.memo = edt_memo.getText().toString();

    }

    private void finishDialog() {
        String type_mag = "";
        if (EDIT_TYPE.equals("0")) {
            type_mag = "명함 등록을 취소 하시겠습니까?";
        } else {
            type_mag = "명함 수정을 취소 하시겠습니까?";
        }
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage(type_mag).setCancelable(false).setPositiveButton("아니오",
                (dialog, which) -> dialog.dismiss()).setNegativeButton("네",
                (dialog, which) -> finish());
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }


    private void SendNameCard(NameCardList nameCardList) {
        UserInfo userInfo = PrefKit.getUserInfo(this);

        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_ID", String.valueOf(nameCardList.companyId));
        body.put("COMPANY_NAME", nameCardList.company);
        body.put("HOMEPAGE", nameCardList.homepage);
        body.put("ADDR", nameCardList.address);
        body.put("COMPANY_STAFF_ID", String.valueOf(nameCardList.companyStaffId));
        body.put("STAFF_NAME", nameCardList.name);
        body.put("STAFF_EMAIL", nameCardList.email);
        body.put("STAFF_MOBILE", nameCardList.mobile);
        body.put("STAFF_PHONE", nameCardList.tel);
        body.put("STAFF_DEPT", nameCardList.department);
        body.put("STAFF_POSITION", nameCardList.position);
        body.put("USER_ID", userInfo.LOGIN_ID);
        body.put("COMPANY_STAFF_MEMO", nameCardList.memo);
        new TelKit(this, this, mProgressBarLayout).requestMultipart(TelKit.URL_API_INSERT_OCR_STAFF, body, null, null, null, 0);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_INSERT_OCR_STAFF)) {
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
                            Kit.ActivityManager.finishActivity(NameCardViewActivity.class);
                            Toast.makeText(this, "명함 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(NameCardEditActivity.this, NameCardViewActivity.class);
                            assert data != null;
                            intent.putExtra("COMPANY_STAFF_ID", data.optString("COMPANY_STAFF_ID"));
                            intent.putExtra("WORK_FLAG", "");
                            intent.putExtra("EDIT_TYPE", "1");
                            startActivity(intent);
                            finish();

//                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
//                            alert_confirm.setMessage("명함 수정이 완료되었습니다.");
//                            alert_confirm.setCancelable(false);
//                            alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    Kit.ActivityManager.finishActivity(NameCardViewActivity.class);
//
//                                    Intent intent = new Intent(NameCardEditActivity.this, NameCardViewActivity.class);
//                                    assert data != null;
//                                    intent.putExtra("COMPANY_STAFF_ID", data.optString("COMPANY_STAFF_ID"));
//                                    intent.putExtra("EDIT_TYPE", "1");
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            });
//                            AlertDialog alert = alert_confirm.create();
//                            alert.show();
                        } else {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                    btn_name_card_save.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
            }
            btn_name_card_save.setEnabled(true);
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    private void addContact(NameCardList nameCardList) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        //int rawContactID = ops.size();
        int rawContactID = 0;
        // RawContact 생성
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
//                        ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .build());

        // 이름
        if (!edt_name.getText().toString().isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, edt_name.getText().toString())
                    .build());
        }
        // 휴대폰
        if (!edt_phone.getText().toString().isEmpty()) {
            addPhone(ops, rawContactID, edt_phone.getText().toString(),
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        // 회사전화
        if (!edt_tel.getText().toString().isEmpty()) {
            addPhone(ops, rawContactID, edt_tel.getText().toString(),
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        }
        // 팩스
//        if (!nameCardList.fax.isEmpty()) {
//            addPhone(ops, rawContactID, nameCardList.fax,
//                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);
//        }

        // 이메일
        if (!edt_email.getText().toString().isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, edt_email.getText().toString())
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // 회사 / 직급 / 부서
        String company = edt_company.getText().toString();
        String title = edt_position.getText().toString();
        String department = edt_department.getText().toString();

        ContentProviderOperation.Builder orgBuilder =
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);

        if (!company.isEmpty())
            orgBuilder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company);

        if (!title.isEmpty())
            orgBuilder.withValue(ContactsContract.CommonDataKinds.Organization.TITLE, title);

        if (!department.isEmpty())
            orgBuilder.withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department);

        ops.add(orgBuilder.build());

        // 주소
//        if (nameCardList.address != null && !nameCardList.address.isEmpty()) {
//            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
//                    .withValue(ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, nameCardList.address)
//                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
//                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
//                    .build());
//        }
//
//        // 웹사이트
//        if (nameCardList.homepage != null && !nameCardList.homepage.isEmpty()) {
//            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
//                    .withValue(ContactsContract.Data.MIMETYPE,
//                            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.Website.URL, nameCardList.homepage)
//                    .withValue(ContactsContract.CommonDataKinds.Website.TYPE,
//                            ContactsContract.CommonDataKinds.Website.TYPE_WORK)
//                    .build());
//        }

        // 명함 이미지
        if (imageUri != null) {
            Bitmap bitmap = getBitmapFromUri(this, imageUri);
            if (bitmap != null) {
                Log.e(TAG, "imageUri : " + imageUri);
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] imageBytes = stream.toByteArray();

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                        .build());
            }
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "내 휴대폰에 저장 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "내 휴대폰에 저장 실패", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addPhone(ArrayList<ContentProviderOperation> ops,
                          int rawContactID,
                          String number,
                          int type) {

        if (number == null || number.isEmpty()) return;

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, type)
                .build());
    }

    private Bitmap getBitmapFromUri(Context context, Uri uri) {

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void saveContact(NameCardList nameCardList) {
        lastSavedPhoneNumber = "";
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, edt_name.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, edt_phone.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, edt_tel.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, edt_email.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, edt_company.getText().toString());
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, edt_position.getText().toString());
        if (!nameCardList.department.isEmpty()) {
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, edt_department.getText().toString() + " / " + edt_position.getText().toString());
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, edt_position.getText().toString());
        }
        lastSavedPhoneNumber = edt_phone.getText().toString();
        // 실행
        //startActivityForResult(intent, REQ_INSERT_CONTACT);
        contactInsertLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> contactInsertLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        new Handler().postDelayed(() -> {

                            long contactId = getContactIdByPhone(lastSavedPhoneNumber);

                            if (contactId != -1) {
                                updateContactPhoto(contactId, imageUri);
                            }

                        }, 500);

                    }
            );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_INSERT_CONTACT) {
            // 연락처 저장 후 약간 딜레이 주는게 안전
            new Handler().postDelayed(() -> {

                long contactId = getContactIdByPhone(lastSavedPhoneNumber);

                if (contactId != -1) {
                    updateContactPhoto(contactId, imageUri);
                }

            }, 500);
        }
    }

    private long getContactIdByPhone(String phone) {

        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone));

        Cursor cursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.PhoneLookup._ID},
                null,
                null,
                null
        );

        long contactId = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
            }
            cursor.close();
        }

        return contactId;
    }

    private void updateContactPhoto(long contactId, Uri imageUri) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            //Bitmap bitmap = getBitmapFromURL(imageUrl);
            Bitmap bitmap = getBitmapFromUri(context, imageUri);

            if (bitmap == null) return;

            bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] imageBytes = stream.toByteArray();

            runOnUiThread(() -> applyPhoto(contactId, imageBytes));
        });
    }

    private void applyPhoto(long contactId, byte[] imageBytes) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // 기존 사진 삭제
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{
                                String.valueOf(contactId),
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        })
                .build());

        // 새 사진 추가
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, getRawContactId(contactId))
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "내 휴대폰에 저장 완료", Toast.LENGTH_SHORT).show();
//            btn_add_contacts.setText("내 휴대폰에 저장 완료");
//            btn_add_contacts.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getRawContactId(long contactId) {

        Cursor cursor = getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)},
                null
        );

        long rawId = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                rawId = cursor.getLong(0);
            }
            cursor.close();
        }

        return rawId;
    }
}
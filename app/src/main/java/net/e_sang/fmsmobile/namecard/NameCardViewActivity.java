package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.HistoryItem;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.BaseActivity;
import net.e_sang.fmsmobile.ui.RegSalesActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NameCardViewActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private LinearLayout mProgressBarLayout, memo_layout;
    private ImageView image_view_crop;
    private TextView txt_name_view, txt_company_view, txt_department_view, txt_position_view, txt_phone_view, txt_tel_view, txt_email_view, txt_addr_view, txt_homepage_view, txt_fax_view, txt_memo_view;
    private Button btn_name_card_edit, btn_name_card_cancel, btn_fms_sales, btn_add_contacts;
    private ImageButton btn_history_1, btn_history_2, btn_history_3, btn_history_4, btn_history_5, btn_history_6;

    private NameCardList nameCardList = null;
    private ArrayList<HistoryItem> mHistoryItem = new ArrayList<>();
    private String mCOMPANY_STAFF_ID = "";
    private String mWORK_FLAG = "";
    private static final int REQ_INSERT_CONTACT = 1003;
    private String lastSavedPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_view);
        applyInsets();
        Kit.ActivityManager.register(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_name_card_view);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mProgressBarLayout = findViewById(R.id.progressBar_layout);
        image_view_crop = findViewById(R.id.image_view_crop);
        txt_name_view = findViewById(R.id.txt_name_view);
        txt_company_view = findViewById(R.id.txt_company_view);
        txt_company_view.setPaintFlags(
                txt_company_view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );
        txt_company_view.setOnClickListener(this);
        txt_department_view = findViewById(R.id.txt_department_view);
        txt_position_view = findViewById(R.id.txt_position_view);
        txt_phone_view = findViewById(R.id.txt_phone_view);
        txt_tel_view = findViewById(R.id.txt_tel_view);
        txt_email_view = findViewById(R.id.txt_email_view);
        txt_addr_view = findViewById(R.id.txt_addr_view);
        txt_homepage_view = findViewById(R.id.txt_homepage_view);
        txt_fax_view = findViewById(R.id.txt_fax_view);
        btn_name_card_edit = findViewById(R.id.btn_name_card_edit);
        btn_name_card_edit.setOnClickListener(this);
        btn_name_card_cancel = findViewById(R.id.btn_name_card_cancel);
        btn_name_card_cancel.setOnClickListener(this);
        btn_history_1 = findViewById(R.id.btn_history_1);
        btn_history_2 = findViewById(R.id.btn_history_2);
        btn_history_3 = findViewById(R.id.btn_history_3);
        btn_history_4 = findViewById(R.id.btn_history_4);
        btn_history_5 = findViewById(R.id.btn_history_5);
        btn_history_6 = findViewById(R.id.btn_history_6);
        btn_fms_sales = findViewById(R.id.btn_fms_sales);
        btn_fms_sales.setOnClickListener(this);
        btn_history_1.setOnClickListener(this);
        btn_history_2.setOnClickListener(this);
        btn_history_3.setOnClickListener(this);
        btn_history_4.setOnClickListener(this);
        btn_history_5.setOnClickListener(this);
        btn_history_6.setOnClickListener(this);
        btn_add_contacts = findViewById(R.id.btn_add_contacts);
        btn_add_contacts.setOnClickListener(this);
        memo_layout = findViewById(R.id.memo_layout);
        txt_memo_view = findViewById(R.id.txt_memo_view);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            mCOMPANY_STAFF_ID = intent.getExtras().getString("COMPANY_STAFF_ID");
            mWORK_FLAG = intent.getExtras().getString("WORK_FLAG");
            String mEDIT_TYPE = intent.getExtras().getString("EDIT_TYPE");
            Log.e(TAG, "mCOMPANY_STAFF_ID: " + mCOMPANY_STAFF_ID);
            Log.e(TAG, "mWORK_FLAG: " + mWORK_FLAG);
            Log.e(TAG, "mEDIT_TYPE: " + mEDIT_TYPE);

            setLoadCompanyStaff(mCOMPANY_STAFF_ID);
        }

        // 뒤로 가기 콜백 생성
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* 활성화 상태 */) {
            @Override
            public void handleOnBackPressed() {
                // 뒤로 가기 시 실행할 커스텀 로직
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        };

        // OnBackPressedDispatcher에 콜백 등록 (this: LifecycleOwner)
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_name_card_edit) {
            Intent intent = new Intent(this, NameCardEditActivity.class);
            intent.putExtra("image_uri", "");
            intent.putExtra("EDIT_TYPE", "1");
            intent.putExtra("NameCard", nameCardList);
            setResult(RESULT_OK, intent);
            startActivity(intent);
        } else if (id == R.id.btn_history_1) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "1");
        } else if (id == R.id.btn_history_2) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "2");
        } else if (id == R.id.btn_history_3) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "3");
        } else if (id == R.id.btn_history_4) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "4");
        } else if (id == R.id.btn_history_5) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "5");
        } else if (id == R.id.btn_history_6) {
            getLoadStaffHistory(mCOMPANY_STAFF_ID, "6");
        } else if (id == R.id.txt_company_view) {
            if (mWORK_FLAG.equals("N")) {
                showManagerWorkFalgNDialog();
            } else {
                CompanyInfo companyInfo = new CompanyInfo();
                companyInfo.COMPANY_ID = nameCardList.companyId;
                companyInfo.COMPANY_NAME = nameCardList.company;

                Intent intent = new Intent(this, RegSalesActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo);
                startActivity(intent);
            }
        } else if (id == R.id.btn_add_contacts) {
            if (!Kit.isContactExists(this, nameCardList.name, nameCardList.mobile)) {
                nameCardImageDown(nameCardList);
                //saveContact(nameCardList);
            } else {
                Toast.makeText(this, "이미 연락처에 등록된 명함입니다.", Toast.LENGTH_SHORT).show();
//                btn_add_contacts.setText("내 휴대폰에 저장 완료");
//                btn_add_contacts.setEnabled(false);
            }
        }
    }

    private void setNameCard(NameCardList nameCardList) {
        if (nameCardList == null) return;

        if (!TextUtils.isEmpty(nameCardList.image)) {
            image_view_crop.setImageResource(0);
            Glide.with(image_view_crop.getContext())
                    .load("https://mfms.esfair.kr/" + nameCardList.image)
                    .placeholder(getDrawable(R.drawable.namecardempty))
                    .error(getDrawable(R.drawable.namecardempty))
                    .into(image_view_crop);
        }

        if (mWORK_FLAG.equals("N") || nameCardList.work_flag.equals("N")) {
            txt_name_view.setText("(퇴직) " + nameCardList.name);
        } else {
            txt_name_view.setText(nameCardList.name);
        }

        txt_company_view.setText(nameCardList.company);
        txt_department_view.setText(nameCardList.department);
        txt_position_view.setText(nameCardList.position);
        txt_phone_view.setText(nameCardList.mobile);
        txt_tel_view.setText(nameCardList.tel);
        txt_email_view.setText(nameCardList.email);
        txt_addr_view.setText(nameCardList.address);
        txt_homepage_view.setText(nameCardList.homepage);
        txt_fax_view.setText(nameCardList.fax);

        txt_addr_view.setAutoLinkMask(Linkify.MAP_ADDRESSES);
        txt_addr_view.setMovementMethod(LinkMovementMethod.getInstance());
        txt_memo_view.setText(nameCardList.memo);

        txt_email_view.setOnTouchListener(copyTouchListener);
        //txt_phone_view.setOnTouchListener(copyTouchListener);
        //txt_tel_view.setOnTouchListener(copyTouchListener);
        txt_addr_view.setOnTouchListener(copyTouchListener);
        txt_memo_view.setOnTouchListener(copyTouchListener);

        if (nameCardList.mobile.isEmpty()) {
            btn_add_contacts.setVisibility(GONE);
        } else {
            btn_add_contacts.setVisibility(VISIBLE);
        }

        if (nameCardList.memo.isEmpty()) {
            memo_layout.setVisibility(VISIBLE);
            txt_memo_view.setHint("담당자를 기억할 수 있는 메모를 남겨주세요.");
        } else {
            memo_layout.setVisibility(VISIBLE);
        }
    }

    private final View.OnTouchListener copyTouchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN && v instanceof TextView) {
            TextView textView = (TextView) v;
            if (textView.getText().length() != 0) {
                setTextClipboard(textView.getText().toString());
            }
        }
        return true;
    };

    private void setTextClipboard(String text) {
        ClipboardManager clipboard =
                (ClipboardManager) getApplicationContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
        if (text != null && !text.isEmpty()) {
            ClipData clipData = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clipData);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S_V2) {
                Toast.makeText(this, "클립보드에 복사했어요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLoadCompanyStaff(String companyStaffId) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_STAFF_ID", companyStaffId);
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        new TelKit(this, this, mProgressBarLayout).request(TelKit.URL_API_OCR_STAFF_DETAIL, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_OCR_STAFF_DETAIL)) {
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
                            nameCardList = null;

                            assert data != null;
                            nameCardList = new NameCardList();
                            nameCardList.work_flag = data.optString("WORK_FLAG");
                            nameCardList.companyId = data.optInt("COMPANY_ID");
                            nameCardList.company = data.optString("COMPANY_NAME");
                            nameCardList.homepage = data.optString("HOMEPAGE");
                            nameCardList.fax = data.optString("FAX");
                            nameCardList.address = data.optString("ADDR");
                            nameCardList.companyStaffId = data.optInt("COMPANY_STAFF_ID");
                            nameCardList.name = data.optString("STAFF_NAME");
                            nameCardList.mobile = data.optString("STAFF_MOBILE");
                            nameCardList.tel = data.optString("STAFF_PHONE");
                            nameCardList.email = data.optString("STAFF_EMAIL");
                            nameCardList.department = data.optString("STAFF_DEPT");
                            nameCardList.position = data.optString("STAFF_POSITION");
                            nameCardList.date = data.optString("UPDATE_DATE");
                            nameCardList.image = data.optString("NAMECARD_URL");
                            nameCardList.memo = data.optString("MEMO_CONTENT");
                            setNameCard(nameCardList);
                        } else {
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_STAFF_HISTORY)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    Log.e(TAG, "URL_API_OCR_STAFF_DETAIL json : " + json);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("success".equals(code)) {
                            JSONArray data_list = json.optJSONArray("data");
                            if (data_list != null) {
                                mHistoryItem.clear();
                                for (int i = 0; i < data_list.length(); i++) {
                                    JSONObject json_list = data_list.optJSONObject(i);
                                    if (json_list != null) {
                                        HistoryItem historyItem = new HistoryItem();
                                        historyItem.ModifyDate = json_list.optString("ModifyDate");
                                        historyItem.Modifyer = json_list.optString("Modifyer");
                                        historyItem.ModifyValue = json_list.optString("ModifyValue");
                                        mHistoryItem.add(historyItem);
                                    }
                                }
                                showHistoryPopup();
                            }
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
        mProgressBarLayout.setVisibility(GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Kit.ActivityManager.unregister(this);
    }

    private void getLoadStaffHistory(String companyStaffId, String type) {
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_STAFF_ID", companyStaffId);
        body.put("TYPE", type);
        //Toast.makeText(this, companyStaffId + " " + type, Toast.LENGTH_SHORT).show();
        new TelKit(this, this, mProgressBarLayout).request(TelKit.URL_API_STAFF_HISTORY, body);
    }

    private void showHistoryPopup() {

        View popupView = LayoutInflater.from(this)
                .inflate(R.layout.popup_history, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new HistoryAdapter(mHistoryItem));
        // ✅ 화면 높이 구하기
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;

        // ✅ 최대 높이 (화면 60%)
        int maxHeight = (int) (screenHeight * 0.6);

        recyclerView.post(() -> {
            if (recyclerView.getHeight() > maxHeight) {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = maxHeight;
                recyclerView.setLayoutParams(params);
            }
        });

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                (int) (metrics.widthPixels * 0.8),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(20f);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.showAtLocation(
                findViewById(android.R.id.content),
                Gravity.CENTER,
                0,
                0
        );
    }

    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> historyItems;

        public HistoryAdapter(List<HistoryItem> historyItems) {
            this.historyItems = historyItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = historyItems.get(position);
            holder.tvDate.setText(item.ModifyDate);
            holder.tvTeam.setText(item.ModifyValue);
            holder.tvDesc.setText(item.Modifyer);
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvTeam, tvDesc;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvTeam = itemView.findViewById(R.id.tvTeam);
                tvDesc = itemView.findViewById(R.id.tvDesc);
            }
        }
    }

    private void showManagerWorkFalgNDialog() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_manager_workflag_n);

        // 바깥 투명 처리
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCancelable(false); // 바깥 터치 막기 (원하면 true)
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
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

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    private void nameCardImageDown(NameCardList nameCardList) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            byte[] imageBytes = null;

            if (nameCardList.image != null && !nameCardList.image.isEmpty()) {

                Bitmap bitmap = getBitmapFromURL("https://mfms.esfair.kr/" + nameCardList.image);

                if (bitmap != null) {
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resized.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    imageBytes = stream.toByteArray();
                }
            }

            byte[] finalImageBytes = imageBytes;

            runOnUiThread(() -> {
                addContact(nameCardList, finalImageBytes);
            });

        });
    }

    private void addContact(NameCardList nameCardList, byte[] imageBytes) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        int rawContactID = 0;
        // RawContact 생성
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
//                        ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .build());

        // 이름
        if (!nameCardList.name.isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nameCardList.name)
                    .build());
        }
        // 휴대폰
        if (!nameCardList.mobile.isEmpty()) {
            addPhone(ops, rawContactID, nameCardList.mobile,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        }
        // 회사전화
        if (!nameCardList.tel.isEmpty()) {
            addPhone(ops, rawContactID, nameCardList.tel,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        }
        // 팩스
//        if (!nameCardList.fax.isEmpty()) {
//            addPhone(ops, rawContactID, nameCardList.fax,
//                    ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK);
//        }
        // 이메일
        if (!nameCardList.email.isEmpty()) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, nameCardList.email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // 회사 / 직급 / 부서
        // 회사 / 직급 / 부서
        String company = nameCardList.company;
        String title = nameCardList.position;
        String department = nameCardList.department;

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
        if (imageBytes != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageBytes)
                    .build());
        }

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "명함 연락처 저장 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "명함 연락처 저장 실패", Toast.LENGTH_SHORT).show();
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

    public static Bitmap getBitmapFromURL(String src) {

        HttpURLConnection connection = null;
        InputStream input = null;

        try {

            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.connect();

            input = connection.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(input);

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        } finally {

            try {
                if (input != null) input.close();
            } catch (IOException ignored) {
            }

            if (connection != null) connection.disconnect();
        }
    }

    private void saveContact(NameCardList nameCardList) {
        lastSavedPhoneNumber = "";
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, nameCardList.name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, nameCardList.mobile);
        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, nameCardList.tel);
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, nameCardList.email);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, nameCardList.company);

        if (!nameCardList.department.isEmpty()) {
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, nameCardList.department + " / " + nameCardList.position);
        } else {
            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, nameCardList.position);
        }
        lastSavedPhoneNumber = nameCardList.mobile;
        // 실행
        startActivityForResult(intent, REQ_INSERT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_INSERT_CONTACT) {

            // 연락처 저장 후 약간 딜레이 주는게 안전
            new Handler().postDelayed(() -> {

                long contactId = getContactIdByPhone(lastSavedPhoneNumber);

                if (contactId != -1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ContactPhotoDown(nameCardList, contactId);
                        }
                    });
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

    private void ContactPhotoDown(NameCardList nameCardList, long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (nameCardList.image != null && !nameCardList.image.isEmpty()) {
                Bitmap bitmap = getBitmapFromURL("https://mfms.esfair.kr/" + nameCardList.image);
                bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, true);
                Bitmap finalBitmap = bitmap;
                runOnUiThread(() -> {
                    updateContactPhoto(id, finalBitmap);
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "내 휴대폰에 저장 완료", Toast.LENGTH_SHORT).show();
//                    btn_add_contacts.setText("내 휴대폰에 저장 완료");
//                    btn_add_contacts.setEnabled(false);
                });

            }
        });
    }

    private void updateContactPhoto(long contactId, Bitmap bitmap) {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (bitmap == null) return;

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
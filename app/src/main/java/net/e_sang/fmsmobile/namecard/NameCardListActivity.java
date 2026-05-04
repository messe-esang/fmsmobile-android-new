package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyStaffInfo;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class NameCardListActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();

    private Button btn_name_card_add = null;
    private EditText editSearch, editSearch_company, editSearch_name, editSearch_email, editSearch_mobile;
    private LinearLayout layoutMatchHeader, layoutMatchBody, progressBar_layout;
    private RecyclerView mRecyclerView = null;
    private TextView txtEmpty, start_date, end_date;
    private Button btnSearch, btnSearchAll;
    private RadioGroup rdogroupSortType;
    private ImageView imgArrow;

    private ArrayList<CompanyStaffInfo> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;

    private SmoothProgressBar mProgressBar = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;
    private String Login_id = "";
    private CheckBox my_search;
    private Calendar startCalendar;
    private Calendar endCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_list_new);
        applyInsets();
        Kit.ActivityManager.register(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        btn_name_card_add = findViewById(R.id.btn_name_card_add);
        btn_name_card_add.setOnClickListener(this);
        editSearch = findViewById(R.id.editSearch);
        editSearch_company = findViewById(R.id.editSearch_company);
        editSearch_name = findViewById(R.id.editSearch_name);
        editSearch_email = findViewById(R.id.editSearch_email);
        editSearch_mobile = findViewById(R.id.editSearch_mobile);
        layoutMatchHeader = findViewById(R.id.layoutMatchHeader);
        layoutMatchBody = findViewById(R.id.layoutMatchBody);
        progressBar_layout = findViewById(R.id.progressBar_layout);
        mRecyclerView = findViewById(R.id.recyclerView);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearchAll = findViewById(R.id.btnSearchAll);
        btnSearchAll.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        rdogroupSortType = findViewById(R.id.rdogroupSortType);
        rdogroupSortType.setOnClickListener(this);
        imgArrow = findViewById(R.id.imgArrow);
        mProgressBar = findViewById(R.id.progressBar);
        my_search = findViewById(R.id.my_search);
        start_date = findViewById(R.id.start_date);
        end_date = findViewById(R.id.end_date);
        start_date.setOnClickListener(this);
        end_date.setOnClickListener(this);

        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_menu_title_6);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        layoutMatchHeader.setOnClickListener(v -> {
            if (layoutMatchBody.getVisibility() == View.GONE) {
                layoutMatchBody.setVisibility(View.VISIBLE);
                rdogroupSortType.setVisibility(INVISIBLE);
                imgArrow.setRotation(180f);
            } else {
                Kit.hideSoftKeyboard(NameCardListActivity.this);
                layoutMatchBody.setVisibility(View.GONE);
                rdogroupSortType.setVisibility(View.INVISIBLE);
                imgArrow.setRotation(0f);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    search(false, editSearch.getText().toString(), editSearch_company.getText().toString(), editSearch_name.getText().toString(), editSearch_email.getText().toString(), editSearch_mobile.getText().toString());
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CompanyStaffInfo companyStaffInfo = mItems.get(position);
                Log.e(TAG, "position: " + position);
                Log.e(TAG, "COMPANY_STAFF_ID: " + companyStaffInfo.COMPANY_STAFF_ID);
                Intent intent = new Intent(NameCardListActivity.this, NameCardViewActivity.class);
                intent.putExtra("COMPANY_STAFF_ID", String.valueOf(companyStaffInfo.COMPANY_STAFF_ID));
                intent.putExtra("WORK_FLAG", companyStaffInfo.WORK_FLAG);
                intent.putExtra("EDIT_TYPE", "1");
                startActivity(intent);
            }
        });

        editSearch.setOnEditorActionListener(searchEditorActionListener);
        editSearch_company.setOnEditorActionListener(searchEditorActionListener);
        editSearch_name.setOnEditorActionListener(searchEditorActionListener);
        editSearch_email.setOnEditorActionListener(searchEditorActionListener);
        editSearch_mobile.setOnEditorActionListener(searchEditorActionListener);
        Kit.hideSoftKeyboard(NameCardListActivity.this);

        my_search.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Login_id = PrefKit.getUserInfo(NameCardListActivity.this).LOGIN_ID;
                } else {
                    Login_id = "";
                }
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.charAt(0) == ' ') {
                    editSearch.setText(s.toString().trim());
                    editSearch.setSelection(editSearch.getText().length());
                    editSearch_company.setText("");
                    editSearch_name.setText("");
                    editSearch_email.setText("");
                    editSearch_mobile.setText("");
                    editSearch_company.setEnabled(true);
                    editSearch_name.setEnabled(true);
                    editSearch_email.setEnabled(true);
                    editSearch_mobile.setEnabled(true);
                } else {
                    editSearch_company.setText("");
                    editSearch_name.setText("");
                    editSearch_email.setText("");
                    editSearch_mobile.setText("");
                    editSearch_company.setEnabled(false);
                    editSearch_name.setEnabled(false);
                    editSearch_email.setEnabled(false);
                    editSearch_mobile.setEnabled(false);
                    if (count == 0) {
                        editSearch_company.setEnabled(true);
                        editSearch_name.setEnabled(true);
                        editSearch_email.setEnabled(true);
                        editSearch_mobile.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 백키 눌렀을 때 처리
                if (layoutMatchBody.getVisibility() == VISIBLE) {
                    layoutMatchBody.setVisibility(GONE);
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_name_card_add) {
            Kit.startActivity(this, NameCardCameraActivity.class);
        } else if (id == R.id.btnSearch || id == R.id.btnSearchAll) {
            Log.e(TAG, "btnSearch");
            String keyword = editSearch.getText().toString().trim();
            String companyKeyword = editSearch_company.getText().toString().trim();
            String nameKeyword = editSearch_name.getText().toString().trim();
            String emailKeyword = editSearch_email.getText().toString().trim();
            String mobileKeyword = editSearch_mobile.getText().toString().trim();

            if (my_search.isChecked()) {
                sendSearch();
            } else if (keyword.isEmpty()
                    && companyKeyword.isEmpty()
                    && nameKeyword.isEmpty()
                    && emailKeyword.isEmpty()
                    && mobileKeyword.isEmpty()) {
                // 5개 중 하나라도 값이 있는지 체크
                Kit.showAlertDialog(
                        NameCardListActivity.this,
                        "검색어를 하나 이상 입력해주세요.",
                        "확인"
                );
            } else {
                sendSearch();
            }
        } else if (id == R.id.rdogroupSortType) {
            Log.e(TAG, "rdogroupSortType");
            if (layoutMatchBody.getVisibility() == View.GONE) {
                layoutMatchBody.setVisibility(View.VISIBLE);
                rdogroupSortType.setVisibility(INVISIBLE);
                imgArrow.setRotation(180f);
            } else {
                Kit.hideSoftKeyboard(NameCardListActivity.this);
                layoutMatchBody.setVisibility(View.GONE);
                rdogroupSortType.setVisibility(INVISIBLE);
                imgArrow.setRotation(0f);
            }
        } else if (id == R.id.start_date) {
            startCalendar = Calendar.getInstance();
            DatePickerDialog startDialog = new DatePickerDialog(
                    NameCardListActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        startCalendar.set(year, month, dayOfMonth);
                        String startDate = sdf.format(startCalendar.getTime());
                        start_date.setText(startDate);
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH)
            );
            startDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000);

            if (!isFinishing()) {
                startDialog.show();
            }
        } else if (id == R.id.end_date) {
            if (startCalendar == null || start_date.getText().toString().isEmpty()) {
                Toast.makeText(this, "시작 날짜 먼저 선택", Toast.LENGTH_SHORT).show();
                return;
            }

            endCalendar = Calendar.getInstance();

            DatePickerDialog endDialog = new DatePickerDialog(
                    NameCardListActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        endCalendar.set(year, month, dayOfMonth);
                        String endDate = sdf.format(endCalendar.getTime());
                        end_date.setText(endDate);
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
            );

            long startTime = startCalendar.getTimeInMillis();
            endDialog.getDatePicker().setMaxDate(startTime);

            if (!isFinishing()) {
                endDialog.show();
            }
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<CompanyStaffInfo> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<CompanyStaffInfo> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_namecard_list_item, parent, false);
            return new ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            CompanyStaffInfo companyStaffInfo = mItemList.get(position);

            if (companyStaffInfo.WORK_FLAG.equals("Y")) {
                holder.tvName.setText(companyStaffInfo.STAFF_NAME);
            } else {
                holder.tvName.setText("(퇴직) " + companyStaffInfo.STAFF_NAME);
            }

            holder.tvCompany.setText(companyStaffInfo.COMPANY_NAME);

            if (Kit.isNotNullNotEmpty(companyStaffInfo.STAFF_POSITION)) {
                holder.tvLine.setVisibility(View.VISIBLE);
                holder.tvPosition.setText(companyStaffInfo.STAFF_POSITION);
            } else {
                holder.tvLine.setVisibility(View.GONE);
                holder.tvPosition.setText("");
            }

            if (Kit.isNotNullNotEmpty(companyStaffInfo.UPDATE_DATE) && !companyStaffInfo.UPDATE_DATE.equals("-")) {
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvDate.setText("최종 수정 : " + companyStaffInfo.UPDATE_DATE);
            } else {
                holder.tvDate.setVisibility(INVISIBLE);

            }

            holder.tvMobile.setText(companyStaffInfo.STAFF_MOBILE);

            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            txtEmpty.setVisibility(count > 0 ? INVISIBLE : VISIBLE);

            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RecycleAdapter mAdapter;
            public LinearLayout layoutItem;
            public TextView tvName, tvDate, tvPosition, tvLine, tvCompany, tvMobile;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.tvName = itemView.findViewById(R.id.tvName);
                this.tvCompany = itemView.findViewById(R.id.tvCompany);
                this.tvDate = itemView.findViewById(R.id.tvDate);
                this.tvPosition = itemView.findViewById(R.id.tvPosition);
                this.tvLine = itemView.findViewById(R.id.tvLine);
                this.tvMobile = itemView.findViewById(R.id.tvMobile);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void search(boolean isInit, String keyword, String companyName, String staffName, String staffEmail, String staffMobile) {
        layoutMatchBody.setVisibility(View.GONE);
        rdogroupSortType.setVisibility(INVISIBLE);
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mAdapter.notifyDataSetChanged();
        }
        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);

            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE", String.valueOf(++page));
            body.put("PAGE_SIZE", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("COMPANY_NAME", companyName);
            body.put("STAFF_NAME", staffName);
            body.put("STAFF_EMAIL", staffEmail);
            body.put("STAFF_MOBILE", staffMobile);
            body.put("KEYWORD", keyword);
            body.put("START_DATE", start_date.getText().toString());
            body.put("END_DATE", end_date.getText().toString());
            body.put("USER_ID", Login_id);
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_OCR_STAFF_LIST, body);
            } else {
                new TelKit(this, this, progressBar_layout).request(TelKit.URL_API_OCR_STAFF_LIST, body);
            }
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_OCR_STAFF_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    Log.e(TAG, "json: " + json.toString());
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        Log.e(TAG, "code: " + code);
                        if ("success".equalsIgnoreCase(code)) {
                            JSONArray data_list = json.optJSONArray("data");
                            if (data_list != null) {
                                for (int i = 0; i < data_list.length(); i++) {
                                    JSONObject json_list = data_list.optJSONObject(i);
                                    if (json_list != null) {
                                        CompanyStaffInfo companyStaffInfo = new CompanyStaffInfo();
                                        companyStaffInfo.COMPANY_STAFF_ID = json_list.optInt("COMPANY_STAFF_ID");
                                        companyStaffInfo.COMPANY_ID = json_list.optInt("COMPANY_ID");
                                        companyStaffInfo.COMPANY_NAME = json_list.optString("COMPANY_NAME");
                                        companyStaffInfo.STAFF_NAME = json_list.optString("STAFF_NAME");
                                        companyStaffInfo.STAFF_MOBILE = json_list.optString("STAFF_MOBILE");
                                        companyStaffInfo.STAFF_PHONE = json_list.optString("STAFF_PHONE");
                                        companyStaffInfo.STAFF_EMAIL = json_list.optString("STAFF_EMAIL");
                                        companyStaffInfo.STAFF_DEPT = json_list.optString("STAFF_DEPT");
                                        companyStaffInfo.STAFF_POSITION = json_list.optString("STAFF_POSITION");
                                        companyStaffInfo.UPDATE_DATE = json_list.optString("UPDATE_DATE");
                                        companyStaffInfo.TM_COUNT = json_list.optInt("TM_COUNT");
                                        companyStaffInfo.WORK_FLAG = json_list.optString("WORK_FLAG");
                                        mItems.add(companyStaffInfo);
                                    }
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "데이터를 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
            }
            btnSearchAll.setEnabled(true);
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
        progressBar_layout.setVisibility(GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Kit.ActivityManager.unregister(this);
    }

    private final TextView.OnEditorActionListener searchEditorActionListener =
            new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String keyword = editSearch.getText().toString().trim();
                        String companyKeyword = editSearch_company.getText().toString().trim();
                        String nameKeyword = editSearch_name.getText().toString().trim();
                        String emailKeyword = editSearch_email.getText().toString().trim();
                        String mobileKeyword = editSearch_mobile.getText().toString().trim();

                        // 5개 중 하나라도 값이 있는지 체크
                        if (my_search.isChecked()) {
                            sendSearch();
                        } else if (keyword.isEmpty()
                                && companyKeyword.isEmpty()
                                && nameKeyword.isEmpty()
                                && emailKeyword.isEmpty()
                                && mobileKeyword.isEmpty()) {

                            Kit.showAlertDialog(
                                    NameCardListActivity.this,
                                    "검색어를 하나 이상 입력해주세요.",
                                    "확인"
                            );
                        } else {
                            sendSearch();
                        }
                        return true;
                    }
                    return false;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add("등록").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        getMenuInflater().inflate(R.menu.name_card_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add || "등록".equals(item.getTitle())) {
            try {
                Kit.startActivity(this, NameCardCameraActivity.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    private void sendSearch() {
        btnSearchAll.setEnabled(false);
        Kit.hideSoftKeyboard(NameCardListActivity.this);
        layoutMatchBody.setVisibility(View.GONE);
        rdogroupSortType.setVisibility(INVISIBLE);
        search(true, editSearch.getText().toString(), editSearch_company.getText().toString(), editSearch_name.getText().toString(), editSearch_email.getText().toString(), editSearch_mobile.getText().toString());
    }
}
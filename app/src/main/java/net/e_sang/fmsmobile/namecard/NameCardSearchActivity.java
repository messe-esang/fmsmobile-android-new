package net.e_sang.fmsmobile.namecard;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.CompanyStaffInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.ui.BaseActivity;
import net.e_sang.fmsmobile.ui.NameCardRegSalesActivity;
import net.e_sang.fmsmobile.ui.RegSalesActivity;
import net.e_sang.fmsmobile.ui.SearchActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class NameCardSearchActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mPhoneRecyclerView = null;
    private RecyclerView mRecyclerView = null;
    private ArrayList<CompanyInfo> mItems = new ArrayList<>();
    private ArrayList<CompanyStaffInfo> mPhoneItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private PhoneRecycleAdapter mPhoneAdapter = null;
    private EditText mEditSearch = null;
    private String mSearchType = "";
    private LinearLayout mProgressBarLayout, progressBar_Phone_layout = null;
    private NameCardList nameCardList = null;
    private LinearLayout btn_new_company_layout;

    private LinearLayout txtEmpty_layout, txtEmptyPhone_layout, layoutPhoneHeader, layoutCompanyHeader;
    private ImageView imgPhoneArrow, imgCompanyArrow;
    private TextView txt_phone_count, txt_company_count, id_company;
    private int phone_count, company_count;

    private int pageMobile = 0;
    private boolean mCanLoadMoreMobile = true;
    private boolean isLoadingMobile = false;
    private SmoothProgressBar progressBar_Phone = null;

    private int pageCompany = 0;
    private boolean mCanLoadMoreCompany = true;
    private boolean isLoadingCompany = false;
    private SmoothProgressBar progressBar_Company = null;
    private boolean isFirst = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_search);
        applyInsets();
        Kit.ActivityManager.register(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        mEditSearch = findViewById(R.id.editSearch);
        mRecyclerView = findViewById(R.id.recyclerView);
        mPhoneRecyclerView = findViewById(R.id.phoneRecyclerView);

        mProgressBarLayout = findViewById(R.id.progressBar_layout);
        progressBar_Company = findViewById(R.id.progressBar_Company);

        btn_new_company_layout = findViewById(R.id.btn_new_company_layout);
        btn_new_company_layout.setOnClickListener(this);

        txtEmpty_layout = findViewById(R.id.txtEmpty_layout);
        txtEmptyPhone_layout = findViewById(R.id.txtEmptyPhone_layout);
        progressBar_Phone_layout = findViewById(R.id.progressBar_Phone_layout);
        progressBar_Phone = findViewById(R.id.progressBar_Phone);
        layoutPhoneHeader = findViewById(R.id.layoutPhoneHeader);
        layoutCompanyHeader = findViewById(R.id.layoutCompanyHeader);
        txt_phone_count = findViewById(R.id.phone_count);
        txt_company_count = findViewById(R.id.company_count);
        imgPhoneArrow = findViewById(R.id.imgPhoneArrow);
        imgCompanyArrow = findViewById(R.id.imgCompanyArrow);
        id_company = findViewById(R.id.id_company);


        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_name_card_search);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CompanyInfo companyInfo = mItems.get(position);
                Intent intent = new Intent(NameCardSearchActivity.this, NameCardRegSalesActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra("TYPE", "company");
                intent.putExtra("NameCard", nameCardList);
                intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo);
                startActivity(intent);
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoadingCompany && !mRecyclerView.canScrollVertically(1)) {
                    search(false, mEditSearch.getText().toString());
                    isLoadingCompany = true;
                }
            }
        });

        LinearLayoutManager layoutManagerPhone = new LinearLayoutManager(this);
        layoutManagerPhone.setOrientation(LinearLayoutManager.VERTICAL);
        mPhoneRecyclerView.setLayoutManager(layoutManagerPhone);
        mPhoneAdapter = new PhoneRecycleAdapter(this, mPhoneItems);
        mPhoneRecyclerView.setAdapter(mPhoneAdapter);
        mPhoneAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CompanyStaffInfo companyStaffInfo = mPhoneItems.get(position);
                Intent intent = new Intent(NameCardSearchActivity.this, NameCardRegSalesActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra("TYPE", "phone");
                intent.putExtra("NameCard", nameCardList);
                intent.putExtra(Extra.KEY_COMPANY_STAFF_INFO, companyStaffInfo);
                startActivity(intent);
            }
        });
        mPhoneRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoadingMobile && !mPhoneRecyclerView.canScrollVertically(1)) {
                    phoneSearch(false, nameCardList.name, nameCardList.mobile, nameCardList.email);
                    isLoadingMobile = true;
                }
            }
        });

        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString();
                    if (keyword.isEmpty()) {
                        Kit.showAlertDialog(NameCardSearchActivity.this, "검색어를 입력해주세요.", "확인");
                    } else {
                        search(true, keyword);
                        Kit.hideSoftKeyboard(NameCardSearchActivity.this);
                    }
                    return true;
                }
                return false;
            }
        });
//        mEditSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                search(true,s.toString());
//                if (count == 0) {
//                    mItems.clear();
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            nameCardList = getIntent().getParcelableExtra("NameCard");
            if (nameCardList != null) {
                TextView name = findViewById(R.id.id_name);
                TextView mobile = findViewById(R.id.id_mobile);
                name.setText(nameCardList.name);
                mobile.setText(nameCardList.mobile);
                id_company.setText(nameCardList.company);
                Log.e(TAG, "companyName: " + nameCardList.company);
                Log.e(TAG, "companyId: " + nameCardList.companyId);

                phoneSearch(true, nameCardList.name, nameCardList.mobile, nameCardList.email);
                searchCompany(nameCardList.company);
            }
        }

        layoutPhoneHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhoneRecyclerView.getVisibility() == View.VISIBLE) {
                    mPhoneRecyclerView.setVisibility(View.GONE);
                    imgPhoneArrow.setRotation(0);
                    txtEmptyPhone_layout.setVisibility(GONE);
                } else {
                    mPhoneRecyclerView.setVisibility(View.VISIBLE);
                    imgPhoneArrow.setRotation(180);
                    if (phone_count == 0) {
                        txtEmptyPhone_layout.setVisibility(VISIBLE);
                    }
                    mRecyclerView.setVisibility(View.GONE);
                    mEditSearch.setVisibility(GONE);
                    imgCompanyArrow.setRotation(0);
                    txtEmpty_layout.setVisibility(GONE);
                }
            }
        });

        layoutCompanyHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirst = false;
                if (mRecyclerView.getVisibility() == View.VISIBLE) {
                    mRecyclerView.setVisibility(View.GONE);
                    mEditSearch.setVisibility(GONE);
                    imgCompanyArrow.setRotation(0);
                    txtEmpty_layout.setVisibility(GONE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mEditSearch.setVisibility(VISIBLE);
                    imgCompanyArrow.setRotation(180);
                    if (company_count == 0) {
                        txtEmpty_layout.setVisibility(VISIBLE);
                    }
                    mPhoneRecyclerView.setVisibility(View.GONE);
                    imgPhoneArrow.setRotation(0);
                    txtEmptyPhone_layout.setVisibility(GONE);
                }
            }
        });

        mPhoneRecyclerView.setVisibility(View.VISIBLE);
        imgPhoneArrow.setRotation(180);

        mRecyclerView.setVisibility(View.INVISIBLE);
        mEditSearch.setVisibility(GONE);
        imgCompanyArrow.setRotation(0);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_new_company_layout) {
            Log.e(TAG, "image : " + nameCardList.image);
            Intent intent = new Intent(NameCardSearchActivity.this, NewCompanyActivity.class);
            intent.putExtra("NameCard", nameCardList);
            intent.putExtra("entry_path", "search");
            setResult(RESULT_OK, intent);
            startActivity(intent);
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<CompanyInfo> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<CompanyInfo> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_namecard_company_list, parent, false);
            return new ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            CompanyInfo companyInfo = mItemList.get(position);

            holder.tvCompany.setText(companyInfo.COMPANY_NAME);

            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            if (!isFirst) {
                txtEmpty_layout.setVisibility(count > 0 ? GONE : VISIBLE);
            }
            company_count = count;
            txt_company_count.setText("(" + count + ")");
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
            public TextView tvCompany;

            public ItemViewHolder(View itemView, RecycleAdapter adapter) {
                super(itemView);

                this.mAdapter = adapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.tvCompany = itemView.findViewById(R.id.tvCompany);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void phoneSearch(boolean isInit, String name, String mobile, String email) {
        mProgressBarLayout.setVisibility(VISIBLE);
        progressBar_Phone_layout.setVisibility(VISIBLE);
        if (isInit) {
            mCanLoadMoreMobile = true;
            pageMobile = 0;
            mPhoneItems.clear();
            mPhoneAdapter.notifyDataSetChanged();
        }
        if (mCanLoadMoreMobile) {
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE", String.valueOf(++pageMobile));
            body.put("PAGE_SIZE", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("STAFF_MOBILE", mobile);
            if (pageMobile > 1) {
                new TelKit(this, this, progressBar_Phone).request(TelKit.URL_API_OCR_STAFF_LIST, body);
            } else {
                new TelKit(this, this, progressBar_Phone_layout).request(TelKit.URL_API_OCR_STAFF_LIST, body);
            }
        }
    }

    protected void search(boolean isInit, String keyword) {
        if (isInit || keyword.isEmpty()) {
            mCanLoadMoreCompany = true;
            pageCompany = 0;
            mItems.clear();
            mAdapter.notifyDataSetChanged();
        }
        if (mCanLoadMoreCompany) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            Log.e(TAG, "userInfo.SYS_ID: " + userInfo.SYS_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("page_view_count", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("current_page_index", String.valueOf(++pageCompany));
            body.put("company_name", keyword);
            body.put("system_id", userInfo.SYS_ID);
            body.put("search_type", "1");
            if (pageCompany > 1) {
                new TelKit(this, this, progressBar_Company).request(TelKit.URL_API_GET_COMPANY_LIST, body);
            } else {
                new TelKit(this, this, mProgressBarLayout).request(TelKit.URL_API_GET_COMPANY_LIST, body);
            }
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_COMPANY_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        //mItems.clear();
                        Log.e(TAG, "code: " + code);
                        if ("ok".equalsIgnoreCase(code)) {
                            JSONArray list = json.optJSONArray("list");
                            Log.e(TAG, "list: " + list.toString());
                            if (list != null) {
                                for (int i = 0; i < list.length(); i++) {
                                    Object obj = list.optJSONObject(i);
                                    if (obj != null) {
                                        JSONObject c = (JSONObject) obj;
                                        CompanyInfo companyInfo = new CompanyInfo();
                                        companyInfo.COMPANY_ID = c.optInt("COMPANY_ID");
                                        companyInfo.COMPANY_NAME = c.optString("COMPANY_NAME");
                                        companyInfo.LAST_FAIR_DESC = c.optString("LAST_FAIR_DESC");
                                        mItems.add(companyInfo);
                                    }
                                }
                            }
                            isLoadingCompany = false;
                        } else {
                            mCanLoadMoreCompany = false;
                            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "데이터를 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_OCR_STAFF_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    Log.e(TAG, "json: " + json.toString());
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        //mPhoneItems.clear();
                        Log.e(TAG, "code: " + code);
                        if ("success".equalsIgnoreCase(code)) {
                            JSONArray list = json.optJSONArray("data");
                            Log.e(TAG, "data: " + list.toString());
                            if (list != null) {
                                for (int i = 0; i < list.length(); i++) {
                                    Object obj = list.optJSONObject(i);
                                    if (obj != null) {
                                        JSONObject c = (JSONObject) obj;
                                        CompanyStaffInfo companyStaffInfo = new CompanyStaffInfo();
                                        companyStaffInfo.COMPANY_STAFF_ID = c.optInt("COMPANY_STAFF_ID");
                                        companyStaffInfo.COMPANY_ID = c.optInt("COMPANY_ID");
                                        companyStaffInfo.COMPANY_NAME = c.optString("COMPANY_NAME");
                                        companyStaffInfo.STAFF_NAME = c.optString("STAFF_NAME");
                                        companyStaffInfo.STAFF_MOBILE = c.optString("STAFF_MOBILE");
                                        companyStaffInfo.STAFF_PHONE = c.optString("STAFF_PHONE");
                                        companyStaffInfo.STAFF_EMAIL = c.optString("STAFF_EMAIL");
                                        companyStaffInfo.STAFF_DEPT = c.optString("STAFF_DEPT");
                                        companyStaffInfo.STAFF_POSITION = c.optString("STAFF_POSITION");
                                        companyStaffInfo.UPDATE_DATE = c.optString("UPDATE_DATE");
                                        companyStaffInfo.TM_COUNT = c.optInt("TM_COUNT");
                                        companyStaffInfo.WORK_FLAG = c.optString("WORK_FLAG");
                                        mPhoneItems.add(companyStaffInfo);
                                    }
                                }
                            }
                            isLoadingMobile = false;
                        } else {
                            mCanLoadMoreMobile = false;
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mPhoneAdapter.notifyDataSetChanged();
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
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
        mProgressBarLayout.setVisibility(GONE);
        progressBar_Phone_layout.setVisibility(GONE);
    }

    public class PhoneRecycleAdapter extends RecyclerView.Adapter<PhoneRecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<CompanyStaffInfo> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public PhoneRecycleAdapter(Context context, List<CompanyStaffInfo> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_namecard_list, parent, false);
            return new ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            CompanyStaffInfo companyStaffInfo = mPhoneItems.get(position);

            if (companyStaffInfo.WORK_FLAG.equals("Y")) {
                holder.tvName.setText(companyStaffInfo.STAFF_NAME);
            } else {
                holder.tvName.setText("(퇴직) " + companyStaffInfo.STAFF_NAME);
            }

            holder.tvPhone.setText(companyStaffInfo.STAFF_MOBILE);
            holder.tvEmail.setText(companyStaffInfo.STAFF_EMAIL);
            holder.tvCompany.setText(companyStaffInfo.COMPANY_NAME + " / " + companyStaffInfo.STAFF_POSITION);
            holder.tvCount.setText(companyStaffInfo.TM_COUNT + "건");
            if (Kit.isNotNullNotEmpty(companyStaffInfo.UPDATE_DATE) && !companyStaffInfo.UPDATE_DATE.equals("-")) {
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvDate.setText("최종 수정 : " + companyStaffInfo.UPDATE_DATE);
            } else {
                holder.tvDate.setVisibility(View.INVISIBLE);
            }

            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            txtEmptyPhone_layout.setVisibility(count > 0 ? GONE : VISIBLE);
            phone_count = count;
            txt_phone_count.setText("(" + count + ")");
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
            public PhoneRecycleAdapter mPhoneAdapter;
            public LinearLayout layoutItem;
            public TextView tvName;
            public TextView tvPhone;
            public TextView tvEmail;
            public TextView tvCompany;
            public TextView tvCount;
            public TextView tvDate;


            public ItemViewHolder(View itemView, PhoneRecycleAdapter mAdapter) {
                super(itemView);

                this.mPhoneAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.tvName = itemView.findViewById(R.id.tvName);
                this.tvPhone = itemView.findViewById(R.id.tvPhone);
                this.tvEmail = itemView.findViewById(R.id.tvEmail);
                this.tvCompany = itemView.findViewById(R.id.tvCompany);
                this.tvCount = itemView.findViewById(R.id.tvCount);
                this.tvDate = itemView.findViewById(R.id.tvDate);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mPhoneAdapter.onItemHolderClick(this);
            }
        }
    }

    private void searchCompany(String companyName) {
        mEditSearch.setText(companyName);
        mEditSearch.setSelection(mEditSearch.length());
        search(true, mEditSearch.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Kit.ActivityManager.unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
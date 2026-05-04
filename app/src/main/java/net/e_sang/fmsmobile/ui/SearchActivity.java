package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;


import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class SearchActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<CompanyInfo> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private EditText mEditSearch, mEditPhoneSearch = null;
    private Button btnPhoneSearch = null;
    private String mSearchType = "";
    private String mSearchText = "";
    private LinearLayout mProgressBarLayout = null;

    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;
    private SmoothProgressBar smoothprogressbar = null;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        mEditSearch = findViewById(R.id.editSearch);
        mRecyclerView = findViewById(R.id.recyclerView);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mEditPhoneSearch = findViewById(R.id.editPhoneSearch);
        btnPhoneSearch = findViewById(R.id.btnPhoneSearch);
        btnPhoneSearch.setOnClickListener(this);
        mProgressBarLayout = findViewById(R.id.progressBar_layout);
        smoothprogressbar = findViewById(R.id.smoothprogressbar);

        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_search_company);
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
                Intent intent = new Intent(SearchActivity.this, RegSalesActivity.class);
//                Intent intent = new Intent(SearchActivity.this, RegSalesOutlineActivity.class);
//                Intent intent = new Intent(SearchActivity.this, RegSalesBoxActivity.class);
                intent.putExtra("entry_path", "search");
                intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo);
                startActivity(intent);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    search(false, mSearchText, mSearchType);
                    isLoading = true;
                }
            }
        });

        mEditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString();
                    if (keyword.isEmpty()) {
                        Kit.showAlertDialog(SearchActivity.this, "검색어를 입력해주세요.", "확인");
                    } else {
                        search(true, keyword, "1");
                        Kit.hideSoftKeyboard(SearchActivity.this);
                    }
                    return true;
                }
                return false;
            }
        });
        mEditSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    search(true, s.toString(), "1");
                };

                handler.postDelayed(searchRunnable, 300); // 300ms 딜레이
            }
        });

        mEditPhoneSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = v.getText().toString();
                    if (keyword.isEmpty()) {
                        Kit.showAlertDialog(SearchActivity.this, "전화번호를 입력해주세요.", "확인");
                    } else {
                        search(true, keyword, "2");
                        Kit.hideSoftKeyboard(SearchActivity.this);
                    }
                    return true;
                }
                return false;
            }
        });
        mEditSearch.requestFocus();
    }

    // View.OnClickListener
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnPhoneSearch) {
            if (mEditPhoneSearch.getText().toString().isEmpty()) {
                Kit.showAlertDialog(SearchActivity.this, "전화번호를 입력해주세요.", "확인");
            } else {
                search(true, mEditPhoneSearch.getText().toString(), "2");
                Kit.hideSoftKeyboard(SearchActivity.this);
            }
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
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_search_company, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            CompanyInfo companyInfo = mItemList.get(position);

            holder.txtName.setText(companyInfo.COMPANY_NAME);

            if (Kit.isNotNullNotEmpty(companyInfo.LAST_FAIR_DESC)) {
                holder.txtFair.setText(companyInfo.LAST_FAIR_DESC);
            } else {
                holder.txtFair.setText("없음");
            }
            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(RecycleAdapter.ItemViewHolder itemHolder) {
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
            public TextView txtName;
            public TextView txtFair;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.txtName = itemView.findViewById(R.id.txtName);
                this.txtFair = itemView.findViewById(R.id.txtFair);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void search(boolean isInit, String keyword, String type) {
        mSearchType = type;
        mSearchText = keyword;
        if (keyword.isEmpty()) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            updateAdapter();
        } else {
            if (isInit) {
                mCanLoadMore = true;
                page = 0;
                mItems.clear();
                updateAdapter();
            }
            if (mCanLoadMore) {
                UserInfo userInfo = PrefKit.getUserInfo(this);
                HashMap<String, String> body = new HashMap<>();
                body.put("page_view_count", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
                body.put("current_page_index", String.valueOf(++page));
                body.put("company_name", keyword);
                body.put("system_id", userInfo.SYS_ID);
                body.put("search_type", type);

                if (page > 1) {
                    new TelKit(this, this, smoothprogressbar).request(TelKit.URL_API_GET_COMPANY_LIST, body);
                } else {
                    new TelKit(this, this, mProgressBarLayout).request(TelKit.URL_API_GET_COMPANY_LIST, body);
                }
            }
        }
    }

    private void updateAdapter() {
        if (mRecyclerView == null || mAdapter == null) return;

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    mAdapter.notifyDataSetChanged();
                    if (mTxtEmpty != null) {
                        mTxtEmpty.setVisibility(mItems.size() > 0 ? View.INVISIBLE : View.VISIBLE);
                    }
                }
            }
        });
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
                        if ("ok".equalsIgnoreCase(code)) {
                            JSONArray list = json.optJSONArray("list");
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
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            if (mSearchType.equals("2")) {
                                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                        updateAdapter();
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
        mProgressBarLayout.setVisibility(View.GONE);
    }
}
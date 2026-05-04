package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.ReceivableList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class ReceivableActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<ReceivableList> mItems = new ArrayList<>();
    private ReceivableActivity.RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty, mFair_name, mReceivable_title_all_money = null;
    private String FAIR_ID = "";
    private String FAIR_NAME = "";
    private String TotalFairAmt = "";
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_receivable);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivable);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_receivable));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("채권업체");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            //TotalFairAmt = intent.getExtras().getString("TotalFairAmt");
            Log.e(TAG, "FAIR_ID : " + FAIR_ID);
            Log.e(TAG, "FAIR_NAME : " + FAIR_NAME);
            Log.e(TAG, "TotalFairAmt : " + TotalFairAmt);
        }

        mRecyclerView = findViewById(R.id.receivable_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mFair_name = findViewById(R.id.fair_name);
        mReceivable_title_all_money = findViewById(R.id.receivable_title_all_money);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ReceivableActivity.RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
//                    geReceivableList(false);
//                    isLoading = true;
//                }
//            }
//        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                ReceivableList eventList = mItems.get(position);
                Intent intent = new Intent(getApplicationContext(), ReceivableDetailActivity.class);
                intent.putExtra("FAIR_ID", eventList.FAIR_ID);
                intent.putExtra("COMPANY_ID", eventList.COMPANY_ID);
                intent.putExtra("COMPANY_FAIR_REQ_ID", eventList.COMPANY_FAIR_REQ_ID);
                intent.putExtra("BALANCE_AMT", eventList.BALANCE_AMT);
                intent.putExtra("DEPOSIT_AMT", eventList.DEPOSIT_AMT);
                startActivity(intent);
            }
        });

        mFair_name.setText(FAIR_NAME);
        mReceivable_title_all_money.setText(TotalFairAmt);

        geReceivableList(true);
    }

    protected void geReceivableList(boolean isInit) {
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }
        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%s&FAIR_ID=%s", MyApplication.PAGE_VIEW_COUNT, ++page, FAIR_ID);
            //String body = String.format("FAIR_ID=%s&SYSTEM_ID=%s", FAIR_ID, userInfo.SYS_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("FAIR_ID", FAIR_ID);
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_RECEIVABLE_LIST, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_RECEIVABLE_LIST, body);
            }
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_RECEIVABLE_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");

                        if ("ok".equals(code)) {
                            String resultAmount = json.optString("receivable_amount");
                            String resultList = json.optString("receivable_list");

                            JSONObject resultAmount_obj = new JSONObject(resultAmount);
                            String fair_name = resultAmount_obj.optString("FAIR_NAME");
                            String balance_cnt = resultAmount_obj.optString("BALANCE_CNT");
                            String balance_amt = resultAmount_obj.optString("BALANCE_AMT");
                            Log.e(TAG, "fair_name: " + fair_name);
                            Log.e(TAG, "balance_cnt: " + balance_cnt);
                            Log.e(TAG, "balance_amt: " + balance_amt);

                            mFair_name.setText(fair_name);
                            mReceivable_title_all_money.setText(convertCurrencyStr(Double.parseDouble(balance_amt)));

                            JSONArray jsonArray = new JSONArray(resultList);
                            Log.e(TAG, "JSONObject resultList: " + resultList);
                            Log.e(TAG, "JSONObject jsonArray: " + jsonArray);
                            Log.e(TAG, "JSONObject jsonArray.length(): " + jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    ReceivableList receivableList = new ReceivableList();

                                    receivableList.FAIR_ID = json_list.optString("FAIR_ID");
                                    receivableList.COMPANY_FAIR_REQ_ID = json_list.optString("COMPANY_FAIR_REQ_ID");
                                    receivableList.COMPANY_ID = json_list.optString("COMPANY_ID");
                                    receivableList.COMPANY_NAME = json_list.optString("COMPANY_NAME");
                                    receivableList.FAIR_AMT = convertCurrencyStr(Double.parseDouble(json_list.optString("FAIR_AMT").replace("null", "0")));
                                    receivableList.DEPOSIT_AMT = convertCurrencyStr(Double.parseDouble(json_list.optString("DEPOSIT_AMT").replace("null", "0")));
                                    receivableList.BALANCE_AMT = convertCurrencyStr(Double.parseDouble(json_list.optString("BALANCE_AMT").replace("null", "0")));
                                    mItems.add(receivableList);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(ReceivableActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "채권업체 목록을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReceivableActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReceivableActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ReceivableActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<ReceivableActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<ReceivableList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<ReceivableList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public ReceivableActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.receivable_list_item, parent, false);
            return new ReceivableActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ReceivableActivity.RecycleAdapter.ItemViewHolder holder, int position) {
            ReceivableList receivablelist = mItemList.get(position);

            Resources res = getResources();
            holder.receivable_title.setText(receivablelist.COMPANY_NAME);
            holder.receivable_title_money.setText(receivablelist.BALANCE_AMT);
            holder.receivable_all_money.setText(receivablelist.FAIR_AMT);
            holder.receivable_money.setText(receivablelist.DEPOSIT_AMT);
            if (mOnClickListener != null) {
                holder.receivable_list_item_layout.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            mTxtEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(ReceivableActivity.RecycleAdapter.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ReceivableActivity.RecycleAdapter mAdapter;
            public LinearLayout receivable_list_item_layout;
            public TextView receivable_title;
            public TextView receivable_title_money;
            public TextView receivable_all_money;
            public TextView receivable_money;

            public ItemViewHolder(View itemView, ReceivableActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.receivable_list_item_layout = itemView.findViewById(R.id.receivable_list_item_layout);
                this.receivable_title = itemView.findViewById(R.id.receivable_title);
                this.receivable_title_money = itemView.findViewById(R.id.receivable_title_money);
                this.receivable_all_money = itemView.findViewById(R.id.receivable_all_money);
                this.receivable_money = itemView.findViewById(R.id.receivable_money);
                receivable_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
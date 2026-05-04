package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;

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
import android.widget.*;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class ReceivableListActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<ReceivableEventList> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_receivable);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivable_list);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_receivable));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("채권관리");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.receivable_event_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

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
                    geReceivableFairList(false);
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                ReceivableEventList receivableEventList = mItems.get(position);
                Intent intent = new Intent(getApplicationContext(), ReceivableActivity.class);
                intent.putExtra("FAIR_ID", receivableEventList.FAIR_ID);
                intent.putExtra("FAIR_NAME", receivableEventList.FAIR_NAME);
                //intent.putExtra("TotalFairAmt", receivableEventList.TotalFairAmt.replace("null", "0"));
                startActivity(intent);
            }
        });

        geReceivableFairList(true);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_REC_DASH)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");

                        if ("ok".equals(code)) {
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    ReceivableEventList receivableEventList = new ReceivableEventList();

                                    receivableEventList.FAIR_ID = json_list.optString("FAIR_ID");
                                    receivableEventList.FAIR_MASTER_ID = json_list.optString("FAIR_MASTER_ID");
                                    receivableEventList.FAIR_DESC = json_list.optString("FAIR_DESC");
                                    receivableEventList.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    //receivableEventList.FAIR_STR_DATE = json_list.optString("FAIR_STR_DATE");
                                    //receivableEventList.FAIR_END_DATE = json_list.optString("FAIR_END_DATE");
                                    receivableEventList.CompanyCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("TotalRecCompanyCnt").replace("null", "0")));
                                    //receivableEventList.BoothCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("BoothCnt").replace("null", "0")));
                                    receivableEventList.TotalFairAmt = convertCurrencyStr(Double.parseDouble(json_list.optString("TotalRecAmt").replace("null", "0")));
                                    //receivableEventList.BalanceCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("BalanceCnt").replace("null", "0")));
                                    //receivableEventList.BalanceAmt = convertCurrencyStr(Double.parseDouble(json_list.optString("BalanceAmt").replace("null", "0")));
                                    mItems.add(receivableEventList);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(ReceivableListActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "채권관리 목록이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReceivableListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReceivableListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ReceivableListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<ReceivableEventList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<ReceivableEventList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.receivable_event_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            ReceivableEventList receivableeventlist = mItemList.get(position);

            holder.receivable_event_title.setText(receivableeventlist.FAIR_NAME);
            holder.balance_cnt.setText(receivableeventlist.CompanyCnt.replace("null", "0"));
            holder.balance_amt.setText(receivableeventlist.TotalFairAmt.replace("null", "0"));
            if (mOnClickListener != null) {
                holder.receivable_event_list_item_layout.setOnClickListener(mOnClickListener);
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
            public LinearLayout receivable_event_list_item_layout;
            public TextView receivable_event_title;
            public TextView balance_cnt;
            public TextView balance_amt;


            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.receivable_event_list_item_layout = itemView.findViewById(R.id.receivable_event_list_item_layout);
                this.receivable_event_title = itemView.findViewById(R.id.receivable_event_title);
                this.balance_cnt = itemView.findViewById(R.id.balance_cnt);
                this.balance_amt = itemView.findViewById(R.id.balance_amt);
                receivable_event_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }

    protected void geReceivableFairList(boolean isInit) {
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }

        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%s&SYSTEM_ID=%s&FAIR_YEAR=%s", MyApplication.PAGE_VIEW_COUNT, ++page, userInfo.SYS_ID, "");
            //String body = String.format("SYSTEM_ID=%s&PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%s", userInfo.SYS_ID, MyApplication.PAGE_VIEW_COUNT, ++page);
            HashMap<String, String> body = new HashMap<>();
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_REC_DASH, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_REC_DASH, body);
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
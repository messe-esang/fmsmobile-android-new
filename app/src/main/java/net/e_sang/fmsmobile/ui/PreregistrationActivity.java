package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
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
import net.e_sang.fmsmobile.data.PreregistrationList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class PreregistrationActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<PreregistrationList> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RadioGroup preregistration_rdogroup = null;
    private String FAIR_TYPE = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_C9B5AF);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preregistration);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_C9B5AF));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_preregistration);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.preregistration_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        preregistration_rdogroup = findViewById(R.id.preregistration_rdogroup);

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
                    getPreregistrationList(false, FAIR_TYPE);
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PreregistrationList preregistrationList = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                //Toast.makeText(EventActivity.this, eventList.FAIR_ID, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PreregistrationDetailActivity.class);
                intent.putExtra("FAIR_ID", preregistrationList.FAIR_ID);
                intent.putExtra("FAIR_MASTER_ID", preregistrationList.FAIR_MASTER_ID);
                intent.putExtra("FAIR_NAME", preregistrationList.FAIR_NAME);
                intent.putExtra("FAIR_PLACE", preregistrationList.FAIR_PLACE);
                intent.putExtra("FAIR_DATE", preregistrationList.FAIR_STR_DATE.substring(0, 10) + " ~ " + preregistrationList.FAIR_END_DATE.substring(0, 10));
                intent.putExtra("FAIR_YEAR", preregistrationList.FAIR_STR_DATE.substring(0, 4));
                startActivity(intent);
            }
        });

        preregistration_rdogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.old_fair_btn) {
                    FAIR_TYPE = "0";
                } else if (checkedId == R.id.new_fair_btn) {
                    FAIR_TYPE = "1";
                } else {
                    FAIR_TYPE = "1";
                }
                getPreregistrationList(true, FAIR_TYPE);
            }
        });

        getPreregistrationList(true, FAIR_TYPE);
    }

    protected void getPreregistrationList(boolean isInit, String type) {
        Log.e(TAG, "getPreregistrationList type: " + type);
//        UserInfo userInfo = PrefKit.getUserInfo(this);
//        String body = String.format("SYSTEM_ID=%s", userInfo.SYS_ID);
//        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_PREREGISTRATION_FAIR_LIST, body);

        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }

        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%d&SYSTEM_ID=%s&TYPE=%s", MyApplication.PAGE_VIEW_COUNT, ++page, userInfo.SYS_ID, type);
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            body.put("TYPE", type);
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_PREREGISTRATION_FAIR_TYPE_LIST, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_PREREGISTRATION_FAIR_TYPE_LIST, body);
            }
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_PREREGISTRATION_FAIR_TYPE_LIST)) {
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
                                Log.e("PreregistrationActivity", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    PreregistrationList preregistrationList = new PreregistrationList();

                                    preregistrationList.FAIR_ID = json_list.optString("FAIR_ID");
                                    preregistrationList.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    preregistrationList.FAIR_STR_DATE = json_list.optString("FAIR_STR_DATE");
                                    preregistrationList.FAIR_END_DATE = json_list.optString("FAIR_END_DATE");
                                    preregistrationList.PRE_VISITOR_TOT_CNT = json_list.optString("PRE_VISITOR_TOT_CNT").replaceAll(" ", "");
                                    preregistrationList.NEW_PRE_VISITOR_TOT_CNT = json_list.optString("NEW_PRE_VISITOR_TOT_CNT");
                                    preregistrationList.FAIR_MASTER_ID = json_list.optString("FAIR_MASTER_ID");
                                    preregistrationList.FAIR_PLACE = json_list.optString("FAIR_PLACE");
                                    preregistrationList.FAIR_SEQ = json_list.optString("FAIR_SEQ");
                                    preregistrationList.FAIR_PLACE_NAME = json_list.optString("FAIR_PLACE_NAME");
                                    preregistrationList.NEW_RATE = Kit.getNewRate(json_list.optString("PRE_VISITOR_TOT_CNT").replace("null", "0"), json_list.optString("NEW_PRE_VISITOR_TOT_CNT").replace("null", "0"));

                                    mItems.add(preregistrationList);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(PreregistrationActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PreregistrationActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PreregistrationActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PreregistrationActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<PreregistrationList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<PreregistrationList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.preregistration_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            PreregistrationList preregistrationList = mItemList.get(position);

            Resources res = getResources();
            holder.preregistrationList_title.setText(preregistrationList.FAIR_NAME);
            holder.preregistrationList_count1.setText(String.format(res.getString(R.string.str_title_preregistration_count1), convertCurrencyStr(Double.parseDouble(preregistrationList.NEW_PRE_VISITOR_TOT_CNT.replace("null", "0")))));
            holder.preregistrationList_count2.setText(String.format(res.getString(R.string.str_title_preregistration_count2), convertCurrencyStr(Double.parseDouble(preregistrationList.PRE_VISITOR_TOT_CNT.replace("null", "0")))));
            holder.preregistration_rate.setText(String.format(res.getString(R.string.str_title_preregistration_rate), preregistrationList.NEW_RATE.replace("null", "0")));
            holder.preregistrationList_str_date.setText(preregistrationList.FAIR_STR_DATE.substring(0, 10));
            holder.preregistrationList_end_date.setText(preregistrationList.FAIR_END_DATE.substring(0, 10));
            if (mOnClickListener != null) {
                holder.preregistrationList_list_item_layout.setOnClickListener(mOnClickListener);
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
            public LinearLayout preregistrationList_list_item_layout;
            public TextView preregistrationList_title;
            public TextView preregistrationList_count1;
            public TextView preregistrationList_count2;
            public TextView preregistration_rate;
            public TextView preregistrationList_str_date;
            public TextView preregistrationList_end_date;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.preregistrationList_list_item_layout = itemView.findViewById(R.id.preregistration_list_item_layout);
                this.preregistrationList_title = itemView.findViewById(R.id.preregistration_title);
                this.preregistrationList_count1 = itemView.findViewById(R.id.preregistration_count1);
                this.preregistrationList_count2 = itemView.findViewById(R.id.preregistration_count2);
                this.preregistration_rate = itemView.findViewById(R.id.preregistration_rate);
                this.preregistrationList_str_date = itemView.findViewById(R.id.preregistration_str_date);
                this.preregistrationList_end_date = itemView.findViewById(R.id.preregistration_end_date);
                preregistrationList_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }

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

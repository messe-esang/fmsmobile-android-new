package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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

import com.weiwangcn.betterspinner.library.BetterSpinner;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AnnualVisitorStatisticsActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private BetterSpinner spinner_annual_visitor_statistics;
    private RecyclerView mRecyclerView = null;
    private ArrayList<FairTeam> mFairTeamItems = new ArrayList<>();
    private ArrayList<AnnualVisitorStatisticsList> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private String FAIR_MASTER_ID = "";
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout, annual_visitor_statistics_layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_FFF4D5);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_visitor_statistics);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_FFF4D5));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_annual_visitor_statistics);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        spinner_annual_visitor_statistics = findViewById(R.id.spinner_annual_visitor_statistics);

        mRecyclerView = findViewById(R.id.annual_visitor_statistics_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        annual_visitor_statistics_layout = findViewById(R.id.annual_visitor_statistics_layout);

        // 전시회 팀 가져오기
        getFairTeamList();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
//                    getAnnualVisitorStatisticsList(false);
//                    isLoading = true;
//                }
//            }
//        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AnnualVisitorStatisticsList annualvisitorstatisticslist = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                Intent intent = new Intent(getApplicationContext(), AnnualVisitorStatisticsDetailActivity.class);
                intent.putExtra("FAIR_NAME", annualvisitorstatisticslist.FAIR_NAME + " - " + annualvisitorstatisticslist.FAIR_PLACE);
                intent.putExtra("FAIR_MASTER_ID", annualvisitorstatisticslist.FAIR_MASTER_ID);
                intent.putExtra("FAIR_PLACE", annualvisitorstatisticslist.FAIR_PLACE);
                intent.putExtra("FAIR_PLACE_ID", annualvisitorstatisticslist.FAIR_PLACE_ID);
                startActivity(intent);
            }
        });


        spinner_annual_visitor_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFairTeamItems.size() != 0) {
                    spinner_annual_visitor_statistics.showDropDown();
                } else {
                    Toast.makeText(AnnualVisitorStatisticsActivity.this, "담당하는 행사가 없습니다.", Toast.LENGTH_SHORT).show();
                    annual_visitor_statistics_layout.setVisibility(View.GONE);
                }
            }
        });

        spinner_annual_visitor_statistics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FairTeam fairTeam = mFairTeamItems.get(position);
                FAIR_MASTER_ID = fairTeam.FAIR_MASTER_ID;
                getAnnualVisitorStatisticsList(true);
            }
        });

        //getAnnualVisitorStatisticsList(true);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (spinner_annual_visitor_statistics != null && spinner_annual_visitor_statistics.isPopupShowing()) {
                    spinner_annual_visitor_statistics.dismissDropDown();
                } else {
                    finish();
                }
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        if (spinner_annual_visitor_statistics != null && spinner_annual_visitor_statistics.isPopupShowing()) {
//            spinner_annual_visitor_statistics.dismissDropDown();
//        } else {
//            super.onBackPressed();
//        }
//    }

    protected void getFairTeamList() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("system_id=%s", userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this).request(TelKit.URL_API_GET_MASTER_CODE, body);
    }

    protected void getAnnualVisitorStatisticsList(boolean isInit) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("FAIR_MASTER_ID=%s&system_id=%s", FAIR_MASTER_ID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("FAIR_MASTER_ID", FAIR_MASTER_ID);
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_FAIR_LIST, body);
//        if (isInit) {
//            mCanLoadMore = true;
//            page = 0;
//            mItems.clear();
//            mRecyclerView.setAdapter(mAdapter);
//        }
//
//        if (mCanLoadMore) {
//            UserInfo userInfo = PrefKit.getUserInfo(this);
//            String body = String.format("FAIR_MASTER_ID=%s", FAIR_MASTER_ID);
//            if (page > 1) {
//                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_FAIR_LIST, body);
//            } else {
//                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_FAIR_LIST, body);
//            }
//        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_MASTER_CODE)) {
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
                                Log.e("AnnualVisitorStatistics", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    FairTeam fairteam = new FairTeam();
                                    fairteam.ROW_NO = json_list.optString("ROW_NO");
                                    fairteam.FAIR_MASTER_ID = json_list.optString("FAIR_MASTER_ID");
                                    fairteam.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    mFairTeamItems.add(fairteam);
                                }
                            }

                            String[] events = new String[mFairTeamItems.size()];
                            if (mFairTeamItems.size() > 0) {
                                Log.e("AnnualVisitorStatistics", "mFairTeamItems.size() : " + mFairTeamItems.size());
                                for (int i = 0; i < mFairTeamItems.size(); i++) {
                                    events[i] = mFairTeamItems.get(i).FAIR_NAME;
                                    spinner_annual_visitor_statistics.setText(mFairTeamItems.get(0).FAIR_NAME);
                                }

                                ArrayAdapter adapterManagers = new ArrayAdapter(this, R.layout.spinner_item, events);
                                spinner_annual_visitor_statistics.setAdapter(adapterManagers);
                                FAIR_MASTER_ID = mFairTeamItems.get(0).FAIR_MASTER_ID;
                                getAnnualVisitorStatisticsList(true);
                            }
                        } else {
                            Toast.makeText(this, "담당하는 행사가 없습니다.", Toast.LENGTH_SHORT).show();
                            annual_visitor_statistics_layout.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AnnualVisitorStatisticsActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_FAIR_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            mItems.clear();
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e("AnnualVisitorStatistics", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    AnnualVisitorStatisticsList annualvisitorstatisticslist = new AnnualVisitorStatisticsList();

                                    annualvisitorstatisticslist.FAIR_MASTER_ID = json_list.optString("FAIR_MASTER_ID");
                                    annualvisitorstatisticslist.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    annualvisitorstatisticslist.FAIR_PLACE_ID = json_list.optString("FAIR_PLACE_ID");
                                    annualvisitorstatisticslist.FAIR_PLACE = json_list.optString("FAIR_PLACE");
                                    mItems.add(annualvisitorstatisticslist);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(AnnualVisitorStatisticsActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 목록을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AnnualVisitorStatisticsActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AnnualVisitorStatisticsActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AnnualVisitorStatisticsActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<AnnualVisitorStatisticsList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<AnnualVisitorStatisticsList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.annual_visitor_statistics_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            AnnualVisitorStatisticsList annualvisitorstatisticslist = mItemList.get(position);

            Resources res = getResources();
            holder.annual_visitor_statistics_title.setText(annualvisitorstatisticslist.FAIR_NAME + " - " + annualvisitorstatisticslist.FAIR_PLACE);
            if (mOnClickListener != null) {
                holder.annual_visitor_statistics_list_item_layout.setOnClickListener(mOnClickListener);
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
            public LinearLayout annual_visitor_statistics_list_item_layout;
            public TextView annual_visitor_statistics_title;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.annual_visitor_statistics_list_item_layout = itemView.findViewById(R.id.annual_visitor_statistics_list_item_layout);
                this.annual_visitor_statistics_title = itemView.findViewById(R.id.annual_visitor_statistics_title);
                annual_visitor_statistics_list_item_layout.setOnClickListener(this);
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

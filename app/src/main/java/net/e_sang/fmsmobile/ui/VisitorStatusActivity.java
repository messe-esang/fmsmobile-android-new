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

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.VisitorStatusList;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VisitorStatusActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<VisitorStatusList> mItems = new ArrayList<>();
    private VisitorStatusActivity.RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_D9CCB6);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_status);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_D9CCB6));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_visitor_status);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.visitor_status_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new VisitorStatusActivity.RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VisitorStatusList visitorstatuslist = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                Intent intent = new Intent(getApplicationContext(), VisitorStatusTabDetailActivity.class);
                intent.putExtra("FAIR_ID", visitorstatuslist.FAIR_ID);
                intent.putExtra("FAIR_NAME", visitorstatuslist.FAIR_NAME);
                intent.putExtra("FAIR_DATE", visitorstatuslist.FAIR_STR_DATE.substring(0, 10) + " ~ " + visitorstatuslist.FAIR_END_DATE.substring(0, 10));
                startActivity(intent);
            }
        });

        getFairList(true);
    }

    protected void getFairList(boolean isInit) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("system_id=%s", userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_VISITOR_STATUS_FAIR_LIST, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_VISITOR_STATUS_FAIR_LIST)) {
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
                                Log.e("VisitorStatusActivity", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    VisitorStatusList visitorvtatuslist = new VisitorStatusList();
                                    visitorvtatuslist.FAIR_ID = json_list.optString("FAIR_ID");
                                    visitorvtatuslist.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    visitorvtatuslist.FAIR_STR_DATE = json_list.optString("FAIR_STR_DATE");
                                    visitorvtatuslist.FAIR_END_DATE = json_list.optString("FAIR_END_DATE");
                                    mItems.add(visitorvtatuslist);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(VisitorStatusActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "입장객현황을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(VisitorStatusActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(VisitorStatusActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(VisitorStatusActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<VisitorStatusActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<VisitorStatusList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<VisitorStatusList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public VisitorStatusActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.visitor_status_list_item, parent, false);
            return new VisitorStatusActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(VisitorStatusActivity.RecycleAdapter.ItemViewHolder holder, int position) {
            VisitorStatusList visitorstatuslist = mItemList.get(position);

            holder.visitor_status_title.setText(visitorstatuslist.FAIR_NAME);
            holder.visitor_status_str_date.setText(visitorstatuslist.FAIR_STR_DATE.substring(0, 10));
            holder.visitor_status_end_date.setText(visitorstatuslist.FAIR_END_DATE.substring(0, 10));
            if (mOnClickListener != null) {
                holder.visitor_status_list_item_layout.setOnClickListener(mOnClickListener);
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

        private void onItemHolderClick(VisitorStatusActivity.RecycleAdapter.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public VisitorStatusActivity.RecycleAdapter mAdapter;
            public LinearLayout visitor_status_list_item_layout;
            public TextView visitor_status_title;
            public TextView visitor_status_str_date;
            public TextView visitor_status_end_date;

            public ItemViewHolder(View itemView, VisitorStatusActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.visitor_status_list_item_layout = itemView.findViewById(R.id.visitor_status_list_item_layout);
                this.visitor_status_title = itemView.findViewById(R.id.visitor_status_title);
                this.visitor_status_str_date = itemView.findViewById(R.id.visitor_status_str_date);
                this.visitor_status_end_date = itemView.findViewById(R.id.visitor_status_end_date);
                visitor_status_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }
}

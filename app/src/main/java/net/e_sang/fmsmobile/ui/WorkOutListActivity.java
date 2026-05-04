package net.e_sang.fmsmobile.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.WorkOut;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkOutListActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private ArrayList<WorkOut> mItems = new ArrayList<>();
    private RecyclerView mRecyclerView = null;
    private WorkOutListActivity.RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private FloatingActionButton fab_main = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_out_list);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_company));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("외근등록내역");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.work_out_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        fab_main = findViewById(R.id.fab_main);
        fab_main.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new WorkOutListActivity.RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    getWorkOutList(false);
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WorkOut workout = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " WOID:" + workout.WOID);
                Intent intent = new Intent(getApplicationContext(), WorkOutActivity.class);
                intent.putExtra("WOID", workout.WOID);
                startActivity(intent);
            }
        });

        //getWorkOutList(true);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_main) {
            TedPermission.create()
                    .setDeniedMessage("설정에서 앱 권한을 모두 허용해 주세요.")
                    .setRationaleConfirmText("확인")
                    .setDeniedCloseButtonText("취소")
                    .setGotoSettingButtonText("설정")
                    .setPermissionListener(new PermissionListener() {

                        @Override
                        public void onPermissionGranted() {
                            Intent intent = new Intent(getApplicationContext(), WorkOutActivity.class);
                            intent.putExtra("WOID", "");
                            startActivity(intent);
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            Toast.makeText(WorkOutListActivity.this, "휴대폰 설정에서 앱 위치 권한 허용 체크 부탁 드립니다.", Toast.LENGTH_SHORT).show();
                        }
                    }).setPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                    .check();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mItems.clear();
            getWorkOutList(true);
        }
    }

    protected void getWorkOutList(boolean isInit) {
        Log.e(TAG, "getWorkOutList");
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }

        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("system_id=%s&page_view_count=%s&current_page_index=%s", userInfo.SYS_ID, MyApplication.PAGE_VIEW_COUNT, ++page);
            HashMap<String, String> body = new HashMap<>();
            body.put("system_id", userInfo.SYS_ID);
            body.put("page_view_count", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("current_page_index", String.valueOf(++page));
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_MY_WORK_OUT_LIST, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_MY_WORK_OUT_LIST, body);
            }
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_MY_WORK_OUT_LIST)) {
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
                                Log.e("WorkOutListActivity", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    WorkOut workout = new WorkOut();

                                    workout.TOT_CNT = json_list.optString("TOT_CNT");
                                    workout.ROW_NO = json_list.optString("ROW_NO");
                                    workout.FMS_USER_NAME = json_list.optString("FMS_USER_NAME");
                                    workout.WOID = json_list.optString("WOID");
                                    workout.WORK_CONTENT = json_list.optString("WORK_CONTENT");
                                    workout.CREATE_DATE = json_list.optString("CREATE_DATE");
                                    workout.IP_ADDRESS = json_list.optString("IP_ADDRESS");
                                    workout.LATITUDE = json_list.optString("LATITUDE");
                                    workout.LONGITUDE = json_list.optString("LONGITUDE");
                                    workout.START_WORK_DATETIME = json_list.optString("START_WORK_DATETIME").replace("null", "");
                                    workout.END_WORK_DATETIME = json_list.optString("END_WORK_DATETIME").replace("null", "");
                                    workout.UPDATE_DATETIME = json_list.optString("UPDATE_DATETIME").replace("null", "");
                                    mItems.add(workout);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(WorkOutListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WorkOutListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WorkOutListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<WorkOut> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<WorkOut> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.work_out_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            WorkOut workout = mItemList.get(position);

            holder.work_content.setText(Html.fromHtml(workout.WORK_CONTENT, Html.FROM_HTML_MODE_LEGACY));
            holder.start_date.setText(workout.START_WORK_DATETIME);
            holder.end_date.setText(workout.END_WORK_DATETIME);
            if (mOnClickListener != null) {
                holder.work_out_layoutItem.setOnClickListener(mOnClickListener);
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
            public LinearLayout work_out_layoutItem;
            public TextView work_content;
            public TextView start_date;
            public TextView end_date;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.work_out_layoutItem = itemView.findViewById(R.id.work_out_layoutItem);
                this.work_content = itemView.findViewById(R.id.work_content);
                this.start_date = itemView.findViewById(R.id.start_date);
                this.end_date = itemView.findViewById(R.id.end_date);
                work_out_layoutItem.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }
}
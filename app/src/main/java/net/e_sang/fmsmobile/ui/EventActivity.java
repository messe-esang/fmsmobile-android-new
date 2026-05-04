package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import androidx.activity.OnBackPressedCallback;
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

import com.weiwangcn.betterspinner.library.BetterSpinner;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.EventList;
import net.e_sang.fmsmobile.data.MasterCode;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class EventActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private ArrayList<MasterCode> mMasterCodeItems = new ArrayList<>();
    private BetterSpinner spinner_event, spinner_date;
    private RecyclerView mRecyclerView = null;
    private ArrayList<EventList> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private String FAIR_MASTER_ID = "";
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RadioGroup event_rdogroup = null;
    private String EVENT_TYPE = "0";
    private LinearLayout spinner_event_layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_event);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_event));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_event);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        spinner_event_layout = findViewById(R.id.spinner_event_layout);
        spinner_event = findViewById(R.id.spinner_event);
        spinner_date = findViewById(R.id.spinner_date);

        mRecyclerView = findViewById(R.id.event_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        event_rdogroup = findViewById(R.id.event_rdogroup);
        // 전시회 구분 가져오기
        getFairMasterList();

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
                    getFairList(false, EVENT_TYPE);
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventList eventList = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
                //Toast.makeText(EventActivity.this, eventList.FAIR_ID, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), EventDetailActivity.class);
                intent.putExtra("FAIR_ID", eventList.FAIR_ID);
                intent.putExtra("FAIR_MASTER_ID", eventList.FAIR_MASTER_ID);
                startActivity(intent);
            }
        });

        spinner_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMasterCodeItems.size() != 0) {
                    spinner_event.showDropDown();
                } else {
                    Toast.makeText(EventActivity.this, "전시회가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinner_event.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterCode mastercode = mMasterCodeItems.get(position);
                FAIR_MASTER_ID = mastercode.VALUE;
                getFairList(true, EVENT_TYPE);
                //Toast.makeText(EventActivity.this, mastercode.DESC + " / " + mastercode.VALUE, Toast.LENGTH_SHORT).show();
            }
        });

        spinner_date.setOnClickListener(new View.OnClickListener() {
            Calendar calendar = new GregorianCalendar(Locale.KOREA);
            int choosenYear = calendar.get(Calendar.YEAR);

            @Override
            public void onClick(View v) {
                MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(EventActivity.this, new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        spinner_date.setText(Integer.toString(selectedYear));
                        getFairList(true, EVENT_TYPE);
                    }
                }, choosenYear, 0);

                builder.showYearOnly()
                        .setYearRange(2002, choosenYear)
                        .build()
                        .show();
            }
        });

        event_rdogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.old_event_btn) {
                    EVENT_TYPE = "0";
                } else if (checkedId == R.id.new_event_btn) {
                    EVENT_TYPE = "1";
                } else {
                    EVENT_TYPE = "0";
                }
                getFairList(true, EVENT_TYPE);
            }
        });

        UserInfo userInfo = PrefKit.getUserInfo(this);
        if ("3".equals(userInfo.POSITION_ID)) {  //팀장
            spinner_event_layout.setVisibility(View.GONE);
        }

        getFairList(true, EVENT_TYPE);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (spinner_event != null && spinner_event.isPopupShowing()) {
                    spinner_event.dismissDropDown();
                } else {
                    finish();
                }
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        if (spinner_event != null && spinner_event.isPopupShowing()) {
//            spinner_event.dismissDropDown();
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_MASTER_CODE)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String resultStr = json.optString("result");
                    String resultList = json.optString("list");

                    JSONObject result_obj = new JSONObject(resultStr);
                    String code = result_obj.optString("code");
                    String msg = result_obj.optString("msg");

                    if ("ok".equals(code)) {
                        JSONArray jsonArray = new JSONArray(resultList);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json_list = jsonArray.getJSONObject(i);
                            Log.e("EventActivity", "JSONObject json_list: " + json_list);
                            if (json_list != null) {
                                MasterCode mastercode = new MasterCode();
                                mastercode.VALUE = json_list.optString("VALUE");
                                mastercode.DESC = json_list.optString("DESC");
                                mMasterCodeItems.add(mastercode);
                            }
                        }

                        String[] events = new String[mMasterCodeItems.size()];
                        if (mMasterCodeItems.size() > 0) {
                            Log.e("EventActivity", "mMasterCodeItems.size() : " + mMasterCodeItems.size());
                            for (int i = 0; i < mMasterCodeItems.size(); i++) {
                                events[i] = mMasterCodeItems.get(i).DESC;
                            }
                        }
                        ArrayAdapter adapterManagers = new ArrayAdapter(this, R.layout.spinner_item, events);
                        spinner_event.setAdapter(adapterManagers);
                    } else {
                        //Toast.makeText(EventActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EventActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_EVENT_FAIR_LIST)) {
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
                                Log.e("EventActivity", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    EventList eventList = new EventList();

                                    eventList.TOT_CNT = json_list.optString("TOT_CNT");
                                    eventList.ROW_NO = json_list.optString("ROW_NO");
                                    eventList.FAIR_ID = json_list.optString("FAIR_ID");
                                    eventList.FAIR_MASTER_ID = json_list.optString("FAIR_MASTER_ID");
                                    //eventList.FAIR_YEAR = json_list.optString("FAIR_YEAR");
                                    eventList.FAIR_DESC = json_list.optString("FAIR_DESC");
                                    eventList.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    //eventList.TEAM = json_list.optString("TEAM");
                                    eventList.FAIR_STR_DATE = json_list.optString("FAIR_STR_DATE").replace("null", "0");
                                    eventList.FAIR_END_DATE = json_list.optString("FAIR_END_DATE").replace("null", "0");
                                    //eventList.FAIR_STATUS_DESC = json_list.optString("FAIR_STATUS_DESC");
                                    //eventList.CompanyCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("CompanyCnt").replace("null", "0")));
                                    //eventList.BoothCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("BoothCnt").replace("null", "0")));
                                    //eventList.TotalFairAmt = convertCurrencyStr(Double.parseDouble(json_list.optString("TotalFairAmt").replace("null", "0")));
                                    //eventList.BalanceCnt = convertCurrencyStr(Double.parseDouble(json_list.optString("BalanceCnt").replace("null", "0")));
                                    //eventList.BalanceAmt = convertCurrencyStr(Double.parseDouble(json_list.optString("BalanceAmt").replace("null", "0")));
                                    mItems.add(eventList);
                                }
                            }
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                            //Toast.makeText(EventActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EventActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(EventActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<EventList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<EventList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.event_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            EventList eventlist = mItemList.get(position);

            Resources res = getResources();
            holder.event_title.setText(eventlist.FAIR_NAME);
            //holder.event_count1.setText(String.format(res.getString(R.string.str_title_event_count1), eventlist.CompanyCnt.replace("null", "0")));
            //holder.event_count2.setText(String.format(res.getString(R.string.str_title_event_count2), eventlist.BoothCnt.replace("null", "0")));
            //holder.event_money.setText(String.format(res.getString(R.string.str_title_event_money), eventlist.TotalFairAmt.replace("null", "0")));
            holder.event_str_date.setText(eventlist.FAIR_STR_DATE.substring(0, 10));
            holder.event_end_date.setText(eventlist.FAIR_END_DATE.substring(0, 10));
            if (mOnClickListener != null) {
                holder.event_list_item_layout.setOnClickListener(mOnClickListener);
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
            public LinearLayout event_list_item_layout;
            public TextView event_title;
            public TextView event_count1;
            public TextView event_count2;
            public TextView event_money;
            public TextView event_str_date;
            public TextView event_end_date;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.event_list_item_layout = itemView.findViewById(R.id.event_list_item_layout);
                this.event_title = itemView.findViewById(R.id.event_title);
                this.event_count1 = itemView.findViewById(R.id.event_count1);
                this.event_count2 = itemView.findViewById(R.id.event_count2);
                this.event_money = itemView.findViewById(R.id.event_money);
                this.event_str_date = itemView.findViewById(R.id.event_str_date);
                this.event_end_date = itemView.findViewById(R.id.event_end_date);
                event_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }

    protected void getFairMasterList() {
        //String body = String.format("master_grp_type=%s&master_code_group_id=%s", "FAIR", "");
        HashMap<String, String> body = new HashMap<>();
        body.put("master_grp_type", "FAIR");
        body.put("master_code_group_id", "");
        new TelKit(this, this).request(TelKit.URL_API_MASTER_CODE, body);
    }

    protected void getFairList(boolean isInit, String type) {
        Log.e("EventActivity", "getFairList type: " + type);
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }

        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%s&SYSTEM_ID=%s&FAIR_MASTER_ID=%s&TYPE=%s", MyApplication.PAGE_VIEW_COUNT, ++page, userInfo.SYS_ID, FAIR_MASTER_ID, type);
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            body.put("FAIR_MASTER_ID", FAIR_MASTER_ID);
            body.put("TYPE", type);
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_EVENT_FAIR_LIST, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_EVENT_FAIR_LIST, body);
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

package net.e_sang.fmsmobile.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.ActionPlanCommentList;
import net.e_sang.fmsmobile.data.ActionPlanTeam;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ActionPlanListActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private SectionedRecyclerViewAdapter sectionAdapter = null;
    private TextView mTxtEmpty = null;
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private FloatingActionButton fab_main = null;

    private HorizontalCalendar horizontalCalendar;
    private String mDate = "";
    private UserInfo userInfo = null;
    private TextView date_view, date_week_view = null;

    private Spinner spinner_team = null;
    private ArrayList<ActionPlanTeam> mActionPlanTeams = new ArrayList<>();
    private static HashMap<String, String> map = new HashMap<>();
    private String mDEPT = "";
    private int Count = 0;

    private int spinnerPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_35A4F3);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_plan_list);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_35A4F3));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("일정등록내역");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        sectionAdapter = new SectionedRecyclerViewAdapter();

        mRecyclerView = findViewById(R.id.work_out_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        fab_main = findViewById(R.id.fab_main);
        fab_main.setOnClickListener(this);
        date_view = findViewById(R.id.date_view);
        date_week_view = findViewById(R.id.date_week_view);
        spinner_team = findViewById(R.id.spinner_team);

        userInfo = PrefKit.getUserInfo(this);

        spinner_team.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "position: " + position);
                ActionPlanTeam actionPlanTeam = mActionPlanTeams.get(position);
                mDEPT = actionPlanTeam.DEPT;
                getActionPlanList(mDate, mDEPT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Date currentTime = Calendar.getInstance().getTime();
        mDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        date_view.setText(new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(currentTime));
        date_week_view.setText(new SimpleDateFormat("EE요일", Locale.getDefault()).format(currentTime));
        if (date_week_view.getText().equals("토요일")) {
            date_week_view.setTextColor(getResources().getColor(R.color.color_blue));
        } else if (date_week_view.getText().equals("일요일")) {
            date_week_view.setTextColor(getResources().getColor(R.color.color_text_error));
        } else {
            date_week_view.setTextColor(getResources().getColor(R.color.black));
        }

        /* start 2 months ago from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -5);

        /* end after 2 months from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 5);
        // Default Date set to Today.
        final Calendar defaultSelectedDate = Calendar.getInstance();


        horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .configure()
                .formatTopText("MMM")
                .formatMiddleText("dd")
                .formatBottomText("EEE")
                .showTopText(true)
                .showBottomText(true)
                .textColor(getResources().getColor(R.color.color_gray), Color.WHITE)
                .colorTextTop(Color.WHITE, Color.parseColor("#FFD400"))
                .colorTextMiddle(Color.WHITE, Color.parseColor("#FFD400"))
                .colorTextBottom(Color.WHITE, Color.parseColor("#FFD400"))
                .end()
                .defaultSelectedDate(defaultSelectedDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                date_view.setText(DateFormat.format("yyyy년 MM월 dd일", date).toString());
                date_week_view.setText(DateFormat.format("EE요일", date).toString());
                if (date_week_view.getText().equals("토요일")) {
                    date_week_view.setTextColor(getResources().getColor(R.color.color_blue));
                } else if (date_week_view.getText().equals("일요일")) {
                    date_week_view.setTextColor(getResources().getColor(R.color.color_text_error));
                } else {
                    date_week_view.setTextColor(getResources().getColor(R.color.black));
                }
                mDate = DateFormat.format("yyyyMMdd", date).toString();
                getActionPlanList(mDate, mDEPT);
            }

        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(sectionAdapter);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_main) {
            Intent intent = new Intent(getApplicationContext(), ActionPlanActivity.class);
            intent.putExtra("IDX", "");
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sectionAdapter != null) {
            getActionPlanList(mDate, mDEPT);
        }
    }

    protected void getActionPlanList(String date, String dept) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        body.put("FROM_DT", date);
        body.put("USER_ID", userInfo.LOGIN_ID);
        body.put("DEPT", dept);
        new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_ACTION_PLAN_LIST, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_ACTION_PLAN_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            sectionAdapter.removeAllSections();
                            JSONArray jsonArray = new JSONArray(resultList);
                            if (Count == 0) {
                                map.put("", "전체");
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                ArrayList<ActionPlanCommentList> mActionPlanCommentListItems = new ArrayList<>();
                                if (json_list != null) {
                                    if (Count == 0) {
                                        map.put(json_list.optString("DEPT"), json_list.optString("DEPT_DESC"));
                                    }
                                    String ComentList = json_list.optString("PART_1").replace("null", "");
                                    if (Kit.isNotNullNotEmpty(ComentList)) {
                                        try {
                                            String[] comment_all = ComentList.split("\\$\\$\\$");
                                            for (int c = 0; c < comment_all.length; c++) {
                                                String comment = comment_all[c];
                                                String[] comment_item = comment.split("\\|\\|");
                                                ActionPlanCommentList actionPlanCommentList = new ActionPlanCommentList();
                                                for (int j = 0; j < comment_item.length; j++) {
                                                    actionPlanCommentList.IDX = comment_item[0];
                                                    actionPlanCommentList.COMMENT = comment_item[1];
                                                    actionPlanCommentList.DATE = comment_item[2];
                                                    actionPlanCommentList.SYSTEM_ID = comment_item[3];
                                                }
                                                mActionPlanCommentListItems.add(actionPlanCommentList);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    sectionAdapter.addSection(new ActionPlanListActivity.ContactsSection(json_list.optString("DEPT_DESC").replace("null", ""), json_list.optString("DEPT").replace("null", ""), json_list.optString("NAME").replace("null", ""), json_list.optString("SYSTEM_ID").replace("null", ""), mActionPlanCommentListItems));
                                    sectionAdapter.notifyDataSetChanged();
                                }
                            }
                            if (Count == 0) {
                                Iterator<String> iter = map.keySet().iterator();
                                while (iter.hasNext()) {
                                    String key = iter.next();
                                    String value = map.get(key);

                                    ActionPlanTeam actionPlanTeam = new ActionPlanTeam();
                                    actionPlanTeam.DEPT = key;
                                    actionPlanTeam.DEPT_DESC = value;
                                    mActionPlanTeams.add(actionPlanTeam);
                                    Log.e(TAG, "key : " + key + ", value : " + value);
                                }


                                String[] events = new String[mActionPlanTeams.size()];
                                if (mActionPlanTeams.size() > 0) {
                                    for (int j = 0; j < mActionPlanTeams.size(); j++) {
                                        events[j] = mActionPlanTeams.get(j).DEPT_DESC;
                                        map.put(mActionPlanTeams.get(j).DEPT, mActionPlanTeams.get(j).DEPT_DESC);
                                    }
                                }

                                ArrayAdapter adapterManagers = new ArrayAdapter(this, R.layout.spinner_item, events);
                                spinner_team.setAdapter(adapterManagers);

                                spinnerPosition = adapterManagers.getPosition(userInfo.DEPT_NAME);
                                spinner_team.setSelection(spinnerPosition);
                            }
                            Count++;
                        } else {
                            Toast.makeText(this, "일정을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "일정을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ActionPlanListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ActionPlanListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ActionPlanListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private class ContactsSection extends Section {

        private List<ActionPlanCommentList> mItemList;
        String DEPT_DESC;
        String DEPT;
        String NAME;
        String SYSTEM_ID;

        ContactsSection(String dept_desc, String dept, String name, String system_id, List<ActionPlanCommentList> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.layout_item_action_plan_comment)
                    .headerResourceId(R.layout.action_plan_list_item)
                    .build());
            this.DEPT_DESC = dept_desc;
            this.DEPT = dept;
            this.NAME = name;
            this.SYSTEM_ID = system_id;
            this.mItemList = list;
        }

        @Override
        public int getContentItemsTotal() {
            return mItemList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ActionPlanListActivity.ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ActionPlanListActivity.ItemViewHolder itemHolder = (ActionPlanListActivity.ItemViewHolder) holder;

            final ActionPlanCommentList actionPlanCommentList = mItemList.get(position);

            itemHolder.action_plan_idx.setText(actionPlanCommentList.IDX);
            itemHolder.action_plan_comment.setText(actionPlanCommentList.COMMENT);
            itemHolder.action_plan_date.setText(actionPlanCommentList.DATE);
            itemHolder.action_plan_system_id.setText(actionPlanCommentList.SYSTEM_ID);

            if (position % 2 == 0) {
                itemHolder.img_view.setBackgroundColor(getResources().getColor(R.color.color_BDCF68));
                itemHolder.comment_layout.setBackgroundResource(R.drawable.gray_rounded_comment_bg1);
            } else {
                itemHolder.img_view.setBackgroundColor(getResources().getColor(R.color.color_35A4F3));
                itemHolder.comment_layout.setBackgroundResource(R.drawable.gray_rounded_comment_bg2);
            }

            if (position == 0) {
                itemHolder.img_subdirectory.setVisibility(View.VISIBLE);
            } else {
                itemHolder.img_subdirectory.setVisibility(View.INVISIBLE);
            }
            if (userInfo.SYS_ID.equals(actionPlanCommentList.SYSTEM_ID)) {
                if (dateCompare(actionPlanCommentList.DATE) == true) {
                    itemHolder.search_icon.setVisibility(View.VISIBLE);
                    itemHolder.layoutCommentItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(TAG, "actionPlanCommentList.IDX : " + actionPlanCommentList.IDX);
                            Intent intent = new Intent(getApplicationContext(), ActionPlanActivity.class);
                            intent.putExtra("IDX", actionPlanCommentList.IDX);
                            intent.putExtra("DATE", actionPlanCommentList.DATE);
                            startActivity(intent);
                        }
                    });
                } else {
                    itemHolder.search_icon.setVisibility(View.GONE);
                }
            } else {
                itemHolder.search_icon.setVisibility(View.GONE);
            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new ActionPlanListActivity.HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            ActionPlanListActivity.HeaderViewHolder headerHolder = (ActionPlanListActivity.HeaderViewHolder) holder;
            headerHolder.dept_desc.setText(DEPT_DESC);
            headerHolder.name.setText(NAME);
            UserInfo userInfo = PrefKit.getUserInfo(ActionPlanListActivity.this);

            if (userInfo.SYS_ID.equals(SYSTEM_ID)) {
                headerHolder.action_plan_layoutItem.setBackgroundColor(getResources().getColor(R.color.color_FFD400));
                headerHolder.dept_desc.setTextColor(Color.BLACK);
                headerHolder.name.setTextColor(Color.BLACK);
            } else {
                headerHolder.action_plan_layoutItem.setBackgroundColor(Color.WHITE);
                headerHolder.dept_desc.setTextColor(Color.BLACK);
                headerHolder.name.setTextColor(Color.BLACK);
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout action_plan_layoutItem;
        public TextView dept_desc;
        public TextView name;

        HeaderViewHolder(View view) {
            super(view);
            action_plan_layoutItem = view.findViewById(R.id.action_plan_layoutItem);
            dept_desc = view.findViewById(R.id.dept_desc);
            name = view.findViewById(R.id.name);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layoutCommentItem;
        public TextView action_plan_idx;
        public TextView action_plan_comment;
        public TextView action_plan_date;
        public TextView action_plan_system_id;
        public ImageView search_icon;
        public LinearLayout comment_layout;
        public ImageView img_view;
        public ImageView img_subdirectory;

        ItemViewHolder(View view) {
            super(view);
            this.layoutCommentItem = itemView.findViewById(R.id.layoutCommentItem);
            this.action_plan_idx = itemView.findViewById(R.id.action_plan_idx);
            this.action_plan_comment = itemView.findViewById(R.id.action_plan_comment);
            this.action_plan_date = itemView.findViewById(R.id.action_plan_date);
            this.action_plan_system_id = itemView.findViewById(R.id.action_plan_system_id);
            this.search_icon = itemView.findViewById(R.id.search_icon);
            this.comment_layout = itemView.findViewById(R.id.comment_layout);
            this.img_view = itemView.findViewById(R.id.img_view);
            this.img_subdirectory = itemView.findViewById(R.id.img_subdirectory);

        }
    }

    private boolean dateCompare(String getDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        Date currentTime = Calendar.getInstance().getTime();
        String st_today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        Date toDay = null;
        Date getDay = null;

        try {
            toDay = format.parse(st_today);
            getDay = format.parse(getDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int compare = toDay.compareTo(getDay);

        if (compare > 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Today").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ("Today".equals(item.getTitle())) {
            try {
                horizontalCalendar.goToday(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }
}
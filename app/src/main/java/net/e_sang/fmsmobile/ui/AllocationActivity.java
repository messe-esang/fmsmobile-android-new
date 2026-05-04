package net.e_sang.fmsmobile.ui;

import android.content.Intent;
import android.content.res.Resources;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.*;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.AssignList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

import org.json.JSONArray;
import org.json.JSONObject;


public class AllocationActivity extends BaseActivity implements TelKit.OnResultListener {

    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    final private int pageCnt = 10;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private SectionedRecyclerViewAdapter sectionAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_allocation);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocation);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_allocation));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_allocation);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.allocation_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        sectionAdapter = new SectionedRecyclerViewAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(sectionAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    //load(false);
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_ASSIGN_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String resultStr = json.optString("result");
                    String resultList = json.optString("list");

                    JSONObject result_obj = new JSONObject(resultStr);
                    String code = result_obj.optString("code");
                    String msg = result_obj.optString("msg");
                    Log.e(TAG, "resultList : " + resultList);
                    if ("ok".equals(code)) {
                        JSONArray jsonArray = new JSONArray(resultList);
                        Kit.log(Kit.LogType.TEST, "list.length() = " + jsonArray.length());
                        Log.e(TAG, "jsonArray : " + jsonArray);
                        sectionAdapter.removeAllSections();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json_list = jsonArray.getJSONObject(i);
                            String assignList = json_list.optString("assignList");

                            ArrayList<AssignList> mAssignListItems = new ArrayList<>();
                            if (assignList != null) {
                                JSONArray assignListArray = new JSONArray(assignList);

                                for (int j = 0; j < assignListArray.length(); j++) {
                                    JSONObject assign_list = assignListArray.getJSONObject(j);

                                    if (assign_list != null) {
                                        AssignList assignlist = new AssignList();
                                        assignlist.FAIR_ID = assign_list.optString("FAIR_ID");
                                        assignlist.SYSTEM_ID = assign_list.optString("SYSTEM_ID");
                                        assignlist.FAIR_DESC = assign_list.optString("FAIR_DESC");
                                        assignlist.FAIR_NAME = assign_list.optString("FAIR_NAME");
                                        assignlist.USER_ID = assign_list.optString("USER_ID");
                                        assignlist.NAME = assign_list.optString("NAME");
                                        assignlist.Complete = assign_list.optString("Complete");
                                        assignlist.AssignCnt = assign_list.optString("AssignCnt");
                                        assignlist.AssignRate = assign_list.optString("AssignRate");
                                        mAssignListItems.add(assignlist);
                                    }
                                }
                            }
                            sectionAdapter.addSection(new ContactsSection(json_list.optString("fair_id"), json_list.optString("fair_name"), json_list.optString("chief_yn"), mAssignListItems));
                            sectionAdapter.notifyDataSetChanged();
                        }
                        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        mTxtEmpty.setVisibility(resultList.equals("null") ? View.VISIBLE : View.GONE);
                        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private class ContactsSection extends Section {

        private List<AssignList> mItemList;
        String title;
        String fair_id;
        String chief_yn;

        ContactsSection(String fair_id, String title, String chief_yn, List<AssignList> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.member_item)
                    .headerResourceId(R.layout.member_list_item)
                    .build());
            this.title = title;
            this.fair_id = fair_id;
            this.chief_yn = chief_yn;
            this.mItemList = list;
        }

        @Override
        public int getContentItemsTotal() {
            return mItemList.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            Resources res = getResources();
            AssignList allocationdetail = mItemList.get(position);
            itemHolder.member_name.setText(allocationdetail.NAME);
            itemHolder.member_count1.setText(allocationdetail.Complete);
            itemHolder.member_count2.setText(allocationdetail.AssignCnt);
            itemHolder.member_count3.setText(String.format(res.getString(R.string.str_assign_tot_cnt), allocationdetail.AssignRate.replace("null", "0")));

            UserInfo userInfo = PrefKit.getUserInfo(AllocationActivity.this);
            if (userInfo != null && allocationdetail.SYSTEM_ID.equals(userInfo.SYS_ID)) {
                itemHolder.member_name.setTextColor(getResources().getColor(R.color.color_class_allocation));
                itemHolder.member_count1.setTextColor(getResources().getColor(R.color.color_class_allocation));
                itemHolder.member_count2.setTextColor(getResources().getColor(R.color.color_class_allocation));
            } else {
                itemHolder.member_name.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                itemHolder.member_count1.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                itemHolder.member_count2.setTextColor(getResources().getColor(R.color.color_9A9A9A));
            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.member_title.setText(title);
            UserInfo userInfo = PrefKit.getUserInfo(AllocationActivity.this);
            //if(Integer.parseInt(userInfo.POSITION_ID) == 3 && "팀장".equals(userInfo.POSITION_NAME)) {
            //if (Integer.parseInt(userInfo.POSITION_ID) <= 3) {
            if (chief_yn.equals("Y")) {
                headerHolder.assign_img_btn.setVisibility(View.VISIBLE);
            } else {
                headerHolder.assign_img_btn.setVisibility(View.GONE);
            }

            headerHolder.assign_img_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "HeaderViewHolder onClick : " + fair_id);
                    Intent intent = new Intent(getApplicationContext(), AssignListActivity.class);
                    intent.putExtra("FAIR_ID", fair_id);
                    intent.putExtra("FAIR_NAME", title);
                    startActivity(intent);
                }
            });
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public TextView member_title;
        public ImageButton assign_img_btn;

        HeaderViewHolder(View view) {
            super(view);
            member_title = view.findViewById(R.id.member_title);
            assign_img_btn = view.findViewById(R.id.assign_img_btn);
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView member_name;
        public TextView member_count1;
        public TextView member_count2;
        public TextView member_count3;

        ItemViewHolder(View view) {
            super(view);

            this.member_name = itemView.findViewById(R.id.member_name);
            this.member_count1 = itemView.findViewById(R.id.member_count1);
            this.member_count2 = itemView.findViewById(R.id.member_count2);
            this.member_count3 = itemView.findViewById(R.id.member_count3);
        }
    }

    protected void getAssignList() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            //String body = String.format("system_id=%s", userInfo.SYS_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("system_id", userInfo.SYS_ID);
            new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_ASSIGN_LIST, body);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAssignList();
    }
}
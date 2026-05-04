package net.e_sang.fmsmobile.ui;

import static android.view.View.INVISIBLE;

import android.content.res.Resources;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.AssignList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SalesActivity extends BaseActivity implements TelKit.OnResultListener {
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
        int themeColor = ContextCompat.getColor(this, R.color.color_class_sales);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_sales));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_sales);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.sales_recycler);
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

        getSalesList();
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_SALES_REPORT_LIST)) {
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
                        Log.e(TAG, "list.length() = " + jsonArray.length());
                        Log.e(TAG, "jsonArray : " + jsonArray);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject json_list = jsonArray.getJSONObject(i);
                            String assignList = json_list.optString("salesList");

                            ArrayList<AssignList> mAssignListItems = new ArrayList<>();
                            if (assignList != null) {
                                JSONArray assignListArray = new JSONArray(assignList);
                                Log.e(TAG, "assignListArray.length() = " + assignListArray.length());

                                int totalCompanyCnt = 0;
                                int totalBoothCnt = 0;

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
                                        assignlist.Complete = assign_list.optString("CompanyCnt");
                                        assignlist.AssignCnt = assign_list.optString("BoothCnt");
                                        assignlist.AssignRate = assign_list.optString("SalesRate");
                                        // 숫자 합계 구하기
                                        totalCompanyCnt += assign_list.optInt("CompanyCnt", 0);
                                        totalBoothCnt += assign_list.optInt("BoothCnt", 0);

                                        mAssignListItems.add(assignlist);
                                    }
                                }
// 합계 아이템 추가
                                AssignList totalItem = new AssignList();
                                totalItem.NAME = "합계";  // 구분 표시용
                                totalItem.Complete = String.valueOf(totalCompanyCnt);
                                totalItem.AssignCnt = String.valueOf(totalBoothCnt);
                                mAssignListItems.add(totalItem);

                            }
                            sectionAdapter.addSection(new SalesActivity.ContactsSection(json_list.optString("fair_name"), mAssignListItems));
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
        String titles;

        ContactsSection(String title, List<AssignList> list) {
            super(SectionParameters.builder()
                    .itemResourceId(R.layout.member_item)
                    .headerResourceId(R.layout.sales_list_item)
                    .build());

            this.titles = title;
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
            AssignList salesdetail = mItemList.get(position);
            itemHolder.member_name.setText(salesdetail.NAME);
            itemHolder.member_count1.setText(salesdetail.Complete);
            itemHolder.member_count2.setText(salesdetail.AssignCnt);
            itemHolder.member_count3.setText(String.format(res.getString(R.string.str_assign_tot_cnt), salesdetail.AssignRate.replace("null", "0")));

            UserInfo userInfo = PrefKit.getUserInfo(SalesActivity.this);
            if (userInfo != null && salesdetail.SYSTEM_ID.equals(userInfo.SYS_ID)) {
                itemHolder.member_name.setTextColor(getResources().getColor(R.color.color_class_allocation));
                itemHolder.member_count1.setTextColor(getResources().getColor(R.color.color_class_allocation));
                itemHolder.member_count2.setTextColor(getResources().getColor(R.color.color_class_allocation));
            } else {
                itemHolder.member_name.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                itemHolder.member_count1.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                itemHolder.member_count2.setTextColor(getResources().getColor(R.color.color_9A9A9A));
            }

            if (salesdetail.NAME.equals("합계")) {
                itemHolder.member_count3.setVisibility(INVISIBLE);
                itemHolder.member_name.setTextColor(getResources().getColor(R.color.black));
                itemHolder.member_count1.setTextColor(getResources().getColor(R.color.black));
                itemHolder.member_count2.setTextColor(getResources().getColor(R.color.black));
                itemHolder.sales_item_layout.setBackgroundColor(getResources().getColor(R.color.color_E0E0E3));
            }
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.member_title.setText(titles);
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        public TextView member_title;

        HeaderViewHolder(View view) {
            super(view);
            member_title = (TextView) view.findViewById(R.id.member_title);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout sales_item_layout;
        public TextView member_name;
        public TextView member_count1;
        public TextView member_count2;
        public TextView member_count3;

        ItemViewHolder(View view) {
            super(view);

            this.sales_item_layout = itemView.findViewById(R.id.sales_item_layout);
            this.member_name = itemView.findViewById(R.id.member_name);
            this.member_count1 = itemView.findViewById(R.id.member_count1);
            this.member_count2 = itemView.findViewById(R.id.member_count2);
            this.member_count3 = itemView.findViewById(R.id.member_count3);
        }
    }

    protected void getSalesList() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            //String body = String.format("system_id=%s", userInfo.SYS_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("system_id", userInfo.SYS_ID);
            new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_SALES_REPORT_LIST, body);
        }
    }
}

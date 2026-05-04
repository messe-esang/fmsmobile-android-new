package net.e_sang.fmsmobile.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.android.material.tabs.TabLayout;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoticeActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<Notice> mItems = new ArrayList<>();
    private ArrayList<CompanyInfo> info_mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private String NOTI_TYPE = "";
    private TabLayout tabLayout = null;
    private UserInfo userInfo = null;
    private String[] mTitles = {
            "전체",
            "재통화예정",
            "입금예정",
            "참가신청",
            "신청내용변경",
            "배정내용추가",
            "배정내용변경",
            "영업활동공유",
            "댓글등록",
            "공지사항",
            "세금계산서",
            "동향보고",
            "확인된알림",
    };

    private String[] mAdminTitles = {
            "전체",
            "영업활동공유",
            "댓글등록",
            "공지사항",
            "동향보고",
            "확인된알림",
    };

    public static Notice notice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_notification);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_notification));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_notification);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        tabLayout = findViewById(R.id.tab_layout);

        userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
            for (int i = 0; i < mAdminTitles.length; i++) {
                View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.noti_tab_layout, null, false);
                ImageView tab_image = headerView.findViewById(R.id.tab_image);
                TextView tab_text = headerView.findViewById(R.id.tab_text);
                tab_text.setText(mAdminTitles[i]);
                if (i == 0) {
                    tab_image.setVisibility(View.GONE);
                } else if (i == 1) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_6));
                } else if (i == 2) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_7));
                } else if (i == 3) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_8));
                } else if (i == 4) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_10));
                } else {
                    tab_image.setVisibility(View.GONE);
                }
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setCustomView(headerView);
                tabLayout.addTab(tab);
            }
        } else {
            for (int i = 0; i < mTitles.length; i++) {
                View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.noti_tab_layout, null, false);
                ImageView tab_image = headerView.findViewById(R.id.tab_image);
                TextView tab_text = headerView.findViewById(R.id.tab_text);
                tab_text.setText(mTitles[i]);
                if (i == 0) {
                    tab_image.setVisibility(View.GONE);
                } else if (i == 1) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon));
                } else if (i == 2) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_1));
                } else if (i == 3) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_2));
                } else if (i == 4) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_3));
                } else if (i == 5) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_4));
                } else if (i == 6) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_5));
                } else if (i == 7) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_6));
                } else if (i == 8) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_7));
                } else if (i == 9) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_8));
                } else if (i == 10) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_9));
                } else if (i == 11) {
                    tab_image.setImageDrawable(getDrawable(R.drawable.notice_rdo_icon_10));
                } else {
                    tab_image.setVisibility(View.GONE);
                }
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setCustomView(headerView);
                tabLayout.addTab(tab);
            }
        }

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(20, 0, 20, 0);
            tab.requestLayout();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
                    switch (tab.getPosition()) {
                        case 0:
                            NOTI_TYPE = "";
                            break;
                        case 1:
                            NOTI_TYPE = "7";
                            break;
                        case 2:
                            NOTI_TYPE = "8";
                            break;
                        case 3:
                            NOTI_TYPE = "9";
                            break;
                        case 4:
                            NOTI_TYPE = "11";
                            break;
                        case 5:
                            NOTI_TYPE = "0";
                            break;
                        default:
                            NOTI_TYPE = "";
                            break;
                    }
                } else {
                    switch (tab.getPosition()) {
                        case 0:
                            NOTI_TYPE = "";
                            break;
                        case 1:
                            NOTI_TYPE = "1";
                            break;
                        case 2:
                            NOTI_TYPE = "2";
                            break;
                        case 3:
                            NOTI_TYPE = "3";
                            break;
                        case 4:
                            NOTI_TYPE = "4";
                            break;
                        case 5:
                            NOTI_TYPE = "5";
                            break;
                        case 6:
                            NOTI_TYPE = "6";
                            break;
                        case 7:
                            NOTI_TYPE = "7";
                            break;
                        case 8:
                            NOTI_TYPE = "8";
                            break;
                        case 9:
                            NOTI_TYPE = "9";
                            break;
                        case 10:
                            NOTI_TYPE = "10";
                            break;
                        case 11:
                            NOTI_TYPE = "11";
                            break;
                        case 12:
                            NOTI_TYPE = "0";
                            break;
                        default:
                            NOTI_TYPE = "";
                            break;
                    }
                }

                GetNoticeList(true, NOTI_TYPE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mRecyclerView = findViewById(R.id.notice_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new NoticeActivity.RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    GetNoticeList(false, NOTI_TYPE);
                    isLoading = true;
                }
            }
        });

        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        GetNoticeList(true, NOTI_TYPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("모두 읽음").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ("모두 읽음".equals(item.getTitle())) {
            try {
                AlertDialog.Builder alert_Logout = new AlertDialog.Builder(this);
                alert_Logout.setMessage("알림을 모두 읽음 처리 하시겠습니까?\n\n※읽음 처리한 알림은 '확인된 알림'\n탭에서 확인 가능합니다.").setCancelable(false).setPositiveButton("모두 읽음 처리",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SendReadPushListAll();
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = alert_Logout.create();
                alert.show();
                Button positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.color_text_error));
                negativeButton.setTextColor(getResources().getColor(R.color.color_text_high));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    private void GetNoticeList(boolean isInit, String noti_type) {
        Log.e(TAG, "GetNoticeList : NOTI_TYPE : " + noti_type);
        if (isInit) {
            mCanLoadMore = true;
            page = 0;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
        }

        if (mCanLoadMore) {
            UserInfo userInfo = PrefKit.getUserInfo(this);
            //String body = String.format("PAGE_VIEW_COUNT=%d&CURRENT_PAGE_INDEX=%s&PUSH_TYPE=%s&USER_ID=%s&SYSTEM_ID=%s", MyApplication.PAGE_VIEW_COUNT, ++page, noti_type, userInfo.LOGIN_ID, userInfo.SYS_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            body.put("PUSH_TYPE", noti_type);
            body.put("USER_ID", userInfo.LOGIN_ID);
            body.put("SYSTEM_ID", userInfo.SYS_ID);
            if (page > 1) {
                new TelKit(this, this, mProgressBar).request(TelKit.URL_API_GET_PUSH_LIST, body);
            } else {
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_PUSH_LIST, body);
            }
        }
    }

    private void SendNotiReadStatus(String id) {
        if (id != "" && !NOTI_TYPE.equals("0")) {
            //String body = String.format("PUSH_LIST_ID=%s", id);
            HashMap<String, String> body = new HashMap<>();
            body.put("PUSH_LIST_ID", id);
            new TelKit(this, this).request(TelKit.URL_API_GET_READ_PUSH_LIST, body);
        }
    }

    private void SendReadPushListAll() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            //String body = String.format("user_id=%s", userInfo.LOGIN_ID);
            HashMap<String, String> body = new HashMap<>();
            body.put("user_id", userInfo.LOGIN_ID);
            new TelKit(this, this).request(TelKit.URL_API_READ_PUSH_LIST_ALL, body);
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_PUSH_LIST)) {

                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");

                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        if ("ok".equals(code)) {
                            JSONArray list = json.optJSONArray("list");
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject json_list = list.getJSONObject(i);
                                Log.e("NoticeActivity", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    Notice noticeList = new Notice();

                                    noticeList.TOT_CNT = json_list.optString("TOT_CNT");
                                    noticeList.ROW_NO = json_list.optString("ROW_NO");
                                    noticeList.PUSH_LIST_ID = json_list.optString("PUSH_LIST_ID");
                                    noticeList.PUSH_TYPE = json_list.optString("PUSH_TYPE");
                                    noticeList.CODE_NAME = json_list.optString("CODE_NAME");
                                    noticeList.CREATE_USER = json_list.optString("CREATE_USER");
                                    noticeList.CREATE_DATE = json_list.optString("CREATE_DATE");
                                    noticeList.SEND_CONTENT = json_list.optString("SEND_CONTENT");
                                    noticeList.SEND_SUBJECT = json_list.optString("SEND_SUBJECT");
                                    noticeList.USER_ID = json_list.optString("USER_ID");
                                    noticeList.COMPANY_ID = json_list.optString("COMPANY_ID").replace("null", "");
                                    noticeList.READ_FLAG = json_list.optString("READ_FLAG");
                                    noticeList.FAIR_ID = json_list.optString("FAIR_ID").replace("null", "");
                                    noticeList.SUBJECT = json_list.optString("SUBJECT").replace("null", "");
                                    noticeList.CONTENT = json_list.optString("CONTENT").replace("null", "");
                                    noticeList.USER_NAME = json_list.optString("USER_NAME").replace("null", "");
                                    mItems.add(noticeList);
                                }
                            }
                            //mAdapter.notifyDataSetChanged();
                            isLoading = false;
                        } else {
                            mCanLoadMore = false;
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "알림 목록을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }

            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_READ_PUSH_LIST)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");

                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            Log.d(TAG, "ReadPushList : " + msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ReadPushList Exception");
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_READ_PUSH_LIST_ALL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");

                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            Log.d(TAG, "ReadPushListAll : " + msg);
                            GetNoticeList(true, NOTI_TYPE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ReadPushList Exception");
                }
            } else {
                Toast.makeText(NoticeActivity.this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(NoticeActivity.this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.e(TAG, "onClick id : " + id);
        switch (id) {

            default:
                break;
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<NoticeActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<Notice> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<Notice> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public NoticeActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.notice_item, parent, false);
            return new NoticeActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(NoticeActivity.RecycleAdapter.ItemViewHolder holder, int position) {
            Notice noticelist = mItemList.get(holder.getBindingAdapterPosition());

            holder.position = holder.getBindingAdapterPosition();
            holder.noti_title.setText(noticelist.SEND_SUBJECT);
            holder.noti_msg.setText(noticelist.SEND_CONTENT);
            holder.notice_date.setText(noticelist.CREATE_DATE);
            if (mOnClickListener != null) {
                holder.noti_layoutItem.setOnClickListener(mOnClickListener);
            }
            if (noticelist.PUSH_TYPE.equals("1")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_call));
            } else if (noticelist.PUSH_TYPE.equals("2")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_deposit));
            } else if (noticelist.PUSH_TYPE.equals("3")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_attend));
            } else if (noticelist.PUSH_TYPE.equals("4")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_attend_chagne));
            } else if (noticelist.PUSH_TYPE.equals("5")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_assign));
            } else if (noticelist.PUSH_TYPE.equals("6")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_assign_change));
            } else if (noticelist.PUSH_TYPE.equals("7")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_share));
            } else if (noticelist.PUSH_TYPE.equals("8")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_comments));
            } else if (noticelist.PUSH_TYPE.equals("9")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_notice));
                Spanned result = Html.fromHtml(noticelist.SEND_CONTENT);
                holder.noti_msg.setText(result);
            } else if (noticelist.PUSH_TYPE.equals("10")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_tax));
            } else if (noticelist.PUSH_TYPE.equals("11")) {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_report));
                Spanned result = Html.fromHtml(noticelist.SEND_CONTENT);
                holder.noti_msg.setText(result);
            } else {
                holder.img_fms.setImageDrawable(getDrawable(R.drawable.ico_fms));
            }
            if (noticelist.READ_FLAG.equals("0")) {
                holder.noti_layoutItem.setAlpha(1f);
            } else {
                holder.noti_layoutItem.setAlpha(0.3f);
            }

            UserInfo userInfo = PrefKit.getUserInfo(NoticeActivity.this);
            if (("7".equals(noticelist.PUSH_TYPE) && !noticelist.COMPANY_ID.equals(""))
                    || ("3".equals(userInfo.POSITION_ID) && "5".equals(noticelist.PUSH_TYPE) && !noticelist.FAIR_ID.equals(""))
                    || ("9".equals(noticelist.PUSH_TYPE)) || ("11".equals(noticelist.PUSH_TYPE))) {
                holder.search_icon.setVisibility(View.VISIBLE);
            } else {
                holder.search_icon.setVisibility(View.INVISIBLE);
            }

            Kit.addLinksPhoneNumbers(holder.noti_msg);
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

        private void onItemHolderClick(NoticeActivity.RecycleAdapter.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public NoticeActivity.RecycleAdapter mAdapter;
            public LinearLayout noti_layoutItem;
            public TextView noti_title;
            public TextView noti_msg;
            public TextView notice_date;
            public ImageView search_icon;
            public ImageView img_fms;
            public int position;

            public ItemViewHolder(View itemView, NoticeActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.noti_layoutItem = itemView.findViewById(R.id.noti_layoutItem);
                this.noti_title = itemView.findViewById(R.id.noti_title);
                this.noti_msg = itemView.findViewById(R.id.noti_msg);
                this.notice_date = itemView.findViewById(R.id.notice_date);
                this.search_icon = itemView.findViewById(R.id.search_icon);
                this.img_fms = itemView.findViewById(R.id.img_fms);
                noti_layoutItem.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
                notice = mItems.get(position);
                SendNotiReadStatus(notice.PUSH_LIST_ID);
                v.setAlpha(0.3f);
                notice.READ_FLAG = "1";
                mItems.set(position, notice);
//                mAdapter.notifyItemChanged(position);
                mAdapter.notifyDataSetChanged();
                UserInfo userInfo = PrefKit.getUserInfo(NoticeActivity.this);
                if ("7".equals(notice.PUSH_TYPE) && !notice.COMPANY_ID.equals("")) {
                    Log.e(TAG, "onItemClick notice.COMPANY_ID :" + notice.COMPANY_ID);
                    CompanyInfo companyInfo = new CompanyInfo();
                    companyInfo.COMPANY_ID = Integer.parseInt((notice.COMPANY_ID));
                    info_mItems.add(companyInfo);

                    Intent intent = new Intent(NoticeActivity.this, RegSalesActivity.class);
                    intent.putExtra(Extra.KEY_COMPANY_INFO, companyInfo);
                    intent.putExtra("entry_path", "noti");
                    startActivity(intent);
                } else if ("3".equals(userInfo.POSITION_ID) && "5".equals(notice.PUSH_TYPE) && !notice.FAIR_ID.equals("")) {
                    Intent intent = new Intent(NoticeActivity.this, AssignListActivity.class);
                    intent.putExtra("FAIR_ID", notice.FAIR_ID);
                    startActivity(intent);
                } else if ("9".equals(notice.PUSH_TYPE) || "11".equals(notice.PUSH_TYPE)) {
                    if (Kit.isNotNullNotEmpty(notice.SUBJECT) && Kit.isNotNullNotEmpty(notice.CONTENT)) {
                        Intent intent = new Intent(NoticeActivity.this, WebViewActivity.class);
                        intent.putExtra("SUBJECT", notice.SUBJECT);
                        intent.putExtra("USER_NAME", notice.USER_NAME);
                        startActivity(intent);
                    } else {
                        Toast.makeText(NoticeActivity.this, "공지사항 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
        tabLayout.setScrollPosition(0, 0, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //GetNoticeList(true, NOTI_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.Notification_Check = false;
    }
}
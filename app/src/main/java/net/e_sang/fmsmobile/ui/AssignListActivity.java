package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.weiwangcn.betterspinner.library.BetterSpinner;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.AssignUser;
import net.e_sang.fmsmobile.data.AssignUserList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class AssignListActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private ArrayList<AssignUserList> mItems = new ArrayList<>();
    private ArrayList<AssignUser> mAssignUserItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private String FAIR_ID = "";
    private String FAIR_NAME = "";
    private SmoothProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private boolean isSelectedAll = false;
    private Button assign_yes_btn = null;
    private Button assign_no_btn = null;
    private TextView assign_fair_name = null;
    private BetterSpinner spinner_user;
    private MenuItem OptionsMenu = null;
    private String User_System_ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_allocation);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_list);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_allocation));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_assign_list);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mRecyclerView = findViewById(R.id.assign_list_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        assign_fair_name = findViewById(R.id.assign_fair_name);
        assign_yes_btn = findViewById(R.id.assign_yes_btn);
        assign_no_btn = findViewById(R.id.assign_no_btn);
        spinner_user = findViewById(R.id.spinner_user);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            assign_fair_name.setText(FAIR_NAME);
        }

        assign_yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCount() != 0) {
                    showPopup("승인", getCount(), "2");
                } else {
                    Toast.makeText(AssignListActivity.this, "선택된 배정요청 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        assign_no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCount() != 0) {
                    showPopup("반려", getCount(), "4");
                } else {
                    Toast.makeText(AssignListActivity.this, "선택된 배정요청 항목이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter(this, mItems);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssignUserList assignuserlist = mItems.get(position);
                Log.e(TAG, "onItemClick position: " + position + " id:" + id);
            }
        });

        spinner_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAssignUserItems.size() != 0) {
                    spinner_user.showDropDown();
                } else {
                    Toast.makeText(AssignListActivity.this, "담당자가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinner_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AssignUser assignuser = mAssignUserItems.get(position);
                User_System_ID = assignuser.SYSTEM_ID;
                getAssignList(User_System_ID);
                if (OptionsMenu != null) {
                    OptionsMenu.setTitle("전체선택");
                }
            }
        });

        getAssignList(User_System_ID);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (spinner_user != null && spinner_user.isPopupShowing()) {
                    spinner_user.dismissDropDown();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("전체선택").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        OptionsMenu = item;
        if ("전체선택".equals(item.getTitle())) {
            if (mAdapter.getItemCount() != 0) {
                try {
                    selectAllOnOff(true);
                    item.setTitle("전체해제");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AssignListActivity.this, "배정요청 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else if ("전체해제".equals(item.getTitle())) {
            if (mAdapter.getItemCount() != 0) {
                try {
                    selectAllOnOff(false);
                    item.setTitle("전체선택");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AssignListActivity.this, "배정요청 항목이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        if (spinner_user != null && spinner_user.isPopupShowing()) {
//            spinner_user.dismissDropDown();
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_SET_ASSIGN_STATUS)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("resultAssign");
                    Log.e(TAG, "JSONObject json: " + json);
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            getAssignList(User_System_ID);
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "배정요청 항목 업데이트 중 문제가 발생 하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AssignListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_ASSIGN_LIST_AND_USERS)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        if ("ok".equals(code)) {
                            mItems.clear();
                            JSONObject fair_summary = json.optJSONObject("fair_summary");
                            assign_fair_name.setText(fair_summary.optString("FAIR_NAME"));
                            FAIR_NAME = fair_summary.optString("FAIR_NAME");
                            JSONObject company_list = json.optJSONObject("company_list");
                            String resultList = company_list.optString("list");
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    AssignUserList assignuserlist = new AssignUserList();

                                    assignuserlist.TOT_CNT = json_list.optString("TOT_CNT");
                                    assignuserlist.ROW_NO = json_list.optString("ROW_NO");
                                    assignuserlist.COMPANY_FAIR_REQ_ID = json_list.optString("COMPANY_FAIR_REQ_ID");
                                    assignuserlist.COMPANY_NAME = json_list.optString("COMPANY_NAME");
                                    assignuserlist.FAIR_STATUS_DESC = json_list.optString("FAIR_STATUS_DESC");
                                    assignuserlist.NAME = json_list.optString("NAME");
                                    assignuserlist.ASSIGN_STATUS_DESC = json_list.optString("ASSIGN_STATUS_DESC");
                                    assignuserlist.ASN_REQ_DATE = json_list.optString("ASN_REQ_DATE").substring(0, 10);
                                    assignuserlist.SYSTEM_ID = json_list.optString("SYSTEM_ID");
                                    mItems.add(assignuserlist);
                                }
                            }

                            mAssignUserItems.clear();
                            JSONObject fair_team_list = json.optJSONObject("fair_team_list");
                            String teamList = fair_team_list.optString("list");
                            JSONArray jsonTeamArray = new JSONArray(teamList);
                            if (jsonTeamArray.length() != 0) {
                                AssignUser assignuser = new AssignUser();
                                assignuser.SYSTEM_ID = "";
                                assignuser.NAME = "전체";
                                mAssignUserItems.add(assignuser);
                            }
                            for (int i = 0; i < jsonTeamArray.length(); i++) {
                                JSONObject json_team_list = jsonTeamArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_team_list: " + json_team_list);
                                if (json_team_list != null) {
                                    AssignUser assignuser = new AssignUser();
                                    assignuser.SYSTEM_ID = json_team_list.optString("SYSTEM_ID");
                                    assignuser.USER_ID = json_team_list.optString("USER_ID");
                                    assignuser.NAME = json_team_list.optString("NAME");
                                    assignuser.FAIR_STATUS = json_team_list.optString("FAIR_STATUS");
                                    assignuser.FAIR_STATUS_DESC = json_team_list.optString("FAIR_STATUS_DESC");
                                    mAssignUserItems.add(assignuser);
                                }
                            }

                            String[] UserList = new String[mAssignUserItems.size()];
                            if (mAssignUserItems.size() > 0) {
                                Log.e(TAG, "mAssignUserItems.size() : " + mAssignUserItems.size());
                                for (int i = 0; i < mAssignUserItems.size(); i++) {
                                    UserList[i] = mAssignUserItems.get(i).NAME;
                                }
                            }
                            ArrayAdapter adapterManagers = new ArrayAdapter(this, R.layout.spinner_item, UserList);
                            spinner_user.setAdapter(adapterManagers);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "배정요청 항목을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AssignListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AssignListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AssignListActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    private void selectAllOnOff(boolean type) {
        isSelectedAll = type;
        List<AssignUserList> mItemList = mItems;
        for (int i = 0; i < mItemList.size(); i++) {
            AssignUserList singleStudent = mItemList.get(i);
            singleStudent.isSelected = isSelectedAll;
        }
        mAdapter.notifyDataSetChanged();
    }

    private int getCount() {
        int count = 0;
        List<AssignUserList> mItemList = mItems;
        for (int i = 0; i < mItemList.size(); i++) {
            AssignUserList singleStudent = mItemList.get(i);
            if (singleStudent.isSelected == true) {
                count++;
            }
        }
        return count;
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<AssignUserList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<AssignUserList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.assign_list_item, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            AssignUserList assignuserlist = mItemList.get(position);

            Resources res = getResources();
            holder.assign_title.setText(assignuserlist.COMPANY_NAME);
            if (Kit.isNotNullNotEmpty(assignuserlist.FAIR_STATUS_DESC)) {
                holder.assign_class.setText(assignuserlist.FAIR_STATUS_DESC);
                holder.assign_class.setTextColor(getResources().getColor(R.color.color_396AC0));
            } else {
                holder.assign_class.setText("미등록");
                holder.assign_class.setTextColor(getResources().getColor(R.color.color_DC6A7A));
            }
            holder.assign_name.setText(assignuserlist.NAME);
            holder.assign_date.setText(assignuserlist.ASN_REQ_DATE);
            holder.assign_chk_box.setChecked(assignuserlist.isSelected);

            if (assignuserlist.isSelected) {
                holder.assign_list_item_layout.setBackgroundColor(getResources().getColor(R.color.color_C4ECD5));
            } else {
                holder.assign_list_item_layout.setBackground(null);
            }

            if (mOnClickListener != null) {
                holder.assign_list_item_layout.setOnClickListener(mOnClickListener);
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
                AssignUserList assignuserlist = mItemList.get(itemHolder.getAdapterPosition());
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
                if (itemHolder.assign_chk_box.isChecked()) {
                    assignuserlist.isSelected = false;
                    itemHolder.assign_chk_box.setChecked(false);
                    itemHolder.assign_list_item_layout.setBackground(null);
                } else {
                    assignuserlist.isSelected = true;
                    itemHolder.assign_chk_box.setChecked(true);
                    itemHolder.assign_list_item_layout.setBackgroundColor(getResources().getColor(R.color.color_C4ECD5));
                }
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RecycleAdapter mAdapter;
            public LinearLayout assign_list_item_layout;
            public TextView assign_title;
            public TextView assign_class;
            public TextView assign_name;
            public TextView assign_date;
            public CheckBox assign_chk_box;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.assign_list_item_layout = itemView.findViewById(R.id.assign_list_item_layout);
                this.assign_title = itemView.findViewById(R.id.assign_title);
                this.assign_class = itemView.findViewById(R.id.assign_class);
                this.assign_name = itemView.findViewById(R.id.assign_name);
                this.assign_date = itemView.findViewById(R.id.assign_date);
                this.assign_chk_box = itemView.findViewById(R.id.assign_chk_box);

                assign_list_item_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }

    protected void setAssignStatus(String type) {
        String dest = "";
        List<AssignUserList> mItemList = mItems;
        for (int i = 0; i < mItemList.size(); i++) {
            AssignUserList singleStudent = mItemList.get(i);
            if (singleStudent.isSelected == true) {
                dest += singleStudent.COMPANY_FAIR_REQ_ID + "|" + singleStudent.SYSTEM_ID + ";";
            }
        }

        UserInfo userInfo = PrefKit.getUserInfo(this);
        HashMap<String, String> body = new HashMap<>();
        body.put("FAIR_ID", FAIR_ID);
        body.put("ASSIGN_STATUS", type);
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        body.put("UPDATE_USER", userInfo.LOGIN_ID);
        body.put("DEST_VALUES", dest.substring(0, dest.length() - 1));
        body.put("FAIR_NAME", FAIR_NAME);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_SET_ASSIGN_STATUS, body);
    }

    protected void getAssignList(String userID) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        HashMap<String, String> body = new HashMap<>();
        body.put("FAIR_ID", FAIR_ID);
        body.put("SYSTEM_ID", userID);
        body.put("LOG_SYSTEM_ID", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_ASSIGN_LIST_AND_USERS, body);
    }

    private void showPopup(String type, int cnt, final String status) {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setTitle(type + " 처리 하시겠습니까?");
        alert_confirm.setMessage("선택된 " + cnt + "건의 배정요청 항목을 " + type + " 처리 합니다.").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'YES'
                        setAssignStatus(status);
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 'No'
                        return;
                    }
                });

        AlertDialog alert = alert_confirm.create();
        alert.show();
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

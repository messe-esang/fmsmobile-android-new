package net.e_sang.fmsmobile.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;


import com.weiwangcn.betterspinner.library.BetterSpinner;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressLint("ValidFragment")
public class SalesDetailFragment extends Fragment implements OnClickListener, TelKit.OnResultListener {
    private CompanyInfo mCompanyInfo = null;
    private View mRootView = null;

    private RecyclerView mRecyclerView = null;
    private ArrayList<SalesInfo> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;
    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;
    public RadioButton fair_all_btn;
    public RadioButton fair_my_btn;

    private Activity activity;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
    private BetterSpinner spinnerManagers;
    private ArrayList<RegStaffList> mRegStaffListItems = new ArrayList<>();
    private androidx.appcompat.app.AlertDialog managerDialogFragment;
    private String TAG = getClass().getSimpleName();

    public static SalesDetailFragment getInstance(CompanyInfo companyInfo) {
        SalesDetailFragment fragment = new SalesDetailFragment(companyInfo);
        return fragment;
    }

    protected SalesDetailFragment(CompanyInfo companyInfo) {
        this.mCompanyInfo = companyInfo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetCompanyStaffAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_sales_detail, null);
        }

        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mTxtEmpty = mRootView.findViewById(R.id.txtEmpty);
        mProgressBar = mRootView.findViewById(R.id.progressBar);
        fair_all_btn = mRootView.findViewById(R.id.fair_all_btn);
        fair_my_btn = mRootView.findViewById(R.id.fair_my_btn);
        spinnerManagers = mRootView.findViewById(R.id.spinnerManagers);
        spinnerManagers.setFocusable(false);
        fair_all_btn.setOnClickListener(this);
        fair_my_btn.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter(getContext(), mItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    if (fair_my_btn.isChecked()) {
                        UserInfo userInfo = PrefKit.getUserInfo(getContext());
                        load(false, userInfo.SYS_ID, spinnerManagers.getTag().toString());
                    } else {
                        load(false, "", spinnerManagers.getTag().toString());
                    }
                    isLoading = true;
                }
            }
        });
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        // 담당자
        spinnerManagers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                Toast.makeText(RegSalesActivity.this, regStaffList.COMPANY_STAFF_ID + " / " + regStaffList.STAFF_NAME, Toast.LENGTH_SHORT).show();
                if (position == 0) {
                    spinnerManagers.setTag("");
                    spinnerManagers.setText("전체");
                    if (fair_my_btn.isChecked()) {
                        UserInfo userInfo = PrefKit.getUserInfo(getContext());
                        load(true, userInfo.SYS_ID, "");
                    } else {
                        load(true, "", "");
                    }
                } else {
                    RegStaffList regStaffList = mRegStaffListItems.get(position - 1);
                    spinnerManagers.setTag(regStaffList.COMPANY_STAFF_ID);
                    spinnerManagers.setText(regStaffList.STAFF_NAME + " (" + regStaffList.STAFF_POSITION + ")");
                    if (fair_my_btn.isChecked()) {
                        UserInfo userInfo = PrefKit.getUserInfo(getContext());
                        load(true, userInfo.SYS_ID, regStaffList.COMPANY_STAFF_ID);
                    } else {
                        load(true, "", regStaffList.COMPANY_STAFF_ID);
                    }
                }
            }
        });

        spinnerManagers.setOnClickListener(v -> {
            if (mRegStaffListItems.isEmpty()) {
                Toast.makeText(getContext(), "담당자가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                showManagerDialog();
            }
        });

        spinnerManagers.setTag("");
        spinnerManagers.setText("전체");
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Activity activity = getActivity();
        View view = getView();

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        // TODO Auto-generated method stub
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
    }

    // OnClickListener
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.fair_all_btn) {
            mItems.clear();
            load(true, "", spinnerManagers.getTag().toString());
        } else if (id == R.id.fair_my_btn) {
            mItems.clear();
            UserInfo userInfo = PrefKit.getUserInfo(getContext());
            load(true, userInfo.SYS_ID, spinnerManagers.getTag().toString());
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<SalesInfo> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<SalesInfo> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_sales_detail, parent, false);
            return new RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RecycleAdapter.ItemViewHolder holder, int position) {
            SalesInfo salesInfo = mItemList.get(position);

            holder.txtDate.setText(salesInfo.CREATE_DATE);
            holder.txtSalesClasses.setText(salesInfo.TM_TYPE_DESC);
            holder.txtContent.setText(salesInfo.CONTENT);
            holder.txtTeamName.setText(salesInfo.WORK_GROUP_TYPE);
            holder.txtRegistrant.setText(salesInfo.CREATE_USER);

            if (Kit.isNotNullNotEmpty(salesInfo.STAFF_NAME)) {
                holder.txtstaff_layout.setVisibility(View.VISIBLE);
                holder.txtstaff_name.setText(salesInfo.STAFF_NAME);
            } else {
                holder.txtstaff_layout.setVisibility(View.GONE);
            }

            if (mOnClickListener != null) {
                holder.layoutItem.setOnClickListener(mOnClickListener);
            }

            if (salesInfo.COMPANY_TM_CNT != 0 && !salesInfo.REPLIES.isEmpty()) {
                try {
                    String[] comment_all = salesInfo.REPLIES.split("\\$\\$");
                    for (int c = 0; c < comment_all.length; c++) {
                        String comment = comment_all[c];
                        String[] comment_item = comment.split("\\|\\|");
                        View view = getLayoutInflater().inflate(R.layout.layout_item_sales_comment, null);
                        TextView comments_msg_txt = view.findViewById(R.id.comments_msg_txt);
                        TextView comment_txt_date = view.findViewById(R.id.comment_txt_date);
                        TextView comment_txt_name = view.findViewById(R.id.comment_txt_name);
                        for (int j = 0; j < comment_item.length; j++) {
                            comment_txt_date.setText(comment_item[0]);
                            comment_txt_name.setText(comment_item[1]);
                            comments_msg_txt.setText(comment_item[2]);
                        }
                        holder.comment_layout.addView(view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            UserInfo userInfo = PrefKit.getUserInfo(getContext());
            long now = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(now);
            Date fairDate;
            String getDate = sdf.format(date);
            try {
                date = sdf.parse(getDate);
                fairDate = sdf.parse(salesInfo.CREATE_DATE);

                int compare = date.compareTo(fairDate);
                if (compare > 0) {
                    holder.edit_text_btn.setVisibility(View.GONE);
                } else {
                    if (userInfo.SYS_ID.equals(Integer.toString(salesInfo.SYSTEM_ID))) {
                        holder.edit_text_btn.setVisibility(View.VISIBLE);
                    } else {
                        holder.edit_text_btn.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.edit_text_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomDialog(salesInfo.CONTENT, salesInfo.COMPANY_TM_ID);
                }
            });
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            mTxtEmpty.setVisibility(count > 0 ? View.INVISIBLE : View.VISIBLE);

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
            public LinearLayout layoutItem;
            public TextView txtDate;
            public TextView txtContent;
            public TextView txtSalesClasses;
            public TextView txtTeamName;
            public TextView txtRegistrant;
            public LinearLayout comment_layout;
            public LinearLayout txtstaff_layout;
            public TextView txtstaff_name;
            public Button edit_text_btn;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.txtDate = itemView.findViewById(R.id.txtDate);
                this.txtContent = itemView.findViewById(R.id.txtContent);
                this.txtSalesClasses = itemView.findViewById(R.id.txtSalesClasses);
                this.txtTeamName = itemView.findViewById(R.id.txtTeamName);
                this.txtRegistrant = itemView.findViewById(R.id.txtRegistrant);
                this.comment_layout = itemView.findViewById(R.id.comment_layout);
                this.txtstaff_layout = itemView.findViewById(R.id.txtstaff_layout);
                this.txtstaff_name = itemView.findViewById(R.id.txtstaff_name);
                this.edit_text_btn = itemView.findViewById(R.id.edit_text_btn);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void load(boolean isInit, String system_id, String staff_id) {
        if (isInit) {
            mCanLoadMore = true;
            mItems.clear();
            mRecyclerView.setAdapter(mAdapter);
            page = 0;
        }

        if (mCanLoadMore) {
            String company_id = "";
            if (mCompanyInfo.COMPANY_ID > -1) {
                company_id = mCompanyInfo.COMPANY_ID + "";
            }
            String fair_id = "";
            //String system_id = "";
//            String body = String.format("CURRENT_PAGE_INDEX=%d&PAGE_VIEW_COUNT=%d&COMPANY_ID=%s&FAIR_ID=%s&SYSTEM_ID=%s",
//                    ++page,
//                    MyApplication.PAGE_VIEW_COUNT,
//                    company_id,
//                    fair_id,
//                    system_id);
            HashMap<String, String> body = new HashMap<>();
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("COMPANY_ID", company_id);
            body.put("FAIR_ID", fair_id);
            body.put("SYSTEM_ID", system_id);
            body.put("COMPANY_STAFF_ID", staff_id);
            new TelKit(getActivity(), this, mProgressBar).request(TelKit.URL_API_GET_COUNSEL, body);
        }
    }

    // TelKit.OnResultListener
    @Override
    public void onResult(TelKit.Result result) {
        Context context = getContext();

        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_COUNSEL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONArray list = json.optJSONArray("list");
                        if (list != null) {
                            String[] managers = new String[list.length()];
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject item = list.optJSONObject(i);
                                if (item != null) {
                                    SalesInfo salesInfo = new SalesInfo();
                                    salesInfo.COMPANY_TM_ID = item.optInt("COMPANY_TM_ID");
                                    salesInfo.FAIR_ID = item.optInt("FAIR_ID");
                                    salesInfo.FAIR_DESC = item.optString("FAIR_DESC");
                                    salesInfo.FAIR_MANAGER_ID = item.optInt("FAIR_MANAGER_ID");
                                    salesInfo.COMPANY_STAFF_ID = item.optInt("COMPANY_STAFF_ID");
                                    salesInfo.STAFF_NAME = item.optString("STAFF_NAME");
                                    salesInfo.COMPANY_ID = item.optInt("COMPANY_ID");
                                    salesInfo.TM_TYPE = item.optInt("TM_TYPE");
                                    salesInfo.TM_TYPE_DESC = item.optString("TM_TYPE_DESC");
                                    salesInfo.TM_STATUS = item.optInt("TM_STATUS");
                                    salesInfo.TM_STATUS_DESC = item.optString("TM_STATUS_DESC");
                                    salesInfo.CONTENT = item.optString("CONTENT");
                                    salesInfo.RECALL_DATE = item.optString("RECALL_DATE");
                                    salesInfo.BOOTH_CNT = item.optInt("BOOTH_CNT");
                                    salesInfo.NCT_FLAG = item.optString("NCT_FLAG");
                                    salesInfo.CREATE_USER = item.optString("CREATE_USER");
                                    salesInfo.CREATE_DATE = item.optString("CREATE_DATE");
                                    salesInfo.SYSTEM_ID = item.optInt("SYSTEM_ID");
                                    salesInfo.COMPANY_TM_CNT = item.optInt("COMPANY_TM_CNT");
                                    salesInfo.REPLIES = item.optString("REPLIES");
                                    salesInfo.WORK_GROUP_TYPE = item.optString("WORK_GROUP_TYPE");
                                    mItems.add(salesInfo);
                                }
                            }
                        }
                        isLoading = false;
                    } else {
                        mCanLoadMore = false;
                        Log.e(TAG, "msg :" + msg);
                        //Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_UPDATE_COUNSEL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        if (alertDialog != null && alertDialog.isShowing()) {
                            alertDialog.dismiss();
                        }
                        mItems.clear();
                        load(true, "", spinnerManagers.getTag().toString());
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_STAFF_ALL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONArray staffList = json.optJSONArray("list");
                        Log.e("NameCardSalesDetailFragment", "staffList data : " + json);
                        if (staffList != null) {
                            ArrayList<RegStaffList> regStaffLists = new ArrayList<>();
                            for (int i = 0; i < staffList.length(); i++) {
                                JSONObject stafflist_obj = staffList.optJSONObject(i);
                                if (stafflist_obj != null) {
                                    RegStaffList regStaffList = new RegStaffList();
                                    regStaffList.TOT_CNT = stafflist_obj.optString("TOT_CNT");
                                    regStaffList.ROW_NO = stafflist_obj.optString("ROW_NO");
                                    regStaffList.FAIR_ID = stafflist_obj.optString("FAIR_ID");
                                    regStaffList.COMPANY_STAFF_ID = stafflist_obj.optString("COMPANY_STAFF_ID");
                                    regStaffList.STAFF_DEPT = stafflist_obj.optString("STAFF_DEPT");
                                    regStaffList.STAFF_POSITION = stafflist_obj.optString("STAFF_POSITION");
                                    regStaffList.STAFF_NAME = stafflist_obj.optString("STAFF_NAME");
                                    regStaffList.STAFF_MOBILE = stafflist_obj.optString("STAFF_MOBILE");
                                    regStaffList.STAFF_EMAIL = stafflist_obj.optString("STAFF_EMAIL");
                                    regStaffList.STAFF_PHONE = stafflist_obj.optString("STAFF_PHONE");
                                    regStaffList.STAFF_ROLEs = stafflist_obj.optString("STAFF_ROLEs");
                                    regStaffList.STAFF_ROLEs_DESC = stafflist_obj.optString("STAFF_ROLEs_DESC");
                                    regStaffList.FAIR_DESC = stafflist_obj.optString("FAIR_DESC");
                                    regStaffList.FAIR_NAME = stafflist_obj.optString("FAIR_NAME");
                                    regStaffList.WORK_FLAG = stafflist_obj.optString("WORK_FLAG");
                                    regStaffList.CREATE_DATE = stafflist_obj.optString("CREATE_DATE");
                                    regStaffList.UPDATE_DATE = stafflist_obj.optString("UPDATE_DATE");
                                    regStaffLists.add(regStaffList);
                                }
                            }
                            mRegStaffListItems.addAll(regStaffLists);
                            RegStaffList item = new RegStaffList();
                            item.STAFF_NAME = "전체";
                            item.STAFF_POSITION = "";
                            item.COMPANY_STAFF_ID = "";
                            mRegStaffListItems.add(0, item);
                        }
                    } else {
                        //Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "msg :" + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void CustomDialog(String content, int tm_id) {
        dialogBuilder = new AlertDialog.Builder(activity);
        View layoutView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        Button btnCancle = layoutView.findViewById(R.id.btnCancle);
        Button btnSave = layoutView.findViewById(R.id.btnSave);
        EditText put_txtContent = layoutView.findViewById(R.id.put_txtContent);
        TextView txtErrorContent = layoutView.findViewById(R.id.txtErrorContent);

        put_txtContent.setText(content);
        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        alertDialog.show();

        btnCancle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        put_txtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0 && put_txtContent.getText().length() == 0) {
                    btnSave.setEnabled(false);
                    txtErrorContent.setVisibility(View.VISIBLE);
                } else {
                    if (put_txtContent.getText().toString().trim().isEmpty()) {
                        btnSave.setEnabled(false);
                        txtErrorContent.setVisibility(View.VISIBLE);
                    } else {
                        btnSave.setEnabled(true);
                        txtErrorContent.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateCounsel(tm_id, put_txtContent.getText().toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        if (managerDialogFragment != null && managerDialogFragment.isShowing()) {
            managerDialogFragment.dismiss();
        }

    }

    protected void UpdateCounsel(int tm_id, String content) {
        UserInfo userInfo = PrefKit.getUserInfo(getContext());
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_TM_ID", String.valueOf(tm_id));
        body.put("CONTENT", content);
        body.put("UPDATE_USER", userInfo.LOGIN_ID);
        new TelKit(getActivity(), this, mProgressBar).request(TelKit.URL_API_UPDATE_COUNSEL, body);
    }

    public static class ManagerAdapter extends RecyclerView.Adapter<SalesDetailFragment.ManagerAdapter.ViewHolder> {

        private List<RegStaffList> list;
        private SalesDetailFragment.ManagerAdapter.OnItemClickListener listener;

        public interface OnItemClickListener {
            void onClick(RegStaffList item);
        }

        public ManagerAdapter(List<RegStaffList> list, SalesDetailFragment.ManagerAdapter.OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SalesDetailFragment.ManagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_manager_list_item, parent, false);
            return new SalesDetailFragment.ManagerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SalesDetailFragment.ManagerAdapter.ViewHolder holder, int position) {

            RegStaffList item = list.get(position);
            if (item.WORK_FLAG.equals("N")) {
                holder.tvName.setText("(퇴직) " + item.STAFF_NAME);
            } else {
                holder.tvName.setText(item.STAFF_NAME);
            }
            if (Kit.isNotNullNotEmpty(item.STAFF_POSITION)) {
                holder.tvLine.setVisibility(View.VISIBLE);
                holder.tvPosition.setText(item.STAFF_POSITION);
            } else {
                holder.tvLine.setVisibility(View.GONE);
                holder.tvPosition.setText("");
            }

            if (!item.STAFF_NAME.equals("전체")) {
                if (Kit.isNotNullNotEmpty(item.UPDATE_DATE)) {
                    holder.tvDate.setVisibility(View.VISIBLE);
                    holder.tvDate.setText("최종 수정 : " + item.UPDATE_DATE);
                } else {
                    holder.tvDate.setText("최종 수정 : " + item.CREATE_DATE);
                }
                if (item.CREATE_DATE.isEmpty() && item.UPDATE_DATE.isEmpty()) {
                    holder.tvDate.setVisibility(View.INVISIBLE);
                }
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLine, tvPosition, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvLine = itemView.findViewById(R.id.tvLine);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }

    private void showManagerDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_manager_list, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerManager);
        Button btnCancel = view.findViewById(R.id.dialog_cancel);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Log.e(TAG, "mRegStaffListItems : " + mRegStaffListItems.size());

        SalesDetailFragment.ManagerAdapter adapter = new SalesDetailFragment.ManagerAdapter(mRegStaffListItems, item -> {

            spinnerManagers.setTag(item.COMPANY_STAFF_ID);

            if (item.WORK_FLAG.equals("N")) {
                if (item.STAFF_POSITION.isEmpty()) {
                    spinnerManagers.setText("(퇴직) " + item.STAFF_NAME);
                } else {
                    spinnerManagers.setText("(퇴직) " + item.STAFF_NAME + " (" + item.STAFF_POSITION + ")");
                }
            } else {
                if (item.STAFF_POSITION.isEmpty()) {
                    spinnerManagers.setText(item.STAFF_NAME);
                } else {
                    spinnerManagers.setText(item.STAFF_NAME + " (" + item.STAFF_POSITION + ")");
                }
            }

            if (item.STAFF_NAME.equals("전체")) {
                if (fair_my_btn.isChecked()) {
                    UserInfo userInfo = PrefKit.getUserInfo(getContext());
                    load(true, userInfo.SYS_ID, "");
                } else {
                    load(true, "", "");
                }
            } else {
                if (fair_my_btn.isChecked()) {
                    UserInfo userInfo = PrefKit.getUserInfo(getContext());
                    load(true, userInfo.SYS_ID, item.COMPANY_STAFF_ID);
                } else {
                    load(true, "", item.COMPANY_STAFF_ID);
                }
            }
            managerDialogFragment.dismiss();
        });
        btnCancel.setOnClickListener(v -> managerDialogFragment.dismiss());

        recyclerView.setAdapter(adapter);

        builder.setView(view);   // ✅ 반드시 create() 전에 호출

        managerDialogFragment = builder.create();  // ✅ 여기서 생성

        if (!managerDialogFragment.isShowing()) {
            managerDialogFragment.show();
        }
        Window window = managerDialogFragment.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            if (mRegStaffListItems.size() <= 5) {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            } else {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.6)
                );
            }
        }
    }

    protected void GetCompanyStaffAll() {
        HashMap<String, String> body = new HashMap<>();
        body.put("PAGE_VIEW_COUNT", "100");
        body.put("CURRENT_PAGE_INDEX", "1");
        body.put("COMPANY_ID", String.valueOf(mCompanyInfo.COMPANY_ID));
        new TelKit(getContext(), this).request(TelKit.URL_API_GET_STAFF_ALL, body);
    }
}
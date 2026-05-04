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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.Receivable;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class ReceivableDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView = null;
    private RecyclerView mRecyclerViewDeposit = null;
    private ArrayList<Receivable.ReceivablePerson> mPerson_Items = new ArrayList<>();
    private ArrayList<Receivable.ReceivableDeposit> mDeposit_Items = new ArrayList<>();
    private ReceivableDetailActivity.RecycleAdapter mAdapter = null;
    private ReceivableDetailActivity.RecycleAdapterDeposit mAdapterDeposit = null;
    private TextView mTxtEmpty = null;
    private TextView mTxtEmpty2 = null;
    private TextView receivable_detail_title = null;
    private TextView receivable_detail_rating = null;
    private TextView receivable_balance_amt = null;
    private TextView receivable_amt = null;
    private String FAIR_ID = "";
    private String COMPANY_ID = "";
    private String COMPANY_FAIR_REQ_ID = "";
    private String BALANCE_AMT = "";
    private String DEPOSIT_AMT = "";
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_receivable);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receivable_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_receivable));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_receivable_detail);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
            COMPANY_ID = intent.getExtras().getString("COMPANY_ID");
            COMPANY_FAIR_REQ_ID = intent.getExtras().getString("COMPANY_FAIR_REQ_ID");
            BALANCE_AMT = intent.getExtras().getString("BALANCE_AMT");
            DEPOSIT_AMT = intent.getExtras().getString("DEPOSIT_AMT");
            Log.e(TAG, "FAIR_ID : " + FAIR_ID);
            Log.e(TAG, "COMPANY_ID : " + COMPANY_ID);
            Log.e(TAG, "COMPANY_FAIR_REQ_ID : " + COMPANY_FAIR_REQ_ID);
            Log.e(TAG, "BALANCE_AMT : " + BALANCE_AMT);
            Log.e(TAG, "DEPOSIT_AMT : " + DEPOSIT_AMT);
        }

        mRecyclerView = findViewById(R.id.person_recycler);
        mTxtEmpty = findViewById(R.id.txtEmpty);

        mRecyclerViewDeposit = findViewById(R.id.deposit_recycler);
        mTxtEmpty2 = findViewById(R.id.txtEmpty2);

        receivable_detail_title = findViewById(R.id.receivable_detail_title);
        receivable_detail_rating = findViewById(R.id.receivable_detail_rating);
        receivable_balance_amt = findViewById(R.id.receivable_balance_amt);
        receivable_amt = findViewById(R.id.receivable_amt);

        //업무 담당자
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ReceivableDetailActivity.RecycleAdapter(this, mPerson_Items);
        mRecyclerView.setAdapter(mAdapter);

        //입금내역
        LinearLayoutManager layoutManagerDeposit = new LinearLayoutManager(this);
        layoutManagerDeposit.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewDeposit.setLayoutManager(layoutManagerDeposit);
        mAdapterDeposit = new ReceivableDetailActivity.RecycleAdapterDeposit(this, mDeposit_Items);
        mRecyclerViewDeposit.setAdapter(mAdapterDeposit);

        geReceivableDetail();
    }

    protected void geReceivableDetail() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("fair_id=%s&company_id=%s&company_fair_req_id=%s&system_id=%s", FAIR_ID, COMPANY_ID, COMPANY_FAIR_REQ_ID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("fair_id", FAIR_ID);
        body.put("company_id", COMPANY_ID);
        body.put("company_fair_req_id", COMPANY_FAIR_REQ_ID);
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_RECEIVABLE_DETAIL, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_RECEIVABLE_DETAIL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String resultStr = json.optString("result");

                    JSONObject result_obj = new JSONObject(resultStr);
                    String code = result_obj.optString("code");
                    String msg = result_obj.optString("msg");

                    if ("ok".equals(code)) {
                        String data = json.optString("data");
                        JSONObject data_obj = new JSONObject(data);
                        String company_info = data_obj.optString("company_info");
                        String staff_list = data_obj.optString("staff_list");
                        String receivable = data_obj.optString("receivable");
                        String deposit_history = data_obj.optString("deposit_history");
                        Kit.log(Kit.LogType.TEST, "company_info = " + company_info);
                        Kit.log(Kit.LogType.TEST, "staff_list = " + staff_list);
                        Kit.log(Kit.LogType.TEST, "receivable = " + receivable);
                        Kit.log(Kit.LogType.TEST, "deposit_history = " + deposit_history);

                        if (company_info != "") {
                            JSONObject company_info_obj = new JSONObject(company_info);
                            String COMPANY_NAME = company_info_obj.optString("COMPANY_NAME");
                            String FAIR_STATUS_DESC = company_info_obj.optString("FAIR_STATUS_DESC");

                            receivable_detail_title.setText(COMPANY_NAME);
                            if (Kit.isNotNullNotEmpty(FAIR_STATUS_DESC)) {
                                receivable_detail_rating.setText(FAIR_STATUS_DESC);
                            } else {
                                receivable_detail_rating.setText("미등록");
                            }
                            receivable_balance_amt.setText(BALANCE_AMT);
                            receivable_amt.setText(DEPOSIT_AMT);
                        }
                        if (staff_list != "") {
                            JSONArray staff_list_array = new JSONArray(staff_list);
                            if (staff_list_array.length() > 0) {
                                for (int i = 0; i < staff_list_array.length(); i++) {
                                    JSONObject json_staff_list = staff_list_array.getJSONObject(i);
                                    if (json_staff_list != null) {
                                        Receivable.ReceivablePerson receivableperson = new Receivable.ReceivablePerson();
                                        receivableperson.receivable_detail_name = json_staff_list.optString("STAFF_NAME");
                                        receivableperson.receivable_detail_position = json_staff_list.optString("STAFF_POSITION");
                                        receivableperson.receivable_detail_dept = json_staff_list.optString("STAFF_DEPT");
                                        receivableperson.receivable_detail_phone = json_staff_list.optString("STAFF_PHONE");
                                        receivableperson.receivable_detail_mobile = json_staff_list.optString("STAFF_MOBILE");
                                        mPerson_Items.add(receivableperson);
                                    }
                                }
                            }
                        }
//                        if (receivable != "") {
//                            JSONObject receivable_obj = new JSONObject(receivable);
//                        }
                        if (deposit_history != "") {
                            JSONArray deposit_history_array = new JSONArray(deposit_history);
                            Kit.log(Kit.LogType.TEST, "deposit_history_array.length() = " + deposit_history_array.length());
                            if (deposit_history_array.length() > 0) {
                                for (int i = 0; i < deposit_history_array.length(); i++) {
                                    JSONObject json_deposit_history = deposit_history_array.getJSONObject(i);
                                    if (json_deposit_history != null) {
                                        Receivable.ReceivableDeposit receivableDeposit = new Receivable.ReceivableDeposit();
                                        receivableDeposit.receivable_detail_date = json_deposit_history.optString("DEPOSIT_DATE");
                                        receivableDeposit.receivable_detail_msg = json_deposit_history.optString("CONTENT");
                                        receivableDeposit.receivable_detail_money = convertCurrencyStr(Double.parseDouble(json_deposit_history.optString("DEPOSIT_AMT").replace("null", "0")));
                                        mDeposit_Items.add(receivableDeposit);
                                    }
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        mAdapterDeposit.notifyDataSetChanged();
                        //Toast.makeText(ReceivableDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(ReceivableDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ReceivableDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReceivableDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ReceivableDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 업무담당자
    public class RecycleAdapter extends RecyclerView.Adapter<ReceivableDetailActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<Receivable.ReceivablePerson> mPerson_Items;

        public RecycleAdapter(Context context, List<Receivable.ReceivablePerson> itemList) {
            this.mContext = context;
            this.mPerson_Items = itemList;
        }

        @Override
        public ReceivableDetailActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.person_item, parent, false);
            return new ReceivableDetailActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ReceivableDetailActivity.RecycleAdapter.ItemViewHolder holder, int position) {
            Receivable.ReceivablePerson personlist = mPerson_Items.get(position);

            holder.receivable_detail_name.setText(personlist.receivable_detail_name);
            holder.receivable_detail_position.setText(personlist.receivable_detail_position);
            if (!personlist.receivable_detail_dept.equals("")) {
                holder.receivable_detail_dept.setText("(" + personlist.receivable_detail_dept + ")");
            }
            if (personlist.receivable_detail_position.equals("") && personlist.receivable_detail_dept.equals("")) {
                holder.position_layout.setVisibility(View.GONE);
            } else {
                holder.position_layout.setVisibility(View.VISIBLE);
            }
            holder.receivable_detail_phone.setText(personlist.receivable_detail_phone);
            holder.receivable_detail_mobile.setText(personlist.receivable_detail_mobile);
        }

        @Override
        public int getItemCount() {
            int count = mPerson_Items.size();
            mTxtEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public ReceivableDetailActivity.RecycleAdapter mAdapter;
            public TextView receivable_detail_name;
            public TextView receivable_detail_position;
            public TextView receivable_detail_dept;
            public TextView receivable_detail_phone;
            public TextView receivable_detail_mobile;
            public LinearLayout position_layout;

            public ItemViewHolder(View itemView, ReceivableDetailActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.receivable_detail_name = itemView.findViewById(R.id.receivable_detail_name);
                this.receivable_detail_position = itemView.findViewById(R.id.receivable_detail_position);
                this.receivable_detail_dept = itemView.findViewById(R.id.receivable_detail_dept);
                this.receivable_detail_phone = itemView.findViewById(R.id.receivable_detail_phone);
                this.receivable_detail_mobile = itemView.findViewById(R.id.receivable_detail_mobile);
                this.position_layout = itemView.findViewById(R.id.position_layout);
            }
        }
    }

    // 입금내역
    public class RecycleAdapterDeposit extends RecyclerView.Adapter<ReceivableDetailActivity.RecycleAdapterDeposit.ItemViewHolder> {
        private Context mContext;
        private List<Receivable.ReceivableDeposit> mDeposit_Items;

        public RecycleAdapterDeposit(Context context, List<Receivable.ReceivableDeposit> itemList) {
            this.mContext = context;
            this.mDeposit_Items = itemList;
        }

        @Override
        public ReceivableDetailActivity.RecycleAdapterDeposit.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.deposit_item, parent, false);
            return new ReceivableDetailActivity.RecycleAdapterDeposit.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ReceivableDetailActivity.RecycleAdapterDeposit.ItemViewHolder holder, int position) {
            Receivable.ReceivableDeposit depositlist = mDeposit_Items.get(position);
            holder.receivable_detail_date.setText(depositlist.receivable_detail_date.substring(0, 10));
            holder.receivable_detail_msg.setText(depositlist.receivable_detail_msg);
            holder.receivable_detail_money.setText(depositlist.receivable_detail_money);
        }

        @Override
        public int getItemCount() {
            int count = mDeposit_Items.size();
            mTxtEmpty2.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public ReceivableDetailActivity.RecycleAdapterDeposit mAdapterDeposit;
            public TextView receivable_detail_date;
            public TextView receivable_detail_msg;
            public TextView receivable_detail_money;

            public ItemViewHolder(View itemView, ReceivableDetailActivity.RecycleAdapterDeposit mAdapter) {
                super(itemView);

                this.mAdapterDeposit = mAdapter;
                this.receivable_detail_date = itemView.findViewById(R.id.receivable_detail_date);
                this.receivable_detail_msg = itemView.findViewById(R.id.receivable_detail_msg);
                this.receivable_detail_money = itemView.findViewById(R.id.receivable_detail_money);
            }
        }
    }
}

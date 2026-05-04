package net.e_sang.fmsmobile.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.Infrastructure;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class InfrastructureFragment extends Fragment implements OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    public CompanyInfo mCompanyInfo = null;
    public String FAIR_ID = "";
    private View mRootView = null;

    private RecyclerView mRecyclerView = null;
    private ArrayList<Infrastructure> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;

    private Activity activity;

    public static InfrastructureFragment getInstance(CompanyInfo companyInfo) {
        InfrastructureFragment fragment = new InfrastructureFragment(companyInfo);
        return fragment;
    }

    protected InfrastructureFragment(CompanyInfo companyInfo) {
        this.mCompanyInfo = companyInfo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_infrastructure, null);
        }

        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mTxtEmpty = mRootView.findViewById(R.id.txtEmpty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration did = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(did);
        mAdapter = new RecycleAdapter(getContext(), mItems);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

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
        switch (id) {

            default:
                break;
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context mContext;
        private List<Infrastructure> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        private static final int VIEWTYPE_HEADER = 0;
        private static final int VIEWTYPE_ROW = 1;

        public RecycleAdapter(Context context, List<Infrastructure> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEWTYPE_HEADER) {
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_infrastructure_header, parent, false);
                return new RecycleAdapter.ItemViewHolderHeader(itemView, this);
            } else {
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_infrastructure_row, parent, false);
                return new RecycleAdapter.ItemViewHolderRow(itemView, this);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Infrastructure infra = mItemList.get(position);

            if (holder.getItemViewType() == VIEWTYPE_HEADER) {
                RecycleAdapter.ItemViewHolderHeader ivhh = (RecycleAdapter.ItemViewHolderHeader) holder;
                ivhh.txtTitle.setText(infra.header_name);
            } else {
                RecycleAdapter.ItemViewHolderRow ivhr = (RecycleAdapter.ItemViewHolderRow) holder;
                ivhr.txtItemName.setText(infra.FACIL_NAME);
                ivhr.txtCount.setText(Kit.convertCurrencyStr(infra.REQ_CNT));
                if (mOnClickListener != null) {
                    ivhr.layoutItem.setOnClickListener(mOnClickListener);
                }
            }
        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            mTxtEmpty.setVisibility(count > 0 ? View.INVISIBLE : View.VISIBLE);

            return count;
        }

        @Override
        public int getItemViewType(int position) {
//            return super.getItemViewType(position);

            if (mItemList.get(position).is_header) {
                return VIEWTYPE_HEADER;
            } else {
                return VIEWTYPE_ROW;
            }
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(RecycleAdapter.ItemViewHolderRow itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolderHeader extends RecyclerView.ViewHolder {
            public TextView txtTitle;

            public ItemViewHolderHeader(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.txtTitle = itemView.findViewById(R.id.txtTitle);
            }
        }

        public class ItemViewHolderRow extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RecycleAdapter mAdapter;
            public LinearLayout layoutItem;
            public TextView txtItemName;
            public TextView txtCount;

            public ItemViewHolderRow(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.layoutItem = itemView.findViewById(R.id.layoutItem);
                this.txtItemName = itemView.findViewById(R.id.txtItemName);
                this.txtCount = itemView.findViewById(R.id.txtCount);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void load() {
        mItems.clear();
        mRecyclerView.setAdapter(mAdapter);

        String COMPANY_ID = "";
        if (mCompanyInfo.COMPANY_ID != -1) {
            COMPANY_ID = mCompanyInfo.COMPANY_ID + "";
        }

        // 데이터가 없으면 탭이 비활성화 처리되므로 아래 주석 처리
//        if (COMPANY_ID.isEmpty() || FAIR_ID.isEmpty()) {
//            mAdapter.notifyDataSetChanged();
//            return;
//        }

        //String body = String.format("COMPANY_ID=%s&FAIR_ID=%s", COMPANY_ID, FAIR_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_ID", COMPANY_ID);
        body.put("FAIR_ID", FAIR_ID);
        new TelKit(getActivity(), this).request(TelKit.URL_API_COMPANY_FACIL_REQ, body);
    }

    // TelKit.OnResultListener
    @Override
    public void onResult(TelKit.Result result) {
        Context context = getContext();

        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_COMPANY_FACIL_REQ)) {
                boolean enable = false;
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONArray list = json.optJSONArray("list");
                        if (list != null) {
                            if (list.length() > 0) {
                                enable = true;
                            }

                            mItems.clear();
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject item = list.optJSONObject(i);
                                if (item != null) {
                                    Infrastructure infrastructure = new Infrastructure();
                                    infrastructure.COMPANY_NAME = item.optString("COMPANY_NAME");
                                    infrastructure.FMS_FAIR_NAME = item.optString("FMS_FAIR_NAME");
                                    infrastructure.FACIL_NAME = item.optString("FACIL_NAME");
                                    infrastructure.REQ_CNT = item.optInt("REQ_CNT");
                                    infrastructure.COMPANY_FAIR_REQ_ID = item.optInt("COMPANY_FAIR_REQ_ID");
                                    mItems.add(infrastructure);
                                }
                            }

                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        //Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "msg :" + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }

                Activity activity = getActivity();
                if (activity instanceof RegSalesActivity) {
                    RegSalesActivity regSalesActivity = (RegSalesActivity) activity;
                    regSalesActivity.setTabEnabled(3, enable);
                } else if(activity instanceof NameCardRegSalesActivity) {
                    NameCardRegSalesActivity nameCardRegSalesActivity = (NameCardRegSalesActivity) activity;
                    nameCardRegSalesActivity.setTabEnabled(3, enable);
                }
            }
        }
    }
}
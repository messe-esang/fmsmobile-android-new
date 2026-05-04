package net.e_sang.fmsmobile.ui;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
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

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.FairEnterHistory;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

@SuppressLint("ValidFragment")
public class FairHistoryFragment extends Fragment implements OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    public CompanyInfo mCompanyInfo = null;
    private View mRootView = null;

    private RecyclerView mRecyclerView = null;
    private ArrayList<FairEnterHistory> mItems = new ArrayList<>();
    private RecycleAdapter mAdapter = null;
    private TextView mTxtEmpty = null;

    private int page = 0;
    private boolean mCanLoadMore = true;
    private boolean isLoading = false;      // 스크롤 페이징 처리시 사용
    private SmoothProgressBar mProgressBar = null;

    private Activity activity;

    public static FairHistoryFragment getInstance(CompanyInfo companyInfo) {
        FairHistoryFragment fragment = new FairHistoryFragment(companyInfo);
        return fragment;
    }

    protected FairHistoryFragment(CompanyInfo companyInfo) {
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
            mRootView = inflater.inflate(R.layout.fragment_fair_history, null);
        }

        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        mTxtEmpty = mRootView.findViewById(R.id.txtEmpty);
        mProgressBar = mRootView.findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration did = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(did);
        mAdapter = new RecycleAdapter(getContext(), mItems);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLoading && !mRecyclerView.canScrollVertically(1)) {
                    load(false);
                    isLoading = true;
                }
            }
        });
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
        private List<FairEnterHistory> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        private static final int VIEWTYPE_HEADER = 0;
        private static final int VIEWTYPE_ROW = 1;

        public RecycleAdapter(Context context, List<FairEnterHistory> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEWTYPE_HEADER) {
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_fair_history_header, parent, false);
                return new RecycleAdapter.ItemViewHolderHeader(itemView, this);
            } else {
                View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_fair_history_row, parent, false);
                return new RecycleAdapter.ItemViewHolderRow(itemView, this);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            FairEnterHistory history = mItemList.get(position);

            //if (holder.getItemViewType() == VIEWTYPE_HEADER) {
            //    ItemViewHolderHeader ivhh = (ItemViewHolderHeader)holder;
            //    ivhh.txtTitle.setText(history.header_name);
            //} else {
            ItemViewHolderRow ivhr = (ItemViewHolderRow) holder;
            if ("Y".equalsIgnoreCase(history.FAIR_EXT_FLAG)) {
                ivhr.fair_history_row_layout.setBackgroundColor(getResources().getColor(R.color.color_FAFFD5));
            } else {
                ivhr.fair_history_row_layout.setBackground(null);
            }
            ivhr.txtFairName.setText(history.ATTEND_FAIR_NAME);
            ivhr.txtStep.setText(history.step);
            ivhr.txtBoothCount.setText(history.booth_count + " 부스");
            if (TextUtils.isEmpty(Integer.toString(history.TOTAL_SALE_AMT))) {
                ivhr.txtTotalSaleAmt.setText("- 원");
            } else {
                ivhr.txtTotalSaleAmt.setText(convertCurrencyStr(Double.parseDouble(Integer.toString(history.TOTAL_SALE_AMT))) + "원");
            }

            //ivhr.txtBoothCount.setText(history.BOOTH_CNT + " 부스");
            if (mOnClickListener != null) {
                ivhr.fair_history_row_layout.setOnClickListener(mOnClickListener);
            }
            //}
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
            public LinearLayout fair_history_row_layout;
            public TextView txtFairName;
            public TextView txtStep;
            public TextView txtBoothCount;
            public TextView txtTotalSaleAmt;

            public ItemViewHolderRow(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.fair_history_row_layout = itemView.findViewById(R.id.fair_history_row_layout);
                this.txtFairName = itemView.findViewById(R.id.txtFairName);
                this.txtStep = itemView.findViewById(R.id.txtStep);
                this.txtBoothCount = itemView.findViewById(R.id.txtBoothCount);
                this.txtTotalSaleAmt = itemView.findViewById(R.id.txtTotalSaleAmt);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
            }
        }
    }

    protected void load(boolean isInit) {
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
            HashMap<String, String> body = new HashMap<>();
            body.put("CURRENT_PAGE_INDEX", String.valueOf(++page));
            body.put("PAGE_VIEW_COUNT", String.valueOf(MyApplication.PAGE_VIEW_COUNT));
            body.put("COMPANY_ID", company_id);

//            String body = String.format("CURRENT_PAGE_INDEX=%d&PAGE_VIEW_COUNT=%d&COMPANY_ID=%s",
//                    ++page,
//                    MyApplication.PAGE_VIEW_COUNT,
//                    company_id);
            new TelKit(getActivity(), this, mProgressBar).request(TelKit.URL_API_GET_FAIR_HISTORY, body);
        }
    }

    // TelKit.OnResultListener
    @Override
    public void onResult(TelKit.Result result) {
        Context context = getContext();

        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_FAIR_HISTORY)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONArray list = json.optJSONArray("list");
                        if (list != null) {
                            //mItems.clear();
                            ArrayList<FairEnterHistory> myFairEnterHistories = new ArrayList<>();
                            //ArrayList<FairEnterHistory> otherFairEnterHistories = new ArrayList<>();
                            FairEnterHistory fairEnterHistory;
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject item = list.optJSONObject(i);
                                if (item != null) {
                                    fairEnterHistory = new FairEnterHistory();
                                    fairEnterHistory.COMPANY_FAIR_ATTEND_ID = item.optInt("COMPANY_FAIR_ATTEND_ID");
                                    fairEnterHistory.ATTEND_YEAR = item.optString("ATTEND_YEAR");
                                    fairEnterHistory.FAIR_PLACE_TXT = item.optString("FAIR_PLACE_TXT");
                                    fairEnterHistory.FAIR_SEQ = item.optInt("FAIR_SEQ");
                                    fairEnterHistory.ATTEND_FAIR_NAME = item.optString("ATTEND_FAIR_NAME");
                                    fairEnterHistory.NAME = item.optString("NAME");
                                    fairEnterHistory.CREATE_DATE = item.optString("CREATE_DATE");
                                    fairEnterHistory.FAIR_EXT_FLAG = item.optString("FAIR_EXT_FLAG");
                                    fairEnterHistory.FAIR_STATUS_DESC = item.optString("FAIR_STATUS_DESC");
                                    fairEnterHistory.booth_count = item.optInt("BOOTH_CNT");
                                    fairEnterHistory.TOTAL_SALE_AMT = item.optInt("TOTAL_SALE_AMT");
                                    if (item.optString("FAIR_STATUS_DESC").equals("A1")) {
                                        fairEnterHistory.step = "참가";
                                    } else {
                                        fairEnterHistory.step = "참가취소";
                                    }
                                    //if ("Y".equalsIgnoreCase(fairEnterHistory.FAIR_EXT_FLAG)) {
                                    //    otherFairEnterHistories.add(fairEnterHistory);
                                    //} else {
                                    myFairEnterHistories.add(fairEnterHistory);
                                    //}
                                }
                            }

                            mItems.addAll(myFairEnterHistories);
//                            if (otherFairEnterHistories.size() > 0) {
//                                fairEnterHistory = new FairEnterHistory();
//                                fairEnterHistory.is_header = true;
//                                fairEnterHistory.header_name = "타 전시회 참가 이력";
//                                fairEnterHistory.is_other_fair = true;
//                                mItems.add(fairEnterHistory);
//                            }
//                            mItems.addAll(otherFairEnterHistories);
                        }
                        isLoading = false;
                    } else {
                        mCanLoadMore = false;
                        //Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "msg :" + msg);
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
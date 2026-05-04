package net.e_sang.fmsmobile.ui;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.VisitorStatusDetail;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitorStatusDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitorStatusDetailFragment extends Fragment implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FAIR_NAME = "FAIR_NAME";
    private static final String FAIR_DATE = "FAIR_DATE";
    private static final String FAIR_ID = "FAIR_ID";

    // TODO: Rename and change types of parameters
    private String mFAIR_NAME;
    private String mFAIR_DATE;
    private String mFAIR_ID;


    private TextView visitor_status_fair_name = null;
    private TextView visitor_status_fair_date = null;
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RecyclerView mRecyclerView = null;
    private VisitorStatusDetailFragment.RecycleAdapter mAdapter = null;
    private ArrayList<VisitorStatusDetail> mVisitorStatus_Items = new ArrayList<>();
    private LinearLayout layoutEmpty = null;
    private HorizontalBarChart horizontal_bar_chart = null;
    private SwipeRefreshLayout refresh_layout = null;

    public VisitorStatusDetailFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static VisitorStatusDetailFragment newInstance(String mFAIR_NAME, String mFAIR_DATE, String mFAIR_ID) {
        VisitorStatusDetailFragment fragment = new VisitorStatusDetailFragment();
        Bundle args = new Bundle();
        args.putString(FAIR_NAME, mFAIR_NAME);
        args.putString(FAIR_DATE, mFAIR_DATE);
        args.putString(FAIR_ID, mFAIR_ID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFAIR_NAME = getArguments().getString(FAIR_NAME);
            mFAIR_DATE = getArguments().getString(FAIR_DATE);
            mFAIR_ID = getArguments().getString(FAIR_ID);

            Log.e(TAG, "mFAIR_NAME: " + mFAIR_NAME);
            Log.e(TAG, "mFAIR_DATE: " + mFAIR_DATE);
            Log.e(TAG, "mFAIR_ID: " + mFAIR_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visitor_status_detail, container, false);


        horizontal_bar_chart = view.findViewById(R.id.horizontal_bar_chart);
        horizontal_bar_chart.setNoDataText("표시할 데이터가 없습니다.");
        visitor_status_fair_name = view.findViewById(R.id.visitor_status_fair_name);
        visitor_status_fair_date = view.findViewById(R.id.visitor_status_fair_date);
        mProgressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.progressBar_layout);

        visitor_status_fair_name.setText(mFAIR_NAME);
        visitor_status_fair_date.setText(mFAIR_DATE);

        mRecyclerView = view.findViewById(R.id.visitor_status_detail_recycler);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        refresh_layout = view.findViewById(R.id.refresh_layout);
        refresh_layout.setColorSchemeColors(
                getResources().getColor(R.color.color_DC6A7A)
        );
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getVisitorStatusDetail(true);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new VisitorStatusDetailFragment.RecycleAdapter(getContext(), mVisitorStatus_Items);
        mRecyclerView.setAdapter(mAdapter);

        getVisitorStatusDetail(false);

        return view;
    }

    protected void getVisitorStatusDetail(boolean type) {
        UserInfo userInfo = PrefKit.getUserInfo(getContext());
        HashMap<String, String> body = new HashMap<>();
        body.put("fair_id", mFAIR_ID);
        body.put("system_id", userInfo.SYS_ID);
        if (type) {
            new TelKit(getContext(), this).request(TelKit.URL_API_GET_VISITOR_STATUS_DETAIL, body);
        } else {
            new TelKit(getContext(), this, mLinearLayout).request(TelKit.URL_API_GET_VISITOR_STATUS_DETAIL, body);
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_VISITOR_STATUS_DETAIL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            mVisitorStatus_Items.clear();
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e("VisitorStatusDetail", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    VisitorStatusDetail VisitorStatusDetail = new VisitorStatusDetail();
                                    VisitorStatusDetail.ENTDATE = json_list.optString("ENTDATE");
                                    VisitorStatusDetail.DayTotal = json_list.optString("DayTotal").replace("null", "0");
                                    VisitorStatusDetail.H08 = json_list.optString("H08").replace("null", "0");
                                    VisitorStatusDetail.H09 = json_list.optString("H09").replace("null", "0");
                                    VisitorStatusDetail.H10 = json_list.optString("H10").replace("null", "0");
                                    VisitorStatusDetail.H11 = json_list.optString("H11").replace("null", "0");
                                    VisitorStatusDetail.H12 = json_list.optString("H12").replace("null", "0");
                                    VisitorStatusDetail.H13 = json_list.optString("H13").replace("null", "0");
                                    VisitorStatusDetail.H14 = json_list.optString("H14").replace("null", "0");
                                    VisitorStatusDetail.H15 = json_list.optString("H15").replace("null", "0");
                                    VisitorStatusDetail.H16 = json_list.optString("H16").replace("null", "0");
                                    VisitorStatusDetail.H17 = json_list.optString("H17").replace("null", "0");
                                    VisitorStatusDetail.H18 = json_list.optString("H18").replace("null", "0");
                                    VisitorStatusDetail.H19 = json_list.optString("H19").replace("null", "0");
                                    mVisitorStatus_Items.add(VisitorStatusDetail);
                                }
                            }
                            setHorizontalBarChart();
                        } else {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getContext(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        refresh_layout.setRefreshing(false);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<VisitorStatusDetailFragment.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<VisitorStatusDetail> mVisitorStatus_Items;

        public RecycleAdapter(Context context, List<VisitorStatusDetail> itemList) {
            this.mContext = context;
            this.mVisitorStatus_Items = itemList;
        }

        @Override
        public VisitorStatusDetailFragment.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.visitor_status_detail_item, parent, false);
            return new VisitorStatusDetailFragment.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(VisitorStatusDetailFragment.RecycleAdapter.ItemViewHolder holder, int position) {
            VisitorStatusDetail visitorstatusdetaillist = mVisitorStatus_Items.get(position);

            holder.visitor_status_detail_date.setText(visitorstatusdetaillist.ENTDATE);
            holder.visitor_status_detail_count.setText(convertCurrencyStr(Double.parseDouble(visitorstatusdetaillist.DayTotal)));

            if (position == 0) {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_F5C850));
            } else if (position == 1) {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_81A9E5));
            } else if (position == 2) {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_E66C6C));
            } else if (position == 3) {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_BDCF68));
            } else if (position == 4) {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_A9ACCC));
            } else {
                holder.img_view_color.setBackgroundColor(getResources().getColor(R.color.color_class_sales));
            }

            if (holder.visitor_status_detail_date.getText().equals("총계")) {
                holder.img_view_color.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            int count = mVisitorStatus_Items.size();
            layoutEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public VisitorStatusDetailFragment.RecycleAdapter mAdapter;
            public TextView visitor_status_detail_date;
            public TextView visitor_status_detail_count;
            public ImageView img_view_color;
            public LinearLayout visitor_status_detail_item_layout;

            public ItemViewHolder(View itemView, VisitorStatusDetailFragment.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.visitor_status_detail_date = itemView.findViewById(R.id.visitor_status_detail_date);
                this.visitor_status_detail_count = itemView.findViewById(R.id.visitor_status_detail_count);
                this.visitor_status_detail_item_layout = itemView.findViewById(R.id.visitor_status_detail_item_layout);
                this.img_view_color = itemView.findViewById(R.id.img_view_color);
            }
        }
    }

    private void setHorizontalBarChart() {
        Log.e(TAG, "setHorizontalBarChart mVisitorStatus_Items : " + mVisitorStatus_Items.size());
        if (mVisitorStatus_Items.size() != 0) {
            visitor_status_fair_date.setText(mVisitorStatus_Items.get(0).ENTDATE + " ~ " + mVisitorStatus_Items.get(mVisitorStatus_Items.size() - 2).ENTDATE);
        }
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < mVisitorStatus_Items.size() - 1; i++) {
            ArrayList<BarEntry> yValues = new ArrayList<>();
            yValues.clear();

            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H08)}, 11));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H09)}, 10));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H10)}, 9));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H11)}, 8));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H12)}, 7));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H13)}, 6));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H14)}, 5));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H15)}, 4));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H16)}, 3));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H17)}, 2));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H18)}, 1));
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mVisitorStatus_Items.get(i).H19)}, 0));

            BarDataSet barDataSet = new BarDataSet(yValues, mVisitorStatus_Items.get(i).ENTDATE);
            barDataSet.setBarSpacePercent(15f);

            if (i == 0) {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_F5C850)));
            } else if (i == 1) {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_81A9E5)));
            } else if (i == 2) {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_E66C6C)));
            } else if (i == 3) {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_BDCF68)));
            } else if (i == 4) {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_A9ACCC)));
            } else {
                barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_class_sales)));
            }
            barDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    DecimalFormat myFormatter = new DecimalFormat("###,###");
                    String changeValue = myFormatter.format((int) value);
                    return changeValue;
                }
            });
            dataSets.add(barDataSet);
        }
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("19시");
        labels.add("18시");
        labels.add("17시");
        labels.add("16시");
        labels.add("15시");
        labels.add("14시");
        labels.add("13시");
        labels.add("12시");
        labels.add("11시");
        labels.add("10시");
        labels.add("9시");
        labels.add("8시");

        BarData data = new BarData(labels, dataSets);
        data.setValueTextSize(11f);
        data.setGroupSpace(130f);
        horizontal_bar_chart.setDescription(mFAIR_NAME);
        horizontal_bar_chart.animateY(2000, Easing.EasingOption.Linear);
        horizontal_bar_chart.setDoubleTapToZoomEnabled(false);
        horizontal_bar_chart.setData(data);

        if (mVisitorStatus_Items.size() > 3) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3500));
        } else if (mVisitorStatus_Items.size() > 2) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3000));
        } else {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2500));
        }

        horizontal_bar_chart.getLegend().setWordWrapEnabled(true);
    }
}
package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.VisitorStatusDetail;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.*;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class VisitorStatusDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private String FAIR_ID = "";
    private String FAIR_DATE = "";
    private String FAIR_NAME = "";
    private TextView visitor_status_fair_name = null;
    private TextView visitor_status_fair_date = null;
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RecyclerView mRecyclerView = null;
    private VisitorStatusDetailActivity.RecycleAdapter mAdapter = null;
    private ArrayList<VisitorStatusDetail> mVisitorStatus_Items = new ArrayList<>();
    private LinearLayout layoutEmpty = null;
    private HorizontalBarChart horizontal_bar_chart = null;
    private SwipeRefreshLayout refresh_layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_D9CCB6);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_status_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_D9CCB6));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_visitor_status_detail);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        horizontal_bar_chart = findViewById(R.id.horizontal_bar_chart);
        horizontal_bar_chart.setNoDataText("표시할 데이터가 없습니다.");
        visitor_status_fair_name = findViewById(R.id.visitor_status_fair_name);
        visitor_status_fair_date = findViewById(R.id.visitor_status_fair_date);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            FAIR_DATE = intent.getExtras().getString("FAIR_DATE");
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
        }

        visitor_status_fair_name.setText(FAIR_NAME);
        visitor_status_fair_date.setText(FAIR_DATE);

        mRecyclerView = findViewById(R.id.visitor_status_detail_recycler);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        refresh_layout = findViewById(R.id.refresh_layout);
        refresh_layout.setColorSchemeColors(
                getResources().getColor(R.color.color_DC6A7A)
        );
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getVisitorStatusDetail(true);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new VisitorStatusDetailActivity.RecycleAdapter(this, mVisitorStatus_Items);
        mRecyclerView.setAdapter(mAdapter);

        getVisitorStatusDetail(false);

    }

    protected void getVisitorStatusDetail(boolean type) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("fair_id=%s&system_id=%s", FAIR_ID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("fair_id", FAIR_ID);
        body.put("system_id", userInfo.SYS_ID);
        if (type) {
            new TelKit(this, this).request(TelKit.URL_API_GET_VISITOR_STATUS_DETAIL, body);
        } else {
            new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_VISITOR_STATUS_DETAIL, body);
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
                                    //if(!json_list.optString("ENTDATE").equals("총계")) {
                                    mVisitorStatus_Items.add(VisitorStatusDetail);
                                    //}
                                }
                            }
                            setHorizontalBarChart();
                        } else {
                            Toast.makeText(VisitorStatusDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(VisitorStatusDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(VisitorStatusDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(VisitorStatusDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
        refresh_layout.setRefreshing(false);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<VisitorStatusDetailActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<VisitorStatusDetail> mVisitorStatus_Items;

        public RecycleAdapter(Context context, List<VisitorStatusDetail> itemList) {
            this.mContext = context;
            this.mVisitorStatus_Items = itemList;
        }

        @Override
        public VisitorStatusDetailActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.visitor_status_detail_item, parent, false);
            return new VisitorStatusDetailActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(VisitorStatusDetailActivity.RecycleAdapter.ItemViewHolder holder, int position) {
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
            public VisitorStatusDetailActivity.RecycleAdapter mAdapter;
            public TextView visitor_status_detail_date;
            public TextView visitor_status_detail_count;
            public ImageView img_view_color;
            public LinearLayout visitor_status_detail_item_layout;

            public ItemViewHolder(View itemView, VisitorStatusDetailActivity.RecycleAdapter mAdapter) {
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
        // create BarEntry for Bar Group 1
//        ArrayList<BarEntry> bargroup1 = new ArrayList<>();
//        bargroup1.add(new BarEntry(80, 0));
//        bargroup1.add(new BarEntry(20, 1));
//        bargroup1.add(new BarEntry(50, 2));
//        bargroup1.add(new BarEntry(200, 3));
//        bargroup1.add(new BarEntry(150, 4));
//        bargroup1.add(new BarEntry(190, 5));
//        bargroup1.add(new BarEntry(100, 6));
//        bargroup1.add(new BarEntry(50, 7));
//        bargroup1.add(new BarEntry(250, 8));
//        bargroup1.add(new BarEntry(40, 9));
//        bargroup1.add(new BarEntry(170, 10));
//
//        // create BarEntry for Bar Group 1
//        ArrayList<BarEntry> bargroup2 = new ArrayList<>();
//        bargroup2.add(new BarEntry(60, 0));
//        bargroup2.add(new BarEntry(100, 1));
//        bargroup2.add(new BarEntry(50, 2));
//        bargroup2.add(new BarEntry(250, 3));
//        bargroup2.add(new BarEntry(40, 4));
//        bargroup2.add(new BarEntry(170, 5));
//        bargroup2.add(new BarEntry(20, 6));
//        bargroup2.add(new BarEntry(50, 7));
//        bargroup2.add(new BarEntry(200, 8));
//        bargroup2.add(new BarEntry(150, 9));
//        bargroup2.add(new BarEntry(190, 10));

        // creating dataset for Bar Group1
//        BarDataSet barDataSet1 = new BarDataSet(bargroup1, "2019-03-05");

        //barDataSet1.setColor(Color.rgb(0, 155, 0));
//        barDataSet1.setColors(ColorTemplate.JOYFUL_COLORS);
        //barDataSet1.setColors(Collections.singletonList(Color.BLUE));

        // creating dataset for Bar Group 2
//        BarDataSet barDataSet2 = new BarDataSet(bargroup2, "2019-03-06");
//        barDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);
        //barDataSet2.setColors(Collections.singletonList(Color.RED));

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

//        ArrayList<BarDataSet> dataSets = new ArrayList<>();  // combined all dataset into an arraylist
//        dataSets.add(barDataSet1);
//        dataSets.add(barDataSet2);

        XAxis xAxis = horizontal_bar_chart.getXAxis();
        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // initialize the Bardata with argument labels and dataSet

//        horizontal_bar_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
//            @Override
//            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
//                MyMarkerView mv = new MyMarkerView(VisitorStatusDetailActivity.this, R.layout.custom_marker_view_layout , Float.toString(e.getVal()));
//                // set the marker to the chart
//                horizontal_bar_chart.setMarkerView(mv);
//            }
//
//            @Override
//            public void onNothingSelected() {
//
//            }
//        });
        BarData data = new BarData(labels, dataSets);
        data.setValueTextSize(11f);
        data.setGroupSpace(130f);
        horizontal_bar_chart.setDescription(FAIR_NAME);
        horizontal_bar_chart.animateY(2000, Easing.EasingOption.Linear);
        horizontal_bar_chart.setDoubleTapToZoomEnabled(false);
        horizontal_bar_chart.setData(data);

        if (mVisitorStatus_Items.size() > 3) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3000));
        } else if (mVisitorStatus_Items.size() > 2) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2500));
        }

        horizontal_bar_chart.getLegend().setWordWrapEnabled(true);
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
package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.PreregistrationDetail;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;
import static net.e_sang.fmsmobile.kit.Kit.getPreviousDate;

public class PreregistrationDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private String FAIR_ID = "";
    private String FAIR_MASTER_ID = "";
    private String FAIR_DATE = "";
    private String FAIR_NAME = "";
    private String FAIR_PLACE = "";
    private String FAIR_YEAR = "";
    private BarChart Stacked_Bar_Chart = null;
    private TextView preregistration_fair_name = null;
    private TextView preregistration_fair_date = null;
    private TextView preregistration_fair_base_date = null;
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RecyclerView mRecyclerView = null;
    private PreregistrationDetailActivity.RecycleAdapter mAdapter = null;
    private ArrayList<PreregistrationDetail> mPreregistration_Items = new ArrayList<>();
    private LinearLayout layoutEmpty = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_C9B5AF);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preregistration_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_C9B5AF));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_preregistration_detail);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        Stacked_Bar_Chart = findViewById(R.id.stacked_bar_chart);
        Stacked_Bar_Chart.setNoDataText("표시할 데이터가 없습니다.");
        preregistration_fair_name = findViewById(R.id.preregistration_fair_name);
        preregistration_fair_date = findViewById(R.id.preregistration_fair_date);
        preregistration_fair_base_date = findViewById(R.id.preregistration_fair_base_date);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        preregistration_fair_base_date.setText(getPreviousDate());

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            FAIR_DATE = intent.getExtras().getString("FAIR_DATE");
            FAIR_ID = intent.getExtras().getString("FAIR_ID");
            FAIR_MASTER_ID = intent.getExtras().getString("FAIR_MASTER_ID");
            FAIR_PLACE = intent.getExtras().getString("FAIR_PLACE");
            FAIR_YEAR = intent.getExtras().getString("FAIR_YEAR");
        }

        preregistration_fair_name.setText(FAIR_NAME);
        preregistration_fair_date.setText(FAIR_DATE);

        mRecyclerView = findViewById(R.id.preregistration_detail_recycler);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PreregistrationDetailActivity.RecycleAdapter(this, mPreregistration_Items);
        mRecyclerView.setAdapter(mAdapter);

        getPreregistrationDetail();
    }

    protected void getPreregistrationDetail() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("fair_master_id=%s&fair_place=%s&system_id=%s", FAIR_MASTER_ID, FAIR_PLACE, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("fair_master_id", FAIR_MASTER_ID);
        body.put("fair_place", FAIR_PLACE);
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_PREREGISTRATION_DETAIL, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_PREREGISTRATION_DETAIL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e("PreregistrationDetail", "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    PreregistrationDetail preregistrationDetail = new PreregistrationDetail();

                                    preregistrationDetail.FAIR_ID = json_list.optString("FAIR_ID");
                                    preregistrationDetail.FAIR_YEAR = json_list.optString("FAIR_YEAR");
                                    preregistrationDetail.FAIR_NAME = json_list.optString("FAIR_NAME");
                                    preregistrationDetail.PRE_VISITOR_TOT_CNT = json_list.optString("PRE_VISITOR_TOT_CNT").replaceAll(" ", "");
                                    preregistrationDetail.NEW_PRE_VISITOR_TOT_CNT = json_list.optString("NEW_PRE_VISITOR_TOT_CNT").replaceAll(" ", "");
                                    preregistrationDetail.FAIR_SEQ = json_list.optString("FAIR_SEQ");
                                    preregistrationDetail.FAIR_PLACE_NAME = json_list.optString("FAIR_PLACE_NAME");
                                    int iTotal_cnt = Integer.parseInt(json_list.optString("NEW_PRE_VISITOR_TOT_CNT").replaceAll(" ", ""));
                                    int iNew_cnt = Integer.parseInt(json_list.optString("PRE_VISITOR_TOT_CNT").replaceAll(" ", ""));
                                    String re_count = String.valueOf(iTotal_cnt - iNew_cnt);
                                    preregistrationDetail.PRE_VISITOR_RE_CNT = re_count;
                                    preregistrationDetail.NEW_RATE = Kit.getNewRate(json_list.optString("PRE_VISITOR_TOT_CNT").replace("null", "0"), json_list.optString("NEW_PRE_VISITOR_TOT_CNT").replace("null", "0"));
                                    if (Integer.parseInt(json_list.optString("NEW_PRE_VISITOR_TOT_CNT").replace("null", "0")) > 0) {
                                        mPreregistration_Items.add(preregistrationDetail);
                                    }
                                }
                            }
                            setStackedBarChart();
                        } else {
                            //Toast.makeText(PreregistrationDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PreregistrationDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PreregistrationDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PreregistrationDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<PreregistrationDetailActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<PreregistrationDetail> mPreregistration_Items;

        public RecycleAdapter(Context context, List<PreregistrationDetail> itemList) {
            this.mContext = context;
            this.mPreregistration_Items = itemList;
        }

        @Override
        public PreregistrationDetailActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.preregistration_detail_item, parent, false);
            return new PreregistrationDetailActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(PreregistrationDetailActivity.RecycleAdapter.ItemViewHolder holder, int position) {
            PreregistrationDetail preregistrationdetaillist = mPreregistration_Items.get(position);

            holder.preregistration_detail_year.setText(preregistrationdetaillist.FAIR_YEAR);
            holder.preregistration_detail_cnt.setText(preregistrationdetaillist.FAIR_SEQ);
            holder.preregistration_detail_re_register.setText(convertCurrencyStr(Double.parseDouble(preregistrationdetaillist.PRE_VISITOR_RE_CNT)));
            holder.preregistration_detail_new_registration.setText(convertCurrencyStr(Double.parseDouble(preregistrationdetaillist.NEW_PRE_VISITOR_TOT_CNT)));
            holder.preregistration_detail_the_entire.setText(convertCurrencyStr(Double.parseDouble(preregistrationdetaillist.PRE_VISITOR_TOT_CNT)));
            holder.preregistration_detail_new_rate.setText(preregistrationdetaillist.NEW_RATE + "%");

            Calendar calendar = new GregorianCalendar(Locale.KOREA);
            if (FAIR_YEAR.equals(preregistrationdetaillist.FAIR_YEAR) && FAIR_ID.equals(preregistrationdetaillist.FAIR_ID)) {
                holder.preregistration_detail_item_layout.setBackgroundColor(getResources().getColor(R.color.color_FAFFD5));
            } else {
                holder.preregistration_detail_item_layout.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            int count = mPreregistration_Items.size();
            layoutEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public PreregistrationDetailActivity.RecycleAdapter mAdapter;
            public TextView preregistration_detail_year;
            public TextView preregistration_detail_cnt;
            public TextView preregistration_detail_re_register;
            public TextView preregistration_detail_new_registration;
            public TextView preregistration_detail_the_entire;
            public TextView preregistration_detail_new_rate;
            public LinearLayout preregistration_detail_item_layout;

            public ItemViewHolder(View itemView, PreregistrationDetailActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.preregistration_detail_year = itemView.findViewById(R.id.preregistration_detail_year);
                this.preregistration_detail_cnt = itemView.findViewById(R.id.preregistration_detail_cnt);
                this.preregistration_detail_re_register = itemView.findViewById(R.id.preregistration_detail_re_register);
                this.preregistration_detail_new_registration = itemView.findViewById(R.id.preregistration_detail_new_registration);
                this.preregistration_detail_the_entire = itemView.findViewById(R.id.preregistration_detail_the_entire);
                this.preregistration_detail_new_rate = itemView.findViewById(R.id.preregistration_detail_new_rate);
                this.preregistration_detail_item_layout = itemView.findViewById(R.id.preregistration_detail_item_layout);
            }
        }
    }

    private void setStackedBarChart() {
        Log.e("PreregistrationDetail", "setStackedBarChart mPreregistration_Items : " + mPreregistration_Items.size());
        ArrayList yValues = new ArrayList();
        for (int i = 0; i < mPreregistration_Items.size(); i++) {
            yValues.add(new BarEntry(new float[]{Integer.parseInt(mPreregistration_Items.get(i).PRE_VISITOR_TOT_CNT), Integer.parseInt(mPreregistration_Items.get(i).PRE_VISITOR_RE_CNT)}, i));
        }

        BarDataSet barDataSet3 = new BarDataSet(yValues, "연별 사전등록 현황");
        barDataSet3.setColors(new int[]{ContextCompat.getColor(this, R.color.color_4472C4),
                ContextCompat.getColor(this, R.color.color_ED7D31)});
        barDataSet3.setStackLabels(new String[]{"신규등록", "재등록"});
        barDataSet3.setValueTextColor(Color.WHITE);
        barDataSet3.setValueTextSize(10f);
        barDataSet3.setDrawValues(true);
        barDataSet3.setBarSpacePercent(25f);
        barDataSet3.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                String sValue = "";
                if (value == 0.0) {
                    sValue = "";
                } else {
                    sValue = convertCurrencyStr(Double.parseDouble(Float.toString(value)));
                }
                return sValue;
            }
        });

        Log.e(TAG, "getYValueSum" + barDataSet3.getYValueSum());

        ArrayList<String> labels = new ArrayList<String>();
        for (int i = 0; i < mPreregistration_Items.size(); i++) {
            labels.add(mPreregistration_Items.get(i).FAIR_YEAR + "년 " + mPreregistration_Items.get(i).FAIR_SEQ + "회차");
            //labels.add(mPreregistration_Items.get(i).FAIR_YEAR + "년");
        }

        ArrayList<BarDataSet> dataSets = new ArrayList<>();  // combined all dataset into an arraylist
        dataSets.add(barDataSet3);
        XAxis xAxis = Stacked_Bar_Chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        Stacked_Bar_Chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                MyMarkerView mv = new MyMarkerView(PreregistrationDetailActivity.this, R.layout.custom_marker_view_layout, Stacked_Bar_Chart.getXValue(e.getXIndex()));
                // set the marker to the chart
                Stacked_Bar_Chart.setMarkerView(mv);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // initialize the Bardata with argument labels and dataSet
        BarData data = new BarData(labels, dataSets);
        data.setValueTextSize(10f);
        Stacked_Bar_Chart.setDescription(FAIR_NAME);
        Stacked_Bar_Chart.animateY(2000, Easing.EasingOption.Linear);
        Stacked_Bar_Chart.setDrawValueAboveBar(false);
        Stacked_Bar_Chart.setDoubleTapToZoomEnabled(false);
        Stacked_Bar_Chart.setData(data);
        Stacked_Bar_Chart.setVisibleXRangeMaximum(6);

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

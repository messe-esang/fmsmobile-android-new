package net.e_sang.fmsmobile.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.AnnualVisitorStatisticsDetail;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;
import static net.e_sang.fmsmobile.kit.Kit.getPreviousDate;

public class AnnualVisitorStatisticsDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private String FAIR_PLACE = "";
    private String FAIR_MASTER_ID = "";
    private String FAIR_PLACE_ID = "";
    private String FAIR_NAME = "";
    private TextView annual_visitor_statistics_fair_name = null;
    private TextView annual_visitor_statistics_fair_base_date = null;
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private RecyclerView mRecyclerView = null;
    private AnnualVisitorStatisticsDetailActivity.RecycleAdapter mAdapter = null;
    private ArrayList<AnnualVisitorStatisticsDetail> mAnnualVisitorStatistics_Items = new ArrayList<>();
    private LinearLayout layoutEmpty = null;
    private LineChart line_chart = null;
    private ImageButton more_btn = null;
    private boolean Show_Hide_Check = false;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;
    private int Fair_Day_Count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_FFF4D5);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_visitor_statistics_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_FFF4D5));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_annual_visitor_statistics_detail);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        line_chart = findViewById(R.id.line_chart);
        line_chart.setNoDataText("표시할 데이터가 없습니다.");
        annual_visitor_statistics_fair_name = findViewById(R.id.annual_visitor_statistics_fair_name);
        annual_visitor_statistics_fair_base_date = findViewById(R.id.annual_visitor_statistics_fair_base_date);
        mProgressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.progressBar_layout);
        annual_visitor_statistics_fair_base_date.setText(getPreviousDate());

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            FAIR_NAME = intent.getExtras().getString("FAIR_NAME");
            FAIR_MASTER_ID = intent.getExtras().getString("FAIR_MASTER_ID");
            FAIR_PLACE = intent.getExtras().getString("FAIR_PLACE");
            FAIR_PLACE_ID = intent.getExtras().getString("FAIR_PLACE_ID");
        }
        annual_visitor_statistics_fair_name.setText(FAIR_NAME);

        mRecyclerView = findViewById(R.id.annual_visitor_statistics_detail_recycler);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new AnnualVisitorStatisticsDetailActivity.RecycleAdapter(this, mAnnualVisitorStatistics_Items);
        mRecyclerView.setAdapter(mAdapter);

        getAnnualVisitorStatisticsDetail();

        more_btn = findViewById(R.id.more_btn);
        more_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Show_Hide_Check == false) {
                    Show_Hide_Check = true;
                    more_btn.setImageResource(R.drawable.ic_less);
                } else {
                    Show_Hide_Check = false;
                    more_btn.setImageResource(R.drawable.ic_more);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    protected void getAnnualVisitorStatisticsDetail() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("FAIR_MASTER_ID=%s&FAIR_PLACE=%s&system_id=%s", FAIR_MASTER_ID, FAIR_PLACE_ID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("FAIR_MASTER_ID", FAIR_MASTER_ID);
        body.put("FAIR_PLACE", FAIR_PLACE_ID);
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_DETAIL, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_ANNUA_VISITOR_STATISTICES_DETAIL)) {
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
                                Log.e(TAG, "JSONObject json_list: " + json_list);
                                if (json_list != null) {
                                    AnnualVisitorStatisticsDetail annualvisitorstatisticsdetail = new AnnualVisitorStatisticsDetail();
                                    annualvisitorstatisticsdetail.FAIR_YEAR = json_list.optString("FAIR_YEAR");
                                    annualvisitorstatisticsdetail.FAIR_SEQ = json_list.optString("FAIR_SEQ");
                                    annualvisitorstatisticsdetail.ENTRANCE_TOT_CNT = json_list.optString("ENTRANCE_TOT_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_BEF_CNT = json_list.optString("ENTRANCE_BEF_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_1_CNT = json_list.optString("ENTRANCE_1_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_2_CNT = json_list.optString("ENTRANCE_2_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_3_CNT = json_list.optString("ENTRANCE_3_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_4_CNT = json_list.optString("ENTRANCE_4_CNT").replace("null", "0");
                                    annualvisitorstatisticsdetail.ENTRANCE_5_CNT = json_list.optString("ENTRANCE_5_CNT").replace("null", "0");
                                    if (Integer.parseInt(json_list.optString("ENTRANCE_TOT_CNT").replace("null", "0")) > 0) {
                                        mAnnualVisitorStatistics_Items.add(annualvisitorstatisticsdetail);
                                    }
                                }
                            }

                            setLineChart();
                        } else {
                            //Toast.makeText(VisitorStatusDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AnnualVisitorStatisticsDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AnnualVisitorStatisticsDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AnnualVisitorStatisticsDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<AnnualVisitorStatisticsDetailActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<AnnualVisitorStatisticsDetail> mAnnualVisitorStatistics_Items;

        public RecycleAdapter(Context context, List<AnnualVisitorStatisticsDetail> itemList) {
            this.mContext = context;
            this.mAnnualVisitorStatistics_Items = itemList;
        }

        @Override
        public AnnualVisitorStatisticsDetailActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.annual_visitor_statistics_detail_item, parent, false);
            return new AnnualVisitorStatisticsDetailActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(AnnualVisitorStatisticsDetailActivity.RecycleAdapter.ItemViewHolder holder, final int position) {
            final AnnualVisitorStatisticsDetail annualvisitorstatisticsdetaillist = mAnnualVisitorStatistics_Items.get(position);
            holder.annual_visitor_statistics_detail_date.setText(annualvisitorstatisticsdetaillist.FAIR_YEAR);
            holder.annual_visitor_statistics_detail_count.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_TOT_CNT)));
            holder.annual_visitor_statistics_detail_times.setText(annualvisitorstatisticsdetaillist.FAIR_SEQ);

            Calendar calendar = new GregorianCalendar(Locale.KOREA);
            //if (String.valueOf(calendar.get(Calendar.YEAR)).equals(annualvisitorstatisticsdetaillist.FAIR_YEAR)) {
            if (holder.getBindingAdapterPosition() == 0) {
                holder.annual_visitor_statistics_detail_item_layout.setBackgroundColor(getResources().getColor(R.color.color_FAFFD5));
            } else {
                holder.annual_visitor_statistics_detail_item_layout.setBackgroundColor(Color.TRANSPARENT);
            }

            if (holder.getBindingAdapterPosition() > 0 && Show_Hide_Check == false) {
                holder.annual_visitor_statistics_detail_item_layout.setVisibility(View.GONE);
            } else {
                holder.annual_visitor_statistics_detail_item_layout.setVisibility(View.VISIBLE);
            }

            holder.annual_visitor_statistics_detail_item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialog(R.layout.count_dialog_layout, holder.getBindingAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            int count = mAnnualVisitorStatistics_Items.size();
            layoutEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            more_btn.setVisibility(count <= 1 ? View.INVISIBLE : View.VISIBLE);
            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public AnnualVisitorStatisticsDetailActivity.RecycleAdapter mAdapter;
            public TextView annual_visitor_statistics_detail_date;
            public TextView annual_visitor_statistics_detail_count;
            public TextView annual_visitor_statistics_detail_times;
            public LinearLayout annual_visitor_statistics_detail_item_layout;

            public ItemViewHolder(View itemView, AnnualVisitorStatisticsDetailActivity.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.annual_visitor_statistics_detail_date = itemView.findViewById(R.id.annual_visitor_statistics_detail_year);
                this.annual_visitor_statistics_detail_count = itemView.findViewById(R.id.annual_visitor_statistics_detail_count);
                this.annual_visitor_statistics_detail_times = itemView.findViewById(R.id.annual_visitor_statistics_detail_times);
                this.annual_visitor_statistics_detail_item_layout = itemView.findViewById(R.id.annual_visitor_statistics_detail_item_layout);
            }
        }
    }

    private void setLineChart() {
        Log.e(TAG, "setLineChart mAnnualVisitorStatistics_Items : " + mAnnualVisitorStatistics_Items.size());

        int Day_3 = 0;
        int Day_4 = 0;
        int Day_5 = 0;

        for (int i = 0; i < mAnnualVisitorStatistics_Items.size(); i++) {
            Day_3 += Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_3_CNT);
            Day_4 += Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_4_CNT);
            Day_5 += Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_5_CNT);
        }

        ArrayList<String> xValsDay = new ArrayList<String>(); //일
        LineData data = null;
        if (Day_5 > 0) {
            xValsDay.add("행사 전");
            xValsDay.add("1일차");
            xValsDay.add("2일차");
            xValsDay.add("3일차");
            xValsDay.add("4일차");
            xValsDay.add("5일차");
            ArrayList<LineDataSet> data5Sets = new ArrayList<>();
            for (int k = 0; k < mAnnualVisitorStatistics_Items.size(); k++) {
                ArrayList<Entry> yValues5 = new ArrayList<>();
                yValues5.clear();

                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_BEF_CNT), 0));
                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_1_CNT), 1));
                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_2_CNT), 2));
                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_3_CNT), 3));
                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_4_CNT), 4));
                yValues5.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(k).ENTRANCE_5_CNT), 5));

                LineDataSet linedata5set = new LineDataSet(yValues5, mAnnualVisitorStatistics_Items.get(k).FAIR_YEAR + " " + mAnnualVisitorStatistics_Items.get(k).FAIR_SEQ + "회차");
                linedata5set.setFillColor(Color.WHITE);
                linedata5set.setLineWidth(3f);
                linedata5set.setCircleSize(2f);
                linedata5set.setDrawCircleHole(true);
                linedata5set.setValueTextSize(10f);
                linedata5set.setDrawFilled(false);
                if (k == 0) {
                    linedata5set.setColor(getResources().getColor(R.color.color_67A83C));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_67A83C));
                } else if (k == 1) {
                    linedata5set.setColor(getResources().getColor(R.color.color_FFBB00));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_FFBB00));
                } else if (k == 2) {
                    linedata5set.setColor(getResources().getColor(R.color.color_4E94D2));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_4E94D2));
                } else if (k == 3) {
                    linedata5set.setColor(getResources().getColor(R.color.color_EC7626));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_EC7626));
                } else if (k == 4) {
                    linedata5set.setColor(getResources().getColor(R.color.color_E66C6C));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_E66C6C));
                } else if (k == 5) {
                    linedata5set.setColor(getResources().getColor(R.color.color_A9ACCC));
                    linedata5set.setCircleColor(getResources().getColor(R.color.color_A9ACCC));
                } else {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    linedata5set.setColor(color);
                    linedata5set.setCircleColor(color);
                }
                data5Sets.add(linedata5set);
            }
            data = new LineData(xValsDay, data5Sets);
            Fair_Day_Count = 5;
        } else if (Day_4 > 0) {
            xValsDay.add("행사 전");
            xValsDay.add("1일차");
            xValsDay.add("2일차");
            xValsDay.add("3일차");
            xValsDay.add("4일차");
            ArrayList<LineDataSet> data4Sets = new ArrayList<>();
            for (int j = 0; j < mAnnualVisitorStatistics_Items.size(); j++) {
                ArrayList<Entry> yValues4 = new ArrayList<>();
                yValues4.clear();

                yValues4.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(j).ENTRANCE_BEF_CNT), 0));
                yValues4.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(j).ENTRANCE_1_CNT), 1));
                yValues4.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(j).ENTRANCE_2_CNT), 2));
                yValues4.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(j).ENTRANCE_3_CNT), 3));
                yValues4.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(j).ENTRANCE_4_CNT), 4));

                LineDataSet linedata4set = new LineDataSet(yValues4, mAnnualVisitorStatistics_Items.get(j).FAIR_YEAR + " " + mAnnualVisitorStatistics_Items.get(j).FAIR_SEQ + "회차");
                linedata4set.setFillColor(Color.WHITE);
                linedata4set.setLineWidth(3f);
                linedata4set.setCircleSize(2f);
                linedata4set.setDrawCircleHole(true);
                linedata4set.setValueTextSize(10f);
                linedata4set.setDrawFilled(false);
                if (j == 0) {
                    linedata4set.setColor(getResources().getColor(R.color.color_67A83C));
                    linedata4set.setCircleColor(getResources().getColor(R.color.color_67A83C));
                } else if (j == 1) {
                    linedata4set.setColor(getResources().getColor(R.color.color_FFBB00));
                    linedata4set.setCircleColor(getResources().getColor(R.color.color_FFBB00));
                } else if (j == 2) {
                    linedata4set.setColor(getResources().getColor(R.color.color_4E94D2));
                    linedata4set.setCircleColor(getResources().getColor(R.color.color_4E94D2));
                } else if (j == 3) {
                    linedata4set.setColor(getResources().getColor(R.color.color_EC7626));
                    linedata4set.setCircleColor(getResources().getColor(R.color.color_EC7626));
                } else if (j == 4) {
                    linedata4set.setColor(getResources().getColor(R.color.color_E66C6C));
                    linedata4set.setCircleColor(getResources().getColor(R.color.color_E66C6C));
                } else {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    linedata4set.setColor(color);
                    linedata4set.setCircleColor(color);
                }
                data4Sets.add(linedata4set);
            }
            data = new LineData(xValsDay, data4Sets);
            Fair_Day_Count = 4;
        } else if (Day_3 > 0) {
            xValsDay.add("행사 전");
            xValsDay.add("1일차");
            xValsDay.add("2일차");
            xValsDay.add("3일차");
            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            for (int i = 0; i < mAnnualVisitorStatistics_Items.size(); i++) {
                ArrayList<Entry> yValues = new ArrayList<>();
                yValues.clear();

                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_BEF_CNT), 0));
                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_1_CNT), 1));
                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_2_CNT), 2));
                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_3_CNT), 3));

                LineDataSet linedataset = new LineDataSet(yValues, mAnnualVisitorStatistics_Items.get(i).FAIR_YEAR + " " + mAnnualVisitorStatistics_Items.get(i).FAIR_SEQ + "회차");
                linedataset.setFillColor(Color.WHITE);
                linedataset.setLineWidth(3f);
                linedataset.setCircleSize(2f);
                linedataset.setDrawCircleHole(true);
                linedataset.setValueTextSize(10f);
                linedataset.setDrawFilled(false);
                if (i == 0) {
                    linedataset.setColor(getResources().getColor(R.color.color_67A83C));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_67A83C));
                } else if (i == 1) {
                    linedataset.setColor(getResources().getColor(R.color.color_FFBB00));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_FFBB00));
                } else if (i == 2) {
                    linedataset.setColor(getResources().getColor(R.color.color_4E94D2));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_4E94D2));
                } else if (i == 3) {
                    linedataset.setColor(getResources().getColor(R.color.color_EC7626));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_EC7626));
                } else {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    linedataset.setColor(color);
                    linedataset.setCircleColor(color);
                }
                dataSets.add(linedataset);
            }
            data = new LineData(xValsDay, dataSets);
            Fair_Day_Count = 3;
        } else {
            xValsDay.add("행사 전");
            xValsDay.add("1일차");
            xValsDay.add("2일차");
            ArrayList<LineDataSet> dataSets = new ArrayList<>();
            for (int i = 0; i < mAnnualVisitorStatistics_Items.size(); i++) {
                ArrayList<Entry> yValues = new ArrayList<>();
                yValues.clear();

                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_BEF_CNT), 0));
                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_1_CNT), 1));
                yValues.add(new Entry(Integer.parseInt(mAnnualVisitorStatistics_Items.get(i).ENTRANCE_2_CNT), 2));

                LineDataSet linedataset = new LineDataSet(yValues, mAnnualVisitorStatistics_Items.get(i).FAIR_YEAR + " " + mAnnualVisitorStatistics_Items.get(i).FAIR_SEQ + "회차");
                linedataset.setFillColor(Color.WHITE);
                linedataset.setLineWidth(3f);
                linedataset.setCircleSize(2f);
                linedataset.setDrawCircleHole(true);
                linedataset.setValueTextSize(10f);
                linedataset.setDrawFilled(false);
                if (i == 0) {
                    linedataset.setColor(getResources().getColor(R.color.color_67A83C));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_67A83C));
                } else if (i == 1) {
                    linedataset.setColor(getResources().getColor(R.color.color_FFBB00));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_FFBB00));
                } else if (i == 2) {
                    linedataset.setColor(getResources().getColor(R.color.color_4E94D2));
                    linedataset.setCircleColor(getResources().getColor(R.color.color_4E94D2));
                } else {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    linedataset.setColor(color);
                    linedataset.setCircleColor(color);
                }
                dataSets.add(linedataset);
            }
            data = new LineData(xValsDay, dataSets);
            Fair_Day_Count = 2;
        }

//        ArrayList<String> xVals = new ArrayList<String>();
//        xVals.add("2015년");
//        xVals.add("2016년");
//        xVals.add("2017년");
//        xVals.add("2018년");
//        xVals.add("2019년");
//
//        ArrayList<Entry> yValsAll = new ArrayList<Entry>();
//        yValsAll.add(new Entry(1008f, 0));
//        yValsAll.add(new Entry(1202f, 1));
//        yValsAll.add(new Entry(1305f, 2));
//        yValsAll.add(new Entry(1420f, 3));
//        yValsAll.add(new Entry(1515f, 4));
//
//        ArrayList<Entry> yVals = new ArrayList<Entry>();
//        yVals.add(new Entry(108f, 0));
//        yVals.add(new Entry(202f, 1));
//        yVals.add(new Entry(305f, 2));
//        yVals.add(new Entry(120f, 3));
//        yVals.add(new Entry(215f, 4));
//
//        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
//        yVals1.add(new Entry(116f, 0));
//        yVals1.add(new Entry(230f, 1));
//        yVals1.add(new Entry(345f, 2));
//        yVals1.add(new Entry(225f, 3));
//        yVals1.add(new Entry(404f, 4));
//
//        LineDataSet setAll;
//        LineDataSet set1;
//        LineDataSet set2;
//
//        setAll = new LineDataSet(yValsAll, "전체");
//        set1 = new LineDataSet(yVals, "1일");
//        set2 = new LineDataSet(yVals1, "2일");
//
//        setAll.setFillColor(Color.WHITE);
//        set1.setFillColor(Color.WHITE);
//        set2.setFillColor(Color.WHITE);
//
//        setAll.setColor(getResources().getColor(R.color.color_67A83C));
//        setAll.setCircleColor(getResources().getColor(R.color.color_67A83C));
//        setAll.setLineWidth(3f);
//        setAll.setCircleSize(2f);
//        setAll.setDrawCircleHole(false);
//        setAll.setValueTextSize(10f);
//        setAll.setDrawFilled(true);
//
//        set1.setColor(getResources().getColor(R.color.color_FFBB00));
//        set1.setCircleColor(getResources().getColor(R.color.color_FFBB00));
//        set1.setLineWidth(3f);
//        set1.setCircleSize(2f);
//        set1.setDrawCircleHole(false);
//        set1.setValueTextSize(10f);
//        set1.setDrawFilled(true);
//
//        set2.setColor(getResources().getColor(R.color.color_4E94D2));
//        set2.setCircleColor(getResources().getColor(R.color.color_4E94D2));
//        set2.setLineWidth(3f);
//        set2.setCircleSize(2f);
//        set2.setDrawCircleHole(false);
//        set2.setValueTextSize(10f);
//        set2.setDrawFilled(true);
//
//        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
//        dataSets.add(setAll); // add the datasets
//        dataSets.add(set1); // add the datasets
//        dataSets.add(set2); // add the datasets

        // create a data object with the datasets
//        LineData data = new LineData(xValsDay, dataSets);

        XAxis xAxis = line_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = line_chart.getAxisRight();
        //yAxis.setEnabled(false);

        line_chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                MyMarkerView mv = new MyMarkerView(AnnualVisitorStatisticsDetailActivity.this, R.layout.custom_marker_view_layout, Float.toString(e.getVal()));
                // set the marker to the chart
                line_chart.setMarkerView(mv);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // set data
        line_chart.animateY(2000, Easing.EasingOption.Linear);
        line_chart.setDescription(FAIR_NAME);
        line_chart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        line_chart.getLegend().setWordWrapEnabled(true);
        line_chart.getLegend().setXOffset(-28);
        line_chart.setDoubleTapToZoomEnabled(false);
        line_chart.setData(data);

//        Legend l = line_chart.getLegend();
//        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
//        l.setForm(Legend.LegendForm.CIRCLE);
//        l.setDirection(Legend.LegendDirection.RIGHT_TO_LEFT);
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

    private void showAlertDialog(int layout, int position) {
        final AnnualVisitorStatisticsDetail annualvisitorstatisticsdetaillist = mAnnualVisitorStatistics_Items.get(position);

        dialogBuilder = new AlertDialog.Builder(this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        Button btnOK = layoutView.findViewById(R.id.btnOK);
        TextView fair_year = layoutView.findViewById(R.id.fair_year);
        TextView fair_seq = layoutView.findViewById(R.id.fair_seq);
        TextView fair_count_0 = layoutView.findViewById(R.id.fair_count_0);
        TextView fair_count_1 = layoutView.findViewById(R.id.fair_count_1);
        TextView fair_count_2 = layoutView.findViewById(R.id.fair_count_2);
        TextView fair_count_3 = layoutView.findViewById(R.id.fair_count_3);
        TextView fair_count_4 = layoutView.findViewById(R.id.fair_count_4);
        TextView fair_count_5 = layoutView.findViewById(R.id.fair_count_5);
        TextView fair_count_total = layoutView.findViewById(R.id.fair_count_total);
        LinearLayout fair_count_3_layout = layoutView.findViewById(R.id.fair_count_3_layout);
        LinearLayout fair_count_4_layout = layoutView.findViewById(R.id.fair_count_4_layout);
        LinearLayout fair_count_5_layout = layoutView.findViewById(R.id.fair_count_5_layout);
        fair_year.setText(annualvisitorstatisticsdetaillist.FAIR_YEAR + " 년도");
        fair_seq.setText(annualvisitorstatisticsdetaillist.FAIR_SEQ + " 회차");
        fair_count_0.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_BEF_CNT)));
        fair_count_1.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_1_CNT)));
        fair_count_2.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_2_CNT)));
        fair_count_total.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_TOT_CNT)));
        if (Fair_Day_Count >= 3) {
            fair_count_3.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_3_CNT)));
            fair_count_3_layout.setVisibility(View.VISIBLE);
        }
        if (Fair_Day_Count >= 4) {
            fair_count_4.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_4_CNT)));
            fair_count_4_layout.setVisibility(View.VISIBLE);
        }
        if (Fair_Day_Count >= 5) {
            fair_count_5.setText(convertCurrencyStr(Double.parseDouble(annualvisitorstatisticsdetaillist.ENTRANCE_5_CNT)));
            fair_count_5_layout.setVisibility(View.VISIBLE);
        }

        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        //alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
}

package net.e_sang.fmsmobile.ui;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import net.e_sang.fmsmobile.data.SamePlaceFairs;
import net.e_sang.fmsmobile.data.SamePlaceFairsCount;
import net.e_sang.fmsmobile.data.SamePlaceFairsTime;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitorStatusCompareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitorStatusCompareFragment extends Fragment implements TelKit.OnResultListener {

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
    private LinearLayout mLinearLayout, date_name_layout_1, date_name_layout_2, date_name_layout_3 = null;
    private RecyclerView mRecyclerView = null;
    private VisitorStatusCompareFragment.RecycleAdapter mAdapter = null;
    private ArrayList<SamePlaceFairsCount> mVisitorStatus_Items = new ArrayList<>();
    private ArrayList<SamePlaceFairsTime> mSamePlaceFairsTime_Items = new ArrayList<>();
    private LinearLayout layoutEmpty = null;
    private HorizontalBarChart horizontal_bar_chart = null;
    private SwipeRefreshLayout refresh_layout = null;
    private ArrayList<SamePlaceFairs> mSamePlaceFairsItems = new ArrayList<>();
    private Map<Integer, String> position_fair_id = new HashMap<>();
    private Spinner spinner;
    private int Count = 0;
    private Button compare_btn;
    private FairAdapter fairAdapter = null;
    private ImageView date_color_1, date_color_2, date_color_3 = null;
    private TextView date_name_1, date_name_2, date_name_3 = null;
    private boolean Start_type = true;
    private String mDay_select = "1";

    private TextView title_txt;
    private CheckBox chk_same_place_yn;
    String mSame_Place_YN = "Y";

    public VisitorStatusCompareFragment() {
        // Required empty public constructor
    }

    public static VisitorStatusCompareFragment newInstance(String mFAIR_NAME, String mFAIR_DATE, String mFAIR_ID) {
        VisitorStatusCompareFragment fragment = new VisitorStatusCompareFragment();
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
        View view = inflater.inflate(R.layout.fragment_visitor_status_compare, container, false);

        date_name_layout_1 = view.findViewById(R.id.date_name_layout_1);
        date_name_layout_2 = view.findViewById(R.id.date_name_layout_2);
        date_name_layout_3 = view.findViewById(R.id.date_name_layout_3);
        date_color_1 = view.findViewById(R.id.date_color_1);
        date_color_2 = view.findViewById(R.id.date_color_2);
        date_color_3 = view.findViewById(R.id.date_color_3);
        date_name_1 = view.findViewById(R.id.date_name_1);
        date_name_2 = view.findViewById(R.id.date_name_2);
        date_name_3 = view.findViewById(R.id.date_name_3);
        title_txt = view.findViewById(R.id.title_txt);
        chk_same_place_yn = view.findViewById(R.id.chk_same_place_yn);
        date_color_1.setBackgroundColor(getResources().getColor(R.color.color_E66C6C));
        date_color_2.setBackgroundColor(getResources().getColor(R.color.color_81A9E5));
        date_color_3.setBackgroundColor(getResources().getColor(R.color.color_F5C850));
        horizontal_bar_chart = view.findViewById(R.id.horizontal_bar_chart);
        horizontal_bar_chart.setNoDataText("표시할 데이터가 없습니다.");
        visitor_status_fair_name = view.findViewById(R.id.visitor_status_fair_name);
        visitor_status_fair_date = view.findViewById(R.id.visitor_status_fair_date);
        mProgressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.progressBar_layout);

        spinner = view.findViewById(R.id.spinner);
        compare_btn = view.findViewById(R.id.compare_btn);

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
                Log.e(TAG, "onRefresh mDay_select : " + mDay_select);
                getInquiryVisitorsStats();
                //getInquiryVisitorsStatsByHours(mDay_select);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new VisitorStatusCompareFragment.RecycleAdapter(getActivity(), mVisitorStatus_Items);
        mRecyclerView.setAdapter(mAdapter);

        fairAdapter = new FairAdapter(getActivity(), 0,
                mSamePlaceFairsItems);
        spinner.setAdapter(fairAdapter);
        Start_type = true;
        getSamePlaceFairs();

        compare_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVisitorStatus_Items.size() == 0) {
                    Toast.makeText(getActivity(), "입장객현황 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                } else if (mSamePlaceFairsItems.size() == 0) {
                    Toast.makeText(getActivity(), "비교 전시 목록이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    showAlertDialog();
                }
            }
        });

        chk_same_place_yn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(TAG, "chk_same_place_yn onCheckedChanged : " + isChecked);
                if (isChecked) {
                    mSame_Place_YN = "Y";
                } else {
                    mSame_Place_YN = "N";
                }
                Start_type = true;
                getSamePlaceFairs();
            }
        });
        return view;
    }

    protected void getSamePlaceFairs() {
        mLinearLayout.setVisibility(View.VISIBLE);
        HashMap<String, String> body = new HashMap<>();
        body.put("fair_id", mFAIR_ID);
        body.put("same_place_yn", mSame_Place_YN);
        new TelKit(getActivity(), this).request(TelKit.URL_API_GET_SAME_PLACE_FAIRS, body);
    }

    protected void getInquiryVisitorsStats() {
        Log.e(TAG, "getInquiryVisitorsStats : " + position_fair_id.size());
        mLinearLayout.setVisibility(View.VISIBLE);
        if (position_fair_id.size() == 0) {
            HashMap<String, String> body = new HashMap<>();
            body.put("fair_ids", mFAIR_ID);
            new TelKit(getActivity(), this).request(TelKit.URL_API_INQUIRY_VISITORS_STATS, body);
        } else if (position_fair_id.size() != 0) {
            HashMap<String, String> body = new HashMap<>();
            body.put("fair_ids", mFAIR_ID + "," + position_fair_id.values().toString().replace("[", "").replace("]", "").replace(" ", ""));
            Log.e(TAG, "비교하기 : " + mFAIR_ID + "," + position_fair_id.values().toString().replace("[", "").replace("]", "").replace(" ", ""));
            new TelKit(getActivity(), this).request(TelKit.URL_API_INQUIRY_VISITORS_STATS, body);
        } else {
            Toast.makeText(getActivity(), "비교 전시를 선택해 주세요.", Toast.LENGTH_SHORT).show();
            mLinearLayout.setVisibility(View.GONE);
        }
    }

    protected void getInquiryVisitorsStatsByHours(String day) {
        mLinearLayout.setVisibility(View.VISIBLE);
        HashMap<String, String> body = new HashMap<>();
        if (position_fair_id.size() != 0) {
            body.put("fair_ids", mFAIR_ID + "," + position_fair_id.values().toString().replace("[", "").replace("]", "").replace(" ", ""));
        } else {
            body.put("fair_ids", mFAIR_ID);
        }
        body.put("day", day);
        Log.e(TAG, "getInquiryVisitorsStatsByHours fair_ids: " + mFAIR_ID + "," + position_fair_id.values().toString().replace("[", "").replace("]", "").replace(" ", ""));
        Log.e(TAG, "getInquiryVisitorsStatsByHours day: " + day);
        new TelKit(getActivity(), this, mLinearLayout).request(TelKit.URL_API_INQUIRY_VISITORS_STATS_HOURS, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_INQUIRY_VISITORS_STATS_HOURS)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("list");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            mSamePlaceFairsTime_Items.clear();
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_list3: " + json_list);
                                if (json_list != null) {
                                    SamePlaceFairsTime samePlaceFairsTime = new SamePlaceFairsTime();
                                    samePlaceFairsTime.FAIR_SHORT_NAME = json_list.optString("FAIR_SHORT_NAME");
                                    samePlaceFairsTime.FAIR_ID = json_list.optString("FAIR_ID");
                                    samePlaceFairsTime.DAY_TOTAL = json_list.optString("DAY_TOTAL").replace("null", "0");
                                    samePlaceFairsTime.H08 = json_list.optString("H08").replace("null", "0");
                                    samePlaceFairsTime.H09 = json_list.optString("H09").replace("null", "0");
                                    samePlaceFairsTime.H10 = json_list.optString("H10").replace("null", "0");
                                    samePlaceFairsTime.H11 = json_list.optString("H11").replace("null", "0");
                                    samePlaceFairsTime.H12 = json_list.optString("H12").replace("null", "0");
                                    samePlaceFairsTime.H13 = json_list.optString("H13").replace("null", "0");
                                    samePlaceFairsTime.H14 = json_list.optString("H14").replace("null", "0");
                                    samePlaceFairsTime.H15 = json_list.optString("H15").replace("null", "0");
                                    samePlaceFairsTime.H16 = json_list.optString("H16").replace("null", "0");
                                    samePlaceFairsTime.H17 = json_list.optString("H17").replace("null", "0");
                                    samePlaceFairsTime.H18 = json_list.optString("H18").replace("null", "0");
                                    samePlaceFairsTime.H19 = json_list.optString("H19").replace("null", "0");
                                    mSamePlaceFairsTime_Items.add(samePlaceFairsTime);
                                }
                            }
                            setHorizontalBarChart();
                        } else {
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            mLinearLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(getActivity(), "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                        mLinearLayout.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    mLinearLayout.setVisibility(View.GONE);
                }
                //mLinearLayout.setVisibility(View.GONE);
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_SAME_PLACE_FAIRS)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String resultList = json.optString("data");
                    String default_fair_ids = json.optString("default_fair_ids");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            mSamePlaceFairsItems.clear();
                            JSONArray jsonArray = new JSONArray(resultList);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject json_list = jsonArray.getJSONObject(i);
                                Log.e(TAG, "JSONObject json_list1: " + json_list);
                                if (json_list != null) {
                                    SamePlaceFairs samePlaceFairs = new SamePlaceFairs();
                                    samePlaceFairs.FAIR_ID = json_list.optString("FAIR_ID");
                                    samePlaceFairs.FAIR_DESC = json_list.optString("FAIR_DESC");
                                    samePlaceFairs.FAIR_SHORT_DESC = json_list.optString("FAIR_SHORT_DESC");
                                    mSamePlaceFairsItems.add(samePlaceFairs);
                                }
                            }

                            Log.e(TAG, "default_fair_ids : " + default_fair_ids);
                            JSONArray default_fair_idsArray = new JSONArray(default_fair_ids);
                            position_fair_id.clear();
                            for (int i = 0; i < default_fair_idsArray.length(); i++) {
                                Log.e(TAG, "JSONObject default_fair_idsArray: " + default_fair_idsArray.get(i));
                                position_fair_id.put(i, default_fair_idsArray.get(i).toString());
                            }
                        } else {
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                        fairAdapter.notifyDataSetChanged();
                        Log.e(TAG, "Start_type: " + Start_type);
                        if (Start_type) {
                            getInquiryVisitorsStats();
                        }
                    } else {
                        Toast.makeText(getActivity(), "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_INQUIRY_VISITORS_STATS)) {
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
                                Log.e(TAG, "JSONObject json_list2: " + json_list);
                                if (json_list != null) {
                                    SamePlaceFairsCount samePlaceFairsCount = new SamePlaceFairsCount();
                                    samePlaceFairsCount.DAY_NAME = json_list.optString("DAY_NAME");
                                    samePlaceFairsCount.ENTER_COUNT_FAIR_1 = json_list.optString("ENTER_COUNT_FAIR_1").replace("null", "0");
                                    samePlaceFairsCount.ENTER_COUNT_FAIR_2 = json_list.optString("ENTER_COUNT_FAIR_2").replace("null", "0");
                                    samePlaceFairsCount.ENTER_COUNT_FAIR_3 = json_list.optString("ENTER_COUNT_FAIR_3").replace("null", "0");
                                    mVisitorStatus_Items.add(samePlaceFairsCount);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                            Log.e(TAG, "mDay_select : " + mDay_select);
                            getInquiryVisitorsStatsByHours(mDay_select);
                        } else {
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "msg : " + msg);
                            mLinearLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(getActivity(), "행사 내역을 가져오지 못했습니다", Toast.LENGTH_SHORT).show();
                        mLinearLayout.setVisibility(View.GONE);
                    }
                    //mLinearLayout.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    mLinearLayout.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getActivity(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                mLinearLayout.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(getActivity(), "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            mLinearLayout.setVisibility(View.GONE);
        }
        refresh_layout.setRefreshing(false);
    }

    public class RecycleAdapter extends RecyclerView.Adapter<VisitorStatusCompareFragment.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<SamePlaceFairsCount> mSamePlaceFairsCount_Items;
        private int selectPosition = 0;

        public RecycleAdapter(Context context, List<SamePlaceFairsCount> itemList) {
            this.mContext = context;
            this.mSamePlaceFairsCount_Items = itemList;
        }

        @Override
        public VisitorStatusCompareFragment.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.fair_day_count_item, parent, false);
            Log.e(TAG, "onCreateViewHolder: ");
            return new VisitorStatusCompareFragment.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(VisitorStatusCompareFragment.RecycleAdapter.ItemViewHolder holder, int position) {
            SamePlaceFairsCount samePlaceFairsCount = mSamePlaceFairsCount_Items.get(position);

            int layout_count = position_fair_id.size() + 1;
            Log.e(TAG, "layout_count: " + layout_count);

            holder.fair_date.setText(samePlaceFairsCount.DAY_NAME);
            holder.fair_date_count_1.setText(convertCurrencyStr(Double.parseDouble(samePlaceFairsCount.ENTER_COUNT_FAIR_1)));
            holder.fair_date_count_2.setText(convertCurrencyStr(Double.parseDouble(samePlaceFairsCount.ENTER_COUNT_FAIR_2)));
            holder.fair_date_count_3.setText(convertCurrencyStr(Double.parseDouble(samePlaceFairsCount.ENTER_COUNT_FAIR_3)));

            holder.daychkbtn.setOnCheckedChangeListener(null);
            holder.daychkbtn.setChecked(position == selectPosition);
            holder.daychkbtn.setTag(position);

            if (layout_count == 2) {
                holder.fair_date_count_2.setVisibility(View.VISIBLE);
                holder.fair_date_count_3.setVisibility(View.GONE);
                date_name_layout_2.setVisibility(View.VISIBLE);
                date_name_layout_3.setVisibility(View.GONE);
            } else if (layout_count == 3) {
                holder.fair_date_count_2.setVisibility(View.VISIBLE);
                holder.fair_date_count_3.setVisibility(View.VISIBLE);
                date_name_layout_2.setVisibility(View.VISIBLE);
                date_name_layout_3.setVisibility(View.VISIBLE);
            } else {
                holder.fair_date_count_2.setVisibility(View.GONE);
                holder.fair_date_count_3.setVisibility(View.GONE);
                date_name_layout_2.setVisibility(View.GONE);
                date_name_layout_3.setVisibility(View.GONE);
            }

            if (holder.fair_date.getText().equals("총계")) {
                holder.daychkbtn.setVisibility(View.INVISIBLE);
                holder.fair_day_count_item_layout.setEnabled(false);
            } else {
                holder.daychkbtn.setVisibility(View.VISIBLE);
                holder.fair_day_count_item_layout.setEnabled(true);
            }

            holder.daychkbtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.e(TAG, "라디오 버튼 클릭 setOnClickListener");
                    selectPosition = holder.getBindingAdapterPosition();
                    itemCheckChanged(buttonView);
                    Log.e(TAG, "mDay_select : " + mDay_select);
                    Log.e(TAG, "getSelectedItem() : " + getSelectedItem());
                    mDay_select = getSelectedItem();
                    getInquiryVisitorsStats();
                    //getInquiryVisitorsStatsByHours(getSelectedItem());
                }
            });

            holder.fair_day_count_item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.daychkbtn.toggle();
                }
            });
        }

        private void itemCheckChanged(View v) {
            selectPosition = (Integer) v.getTag();
            notifyDataSetChanged();
        }

        //Return the selectedPosition item
        public String getSelectedItem() {
            if (selectPosition != -1) {
                title_txt.setText(mSamePlaceFairsCount_Items.get(selectPosition).DAY_NAME + " 시간대별 비교");
                Log.e(TAG, "getSelectedItem : " + mSamePlaceFairsCount_Items.get(selectPosition).DAY_NAME.replace("일차", ""));
                //mDay_select = mSamePlaceFairsCount_Items.get(selectPosition).DAY_NAME.replace("일차", "");
                return mSamePlaceFairsCount_Items.get(selectPosition).DAY_NAME.replace("일차", "");
            }
            return "";
        }

        @Override
        public int getItemCount() {
            int count = mVisitorStatus_Items.size();
            layoutEmpty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);

            return count;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            public VisitorStatusCompareFragment.RecycleAdapter mAdapter;
            public RadioButton daychkbtn;
            public TextView fair_date;
            public TextView fair_date_count_1;
            public TextView fair_date_count_2;
            public TextView fair_date_count_3;
            public LinearLayout fair_day_count_item_layout;

            public ItemViewHolder(View itemView, VisitorStatusCompareFragment.RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.daychkbtn = itemView.findViewById(R.id.daychkbtn);
                this.fair_date = itemView.findViewById(R.id.fair_date);
                this.fair_date_count_1 = itemView.findViewById(R.id.fair_date_count_1);
                this.fair_date_count_2 = itemView.findViewById(R.id.fair_date_count_2);
                this.fair_date_count_3 = itemView.findViewById(R.id.fair_date_count_3);
                this.fair_day_count_item_layout = itemView.findViewById(R.id.fair_day_count_item_layout);
            }
        }
    }

    private void setHorizontalBarChart() {
        Log.e(TAG, "setHorizontalBarChart mSamePlaceFairsTime_Items : " + mSamePlaceFairsTime_Items.size());
        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        if (mSamePlaceFairsTime_Items.size() != 0) {
            for (int i = 0; i < mSamePlaceFairsTime_Items.size(); i++) {

                ArrayList<BarEntry> yValues = new ArrayList<>();
                yValues.clear();

                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H08)}, 11));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H09)}, 10));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H10)}, 9));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H11)}, 8));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H12)}, 7));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H13)}, 6));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H14)}, 5));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H15)}, 4));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H16)}, 3));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H17)}, 2));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H18)}, 1));
                yValues.add(new BarEntry(new float[]{Integer.parseInt(mSamePlaceFairsTime_Items.get(i).H19)}, 0));

                BarDataSet barDataSet = new BarDataSet(yValues, mSamePlaceFairsTime_Items.get(i).FAIR_SHORT_NAME);
                barDataSet.setBarSpacePercent(15f);

                if (i == 0) {
                    barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_E66C6C)));
                } else if (i == 1) {
                    barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_81A9E5)));
                    date_name_2.setText(mSamePlaceFairsTime_Items.get(i).FAIR_SHORT_NAME);
                    date_name_2.setLines(2);
                } else if (i == 2) {
                    barDataSet.setColors(Collections.singletonList(getResources().getColor(R.color.color_F5C850)));
                    date_name_3.setText(mSamePlaceFairsTime_Items.get(i).FAIR_SHORT_NAME);
                    date_name_3.setLines(2);
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
        } else {
            ArrayList<BarEntry> yValues = new ArrayList<>();
            yValues.clear();
            for (int i = 0; i < 12; i++) {
                yValues.add(new BarEntry(new float[]{0}, i));
            }
            BarDataSet barDataSet = new BarDataSet(yValues, null);
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

        if (mSamePlaceFairsTime_Items.size() > 2) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3500));
        } else if (mSamePlaceFairsTime_Items.size() > 1) {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3000));
        } else {
            horizontal_bar_chart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2500));
        }
        horizontal_bar_chart.getLegend().setWordWrapEnabled(true);
        mLinearLayout.setVisibility(View.GONE);
    }

    public class FairAdapter extends ArrayAdapter<SamePlaceFairs> {
        private Context mContext;
        private ArrayList<SamePlaceFairs> listState = null;
        private FairAdapter myAdapter;
        private boolean isFromView = false;

        public FairAdapter(Context context, int resource, List<SamePlaceFairs> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.listState = (ArrayList<SamePlaceFairs>) objects;
            this.myAdapter = this;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(final int position, View convertView,
                                  ViewGroup parent) {

            final FairAdapter.ViewHolder holder;
            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(mContext);
                convertView = layoutInflator.inflate(R.layout.custom_spinner_item, null);

                holder = new FairAdapter.ViewHolder();
                holder.mTextView = (TextView) convertView
                        .findViewById(R.id.fair_name);
                holder.mCheckBox = (CheckBox) convertView
                        .findViewById(R.id.fair_chk);
                convertView.setTag(holder);
                if (Start_type && position_fair_id.size() != 0) {
                    Count = position_fair_id.size();
                    for (int i = 0; i < position_fair_id.size(); i++) {
                        Log.e(TAG, "position_fair_id.values() : " + position_fair_id.values());
                        Log.e(TAG, "position_fair_id.get(i).toString() : " + position_fair_id.get(i));
                        if (position_fair_id.get(i).equals(listState.get(i).FAIR_ID)) {
                            listState.get(i).setSelected(true);
                        } else {
                            listState.get(i).setSelected(false);
                        }
                    }
                    Start_type = false;
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextView.setText(listState.get(position).FAIR_DESC);
            // To check weather checked event fire from getview() or user input
            isFromView = true;
            holder.mCheckBox.setChecked(listState.get(position).isSelected());
            isFromView = false;
            Log.e(TAG, "getCustomView position : " + position);

            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mCheckBox.toggle();
                }
            });

            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.e(TAG, "Count : " + Count);
                    Log.e(TAG, "isFromView : " + isFromView);
                    if (!isFromView) {
                        listState.get(position).setSelected(isChecked);
                        if (isChecked) {
                            Count = Count + 1;
                            if (Count > 2) {
                                Count = Count - 1;
                                buttonView.setChecked(false);
                                listState.get(position).setSelected(false);
                                Toast.makeText(mContext, "전시회를 2개 이상 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("MyAdapter", "FAIR_ID : " + listState.get(position).FAIR_ID);
                                Log.e("MyAdapter", "FAIR_SHORT_DESC : " + listState.get(position).FAIR_SHORT_DESC);
                                Log.e("MyAdapter", "position : " + position);
                                position_fair_id.put(position, listState.get(position).FAIR_ID);
                                Log.e(TAG, "isChecked Count : " + Count);

                                if (Count == 1) {
                                    date_name_2.setText(listState.get(position).FAIR_SHORT_DESC);
                                    date_name_2.setLines(2);
                                } else if (Count == 2) {
                                    date_name_3.setText(listState.get(position).FAIR_SHORT_DESC);
                                    date_name_3.setLines(2);
                                }
                            }
                        } else {
                            if (Count > 0) {
                                Count = Count - 1;
                                position_fair_id.remove(position);
                                Log.e(TAG, "Not isChecked Count : " + Count);
                            }
                        }
                        mDay_select = "1";
                        title_txt.setText(mDay_select + "일차 시간대별 비교");
                        mVisitorStatus_Items.clear();
                        mAdapter = new VisitorStatusCompareFragment.RecycleAdapter(getActivity(), mVisitorStatus_Items);
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        mSamePlaceFairsTime_Items.clear();
                        setHorizontalBarChart();
                    }
                    Log.e(TAG, "position_fair_id : " + position_fair_id.values());

                    if (Count == 3) {
                        date_name_layout_2.setVisibility(View.VISIBLE);
                        date_name_layout_3.setVisibility(View.VISIBLE);
                    } else if (Count == 2) {
                        date_name_layout_2.setVisibility(View.VISIBLE);
                        date_name_layout_3.setVisibility(View.GONE);
                    } else {
                        date_name_layout_2.setVisibility(View.GONE);
                        date_name_layout_3.setVisibility(View.GONE);
                    }
                }
            });

            return convertView;
        }

        private class ViewHolder {
            private TextView mTextView;
            private CheckBox mCheckBox;
        }
    }

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_same_fair_dialog, null);
        builder.setView(view);

        final ListView listview = (ListView) view.findViewById(R.id.listview_alterdialog_list);
        final Button dialog_cancel = (Button) view.findViewById(R.id.dialog_cancel);
        final Button dialog_ok = (Button) view.findViewById(R.id.dialog_ok);
        final AlertDialog dialog = builder.create();

        FairAdapter simpleAdapter = new FairAdapter(getActivity(),
                R.layout.custom_spinner_item, mSamePlaceFairsItems);

        listview.setAdapter(simpleAdapter);

        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInquiryVisitorsStats();
                dialog.dismiss();
            }
        });
        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
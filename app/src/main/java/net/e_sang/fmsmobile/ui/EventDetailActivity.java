package net.e_sang.fmsmobile.ui;

import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import java.util.HashMap;

public class EventDetailActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private String FAIR_ID = "";
    private String FAIR_MASTER_ID = "";
    private TextView fair_name = null;
    private TextView tot_cnt = null;
    private TextView new_cnt = null;
    private TextView old_cnt = null;
    private TextView tot_booth_cnt = null;
    private TextView booth_1 = null;
    private TextView booth_2 = null;
    private TextView booth_3 = null;
    private TextView tot_facil_amt = null;
    private TextView sub_facil_amt = null;
    private TextView balance_amt = null;
    private TextView balance_cnt = null;
    private TextView booth_facil_amt = null;
    private TextView etc_facil_amt = null;
    private CircularProgressBar mProgressBar = null;
    private LinearLayout mLinearLayout = null;
    private TextView add_sales_amt = null;
    private TextView discount_amt = null;
    private TextView real_facil_amt = null;
    private TextView sale_amt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_event);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_event));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_event_detail);
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
            FAIR_MASTER_ID = intent.getExtras().getString("FAIR_MASTER_ID");
        }

        fair_name = findViewById(R.id.fair_name);
        tot_cnt = findViewById(R.id.tot_cnt);
        new_cnt = findViewById(R.id.new_cnt);
        old_cnt = findViewById(R.id.old_cnt);
        tot_booth_cnt = findViewById(R.id.tot_booth_cnt);
        booth_1 = findViewById(R.id.booth_1);
        booth_2 = findViewById(R.id.booth_2);
        booth_3 = findViewById(R.id.booth_3);
        tot_facil_amt = findViewById(R.id.tot_facil_amt);
        sub_facil_amt = findViewById(R.id.sub_facil_amt);
        balance_amt = findViewById(R.id.balance_amt);
        balance_cnt = findViewById(R.id.balance_cnt);
        booth_facil_amt = findViewById(R.id.booth_facil_amt);
        etc_facil_amt = findViewById(R.id.etc_facil_amt);
        add_sales_amt = findViewById(R.id.add_sales_amt);
        discount_amt = findViewById(R.id.discount_amt);
        real_facil_amt = findViewById(R.id.real_facil_amt);
        sale_amt = findViewById(R.id.sale_amt);

        getFairSummary();
    }

    protected void getFairSummary() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("FAIR_MASTER_ID=%s&FAIR_ID=%s&SYSTEM_ID=%s", FAIR_MASTER_ID, FAIR_ID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("FAIR_MASTER_ID", FAIR_MASTER_ID);
        body.put("FAIR_ID", FAIR_ID);
        body.put("SYSTEM_ID", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_COMPANY_FAIR_SUMMARY, body);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_COMPANY_FAIR_SUMMARY)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String resultStr = json.optString("result");
                    String resultdata = json.optString("data");

                    JSONObject result_obj = new JSONObject(resultStr);
                    String code = result_obj.optString("code");
                    String msg = result_obj.optString("msg");

                    if ("ok".equals(code)) {
                        if (resultdata != null) {
                            JSONObject resultdata_obj = new JSONObject(resultdata);
                            fair_name.setText(resultdata_obj.optString("FAIR_NAME").replace("null", "0"));
                            tot_cnt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOT_CNT").replace("null", "0"))));
                            new_cnt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("NEW_CNT").replace("null", "0"))));
                            old_cnt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("OLD_CNT").replace("null", "0"))));
                            tot_booth_cnt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOT_BOOTH_CNT").replace("null", "0"))));
                            booth_1.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BOOTH_1").replace("null", "0"))));
                            booth_2.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BOOTH_2").replace("null", "0"))));
                            booth_3.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BOOTH_3").replace("null", "0"))));
                            tot_facil_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOT_FACIL_AMT").replace("null", "0"))));
                            sub_facil_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("SUB_FACIL_AMT").replace("null", "0"))));
                            balance_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BALANCE_AMT").replace("null", "0"))));
                            balance_cnt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BALANCE_CNT").replace("null", "0"))));
                            booth_facil_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("BOOTH_FACIL_AMT").replace("null", "0"))));
                            etc_facil_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("ETC_FACIL_AMT").replace("null", "0"))));

                            if (!"0".equals(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_ADD_SALES_AMT").replace("null", "0"))))) {
                                add_sales_amt.setText("+" + convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_ADD_SALES_AMT").replace("null", "0"))));
                            } else {
                                add_sales_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_ADD_SALES_AMT").replace("null", "0"))));
                            }
                            if (!"0".equals(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_DISCOUNT_AMT").replace("null", "0"))))) {
                                discount_amt.setText("-" + convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_DISCOUNT_AMT").replace("null", "0"))));
                            } else {
                                discount_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOTAL_DISCOUNT_AMT").replace("null", "0"))));
                            }
                            real_facil_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOT_REAL_FACIL_AMT").replace("null", "0"))));
                            sale_amt.setText(convertCurrencyStr(Double.parseDouble(resultdata_obj.optString("TOT_SALE_AMT").replace("null", "0"))));
                        }
                        //Toast.makeText(EventDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(EventDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EventDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EventDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(EventDetailActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
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

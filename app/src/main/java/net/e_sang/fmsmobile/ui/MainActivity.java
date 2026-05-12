package net.e_sang.fmsmobile.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import net.e_sang.fmsmobile.MyApplication;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.Kit.LogType;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static net.e_sang.fmsmobile.MyApplication.mFirebaseAnalytics;
import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import net.e_sang.fmsmobile.BuildConfig;
import net.e_sang.fmsmobile.namecard.NameCardCameraActivity;
import net.e_sang.fmsmobile.namecard.NameCardListActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener, NavigationView.OnNavigationItemSelectedListener {
    private String TAG = getClass().getSimpleName();
    // 알림 현황판 Start
    private RecyclerView mRecyclerViewNotiDash = null;
    private ArrayList<NotiDash> mNotiDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterNoti mAdapterNotiDash = null;
    private TextView noti_Empty = null;
    // 알림 현황판 End

    // 영업 현황판 Start
    private RecyclerView mRecyclerViewSalesDash = null;
    private ArrayList<SalesDash> mSalesDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterSales mAdapterSalesDash = null;
    private TextView sales_Empty = null;
    // 영업 현황판 End

    // 배정 현황판 Start
    private RecyclerView mRecyclerViewAssignDash = null;
    private ArrayList<AssignDash> mAssignDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterAssign mAdapterAssignDash = null;
    private TextView assign_Empty = null;
    // 배정 현황판 End

    // 채권 현황판 Start
    private RecyclerView mRecyclerViewRecDash = null;
    private ArrayList<RecDash> mRecDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterRec mAdapterRecDash = null;
    private TextView rec_Empty = null;
    // 채권 현황판 End

    // 행사 현황판 Start
    private RecyclerView mRecyclerViewFairDash = null;
    private ArrayList<FairDash> mFairDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterFair mAdapterFairDash = null;
    private TextView fair_Empty = null;
    // 행사 현황판 End

    // 사전등록 현황판 Start
    private RecyclerView mRecyclerViewPreregistrationDash = null;
    private ArrayList<PreregistrationDash> mPreregistrationDash_Items = new ArrayList<>();
    private MainActivity.RecycleAdapterPreregistration mAdapterPreregistrationDash = null;
    private TextView preregistration_Empty = null;
    // 사전등록 현황판 End

    private LinearLayout mLinearLayout = null;
    private LinearLayout main_layout = null;
    private BackPressCloseHandler backPressCloseHandler = null;
    private boolean NOTI_CHECK = false;
    private DrawerLayout drawer = null;
    private NavigationView navigationView = null;
    private TextView nav_team = null;
    private TextView nav_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_F8F8F8);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyInsets();
        overridePendingTransition(0, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_F8F8F8));
            setSupportActionBar(toolbar);
//            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
//            txtToolbarTitle.setTextColor(getResources().getColor(R.color.color_030303));
//            txtToolbarTitle.setText(R.string.app_name);
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.btn_briefcase_bg_ripple);
        }

        main_layout = findViewById(R.id.main);
        backPressCloseHandler = new BackPressCloseHandler(this, main_layout);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        View root = findViewById(R.id.drawer_layout);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        findViewById(R.id.editID).setOnClickListener(this);
        findViewById(R.id.allocation_layout).setOnClickListener(this);
        findViewById(R.id.sales_layout).setOnClickListener(this);
        findViewById(R.id.receivable_layout).setOnClickListener(this);
        findViewById(R.id.notification_layout).setOnClickListener(this);
        findViewById(R.id.fair_layout).setOnClickListener(this);
        findViewById(R.id.event_layout_bottom).setOnClickListener(this);
        findViewById(R.id.preregistration_layout).setOnClickListener(this);
        findViewById(R.id.admission_statistics_layout_bottom).setOnClickListener(this);
        findViewById(R.id.entrance_status_layout_bottom).setOnClickListener(this);
        findViewById(R.id.preregistration_layout_bottom).setOnClickListener(this);
        findViewById(R.id.receivable_layout_bottom).setOnClickListener(this);
        findViewById(R.id.notification_layout_bottom).setOnClickListener(this);
        findViewById(R.id.btn_fms_web).setOnClickListener(this);
        findViewById(R.id.btn_add_report).setOnClickListener(this);
        findViewById(R.id.name_card_layout_bottom).setOnClickListener(this);

        mLinearLayout = findViewById(R.id.progressBar_layout);
        // 알림 현황판 Start
        mRecyclerViewNotiDash = findViewById(R.id.noti_recyclerview);
        noti_Empty = findViewById(R.id.noti_Empty);
        LinearLayoutManager layoutManagerNoti = new LinearLayoutManager(this);
        layoutManagerNoti.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewNotiDash.setLayoutManager(layoutManagerNoti);
        mAdapterNotiDash = new MainActivity.RecycleAdapterNoti(this, mNotiDash_Items);
        mRecyclerViewNotiDash.setAdapter(mAdapterNotiDash);
        mAdapterNotiDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, NoticeActivity.class);
            }
        });
        // 알림 현황판 End

        // 영업 현황판 Start
        mRecyclerViewSalesDash = findViewById(R.id.sales_recyclerview);
        sales_Empty = findViewById(R.id.sales_Empty);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewSalesDash.setLayoutManager(layoutManager);
        mAdapterSalesDash = new MainActivity.RecycleAdapterSales(this, mSalesDash_Items);
        mRecyclerViewSalesDash.setAdapter(mAdapterSalesDash);
        mAdapterSalesDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, SalesActivity.class);
            }
        });
        // 영업 현황판 End

        // 배정 현황판 Start
        mRecyclerViewAssignDash = findViewById(R.id.assign_recyclerview);
        assign_Empty = findViewById(R.id.assign_Empty);
        LinearLayoutManager layoutManagerAssign = new LinearLayoutManager(this);
        layoutManagerAssign.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewAssignDash.setLayoutManager(layoutManagerAssign);
        mAdapterAssignDash = new MainActivity.RecycleAdapterAssign(this, mAssignDash_Items);
        mRecyclerViewAssignDash.setAdapter(mAdapterAssignDash);
        mAdapterAssignDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, AllocationActivity.class);
            }
        });
        // 배정 현황판 End

        // 채권 현황판 Start
        mRecyclerViewRecDash = findViewById(R.id.rec_recyclerview);
        rec_Empty = findViewById(R.id.rec_Empty);
        LinearLayoutManager layoutManagerRec = new LinearLayoutManager(this);
        layoutManagerRec.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewRecDash.setLayoutManager(layoutManagerRec);
        mAdapterRecDash = new MainActivity.RecycleAdapterRec(this, mRecDash_Items);
        mRecyclerViewRecDash.setAdapter(mAdapterRecDash);
        mAdapterRecDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, ReceivableListActivity.class);
            }
        });
        // 채권 현황판 End

        // 행사 현황판 Start
        mRecyclerViewFairDash = findViewById(R.id.fair_recyclerview);
        fair_Empty = findViewById(R.id.fair_Empty);
        LinearLayoutManager layoutManagerFair = new LinearLayoutManager(this);
        layoutManagerFair.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewFairDash.setLayoutManager(layoutManagerFair);
        mAdapterFairDash = new MainActivity.RecycleAdapterFair(this, mFairDash_Items);
        mRecyclerViewFairDash.setAdapter(mAdapterFairDash);
        mAdapterFairDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, EventActivity.class);
            }
        });
        // 행사 현황판 End

        // 사전등록 현황판 Start
        mRecyclerViewPreregistrationDash = findViewById(R.id.preregistration_recyclerview);
        preregistration_Empty = findViewById(R.id.preregistration_Empty);
        LinearLayoutManager layoutManagerPreregistration = new LinearLayoutManager(this);
        layoutManagerPreregistration.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerViewPreregistrationDash.setLayoutManager(layoutManagerPreregistration);
        mAdapterPreregistrationDash = new MainActivity.RecycleAdapterPreregistration(this, mPreregistrationDash_Items);
        mRecyclerViewPreregistrationDash.setAdapter(mAdapterPreregistrationDash);
        mAdapterPreregistrationDash.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kit.startActivity(MainActivity.this, PreregistrationActivity.class);
            }
        });
        // 사전등록 현황판 End

        int visibleAllocation = View.GONE;      // 배정
        int visibleEvent = View.GONE;           // 행사
        int visibleSales = View.GONE;           // 영업
        int visibleReceivable = View.GONE;      // 채권
        int visibleNotification = View.GONE;    // 알림
        int visibleEventBottom = View.GONE;     // 행사 하단
        int visiblePreregistrationBottom = View.GONE;     // 사전등록
        int visibleAdmissionStatisticsBottom = View.GONE;     // 연별입장객통계 하단
        int visibleEntranceStatusBottom = View.GONE;     // 입장객현황 하단
        int visiblePreregistrationBottomBottom = View.GONE;     // 사전등록 하단
        int visibleReceivableStatusBottom = View.GONE;     // 채권 하단
        int visibleNotificationStatusBottom = View.GONE;     // 알림 하단
        int visibleNameCardBottom = View.GONE;     // 명함등록 상단

        final UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null && Kit.isNotNullNotEmpty(userInfo.SYS_ID)) {
            if (!"".equals(userInfo.LOGIN_ID)) {
                mFirebaseAnalytics.setUserId(userInfo.LOGIN_ID);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.METHOD, userInfo.LOGIN_ID);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
                //Crashlytics.setUserIdentifier(userInfo.LOGIN_ID);
            }
            //FirebaseMessaging.getInstance().getToken();
            // 푸시 알림 토큰 전송
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isComplete()) {
                        String token = task.getResult();
                        Log.e(TAG, "onSuccess::token_id = " + token);
                        TelKit.tokenRegistrationToServer(MainActivity.this, token, userInfo.SYS_ID, userInfo.LOGIN_ID);
                    }
                }
            });

//            if ("esgroup".equals(BuildConfig.APP_FLAVOR)) {
            Log.e(TAG, "esgroup");
            // 직책에 따라 표시/숨김
            if ("1".equals(userInfo.POSITION_ID)) {     // 관리자
                visibleEvent = View.VISIBLE;
                visiblePreregistrationBottom = View.VISIBLE;
                visibleAdmissionStatisticsBottom = View.VISIBLE;
                visibleEntranceStatusBottom = View.VISIBLE;
                visibleReceivableStatusBottom = View.VISIBLE;     // 채권 하단
                visibleNotificationStatusBottom = View.VISIBLE;     // 알림 하단
            } else if ("2".equals(userInfo.POSITION_ID) || "3".equals(userInfo.POSITION_ID)) {      // 부서장, 팀장
                visibleAllocation = View.VISIBLE;
                visibleSales = View.VISIBLE;
                visibleReceivable = View.VISIBLE;
                visibleNotification = View.VISIBLE;
                visibleEventBottom = View.VISIBLE;
                visiblePreregistrationBottomBottom = View.VISIBLE;
                visibleAdmissionStatisticsBottom = View.VISIBLE;
                visibleEntranceStatusBottom = View.VISIBLE;
            } else {
                visibleAllocation = View.VISIBLE;
                visibleSales = View.VISIBLE;
                visibleReceivable = View.VISIBLE;
                visibleNotification = View.VISIBLE;
                visiblePreregistrationBottomBottom = View.VISIBLE;  // 사전등록 하단
                visibleAdmissionStatisticsBottom = View.VISIBLE;    // 연별입장객통계 하단
                visibleEntranceStatusBottom = View.VISIBLE; // 입장객현황 하단
            }
//            } else if ("exco".equals(BuildConfig.APP_FLAVOR)) {
//                Log.e(TAG, "exco");
//                // 직책에 따라 표시/숨김
//                if ("1".equals(userInfo.POSITION_ID)) {     // 관리자
//                    visibleEvent = View.VISIBLE;
//                    visibleReceivableStatusBottom = View.VISIBLE;     // 채권 하단
//                    visibleNotificationStatusBottom = View.VISIBLE;     // 알림 하단
//                } else if ("2".equals(userInfo.POSITION_ID) || "3".equals(userInfo.POSITION_ID)) {      // 부서장, 팀장
//                    visibleAllocation = View.VISIBLE;
//                    visibleSales = View.VISIBLE;
//                    visibleReceivable = View.VISIBLE;
//                    visibleNotification = View.VISIBLE;
//                    visibleEventBottom = View.VISIBLE;
//                } else {
//                    visibleAllocation = View.VISIBLE;
//                    visibleSales = View.VISIBLE;
//                    visibleReceivable = View.VISIBLE;
//                    visibleNotification = View.VISIBLE;
//                }
//            }

//            if ("exco".equals(BuildConfig.APP_FLAVOR)) {
//                visibleNameCardBottom = View.GONE;
//            } else {
//                visibleNameCardBottom = View.VISIBLE;
//            }
            visibleNameCardBottom = View.VISIBLE;   //담당자 간편 등록
        }

        findViewById(R.id.allocation_layout).setVisibility(visibleAllocation);
        findViewById(R.id.fair_layout).setVisibility(visibleEvent);
        findViewById(R.id.sales_layout).setVisibility(visibleSales);
        findViewById(R.id.receivable_layout).setVisibility(visibleReceivable);
        findViewById(R.id.notification_layout).setVisibility(visibleNotification);
        findViewById(R.id.event_layout_bottom).setVisibility(visibleEventBottom);
        findViewById(R.id.preregistration_layout).setVisibility(visiblePreregistrationBottom);
        findViewById(R.id.admission_statistics_layout_bottom).setVisibility(visibleAdmissionStatisticsBottom);
        findViewById(R.id.entrance_status_layout_bottom).setVisibility(visibleEntranceStatusBottom);
        findViewById(R.id.preregistration_layout_bottom).setVisibility(visiblePreregistrationBottomBottom);

        findViewById(R.id.receivable_layout_bottom).setVisibility(visibleReceivableStatusBottom);
        findViewById(R.id.notification_layout_bottom).setVisibility(visibleNotificationStatusBottom);
        findViewById(R.id.name_card_layout_bottom).setVisibility(visibleNameCardBottom);
        showSecuritySnackbar();

        Intent intent = getIntent(); /*데이터 수신*/
        NOTI_CHECK = intent.getBooleanExtra(Extra.KEY_NOTI_CHECK, false);
        if (NOTI_CHECK == true || MyApplication.Notification_Check == true) {
            Kit.startActivity(this, NoticeActivity.class);
            MyApplication.Notification_Check = false;
        }

        View headerView = navigationView.getHeaderView(0);
        nav_team = (TextView) headerView.findViewById(R.id.nav_team);
        nav_team.setText("부서 : " + userInfo.TEAM_NAME);
        nav_name = (TextView) headerView.findViewById(R.id.nav_name);
        nav_name.setText("이름 : " + userInfo.USER_NAME);
        Log.e(TAG, "onCreate");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    backPressCloseHandler.onBackPressed();
                }
            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            backPressCloseHandler.onBackPressed();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            Kit.startActivity(this, WorkOutListActivity.class);
            return true;
        } else if (itemId == R.id.action_plan) {
            Kit.startActivity(this, ActionPlanListActivity.class);
            return true;
        } else if (itemId == R.id.logout) {
            try {
                AlertDialog.Builder alert_Logout = new AlertDialog.Builder(this);
                alert_Logout.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("로그아웃",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PrefKit.setUserInfo(MainActivity.this, null);
                                Kit.startActivity(MainActivity.this, LoginActivity.class);
                                finish();
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = alert_Logout.create();
                alert.show();
                Button positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.color_text_error));
                negativeButton.setTextColor(getResources().getColor(R.color.color_text_high));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.editID) {
            Log.e(TAG, "업체검색");
            Kit.startActivity(this, SearchActivity.class);
        } else if (id == R.id.allocation_layout) {
            Log.e(TAG, "배정");
            Kit.startActivity(this, AllocationActivity.class);
        } else if (id == R.id.sales_layout) {
            Log.e(TAG, "영업");
            Kit.startActivity(this, SalesActivity.class);
        } else if (id == R.id.receivable_layout || id == R.id.receivable_layout_bottom) {
            Log.e(TAG, "채권");
            Kit.startActivity(this, ReceivableListActivity.class);
        } else if (id == R.id.notification_layout || id == R.id.notification_layout_bottom) {
            Log.e(TAG, "알림");
            Kit.startActivity(this, NoticeActivity.class);
        } else if (id == R.id.fair_layout || id == R.id.event_layout_bottom) {
            Log.e(TAG, "행사");
            Kit.startActivity(this, EventActivity.class);
        } else if (id == R.id.preregistration_layout || id == R.id.preregistration_layout_bottom) {
            Log.e(TAG, "사전등록");
            Kit.startActivity(this, PreregistrationActivity.class);
        } else if (id == R.id.admission_statistics_layout_bottom) {
            Log.e(TAG, "연별입장객통계");
            Kit.startActivity(this, AnnualVisitorStatisticsActivity.class);
        } else if (id == R.id.entrance_status_layout_bottom) {
            Log.e(TAG, "입장객현황");
            Kit.startActivity(this, VisitorStatusActivity.class);
        } else if (id == R.id.btn_fms_web) {
            Log.e(TAG, "FMS Browser");
            Kit.startActivity(this, FMSWebActivity.class);
        } else if (id == R.id.btn_add_report) {
            Log.e(TAG, "동향보고 등록");
            Kit.startActivity(this, ReportActivity.class);
        } else if (id == R.id.name_card_layout_bottom) {
            Log.e(TAG, "OCR 명함 등록");
            Kit.startActivity(this, NameCardCameraActivity.class);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        getHomeDash();
    }

    protected void getHomeDash() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            String body = String.format("system_id=%s", userInfo.SYS_ID);
            new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_HOME_DASH_LIST, body);
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_HOME_DASH_LIST)) {

                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    JSONObject dataObj = json.optJSONObject("data");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");

                    if ("ok".equals(code)) {
                        JSONObject noti_data = dataObj.optJSONObject("noti_data");
                        if ("ok".equals(noti_data.optString("Noti_result"))) {
                            setNoti(noti_data.optString("Noti"), noti_data.optString("Noti_count"));
                        } else if ("fail_Exception".equals(noti_data.optString("Noti_result"))) {
                            noti_Empty.setText("알림 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setNoti(noti_data.optString("Noti_result"), noti_data.optString("Noti_count"));
                        }

                        JSONObject assign_data = dataObj.optJSONObject("assign_data");
                        if ("ok".equals(assign_data.optString("Assign_result"))) {
                            setAssign(assign_data.optString("Assign"), assign_data.optString("Assign_count"));
                        } else if ("fail_Exception".equals(assign_data.optString("Assign_result"))) {
                            assign_Empty.setText("배정 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setAssign(assign_data.optString("Assign_result"), assign_data.optString("Assign_count"));
                        }

                        JSONObject rec_data = dataObj.optJSONObject("rec_data");
                        if ("ok".equals(rec_data.optString("Rec_result"))) {
                            setRec(rec_data.optString("Rec"), rec_data.optString("Rec_count"));
                        } else if ("fail_Exception".equals(rec_data.optString("Rec_result"))) {
                            rec_Empty.setText("채권관리 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setRec(rec_data.optString("Rec_result"), rec_data.optString("Rec_count"));
                        }

                        JSONObject sales_data = dataObj.optJSONObject("sales_data");
                        if ("ok".equals(sales_data.optString("Sales_result"))) {
                            setSales(sales_data.optString("Sales"), sales_data.optString("Sales_count"));
                        } else if ("fail_Exception".equals(sales_data.optString("Sales_result"))) {
                            sales_Empty.setText("영업 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setSales(sales_data.optString("Sales_result"), sales_data.optString("Sales_count"));
                        }

                        JSONObject fair_data = dataObj.optJSONObject("fair_data");
                        if ("ok".equals(fair_data.optString("Fair_result"))) {
                            setFair(fair_data.optString("Fair"), fair_data.optString("Fair_count"));
                        } else if ("fail_Exception".equals(fair_data.optString("Fair_result"))) {
                            fair_Empty.setText("행사 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setFair(fair_data.optString("Fair_result"), fair_data.optString("Fair_count"));
                        }

                        JSONObject preregistration_data = dataObj.optJSONObject("preregistration_data");
                        if ("ok".equals(preregistration_data.optString("Preregistration_result"))) {
                            setPreregistration(preregistration_data.optString("Preregistration"), preregistration_data.optString("Preregistration_count"));
                        } else if ("fail_Exception".equals(preregistration_data.optString("Preregistration_result"))) {
                            preregistration_Empty.setText("사전등록 데이터가 잘못되었습니다.\n관리자에게 문의해 주세요.");
                        } else {
                            setPreregistration(preregistration_data.optString("Preregistration_result"), preregistration_data.optString("Preregistration_count"));
                        }
                    } else {
                        // Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 알림 현황판 Start
    private void setNoti(String noti, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray notiArray = new JSONArray(noti);
                mNotiDash_Items.clear();
                UserInfo userInfo = PrefKit.getUserInfo(this);

                for (int i = 0; i < notiArray.length(); i++) {
                    JSONObject noti_list = notiArray.getJSONObject(i);
                    Log.e(TAG, "noti_list: " + noti_list);
                    if (noti_list != null) {
                        NotiDash notidash = new NotiDash();
                        notidash.PUSH_TYPE = noti_list.optString("PUSH_TYPE");
                        notidash.CODE_NAME = noti_list.optString("CODE_NAME");
                        notidash.NotiCnt = noti_list.optString("NotiCnt");

                        if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
                            if ("7".equals(noti_list.optString("PUSH_TYPE")) || "8".equals(noti_list.optString("PUSH_TYPE")) || "9".equals(noti_list.optString("PUSH_TYPE")) || "11".equals(noti_list.optString("PUSH_TYPE"))) {
                                mNotiDash_Items.add(notidash);
                            }
                        } else {
                            mNotiDash_Items.add(notidash);
                        }
                    }
                }
                mAdapterNotiDash.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }
    // 알림 현황판 End

    // 채권 현황판 Start
    private void setRec(String rec, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray recArray = new JSONArray(rec);
                mRecDash_Items.clear();
                for (int i = 0; i < recArray.length(); i++) {
                    JSONObject rec_list = recArray.getJSONObject(i);
                    Log.e(TAG, "rec_list: " + rec_list);
                    if (rec_list != null) {
                        RecDash recdash = new RecDash();
                        recdash.FAIR_NAME = rec_list.optString("FAIR_NAME");
                        recdash.TotalRecCompanyCnt = rec_list.optString("TotalRecCompanyCnt");
                        recdash.TotalRecAmt = rec_list.optString("TotalRecAmt");
                        recdash.MyRecCompanyCnt = rec_list.optString("MyRecCompanyCnt");
                        recdash.MyRecAmt = rec_list.optString("MyRecAmt");
                        if (i <= 1) {   //시연용 임시 수정사항 Start
                            mRecDash_Items.add(recdash);
                        }   //시연용 임시 수정사항 End
                    }
                }
                mAdapterRecDash.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_briefcase) {
            Kit.startActivity(this, WorkOutListActivity.class);
        } else if (itemId == R.id.nav_report) {
            Kit.startActivity(this, ReportActivity.class);
        } else if (itemId == R.id.nav_calendar) {
            Kit.startActivity(this, ActionPlanListActivity.class);
        } else if (itemId == R.id.nav_qr_code) {
            Kit.startActivity(this, BarcodeActivity.class);
        } else if (itemId == R.id.nav_name_card) {
            Kit.startActivity(this, NameCardListActivity.class);
        } else if (itemId == R.id.nav_logout) {
            try {
                AlertDialog.Builder alert_Logout = new AlertDialog.Builder(this);
                alert_Logout.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("로그아웃",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PrefKit.setUserInfo(MainActivity.this, null);
                                Kit.startActivity(MainActivity.this, LoginActivity.class);
                                finish();
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = alert_Logout.create();
                alert.show();
                Button positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.color_text_error));
                negativeButton.setTextColor(getResources().getColor(R.color.color_text_high));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 알림 현황판 Start
    public class RecycleAdapterNoti extends RecyclerView.Adapter<MainActivity.RecycleAdapterNoti.ItemViewHolder> {
        private Context mContext;
        private List<NotiDash> mNotiDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterNoti(Context context, List<NotiDash> itemList) {
            this.mContext = context;
            this.mNotiDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterNoti.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.noti_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterNoti.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterNoti.ItemViewHolder holder, int position) {
            NotiDash notidash = mNotiDash_Items.get(position);

            holder.noti_title.setText(notidash.CODE_NAME);
            holder.noti_cnt.setText(notidash.NotiCnt);

            if (notidash.PUSH_TYPE.equals("1")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_call_title));
            } else if (notidash.PUSH_TYPE.equals("2")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_deposit_title));
            } else if (notidash.PUSH_TYPE.equals("3")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_attend_title));
            } else if (notidash.PUSH_TYPE.equals("4")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_attend_chagne_title));
            } else if (notidash.PUSH_TYPE.equals("5")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_assign_title));
            } else if (notidash.PUSH_TYPE.equals("6")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_assign_change_title));
            } else if (notidash.PUSH_TYPE.equals("7")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_share_title));
            } else if (notidash.PUSH_TYPE.equals("8")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ic_comments));
            } else if (notidash.PUSH_TYPE.equals("9")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ic_notice));
            } else if (notidash.PUSH_TYPE.equals("10")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ico_tax_title));
            } else if (notidash.PUSH_TYPE.equals("11")) {
                holder.noti_img.setImageDrawable(getDrawable(R.drawable.ic_report));
            }

            if (mOnClickListener != null) {
                holder.noti_layout.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mNotiDash_Items.size();
            noti_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterNoti.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterNoti mAdapterNotiDash;
            public LinearLayout noti_layout;
            public TextView noti_title;
            public TextView noti_cnt;
            public ImageView noti_img;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterNoti mAdapter) {
                super(itemView);

                this.mAdapterNotiDash = mAdapter;
                this.noti_layout = itemView.findViewById(R.id.noti_layout);
                this.noti_title = itemView.findViewById(R.id.noti_title);
                this.noti_cnt = itemView.findViewById(R.id.noti_cnt);
                this.noti_img = itemView.findViewById(R.id.noti_img);
                noti_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterNotiDash.onItemHolderClick(this);
            }
        }
    }
    // 알림 현황판 End

    // 채권 현황판 Start
    public class RecycleAdapterRec extends RecyclerView.Adapter<MainActivity.RecycleAdapterRec.ItemViewHolder> {
        private Context mContext;
        private List<RecDash> mRecDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterRec(Context context, List<RecDash> itemList) {
            this.mContext = context;
            this.mRecDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterRec.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.rec_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterRec.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterRec.ItemViewHolder holder, int position) {
            RecDash recdash = mRecDash_Items.get(position);

            Resources res = getResources();
            holder.rec_layout_title.setText(recdash.FAIR_NAME);
            holder.rec_tot_cnt_1.setText(recdash.TotalRecCompanyCnt);
            holder.rec_tot_cnt_2.setText(String.format(res.getString(R.string.str_rec_tot_cnt), recdash.TotalRecAmt.replace("null", "0")));
            holder.rec_my_cnt_1.setText(recdash.MyRecCompanyCnt);
            holder.rec_my_cnt_2.setText(String.format(res.getString(R.string.str_rec_my_cnt), recdash.MyRecAmt.replace("null", "0")));

            if (mOnClickListener != null) {
                holder.rec_layout.setOnClickListener(mOnClickListener);
            }

            UserInfo userInfo = PrefKit.getUserInfo(MainActivity.this);
            if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
                holder.rec_my_layout.setVisibility(View.INVISIBLE);
            } else {
                holder.rec_my_layout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            int count = mRecDash_Items.size();
            rec_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterRec.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterRec mAdapterRecDash;
            public LinearLayout rec_layout;
            public LinearLayout rec_my_layout;
            public TextView rec_layout_title;
            public TextView rec_tot_cnt_1;
            public TextView rec_tot_cnt_2;
            public TextView rec_my_cnt_1;
            public TextView rec_my_cnt_2;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterRec mAdapter) {
                super(itemView);

                this.mAdapterRecDash = mAdapter;
                this.rec_layout = itemView.findViewById(R.id.rec_layout);
                this.rec_my_layout = itemView.findViewById(R.id.rec_my_layout);
                this.rec_layout_title = itemView.findViewById(R.id.rec_layout_title);
                this.rec_tot_cnt_1 = itemView.findViewById(R.id.rec_tot_cnt_1);
                this.rec_tot_cnt_2 = itemView.findViewById(R.id.rec_tot_cnt_2);
                this.rec_my_cnt_1 = itemView.findViewById(R.id.rec_my_cnt_1);
                this.rec_my_cnt_2 = itemView.findViewById(R.id.rec_my_cnt_2);
                rec_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterRecDash.onItemHolderClick(this);
            }
        }
    }
    // 채권 현황판 End

    // 배정 현황판 Start
    private void setAssign(String assign, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray assignArray = new JSONArray(assign);
                mAssignDash_Items.clear();
                for (int i = 0; i < assignArray.length(); i++) {
                    JSONObject assign_list = assignArray.getJSONObject(i);
                    Log.e(TAG, "assign_list: " + assign_list);
                    if (assign_list != null) {
                        AssignDash assigndash = new AssignDash();
                        assigndash.FAIR_NAME = assign_list.optString("FAIR_NAME");
                        assigndash.TotalAssignRate = assign_list.optString("TotalAssignRate");
                        assigndash.MyAssignRate = assign_list.optString("MyAssignRate");
                        mAssignDash_Items.add(assigndash);
                    }
                }
                mAdapterAssignDash.notifyDataSetChanged();
                findViewById(R.id.assign_tot_my).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            findViewById(R.id.assign_tot_my).setVisibility(View.GONE);
        }
    }

    public class RecycleAdapterAssign extends RecyclerView.Adapter<MainActivity.RecycleAdapterAssign.ItemViewHolder> {
        private Context mContext;
        private List<AssignDash> mAssignDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterAssign(Context context, List<AssignDash> itemList) {
            this.mContext = context;
            this.mAssignDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterAssign.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.assign_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterAssign.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterAssign.ItemViewHolder holder, int position) {
            AssignDash assigndash = mAssignDash_Items.get(position);

            Resources res = getResources();
            holder.assign_layout_title.setText(assigndash.FAIR_NAME);
            holder.assign_tot_cnt.setText(String.format(res.getString(R.string.str_assign_tot_cnt), assigndash.TotalAssignRate.replace("null", "0")));
            holder.assign_my_cnt.setText(String.format(res.getString(R.string.str_assign_my_cnt), assigndash.MyAssignRate.replace("null", "0")));
            if (mOnClickListener != null) {
                holder.assign_layout.setOnClickListener(mOnClickListener);
            }

            UserInfo userInfo = PrefKit.getUserInfo(MainActivity.this);
            if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
                holder.assign_my_cnt.setVisibility(View.GONE);
                findViewById(R.id.assign_my_title).setVisibility(View.GONE);
            } else {
                holder.assign_my_cnt.setVisibility(View.VISIBLE);
                findViewById(R.id.assign_my_title).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            int count = mAssignDash_Items.size();
            assign_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterAssign.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterAssign mAdapterAssignDash;
            public LinearLayout assign_layout;
            public TextView assign_layout_title;
            public TextView assign_tot_cnt;
            public TextView assign_my_cnt;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterAssign mAdapter) {
                super(itemView);

                this.mAdapterAssignDash = mAdapter;
                this.assign_layout = itemView.findViewById(R.id.assign_layout);
                this.assign_layout_title = itemView.findViewById(R.id.assign_layout_title);
                this.assign_tot_cnt = itemView.findViewById(R.id.assign_tot_cnt);
                this.assign_my_cnt = itemView.findViewById(R.id.assign_my_cnt);
                assign_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterAssignDash.onItemHolderClick(this);
            }
        }
    }
    // 배정 현황판 End

    // 영업 현황판 Start
    private void setSales(String sales, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray salesArray = new JSONArray(sales);
                mSalesDash_Items.clear();
                for (int i = 0; i < salesArray.length(); i++) {
                    JSONObject sales_list = salesArray.getJSONObject(i);
                    Log.e(TAG, "sales_list: " + sales_list);
                    if (sales_list != null) {
                        SalesDash salesdash = new SalesDash();
                        salesdash.FAIR_NAME = sales_list.optString("FAIR_NAME");
                        salesdash.TotalAttendCompanyCnt = sales_list.optString("TotalAttendCompanyCnt");
                        salesdash.TotalAttendBoothCnt = sales_list.optString("TotalAttendBoothCnt");
                        salesdash.MyAttendCompanyCnt = sales_list.optString("MyAttendCompanyCnt");
                        salesdash.MyAttendBoothCnt = sales_list.optString("MyAttendBoothCnt");
                        mSalesDash_Items.add(salesdash);
                    }
                }
                mAdapterSalesDash.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    public class RecycleAdapterSales extends RecyclerView.Adapter<MainActivity.RecycleAdapterSales.ItemViewHolder> {
        private Context mContext;
        private List<SalesDash> mSalesDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterSales(Context context, List<SalesDash> itemList) {
            this.mContext = context;
            this.mSalesDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterSales.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.sales_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterSales.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterSales.ItemViewHolder holder, int position) {
            SalesDash salesdash = mSalesDash_Items.get(position);

            holder.sales_layout_title.setText(salesdash.FAIR_NAME);
            holder.sales_tot_cnt_1.setText(salesdash.TotalAttendCompanyCnt);
            holder.sales_tot_cnt_2.setText(salesdash.TotalAttendBoothCnt);
            holder.sales_my_cnt_1.setText(salesdash.MyAttendCompanyCnt);
            holder.sales_my_cnt_2.setText(salesdash.MyAttendBoothCnt);
            if (mOnClickListener != null) {
                holder.sales_layout.setOnClickListener(mOnClickListener);
            }

            UserInfo userInfo = PrefKit.getUserInfo(MainActivity.this);
            if (userInfo != null && Integer.parseInt(userInfo.POSITION_ID) <= 2) {
                holder.sales_my_layout.setVisibility(View.INVISIBLE);
            } else {
                holder.sales_my_layout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            int count = mSalesDash_Items.size();
            sales_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterSales.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterSales mAdapterSalesDash;
            public LinearLayout sales_layout;
            public LinearLayout sales_my_layout;
            public TextView sales_layout_title;
            public TextView sales_tot_cnt_1;
            public TextView sales_tot_cnt_2;
            public TextView sales_my_cnt_1;
            public TextView sales_my_cnt_2;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterSales mAdapter) {
                super(itemView);

                this.mAdapterSalesDash = mAdapter;
                this.sales_layout = itemView.findViewById(R.id.sales_layout);
                this.sales_my_layout = itemView.findViewById(R.id.sales_my_layout);
                this.sales_layout_title = itemView.findViewById(R.id.sales_layout_title);
                this.sales_tot_cnt_1 = itemView.findViewById(R.id.sales_tot_cnt_1);
                this.sales_tot_cnt_2 = itemView.findViewById(R.id.sales_tot_cnt_2);
                this.sales_my_cnt_1 = itemView.findViewById(R.id.sales_my_cnt_1);
                this.sales_my_cnt_2 = itemView.findViewById(R.id.sales_my_cnt_2);
                sales_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterSalesDash.onItemHolderClick(this);
            }
        }
    }
    // 영업 현황판 End

    // 행사 현황판 Start
    private void setFair(String fair, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray fairArray = new JSONArray(fair);
                mFairDash_Items.clear();
                for (int i = 0; i < fairArray.length(); i++) {
                    JSONObject fair_list = fairArray.getJSONObject(i);
                    Log.e(TAG, "fair_list: " + fair_list);
                    if (fair_list != null) {
                        FairDash fairdash = new FairDash();
                        fairdash.FAIR_NAME = fair_list.optString("FAIR_NAME");
                        fairdash.FAIR_STR_DATE = fair_list.optString("FAIR_STR_DATE");
                        fairdash.FAIR_END_DATE = fair_list.optString("FAIR_END_DATE");
                        fairdash.TotalFairAmt = convertCurrencyStr(Double.parseDouble(fair_list.optString("TotalFairAmt").replace("null", "0")));
                        mFairDash_Items.add(fairdash);
                    }
                }
                mAdapterFairDash.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    public class RecycleAdapterFair extends RecyclerView.Adapter<MainActivity.RecycleAdapterFair.ItemViewHolder> {
        private Context mContext;
        private List<FairDash> mFairDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterFair(Context context, List<FairDash> itemList) {
            this.mContext = context;
            this.mFairDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterFair.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.fair_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterFair.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterFair.ItemViewHolder holder, int position) {
            FairDash fairdash = mFairDash_Items.get(position);

            holder.fair_layout_title.setText(fairdash.FAIR_NAME);
            holder.fair_tot_amt.setText(fairdash.TotalFairAmt);

            long now = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date(now);
            Date fairDate = null;
            String getDate = sdf.format(date);
            try {
                date = sdf.parse(getDate);
                fairDate = sdf.parse(fairdash.FAIR_END_DATE);

                int compare = date.compareTo(fairDate);
                if (compare > 0) {
                    holder.fair_layout_title.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                    holder.fair_tot_amt.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                    holder.fair_tot_text.setTextColor(getResources().getColor(R.color.color_9A9A9A));
                } else {
                    holder.fair_layout_title.setTextColor(getResources().getColor(R.color.color_class_event));
                    holder.fair_tot_amt.setTextColor(getResources().getColor(R.color.color_class_event));
                    holder.fair_tot_text.setTextColor(getResources().getColor(R.color.color_class_event));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mOnClickListener != null) {
                holder.fair_layout.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mFairDash_Items.size();
            fair_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterFair.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterFair mAdapterFairDash;
            public LinearLayout fair_layout;
            public TextView fair_layout_title;
            public TextView fair_tot_amt;
            public TextView fair_tot_text;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterFair mAdapter) {
                super(itemView);

                this.mAdapterFairDash = mAdapter;
                this.fair_layout = itemView.findViewById(R.id.fair_layout);
                this.fair_layout_title = itemView.findViewById(R.id.fair_layout_title);
                this.fair_tot_amt = itemView.findViewById(R.id.fair_tot_amt);
                this.fair_tot_text = itemView.findViewById(R.id.fair_tot_text);
                fair_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterFairDash.onItemHolderClick(this);
            }
        }
    }
    // 행사 현황판 End

    // 사전등록 현황판 Start
    private void setPreregistration(String preregistration, String cnt) {
        if (!"0".equals(cnt)) {
            try {
                JSONArray preregistrationArray = new JSONArray(preregistration);
                mPreregistrationDash_Items.clear();
                for (int i = 0; i < preregistrationArray.length(); i++) {
                    JSONObject preregistration_list = preregistrationArray.getJSONObject(i);
                    Log.e(TAG, "Preregistration_list: " + preregistration_list);
                    if (preregistration_list != null) {
                        PreregistrationDash preregistrationdash = new PreregistrationDash();
                        preregistrationdash.FAIR_NAME = preregistration_list.optString("FAIR_NAME");
                        preregistrationdash.TotalPreregistrationCnt = convertCurrencyStr(Double.parseDouble(preregistration_list.optString("NEW_PRE_VISITOR_TOT_CNT").replace("null", "0")));
                        if (i <= 3) {
                            mPreregistrationDash_Items.add(preregistrationdash);
                        }
                    }
                }
                mAdapterPreregistrationDash.notifyDataSetChanged();
                if (MyApplication.Notification_Check == true) {
                    Kit.startActivity(this, NoticeActivity.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    public class RecycleAdapterPreregistration extends RecyclerView.Adapter<MainActivity.RecycleAdapterPreregistration.ItemViewHolder> {
        private Context mContext;
        private List<PreregistrationDash> mPreregistrationDash_Items;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapterPreregistration(Context context, List<PreregistrationDash> itemList) {
            this.mContext = context;
            this.mPreregistrationDash_Items = itemList;
        }

        @Override
        public MainActivity.RecycleAdapterPreregistration.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.preregistration_dash_list_item, parent, false);
            return new MainActivity.RecycleAdapterPreregistration.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(MainActivity.RecycleAdapterPreregistration.ItemViewHolder holder, int position) {
            PreregistrationDash preregistrationdash = mPreregistrationDash_Items.get(position);

            holder.preregistration_layout_title.setText(preregistrationdash.FAIR_NAME);
            holder.preregistration_tot_amt.setText(preregistrationdash.TotalPreregistrationCnt.replaceAll(" ", ""));

//            if (position == 0 || position == 1) {
//                holder.preregistration_layout_title.setTextColor(getResources().getColor(R.color.color_class_event));
//                holder.preregistration_tot_amt.setTextColor(getResources().getColor(R.color.color_class_event));
//                holder.preregistration_tot_text.setTextColor(getResources().getColor(R.color.color_class_event));
//            } else {
//                holder.preregistration_layout_title.setTextColor(getResources().getColor(R.color.color_9A9A9A));
//                holder.preregistration_tot_amt.setTextColor(getResources().getColor(R.color.color_9A9A9A));
//                holder.preregistration_tot_text.setTextColor(getResources().getColor(R.color.color_9A9A9A));
//            }

            if (mOnClickListener != null) {
                holder.preregistration_layout.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            int count = mPreregistrationDash_Items.size();
            preregistration_Empty.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        private void onItemHolderClick(MainActivity.RecycleAdapterPreregistration.ItemViewHolder itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public MainActivity.RecycleAdapterPreregistration mAdapterPreregistrationDash;
            public LinearLayout preregistration_layout;
            public TextView preregistration_layout_title;
            public TextView preregistration_tot_amt;
            public TextView preregistration_tot_text;

            public ItemViewHolder(View itemView, MainActivity.RecycleAdapterPreregistration mAdapter) {
                super(itemView);

                this.mAdapterPreregistrationDash = mAdapter;
                this.preregistration_layout = itemView.findViewById(R.id.preregistration_layout);
                this.preregistration_layout_title = itemView.findViewById(R.id.preregistration_layout_title);
                this.preregistration_tot_amt = itemView.findViewById(R.id.preregistration_tot_amt);
                this.preregistration_tot_text = itemView.findViewById(R.id.preregistration_tot_text);
                preregistration_layout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                mAdapterPreregistrationDash.onItemHolderClick(this);
            }
        }
    }
    // 사전등록 현황판 End

    private void showSecuritySnackbar() {
        Snackbar snack = Snackbar.make(main_layout, "회사정보 유출 금지!!\n회사 내부의 소중한 정보를 보호해 주세요.", Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView msg = view.findViewById(com.google.android.material.R.id.snackbar_text);
        msg.setTextSize(15);
        msg.setMaxLines(2);
        msg.setTextColor(getResources().getColor(R.color.color_text_error));
        snack.show();
    }
}
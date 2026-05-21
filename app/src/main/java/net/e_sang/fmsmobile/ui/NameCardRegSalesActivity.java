package net.e_sang.fmsmobile.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.CompanyInfo;
import net.e_sang.fmsmobile.data.CompanyStaffInfo;
import net.e_sang.fmsmobile.data.ConventionCode;
import net.e_sang.fmsmobile.data.Extra;
import net.e_sang.fmsmobile.data.MasterCode;
import net.e_sang.fmsmobile.data.NameCardList;
import net.e_sang.fmsmobile.data.RegFairList;
import net.e_sang.fmsmobile.data.RegReceivableAmountList;
import net.e_sang.fmsmobile.data.RegStaffList;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.data.WorkGroupCode;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.namecard.ContactBottomSheet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class NameCardRegSalesActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private CompanyInfo mCompanyInfo = new CompanyInfo();
    private CompanyStaffInfo mCompanyStaffInfo = new CompanyStaffInfo();
    private LinearLayout progressBar_layout = null;

    private TabLayout mTabLayout = null;
    private ScrollableViewPager mViewPager = null;
    private ViewPagerAdapter mViewPagerAdapter = null;
    private String[] mTabTitles = {
            "영업세부정보",
            "전시참가이력",
            "업체정보",
            //        "시설정보",
    };

    Hashtable<String, String> WorkGroup = new Hashtable<String, String>();

    private ArrayList<RegStaffList> mRegStaffListItems = new ArrayList<>();
    private ArrayList<RegFairList> mRegFairListItems = new ArrayList<>();
    private ArrayList<MasterCode> mMasterCodeItems = new ArrayList<>();
    private TextView txtName, txtBrandName, txtClass = null;


    private String entry_path = "";
    private SingleDateAndTimePickerDialog.Builder mSingleDateAndTimePickerDialog = null;
    private android.app.AlertDialog.Builder dialogBuilder = null;
    private android.app.AlertDialog alertDialog = null;
    private RecyclerView mRecyclerView = null;
    private NameCardRegSalesActivity.RecycleAdapter mAdapter = null;

    private TextView txtToolbarTitle = null;

    private NameCardList nameCardList = null;
    private String TYPE = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_reg_sales);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);

        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);

        txtName = findViewById(R.id.txtName);
        txtBrandName = findViewById(R.id.txtBrandName);
        txtClass = findViewById(R.id.txtClass);


        progressBar_layout = findViewById(R.id.progressBar_layout);

        WorkGroup.put("", "");

        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_name_card_company_view);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            entry_path = intent.getExtras().getString("entry_path");
            TYPE = getIntent().getExtras().getString("TYPE");
            nameCardList = getIntent().getParcelableExtra("NameCard");
            if (TYPE.equals("company")) {
                mCompanyInfo = getIntent().getParcelableExtra(Extra.KEY_COMPANY_INFO);
                Log.e(TAG, "mCompanyInfo : " + mCompanyInfo.COMPANY_ID);
            } else {
                mCompanyStaffInfo = getIntent().getParcelableExtra(Extra.KEY_COMPANY_STAFF_INFO);
                assert mCompanyStaffInfo != null;
                Log.e(TAG, "mCompanyStaffInfo : " + mCompanyStaffInfo.COMPANY_ID);
                Log.e(TAG, "mCompanyStaffInfo : " + mCompanyStaffInfo.COMPANY_STAFF_ID);
            }

            Log.e(TAG, "entry_path : " + entry_path);
            Log.e(TAG, "TYPE : " + TYPE);
            Log.e(TAG, "nameCardList : " + nameCardList.name);
            Log.e(TAG, "nameCardList : " + nameCardList.image);
        }

        mTabLayout.removeAllTabs();
        for (int i = 0; i < mTabTitles.length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.layout_tab, null);
            TextView txtTabTitle = view.findViewById(R.id.txtTabTitle);
            txtTabTitle.setText(mTabTitles[i]);
            TabLayout.Tab tab = mTabLayout.newTab();
            tab.setCustomView(view);
            mTabLayout.addTab(tab);
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mViewPager.setCurrentItem(position);
                Kit.hideSoftKeyboard(NameCardRegSalesActivity.this);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCanScroll(false);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                mTabLayout.setCurrentTab(position);
                TabLayout.Tab tab = mTabLayout.getTabAt(position);
                tab.select();

                Kit.hideSoftKeyboard(NameCardRegSalesActivity.this);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mCompanyStaffInfo != null && TYPE.equals("phone")) {
            mViewPagerAdapter.addFragment(NameCardSalesDetailFragment.getInstance(mCompanyInfo, mCompanyStaffInfo, nameCardList, String.valueOf(mCompanyStaffInfo.COMPANY_STAFF_ID)), mTabTitles[0]);
        } else {
            mViewPagerAdapter.addFragment(NameCardSalesDetailFragment.getInstance(mCompanyInfo, mCompanyStaffInfo, nameCardList, ""), mTabTitles[0]);
        }
//        mViewPagerAdapter.addFragment(NameCardSalesDetailFragment.getInstance(mCompanyInfo, nameCardList), mTabTitles[0]);
        mViewPagerAdapter.addFragment(FairHistoryFragment.getInstance(mCompanyInfo), mTabTitles[1]);
        mViewPagerAdapter.addFragment(CompanyBasicInfoFragment.getInstance(mCompanyInfo), mTabTitles[2]);
        // mViewPagerAdapter.addFragment(InfrastructureFragment.getInstance(mCompanyInfo), mTabTitles[3]);
        mViewPagerAdapter.notifyDataSetChanged();

        load(true);
        //GetCompanyStaffAll();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSingleDateAndTimePickerDialog != null) {
                    mSingleDateAndTimePickerDialog.close();
                    mSingleDateAndTimePickerDialog = null;
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
//        if (id == R.id.btn_company_connection_layout) {
//            Toast.makeText(this, "기업 연결 : " + nameCardList.company, Toast.LENGTH_SHORT).show();
//        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    protected void load(boolean init) {
        if (init) {
            mRegStaffListItems.clear();
            mRegFairListItems.clear();
            mMasterCodeItems.clear();
        }

        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            String FAIR_ID = "";
//            Kit.log(Kit.LogType.VALUE, "load::FAIR_ID = " + FAIR_ID);
            //String body = String.format("system_id=%s&company_id=%s&fair_id=%s&entry_path=%s", userInfo.SYS_ID, mCompanyInfo.COMPANY_ID, FAIR_ID, entry_path);
            HashMap<String, String> body = new HashMap<>();
            body.put("system_id", userInfo.SYS_ID);
            if (TYPE.equals("company")) {
                body.put("company_id", String.valueOf(mCompanyInfo.COMPANY_ID));
            } else {
                body.put("company_id", String.valueOf(mCompanyStaffInfo.COMPANY_ID));
            }
            body.put("fair_id", FAIR_ID);
            body.put("entry_path", entry_path);
            int requestCode = init ? 1 : 0;
            new TelKit(this, this, progressBar_layout).request(TelKit.URL_API_GET_REG_COMPANY_INFO, body, requestCode);

            //body = String.format("master_grp_type=%s&master_code_group_id=%s", "TM_GRADE", "");
            body = new HashMap<>();
            body.put("master_grp_type", "TM_GRADE");
            body.put("master_code_group_id", "");
            new TelKit(this, this).request(TelKit.URL_API_MASTER_CODE, body);
        } else {
            Kit.showAlertDialog(this, "", getResources().getString(R.string.str_error_generic), "확인");
        }
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_REG_COMPANY_INFO)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONObject data = json.optJSONObject("data");
                        if (data != null) {
                            JSONObject companyinfo = data.optJSONObject("companyInfo");
                            if (companyinfo != null) {
                                mCompanyInfo.COMPANY_ID = companyinfo.optInt("COMPANY_ID");
                                mCompanyInfo.COMPANY_NAME = companyinfo.optString("COMPANY_NAME");
                                mCompanyInfo.BRAND_NAME = companyinfo.optString("BRAND_NAME");
                                mCompanyInfo.BIZ_NO = companyinfo.optString("BIZ_NO");
                                mCompanyInfo.FAIR_STATUS_DESC = companyinfo.optString("FAIR_STATUS_DESC");
                                mCompanyInfo.CATEGORY_DESC = companyinfo.optString("CATEGORY_DESC");
                                mCompanyInfo.DISPLAY_ITEMS = companyinfo.optString("DISPLAY_ITEMS");
                                mCompanyInfo.CEO_NAME = companyinfo.optString("CEO_NAME");
                                mCompanyInfo.STAFF_NAME = companyinfo.optString("STAFF_NAME");
                                mCompanyInfo.STAFF_MOBILE = companyinfo.optString("STAFF_MOBILE");
                                mCompanyInfo.ROLE = companyinfo.optInt("ROLE");
                                mCompanyInfo.ZIP_CODE = companyinfo.optString("ZIP_CODE");
                                mCompanyInfo.ADDR = companyinfo.optString("ADDR");
                                mCompanyInfo.ADDR_DETAIL = companyinfo.optString("ADDR_DETAIL");
                                mCompanyInfo.FAIR_HISTORY = companyinfo.optString("FAIR_HISTORY");
                                mCompanyInfo.USER_ID = companyinfo.optString("USER_ID");
                                mCompanyInfo.PASSWORD = companyinfo.optString("PASSWORD");
                                mCompanyInfo.CONTENT = companyinfo.optString("CONTENT");
                                mCompanyInfo.INFLOW_PATH = companyinfo.optInt("INFLOW_PATH");
                                mCompanyInfo.EMAIL = companyinfo.optString("EMAIL");
                                mCompanyInfo.FAX = companyinfo.optString("FAX");
                                mCompanyInfo.HOMEPAGE = companyinfo.optString("HOMEPAGE");
                                mCompanyInfo.TEL_NO = companyinfo.optString("TEL_NO");
                                mCompanyInfo.RCV_AMT = companyinfo.optInt("RCV_AMT");
                                mCompanyInfo.RCV_COLLECT_DATE = companyinfo.optString("RCV_COLLECT_DATE");

                                txtName.setText(mCompanyInfo.COMPANY_NAME);
                                if (Kit.isNotNullNotEmpty(mCompanyInfo.BRAND_NAME)) {
                                    txtBrandName.setText("[" + mCompanyInfo.BRAND_NAME + "]");
                                    txtBrandName.setVisibility(VISIBLE);
                                } else {
                                    txtBrandName.setVisibility(GONE);
                                }
                                String FAIR_STATUS_DESC = companyinfo.optString("FAIR_STATUS_DESC");
                                if (Kit.isNotNullNotEmpty(FAIR_STATUS_DESC)) {
                                    txtClass.setText(FAIR_STATUS_DESC);
                                } else {
                                    txtClass.setText("미등록");
                                }
                            }

                            mRegStaffListItems.clear();
                            mCompanyInfo.regStaffLists.clear();

                            JSONArray staffList = data.optJSONArray("staffList");
                            if (staffList != null) {
                                ArrayList<RegStaffList> regStaffLists = new ArrayList<>();
                                for (int i = 0; i < staffList.length(); i++) {
                                    JSONObject stafflist_obj = staffList.optJSONObject(i);
                                    if (stafflist_obj != null) {
                                        RegStaffList regStaffList = new RegStaffList();
                                        regStaffList.TOT_CNT = stafflist_obj.optString("TOT_CNT");
                                        regStaffList.ROW_NO = stafflist_obj.optString("ROW_NO");
                                        regStaffList.COMPANY_STAFF_ID = stafflist_obj.optString("COMPANY_STAFF_ID");
                                        regStaffList.STAFF_DEPT = stafflist_obj.optString("STAFF_DEPT");
                                        regStaffList.STAFF_POSITION = stafflist_obj.optString("STAFF_POSITION");
                                        regStaffList.STAFF_NAME = stafflist_obj.optString("STAFF_NAME");
                                        regStaffList.STAFF_MOBILE = stafflist_obj.optString("STAFF_MOBILE");
                                        regStaffList.STAFF_EMAIL = stafflist_obj.optString("STAFF_EMAIL");
                                        regStaffList.STAFF_PHONE = stafflist_obj.optString("STAFF_PHONE");
                                        regStaffList.STAFF_ROLEs = stafflist_obj.optString("STAFF_ROLEs");
                                        regStaffList.STAFF_ROLEs_DESC = stafflist_obj.optString("STAFF_ROLEs_DESC");
                                        regStaffList.FAIR_DESC = stafflist_obj.optString("FAIR_DESC");
                                        regStaffList.FAIR_NAME = stafflist_obj.optString("FAIR_NAME");
                                        regStaffList.CREATE_DATE = stafflist_obj.optString("CREATE_DATE");
                                        regStaffList.UPDATE_DATE = stafflist_obj.optString("UPDATE_DATE");
                                        regStaffLists.add(regStaffList);
                                    }
                                }
                                mRegStaffListItems.addAll(regStaffLists);
                                mCompanyInfo.regStaffLists.addAll(regStaffLists);
                            }

                            mRegFairListItems.clear();
                            RegFairList regFairList = new RegFairList();
                            regFairList.FAIR_NAME = "행사 선택";
                            mRegFairListItems.add(regFairList);
                            JSONArray fairList = data.optJSONArray("fairList");
//                            int selectedIdx = 0;
                            String FAIR_ID = "";
                            if (fairList != null) {
                                for (int i = 0; i < fairList.length(); i++) {
                                    JSONObject fairlist_obj = fairList.getJSONObject(i);
                                    if (fairlist_obj != null) {
                                        regFairList = new RegFairList();
                                        regFairList.FAIR_ID = fairlist_obj.optString("FAIR_ID");
                                        regFairList.FAIR_MASTER_ID = fairlist_obj.optString("FAIR_MASTER_ID");
                                        regFairList.FAIR_DESC = fairlist_obj.optString("FAIR_DESC");
                                        regFairList.FAIR_NAME = fairlist_obj.optString("FAIR_NAME");
                                        mRegFairListItems.add(regFairList);

//                                        if (!FAIR_ID.isEmpty() && FAIR_ID.equalsIgnoreCase(regFairList.FAIR_ID)) {
//                                            selectedIdx = i + 1;
//                                        }
                                    }
                                }
                            }
                            String[] events = new String[mRegFairListItems.size()];
                            for (int i = 0; i < mRegFairListItems.size(); i++) {
                                events[i] = mRegFairListItems.get(i).FAIR_NAME;
                            }
                            ArrayAdapter<String> adapterEvents = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, events);

                            Fragment fragment = mViewPagerAdapter.getItem(0);
                            if (fragment instanceof NameCardSalesDetailFragment) {
                                NameCardSalesDetailFragment nameCardSalesDetailFragment = (NameCardSalesDetailFragment) fragment;
                                nameCardSalesDetailFragment.mCompanyInfo = mCompanyInfo;
                                if (mCompanyStaffInfo != null && TYPE.equals("phone")) {
                                    nameCardSalesDetailFragment.load(true, "", String.valueOf(mCompanyStaffInfo.COMPANY_STAFF_ID));
                                } else {
                                    nameCardSalesDetailFragment.load(true, "", "");
                                }
                            }

                            fragment = mViewPagerAdapter.getItem(1);
                            if (fragment instanceof FairHistoryFragment) {
                                FairHistoryFragment fairHistoryFragment = (FairHistoryFragment) fragment;
                                fairHistoryFragment.mCompanyInfo = mCompanyInfo;
                                fairHistoryFragment.load(true);
                            }

                            fragment = mViewPagerAdapter.getItem(2);
                            if (fragment instanceof CompanyBasicInfoFragment) {
                                CompanyBasicInfoFragment companyBasicInfoFragment = (CompanyBasicInfoFragment) fragment;
                                companyBasicInfoFragment.mCompanyInfo = mCompanyInfo;
                                companyBasicInfoFragment.load();
                            }

                            if (mViewPagerAdapter != null) {
                                mViewPagerAdapter.notifyDataSetChanged();
                            }
                            if (result.mRequestCode == 1) {                 // init
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTabLayout.getTabAt(0).select();
                                        Kit.hideSoftKeyboard(NameCardRegSalesActivity.this);
                                    }
                                }, 1000);
                            }
                        }
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_MASTER_CODE)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        JSONArray list = json.optJSONArray("list");
                        if (list != null) {
                            String[] salesClasses = new String[list.length()];
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject fairlist_obj = list.getJSONObject(i);
                                if (fairlist_obj != null) {
                                    MasterCode masterCode = new MasterCode();
                                    masterCode.VALUE = fairlist_obj.optString("VALUE");
                                    masterCode.DESC = fairlist_obj.optString("DESC");
                                    mMasterCodeItems.add(masterCode);

                                    salesClasses[i] = fairlist_obj.optString("DESC");
                                }
                                ArrayAdapter<String> adapterSalesClasses = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, salesClasses);
                            }
                        }
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_STAFF_ALL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        mRegStaffListItems.clear();
                        mCompanyInfo.regStaffLists.clear();

                        JSONArray staffList = json.optJSONArray("list");
                        if (staffList != null) {
                            ArrayList<RegStaffList> regStaffLists = new ArrayList<>();
                            for (int i = 0; i < staffList.length(); i++) {
                                JSONObject stafflist_obj = staffList.optJSONObject(i);
                                if (stafflist_obj != null) {
                                    RegStaffList regStaffList = new RegStaffList();
                                    regStaffList.TOT_CNT = stafflist_obj.optString("TOT_CNT");
                                    regStaffList.ROW_NO = stafflist_obj.optString("ROW_NO");
                                    regStaffList.FAIR_ID = stafflist_obj.optString("FAIR_ID");
                                    regStaffList.COMPANY_STAFF_ID = stafflist_obj.optString("COMPANY_STAFF_ID");
                                    regStaffList.STAFF_DEPT = stafflist_obj.optString("STAFF_DEPT");
                                    regStaffList.STAFF_POSITION = stafflist_obj.optString("STAFF_POSITION");
                                    regStaffList.STAFF_NAME = stafflist_obj.optString("STAFF_NAME");
                                    regStaffList.STAFF_MOBILE = stafflist_obj.optString("STAFF_MOBILE");
                                    regStaffList.STAFF_EMAIL = stafflist_obj.optString("STAFF_EMAIL");
                                    regStaffList.STAFF_PHONE = stafflist_obj.optString("STAFF_PHONE");
                                    regStaffList.STAFF_ROLEs = stafflist_obj.optString("STAFF_ROLEs");
                                    regStaffList.STAFF_ROLEs_DESC = stafflist_obj.optString("STAFF_ROLEs_DESC");
                                    regStaffList.FAIR_DESC = stafflist_obj.optString("FAIR_DESC");
                                    regStaffList.FAIR_NAME = stafflist_obj.optString("FAIR_NAME");
                                    regStaffList.CREATE_DATE = stafflist_obj.optString("CREATE_DATE");
                                    regStaffList.UPDATE_DATE = stafflist_obj.optString("UPDATE_DATE");
                                    regStaffLists.add(regStaffList);
                                }
                            }
                            mRegStaffListItems.addAll(regStaffLists);
                            mCompanyInfo.regStaffLists.addAll(regStaffLists);

                            Fragment fragment = mViewPagerAdapter.getItem(2);
                            if (fragment instanceof CompanyBasicInfoFragment) {
                                CompanyBasicInfoFragment companyBasicInfoFragment = (CompanyBasicInfoFragment) fragment;
                                companyBasicInfoFragment.mCompanyInfo = mCompanyInfo;
                                companyBasicInfoFragment.load();
                            }
                        }

                    } else {
                        Log.e(TAG, "URL_API_GET_COUNSEL msg :" + msg);
                        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
    }


    public void setTabEnabled(int tabIndex, boolean enable) {
        TabLayout.Tab tab = mTabLayout.getTabAt(tabIndex);
        TextView txtTabTitle = tab.getCustomView().findViewById(R.id.txtTabTitle);
        txtTabTitle.setEnabled(enable);
        View view = mTabLayout.getChildAt(0);
        if (view instanceof LinearLayout) {
            LinearLayout tabStrip = (LinearLayout) view;
            View tabView = tabStrip.getChildAt(tabIndex);
            if (enable) {
                tabView.setOnTouchListener(null);
            } else {
                tabView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }
        }
    }


    protected String getTag(View view) {
        String tag = "";
        Object obj = view.getTag();
        if (obj != null) {
            tag = (String) obj;
        }

        return tag;
    }

//    @Override
//    public void onBackPressed() {
//        if (mSingleDateAndTimePickerDialog != null) {
//            mSingleDateAndTimePickerDialog.close();
//            mSingleDateAndTimePickerDialog = null;
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<RegReceivableAmountList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<RegReceivableAmountList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_receivable_amount_row, parent, false);
            return new ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            RegReceivableAmountList regReceivableAmountList = mItemList.get(position);

            holder.fair_name.setText(regReceivableAmountList.FAIR_NAME);
            if (TextUtils.isEmpty(regReceivableAmountList.TOT_RECV_AMT)) {
                holder.tot_recv_atm.setText("- 원");
            } else {
                holder.tot_recv_atm.setText(convertCurrencyStr(Double.parseDouble(regReceivableAmountList.TOT_RECV_AMT)) + "원");
            }

            if (TextUtils.isEmpty(regReceivableAmountList.PAY_DUE_DATE)) {
                holder.pay_due_date.setText("-");
            } else {
                try {
                    SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date formatDate = dtFormat.parse(regReceivableAmountList.PAY_DUE_DATE);
                    String strNewDtFormat = newDtFormat.format(formatDate);
                    holder.pay_due_date.setText(strNewDtFormat);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public int getItemCount() {
            int count = mItemList.size();
            return count;
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        private void onItemHolderClick(ItemViewHolder
                                               itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RecycleAdapter mAdapter;
            public TextView fair_name;
            public TextView tot_recv_atm;
            public TextView pay_due_date;

            public ItemViewHolder(View itemView, RecycleAdapter mAdapter) {
                super(itemView);

                this.mAdapter = mAdapter;
                this.fair_name = itemView.findViewById(R.id.fair_name);
                this.tot_recv_atm = itemView.findViewById(R.id.tot_recv_atm);
                this.pay_due_date = itemView.findViewById(R.id.pay_due_date);
            }

            @Override
            public void onClick(View v) {
                mAdapter.onItemHolderClick(this);
                Log.e(TAG, "onClick");
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void GetCompanyStaffAll() {
        UserInfo userInfo = PrefKit.getUserInfo(this);

        if (userInfo != null) {
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE_VIEW_COUNT", "100");
            body.put("CURRENT_PAGE_INDEX", "1");
            body.put("COMPANY_ID", String.valueOf(mCompanyInfo.COMPANY_ID));
            new TelKit(this, this, progressBar_layout).request(TelKit.URL_API_GET_STAFF_ALL, body);
        }
    }
}
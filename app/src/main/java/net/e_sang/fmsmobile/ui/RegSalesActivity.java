package net.e_sang.fmsmobile.ui;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
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

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import net.e_sang.fmsmobile.BuildConfig;
import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.*;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;
import net.e_sang.fmsmobile.namecard.NameCardListActivity;

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

public class RegSalesActivity extends BaseActivity implements View.OnClickListener, TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private CompanyInfo mCompanyInfo = null;
    private BetterSpinner mSpinnerEvents = null;
    private BetterSpinner mSpinnerManagers = null;
    private BetterSpinner mSpinnerSalesClasses = null;
    private Spinner mSpinnerWorkGroupCode = null;
    private Spinner mSpinnerConventionCenter = null;
    private EditText mEditBoothNum = null;
    private TextInputEditText mEditContent = null;
    private EditText mEditDate = null;
    private CheckBox mBtnNCT = null;
    private CheckBox mBtnRecall = null;
    private Button mBtnRegister = null;
    private AppBarLayout appBarLayout = null;
    private LinearLayout progressBar_layout = null;

    private TabLayout mTabLayout = null;
    private ScrollableViewPager mViewPager = null;
    private ViewPagerAdapter mViewPagerAdapter = null;
    private String[] mTabTitles = {
            "영업세부정보",
            "전시참가이력",
            "업체정보",
            "시설정보",
    };

    Hashtable<String, String> WorkGroup = new Hashtable<String, String>();

    private ArrayList<RegStaffList> mRegStaffListItems = new ArrayList<>();
    private ArrayList<RegFairList> mRegFairListItems = new ArrayList<>();
    private ArrayList<RegFairList> mRegFairSearchListItems = new ArrayList<>();
    private ArrayList<MasterCode> mMasterCodeItems = new ArrayList<>();
    private ArrayList<WorkGroupCode> mWorkGroupCodeItems = new ArrayList<WorkGroupCode>();
    private ArrayList<ConventionCode> mConventionCodeItems = new ArrayList<>();
    private ArrayList<RegReceivableAmountList> mRegReceivableAmountListItems = new ArrayList<>();
    private TextView txtName, txtBrandName, txtClass, remainReceivableAmount = null;
    private RadioGroup rdogroupSalesType, rdogroupSalesTypeTwo, rdogroupWorkType = null;
    private TextView txtErrorSalesType = null;
    private TextView txtErrorEvents = null;
    private TextView txtErrorManagers = null;
    private TextView txtErrorSalesClass = null;
    private TextView txtErrorBoothNum = null;
    private TextView txtErrorContent = null;
    private TextView txtErrorRdogroupWorkType = null;
    private String entry_path = "";
    private SingleDateAndTimePickerDialog.Builder mSingleDateAndTimePickerDialog = null;
    private android.app.AlertDialog.Builder dialogBuilder = null;
    private android.app.AlertDialog alertDialog = null;
    private RecyclerView mRecyclerView = null;
    private RegSalesActivity.RecycleAdapter mAdapter = null;
    private CheckBox chkVoice = null;
    private TextView txtVoice = null;
    private SpeechRecognizer speechRecognizer;
    private Intent voiceIntent;
    boolean recording = false;  //현재 녹음중인지 여부
    private Button spinnerManagersAdd = null;
    public static final int REQUEST_MANAGER_ADD = 4001;  // 내 위치 정보 가져오기
    private TextView txtToolbarTitle = null;

    private String WORK_GROUP_CODE = "";
    private RadioButton mRdoWorkType_S = null;
    private RadioButton mRdoWorkType_E = null;

    private AlertDialog managerDialog;
    private AlertDialog fairListDialog;
    private static LinearLayout txtEmptyFair_layout = null;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validate();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_sales);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        mSpinnerEvents = findViewById(R.id.spinnerEvents);
        mSpinnerManagers = findViewById(R.id.spinnerManagers);
        mSpinnerSalesClasses = findViewById(R.id.spinnerSalesClasses);
        mSpinnerWorkGroupCode = findViewById(R.id.spinnerWorkGroupCode);
        mSpinnerConventionCenter = findViewById(R.id.spinnerConventionCenter);
        mEditBoothNum = findViewById(R.id.editBoothNum);
        mEditContent = findViewById(R.id.editContent);
        mEditDate = findViewById(R.id.editDate);
        mTabLayout = findViewById(R.id.tabLayout);
        mViewPager = findViewById(R.id.viewPager);
        mBtnNCT = findViewById(R.id.btnNCT);
        mBtnRecall = findViewById(R.id.btnRecall);
        txtName = findViewById(R.id.txtName);
        txtBrandName = findViewById(R.id.txtBrandName);
        txtClass = findViewById(R.id.txtClass);
        mBtnRegister = findViewById(R.id.btnRegister);
        rdogroupSalesType = findViewById(R.id.rdogroupSalesType);
        rdogroupSalesType.setOnCheckedChangeListener(listener1);
        rdogroupSalesTypeTwo = findViewById(R.id.rdogroupSalesTypeTwo);
        rdogroupSalesTypeTwo.setOnCheckedChangeListener(listener2);
        rdogroupWorkType = findViewById(R.id.rdogroupWorkType);
        rdogroupWorkType.setOnCheckedChangeListener(listener3);
        txtErrorSalesType = findViewById(R.id.txtErrorSalesType);
        txtErrorEvents = findViewById(R.id.txtErrorEvents);
        txtErrorManagers = findViewById(R.id.txtErrorManagers);
        txtErrorSalesClass = findViewById(R.id.txtErrorSalesClass);
        txtErrorBoothNum = findViewById(R.id.txtErrorBoothNum);
        txtErrorContent = findViewById(R.id.txtErrorContent);
        txtErrorRdogroupWorkType = findViewById(R.id.txtErrorRdogroupWorkType);
        appBarLayout = findViewById(R.id.appbar);
        progressBar_layout = findViewById(R.id.progressBar_layout);
        remainReceivableAmount = findViewById(R.id.remainReceivableAmount);
        chkVoice = findViewById(R.id.chkVoice);
        txtVoice = findViewById(R.id.txtVoice);
        spinnerManagersAdd = findViewById(R.id.spinnerManagersAdd);
        remainReceivableAmount.setPaintFlags(remainReceivableAmount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        remainReceivableAmount.setOnClickListener(this);
        spinnerManagersAdd.setOnClickListener(this);
        mSpinnerManagers.setFocusable(false);
        mSpinnerEvents.setFocusable(false);
        mSpinnerSalesClasses.setFocusable(false);
        mSpinnerWorkGroupCode.setFocusable(false);
        mSpinnerConventionCenter.setFocusable(false);
        mRdoWorkType_S = findViewById(R.id.rdoWorkType_S);
        mRdoWorkType_E = findViewById(R.id.rdoWorkType_E);
        mBtnRecall.setOnClickListener(this);

        WorkGroup.put("", "");
        mCompanyInfo = getIntent().getParcelableExtra(Extra.KEY_COMPANY_INFO);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText(R.string.str_title_sales_reg);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        mEditContent.setOnClickListener(this);
        mEditDate.setOnClickListener(this);
        mBtnRegister.setOnClickListener(this);

        mTabLayout.removeAllTabs();
        for (int i = 0; i < 4; i++) {
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
                Kit.hideSoftKeyboard(RegSalesActivity.this);
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

                Kit.hideSoftKeyboard(RegSalesActivity.this);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPagerAdapter.addFragment(SalesDetailFragment.getInstance(mCompanyInfo), mTabTitles[0]);
        mViewPagerAdapter.addFragment(FairHistoryFragment.getInstance(mCompanyInfo), mTabTitles[1]);
        mViewPagerAdapter.addFragment(CompanyBasicInfoFragment.getInstance(mCompanyInfo), mTabTitles[2]);
        mViewPagerAdapter.addFragment(InfrastructureFragment.getInstance(mCompanyInfo), mTabTitles[3]);
        mViewPagerAdapter.notifyDataSetChanged();

        // 행사
        mSpinnerEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    RegFairList regFairList = mRegFairListItems.get(position);
                    mSpinnerEvents.setTag(regFairList.FAIR_ID);
                } else {
                    mSpinnerEvents.setTag("");
                }
                String FAIR_ID = getTag(mSpinnerEvents);
//                Kit.log(Kit.LogType.VALUE, "setOnItemClickListener::FAIR_ID = " + FAIR_ID);
                load(false);
            }
        });

        mSpinnerEvents.setOnClickListener(v -> {
            showFairListDialog();
        });

        // 담당자
        mSpinnerManagers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RegStaffList regStaffList = mRegStaffListItems.get(position);
//                Toast.makeText(RegSalesActivity.this, regStaffList.COMPANY_STAFF_ID + " / " + regStaffList.STAFF_NAME, Toast.LENGTH_SHORT).show();
                mSpinnerManagers.setTag(regStaffList.COMPANY_STAFF_ID);
                validate();
            }
        });

        mSpinnerManagers.setOnClickListener(v -> {
            if (mRegStaffListItems.isEmpty()) {
                Toast.makeText(this, "담당자가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                showManagerDialog();
            }
        });

//        mSpinnerSalesClasses.setDropDownHeight(0); // 최초 disable
        mSpinnerSalesClasses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MasterCode mastercode = mMasterCodeItems.get(position);
//                Toast.makeText(RegSalesActivity.this, mastercode.DESC + " / " + mastercode.VALUE, Toast.LENGTH_SHORT).show();
                mSpinnerSalesClasses.setTag(mastercode.VALUE);
                validate();
            }
        });

        mWorkGroupCodeItems.add(new WorkGroupCode("행사", "1001"));
        //if ("esgroup".equals(BuildConfig.APP_FLAVOR)) {
            mWorkGroupCodeItems.add(new WorkGroupCode("전시장", "2001"));
        //}
        mWorkGroupCodeItems.add(new WorkGroupCode("기관/협단체 등", "3001"));
        mWorkGroupCodeItems.add(new WorkGroupCode("기타", "4001"));
        ArrayAdapter<WorkGroupCode> adapterWorkGroupCode = new ArrayAdapter<WorkGroupCode>(this, android.R.layout.simple_list_item_1, mWorkGroupCodeItems);
        mSpinnerWorkGroupCode.setAdapter(adapterWorkGroupCode);
        mSpinnerWorkGroupCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WorkGroupCode spinnerItem = (WorkGroupCode) parent.getItemAtPosition(position);
                mSpinnerWorkGroupCode.setTag(spinnerItem.getValue());
                WORK_GROUP_CODE = spinnerItem.getValue();
                Log.e(TAG, "mSpinnerWorkGroupCode WORK_GROUP_CODE:" + WORK_GROUP_CODE);
                if (!WORK_GROUP_CODE.equals("1001")) {
                    listener3.onCheckedChanged(rdogroupWorkType, R.id.rdoWorkType_E);
                    mRdoWorkType_S.setEnabled(false);
                } else {
                    listener3.onCheckedChanged(rdogroupWorkType, R.id.rdoWorkType_S);
                    mRdoWorkType_S.setEnabled(true);
                }

                if (WORK_GROUP_CODE.equals("2001")) {
                    mSpinnerConventionCenter.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mConventionCodeItems.add(new ConventionCode("수원메쎄", "2001"));
        mConventionCodeItems.add(new ConventionCode("오스코", "2002"));
        mConventionCodeItems.add(new ConventionCode("IICC", "2003"));
        ArrayAdapter<ConventionCode> adapterConventionCode = new ArrayAdapter<ConventionCode>(this, android.R.layout.simple_list_item_1, mConventionCodeItems);
        mSpinnerConventionCenter.setAdapter(adapterConventionCode);

        mSpinnerConventionCenter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ConventionCode spinnerItem = (ConventionCode) parent.getItemAtPosition(position);
                Log.e(TAG, "Value:" + spinnerItem.getValue());
                mSpinnerConventionCenter.setTag(spinnerItem.getValue());

                if ("2001".equals(getTag(mSpinnerWorkGroupCode))) {
                    WORK_GROUP_CODE = spinnerItem.getValue();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEditBoothNum.addTextChangedListener(mTextWatcher);
        mEditContent.addTextChangedListener(mTextWatcher);
        mEditDate.addTextChangedListener(mTextWatcher);

        voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");   //한국어
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말해주세요"); // 유저에게 보여줄 문자

        chkVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("RegSalesActivity", "rdoMeetIn onCheckedChanged : " + isChecked);
                TedPermission.create()
                        .setDeniedMessage("설정에서 앱 권한을 확인해 주세요.")
                        .setRationaleConfirmText("확인")
                        .setDeniedCloseButtonText("취소")
                        .setGotoSettingButtonText("설정")
                        .setPermissionListener(new PermissionListener() {

                            @Override
                            public void onPermissionGranted() {
                                if (isChecked) {
                                    StartRecord();
                                } else {
                                    StopRecord();
                                }
                            }

                            @Override
                            public void onPermissionDenied(List<String> deniedPermissions) {
                                recording = false;
                                txtVoice.setTextColor(getResources().getColor(R.color.color_blue, null));
                                txtVoice.setText("*버튼을 누르시면 음성으로 영업 내용을 입력할 수 있습니다.");
                                Toast.makeText(RegSalesActivity.this, "앱 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 일부서비스의 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setPermissions(new String[]{Manifest.permission.RECORD_AUDIO})
                        .check();
            }
        });

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            entry_path = intent.getExtras().getString("entry_path");
            Log.e("RegSalesActivity", "entry_path : " + entry_path);
        }
        load(true);
        GetCompanyStaffAll();

        mBtnNCT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                Log.e("RegSalesActivity", "mBtnNCT onCheckedChanged : " + isChecked);
                mBtnNCT.setChecked(isChecked);
                validate();
            }
        });
        mBtnRecall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                Log.e("RegSalesActivity", "mBtnRecall onCheckedChanged : " + isChecked);
                if (isChecked) {
                    showDateAndTimePickerDialog();
                } else {
                    mBtnRecall.setChecked(isChecked);
                    mEditDate.setVisibility(View.INVISIBLE);
                    mEditDate.setText("");
                }
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSpinnerEvents != null && mSpinnerEvents.isPopupShowing()) {
                    mSpinnerEvents.dismissDropDown();
                } else if (mSpinnerManagers != null && mSpinnerManagers.isPopupShowing()) {
                    mSpinnerManagers.dismissDropDown();
                } else if (mSpinnerSalesClasses != null && mSpinnerSalesClasses.isPopupShowing()) {
                    mSpinnerSalesClasses.dismissDropDown();
                } else if (mSingleDateAndTimePickerDialog != null) {
                    mSingleDateAndTimePickerDialog.close();
                    mSingleDateAndTimePickerDialog = null;
                } else {
                    finish();
                }
            }
        });
    }

    //영업 유형
    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rdogroupSalesTypeTwo.setOnCheckedChangeListener(null);
                rdogroupSalesTypeTwo.clearCheck();
                rdogroupSalesTypeTwo.setOnCheckedChangeListener(listener2);
            }
            validate();
        }
    };

    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rdogroupSalesType.setOnCheckedChangeListener(null);
                rdogroupSalesType.clearCheck();
                rdogroupSalesType.setOnCheckedChangeListener(listener1);
            }
            validate();
        }
    };

    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.e("RegSalesActivity", "listener3 onCheckedChanged : " + checkedId);
            if (checkedId == R.id.rdoWorkType_S) {
                txtToolbarTitle.setText(R.string.str_title_sales_reg);
                mRdoWorkType_S.setChecked(true);
                mRdoWorkType_E.setChecked(false);
            } else {
                txtToolbarTitle.setText("영업 외 활동 등록");
                mRdoWorkType_S.setChecked(false);
                mRdoWorkType_E.setChecked(true);
            }
            validate();
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
//        if (id == R.id.btnNCT) {
//            boolean isSelected = !mBtnNCT.isSelected();
//            mBtnNCT.setSelected(isSelected);
//            validate();
//        } else
//        if (id == R.id.btnRecall) {
//            boolean isChecked = mBtnRecall.isChecked();
//            Log.e("RegSalesActivity", "mBtnRecall isChecked : " + isChecked);
//                mBtnRecall.setSelected(isSelected);
//                validate();
//            if (isChecked) {
//                showDateAndTimePickerDialog();
//            } else {
//                mBtnRecall.setSelected(isChecked);
//                mEditDate.setVisibility(View.INVISIBLE);
//                mEditDate.setText("");
//            }
//        } else
        if (id == R.id.btnRegister) {
            registerSalesInfo();
        } else if (id == R.id.remainReceivableAmount) {
            showAlertDialog(R.layout.receivable_amount_dialog_layout);
        } else if (id == R.id.spinnerManagersAdd) {
            Intent intent = new Intent(RegSalesActivity.this, RegStaffAddActivity.class);
            intent.putExtra(Extra.KEY_COMPANY_ID, mCompanyInfo.COMPANY_ID);
            mStartForResult.launch(intent);
        }
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

    /*
    protected void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        Kit.log(LogType.VALUE, calendar.get(Calendar.YEAR) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.MONTH) + 1 + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.DATE) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.HOUR_OF_DAY) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.MINUTE) + "");

        DatePickerDialog dialog = new DatePickerDialog(RegSalesActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, date);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      // "yyyy-MM-dd HH:mm:ss.SSS"
                mEditDate.setText(sdf.format(c.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        dialog.show();
    }

    protected void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        Kit.log(LogType.VALUE, calendar.get(Calendar.YEAR) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.MONTH) + 1 + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.DATE) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.HOUR_OF_DAY) + "");
        Kit.log(LogType.VALUE, calendar.get(Calendar.MINUTE) + "");

        TimePickerDialog dialog = new TimePickerDialog(RegSalesActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");       // "yyyy-MM-dd HH:mm:ss.SSS"
                mEditTime.setText(sdf.format(c.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }
    */

    protected void showDateAndTimePickerDialog() {
        mSingleDateAndTimePickerDialog = new SingleDateAndTimePickerDialog.Builder(this);
        mSingleDateAndTimePickerDialog.minutesStep(10);
        mSingleDateAndTimePickerDialog.displayAmPm(false);
        mSingleDateAndTimePickerDialog.minDateRange(new Date());
        mSingleDateAndTimePickerDialog.setDayFormatter(new SimpleDateFormat("MM월 dd일 E"));
        mSingleDateAndTimePickerDialog.mainColor(getResources().getColor(R.color.color_class_company));
        mSingleDateAndTimePickerDialog.title("재통화예정일");
        mSingleDateAndTimePickerDialog.listener(new SingleDateAndTimePickerDialog.Listener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");       // "yyyy-MM-dd HH:mm:ss.SSS"
                mBtnRecall.setChecked(true);
                mEditDate.setVisibility(View.VISIBLE);
                mEditDate.setText(sdf.format(date));
                mSingleDateAndTimePickerDialog = null;
            }
        });
        mSingleDateAndTimePickerDialog.displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
            @Override
            public void onDisplayed(SingleDateAndTimePicker picker) {
                picker.setDefaultDate(new Date());
            }

            @Override
            public void onClosed(SingleDateAndTimePicker singleDateAndTimePicker) {
                if (mEditDate.getVisibility() != VISIBLE) {
                    mBtnRecall.setChecked(false);
                    mEditDate.setVisibility(INVISIBLE);
                    mEditDate.setText("");
                    mSingleDateAndTimePickerDialog = null;
                }
            }
        });

        mSingleDateAndTimePickerDialog.display();
    }

    protected void load(boolean init) {
        if (init) {
            rdogroupSalesType.clearCheck();
            rdogroupSalesType.setTag(null);
            rdogroupSalesTypeTwo.clearCheck();
            rdogroupSalesTypeTwo.setTag(null);

            mSpinnerEvents.setText("");
            mSpinnerEvents.setTag(null);

            mSpinnerManagers.setText("");
            mSpinnerManagers.setTag(null);

            mBtnNCT.setChecked(false);
            mBtnNCT.setVisibility(View.INVISIBLE);

            mSpinnerSalesClasses.setText("");
            mSpinnerSalesClasses.setTag(null);

            mEditBoothNum.setText("");

            mEditContent.setText("");

            mBtnRecall.setChecked(false);

            mEditDate.setText("");

            mRegStaffListItems.clear();
            mRegFairListItems.clear();
            mMasterCodeItems.clear();
            mRegReceivableAmountListItems.clear();
        }

        UserInfo userInfo = PrefKit.getUserInfo(this);
        if (userInfo != null) {
            String FAIR_ID = getTag(mSpinnerEvents);
//            Kit.log(Kit.LogType.VALUE, "load::FAIR_ID = " + FAIR_ID);
            //String body = String.format("system_id=%s&company_id=%s&fair_id=%s&entry_path=%s", userInfo.SYS_ID, mCompanyInfo.COMPANY_ID, FAIR_ID, entry_path);
            HashMap<String, String> body = new HashMap<>();
            body.put("system_id", userInfo.SYS_ID);
            body.put("company_id", String.valueOf(mCompanyInfo.COMPANY_ID));
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
                                    txtBrandName.setVisibility(View.VISIBLE);
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
                            mSpinnerManagers.clearListSelection();
                            mSpinnerManagers.setText("");
                            mSpinnerManagers.setTag(null);
                            JSONArray staffList = data.optJSONArray("staffList");
                            Log.e(TAG, "staffList: " + staffList.toString());
                            if (staffList != null) {
                                String[] managers = new String[staffList.length()];
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
                                        regStaffList.WORK_FLAG = stafflist_obj.optString("WORK_FLAG");
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
                            String FAIR_ID = getTag(mSpinnerEvents);
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
                                    }
                                }
                            }
                            String[] events = new String[mRegFairListItems.size()];
                            for (int i = 0; i < mRegFairListItems.size(); i++) {
                                events[i] = mRegFairListItems.get(i).FAIR_NAME;
                            }
                            ArrayAdapter<String> adapterEvents = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, events);
                            //mSpinnerEvents.setAdapter(adapterEvents);

                            validate();

                            Fragment fragment = mViewPagerAdapter.getItem(0);
                            if (fragment instanceof SalesDetailFragment) {
                                SalesDetailFragment salesDetailFragment = (SalesDetailFragment) fragment;
                                salesDetailFragment.load(true, "", "");
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

                            fragment = mViewPagerAdapter.getItem(3);
                            if (fragment instanceof InfrastructureFragment) {
                                InfrastructureFragment infrastructureFragment = (InfrastructureFragment) fragment;
                                infrastructureFragment.FAIR_ID = FAIR_ID;
                                infrastructureFragment.load();
                            }

                            if (result.mRequestCode == 1) {                 // init
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTabLayout.getTabAt(0).select();
                                        appBarLayout.setExpanded(false, true);
                                        Kit.hideSoftKeyboard(RegSalesActivity.this);
                                    }
                                }, 1000);
                            }

                            String ReceivableAmount = data.optString("remainReceivableAmount");
                            if (TextUtils.isEmpty(ReceivableAmount)) {
                                remainReceivableAmount.setVisibility(GONE);
                                remainReceivableAmount.setText("");
                            } else {
                                remainReceivableAmount.setVisibility(View.VISIBLE);
                                remainReceivableAmount.setText(ReceivableAmount);
                            }

                            mRegReceivableAmountListItems.clear();
                            JSONArray remainReceivableAmountList = data.optJSONArray("remainReceivableAmountList");
                            if (remainReceivableAmountList != null) {
                                for (int i = 0; i < remainReceivableAmountList.length(); i++) {
                                    JSONObject remainReceivableAmountList_obj = remainReceivableAmountList.getJSONObject(i);
                                    if (remainReceivableAmountList_obj != null) {
                                        RegReceivableAmountList RegReceivableAmountList = new RegReceivableAmountList();
                                        RegReceivableAmountList.FAIR_NAME = remainReceivableAmountList_obj.optString("FAIR_NAME");
                                        RegReceivableAmountList.TOT_RECV_AMT = remainReceivableAmountList_obj.optString("TOT_RECV_AMT");
                                        RegReceivableAmountList.PAY_DUE_DATE = remainReceivableAmountList_obj.optString("PAY_DUE_DATE");
                                        mRegReceivableAmountListItems.add(RegReceivableAmountList);
                                    }
                                }
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
                                mSpinnerSalesClasses.setAdapter(adapterSalesClasses);
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
                        mSpinnerManagers.clearListSelection();
                        mSpinnerManagers.setText("");
                        mSpinnerManagers.setTag(null);
                        JSONArray staffList = json.optJSONArray("list");
                        if (staffList != null) {
                            String[] managers = new String[staffList.length()];
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
                        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "URL_API_GET_STAFF_ALL msg :" + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_SET_COUNSEL)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    String code = resultObj.optString("code");
                    String msg = resultObj.optString("msg");
                    if ("ok".equals(code)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("영업 활동이 등록되었습니다.");
                        builder.setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
//                                        finish();
                                        load(true);
                                    }
                                });
                        builder.show();
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.str_error_generic), Toast.LENGTH_SHORT).show();
        }
    }

    protected void registerSalesInfo() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        Object obj;

        //
        String COMPANY_ID = "";
        if (mCompanyInfo.COMPANY_ID == -1) {
            Kit.showAlertDialog(this, "", "업체 식별 정보가 없습니다. 영업 활동을 등록할 수 없습니다.", "확인");
            return;
        }
        COMPANY_ID = mCompanyInfo.COMPANY_ID + "";

        // 행사 선택
        String FAIR_ID = getTag(mSpinnerEvents);

        //
        String SYSTEM_ID = userInfo.SYS_ID;
        if (!Kit.isNotNullNotEmpty(SYSTEM_ID)) {
            Kit.showAlertDialog(this, "", "계정 식별 정보가 없습니다. 영업 활동을 등록할 수 없습니다.", "확인");
            return;
        }

        String WORK_TYPE = ""; // S:영업 , E:영업외
        int rdoBtnID = rdogroupWorkType.getCheckedRadioButtonId();
        switch (rdoBtnID) {
            case R.id.rdoWorkType_S:
                WORK_TYPE = "S";
                break;
            case R.id.rdoWorkType_E:
                WORK_TYPE = "E";
                break;
        }

        int btnID = -1;
        if (rdogroupSalesType.getCheckedRadioButtonId() != -1) {
            btnID = rdogroupSalesType.getCheckedRadioButtonId();
        } else {
            btnID = rdogroupSalesTypeTwo.getCheckedRadioButtonId();
        }
        // 영업 유형
        String TM_TYPE = "";
        if (btnID == R.id.rdoEmail) {
            TM_TYPE = "6";
        } else if (btnID == R.id.rdoMeetOut) {
            TM_TYPE = "3";
        } else if (btnID == R.id.rdoCallIn) {
            TM_TYPE = "2";
        } else if (btnID == R.id.rdoCallOut) {
            TM_TYPE = "1";
        } else if (btnID == R.id.rdoMeetField) {
            TM_TYPE = "7";
        } else if (btnID == R.id.rdoMeetIn) {
            TM_TYPE = "4";
        } else if (btnID == R.id.rdoEct) {
            TM_TYPE = "8";
        } else {
            TM_TYPE = "";
        }

        if (TM_TYPE.isEmpty()) {
            Kit.showAlertDialog(this, "", "영업 유형을 선택해주세요.", "확인");
            return;
        }

        // NCT (Not Contact) 여부
        String NCT_FLAG = "";
        if (mBtnNCT.isChecked()) {
            NCT_FLAG = "Y";
        } else {
            NCT_FLAG = "N";
        }

        // 영업 등급
        String TM_STATUS = "";
        obj = mSpinnerSalesClasses.getTag();
        if (obj != null) {
            TM_STATUS = (String) obj;
        }

        // 업체 담당자 선택
        String COMPANY_STAFF_ID = "";
        obj = mSpinnerManagers.getTag();
        if (obj != null) {
            COMPANY_STAFF_ID = (String) obj;
        }
        if (NCT_FLAG.equalsIgnoreCase("N")) {
            if (!TM_STATUS.equalsIgnoreCase("6") && !TM_STATUS.equalsIgnoreCase("7") && !TM_STATUS.equalsIgnoreCase("8")) {     // C1, C2, T 이면 담당자 지정 필수 아님
                if (COMPANY_STAFF_ID.isEmpty()) {
                    Kit.showAlertDialog(this, "", "업체 담당자를 선택해주세요.", "확인");
                    return;
                }
            }
        }

        // 영업 활동 내용
        String CONTENT = mEditContent.getText().toString().trim();

        // 예상 부스 수
        String BOOTH_CNT = mEditBoothNum.getText().toString().trim();

        // 재통화 예정일
        String RECALL_DATE = "";
        if (mBtnRecall.isChecked()) {
            String date = mEditDate.getText().toString();
            if (date.isEmpty()) {
                Kit.showAlertDialog(this, "", "재통화 예정일시를 선택해주세요.", "확인");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");      // "yyyy-MM-dd HH:mm:ss.SSS"
            Date dt = null;
            try {
                dt = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dt == null) {
                Kit.showAlertDialog(this, "", "재통화 예정일시를 파싱할 수 없습니다.", "확인");
                return;
            }
            sdf = new SimpleDateFormat("yyyyMMddHHmm");      // "yyyy-MM-dd HH:mm:ss.SSS"
            RECALL_DATE = sdf.format(dt);
        }

        //
        String CREATE_USER = userInfo.LOGIN_ID;
        if (CREATE_USER.isEmpty()) {
            Kit.showAlertDialog(this, "", "로그인 계정 아이디가 없습니다. 영업 활동을 등록할 수 없습니다.", "확인");
            return;
        }

        // FAIR_MANAGER_ID 사용 안함
//        String body = String.format(
//                "COMPANY_ID=%s&FAIR_ID=%s&FAIR_MANAGER_ID=&SYSTEM_ID=%s&COMPANY_STAFF_ID=%s" +
//                        "&TM_TYPE=%s&TM_STATUS=%s&CONTENT=%s&BOOTH_CNT=%s&RECALL_DATE=%s&NCT_FLAG=%s&CREATE_USER=%s",
//                COMPANY_ID, FAIR_ID, SYSTEM_ID, COMPANY_STAFF_ID,
//                TM_TYPE, TM_STATUS, CONTENT, BOOTH_CNT, RECALL_DATE, NCT_FLAG, CREATE_USER);
        HashMap<String, String> body = new HashMap<>();
        body.put("COMPANY_ID", COMPANY_ID);
        body.put("FAIR_ID", FAIR_ID);
        body.put("FAIR_MANAGER_ID", "");        // FAIR_MANAGER_ID 사용 안함
        body.put("SYSTEM_ID", SYSTEM_ID);
        body.put("COMPANY_STAFF_ID", COMPANY_STAFF_ID);
        body.put("TM_TYPE", TM_TYPE);
        body.put("TM_STATUS", TM_STATUS);
        body.put("CONTENT", CONTENT);
        body.put("BOOTH_CNT", BOOTH_CNT);
        body.put("RECALL_DATE", RECALL_DATE);
        body.put("NCT_FLAG", NCT_FLAG);
        body.put("CREATE_USER", CREATE_USER);
        body.put("WORK_TYPE", WORK_TYPE);
        body.put("WORK_GROUP_CODE", WORK_GROUP_CODE);
        new TelKit(this, this, progressBar_layout).request(TelKit.URL_API_SET_COUNSEL, body);
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

    protected void validate() {
        boolean isChecked, visible;
        boolean validSalesType = false, validEvents = false, validManagers = false,
                validSalesClass = false, validBoothNum = false, validContent = false, validRecall = false;
//        String tag;

        Log.e(TAG, "validate WORK_GROUP_CODE:" + WORK_GROUP_CODE);
        // 영업 유형
        int checkedSalesType = rdogroupSalesType.getCheckedRadioButtonId();
        int checkedSalesTypeTwo = rdogroupSalesTypeTwo.getCheckedRadioButtonId();
        int checkedWorkType = rdogroupWorkType.getCheckedRadioButtonId();
        if (checkedSalesType != -1 || checkedSalesTypeTwo != -1) {        // 유 선택되어 있고
            validSalesType = true;
        }

        if (WORK_GROUP_CODE.equals("1001")) {
            mSpinnerEvents.setVisibility(View.VISIBLE);
            txtErrorEvents.setVisibility(View.VISIBLE);
            mSpinnerConventionCenter.setVisibility(GONE);
            txtErrorRdogroupWorkType.setVisibility(GONE);
        } else {
            validEvents = true;
            mSpinnerEvents.setText("");
            mSpinnerEvents.setTag(null);
            mSpinnerEvents.setVisibility(GONE);
            txtErrorEvents.setVisibility(GONE);
            txtErrorRdogroupWorkType.setVisibility(View.VISIBLE);
            if (WORK_GROUP_CODE.equals("2001") || WORK_GROUP_CODE.equals("2002") || WORK_GROUP_CODE.equals("2003")) {
                mSpinnerConventionCenter.setVisibility(View.VISIBLE);
            } else {
                mSpinnerConventionCenter.setVisibility(GONE);
            }
        }

        // NCT, 영업 등급
        if (checkedSalesType == R.id.rdoCallIn || checkedSalesType == R.id.rdoEmail || checkedSalesTypeTwo == R.id.rdoMeetIn) {
            mBtnNCT.setVisibility(View.INVISIBLE);
            mBtnNCT.setChecked(false);
        } else {
            mBtnNCT.setVisibility(View.VISIBLE);
            if (checkedSalesTypeTwo == R.id.rdoEct) {
                mBtnNCT.setChecked(true);
            }
        }

        isChecked = mBtnNCT.isChecked();
        if (checkedWorkType == R.id.rdoWorkType_E || isChecked) {
            mSpinnerSalesClasses.setVisibility(View.INVISIBLE);
            mSpinnerSalesClasses.removeTextChangedListener(mTextWatcher);
            mSpinnerSalesClasses.setText("");
            mSpinnerSalesClasses.setTag(null);
            mSpinnerSalesClasses.addTextChangedListener(mTextWatcher);

            mEditBoothNum.setVisibility(View.INVISIBLE);
            mEditBoothNum.removeTextChangedListener(mTextWatcher);
            mEditBoothNum.setText("");
            mEditBoothNum.addTextChangedListener(mTextWatcher);
        } else {
            mSpinnerSalesClasses.setVisibility(View.VISIBLE);
            visible = false;
            String salesClass = getTag(mSpinnerSalesClasses);
            if (salesClass.equalsIgnoreCase("2") || salesClass.equalsIgnoreCase("4")) {     // A2 or B1
                visible = true;
            } else {
                mEditBoothNum.removeTextChangedListener(mTextWatcher);
                mEditBoothNum.setText("");
                mEditBoothNum.addTextChangedListener(mTextWatcher);
            }
            mEditBoothNum.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

        // 행사, 영업 등급, 예상부스 수, 영업 내용, 담당자
        isChecked = mBtnNCT.isChecked();
        if (isChecked) {
            String event = getTag(mSpinnerEvents);
            if (!event.isEmpty()) {                       // 행사 선택되어 있고
                validEvents = true;
            }
            validSalesClass = true;
            validBoothNum = true;
            validManagers = true;
            String content = Objects.requireNonNull(mEditContent.getText()).toString();
            if (!content.isEmpty() && !mEditContent.getText().toString().trim().isEmpty()) {                           // 영업 내용이 입력되어 있고
                validContent = true;
            }
        } else {
            String salesClass = getTag(mSpinnerSalesClasses);
            String manager = getTag(mSpinnerManagers);
            if (!manager.isEmpty()) {                           // 담당자 선택되어 있는지...
                validManagers = true;
            }

            if (!salesClass.isEmpty()) {                           // 영업 등급 선택되어 있고
                validSalesClass = true;
                if (salesClass.equalsIgnoreCase("6") || salesClass.equalsIgnoreCase("7") || salesClass.equalsIgnoreCase("8")) {     // C1, C2, T 이면 담당자 지정 필수 아님
                    validManagers = true;
                } else {
                    //String manager = getTag(mSpinnerManagers);
                    if (!manager.isEmpty()) {                           // 담당자 선택되어 있는지...
                        validManagers = true;
                    }
                }

                if (salesClass.equalsIgnoreCase("2") || salesClass.equalsIgnoreCase("4")) {     // A2 or B1
                    String num = mEditBoothNum.getText().toString();
                    if (!num.isEmpty()) {
                        validBoothNum = true;
                    }
                } else {
                    validBoothNum = true;
                    mEditBoothNum.removeTextChangedListener(mTextWatcher);
                    mEditBoothNum.setText("");
                    mEditBoothNum.addTextChangedListener(mTextWatcher);
                }

                String event = getTag(mSpinnerEvents);
                if (!event.isEmpty()) {                       // 행사 선택되어 있고
                    validEvents = true;
                }
            } else {
                String tag = getTag(mSpinnerEvents);
                if (!tag.isEmpty()) {                       // 행사 선택되어 있고
                    validEvents = true;
                }
                validBoothNum = true;

                if (checkedWorkType == R.id.rdoWorkType_E || checkedSalesType == R.id.rdoEmail) {
                    validSalesClass = true;
                }

            }

            String content = Objects.requireNonNull(mEditContent.getText()).toString();
            if (!content.isEmpty() && !mEditContent.getText().toString().trim().isEmpty()) {                          // 영업 내용이 입력되어 있고
                validContent = true;
            }
        }

        // 재통화예정
        isChecked = mBtnRecall.isChecked();
        if (isChecked) {
            String date = mEditDate.getText().toString();
            if (!date.isEmpty()) {
                validRecall = true;
            }
        } else {
            validRecall = true;
        }
        mEditDate.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

        txtErrorSalesType.setVisibility(validSalesType ? View.INVISIBLE : View.VISIBLE);
        txtErrorEvents.setVisibility(validEvents ? View.INVISIBLE : View.VISIBLE);
        txtErrorManagers.setVisibility(validManagers ? View.INVISIBLE : View.VISIBLE);
        txtErrorSalesClass.setVisibility(validSalesClass ? View.INVISIBLE : View.VISIBLE);
        txtErrorBoothNum.setVisibility(validBoothNum ? View.INVISIBLE : View.VISIBLE);
        txtErrorContent.setVisibility(validContent ? View.INVISIBLE : View.VISIBLE);
        mBtnRegister.setEnabled(validSalesType && validEvents && validManagers && validSalesClass && validBoothNum && validContent && validRecall);
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
//        if (mSpinnerEvents != null && mSpinnerEvents.isPopupShowing()) {
//            mSpinnerEvents.dismissDropDown();
//        } else if (mSpinnerManagers != null && mSpinnerManagers.isPopupShowing()) {
//            mSpinnerManagers.dismissDropDown();
//        } else if (mSpinnerSalesClasses != null && mSpinnerSalesClasses.isPopupShowing()) {
//            mSpinnerSalesClasses.dismissDropDown();
//        } else if (mSingleDateAndTimePickerDialog != null) {
//            mSingleDateAndTimePickerDialog.close();
//            mSingleDateAndTimePickerDialog = null;
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (managerDialog != null && managerDialog.isShowing()) {
            managerDialog.dismiss();
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void showAlertDialog(int layout) {
        Log.e("RegSalesActivity", "mRegReceivableAmountListItems : " + mRegReceivableAmountListItems.size());
        dialogBuilder = new android.app.AlertDialog.Builder(this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        Button btnOK = layoutView.findViewById(R.id.btnOK);
        dialogBuilder.setView(layoutView);
        mRecyclerView = layoutView.findViewById(R.id.receivable_amount_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RegSalesActivity.RecycleAdapter(this, mRegReceivableAmountListItems);
        mRecyclerView.setAdapter(mAdapter);

        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public class RecycleAdapter extends RecyclerView.Adapter<RegSalesActivity.RecycleAdapter.ItemViewHolder> {
        private Context mContext;
        private List<RegReceivableAmountList> mItemList;
        private AdapterView.OnItemClickListener mOnItemClickListener;
        private View.OnClickListener mOnClickListener;

        public RecycleAdapter(Context context, List<RegReceivableAmountList> itemList) {
            this.mContext = context;
            this.mItemList = itemList;
        }

        @Override
        public RegSalesActivity.RecycleAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_item_receivable_amount_row, parent, false);
            return new RegSalesActivity.RecycleAdapter.ItemViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(RegSalesActivity.RecycleAdapter.ItemViewHolder holder, int position) {
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

        private void onItemHolderClick(RegSalesActivity.RecycleAdapter.ItemViewHolder
                                               itemHolder) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.getAdapterPosition(), itemHolder.getItemId());
            }
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public RegSalesActivity.RecycleAdapter mAdapter;
            public TextView fair_name;
            public TextView tot_recv_atm;
            public TextView pay_due_date;

            public ItemViewHolder(View itemView, RegSalesActivity.RecycleAdapter mAdapter) {
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

    //녹음 시작
    private void StartRecord() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recording = true;
            txtVoice.setTextColor(getResources().getColor(R.color.color_text_error, null));
            txtVoice.setText("음성으로 영업등록 입력중 입니다...");

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            //speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(), ComponentName.unflattenFromString("com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService"));
            speechRecognizer.setRecognitionListener(voicelistener);
            speechRecognizer.startListening(voiceIntent);
        } else {
            chkVoice.setChecked(false);
            recording = false;
            txtVoice.setTextColor(getResources().getColor(R.color.color_blue, null));
            txtVoice.setText("*버튼을 누르시면 음성으로 영업 내용을 입력할 수 있습니다.");
            Toast.makeText(getApplicationContext(), "해당 기능을 사용 하시려면 Google앱을 활성화 시켜 주세요.", Toast.LENGTH_SHORT).show();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://market.android.com/details?id=com.google.android.googlequicksearchbox"));
            startActivity(browserIntent);
        }
    }

    //녹음 중지
    private void StopRecord() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recording = false;
            txtVoice.setTextColor(getResources().getColor(R.color.color_blue, null));
            txtVoice.setText("*버튼을 누르시면 음성으로 영업 내용을 입력할 수 있습니다.");

            if (speechRecognizer != null) {
                speechRecognizer.stopListening();   //녹음 중지
                Toast.makeText(getApplicationContext(), "음성 기록을 중지 합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private RecognitionListener voicelistener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "ERROR_AUDIO";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    //message = "클라이언트 에러";
                    //speechRecognizer.stopListening()을 호출하면 발생하는 에러
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "ERROR_NETWORK";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "ERROR_NETWORK_TIMEOUT";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    if (recording) {
                        StartRecord();
                    }
                    return; //토스트 메세지 출력 X
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "ERROR_RECOGNIZER_BUSY";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "ERROR_SERVER";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "ERROR_SPEECH_TIMEOUT";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
            StopRecord();
            chkVoice.setChecked(false);
            recording = false;
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);    //인식 결과를 담은 ArrayList
            String originText = mEditContent.getText().toString();  //기존 text
            //인식 결과
            String newText = "";
            for (int i = 0; i < matches.size(); i++) {
                newText += matches.get(0);
            }
            mEditContent.setText(originText + newText + " ");    //기존의 text에 인식 결과를 이어붙임
            speechRecognizer.startListening(voiceIntent);    //녹음버튼을 누를 때까지 계속 녹음해야 하므로 녹음 재개
            mEditContent.setSelection(mEditContent.getText().length());
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speechRecognizer != null && recording == true) {
            StopRecord();
            chkVoice.setChecked(false);
        }
    }

    // launcher 선언
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //result.getResultCode()를 통하여 결과값 확인
                if (result.getResultCode() == RESULT_OK) {
                    //ToDo
                    Log.e("RegSalesActivity", "onActivityResult REQUEST_MANAGER_ADD RESULT_OK");
                    GetCompanyStaffAll();
                }
                if (result.getResultCode() == RESULT_CANCELED) {
                    //ToDo
                    Log.e("RegSalesActivity", "onActivityResult REQUEST_MANAGER_ADD RESULT_CANCELED");
                }
            }
    );

    protected void GetCompanyStaffAll() {
        UserInfo userInfo = PrefKit.getUserInfo(this);

        if (userInfo != null) {
            Log.e(TAG, "COMPANY_ID : " + String.valueOf(mCompanyInfo.COMPANY_ID));
            HashMap<String, String> body = new HashMap<>();
            body.put("PAGE_VIEW_COUNT", "100");
            body.put("CURRENT_PAGE_INDEX", "1");
            body.put("COMPANY_ID", String.valueOf(mCompanyInfo.COMPANY_ID));
            new TelKit(this, this, progressBar_layout).request(TelKit.URL_API_GET_STAFF_ALL, body);
        }
    }

    public static class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.ViewHolder> {

        private List<RegStaffList> list;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onClick(RegStaffList item);
        }

        public ManagerAdapter(List<RegStaffList> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_manager_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            RegStaffList item = list.get(position);
            if (item.WORK_FLAG.equals("N")) {
                holder.tvName.setText("(퇴직) " + item.STAFF_NAME);
            } else {
                holder.tvName.setText(item.STAFF_NAME);
            }
            if (Kit.isNotNullNotEmpty(item.STAFF_POSITION)) {
                holder.tvLine.setVisibility(View.VISIBLE);
                holder.tvPosition.setText(item.STAFF_POSITION);
            } else {
                holder.tvLine.setVisibility(GONE);
                holder.tvPosition.setText("");
            }

            if (Kit.isNotNullNotEmpty(item.UPDATE_DATE)) {
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvDate.setText("최종 수정 : " + item.UPDATE_DATE);
            } else {
                holder.tvDate.setText("최종 수정 : " + item.CREATE_DATE);
            }

            if (item.CREATE_DATE.isEmpty() && item.UPDATE_DATE.isEmpty()) {
                holder.tvDate.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLine, tvPosition, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvLine = itemView.findViewById(R.id.tvLine);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }

    private void showManagerDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_manager_list, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerManager);
        Button btnCancel = view.findViewById(R.id.dialog_cancel);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.e(TAG, "mRegStaffListItems : " + mRegStaffListItems.size());

        ManagerAdapter adapter = new ManagerAdapter(mRegStaffListItems, item -> {

            mSpinnerManagers.setTag(item.COMPANY_STAFF_ID);

            if (item.STAFF_POSITION.isEmpty()) {
                mSpinnerManagers.setText(item.STAFF_NAME);
            } else {
                mSpinnerManagers.setText(
                        item.STAFF_NAME + " (" + item.STAFF_POSITION + ")"
                );
            }
            validate();
            managerDialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> managerDialog.dismiss());

        recyclerView.setAdapter(adapter);

        builder.setView(view);   // ✅ 반드시 create() 전에 호출

        managerDialog = builder.create();  // ✅ 여기서 생성

        if (!managerDialog.isShowing()) {
            managerDialog.show();
        }
        Window window = managerDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            if (mRegStaffListItems.size() <= 5) {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        WindowManager.LayoutParams.WRAP_CONTENT
                );
            } else {
                window.setLayout(
                        (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                        (int) (getResources().getDisplayMetrics().heightPixels * 0.6)
                );
            }
        }
    }

    private void showFairListDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_fair_list, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFairList);
        EditText editFairSearch = view.findViewById(R.id.editFairSearch);
        Button btnCancel = view.findViewById(R.id.dialog_cancel);
        Button btnSearchFair = view.findViewById(R.id.btnSearchFair);
        txtEmptyFair_layout = view.findViewById(R.id.txtEmptyFair_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Log.e(TAG, "mRegFairListItems : " + mRegFairListItems.size());
        editFairSearch.requestFocus();
        // 처음에는 전체 리스트 표시
        mRegFairSearchListItems.clear();
        mRegFairSearchListItems.addAll(mRegFairListItems);

        final FairListAdapter fairListAdapter = new FairListAdapter(mRegFairSearchListItems, item -> {

            if (item.FAIR_NAME.equals("행사 선택")) {
                mSpinnerEvents.setTag("");
                mRegFairSearchListItems.clear();
                mRegFairSearchListItems.addAll(mRegFairListItems);
            } else {
                mSpinnerEvents.setTag(item.FAIR_ID);
            }
            mSpinnerEvents.setText(item.FAIR_NAME);
            load(false);
            fairListDialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> fairListDialog.dismiss());

        recyclerView.setAdapter(fairListAdapter);

        // 검색
        editFairSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Kit.hideSoftKeyboard(RegSalesActivity.this);
                    filterFairList(editFairSearch.getText().toString(), fairListAdapter);
                    return true;
                }
                return false;
            }
        });
        btnSearchFair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Kit.hideSoftKeyboard(RegSalesActivity.this);
                filterFairList(editFairSearch.getText().toString(), fairListAdapter);
            }
        });

        builder.setView(view);   // ✅ 반드시 create() 전에 호출

        fairListDialog = builder.create();  // ✅ 여기서 생성

        if (!fairListDialog.isShowing()) {
            fairListDialog.show();
        }
        Window window = fairListDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER);
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.6)
            );
        }
    }

    private void filterFairList(String keyword, FairListAdapter fairListAdapter) {

        mRegFairSearchListItems.clear();
        if (keyword.isEmpty()) {
            mRegFairSearchListItems.addAll(mRegFairListItems);
        } else {

            for (RegFairList item : mRegFairListItems) {

                if (item.FAIR_NAME != null &&
                        item.FAIR_NAME.toLowerCase().contains(keyword.toLowerCase())) {

                    mRegFairSearchListItems.add(item);
                }
            }
        }
        fairListAdapter.notifyDataSetChanged();
    }

    public static class FairListAdapter extends RecyclerView.Adapter<FairListAdapter.ViewHolder> {

        private List<RegFairList> list;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onClick(RegFairList item);
        }

        public FairListAdapter(List<RegFairList> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_manager_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            RegFairList item = list.get(position);

            holder.tvName.setText(item.FAIR_NAME);
            holder.tvLine.setVisibility(GONE);
            holder.tvPosition.setVisibility(GONE);
            holder.tvDate.setVisibility(GONE);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }

        @Override
        public int getItemCount() {
            int count = list.size();
            txtEmptyFair_layout.setVisibility(count > 0 ? INVISIBLE : VISIBLE);
            return count;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvLine, tvPosition, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvLine = itemView.findViewById(R.id.tvLine);
                tvPosition = itemView.findViewById(R.id.tvPosition);
                tvDate = itemView.findViewById(R.id.tvDate);
            }
        }
    }
}
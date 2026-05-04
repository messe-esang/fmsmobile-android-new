package net.e_sang.fmsmobile.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WorkOutActivity extends BaseActivity implements TelKit.OnResultListener, View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private LinearLayout mLinearLayout = null;
    private EditText mEditStartDate, mEditEndDate = null;
    private TextInputEditText mEditContent_WorkOut = null;
    private Button mBtnRegister_WorkOut = null;
    private TextView txtErrorContent = null;
    private String WOID = "";
    private SingleDateAndTimePickerDialog.Builder mSingleDateAndTimePickerDialog = null;
    private LocationManager lm = null;
    private String LATITUDE = "";
    private String LONGITUDE = "";
    private String Finish_Msg = "외근등록을 취소 하시겠습니까?";
    // 내 위치 정보 가져오기 Start.
    public static LocationCallback locationCallback;
    public static FusedLocationProviderClient fusedLocationClient;
    public static LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTINGS = 3007;  // 내 위치 정보 가져오기
    // 내 위치 정보 가져오기 End.

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mEditStartDate.getText().toString().trim().isEmpty()) {
                mBtnRegister_WorkOut.setEnabled(false);
                txtErrorContent.setText("* 시작 시간을 입력해주세요.");
                txtErrorContent.setVisibility(View.VISIBLE);
            } else if (mEditEndDate.getText().toString().trim().isEmpty()) {
                mBtnRegister_WorkOut.setEnabled(false);
                txtErrorContent.setText("* 종료 시간을 입력해주세요.");
                txtErrorContent.setVisibility(View.VISIBLE);
            } else if (mEditContent_WorkOut.getText().toString().trim().isEmpty()) {
                mBtnRegister_WorkOut.setEnabled(false);
                txtErrorContent.setText("* 외근 등록 내용을 입력해주세요.");
                txtErrorContent.setVisibility(View.VISIBLE);
            } else {
                mBtnRegister_WorkOut.setEnabled(true);
                txtErrorContent.setVisibility(View.INVISIBLE);
            }
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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_work_out);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_company));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("외근등록");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        View root = findViewById(R.id.main);

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

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent != null) {
            WOID = intent.getExtras().getString("WOID");
        }

        // 내 위치 정보 가져오기 Start.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    LATITUDE = Double.toString(location.getLatitude());
                    LONGITUDE = Double.toString(location.getLongitude());
                }
            }
        };
        getMyLocation();
        // 내 위치 정보 가져오기 End.

        mLinearLayout = findViewById(R.id.progressBar_layout);

        txtErrorContent = findViewById(R.id.txtErrorContent);
        mBtnRegister_WorkOut = findViewById(R.id.btnRegister_WorkOut);
        mEditContent_WorkOut = findViewById(R.id.editContent_WorkOut);
        mEditStartDate = findViewById(R.id.editStartDate);
        mEditEndDate = findViewById(R.id.editEndDate);

        mBtnRegister_WorkOut.setOnClickListener(this);
        mEditContent_WorkOut.setOnClickListener(this);
        mEditStartDate.setOnClickListener(this);
        mEditEndDate.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mEditStartDate.addTextChangedListener(mTextWatcher);
        mEditEndDate.addTextChangedListener(mTextWatcher);
        mEditContent_WorkOut.addTextChangedListener(mTextWatcher);
        if (!TextUtils.isEmpty(WOID) && !WOID.equals("0")) {
            getWorkOut();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mSingleDateAndTimePickerDialog != null) {
                    mSingleDateAndTimePickerDialog.dismiss();
                    mSingleDateAndTimePickerDialog = null;
                } else {
                    //super.onBackPressed();
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(WorkOutActivity.this);
                    alert_confirm.setMessage(Finish_Msg).setCancelable(false).setPositiveButton("네",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'YES'
                                    finish();
                                }
                            }).setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    return;
                                }
                            });

                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.editStartDate) {
            showDateAndTimePickerDialog(true);
        } else if (id == R.id.editEndDate) {
            showDateAndTimePickerDialog(false);
        } else if (id == R.id.btnRegister_WorkOut) {
            setWorkOut();
        }
    }

    protected void getWorkOut() {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        //String body = String.format("WOID=%s&system_id=%s", WOID, userInfo.SYS_ID);
        HashMap<String, String> body = new HashMap<>();
        body.put("WOID", WOID);
        body.put("system_id", userInfo.SYS_ID);
        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_WORK_OUT, body);
    }

    protected void setWorkOut() {
        UserInfo userInfo = PrefKit.getUserInfo(this);

        if (!TextUtils.isEmpty(mEditStartDate.getText().toString()) && !TextUtils.isEmpty(mEditEndDate.getText().toString())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startdate = null;
            Date endDate = null;
            try {
                startdate = sdf.parse(mEditStartDate.getText().toString());
                endDate = sdf.parse(mEditEndDate.getText().toString());

                int compare = startdate.compareTo(endDate);
                if (compare > 0) {
                    Toast.makeText(WorkOutActivity.this, "종료시간보다 시작시간이 미래일수는 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    if (userInfo.SYS_ID != "" && mEditContent_WorkOut.getText().length() != 0 && LATITUDE != "" && LONGITUDE != "") {
//                        String body = String.format("WOID=%s&SYSTEM_ID=%s&WORK_CONTENT=%s&IP_ADDRESS=%s&LATITUDE=%s&LONGITUDE=%s&START_WORK_DATETIME=%s&END_WORK_DATETIME=%s", WOID, userInfo.SYS_ID, mEditContent_WorkOut.getText().toString(), "", LATITUDE, LONGITUDE, mEditStartDate.getText().toString(), mEditEndDate.getText().toString());
                        HashMap<String, String> body = new HashMap<>();
                        body.put("WOID", WOID);
                        body.put("SYSTEM_ID", userInfo.SYS_ID);
                        body.put("WORK_CONTENT", mEditContent_WorkOut.getText().toString());
                        body.put("IP_ADDRESS", "");
                        body.put("LATITUDE", LATITUDE);
                        body.put("LONGITUDE", LONGITUDE);
                        body.put("START_WORK_DATETIME", mEditStartDate.getText().toString());
                        body.put("END_WORK_DATETIME", mEditEndDate.getText().toString());
                        new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_WORK_OUT_INSERT, body);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (userInfo.SYS_ID != "" && mEditContent_WorkOut.getText().length() != 0 && LATITUDE != "" && LONGITUDE != "") {
//                String body = String.format("WOID=%s&SYSTEM_ID=%s&WORK_CONTENT=%s&IP_ADDRESS=%s&LATITUDE=%s&LONGITUDE=%s&START_WORK_DATETIME=%s&END_WORK_DATETIME=%s", WOID, userInfo.SYS_ID, mEditContent_WorkOut.getText().toString(), "", LATITUDE, LONGITUDE, mEditStartDate.getText().toString(), mEditEndDate.getText().toString());
                HashMap<String, String> body = new HashMap<>();
                body.put("WOID", WOID);
                body.put("SYSTEM_ID", userInfo.SYS_ID);
                body.put("WORK_CONTENT", mEditContent_WorkOut.getText().toString());
                body.put("IP_ADDRESS", "");
                body.put("LATITUDE", LATITUDE);
                body.put("LONGITUDE", LONGITUDE);
                body.put("START_WORK_DATETIME", mEditStartDate.getText().toString());
                body.put("END_WORK_DATETIME", mEditEndDate.getText().toString());
                new TelKit(this, this, mLinearLayout).request(TelKit.URL_API_GET_WORK_OUT_INSERT, body);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
            alert_confirm.setMessage(Finish_Msg).setCancelable(false).setPositiveButton("네",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'YES'
                            finish();
                        }
                    }).setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 'No'
                            return;
                        }
                    });

            AlertDialog alert = alert_confirm.create();
            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mIsSucc && Kit.isNotNullNotEmpty(result.mResponse)) {
            if (result.mRequestUrl.equals(TelKit.URL_API_GET_WORK_OUT)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            JSONObject resultWork_out = json.optJSONObject("work_out");
                            Log.e(TAG, "getWorkOut onResult : " + resultWork_out);
                            mEditStartDate.setText(resultWork_out.optString("START_WORK_DATETIME").replace("null", ""));
                            mEditEndDate.setText(resultWork_out.optString("END_WORK_DATETIME").replace("null", ""));
                            mEditContent_WorkOut.setText(resultWork_out.optString("WORK_CONTENT").replace("null", ""));
                            mEditContent_WorkOut.setSelection(mEditContent_WorkOut.length());
                            mBtnRegister_WorkOut.setText("수정");
                            mBtnRegister_WorkOut.setEnabled(true);
                            mEditStartDate.setEnabled(false);
                            Finish_Msg = "외근등록 수정을 취소 하시겠습니까?";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(WorkOutActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (result.mRequestUrl.equals(TelKit.URL_API_GET_WORK_OUT_INSERT)) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    JSONObject resultObj = json.optJSONObject("result");
                    if (resultObj != null) {
                        String code = resultObj.optString("code");
                        String msg = resultObj.optString("msg");
                        if ("ok".equals(code)) {
                            Toast.makeText(WorkOutActivity.this, "외근등록이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(WorkOutActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(WorkOutActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(WorkOutActivity.this, "요청한 작업을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void showDateAndTimePickerDialog(final boolean type) {
        mSingleDateAndTimePickerDialog = new SingleDateAndTimePickerDialog.Builder(this);
        mSingleDateAndTimePickerDialog.minutesStep(30);
        mSingleDateAndTimePickerDialog.displayAmPm(false);
        mSingleDateAndTimePickerDialog.displayDays(false);
        mSingleDateAndTimePickerDialog.setDayFormatter(new SimpleDateFormat("MM월 dd일 E"));
        mSingleDateAndTimePickerDialog.mainColor(getResources().getColor(R.color.color_class_company));
        mSingleDateAndTimePickerDialog.displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
            @Override
            public void onDisplayed(SingleDateAndTimePicker picker) {
                picker.setDefaultDate(new Date());
            }

            @Override
            public void onClosed(SingleDateAndTimePicker singleDateAndTimePicker) {

            }
        });
        if (type) {
            mSingleDateAndTimePickerDialog.title("시작 시간");
        } else {
            mSingleDateAndTimePickerDialog.title("종료 시간");
        }

        mSingleDateAndTimePickerDialog.listener(new SingleDateAndTimePickerDialog.Listener() {
            @Override
            public void onDateSelected(Date date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (type) {
                    mEditStartDate.setText(sdf.format(date));
                } else {
                    mEditEndDate.setText(sdf.format(date));
                }

                mSingleDateAndTimePickerDialog = null;
            }
        });
        mSingleDateAndTimePickerDialog.display();
    }

    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    // 내 위치 정보 가져오기 Start.
    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "휴대폰 설정에서 앱 위치 권한 허용 체크 부탁 드립니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Logic to handle location object
                    LATITUDE = Double.toString(location.getLatitude());
                    LONGITUDE = Double.toString(location.getLongitude());
                    startLocationUpdates();
                } else {
                    createLocationRequest();
                }
            }
        });
    }

    protected void createLocationRequest() {
        SettingsClient client = LocationServices.getSettingsClient(this);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(WorkOutActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(WorkOutActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "휴대폰 설정에서 앱 위치 권한 허용 체크 부탁 드립니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public static void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "휴대폰에 위치 기능을 사용하지 않으시면 해당 서비스를 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }

    // 내 위치 정보 가져오기 End.
}
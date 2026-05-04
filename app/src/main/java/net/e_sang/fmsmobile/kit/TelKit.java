package net.e_sang.fmsmobile.kit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.e_sang.fmsmobile.MyApplication.AlertDialog_Check;
import static net.e_sang.fmsmobile.kit.Kit.TAG;
import static net.e_sang.fmsmobile.kit.Kit.isNotNullNotEmpty;

import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.BuildConfig;

public class TelKit {

    public static final String URL_API_BASE_PRD = BuildConfig.API_URL;  // 운영 서버
    //    public static final String URL_API_BASE_PRD = "http://10.4.0.28:9901/API";  // 백현재's 서버
    public static final String URL_API_BASE_DEV = "http://10.0.0.99:3100/API";  // 개발 서버

    // push notification
    public static final String URL_API_PUSH_TOKEN = "/USR/RegPushToken/";  // 푸시알림 토큰 등록

    // deploy
    public static final String URL_API_GET_VERSION = "/COM/GetVersion/";
    public static final String APK_FILE_NAME = BuildConfig.APK_FILE_NAME;
    public static final String URL_DOWNLOAD_APK = BuildConfig.URL_DOWNLOAD_APK + "files/" + APK_FILE_NAME;

    public static final String URL_API_USER_LOGIN = "/USR/UserLogin/";   // 사용자 로그인
    public static final String URL_API_USER_AUTO_LOGIN = "/USR/UserAutoLogin/";
    public static final String URL_API_FAIR_LIST = "/ADM/FairList/"; // 전체 행사 리스트
    public static final String URL_API_FAIR_MASTER_LIST = "/ADM/FairMasterList/"; // 전체 행사 마스터 리스트
    public static final String URL_API_MASTER_CODE = "/COM/MasterCode/";  // 마스터 코드
    public static final String URL_API_COMPANY_DEPOSIT_HISTORY = "/FAR/CompanyDepositHistory/";  // 입금내역 관리
    public static final String URL_API_COMPANY_FACIL_REQ = "/FAR/CompanyFacilReq/";  // 시설정보
    public static final String URL_API_GET_ASSIGN_LIST = "/ASN/getAssignList/";  // 배정 리스트
    public static final String URL_API_GET_SALES_REPORT_LIST = "/CPY/getSalesReportList/";  // 영업 리스트
    public static final String URL_API_GET_HOME_DASH_LIST = "/COM/getHomeDash/";  // 홈 현황판 전체
    public static final String URL_API_GET_COUNSEL = "/CPY/Get_CPY_sd_Counsel/";  // 영업세부정보
    public static final String URL_API_GET_FAIR_HISTORY = "/CPY/Get_CPY_sd_FairHistory/";  // 전시참가이력
    public static final String URL_API_GET_REG_COMPANY_INFO = "/CPY/getRegCompanyInfo/";  // 영업활동등록 상세 상단 화면 기본정보 / (업체담당자) / 행사선택 콤보박스 / 업체 담당자 콤보박스
    public static final String URL_API_EVENT_FAIR_LIST = "/FAR/InquiryFairList_Mob/";  // 행사목록
    public static final String URL_API_COMPANY_FAIR_SUMMARY = "/FAR/InquiryCompanyFairSummary/";  // 행사상세
    public static final String URL_API_GET_RECEIVABLE_LIST = "/CPY/getReceivableList/";  // 채권 / 채권(총채권금액)
    public static final String URL_API_GET_RECEIVABLE_DETAIL = "/CPY/getReceivableDetail/";  // 채권상세
    public static final String URL_API_SET_COUNSEL = "/CPY/ins_CPY_in_Counsel/";  // 영업활동 등록
    public static final String URL_API_GET_COMPANY_LIST = "/CPY/getCompanyList";  // 업체 검색
    public static final String URL_API_GET_PUSH_LIST = "/ADM/GetPushList";  // 알림 리스트
    public static final String URL_API_GET_READ_PUSH_LIST = "/ADM/ReadPushList";  // 알림 읽음 처리
    public static final String URL_API_GET_RECEIVABLE_FAIR_LIST = "/FAR/getReceivableFairList/";  // 채권관리 리스트
    public static final String URL_API_GET_PREREGISTRATION_FAIR_LIST = "/STA/getFairList/";  // 사전등록 리스트
    public static final String URL_API_GET_PREREGISTRATION_DETAIL = "/STA/getInquiryStatFair_Mob/";  // 사전등록 상세 리스트
    public static final String URL_API_GET_MASTER_CODE = "/COM/getInquiryMasterCode_Mob/";  // 연별입장객통계 행사목록 가져오기
    public static final String URL_API_GET_ANNUA_VISITOR_STATISTICES_FAIR_LIST = "/STS/getInquiryFair_Place_Mob/";  // 연별입장객통계 리스트
    public static final String URL_API_GET_ANNUA_VISITOR_STATISTICES_DETAIL = "/STA/getInquiryStatFairVisitor_Mob/";  // 연별입장객통계 상세 리스트
    public static final String URL_API_GET_VISITOR_STATUS_FAIR_LIST = "/STS/getInquiryEnteranceList_MOB/";  // 입장객현황 리스트
    public static final String URL_API_GET_VISITOR_STATUS_DETAIL = "/STS/getInquiryEnteranceStatus_MOB/";  // 입장객현황 상세
    public static final String URL_API_GET_WORK_OUT_LIST = "/ADM/InquiryWORK_OUT/";  // 외근활동 내역 리스트
    public static final String URL_API_GET_WORK_OUT_INSERT = "/ADM/InsertWORK_OUT/";  // 외근활동 등록/수정
    public static final String URL_API_GET_WORK_OUT = "/ADM/GetWORK_OUT/";  // 외근활동 내역 가져오기
    public static final String URL_API_GET_MY_WORK_OUT_LIST = "/ADM/InquiryMY_WORK_OUT/";  // 외근활동 내 목록 리스트
    public static final String URL_API_GET_REC_DASH = "/CPY/getRecDash/";  // 채권 행사 리스트 변경
    public static final String URL_API_READ_PUSH_LIST_ALL = "/ADM/ReadPushListAll/";  // 모든 알림 읽음 처리
    public static final String URL_API_GET_ASSIGN_LIST_AND_USERS = "/ASN/InquiryAssignReqCompany/";  // 배정요청 목록및 담당자 목록
    public static final String URL_API_SET_ASSIGN_STATUS = "/ASN/updateAssignStatus/";  // 배정요청 승인/반려 업데이트
    public static final String URL_API_GET_PREREGISTRATION_FAIR_TYPE_LIST = "/STA/getInquiryFair_For_Type_Mob/";  // 사전등록 타입 추가 리스트
    public static final String URL_API_GET_ACTION_PLAN_LIST = "/ADM/InquiryActionPlanList/";  // 일정관리 리스트
    public static final String URL_API_GET_ACTION_PLAN_DETAIL = "/ADM/GetActionPlan/";  // 일정관리 일자별 상세 항목
    public static final String URL_API_DELETE_ACTION_PLAN = "/ADM/DeleteActionPlan/";  // 일정관리 삭제
    public static final String URL_API_INSERT_ACTION_PLAN = "/ADM/InsertActionPlan/";  // 일정관리 등록/수정
    public static final String URL_API_INSERT_STAFF = "/CPY/InsertStaff/";  // 담당자 추가
    public static final String URL_API_GET_STAFF_ALL = "/CPY/GetCompanyStaffAll/";  // 담당자 명단
    //public static final MediaType TEXT = MediaType.parse("text/html; charset=utf-8");
    public static final String URL_API_INSERT_NOTICE = "/ADM/InsertNOTICE/";  // 동향보고 등록.

    public static final String URL_API_GET_SAME_PLACE_FAIRS = "/FAR/getSamePlaceFairs/";  // 특정 전시회의 같은 장소 전시회 목록 가져오기.
    public static final String URL_API_INQUIRY_VISITORS_STATS = "/STA/inquiryVisitorsStats/";  // 입장객 일자별 통계.
    public static final String URL_API_INQUIRY_VISITORS_STATS_HOURS = "/STA/inquiryVisitorsStatsByHours/";  // 입장객 시간별 통계.

    public static final String URL_API_PASSWORD_RESET = "/CPY/UpdatePassword/";
    public static final String URL_API_UPDATE_COUNSEL = "/CPY/UpdateCounsel/";
    public static final String URL_API_REGIST_ATTENDANCE_QRCODE = "/ONS/RegistAttendanceQRcode/";

    public static final String URL_API_PARSENAMECARD = "/CPY/ParseNameCard/";
    public static final String URL_API_OCR_STAFF_LIST = "/CPY/getOCRStaffs/";
    public static final String URL_API_OCR_STAFF_DETAIL = "/CPY/getOCRStaffDetail/";
    public static final String URL_API_INSERT_OCR_STAFF = "/CPY/InsertOCRStaff/";
    public static final String URL_API_STAFF_HISTORY = "/CPY/getOCRStaffColumnHistory/";

    public static final String URL_API_NAMECARD_LIST = "https://dev.e-sang.net/esgroup/nameCardDummyByRandom.json"; // 테스트 삭제

    private static final String TAG = "TelKit";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private OnResultListener mOnResultListener = null;
    private OkHttpClient mHttpClient = null;
    private ProgressBar mProgressBar = null;
    private View mProgressView = null;
    private ProgressDialog mProgressDialog = null;
    private Context mContext = null;
    private LinearLayout mLinearLayout = null;

    public static class Result {
        public boolean mIsSucc = false;
        public String mRequestUrl = "";
        public String mResponse = "";
        public int mRequestCode = 0;
    }

    public interface OnResultListener {
        void onResult(Result result);
    }

    public TelKit(Context context, OnResultListener listener) {
        mContext = context;
        mOnResultListener = listener;
        initHttpClient();
    }

    public TelKit(Context context, OnResultListener listener, ProgressBar progressBar) {
        this(context, listener);
        mProgressBar = progressBar;
    }

    public TelKit(Context context, OnResultListener listener, View progressView) {
        this(context, listener);
        mProgressView = progressView;
    }

    public TelKit(Context context, OnResultListener listener, ProgressDialog progressDialog) {
        this(context, listener);
        mProgressDialog = progressDialog;
    }

    public TelKit(Context context, OnResultListener listener, LinearLayout linearLayout) {
        this(context, listener);
        mLinearLayout = linearLayout;
    }

    private void initHttpClient() {
        if (mHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(20, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            mHttpClient = builder.build();
        }
    }

    public void request(String url, HashMap<String, String> body) {
        request(url, body, 0);
    }

    public void request(String url, String body) {
        Map<String, String> bodyMap = parseQuery(body);
        request(url, new HashMap<>(bodyMap), 0);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.trim().isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }

    public void request(String url, HashMap<String, String> body, int requestCode) {
        if (!isNetworkConnected(mContext)) {
            showNetworkAlertDialog();
            return;
        }

        showProgress();

        executorService.submit(() -> {
            Result result = new Result();
            result.mIsSucc = false;
            result.mRequestUrl = url;
            result.mRequestCode = requestCode;

            try {
                FormBody.Builder formBuilder = new FormBody.Builder();
                if (body != null) {
                    for (Map.Entry<String, String> entry : body.entrySet()) {
                        formBuilder.add(entry.getKey(), entry.getValue());
                    }
                }

                RequestBody requestBody = formBuilder.build();
                Request.Builder requestBuilder = new Request.Builder();

                String baseUrl = PrefKit.getTestMode(mContext) ? URL_API_BASE_DEV : URL_API_BASE_PRD;
                String fullUrl = baseUrl + url;

                String login_id = "";
                UserInfo userInfo = PrefKit.getUserInfo(mContext);

                if (userInfo != null && userInfo.LOGIN_ID != null && !userInfo.LOGIN_ID.isEmpty()) {
                    login_id = ":" + userInfo.LOGIN_ID;
                }
                requestBuilder.url(fullUrl)
                        .addHeader("Authorization", "basic " + Kit.getBase64encode(mContext, "fms:3D77DC1AB50A49A1BFB82A2C3126C6F8" + login_id))
                        .addHeader("APIVer", Kit.WEB_API_VER)
                        .post(requestBody);

                Response response = mHttpClient.newCall(requestBuilder.build()).execute();
                result.mResponse = response.body().string();
                result.mIsSucc = true;

            } catch (Exception e) {
                e.printStackTrace();
                result.mIsSucc = false;
                if (e instanceof SocketTimeoutException) {
                    exceptionShowToastAndExit("SocketTimeoutException.");
                } else if (e instanceof ConnectException) {
                    exceptionShowToastAndExit("ConnectException.");
                } else if (e instanceof UnknownHostException) {
                    exceptionShowToastAndExit("UnknownHostException.");
                } else {
                    exceptionShowToastAndExit(e.getClass().getName());
                }
            }

            mainHandler.post(() -> {
                hideProgress();
                if (mOnResultListener != null) {
                    mOnResultListener.onResult(result);
                }
            });
        });
    }

    public void requestMultipart(
            String url,
            HashMap<String, String> body,
            String fileParamName,
            File file,
            String mimeType,
            int requestCode
    ) {
        if (!isNetworkConnected(mContext)) {
            showNetworkAlertDialog();
            return;
        }

        showProgress();

        executorService.submit(() -> {
            Result result = new Result();
            result.mRequestUrl = url;
            result.mRequestCode = requestCode;
            result.mIsSucc = false;

            try {
                MultipartBody.Builder multipartBuilder =
                        new MultipartBody.Builder().setType(MultipartBody.FORM);

                // 일반 파라미터
                if (body != null) {
                    for (Map.Entry<String, String> entry : body.entrySet()) {
                        multipartBuilder.addFormDataPart(
                                entry.getKey(),
                                entry.getValue()
                        );
                    }
                }

                // 파일 파라미터
                if (file != null && file.exists()) {
                    RequestBody fileBody =
                            RequestBody.create(file, MediaType.parse(mimeType));

                    multipartBuilder.addFormDataPart(
                            fileParamName,
                            file.getName(),
                            fileBody
                    );
                }

                RequestBody requestBody = multipartBuilder.build();

                String baseUrl = PrefKit.getTestMode(mContext)
                        ? URL_API_BASE_DEV
                        : URL_API_BASE_PRD;

                String fullUrl = baseUrl + url;

                String login_id = "";
                UserInfo userInfo = PrefKit.getUserInfo(mContext);
                if (userInfo != null && !TextUtils.isEmpty(userInfo.LOGIN_ID)) {
                    login_id = ":" + userInfo.LOGIN_ID;
                }

                Request request = new Request.Builder()
                        .url(fullUrl)
                        .addHeader("Authorization",
                                "basic " + Kit.getBase64encode(
                                        mContext,
                                        "fms:3D77DC1AB50A49A1BFB82A2C3126C6F8" + login_id
                                ))
                        .addHeader("APIVer", Kit.WEB_API_VER)
                        .post(requestBody)
                        .build();

                Response response = mHttpClient.newCall(request).execute();
                result.mResponse = response.body().string();
                result.mIsSucc = response.isSuccessful();

            } catch (Exception e) {
                e.printStackTrace();
                exceptionShowToastAndExit(e.getClass().getSimpleName());
            }

            mainHandler.post(() -> {
                hideProgress();
                if (mOnResultListener != null) {
                    mOnResultListener.onResult(result);
                }
            });
        });
    }

    private void exceptionShowToastAndExit(String message) {
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, message + " 관리자에게 문의해 주세요.", Toast.LENGTH_LONG).show();
                if (activity.getClass().getName().contains("SplashActivity")) {
                    // 앱 종료
                    new Handler(Looper.getMainLooper()).postDelayed(activity::finishAffinity, 500);
                }
            });
        }
    }

    private void showProgress() {
        mainHandler.post(() -> {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            if (mProgressView != null) mProgressView.setVisibility(View.VISIBLE);
            if (mProgressDialog != null) mProgressDialog.show();
            if (mLinearLayout != null) mLinearLayout.setVisibility(View.VISIBLE);
        });
    }

    private void hideProgress() {
        mainHandler.post(() -> {
            if (mProgressBar != null) mProgressBar.setVisibility(View.INVISIBLE);
            if (mProgressView != null) mProgressView.setVisibility(View.INVISIBLE);
            if (mProgressDialog != null) mProgressDialog.dismiss();
            if (mLinearLayout != null) mLinearLayout.setVisibility(View.GONE);
        });
    }

    private void showNetworkAlertDialog() {
        mainHandler.post(() -> {
            if (!AlertDialog_Check) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.")
                        .setCancelable(false)
                        .setPositiveButton("확인", (dialog, which) -> {
                            AlertDialog_Check = false;
                            dialog.dismiss();
                        });
                AlertDialog alertDialog = builder.create();
                if (!alertDialog.isShowing()) {
                    AlertDialog_Check = true;
                    alertDialog.show();
                }
            }
        });
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static void tokenRegistrationToServer(Context context, String token, String sys_id, String usr_id) {
        if (!Kit.TOKEN_REGISTRATION_TO_SERVER)
            return;

        Kit.log(Kit.LogType.TELKIT, "tokenRegistrationToServer::token: " + token);

        if (!isNotNullNotEmpty(token))
            return;

        String device_id = PrefKit.getUserAdId(context);

        HashMap<String, String> bodyStr = new HashMap<>();
        bodyStr.put("SYSTEM_ID", sys_id);
        bodyStr.put("TOKEN_ID", token);
        bodyStr.put("DEVICE_ID", device_id);
        bodyStr.put("DEVICE_FLAG", "1");
        bodyStr.put("UPDATE_USER", usr_id);
        Kit.log(Kit.LogType.VALUE, "bodyStr = " + bodyStr);

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (!bodyStr.isEmpty()) {
            for (Map.Entry<String, String> entry : bodyStr.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody body = formBuilder.build();

        //request
        String url_api_base = PrefKit.getTestMode(context) ? URL_API_BASE_DEV : URL_API_BASE_PRD;
        String url = url_api_base + URL_API_PUSH_TOKEN;
        Kit.log(Kit.LogType.TELKIT, "tokenRegistrationToServer::url: " + url);

        String login_id = "";
        UserInfo userInfo = PrefKit.getUserInfo(context);

        if (userInfo != null && userInfo.LOGIN_ID != null && !userInfo.LOGIN_ID.isEmpty()) {
            login_id = ":" + userInfo.LOGIN_ID;
        }

        try {
            Request request = new Request.Builder()
                    .addHeader("Authorization", "basic " + Kit.getBase64encode(context, "fms:3D77DC1AB50A49A1BFB82A2C3126C6F8" + login_id))
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Kit.log(Kit.LogType.TELKIT, "onFailure::e.getLocalizedMessage() = " + e.getLocalizedMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Kit.log(Kit.LogType.TELKIT, "onResponse::response.body().string() = " + response.body().string());
                }
            });

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
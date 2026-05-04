package net.e_sang.fmsmobile.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.PrefKit;

public class FMSWebActivity extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    private WebView fms_web_view = null;
    private LinearLayout progressBar_layout = null;
    private CookieManager cookieManager = null;
    private final String DEFAULT_URL = "https://fms.esfair.kr/auth/index.aspx";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_primary);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fms_web);
        applyInsets();
        overridePendingTransition(0, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("FMS Browser");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        fms_web_view = findViewById(R.id.fms_web_view);
        progressBar_layout = findViewById(R.id.progressBar_layout);
        WebSettings webSettings = fms_web_view.getSettings();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                fms_web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                fms_web_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            fms_web_view.requestFocus(View.FOCUS_DOWN);
            fms_web_view.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            fms_web_view.setScrollbarFadingEnabled(false);
            //fms_web_view.setInitialScale(1);
            //fms_web_view.clearCache(true); // 웹뷰 캐시 삭제
            fms_web_view.clearHistory(); // 웹뷰 히스토리 삭제
            fms_web_view.setWebViewClient(new FMSWebViewClient());
            cookieManager = CookieManager.getInstance();

            webSettings.setJavaScriptEnabled(true); // 자바스크립트 사용여부
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트가 window.open()을 사용할 수 있도록 설정
            webSettings.setUseWideViewPort(true);   // 화면 사이즈 맞추기 허용여부
            webSettings.setLoadWithOverviewMode(true);  // html의 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
            webSettings.setSupportZoom(true);   // 화면 줌 허용여부
            webSettings.setBuiltInZoomControls(true);  // 안드로이드에서 제공하는 줌 아이콘을 사용할 수 있도록 설정
            webSettings.setDisplayZoomControls(false);  // 화면 줌 컨트롤 삭제
            webSettings.setDomStorageEnabled(true); // DOM(html 인식) 저장소 허용여부
            webSettings.setSupportMultipleWindows(true);    // 새창 띄우기 허용여부
            //webSettings.setAppCacheEnabled(false); // 앱 내부 캐시 사용 여부 설정
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 웹뷰가 캐시를 사용하지 않도록 설정
            webSettings.setDefaultTextEncodingName("UTF-8"); // 인코딩 설정

            //파일 허용
            webSettings.setAllowContentAccess(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setLoadsImagesAutomatically(true);  // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            // 사용자 정보 가져오기
            UserInfo userInfo = PrefKit.getUserInfo(this);
            Log.e(TAG, "UserInfo LOGIN_ID : " + userInfo.LOGIN_ID);
            Log.e(TAG, "UserInfo LOGIN_PWD : " + userInfo.LOGIN_PWD);

            //String postData = "id=" + URLEncoder.encode(userInfo.LOGIN_ID, "UTF-8") + "&password=" + URLEncoder.encode(userInfo.LOGIN_PWD, "UTF-8") + "&password_confirm=" + URLEncoder.encode("", "UTF-8");
            //fms_web_view.postUrl(DEFAULT_URL, postData.getBytes());
            fms_web_view.loadUrl(DEFAULT_URL);
        } catch (Exception e) {
            Log.e(TAG, "Exception : " + e);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fms_web_view.canGoBack()) {
                    if (fms_web_view.getUrl().equals(DEFAULT_URL) || fms_web_view.getUrl().equals("https://fms.esfair.kr/index.aspx") || fms_web_view.getUrl().equals("https://fms.esfair.kr/")) {
                        BackPressedDialog();
                    } else {
                        fms_web_view.goBack();
                    }
                } else {
                    BackPressedDialog();
                }
            }
        });
    }

    private class FMSWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar_layout.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
            Log.e(TAG, "onPageStarted : " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar_layout.setVisibility(View.GONE);
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished : " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //super.onReceivedSslError(view, handler, error);
            final AlertDialog.Builder builder = new AlertDialog.Builder(FMSWebActivity.this);
            builder.setMessage("이 사이트의 보안 인증서는 신뢰할 수 없습니다.");
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (fms_web_view.canGoBack()) {
                if (fms_web_view.getUrl().equals(DEFAULT_URL) || fms_web_view.getUrl().equals("https://fms.esfair.kr/index.aspx") || fms_web_view.getUrl().equals("https://fms.esfair.kr/")) {
                    BackPressedDialog();
                } else {
                    fms_web_view.goBack();
                }
            } else {
                BackPressedDialog();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        //super.onBackPressed();
//        if (fms_web_view.canGoBack()) {
//            if (fms_web_view.getUrl().equals(DEFAULT_URL) || fms_web_view.getUrl().equals("https://fms.esfair.kr/index.aspx") || fms_web_view.getUrl().equals("https://fms.esfair.kr/")) {
//                BackPressedDialog();
//            } else {
//                fms_web_view.goBack();
//            }
//        } else {
//            BackPressedDialog();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cookieManager != null) {
            CookieManager.getInstance().removeAllCookies(null);
        }
    }

    private void BackPressedDialog() {
        AlertDialog.Builder alert_ex = new AlertDialog.Builder(FMSWebActivity.this);
        alert_ex.setTitle("FMS Browser를 종료하시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog alert = alert_ex.create();
        alert.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fms_web_view != null) {
            fms_web_view.onPause();
            fms_web_view.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(fms_web_view != null) {
            fms_web_view.onResume();
            fms_web_view.resumeTimers();
        }
    }
}
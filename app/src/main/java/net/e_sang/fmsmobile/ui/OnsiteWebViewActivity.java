package net.e_sang.fmsmobile.ui;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class OnsiteWebViewActivity extends BaseActivity {
    private String TAG = getClass().getSimpleName();
    private WebView fms_webview = null;
    private LinearLayout progressBar_layout = null;
    public CookieManager cookieManager;
    private final String URL_BASE_LOGIN = "https://onsite.messeesang.com/manager";
    private final String URL_BASE_MAIN = "https://onsite.messeesang.com/Attendances";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_primary);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onsite_web_view);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("현장인력관리 관리자");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        progressBar_layout = findViewById(R.id.progressBar_layout);

        fms_webview = findViewById(R.id.fms_webview);
        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(fms_webview, true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(fms_webview, true);
        fms_webview.getSettings().setJavaScriptEnabled(true);
        fms_webview.getSettings().setDomStorageEnabled(true);
        fms_webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        fms_webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        fms_webview.getSettings().setLoadWithOverviewMode(true);
        fms_webview.getSettings().setBuiltInZoomControls(false);
        fms_webview.getSettings().setSupportZoom(true);
        fms_webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        fms_webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        fms_webview.getSettings().setUseWideViewPort(true);
        fms_webview.getSettings().setTextZoom(110);
        fms_webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        fms_webview.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Toast.makeText(getApplicationContext(), "파일을 다운로드 합니다.", Toast.LENGTH_LONG).show();
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    request.setMimeType(mimetype);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.addRequestHeader("Referer", getReferrer().getAuthority());
                    request.addRequestHeader("Content-Disposition", contentDisposition);
                    request.setDescription("파일 다운로드 중...");
                    String content = contentDisposition.substring(contentDisposition.indexOf("'") + 1);

                    String mFileName = content.replace("'", "").trim();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mFileName = URLDecoder.decode(mFileName, UTF_8);
                    } else {
                        mFileName = URLDecoder.decode(mFileName, "UTF-8");
                    }

                    request.setTitle(mFileName);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mFileName);
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                    dm.enqueue(request);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "파일 다운로드를 실패 하였습니다.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });


        fms_webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("home")) {
                    view.loadUrl(URL_BASE_MAIN);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "onPageStarted Url : " + fms_webview.getUrl());
                progressBar_layout.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e(TAG, "onPageFinished Url : " + fms_webview.getUrl());
                progressBar_layout.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });

        fms_webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView newWebView = new WebView(view.getContext());

                // 새 WebView 설정 (팝업 콘텐츠 보여줄 용도)
                newWebView.getSettings().setJavaScriptEnabled(true);
                newWebView.setWebViewClient(new WebViewClient());

                // 팝업을 다이얼로그 또는 새로운 Activity/Fragment로 띄우려면 여기에 구현
                AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();
                dialog.setView(newWebView);
                dialog.show();

                // WebViewTransport를 통해 새 WebView 연결
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                return true;
            }
        });

        fms_webview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.e(TAG, "fms_webview KEYCODE_BACK getUrl : " + fms_webview.getUrl());
                    if (fms_webview.canGoBack()) {
                        if (fms_webview.getUrl().equals(URL_BASE_LOGIN) || fms_webview.getUrl().equals(URL_BASE_MAIN)) {
                            finish();
                        } else {
                            fms_webview.goBack();
                        }
                    } else {
                        if (fms_webview.getUrl().equals(URL_BASE_LOGIN) || fms_webview.getUrl().equals(URL_BASE_MAIN)) {
                            finish();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        UserInfo userInfo = PrefKit.getUserInfo(this);

        String postData = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            postData = "LoginId=" + URLEncoder.encode(userInfo.LOGIN_ID, UTF_8)
                    + "&Password=" + URLEncoder.encode(userInfo.LOGIN_PWD, UTF_8);
        } else {
            try {
                postData = "LoginId=" + URLEncoder.encode(userInfo.LOGIN_ID, "UTF-8")
                        + "&Password=" + URLEncoder.encode(userInfo.LOGIN_PWD, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                postData = "";
            }
        }
        fms_webview.postUrl(URL_BASE_LOGIN, postData.getBytes());
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        if (cookieManager != null) {
            CookieManager.getInstance().removeAllCookies(null);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fms_webview != null) {
            fms_webview.onPause();
            fms_webview.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fms_webview != null) {
            fms_webview.onResume();
            fms_webview.resumeTimers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (fms_webview.canGoBack()) {
                if (fms_webview.getUrl().equals(URL_BASE_LOGIN) || fms_webview.getUrl().equals(URL_BASE_MAIN)) {
                    finish();
                } else {
                    fms_webview.goBack();
                }
            } else {
                if (fms_webview.getUrl().equals(URL_BASE_LOGIN) || fms_webview.getUrl().equals(URL_BASE_MAIN)) {
                    finish();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

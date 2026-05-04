package net.e_sang.fmsmobile.ui;

import static net.e_sang.fmsmobile.ui.NoticeActivity.notice;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.*;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.delight.android.webview.AdvancedWebView;

import net.e_sang.fmsmobile.R;

import java.net.MalformedURLException;
import java.net.URL;

public class WebViewActivity extends BaseActivity implements AdvancedWebView.Listener {
    private String TAG = getClass().getSimpleName();
    private AdvancedWebView fms_webview = null;
    private LinearLayout progressBar_layout = null;
    public CookieManager cookieManager;
    private TextView title_noti, user_noti = null;
    private String SUBJECT = "";
    private String USER_NAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_notification);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_class_notification));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("알림 상세");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        cookieManager = CookieManager.getInstance();

        progressBar_layout = findViewById(R.id.progressBar_layout);
        title_noti = findViewById(R.id.title_noti);
        user_noti = findViewById(R.id.user_noti);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent != null) {
            SUBJECT = intent.getExtras().getString("SUBJECT");
            USER_NAME = intent.getExtras().getString("USER_NAME");
        }

        title_noti.setText(SUBJECT);
        user_noti.setText(USER_NAME);
        fms_webview = findViewById(R.id.fms_webview);
        fms_webview.setListener(this, this);
        fms_webview.setGeolocationEnabled(false);
        fms_webview.setMixedContentAllowed(true);
        fms_webview.setCookiesEnabled(true);
        fms_webview.setThirdPartyCookiesEnabled(true);
        fms_webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        fms_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        fms_webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //fms_webview.getSettings().setAppCacheEnabled(true);
        fms_webview.getSettings().setLoadWithOverviewMode(true);
//        fms_webview.getSettings().setUseWideViewPort(true);
        fms_webview.getSettings().setBuiltInZoomControls(true);
        fms_webview.getSettings().setSupportZoom(true);
        fms_webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Uri uri = Uri.parse(url);
                    String scheme = uri.getScheme();
                    if ("http".equals(scheme) || "https".equals(scheme)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        fms_webview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

        });
        fms_webview.addHttpHeader("X-Requested-With", "");
        //fms_webview.loadUrl(DEFAULT_PAGE_URL);
        Log.e(TAG, "notice.CONTENT : " + notice.CONTENT);
        //fms_webview.loadData(notice.CONTENT, "text/html", "UTF-8");
        fms_webview.loadDataWithBaseURL(null, notice.CONTENT, "text/html; charset=utf-8", "utf-8", null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (fms_webview.canGoBack()) {
                    fms_webview.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        Log.e(TAG, "onPageStarted");
        progressBar_layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(String url) {
        Log.e(TAG, "onPageFinished");
        progressBar_layout.setVisibility(View.GONE);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        Log.e(TAG, "onPageError");
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        Log.e(TAG, "onDownloadRequested");
    }

    @Override
    public void onExternalPageRequest(String url) {
        Log.e(TAG, "onExternalPageRequest");
    }

    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        fms_webview.onDestroy();
        if (cookieManager != null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                CookieManager.getInstance().removeAllCookies(null);
            } else {
                CookieManager.getInstance().removeAllCookie();
            }
        }
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        if (!fms_webview.onBackPressed()) {
//            return;
//        }
//        super.onBackPressed();
//    }

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
            if (!fms_webview.onBackPressed()) {
                return true;
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

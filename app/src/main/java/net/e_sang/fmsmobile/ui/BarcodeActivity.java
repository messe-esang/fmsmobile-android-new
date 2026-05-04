package net.e_sang.fmsmobile.ui;

import android.content.DialogInterface;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.data.UserInfo;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.PrefKit;
import net.e_sang.fmsmobile.kit.TelKit;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;


public class BarcodeActivity extends BaseActivity implements TelKit.OnResultListener, DecoratedBarcodeView.TorchListener {
    private String TAG = getClass().getSimpleName();
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ViewfinderView viewfinderView;
    private boolean captureFlag = false;
    private BeepManager beepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_primary);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        applyInsets();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary));
            setSupportActionBar(toolbar);
            TextView txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            txtToolbarTitle.setText("현장인력관리");
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_chevron_left_white_36);
        }

        viewfinderView = findViewById(R.id.zxing_viewfinder_view);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scannern);
        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                barcodeScannerView.pause();
                beepManager.playBeepSoundAndVibrate();
                try {
                    if (!captureFlag) {
                        Log.e(TAG, "result.toString() : " + result.toString());
                        sendQRCode(result.toString());
                    }

                    captureFlag = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);

        beepManager = new BeepManager(this);
        beepManager.setBeepEnabled(true);

        changeMaskColor(null);
        changeLaserVisibility(true);
    }

    public void sendQRCode(String code) {
        UserInfo userInfo = PrefKit.getUserInfo(this);
        HashMap<String, String> body = new HashMap<>();
        body.put("qrcode", code);
        body.put("loginId", userInfo.LOGIN_ID);
        new TelKit(this, this).request(TelKit.URL_API_REGIST_ATTENDANCE_QRCODE, body);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void changeMaskColor(View view) {
        Random rnd = new Random();
        int color = Color.argb(180, rnd.nextInt(1), rnd.nextInt(1), rnd.nextInt(1));
        viewfinderView.setMaskColor(color);
    }

    public void changeLaserVisibility(boolean visible) {
        viewfinderView.setLaserVisibility(visible);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mRequestUrl.equals(TelKit.URL_API_REGIST_ATTENDANCE_QRCODE)) {
            if (!result.mResponse.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(result.mResponse);
                    String code = json.optString("code");
                    String msg = json.optString("msg");
                    Log.e(TAG, "code : " + code);
                    Log.e(TAG, "msg : " + msg);

                    AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.qrscan_alert_layout, null);
                    TextView message = dialogView.findViewById(R.id.qr_msg);
                    message.setText(msg);
                    builder.setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    captureFlag = false;
                                    barcodeScannerView.resume();
                                    dialog.dismiss();
                                }
                            }).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.qrcode_menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_onsite) {
            Kit.startActivity(this, OnsiteWebViewActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

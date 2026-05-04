package net.e_sang.fmsmobile.namecard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.common.util.concurrent.ListenableFuture;

import net.e_sang.fmsmobile.R;
import net.e_sang.fmsmobile.kit.CameraOCROverlayView;
import net.e_sang.fmsmobile.kit.Kit;
import net.e_sang.fmsmobile.kit.SoundManager;
import net.e_sang.fmsmobile.ui.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public class NameCardCameraActivity extends BaseActivity {
    private String TAG = getClass().getSimpleName();
    private PreviewView previewView;
    private CameraOCROverlayView overlayView;
    private ImageCapture imageCapture;
    //private MediaActionSound shutterSound;
    private boolean mtPortrait = false;

    private long detectStartTime = 0;
    private boolean autoCaptureTriggered = false;
    private TextView txt_auto_text;
    private SoundManager soundManager;

    // ====== 갤러리 선택 ======
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    // ====== Crop 결과 ======
    private final ActivityResultLauncher<CropImageContractOptions> cropLauncher =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    Uri croppedUri = result.getUriContent();
                    Log.e(TAG, "croppedUri : " + croppedUri.toString());
                    Intent intent = new Intent(this, NameCardEditActivity.class);
                    intent.putExtra("image_uri", croppedUri.toString());
                    intent.putExtra("EDIT_TYPE", "0");
                    setResult(RESULT_OK, intent);
                    startActivity(intent);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.color_class_company);
        setStatusColor(themeColor, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_card_camera);
        applyInsets();
        previewView = findViewById(R.id.previewView);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        overlayView = findViewById(R.id.overlayView);
        txt_auto_text = findViewById(R.id.txt_auto_text);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        // 시스템 셔터음 사용
        //shutterSound = new MediaActionSound();
        //shutterSound.load(MediaActionSound.SHUTTER_CLICK);
        soundManager = new SoundManager(this);
        soundManager.setVolume(1.0f);

//        findViewById(R.id.btnCapture).setOnClickListener(v -> takePicture());
//        findViewById(R.id.btnGallery).setOnClickListener(v -> openGallery());

        findViewById(R.id.btnCapture).setOnClickListener(new Kit.OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                takePicture();
            }
        });
        findViewById(R.id.btnGallery).setOnClickListener(new Kit.OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                openGallery();
            }
        });
        startCamera();

        CameraOCROverlayView overlay = findViewById(R.id.overlayView);
        overlay.setPortrait(mtPortrait);

        Button btn_Portrait = findViewById(R.id.btn_Portrait);
        btn_Portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mtPortrait) {
                    mtPortrait = false;
                    overlay.setPortrait(mtPortrait);
                    btn_Portrait.setText("세로\n명함");
                    overlay.refreshOverlay();
                } else {
                    mtPortrait = true;
                    overlay.setPortrait(mtPortrait);
                    btn_Portrait.setText("가로\n명함");
                    overlayView.refreshDrawableState();
                    overlay.refreshOverlay();
                }

            }
        });
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                int WIDTH = 0;
                int HEIGHT = 0;
                if (mtPortrait) {
                    WIDTH = 480;
                    HEIGHT = 640;
                } else {
                    WIDTH = 640;
                    HEIGHT = 480;
                }
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .setTargetResolution(new Size(WIDTH, HEIGHT)) // 성능용
                        .build();

                analysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(),
                        new CardAnalyzer()
                );

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture,
                        analysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePicture() {
        if (imageCapture == null) {
            Log.e(TAG, "imageCapture is null. Camera might not be initialized yet.");
            return;
        }

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {

                        Bitmap bitmap = imageProxyToBitmap(image);
                        image.close();

                        Bitmap cropped = cropByOverlay(bitmap);

                        boolean detected = isCardLikely(cropped);
                        overlayView.setDetected(detected);

                        if (!detected) {
                            Toast.makeText(
                                    NameCardCameraActivity.this,
                                    "명함을 가이드에 맞춰주세요",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        //playShutterSound();
                        soundManager.playShutter(); // 🔊 셔터음
                        returnResult(cropped);
                    }
                }
        );
    }

    private void returnResult(Bitmap croppedBitmap) {
        Uri uri = saveBitmapAndGetUri(croppedBitmap);
        if (uri == null) return;
        startCrop(uri);
    }


    private Uri saveBitmapAndGetUri(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "card_" + System.currentTimeMillis() + ".jpg");

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== ImageProxy → Bitmap =====
    private Bitmap imageProxyToBitmap(ImageProxy image) {

        if (image.getFormat() == ImageFormat.JPEG) {
            // ✅ JPEG은 Plane 1개
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return rotateBitmap(bitmap, image.getImageInfo().getRotationDegrees());
        }

        // ↓↓↓ YUV 인 경우만 아래 실행
        ImageProxy.PlaneProxy[] planes = image.getPlanes();

        ByteBuffer y = planes[0].getBuffer();
        ByteBuffer u = planes[1].getBuffer();
        ByteBuffer v = planes[2].getBuffer();

        byte[] nv21 = new byte[y.remaining() + u.remaining() + v.remaining()];
        y.get(nv21, 0, y.remaining());
        v.get(nv21, y.remaining(), v.remaining());
        u.get(nv21, y.remaining() + v.remaining(), u.remaining());

        YuvImage yuvImage = new YuvImage(
                nv21,
                ImageFormat.NV21,
                image.getWidth(),
                image.getHeight(),
                null
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(
                new Rect(0, 0, image.getWidth(), image.getHeight()),
                90,
                out
        );

        Bitmap bitmap = BitmapFactory.decodeByteArray(
                out.toByteArray(), 0, out.size()
        );

        return rotateBitmap(bitmap, image.getImageInfo().getRotationDegrees());
    }


    private Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        if (rotation == 0) return bitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // ===== Overlay 기준 Crop =====
    private Bitmap cropByOverlay(Bitmap bitmap) {
        RectF rect = overlayView.getCardRect();

        float scaleX = (float) bitmap.getWidth() / previewView.getWidth();
        float scaleY = (float) bitmap.getHeight() / previewView.getHeight();

        return Bitmap.createBitmap(
                bitmap,
                (int) (rect.left * scaleX),
                (int) (rect.top * scaleY),
                (int) ((rect.right - rect.left) * scaleX),
                (int) ((rect.bottom - rect.top) * scaleY)
        );
    }

    private boolean isCardLikely(Bitmap bitmap) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int edge = 0;
        int horizontal = 0;
        int vertical = 0;
        int step = 8; // 촘촘하게
        int count = 0;

        for (int y = h / 4; y < h * 3 / 4 - step; y += step) {
            for (int x = w / 4; x < w * 3 / 4 - step; x += step) {

                count++;

                int c = getGray(bitmap.getPixel(x, y));
                int r = getGray(bitmap.getPixel(x + step, y));
                int b = getGray(bitmap.getPixel(x, y + step));

                boolean hasEdge = false;

                int diffThreshold = 15;
                if (Math.abs(c - r) > diffThreshold) { horizontal++; hasEdge = true; }
                if (Math.abs(c - b) > diffThreshold) { vertical++; hasEdge = true; }

                if (hasEdge) edge++;
            }
        }

        float ratio = edge / (float) count;

        // ✅ 감지 허용 범위 넓힘
        return ratio > 0.02f && ratio < 0.35f
                && horizontal > 5
                && vertical > 5;
    }

    private int getGray(int color) {
        return (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
    }

    private void playShutterSound() {
//        if (shutterSound != null) {
//            shutterSound.play(MediaActionSound.START_VIDEO_RECORDING);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
            soundManager = null;
        }
    }

    private class CardAnalyzer implements ImageAnalysis.Analyzer {

        private long lastUpdate = 0;

        @Override
        public void analyze(@NonNull ImageProxy image) {
            try {
                // 너무 자주 UI 업데이트 방지 (300ms)
                long now = System.currentTimeMillis();

                if (now - lastUpdate < 300) {
                    image.close();
                    return;
                }
                lastUpdate = now;

                Bitmap bitmap = imageProxyToBitmap(image);
                Bitmap cropped = cropCenter(bitmap, mtPortrait);
                boolean detected = isCardLikely(cropped);
                //runOnUiThread(() -> overlayView.setDetected(detected));

                runOnUiThread(() -> {
                    overlayView.setDetected(detected);
                    handleAutoCapture(detected);
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        }
    }

    private void handleAutoCapture(boolean detected) {

        long now = System.currentTimeMillis();

        if (detected) {

            if (detectStartTime == 0) {
                detectStartTime = now;
            }

            long elapsed = now - detectStartTime;
            int remain = 2 - (int) (elapsed / 1000);

            if (remain > 0) {
                txt_auto_text.setText(remain + "초 유지하면 촬영됩니다");
            }

            if (elapsed >= 2000 && !autoCaptureTriggered) {

                autoCaptureTriggered = true;
                txt_auto_text.setText("촬영합니다");

                takePicture(); // CameraX 촬영 함수
            }

        } else {

            // 명함 사라지면 초기화
            detectStartTime = 0;
            autoCaptureTriggered = false;
            txt_auto_text.setText("");
        }
    }

    private Bitmap cropCenter(Bitmap bitmap, boolean isPortrait) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        float cropRatio = isPortrait ? 0.70f : 0.85f; // 세로 명함은 좀 더 세로 영역 확보
        int cropWidth, cropHeight;

        if (isPortrait) {
            cropHeight = (int) (h * cropRatio);
            cropWidth = (int) (cropHeight * 5f / 9f); // 세로 명함 비율 5:9
        } else {
            cropWidth = (int) (w * cropRatio);
            cropHeight = (int) (cropWidth * 5f / 9f); // 가로 명함 비율 9:5
        }

        int left = (w - cropWidth) / 2;
        int top = (h - cropHeight) / 2;

        return Bitmap.createBitmap(bitmap, left, top, cropWidth, cropHeight);
    }


    // ====== 갤러리 실행 ======
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    // ====== Crop 시작 ======
    private void startCrop(Uri uri) {
        txt_auto_text.setText("");
        CropImageOptions options = new CropImageOptions();
        options.cropMenuCropButtonTitle = "저장";
        cropLauncher.launch(new CropImageContractOptions(uri, options));
    }
}
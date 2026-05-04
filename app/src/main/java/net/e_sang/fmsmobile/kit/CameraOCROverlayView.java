package net.e_sang.fmsmobile.kit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CameraOCROverlayView extends View {

    private RectF cardRect;
    private final Paint borderPaint = new Paint();
    private final Paint dimPaint = new Paint();

    private boolean detected = false;

    // 1. 가로/세로 비율 추가
    private float cardRatio = 9f / 5f; // 기본 가로 명함
    private boolean isPortrait = false; // 세로 명함 여부

    public CameraOCROverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(12f);
        borderPaint.setAntiAlias(true);

        dimPaint.setColor(Color.parseColor("#88000000"));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float width = w * 0.85f;
        float height = width / 1.8f; // 명함 비율 9:5

        if (isPortrait) {
            // 세로 명함: 세로 길이를 기준으로 계산
            height = h * 0.70f;
            width = height / cardRatio;
        } else {
            // 가로 명함: 가로 길이를 기준으로 계산
            width = w * 0.85f;
            height = width / cardRatio;
        }

        float left = (w - width) / 2f;
        float top = (h - height) / 2f;

        cardRect = new RectF(left, top, left + width, top + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cardRect == null) return;
        // 어두운 영역
        canvas.drawRect(0, 0, getWidth(), cardRect.top, dimPaint);
        canvas.drawRect(0, cardRect.bottom, getWidth(), getHeight(), dimPaint);
        canvas.drawRect(0, cardRect.top, cardRect.left, cardRect.bottom, dimPaint);
        canvas.drawRect(cardRect.right, cardRect.top, getWidth(), cardRect.bottom, dimPaint);

        // 명함 테두리
        borderPaint.setColor(detected ? Color.GREEN : Color.WHITE);
        canvas.drawRect(cardRect, borderPaint);
    }

    public RectF getCardRect() {
        return cardRect;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
        invalidate();
    }

    // 2. 세로/가로 전환 메서드
    public void setPortrait(boolean portrait) {
        this.isPortrait = portrait;
        invalidate(); // 다시 그리기
        requestLayout(); // 레이아웃 재계산
    }

    public void refreshOverlay() {
        // cardRect를 다시 계산
        int w = getWidth();
        int h = getHeight();

        if (w == 0 || h == 0) return; // 아직 레이아웃이 안 끝난 경우

        float width, height;
        if (isPortrait) {
            height = h * 0.70f;
            width = height / cardRatio;
        } else {
            width = w * 0.85f;
            height = width / cardRatio;
        }

        float left = (w - width) / 2f;
        float top = (h - height) / 2f;

        cardRect = new RectF(left, top, left + width, top + height);

        invalidate();  // 다시 그리기
    }
}
package org.lh.camera;

import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lh on 14-1-24.
 */
public class OverlayView extends View {

    private Paint mPaint;

    private CountDownTimer diminishTimer;
    private Matrix cameraToViewMatrix;
    private boolean isWaitForDraw;

    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);

        diminishTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                //TODO
            }

            @Override
            public void onFinish() {
                arcs.clear();
                invalidate();
            }
        };
        isWaitForDraw = false;
    }

    List<RectF> arcs = new ArrayList<RectF>();

    public void drawFaces(Camera.Face[] faces) {

        if (null == cameraToViewMatrix) {
            return;
        }

        if (diminishTimer != null) {
            diminishTimer.cancel();
        }

        arcs.clear();

        for (int i = 0; i < faces.length; i++) {
            RectF dst = new RectF();
            cameraToViewMatrix.mapRect(dst, new RectF(faces[i].rect));

            arcs.add(dst);
        }
        if (!isWaitForDraw) {
            isWaitForDraw = true;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawARGB(0, 1, 1, 1);

        if (isWaitForDraw) {
            for (RectF rect : arcs) {
                canvas.drawArc(rect, 0, 360, false, mPaint);
            }
            isWaitForDraw = false;
        }
        if (diminishTimer != null) {
            diminishTimer.start();
        }
    }

    public void cameraToViewCoordinate(CameraPreview preview) {
        cameraToViewMatrix = preview.getCameraToViewMatrix();
    }
}

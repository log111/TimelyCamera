package org.lh.camera;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lh on 14-1-24.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private boolean faceDetectAvailable;
    private Camera.AutoFocusCallback focusCallback;
    private Matrix cameraToViewMatrix;

    public CameraPreview(Context context) {
        super(context);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                if (faceDetectAvailable) {
                    mCamera.startFaceDetection();
                }
                if(focusCallback != null){
                    mCamera.autoFocus(focusCallback);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (null == mCamera) {
            return;
        }

        if (null == holder.getSurface()) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            if (faceDetectAvailable) {
                mCamera.startFaceDetection();
            }
            if(focusCallback != null){
                mCamera.autoFocus(focusCallback);
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            if (faceDetectAvailable) {
                mCamera.stopFaceDetection();
            }
        }
    }

    public void setupCamera() {

        mCamera = Camera.open();
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);

            cameraToViewMatrix = new Matrix();
            cameraToViewMatrix.setScale(1, 1);
            cameraToViewMatrix.postRotate(90);
            cameraToViewMatrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
            cameraToViewMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);

            Rect rect = new Rect(-125, -125, 125, 125);
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            areas.add(new Camera.Area(rect, 1000));
            params.setMeteringAreas(areas);
            mCamera.setParameters(params);
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean startFaceDetection(Camera.FaceDetectionListener callback) {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            int maxFaceNum = params.getMaxNumDetectedFaces();
            if (maxFaceNum > 0) { // check that metering areas are supported
                mCamera.setFaceDetectionListener(callback);
                faceDetectAvailable = true;
                return true;
            }
        }
        faceDetectAvailable = false;
        return false;
    }

    public boolean startAutoFocus(Camera.AutoFocusCallback callback) {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            String mode = params.getFocusMode();
            if (Camera.Parameters.FOCUS_MODE_AUTO.equals(mode)) {
                focusCallback = callback;
                return true;
            }
        }
        return false;
    }

    public Matrix getCameraToViewMatrix(){
        return cameraToViewMatrix;
    }
}

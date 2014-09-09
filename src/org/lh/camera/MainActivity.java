package org.lh.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by lh on 14-1-21.
 */
public class MainActivity extends Activity {

    CameraPreview preview;
    Button shotBtn;
    Button previewBtn;
    TextView counterView;
    private File picDir;
    private TextToSpeech mTts;
    private boolean ttsAvailable;
    private OverlayView notifyView;
    private MediaPlayer soundPlayer;

    private CountDownTimer shotTimer = new CountDownTimer(15000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            long leftSeconds = millisUntilFinished / 1000;
            counterView.setText("" + leftSeconds);
            if (ttsAvailable) {
                mTts.speak(leftSeconds + "", TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        @Override
        public void onFinish() {

            counterView.setVisibility(View.GONE);
            Camera camera = preview.getCamera();
            camera.takePicture(shutterCb, null, shotJPEGCb);
        }
    };

    Camera.AutoFocusCallback focusCb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

            android.util.Log.i("lh", "auto focus: " + success);
            if(success){
                //TODO
            }
        }
    };

    Camera.AutoFocusMoveCallback focusMoveCb = new Camera.AutoFocusMoveCallback() {

        @Override
        public void onAutoFocusMoving(boolean start, Camera camera) {
            android.util.Log.i("lh", "auto focus move: " + start);
        }
    };

    Camera.PictureCallback shotJPEGCb = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                File picFile = getOutputMediaFile();
                FileOutputStream fout = new FileOutputStream(picFile);
                fout.write(data);
                fout.close();

                addToGallery(picFile);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Camera.ShutterCallback shutterCb = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            if (soundPlayer != null) {
                soundPlayer.start();
            }
        }
    };

    TextToSpeech.OnInitListener ttsInitCb = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (TextToSpeech.SUCCESS == status) {
                ttsAvailable = true;
            }
        }
    };

    Camera.FaceDetectionListener faceDetectCb = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                Log.i("lh", "face detected: " + faces.length +
                        " Face 1 Location X: " + faces[0].rect.centerX() +
                        "Y: " + faces[0].rect.centerY());


//                Camera.Parameters params = camera.getParameters();
//                if (params.getMaxNumMeteringAreas() > 0) {
//
//                    int min = 2000;
//                    int mark = -1;
//                    for (int i = 0; i < faces.length; i++) {
//                        int w = faces[i].rect.right - faces[i].rect.left;
//                        if (w < min) {
//                            min = w;
//                            mark = i;
//                        }
//                    }
//
//                    List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
//                    meteringAreas.add(new Camera.Area(faces[mark].rect, 100));
//                    params.setMeteringAreas(meteringAreas);
//
//                    camera.setParameters(params);
//                }

                notifyView.drawFaces(faces);//two frequent that the draw commands will miss their place.

//                Camera.Parameters params = camera.getParameters();
//                if(params.getMaxNumFocusAreas() > 0){
//                    List<Camera.Area> areas = new ArrayList<Camera.Area>();
//                    areas.add(new Camera.Area(faces[0].rect, 1000));
//                    params.setFocusAreas(areas);
//                }

                //camera.setParameters(params);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        preview = (CameraPreview) findViewById(R.id.preview);
        shotBtn = (Button) findViewById(R.id.shotBtn);
        previewBtn = (Button) findViewById(R.id.previewBtn);
        counterView = (TextView) findViewById(R.id.counter);
        notifyView = (OverlayView) findViewById(R.id.overlay);
        

        shotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (preview.getCamera() != null) {

                    shotBtn.setEnabled(false);
                    previewBtn.setEnabled(true);

                    counterView.setVisibility(View.VISIBLE);

                    shotTimer.start();
                } else {
                    Toast.makeText(MainActivity.this, "No camera available", Toast.LENGTH_LONG);
                }
            }
        });

        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (preview.getCamera() != null) {
                    shotBtn.setEnabled(true);
                    previewBtn.setEnabled(false);

                    preview.getCamera().startPreview();
                } else {
                    Toast.makeText(MainActivity.this, "No camera available", Toast.LENGTH_LONG);
                }
            }
        });
        previewBtn.setEnabled(false);
    }


    @Override
    protected void onStart() {
        super.onStart();

        ttsAvailable = false;
        mTts = new TextToSpeech(this, ttsInitCb);

        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.camera_shutter_click_03);
        if (afd != null) {
            soundPlayer = new MediaPlayer();
            try {
                soundPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                soundPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        preview.setupCamera();
        notifyView.cameraToViewCoordinate(preview);

        preview.startFaceDetection(faceDetectCb);
        preview.startAutoFocus(focusCb);
        previewBtn.setEnabled(false);
        shotBtn.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        preview.releaseCamera();
    }

    @Override
    protected void onStop() {
        mTts.shutdown();
        shotTimer.cancel();
        if (soundPlayer != null) {
            soundPlayer.stop();
        }

        super.onStop();
    }

    private File getOutputMediaFile() throws IOException {

        if (null == picDir) {
            picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "TimelyCamera");
            if (!picDir.exists()) {
                picDir.mkdirs();
            }
        }
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = df.format(new Date());
        File ret = new File(picDir, "IMG_" + timeStamp + ".jpg");
        if (!ret.exists()) {
            ret.createNewFile();
        }
        return ret;
    }


    private void addToGallery(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(photo);
        mediaScanIntent.setData(uri);
        sendBroadcast(mediaScanIntent);
    }
}

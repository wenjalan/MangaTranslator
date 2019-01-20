package wenjalan.mangatranslator;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    // viewfinder size scale
    public static final float VF_2_SCREEN_X = 0.5f;
    public static final float VF_2_SCREEN_Y = 0.5f;

    // the camera
    Camera camera;

    // the tag
    public static final String TAG = "MT-Main";

    // the camera manager
    private CameraManager cameraManager;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize camera
        initCamera();

        // init capture button
        initCaptureButton();

        // init view finder
        initViewfinder();
    }

    // camera init
    private void initCamera() {
        // init manager
        cameraManager = new CameraManager(this);

        // open the camera
        camera = cameraManager.getCamera();

        // set the camera to portrait
        camera.setDisplayOrientation(90);

        // enable auto focus
        Camera.Parameters par = camera.getParameters();
        par.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(par);

        // create the preview
        cameraPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
    }

    // capture button
    private void initCaptureButton() {
        // set the capture button listener
        Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, cameraManager.getPictureCallback());
            }
        });
    }

    // init viewfinder
    private void initViewfinder() {
        // size the viewfinder box
        final View viewfinder = findViewById(R.id.box);

        // get the dimensions after they've been laid out
        viewfinder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View container = (View) viewfinder.getParent().getParent();
                int containerHeight = container.getHeight();
                int containerWidth = container.getWidth();
                ViewGroup.LayoutParams params = viewfinder.getLayoutParams();
                params.height = (int) (containerHeight * VF_2_SCREEN_Y);
                params.width = (int) (containerWidth * VF_2_SCREEN_X);
                viewfinder.requestLayout();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        initCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraManager.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
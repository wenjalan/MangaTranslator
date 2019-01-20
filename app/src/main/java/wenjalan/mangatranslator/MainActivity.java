package wenjalan.mangatranslator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    // viewfinder size scale
    public static final float VF_2_SCREEN_X = 0.5f;
    public static final float VF_2_SCREEN_Y = 0.5f;

    // the tag
    public static final String TAG = "MT-Main";

    // the camera manager
    private CameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        this.cameraManager = new CameraManager(this).init();

        // create the preview
        CameraPreview cameraPreview = new CameraPreview(this, cameraManager.getCamera());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        cameraManager.setCameraPreview(cameraPreview);

        // set the capture button listener
        Button captureButton = findViewById(R.id.button_capture);
        cameraManager.setCaptureButton(captureButton);
    }

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
        cameraManager.init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraManager.release();
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
package wenjalan.mangatranslator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import wenjalan.mangatranslator.cloud.TranslationManager;
import wenjalan.mangatranslator.cloud.VisionManager;
import wenjalan.mangatranslator.hardware.CameraManager;
import wenjalan.mangatranslator.hardware.CameraPreview;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    // the reference to this Activity instance
    private static MainActivity MAIN;

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

    // whether or not we're currently translating something
    public static boolean isTranslating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MAIN = this;
        isTranslating = false;

        // initialize camera
        initCamera();

        // init capture button
        initCaptureButton();

        // init view finder
        initViewfinder();

        // init translation view
        initTranslationView();
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
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
    }

    // capture button
    private void initCaptureButton() {
        // set the capture button listener
        // FloatingActionButton captureButton = findViewById(R.id.button_capture);
        View viewFinder = findViewById(R.id.box);
        viewFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if we're not already translating something, take a picture
                if (!isTranslating) {
                    camera.takePicture(null, null, cameraManager.getPictureCallback());
                }
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

    // init translation textview
    private void initTranslationView() {
        // find it
        TextView translationView = findViewById(R.id.translation_text);

        // set the typeface
        Typeface wildwords = Typeface.createFromAsset(getAssets(), "fonts/wildwords.ttf");
        translationView.setTypeface(wildwords);

        // hide the parent view for now
//        ConstraintLayout constraintLayout = (ConstraintLayout) translationView.getParent();
//        constraintLayout.setVisibility(View.INVISIBLE);
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

    // returns the Activity
    public static MainActivity get() {
        return MAIN;
    }

    // sets the translated text field
    public void setTranslatedText(String text) {
        TextView textView = findViewById(R.id.translation_text);

        // set the visibility to visible if we haven't already
        ConstraintLayout constraintLayout = (ConstraintLayout) textView.getParent();
        if (constraintLayout.getVisibility() == View.INVISIBLE) {
            constraintLayout.setVisibility(View.VISIBLE);
        }

        textView.setText(text);
        textView.requestLayout();
    }

    // called by CameraManager when an image has been taken
    public static void onCameraManagerCapture(String filepath) {
        // set translating to true
        isTranslating = true;

        // send to vision
        VisionManager.findText(filepath);
        // log
        Log.d(TAG, "Translating...");
        // update text
        MainActivity.get().setTranslatedText("Finding text...");
    }

    // called by VisionManager when text detection is completed
    public static void onVisionManagerComplete(String text) {
        // send it to TranslationManager for translation
        TranslationManager.translate(text);
        // log
        Log.d(TAG, "Found text:\n" + text);// update text
        MainActivity.get().setTranslatedText("Translating...");
    }

    // called by TranslationManager when text translation is completed
    public static void onTranslationComplete(String translatedText) {
        Log.d(TAG, "Translation Complete:\n" + translatedText);

        // update the text window on screen
        MainActivity.get().setTranslatedText(translatedText);

        // set isTranslating
        isTranslating = false;
    }

    // called when an error happens
    public static void onError() {
        // set translating to false
        isTranslating = false;
    }

}
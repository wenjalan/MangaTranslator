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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {

    // viewfinder size scale
    public static final float VF_2_SCREEN_X = 0.5f;
    public static final float VF_2_SCREEN_Y = 0.5f;

    // the instance of Camera
    static Camera camera = null;
    private CameraPreview cameraPreview;

    // the tag
    public static final String TAG = "MangaTranslator-Main";

    // picture callback
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file!");
                return;
            }

            // get the name
            String filepath = pictureFile.getPath();

            // write the data
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found!");
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file!");
            }

            // write the cropped image
            Bitmap cropped = cropImage(filepath);

            // rotate it
            cropped = rotateBitmap(cropped, 90);

            // write it
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                cropped.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Wrote image to " + filepath);
            camera.startPreview();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize camera
        initCamera();

        // set the capture button listener
        Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

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
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initCamera() {
        // open the camera
        camera = getCameraInstance();

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

    public static Camera getCameraInstance() {
        Camera cam = null;
        try {
            cam = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to open camera!");
        }
        return cam;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private Bitmap cropImage(String filepath) {
        // log
        Log.d(TAG, "Cropping image...");

        Bitmap bitmap = BitmapFactory.decodeFile(filepath);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        // calculate corner
        // center of image - half of the size of the box
        int cropWidth = (int) (width * VF_2_SCREEN_X);
        int cropHeight = (int) (height * VF_2_SCREEN_Y);
        int x = (int) ((width / 2) - (cropWidth / 2));
        int y = (int) ((height / 2) - (cropHeight / 2));

        Bitmap cropped = Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight);

        return cropped;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix mat = new Matrix();
        mat.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
    }

    /* from tutorial */
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MangaTranslator");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
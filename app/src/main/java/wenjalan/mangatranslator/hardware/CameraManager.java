package wenjalan.mangatranslator.hardware;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import wenjalan.mangatranslator.MainActivity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static wenjalan.mangatranslator.MainActivity.VF_2_SCREEN_X;
import static wenjalan.mangatranslator.MainActivity.VF_2_SCREEN_Y;

@SuppressWarnings("deprecation")
public class CameraManager {

    // the amount to scale the image down by to allow for faster processing
    protected static final int RESIZE_SCALE = 4;

    public static final String TAG = "MT-CameraManager";
    public static final String IMAGE_NAME = "capture.jpg";

    private final Context context;

    private Camera camera = null;

    public String filepath = null;

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
            filepath = pictureFile.getPath();

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
            Bitmap bmp = cropImage(filepath);

            // rotate it
            bmp = rotateBitmap(bmp, 90);

            // resize it
            bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth() / RESIZE_SCALE, bmp.getHeight() / RESIZE_SCALE, false);

            // write it
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Log.d(TAG, "Wrote image to " + filepath);

            // resume camera
            camera.startPreview();

            // send back to Main
            MainActivity.onCameraManagerCapture(filepath);
        }
    };

    // constructor
    public CameraManager(Context context) {
        this.context = context;
        initCamera();
    }

    public void release() {
        releaseCamera();
    }

    private void initCamera() {
        Log.d(TAG, "Initializing camera...");
        // open the camera
        camera = getCameraInstance();

        // set the camera to portrait
        camera.setDisplayOrientation(90);

        // enable auto focus and black and white mode
        Camera.Parameters par = camera.getParameters();
        par.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        // par.setColorEffect(Camera.Parameters.EFFECT_MONO);
        camera.setParameters(par);
    }

    public void setCaptureButton(Button captureButton) {
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });
    }

    public Camera getCameraInstance() {
        Camera cam = null;
        try {
            cam = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "Failed to open camera!");
            e.printStackTrace();
            System.exit(1);
        }
        return cam;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
            Log.d(TAG, "Released camera!");
        }
    }

    private Bitmap cropImage(String filepath) {
        // log
        // Log.d(TAG, "Cropping image...");

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
//    /** Create a file Uri for saving an image or video */
//    private static Uri getOutputMediaFileUri(int type){
//        return Uri.fromFile(getOutputMediaFile(type));
//    }

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
//            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                    "IMG_"+ timeStamp + ".jpg");
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    IMAGE_NAME);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public Camera getCamera() {
        return camera;
    }

    public Camera.PictureCallback getPictureCallback() {
        return pictureCallback;
    }
}

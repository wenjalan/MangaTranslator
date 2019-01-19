package wenjalan.mangatranslator;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MangaTranslator";
    private CameraBridgeViewBase mOpenCvCameraView;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV initialized successfully");
        }
        else {
            Log.d(TAG, "OpenCV failed to initialize");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_4_1_0, this, mLoaderCallback);
        OpenCVLoader.initDebug();
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.MainCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // turn on the camera
        mOpenCvCameraView.enableView();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Log.d(TAG, "Got a frame");
        Mat mRgba = inputFrame.rgba();

        // convert to grayscale
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGB2GRAY, 0);

        int viewFinderHeight = 300;
        int viewFinderWidth = 500;

        int height = mRgba.height();
        int width = mRgba.width();

        int corner1 = width / 2 - (viewFinderWidth / 2);
        int corner2 = height / 2 - (viewFinderHeight / 2);

        Rect rect = new Rect(corner1, corner2, viewFinderWidth, viewFinderHeight);

        // white
        Scalar color = new Scalar(255, 255, 255, 100);

        // draw the rectangle
        Imgproc.rectangle(mRgba, rect, color, 5);

        // save this as last frame
        mRgba.copyTo(lastFrame);

        // return the mat
        return mRgba;
    }

    Mat lastFrame = new Mat();

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() { }

    // called by TranslateButton
    public void translate(View view) {
        // create an image of the last frame
//        Imgcodecs.imwrite("image.png", lastFrame);
//        Log.d(TAG, "Wrote an image");

        // find the imageview to display to
        final ImageView imageView = findViewById(R.id.CapturedImageView);

        // display the image
        // Mat imgMat = Imgcodecs.imread("image.png");
        // Mat mat = lastFrame;
        Mat mat = Mat.zeros(1440, 1920, CvType.CV_8UC3);
        Imgproc.putText(mat, "Hello world!", new Point(10, 10), Imgproc.FONT_HERSHEY_SCRIPT_SIMPLEX, 2, new Scalar(200, 200, 200, 0), 2);
        final Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(bmp, mat);

        Log.d(TAG, "Displaying image...");
        // run on main app thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bmp);
            }
        });
    }

}
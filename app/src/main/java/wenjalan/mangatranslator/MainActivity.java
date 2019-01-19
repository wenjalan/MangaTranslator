package wenjalan.mangatranslator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "StrikeZone";
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

    Mat mRgba;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "Got a frame");
        mRgba = inputFrame.rgba();

        int viewFinderHeight = 300;
        int viewFinderWidth = 500;

        int height = mRgba.height();
        int width = mRgba.width();

        int corner1 = width / 2 - (viewFinderWidth / 2);
        int corner2 = height / 2 - (viewFinderHeight / 2);

        Rect rect = new Rect(corner1, corner2, viewFinderWidth, viewFinderHeight);

        // white
        Scalar color = new Scalar(255, 255, 255, 100);

        Imgproc.rectangle(mRgba, rect, color, 5);

        // return the mat
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() { }

}
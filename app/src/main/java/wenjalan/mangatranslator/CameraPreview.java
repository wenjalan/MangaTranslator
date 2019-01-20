package wenjalan.mangatranslator;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "MT-CameraPreview";

    private SurfaceHolder holder;
    private Camera camera;

    public CameraPreview(Context context,Camera camera) {
        super(context);
        this.camera = camera;
        this.holder = getHolder();
        this.holder.addCallback(this);
        this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // set the camera preview to the holder
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Failed to start preview!");
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.holder.getSurface() == null) {
            return;
        }

        // stop the preview
        try {
            this.camera.stopPreview();
        } catch (Exception e) {

        }

        // start with new settings
        try {
            this.camera.setPreviewDisplay(this.holder);
            this.camera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error restarting camera preview!");
            e.printStackTrace();
        }
    }

}

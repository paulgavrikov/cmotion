package de.uni_freiburg.es.sensorrecordingtool.sensors;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.*;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.LinkedList;

/** Grabs frames at the specified videorate and returns them in raw format at maximum
 * resolution as sensorevents.
 *
 * Created by phil on 4/26/16.
 */
public class VideoSensor extends Sensor implements SurfaceHolder.Callback {
    protected static final String TAG = VideoSensor.class.getName();
    public static final String SENSOR_NAME = "video";
    protected final Context context;
    private Camera mCamera;
    private int mRateInMilliHz = 0;
    private SurfaceView mSurface;

    public VideoSensor(Context c) {
        super(c, 1);
        context = c;
        mEvent = new SensorEvent(0);
    }

    @Override
    public String getStringType() {
        return SENSOR_NAME;
    }

    @Override
    public void registerListener(SensorEventListener l, int rate_in_mus, int delay, String format) {
        //if (!PermissionDialog.camera(context))
        //    return;

        if (mListeners.size() == 0) {
            mCamera = Camera.open();
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size mSize = getCameraSize(format);
            params.setPreviewSize(mSize.width, mSize.height);
            mCamera.setParameters(params);

            int bytesPerBuffer = (int) Math.ceil(
                ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8.
                * mSize.width * mSize.height);

            mCamera.addCallbackBuffer(new byte[bytesPerBuffer]);
            mCamera.addCallbackBuffer(new byte[bytesPerBuffer]);

            mRateInMilliHz = (int) (1000 * 1000 / rate_in_mus) * 1000;
            startRecording();
        }

        super.registerListener(l,rate_in_mus,delay, format);
    }

    public static Camera.Size getCameraSize(String format) {
        Camera cam = Camera.open();
        Camera.Parameters params = cam.getParameters();
        Camera.Size mSize = params.getPreviewSize();

        try {
            String[] wh = format.split("x");
            params.setPreviewSize(
                    Integer.parseInt(wh[0]),
                    Integer.parseInt(wh[1]));
        } catch(Exception e) {
            Log.d(TAG, String.format(
                    "unable to parse format '%s', using default resolution %dx%d",
                    (format!=null?format:""), mSize.width, mSize.height));
        }

        return mSize;
    }

    /** can't do a recording without a preview surface, which is why a system overlay is created
     * here that it can seen from anywhere.
     */
    public void startRecording() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mSurface = new SurfaceView(mContext);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
            200, 100, 0, 300,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.LEFT  | Gravity.TOP;
        wm.addView(mSurface, lp);
        mSurface.getHolder().addCallback(this);
    }

    public void stopRecording() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    protected Camera.PreviewCallback preview = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            mEvent.timestamp = System.currentTimeMillis() * 1000 * 1000;
            mEvent.rawdata = bytes;
            notifyListeners();

            /** add the buffer again to the queue */
            mCamera.addCallbackBuffer(bytes);
        }

        // Default format is YCbCr'NV21
    };

    @Override
    public void unregisterListener(SensorEventListener l) {
        super.unregisterListener(l);

        if (mListeners.size() == 0) {
            try {
                WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
                wm.removeView(mSurface);
            } catch(Exception e) {}
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Camera.Parameters p = null;
        try {
            if (mCamera==null)
                mCamera = Camera.open();
            p = mCamera.getParameters();
            p.setPreviewFpsRange(mRateInMilliHz, mRateInMilliHz);
            mCamera.setParameters(p);

            Log.d(TAG, "starting recording with pixel format " + p.getPictureFormat());
            Log.d(TAG, "resolution " + p.getPreviewSize().width + "x" + p.getPreviewSize().height);

            mCamera.setPreviewDisplay(mSurface.getHolder());
            mCamera.setPreviewCallbackWithBuffer(preview);
            mCamera.startPreview();

        } catch(Exception e) {
            stopRecording();

            if (mCamera != null) {
                Log.d(TAG, e.toString());
                Log.d(TAG, "available rates are:");

                if (p != null)
                    for (int r[] : p.getSupportedPreviewFpsRange())
                        Log.d(TAG, Integer.toString(r[0]) + " " + Integer.toString(r[1]) + " milli-Hz");
            }

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurface.getHolder().removeCallback(this);
        stopRecording();
    }
}

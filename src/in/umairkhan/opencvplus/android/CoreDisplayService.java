package in.umairkhan.opencvplus.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by omerjerk on 15/7/14.
 */
public abstract class CoreDisplayService extends Service implements DisplayFrameListener {

    public static final int MODE_DISPLAY_SCREEN = 1;
    public static final int MODE_VIDEO = 2;

    private static final String TAG = "AndroidDisplayView";
    private static final boolean DEBUG = true;

    private int mode;

    CoreWorker coreWorker;

    private DetectionBasedTracker mNativeDetector;

    public static int FACES_COUNT = 0;

    public abstract int setClassifier();
    public abstract int setMode();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(setClassifier());
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        File mCascadeFile = new File(cascadeDir, "cascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
                        mNativeDetector.start();
                        cascadeDir.delete();

                        coreWorker.startRendering(mode, setVideoSource());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public Uri setVideoSource() {return null;}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mode = setMode();
        coreWorker = new CoreWorker(this, this);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void rawFrame(final byte[] frame) {
    }

    @Override
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        MatOfRect faces = new MatOfRect();
        mNativeDetector.detect(frame.gray(), faces);
        Rect[] facesArray = faces.toArray();
        Log.d("omerjerk", "Number of faces = " + facesArray.length);
        FACES_COUNT = facesArray.length;
        Intent intent = new Intent("UPDATE");
        sendBroadcast(intent);
    }

    @Override
    public void onDisplayFrameStarted() {
    }

    @Override
    public void onDisplayFrameStopped() {
    }

    @Override
    public void decoderOutputFormatChanged(MediaFormat format) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ResultDisplayService.class));
    }
}

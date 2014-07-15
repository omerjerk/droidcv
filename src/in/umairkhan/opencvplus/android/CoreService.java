package in.umairkhan.opencvplus.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by omerjerk on 15/7/14.
 */
public class CoreService extends Service implements DisplayFrameListener{

    private static final String TAG = "AndroidDisplayView";
    private static final boolean DEBUG = true;

    CoreWorker coreWorker;

    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    public static int FACES_COUNT = 0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
                        mNativeDetector.start();
                        cascadeDir.delete();

                        coreWorker.startRendering();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        coreWorker = new CoreWorker(this, this);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void rawFrame(final byte[] frame) {

        /* It's better to do detection on background thread so that we won't interfere the codec thread. */
        //new AsyncTask<Void, Void, Void>() {
         //   @Override
         //   protected Void doInBackground(Void... voids) {
                Mat mat = new Mat(1280, 1024, CvType.CV_8UC1);
                mat.put(0, 0, frame);
                MatOfRect faces = new MatOfRect();
                mNativeDetector.detect(mat, faces);
                Rect[] facesArray = faces.toArray();
                Log.d("omerjerk", "Number of faces = " + facesArray.length);
                FACES_COUNT = facesArray.length;
                Intent intent = new Intent("UPDATE");
                sendBroadcast(intent);
                //return null;
           // }
        //}.execute();
    }

    @Override
    public void onNewFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {

    }

    @Override
    public void onDisplayFrameStarted() {

    }

    @Override
    public void onDisplayFrameStopped() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, ResultDisplayService.class));
    }
}

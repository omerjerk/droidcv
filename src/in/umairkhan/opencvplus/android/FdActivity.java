package in.umairkhan.opencvplus.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.*;
import android.view.View;
import android.widget.ImageView;
import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class FdActivity extends Activity implements DisplayFrameListener {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    //private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private int                    mDetectorType       = NATIVE_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private static final boolean DEBUG = true;
    public static final boolean DEBUG_IMAGE_OUT = false;

    ImageView test;
    int k = 0;
    private AndroidDisplayView openCvDisplayView;
    DisplayFrame derp = null;
    byte[] rawFrame;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
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

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    openCvDisplayView.startRendering();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        openCvDisplayView = (AndroidDisplayView) findViewById(R.id.display_view);
        openCvDisplayView.setDisplayFrameListener(this);
        test = (ImageView) findViewById(R.id.image_view);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //if (mOpenCvCameraView != null)
            //mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        //mOpenCvCameraView.disableView();
    }

    @Override
    public void onDisplayFrameStarted() {
        //mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onDisplayFrameStopped() {
        //mGray.release();
        mRgba.release();
    }

    @Override
    public void rawFrame(final byte[] frame) {

        if (!DEBUG) return;
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        k++;
                        if (k % 10 != 0) return;
                        //test.setImageBitmap(null);
                        //int[] rgb = new int[1024*1280];
                        //in.umairkhan.opencvplus.android.Utils.decodeYUV420SP(frame, 1024, 1280, rgb);
                        Mat mat = new Mat(1280, 1024, CvType.CV_8UC1);
                        mat.put(0, 0, frame);
                        Log.d("opencv", "type = " + mat.type() + " channels = " + mat.channels());
                        //Log.d("opencv", "MAT = " + mat.dump());
                        MatOfRect faces = new MatOfRect();
                        mNativeDetector.detect(mat, faces);
                        Log.d("omerjerk", "Number of faces = " + faces.toArray().length);
                        /*
                        for (int i = 0; i < mat.rows(); ++i) {
                            for (int j = 0; j < mat.cols(); ++j) {
                                System.out.print(mat.);
                            }
                        }*/
                        //Mat orig = new Mat(1280, 1024, CvType.CV_8UC3);
                        //Imgproc.cvtColor(mat, orig, Imgproc.COLOR_YUV420sp2RGBA);
                        Bitmap bm = Bitmap.createBitmap(1024, 1280, Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mat, bm);
                        //test.setImageBitmap(bm);

                        if (k % 25 == 0) {
                            String filename = "/sdcard/derp2.jpg";
                            FileOutputStream out = null;

                            try {
                                out = new FileOutputStream(filename);
                                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try{
                                    out.close();
                                } catch(Throwable ignore) {}
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onNewFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
       // mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            //int height = mGray.rows();
            //int width = mGray.cols();
            //Log.d("omerjerk", "width = " + width + " height = " + height);
            if (true) {
              //  mAbsoluteFaceSize = Math.round(height * 0.6f);
            }
            //mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null){}
               // mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                 //       new Size(0, 0), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null) {
                //mNativeDetector.detect(mRgba, faces);
                //Log.d("omerjerk", "cols = " + mRgba.cols());
            } else {
                Log.d(TAG, "Native Detector is NULL");
            }

        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        /*Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}

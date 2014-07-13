package in.umairkhan.opencvplus.android;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import org.xml.sax.Attributes;
import safesax.Element;
import safesax.ElementListener;
import safesax.Parsers;
import safesax.RootElement;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by omerjerk on 12/7/14.
 */
public class AndroidDisplayView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "AndroidDisplayView";
    private static final boolean DEBUG = true;

    private Context mContext = null;

    MediaCodec encoder =  null;
    MediaCodec decoder = null;

    DisplayFrameListener mListener = null;

    SurfaceHolder mHolder = null;

    public AndroidDisplayView(Context context) {
        super(context);
        this.mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public AndroidDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public AndroidDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void setDisplayFrameListener(DisplayFrameListener l) {
        mListener = l;
    }

    public void startRendering() {
        DisplayManager mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        if (DEBUG) Log.d(TAG, "Width = " + getWidth());
        if (DEBUG) Log.d(TAG, "Height = " + getHeight());
        Surface outputSurface = getHolder().getSurface();
        Surface encoderInputSurface = createDisplaySurface(outputSurface);
        mDisplayManager.createVirtualDisplay("OpenCV Virtual Display", 960, 1280, 150, encoderInputSurface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);

        Thread encoderThread = new Thread(new EncoderWorker());
        encoderThread.start();
    }

    private Surface createDisplaySurface(Surface outputSurface) {
        int bitrate;
        int maxFrameRate;
        int height = 1280;
        int width = 960;
        if (encoder != null) {
            try {
                encoder.signalEndOfInputStream();
            } catch (Exception e) {
            }
            encoder = null;
        }
        try {
            String xml = StreamUtility.readFile("/system/etc/media_profiles.xml");
            RootElement root = new RootElement("MediaSettings");
            Element encoderElement = root.requireChild("VideoEncoderCap");
            ArrayList<VideoEncoderCap> encoders = new ArrayList();
            XmlListener mXmlListener = new XmlListener(encoders);
            encoderElement.setElementListener(mXmlListener);
            Reader mReader = new StringReader(xml);
            Parsers.parse(mReader, root.getContentHandler());
            if (encoders.size() != 1) {
                throw new Exception("derp");
            } else {
                VideoEncoderCap v = encoders.get(0);
                int maxWidth = v.maxFrameWidth;
                int maxHeight = v.maxFrameHeight;
                bitrate = v.maxBitRate;
                maxFrameRate = v.maxFrameRate;
                MediaFormat mMediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
                mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
                mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
                mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
                Log.i(TAG, "Starting encoder");
                encoder = MediaCodec.createByCodecName(Utils.selectCodec("video/avc").getName());
                encoder.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                Surface surface = encoder.createInputSurface();

                encoder.start();
                return surface;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
      /*  DisplayManager mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.createVirtualDisplay("OpenCV Render Display", 960, 1280, 150, surfaceHolder.getSurface(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE); */
        //startRendering();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private class EncoderWorker implements Runnable {

        @Override
        public void run() {
            /*
            ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
            DisplayFrame displayFrame = new DisplayFrame(960, 1280);
            mListener.onDisplayFrameStarted();
            while(true) {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int bufIndex = encoder.dequeueOutputBuffer(info, TimeUnit.SECONDS.toMicros(1) / 30);

                if (bufIndex >= 0) {
                    ByteBuffer frameBuffer = outputBuffers[bufIndex];
                    //TODO: we've the buffer. Pass them to OpenCv.
                    byte[] b = new byte[frameBuffer.remaining()];
                    Log.d("omerjerk", "size = " + b.length);
                    frameBuffer.get(b);
                    displayFrame.updateFrame(b);
                    if (mListener != null) {
                        mListener.onNewFrame(displayFrame);
                    }
                    frameBuffer.clear();
                    encoder.releaseOutputBuffer(bufIndex, false);
                } else if (bufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = encoder.getOutputBuffers();
                } else if (bufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    //TODO: stop
                    //mListener.onDisplayFrameStopped();
                    //break;
                }
            } */
            DisplayFrame displayFrame = new DisplayFrame(960, 1280);
            decoder = MediaCodec.createDecoderByType(Utils.MIME_TYPE);
            Utils.doEncodeDecodeVideoFromSurface(encoder, decoder, mListener, displayFrame);
        }
    }

    class XmlListener implements ElementListener {
        final ArrayList<VideoEncoderCap> encoders;

        XmlListener(ArrayList mList) {
            this.encoders = mList;
        }

        @Override
        public void end() {
        }

        @Override
        public void start(Attributes attributes) {
            if (TextUtils.equals(attributes.getValue("name"), "h264")) {
                this.encoders.add(new VideoEncoderCap(attributes));
            }
        }
    }

    private static class VideoEncoderCap {
        int maxBitRate;
        int maxFrameHeight;
        int maxFrameRate;
        int maxFrameWidth;

        public VideoEncoderCap(Attributes attributes) {
            this.maxFrameWidth = Integer.valueOf(attributes.getValue("maxFrameWidth")).intValue();
            this.maxFrameHeight = Integer.valueOf(attributes.getValue("maxFrameHeight")).intValue();
            this.maxBitRate = Integer.valueOf(attributes.getValue("maxBitRate")).intValue();
            this.maxFrameRate = Integer.valueOf(attributes.getValue("maxFrameRate")).intValue();
        }
    }
}

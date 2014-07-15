package in.umairkhan.opencvplus.android;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
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
public class CoreWorker {

    private static final String TAG = "AndroidDisplayView";
    private static final boolean DEBUG = true;

    private Context mContext = null;

    MediaCodec encoder =  null;
    MediaCodec decoder = null;

    DisplayFrameListener mListener = null;

    public CoreWorker(Context context, DisplayFrameListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void startRendering() {
        DisplayManager mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        Surface encoderInputSurface = createDisplaySurface();
        mDisplayManager.createVirtualDisplay("OpenCV Virtual Display", 960, 1280, 150, encoderInputSurface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);

        Thread encoderThread = new Thread(new CodecWorker());
        encoderThread.start();
    }

    private Surface createDisplaySurface() {
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
                encoder = MediaCodec.createByCodecName(CodecUtils.selectCodec(CodecUtils.MIME_TYPE).getName());
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

    private class CodecWorker implements Runnable {

        @Override
        public void run() {
            DisplayFrame displayFrame = new DisplayFrame(1024, 1280);
            decoder = MediaCodec.createDecoderByType(CodecUtils.MIME_TYPE);
            mListener.onDisplayFrameStarted();
            CodecUtils.doEncodeDecodeVideoFromSurface(encoder, decoder, mListener, displayFrame);
            mListener.onDisplayFrameStopped();
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

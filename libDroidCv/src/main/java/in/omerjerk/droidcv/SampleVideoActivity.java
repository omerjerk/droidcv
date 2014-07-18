package in.omerjerk.droidcv;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by omerjerk on 17/7/14.
 */
public class SampleVideoActivity extends Activity implements SurfaceHolder.Callback{

    public static String src_file = "/sdcard/derp.mp4";

    //MediaPlayer mediaPlayer;
    //SurfaceView surfaceView;
    //static SurfaceHolder surfaceHolder = null;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video_activity);
        //surfaceView = (SurfaceView) findViewById(R.id.video_surface_view);
        //surfaceHolder = surfaceView.getHolder();
        //surfaceHolder.addCallback(this);
        //mediaPlayer = new MediaPlayer();
        //mediaPlayer.setLooping(true);
        stopService(new Intent(this, SampleService.class));
        stopService(new Intent(this, SampleVideoService.class));
        startService(new Intent(this, SampleVideoService.class));
        startService(new Intent(this, ResultDisplayService.class));
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        //mediaPlayer.setDisplay(surfaceHolder);
        try {
            //mediaPlayer.setDataSource(this, Uri.parse(src_file));
            //mediaPlayer.prepare();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //mediaPlayer.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void onPause() {
        super.onPause();
        //mediaPlayer.stop();
    }
}

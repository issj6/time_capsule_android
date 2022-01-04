package com.example.timecapsule_2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BackGroundMusic extends Service {


    public BackGroundMusic() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (BgmData.getMediaPlayer() == null) {
            // R.raw.mmp是资源文件，MP3格式的，res下新建raw，hb.mp3放到里面
            BgmData.setMediaPlayer(MediaPlayer.create(this, R.raw.bgm));
            BgmData.getMediaPlayer().setLooping(true);
            BgmData.getMediaPlayer().start();
        }else{
            BgmData.getMediaPlayer().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BgmData.getMediaPlayer().pause();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
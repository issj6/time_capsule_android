package com.example.timecapsule_2;

import android.content.Intent;
import android.media.MediaPlayer;

public class BgmData {
    private static Intent bgmS;
    private static boolean bgmState;
    private static MediaPlayer mediaPlayer;

    public static Intent getBgmS() {
        return bgmS;
    }

    public static void setBgmS(Intent bgmS) {
        BgmData.bgmS = bgmS;
    }

    public static boolean isBgmState() {
        return bgmState;
    }

    public static void setBgmState(boolean bgmState) {
        BgmData.bgmState = bgmState;
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static void setMediaPlayer(MediaPlayer mediaPlayer) {
        BgmData.mediaPlayer = mediaPlayer;
    }
}

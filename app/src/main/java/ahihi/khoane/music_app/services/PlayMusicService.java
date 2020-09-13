package ahihi.khoane.music_app.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import ahihi.khoane.music_app.ui.main.MainActivity;

public class PlayMusicService extends Service {
    private static final String TAG = "nnn";
    private MediaPlayer mediaPlayer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initMediaPlayer();
        play();
        return super.onStartCommand(intent, flags, startId);
    }

    private void play() {
        Uri uri = Uri.parse(MainActivity.arrayList.get(3).getUrl());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mediaPlayer.setVolume(1.0f, 1.0f);
    }

}

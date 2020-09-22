package ahihi.khoane.music_app.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ahihi.khoane.music_app.App;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.broadcast.NotificationActionService;
import ahihi.khoane.music_app.interfacce.ServiceCallbacks;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.model.CallBackModel;
import ahihi.khoane.music_app.ui.detail.PlayActivity;
import ahihi.khoane.music_app.ui.main.MainActivity;
import ahihi.khoane.music_app.utils.CreateNotification;
import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.utils.Playable;

import static ahihi.khoane.music_app.App.mBroadcaster;

public class PlayMusicService extends Service implements Playable {


    private static final String TAG = "nnn";
    public static MediaPlayer mediaPlayer;
    public static boolean isPlaying = true;
    public static int position;
//    private List<AudioModel> arrayList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        position = intent.getIntExtra("postion", 0);
        initMediaPlayer();
        play(position);

        return START_NOT_STICKY;
    }

    private void play(int po) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
            Uri uri = Uri.parse(MainActivity.arrayList.get(po).getUrl());//"content://media/external/audio/media/25"
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (position >= (MainActivity.arrayList.size() - 1)) {
                        mediaPlayer.stop();
                    } else {
                        onMusicNext();
                    }
                }
            });
            CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                    R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size() - 1);
            startForeground(1, CreateNotification.notification);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            Log.d(TAG, "onReceive: " + action);
            switch (action) {
                case "ACTION_PREVIUOS":
                    onMusicPrevious();
                    break;
                case "ACTION_PLAY":
                    if (isPlaying) {
                        onMusicPause();
                    } else {
                        onMusicPlay();
                    }
                    break;
                case "ACTION_NEXT":
                    onMusicNext();
                    break;
                case "ACTION_CLOSE":
                    stopSelf();
                    mediaPlayer.stop();
                    break;
            }
        }
    };

    @Override
    public void onMusicPrevious() {
        position--;
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size() - 1);
        startForeground(1, CreateNotification.notification);
        play(position);
        EventBus.getDefault().post(new AudioModel(MainActivity.arrayList.get(position).getTitle(),
                MainActivity.arrayList.get(position).getDuration(),
                MainActivity.arrayList.get(position).getUrl(), MainActivity.arrayList.get(position).getIdAlbum()));
    }

    @Override
    public void onMusicPlay() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size() - 1);
        startForeground(1, CreateNotification.notification);
        isPlaying = true;
        mediaPlayer.start();
        EventBus.getDefault().post(new AudioModel(MainActivity.arrayList.get(position).getTitle(),
                MainActivity.arrayList.get(position).getDuration(),
                MainActivity.arrayList.get(position).getUrl(), MainActivity.arrayList.get(position).getIdAlbum()));
    }

    @Override
    public void onMusicPause() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_play_arrow_24, position, MainActivity.arrayList.size() - 1);
        startForeground(1, CreateNotification.notification);
        isPlaying = false;
        mediaPlayer.pause();
        EventBus.getDefault().post(new AudioModel(MainActivity.arrayList.get(position).getTitle(),
                MainActivity.arrayList.get(position).getDuration(),
                MainActivity.arrayList.get(position).getUrl(), MainActivity.arrayList.get(position).getIdAlbum()));
    }

    @Override
    public void onMusicNext() {
        position++;
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size() - 1);
        startForeground(1, CreateNotification.notification);
        play(position);
        EventBus.getDefault().post(new AudioModel(MainActivity.arrayList.get(position).getTitle(),
                MainActivity.arrayList.get(position).getDuration(),
                MainActivity.arrayList.get(position).getUrl(), MainActivity.arrayList.get(position).getIdAlbum()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void call(CallBackModel callBackModel) {
        if (callBackModel.getA().equals("0")) {
            onMusicPrevious();
        } else if (callBackModel.getA().equals("1")) {
            if (isPlaying) {
                onMusicPause();
            } else {
                onMusicPlay();
            }
        } else {
            onMusicNext();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

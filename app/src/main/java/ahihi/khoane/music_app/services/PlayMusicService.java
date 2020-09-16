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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ahihi.khoane.music_app.App;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.broadcast.NotificationActionService;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.ui.detail.PlayActivity;
import ahihi.khoane.music_app.ui.main.MainActivity;
import ahihi.khoane.music_app.utils.CreateNotification;
import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.utils.Playable;

public class PlayMusicService extends Service implements Playable {
    private static final String TAG = "nnn";
    public static MediaPlayer mediaPlayer;
    private boolean isPlaying = true;
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
        position = intent.getIntExtra("postion",0);
        initMediaPlayer();
        play(position);

        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size()-1);
        startForeground(1, CreateNotification.notification);

//        Bitmap icon = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(MainActivity.arrayList.get(postion).getIdAlbum()), this));
//
//        PendingIntent pendingIntentPrevious;
//            int drw_previous;
//            if (postion == 0){
//                pendingIntentPrevious = null;
//                drw_previous = 0;
//            } else {
//                Intent intentPrevious = new Intent(this, NotificationActionService.class)
//                        .setAction("ACTION_PREVIUOS");
//                pendingIntentPrevious = PendingIntent.getBroadcast(this, 0,
//                        intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
//                drw_previous = R.drawable.ic_back;
//            }
//
//            Intent intentPlay = new Intent(this, NotificationActionService.class)
//                    .setAction("ACTION_PLAY");
//            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0,
//                    intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            PendingIntent pendingIntentNext;
//            int drw_next;
//            if (postion == MainActivity.arrayList.size()){
//                pendingIntentNext = null;
//                drw_next = 0;
//            } else {
//                Intent intentNext = new Intent(this, NotificationActionService.class)
//                        .setAction("ACTION_NEXT");
//                pendingIntentNext = PendingIntent.getBroadcast(this, 0,
//                        intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
//                drw_next = R.drawable.ic_skip;
//            }
//
//        Intent notificationIntent = new Intent(this, PlayActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
//
//        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
//                .setContentTitle(MainActivity.arrayList.get(postion).getTitle())
//                .setContentText(MainActivity.arrayList.get(postion).getDuration())
//                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
//                .setLargeIcon(icon)
//                .addAction(drw_previous,"", pendingIntentPrevious)
//                .addAction(R.drawable.ic_play,"", pendingIntentPlay)
//                .addAction(drw_next,"", pendingIntentNext)
//                .setContentIntent(pendingIntent)
//                .build();
//        startForeground(1, notification);

//        startForeground(1, CreateNotification.notification);
        return START_NOT_STICKY;
    }

    private void play(int po) {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        Uri uri = Uri.parse(MainActivity.arrayList.get(po).getUrl());//"content://media/external/audio/media/25"
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
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
            Log.d(TAG, "onReceive: "+action);
            switch (action){
                case "ACTION_PREVIUOS":
                    onMusicPrevious();
                    break;
                case "ACTION_PLAY":
                    if (isPlaying){
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
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size()-1);
        startForeground(1, CreateNotification.notification);
        play(position);
    }

    @Override
    public void onMusicPlay() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size()-1);
        startForeground(1, CreateNotification.notification);
        isPlaying = true;
        mediaPlayer.start();
    }

    @Override
    public void onMusicPause() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_play_arrow_24, position, MainActivity.arrayList.size()-1);
        startForeground(1, CreateNotification.notification);
        isPlaying = false;
        mediaPlayer.pause();
    }

    @Override
    public void onMusicNext() {
        position++;
        CreateNotification.createNotification(this, MainActivity.arrayList.get(position),
                R.drawable.ic_baseline_pause_24, position, MainActivity.arrayList.size()-1);
        startForeground(1, CreateNotification.notification);
        play(position);
    }
}

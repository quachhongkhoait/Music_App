package ahihi.khoane.music_app.services;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.io.IOException;
import java.util.List;

import ahihi.khoane.music_app.utils.MediaStyleHelper;
import ahihi.khoane.music_app.R;

public class MusicService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSessionCompat;

    private BroadcastReceiver headPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
                mediaPlayer.pause();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mediaSessionCompat.getSessionToken());
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headPhoneReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(headPhoneReceiver);
        mediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(1);
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if( !successfullyRetrievedAudioFocus() ) {
                return;
            }

            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

            showPlayingNotification();
            mediaPlayer.start();
        }

        @Override
        public void onPause() {
            super.onPause();

            if( mediaPlayer.isPlaying() ) {
                mediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                showPausedNotification();
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(Integer.valueOf(mediaId));
                if (afd == null) {
                    return;
                }

                try {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                } catch (IllegalStateException e) {
                    mediaPlayer.release();
                    initMediaPlayer();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                afd.close();
                initMediaSessionMetadata();

            } catch (IOException e) {
                return;
            }

            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void showPlayingNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(MusicService.this, mediaSessionCompat);
        if( builder == null ) {
            return;
        }


        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(MusicService.this).notify(1, builder.build());
    }

    private void showPausedNotification() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(this, mediaSessionCompat);
        if( builder == null ) {
            return;
        }

        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSessionCompat.getSessionToken()));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat.from(this).notify(1, builder.build());
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Beo Dat May Troi");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Singer: Anh Tho");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mediaSessionCompat.setMetadata(metadataBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mediaPlayer.isPlaying() ) {
                    mediaPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mediaPlayer != null ) {
                    mediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mediaPlayer != null ) {
                    if( !mediaPlayer.isPlaying() ) {
                        mediaPlayer.start();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if( this.mediaPlayer != null ) {
            this.mediaPlayer.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }
}






//package ahihi.khoane.music_app;
//
//        import android.app.Service;
//        import android.content.ContentUris;
//        import android.content.Intent;
//        import android.media.AudioManager;
//        import android.media.MediaPlayer;
//        import android.net.Uri;
//        import android.os.Binder;
//        import android.os.IBinder;
//        import android.os.PowerManager;
//        import android.util.Log;
//
//        import androidx.annotation.Nullable;
//
//        import java.util.ArrayList;
//
//public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
//
//    private final IBinder musicBind = new MusicBinder();
//    //media player
//    private MediaPlayer player;
//    //song list
//    private ArrayList<AudioModel> songs;
//    //current position
//    private int songPosn;
//
//    public void setList(ArrayList<AudioModel> theSongs) {
//        songs = theSongs;
//    }
//
//    public void onCreate() {
//        super.onCreate();
//        songPosn = 0;
//        player = new MediaPlayer();
//        initMusicPlayer();
//    }
//
//    public void initMusicPlayer() {
//        //set player properties
//        player.setWakeMode(getApplicationContext(),
//                PowerManager.PARTIAL_WAKE_LOCK);
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        player.setOnPreparedListener(this);
//        player.setOnCompletionListener(this);
//        player.setOnErrorListener(this);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return musicBind;
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        player.stop();
//        player.release();
//        return false;
//    }
//
//    @Override
//    public void onCompletion(MediaPlayer mediaPlayer) {
//
//    }
//
//    @Override
//    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//        return false;
//    }
//
//    @Override
//    public void onPrepared(MediaPlayer mediaPlayer) {
//        mediaPlayer.start();
//    }
//
//    public class MusicBinder extends Binder {
//        MusicService getService() {
//            return MusicService.this;
//        }
//    }
//
//    public void playSong(){
//        player.reset();
//        AudioModel playSong = songs.get(songPosn);
//        Uri trackUri = Uri.parse(playSong.getUrl());
//        try{
//            player.setDataSource(getApplicationContext(), trackUri);
//        }
//        catch(Exception e){
//            Log.e("MUSIC SERVICE", "Error setting data source", e);
//        }
//        player.prepareAsync();
//    }
//
//}


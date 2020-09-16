package ahihi.khoane.music_app.ui.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;

import ahihi.khoane.music_app.ui.main.MainActivity;
import ahihi.khoane.music_app.utils.CreateNotification;
import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.services.MusicService;
import ahihi.khoane.music_app.utils.Playable;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayActivity extends AppCompatActivity implements Playable{

    private static final String TAG = "zzz";
    private Intent playIntent;

    RotateAnimation rotate;
    private MediaPlayer mMediaPlayer;
    CircleImageView mImgAlbum;
    TextView mTvTitle, mTvCurrentTime, mTvTotalTime;
    SeekBar mSeekBar;
    Handler mHandler = new Handler();
    TextView mBtnPlay, mBtnPrevious, mBtnNext;
    private boolean isPlaying = true;
    AudioModel audioModel;

//    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        init();
        onClick();
        addData();
//        Intent intent = new Intent("test.BroadcastReceiver");
//        sendBroadcast(intent);
    }

//    private void service() {
//        musicConnection = new ServiceConnection() {
//
//            @Override
//            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//                MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
//                //get service
//                musicSrv = binder.getService();
//                //pass list
//                musicSrv.setList(MainActivity.arrayList);
//                musicBound = true;
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                musicBound = false;
//            }
//        };
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
//            playIntent = new Intent(this, MusicService.class);
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
        }
        Log.d(TAG, "onStart: ");
    }

    private void onClick() {
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMusicPlay();
            }
        });
        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMusicPrevious();
//                Log.d(TAG, "onClick: "+MainActivity.arrayList.size());
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMusicNext();
            }
        });
    }

    private void addData() {
        mMediaPlayer = PlayMusicService.mediaPlayer;
        mSeekBar.setProgress(PlayMusicService.mediaPlayer.getCurrentPosition() / 1000 % 60);
        mSeekBar.setMax(PlayMusicService.mediaPlayer.getDuration());
        mTvTotalTime.setText(HandlingMusic.createTimerLabel(PlayMusicService.mediaPlayer.getDuration()));
//        audioModel = getIntent().getExtras().getParcelable("obj");
//        Uri uri = Uri.parse(audioModel.getUrl());
//        Bitmap bitmap = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(audioModel.getIdAlbum()), this));
//        if (bitmap == null) {
//            mImgAlbum.setImageResource(R.drawable.bg_musicerror);
//        } else {
//            mImgAlbum.setImageBitmap(bitmap);
//        }
//        mTvTitle.setText(audioModel.getTitle());
//        mMediaPlayer = MediaPlayer.create(this, uri);
//        mMediaPlayer.start();

//        rotate = new RotateAnimation(0, 10000, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(mMediaPlayer.getDuration());
//        rotate.setInterpolator(new LinearInterpolator());
//        mImgAlbum.startAnimation(rotate);

        //xử lý
        PlayMusicService.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                Log.d(TAG, "onPrepared: ");
//                mSeekBar.setProgress(PlayMusicService.mediaPlayer.getCurrentPosition() / 1000 % 60);
//                mSeekBar.setMax(PlayMusicService.mediaPlayer.getDuration());
//                mTvTotalTime.setText(HandlingMusic.createTimerLabel(PlayMusicService.mediaPlayer.getDuration()));
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    PlayMusicService.mediaPlayer.seekTo(i);
                    mSeekBar.setProgress(i);
                    mTvCurrentTime.setText(HandlingMusic.createTimerLabel(PlayMusicService.mediaPlayer.getCurrentPosition()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        final Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                int min, sec;
                if (PlayMusicService.mediaPlayer != null) {
                    mSeekBar.setProgress(PlayMusicService.mediaPlayer.getCurrentPosition());
                    int mCurrentPosition = mSeekBar.getProgress();
                    mTvCurrentTime.setText(HandlingMusic.createTimerLabel(mCurrentPosition));
                    if (mSeekBar.getProgress() == mSeekBar.getMax()) {
//                        rotate.cancel();
//                        mImgAlbum.startAnimation(rotate);
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        mRunnable.run();
    }

    private void init() {
        mImgAlbum = findViewById(R.id.mImgAlbum);
        mTvTitle = findViewById(R.id.mTvTitle);
        mSeekBar = findViewById(R.id.musicSeekBar);
        mTvCurrentTime = findViewById(R.id.currentTime);
        mTvTotalTime = findViewById(R.id.totalTime);
        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnPrevious = findViewById(R.id.btnPrevious);
        mBtnNext = findViewById(R.id.btnNext);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                //shuffle
                break;
            case R.id.btnPlay:
                stopService(playIntent);
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
//        stopService(playIntent);
//        musicSrv=null;
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.d(TAG, "onCreate: 1");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    public void onMusicPrevious() {
//        PlayMusicService.position--;
////        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
////                R.drawable.ic_baseline_pause_24, PlayMusicService.position, MainActivity.arrayList.size()-1);
////        play(PlayMusicService.position);
    }

    @Override
    public void onMusicPlay() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
                R.drawable.ic_baseline_pause_24, PlayMusicService.position, MainActivity.arrayList.size()-1);
        PlayMusicService.mediaPlayer.start();
        mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        isPlaying = true;
    }

    @Override
    public void onMusicPause() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
                R.drawable.ic_baseline_play_arrow_24, PlayMusicService.position, MainActivity.arrayList.size()-1);
        PlayMusicService.mediaPlayer.pause();
        mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        isPlaying = false;
    }

    @Override
    public void onMusicNext() {
        PlayMusicService.position++;
        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
                R.drawable.ic_baseline_pause_24, PlayMusicService.position, MainActivity.arrayList.size()-1);
        play(PlayMusicService.position);
    }

    private void play(int po) {
        if (PlayMusicService.mediaPlayer.isPlaying()){
            PlayMusicService.mediaPlayer.stop();
        }
        Uri uri = Uri.parse(MainActivity.arrayList.get(po).getUrl());//"content://media/external/audio/media/25"
        PlayMusicService.mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        PlayMusicService.mediaPlayer.start();
    }

}

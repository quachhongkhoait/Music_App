package ahihi.khoane.music_app.ui.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.services.MusicService;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "zzz";
    private ServiceConnection musicConnection;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;

    RotateAnimation rotate;
    private MediaPlayer mMediaPlayer;
    CircleImageView mImgAlbum;
    TextView mTvTitle, mTvCurrentTime, mTvTotalTime;
    SeekBar mSeekBar;
    Handler mHandler = new Handler();
    TextView mBtnPlay;
    Boolean mBooleanCheck = true;
    AudioModel audioModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        init();
        onClick();

//        Intent intent = new Intent(PlayActivity.this, PlayMusicService.class);
//        startService(intent);

        Intent intent = new Intent("test.BroadcastReceiver");
        sendBroadcast(intent);
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
                Intent intent = new Intent(PlayActivity.this, PlayMusicService.class);
                startService(intent);
//                intent.setAction("test.BroadcastReceiver");
//                sendBroadcast(intent);
//                IntentFilter intentFilter = new IntentFilter("test.BroadcastReceiver");
//                mPendingIntent = PendingIntent.getBroadcast(PlayActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//                Toast.makeText(musicSrv, ""+mPendingIntent, Toast.LENGTH_SHORT).show();


//                if (mBooleanCheck) {
//                    mMediaPlayer.pause();
//                    mBooleanCheck = false;
////                    mImgAlbum.startAnimation(rotate);
//                    rotate.cancel();
//                } else {
//                    mMediaPlayer.start();
//                    mBooleanCheck = true;
//                    rotate.start();
//                }
                // tạo thông báo
//                Intent resultIntent = new Intent(PlayActivity.this,MainActivity.class);
//                PendingIntent resultPending = PendingIntent.getActivity(PlayActivity.this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(PlayActivity.this)
//                        .setSmallIcon(R.drawable.bg_musicerror)
//                        .setContentTitle(audioModel.getTitle())
//                        .setContentText("Test thử");
//                mBuilder.setContentIntent(resultPending);
//                NotificationManager mNtfctManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                mNtfctManager.notify(0,mBuilder.build());
            }
        });
    }

    private void addData() {
        audioModel = getIntent().getExtras().getParcelable("obj");
        Uri uri = Uri.parse(audioModel.getUrl());
        Bitmap bitmap = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(audioModel.getIdAlbum()), this));
        if (bitmap == null) {
            mImgAlbum.setImageResource(R.drawable.bg_musicerror);
        } else {
            mImgAlbum.setImageBitmap(bitmap);
        }
        mTvTitle.setText(audioModel.getTitle());
        mMediaPlayer = MediaPlayer.create(this, uri);
        mMediaPlayer.start();

        rotate = new RotateAnimation(0, 10000, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(mMediaPlayer.getDuration());
//        rotate.setInterpolator(new LinearInterpolator());
        mImgAlbum.startAnimation(rotate);

        //xử lý
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mTvTotalTime.setText(HandlingMusic.createTimerLabel(mMediaPlayer.getDuration()));
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mMediaPlayer.seekTo(i);
                    mSeekBar.setProgress(i);
                    mTvCurrentTime.setText(HandlingMusic.createTimerLabel(mMediaPlayer.getCurrentPosition()));
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
                if (mMediaPlayer != null) {
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    int mCurrentPosition = mSeekBar.getProgress();
                    mTvCurrentTime.setText(HandlingMusic.createTimerLabel(mCurrentPosition));
                    if (mSeekBar.getProgress() == mSeekBar.getMax()) {
                        rotate.cancel();
                        mImgAlbum.startAnimation(rotate);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                //shuffle
                break;
            case R.id.btnPlay:
                stopService(playIntent);
                musicSrv = null;
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
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }
}

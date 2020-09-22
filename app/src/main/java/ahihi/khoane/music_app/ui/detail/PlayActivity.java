package ahihi.khoane.music_app.ui.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ahihi.khoane.music_app.interfacce.ServiceCallbacks;
import ahihi.khoane.music_app.model.CallBackModel;
import ahihi.khoane.music_app.model.postPosition;
import ahihi.khoane.music_app.ui.main.MainActivity;
import ahihi.khoane.music_app.utils.CreateNotification;
import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.services.MusicService;
import ahihi.khoane.music_app.utils.Playable;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlayActivity extends AppCompatActivity implements Playable {

    private static final String TAG = "zzz";

    CircleImageView mImgAlbum;
    TextView mTvTitle, mTvCurrentTime, mTvTotalTime;
    SeekBar mSeekBar;
    Handler mHandler = new Handler();
    TextView mBtnPlay, mBtnPrevious, mBtnNext;
    private int postion;

//    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        init();
        onClick();
        Intent intent = getIntent();
        postion = intent.getIntExtra("postion",0);
        addData(postion);
//        Intent intent = new Intent("test.BroadcastReceiver");
//        sendBroadcast(intent);
    }

    private void stopAnimation() {
        mImgAlbum.animate().cancel();
    }

    private void startAnimation() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mImgAlbum.animate().rotationBy(360).withEndAction(this)
                        .setDuration(15000)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        };
        mImgAlbum.animate().rotationBy(360).withEndAction(runnable)
                .setDuration(15000)
                .setInterpolator(new LinearInterpolator()).start();
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
        EventBus.getDefault().register(this);
        Log.d(TAG, "onStart: ");
    }

    private void onClick() {
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new CallBackModel("1"));
            }
        });
        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.arrayList.get(0)==MainActivity.arrayList.get(postion)){
                    EventBus.getDefault().post(new CallBackModel("0"));
                    postion = PlayMusicService.position;
                }
//                onMusicPrevious();
//                Log.d(TAG, "onClick: "+MainActivity.arrayList.size());
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.arrayList.get(MainActivity.arrayList.size()-1)==MainActivity.arrayList.get(postion)){
                    EventBus.getDefault().post(new CallBackModel("3"));
                    postion = PlayMusicService.position;
                }
            }
        });
    }

    private void addData(int pos) {
        startAnimation();
        mSeekBar.setProgress(PlayMusicService.mediaPlayer.getCurrentPosition() / 1000 % 60);
        mSeekBar.setMax(PlayMusicService.mediaPlayer.getDuration());
        mTvTotalTime.setText(HandlingMusic.createTimerLabel(PlayMusicService.mediaPlayer.getDuration()));

        Bitmap bitmap = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(MainActivity.arrayList.get(pos).getIdAlbum()), this));
        if (bitmap == null) {
            mImgAlbum.setImageResource(R.drawable.bg_musicerror);
        } else {
            mImgAlbum.setImageBitmap(bitmap);
        }
        mTvTitle.setText(MainActivity.arrayList.get(pos).getTitle());
        //xử lý
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
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
                R.drawable.ic_baseline_pause_24, PlayMusicService.position, MainActivity.arrayList.size() - 1);
        PlayMusicService.mediaPlayer.start();
        mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
    }

    @Override
    public void onMusicPause() {
        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
                R.drawable.ic_baseline_play_arrow_24, PlayMusicService.position, MainActivity.arrayList.size() - 1);
        PlayMusicService.mediaPlayer.pause();
        mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
    }

    @Override
    public void onMusicNext() {
        PlayMusicService.position++;
        CreateNotification.createNotification(this, MainActivity.arrayList.get(PlayMusicService.position),
                R.drawable.ic_baseline_pause_24, PlayMusicService.position, MainActivity.arrayList.size() - 1);
        play(PlayMusicService.position);
    }

    private void play(int po) {
        if (PlayMusicService.mediaPlayer.isPlaying()) {
            PlayMusicService.mediaPlayer.stop();
        }
        Uri uri = Uri.parse(MainActivity.arrayList.get(po).getUrl());//"content://media/external/audio/media/25"
        PlayMusicService.mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        PlayMusicService.mediaPlayer.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void call(postPosition pos) {
        addData(pos.getPos());
        if (PlayMusicService.isPlaying){
            startAnimation();
            mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } else {
            stopAnimation();
            mBtnPlay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
    }
}

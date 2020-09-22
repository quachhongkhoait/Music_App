package ahihi.khoane.music_app.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.ui.detail.PlayActivity;

public class MainActivity extends AppCompatActivity implements AdapterAudio.OnClickItemMusicListener {

    public static ArrayList<AudioModel> arrayList = new ArrayList<>();
    AdapterAudio adapter;
    RecyclerView mRecyclerView;
    Uri urltest = null;
    String urlimage = null;
    ImageView mImageView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        onValue();
        onEvent();
        adapter.setOnClickItemMusicListener(this);
    }

    private void stopService() {
        Intent intent = new Intent(this, PlayMusicService.class);
        stopService(intent);
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else {
            getMusic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Oke đúng k", Toast.LENGTH_SHORT).show();
                        getMusic();
                    }
                } else {
                    Toast.makeText(this, "Không ok rồi", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    private void getMusic() {
        arrayList.clear();
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentDuration = songCursor.getString(songDuration);
//                String currentDuration = HandlingMusic.convertDuration(songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                Long ur = songCursor.getLong(songID);
                Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ur);
                String albumId = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                arrayList.add(new AudioModel(currentTitle, currentDuration, String.valueOf(trackUri), albumId));
                urltest = trackUri;
                urlimage = albumId;
            } while (songCursor.moveToNext());
            adapter.notifyDataSetChanged();
        }
    }

    private void init() {
        mImageView = findViewById(R.id.img);
        mRecyclerView = findViewById(R.id.mReCyclerView);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new AdapterAudio(this, arrayList);
        mRecyclerView.setAdapter(adapter);
    }

    private void onValue() {
        checkPermission();
    }

    private void onEvent() {
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(arrayList);
//        editor.putString("listmusic", json);
//        editor.commit();
    }

    @Override
    public void onclickItem(int position) {
        Intent mIntent = new Intent(this, PlayActivity.class);
        mIntent.putExtra("postion", position);
        startActivity(mIntent);
        Intent intent = new Intent(this, PlayMusicService.class);
        // Check API Version
        intent.putExtra("postion", position);
        ContextCompat.startForegroundService(this, intent);
        EventBus.getDefault().post(new AudioModel(MainActivity.arrayList.get(position).getTitle(),
                MainActivity.arrayList.get(position).getDuration(),
                MainActivity.arrayList.get(position).getUrl(), MainActivity.arrayList.get(position).getIdAlbum()));
    }
}

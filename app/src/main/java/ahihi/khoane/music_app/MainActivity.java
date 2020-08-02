package ahihi.khoane.music_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<AudioModel> arrayList = new ArrayList<>();
    AdapterAudio adapter;
    RecyclerView mRecyclerView;
    Uri urltest = null;
    String urlimage = null;
    ImageView imageView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
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
            init();
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
                        init();
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
        }
    }

    private void init() {
        imageView = findViewById(R.id.img);
        mRecyclerView = findViewById(R.id.mReCyclerView);
        getMusic();
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new AdapterAudio(this, arrayList);
        adapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(adapter);
    }
}

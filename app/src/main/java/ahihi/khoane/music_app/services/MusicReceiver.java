package ahihi.khoane.music_app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ahihi.khoane.music_app.services.PlayMusicService;

public class MusicReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("nnn", "onReceive: "+intent.getStringExtra("khoa"));
        Intent mIntent = new Intent(context, PlayMusicService.class);
        context.startService(mIntent);
    }
}

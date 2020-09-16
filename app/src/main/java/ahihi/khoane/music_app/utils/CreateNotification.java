package ahihi.khoane.music_app.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ahihi.khoane.music_app.App;
import ahihi.khoane.music_app.R;
import ahihi.khoane.music_app.broadcast.NotificationActionService;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.ui.detail.PlayActivity;
import ahihi.khoane.music_app.ui.main.MainActivity;

public class CreateNotification {

    public static Notification notification;

    public static void createNotification(Context context, AudioModel audioModel, int playbutton, int postion, int size){

        Bitmap icon = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(MainActivity.arrayList.get(postion).getIdAlbum()), context));

        PendingIntent pendingIntentPrevious;
        int drw_previous;
        if (postion == 0){
            pendingIntentPrevious = null;
            drw_previous = 0;
        } else {
            Intent intentPrevious = new Intent(context, NotificationActionService.class)
                    .setAction("ACTION_PREVIUOS");
            pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_previous = R.drawable.ic_baseline_skip_previous_24;
        }

        Intent intentPlay = new Intent(context, NotificationActionService.class)
                .setAction("ACTION_PLAY");
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentNext;
        int drw_next;
        if (postion == size){
            pendingIntentNext = null;
            drw_next = 0;
        } else {
            Intent intentNext = new Intent(context, NotificationActionService.class)
                    .setAction("ACTION_NEXT");
            pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                    intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_next = R.drawable.ic_baseline_skip_next_24;
        }

        Intent intentClose = new Intent(context, NotificationActionService.class)
                .setAction("ACTION_CLOSE");
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 0,
                intentClose, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notificationIntent = new Intent(context, PlayActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,notificationIntent,0);

        notification = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                .setContentTitle(audioModel.getTitle())
                .setContentText(audioModel.getDuration())
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(icon)
                .addAction(drw_previous,"", pendingIntentPrevious)
                .addAction(playbutton,"", pendingIntentPlay)
                .addAction(drw_next,"", pendingIntentNext)
                .addAction(0,"Close", pendingIntentClose)
                .setContentIntent(pendingIntent)
                .build();
    }
}

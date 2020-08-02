package ahihi.khoane.music_app;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel1";

    public static final String ACTIONPREVIOUS = "actionprevious";
    public static final String CHANNEL_PLAY = "actionplay";
    public static final String CHANNEL_NEXT = "actionnext";

    public static Notification sNotification;

    public static void createNotification(Context context,AudioModel audioModel, int playbutton, int pos, int size){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context,"tag");
            Bitmap icon = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(audioModel.getIdAlbum()), context));
            sNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.bg_musicerror)
                    .setContentTitle(audioModel.getTitle())
                    .setContentText(audioModel.getDuration())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
            notificationManagerCompat.notify(1, sNotification);
        }
    }
}

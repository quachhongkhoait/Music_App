package ahihi.khoane.music_app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class App extends Application {
    public static String CHANNEL_ID = "ServiceChannel";
    public static LocalBroadcastManager mBroadcaster;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thong bao nhac",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }
}

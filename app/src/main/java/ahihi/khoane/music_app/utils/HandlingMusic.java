package ahihi.khoane.music_app.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class HandlingMusic {
//    public static String convertDuration(long duration) {
//        String out = null;
//        long hours = 0;
//        try {
//            hours = (duration / 3600000);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            return out;
//        }
//        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
//        String minutes = String.valueOf(remaining_minutes);
//        if (minutes.equals(0)) {
//            minutes = "00";
//        }
//        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000));
//        String seconds = String.valueOf(remaining_seconds);
//        if (seconds.length() < 2) {
//            seconds = "00";
//        } else {
//            seconds = seconds.substring(0, 2);
//        }
//
//        if (hours > 0) {
//            out = hours + ":" + minutes + ":" + seconds;
//        } else {
//            out = minutes + ":" + seconds;
//        }
//
//        return out;
//
//    }

    public static String getCoverArtPath(long albumId, Context context) {

        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );
        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }

    public static String createTimerLabel(int duration) {
        String timerLabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        timerLabel += min + ":";
        if (sec < 10) timerLabel += "0";
        timerLabel += sec;
        return timerLabel;
    }
}

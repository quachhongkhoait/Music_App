package ahihi.khoane.music_app;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioModel implements Parcelable {
    private String title;
    private String duration;
    private String url;
    private String idAlbum;

    public AudioModel(String title, String duration, String url, String idAlbum) {
        this.title = title;
        this.duration = duration;
        this.url = url;
        this.idAlbum = idAlbum;
    }

    protected AudioModel(Parcel in) {
        title = in.readString();
        duration = in.readString();
        url = in.readString();
        idAlbum = in.readString();
    }

    public static final Creator<AudioModel> CREATOR = new Creator<AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel in) {
            return new AudioModel(in);
        }

        @Override
        public AudioModel[] newArray(int size) {
            return new AudioModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(String idAlbum) {
        this.idAlbum = idAlbum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(duration);
        parcel.writeString(url);
        parcel.writeString(idAlbum);
    }
}

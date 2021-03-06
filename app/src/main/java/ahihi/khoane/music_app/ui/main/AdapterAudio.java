package ahihi.khoane.music_app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ahihi.khoane.music_app.interfacce.OnClick;
import ahihi.khoane.music_app.model.AudioModel;
import ahihi.khoane.music_app.services.PlayMusicService;
import ahihi.khoane.music_app.utils.HandlingMusic;
import ahihi.khoane.music_app.ui.detail.PlayActivity;
import ahihi.khoane.music_app.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterAudio extends RecyclerView.Adapter<AdapterAudio.ViewHolder> {

    interface OnClickItemMusicListener {
        void onclickItem(int position);
    }

    private OnClickItemMusicListener onClickItemMusicListener;
    Context mContext;
    List<AudioModel> mLvAudioModel;


    public AdapterAudio(Context context, List<AudioModel> items) {
        this.mContext = context;
        this.mLvAudioModel = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_music, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.mTvTitle.setText(mLvAudioModel.get(position).getTitle());
        holder.mTvDuration.setText(HandlingMusic.createTimerLabel(Integer.parseInt(mLvAudioModel.get(position).getDuration())));
        Bitmap bitmap = BitmapFactory.decodeFile(HandlingMusic.getCoverArtPath(Long.parseLong(mLvAudioModel.get(position).getIdAlbum()), mContext));
        if (bitmap == null) {
            holder.mImgAlbum.setImageResource(R.drawable.bg_musicerror);
        } else {
            holder.mImgAlbum.setImageBitmap(bitmap);
        }
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickItemMusicListener.onclickItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLvAudioModel.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvTitle, mTvDuration;
        CircleImageView mImgAlbum;
        CardView mCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tvTitle);
            mTvDuration = itemView.findViewById(R.id.tvDuration);
            mImgAlbum = itemView.findViewById(R.id.imageAlbum);
            mCardView = itemView.findViewById(R.id.cardview);
//            mCardView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent mIntent = new Intent(mContext, PlayActivity.class);
//                    mContext.startActivity(mIntent);
//                    Intent intent = new Intent(mContext, PlayMusicService.class);
//                    //Check API Version
//                    intent.putExtra("postion",
//
//                            getAdapterPosition());
//                    ContextCompat.startForegroundService(mContext, intent);
//                }
//            });
        }
    }

    public void setOnClickItemMusicListener(OnClickItemMusicListener onClickItemMusicListener) {
        this.onClickItemMusicListener = onClickItemMusicListener;
    }
}

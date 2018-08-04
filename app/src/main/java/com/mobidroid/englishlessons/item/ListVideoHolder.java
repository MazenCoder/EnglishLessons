package com.mobidroid.englishlessons.item;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mobidroid.englishlessons.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ListVideoHolder extends RecyclerView.ViewHolder {

    private ImageView imageListVideo;
    private TextView tvListVideoTitle;

    public ListVideoHolder(@NonNull View itemView) {
        super(itemView);
        imageListVideo = itemView.findViewById(R.id.image_list_video);
        tvListVideoTitle = itemView.findViewById(R.id.tv_title_list_video);
    }

    public void bind(addVideo model) {
//        Picasso.get()
//                .load(model.getDownload_url())
//                .centerCrop()
//                .fit()
//                .into(imageListVideo);

        Glide.with(imageListVideo.getContext())
                .asBitmap()
                .load(model.getDownload_url())
//                .load(Uri.fromFile(new File(model.getDownload_url())))
                .into(imageListVideo);

        tvListVideoTitle.setText(model.getTitle_video());
    }
}
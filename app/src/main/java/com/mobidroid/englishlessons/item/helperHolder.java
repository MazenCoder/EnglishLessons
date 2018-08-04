package com.mobidroid.englishlessons.item;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobidroid.englishlessons.R;
import com.squareup.picasso.Picasso;

public class helperHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView imageCourse;
    private TextView tvTitle;

    public helperHolder(@NonNull View itemView) {
        super(itemView);

        imageCourse = itemView.findViewById(R.id.img_course);
        tvTitle = itemView.findViewById(R.id.tv_title_course);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
    }



    public void bind(Course model) {
        Picasso.get()
                .load(model.getImage())
                .centerCrop()
                .fit()
                .into(imageCourse);

        tvTitle.setText(model.getTitle());
    }
}

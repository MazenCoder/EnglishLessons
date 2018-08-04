package com.mobidroid.englishlessons.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.item.Course;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.MyViewHolder> {

    private static final String TAG = "CourseAdapter";
    List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.course_card, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Course course = courseList.get(i);
        myViewHolder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageCourse;
        private TextView tvTitleCourse;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageCourse   = (ImageView) itemView.findViewById(R.id.img_course);
            tvTitleCourse = (TextView) itemView.findViewById(R.id.tv_title_course);
            itemView.setOnClickListener(this);

        }

        public void bind(Course course) {
            Picasso.get().load(course.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageCourse);

            tvTitleCourse.setText(course.getTitle());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d(TAG, "onClick: position: "+position);
        }
    }
}

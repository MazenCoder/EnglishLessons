package com.mobidroid.englishlessons.admin;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.adapter.FragmentAdapter;
import com.mobidroid.englishlessons.fragment.CourseFragment;
import com.mobidroid.englishlessons.fragment.ImageFragment;
import com.mobidroid.englishlessons.fragment.VideoFragment;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    //  variables
    private static final String TAG = "AdminActivity";

    //  widget
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        init();
    }

    private void init() {
        // TabLayout
        tabLayout = (TabLayout)findViewById(R.id.tab_layout);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ImageFragment());
        fragments.add(new CourseFragment());
        fragments.add(new VideoFragment());

        // ViewPager
        viewPager = (ViewPager)findViewById(R.id.view_pager);

        // Adapter
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
//        viewPager.beginFakeDrag();
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_close);
        tabLayout.getTabAt(0).setText("IMAGES");

        tabLayout.getTabAt(1).setIcon(R.drawable.ic_close);
        tabLayout.getTabAt(1).setText("COURSE");

        tabLayout.getTabAt(2).setIcon(R.drawable.ic_close);
        tabLayout.getTabAt(2).setText("VIDEOS");
    }

    public void addCourse(View view) {
        startActivity(new Intent(getApplicationContext(), AddCourseActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

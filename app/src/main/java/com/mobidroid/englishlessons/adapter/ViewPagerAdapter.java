package com.mobidroid.englishlessons.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mobidroid.englishlessons.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    //private Integer [] images = {R.drawable.image1,R.drawable.image2,R.drawable.image3};
    private List<String> uris;

    private static final String TAG = "ViewPagerAdapter";


    public ViewPagerAdapter(Context context, List<String> uriList) {
        this.context = context;
        this.uris = uriList;
        Log.d(TAG, "ViewPagerAdapter: "+uriList.size());
    }

    @Override
    public int getCount() {
        return uris.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        Picasso.get().load(uris.get(position))
                .centerCrop()
                .fit()
                .placeholder(R.drawable.ic_launcher_background)
                .into(imageView);
        //imageView.setImageResource(images[position]);

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
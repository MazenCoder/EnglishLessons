package com.mobidroid.englishlessons.app;

import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.adapter.CourseAdapter;
import com.mobidroid.englishlessons.adapter.ViewPagerAdapter;
import com.mobidroid.englishlessons.admin.AdminActivity;
import com.mobidroid.englishlessons.item.Course;
import com.mobidroid.englishlessons.item.Images;
import com.mobidroid.englishlessons.item.KEY;
import com.mobidroid.englishlessons.item.helperHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener {

    //  widget
    private Toolbar toolbar_home;
    private ViewPager viewPager;
    private RecyclerView recyclerView_home;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View headerLayout;
    private CircleIndicator indicator;
    private ProgressBar progressBar_home;
    private SwipeRefreshLayout swipeRefresh_home;

    //  variable
    private Images get_images;
    private List<String> uriList = new ArrayList<>();
    private ViewPagerAdapter viewPagerAdapter;
    private FirestorePagingAdapter<Course, helperHolder> adapter;

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        init();
        getImages();
        //initAdapter();
        queryData();
        //initViewPager();
    }

    private void init() {

        swipeRefresh_home = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_home);
        swipeRefresh_home.setOnRefreshListener(this);
        swipeRefresh_home.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        indicator = (CircleIndicator) findViewById(R.id.indicator);
        //  toolbar
        toolbar_home = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar_home);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        progressBar_home = (ProgressBar)findViewById(R.id.progress_bar_home);
        recyclerView_home = (RecyclerView) findViewById(R.id.recycler_view_home);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout   = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar_home,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        headerLayout = navigationView.inflateHeaderView(R.layout.navigation_header);
        FloatingActionButton but_admin = (FloatingActionButton)headerLayout.findViewById(R.id.fab_nav_header_admin);

        but_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
    }

    private void getImages() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(KEY.IMAGES).document(KEY.IMAGES);

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //images = documentSnapshot.toObject(Images.class).getImages_uri_map();

                    get_images = documentSnapshot.toObject(Images.class);

//                List<String> uris = new ArrayList<>();
                    if (get_images != null) {
                        String uri;
                        if (get_images.getImages_uri_map() != null && get_images.getImages_uri_map().size() != 0) {

                            Log.d(TAG, "getImages: "+get_images.getImages_uri_map().size());

                            Set<Map.Entry<String, String>> value = get_images.getImages_uri_map().entrySet();
                            for (Map.Entry<String, String> entry : value) {
                                Log.d(TAG, "bindAds: get key" + entry.getKey() + "  get Values" + entry.getValue());
                                uri = get_images.getImages_uri_map().get(entry.getKey());
                                uriList.add(uri);
                                Log.d(TAG, "onSuccess: "+uri);
                            }
                            viewPagerAdapter = new ViewPagerAdapter(getApplicationContext(), uriList);
                            Log.d(TAG, "onSuccess: uris: "+uriList.size());
                            viewPager.setAdapter(viewPagerAdapter);
                            indicator.setViewPager(viewPager);
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(new MyTimerTask(viewPagerAdapter.getCount()), 3000, 5000);
                        }
                    }
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "getImages: error "+e.getMessage());
        }
    }

    private void initViewPager() {
        Log.d(TAG, "initViewPager: uri list size: "+uriList.size());
//        viewPager.setAdapter(viewPagerAdapter);
//        indicator.setViewPager(viewPager);
//        Log.d(TAG, "getImages: "+viewPagerAdapter.getCount());

//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new MyTimerTask(viewPagerAdapter.getCount()), 3000, 5000);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){

        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onRefresh() {
        try {
            queryData();
            swipeRefresh_home.setRefreshing(false);
        }catch (Exception e) {
            Log.e(TAG, "onRefresh error: "+e.getMessage());
        }
    }

    public class MyTimerTask extends TimerTask {

        int size;
        public MyTimerTask(int count) {
//            this.count = new int[count];
            this.size = count;
        }

        @Override
        public void run() {

            HomeActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (viewPager.getCurrentItem() <= size) {
                            if (viewPager.getCurrentItem()== size-1) {
                                viewPager.setCurrentItem(0);
                            }else {
                                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                            }

                        }
                    }catch (Exception e) { }

                }
            });

        }
    }

    private void queryData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView_home.setLayoutManager(gridLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.COURSE)
                .orderBy("time_created", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Course> options = new FirestorePagingOptions.Builder<Course>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Course.class)
                .build();


        adapter = new FirestorePagingAdapter<Course, helperHolder>(options) {

            @NonNull
            @Override
            public helperHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.course_card, parent, false);
                return new helperHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull final Course model) {
                holder.bind(model);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), List_VideoActivity.class);
                        intent.putExtra(KEY.ID_COURSE, model.getTitle());
                        Log.d(TAG, "onClick: "+model.getTitle());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {

                    // initial load begun
                    case LOADING_INITIAL:
                        Log.d(TAG,"LOADING LOADING INITIAL");
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.INVISIBLE) {
                            progressBar_home.setVisibility(View.VISIBLE);
                        }
                        break;

                    case LOADING_MORE:
                        //loading an additional page
                        Log.d("LOADING", "LOADING MORE");
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.INVISIBLE) {
                            progressBar_home.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LOADED:
//                        intReedsLogged = firestorePagingAdapter.getItemCount();
//                        reedsLogged.setText(String.valueOf(intReedsLogged));
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }
                        // previous load (either initial or additional) completed
                        Log.d("LOADING", "LOADED");
                        break;

                    case FINISHED:
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }
                        Log.d("LOADING","FINISHED");

                        break;

                    case ERROR:
                        //previous load (either initial or additional) failed.  Call the retry() method to retry load.
                        Log.d("LOADING", "LOADING error ");
                        Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_LONG).show();
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }

                        //warningDialog();
                        break;
                }
            }
        };

        recyclerView_home.setAdapter(adapter);
    }

    private void initAdapter() {
        CourseAdapter adapter = new CourseAdapter(KEY.courseList);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView_home.setLayoutManager(gridLayoutManager);
        recyclerView_home.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(HomeActivity.this);
            }
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.do_you_want_to_exit));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.finishAffinity(HomeActivity.this);
                    finish();
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
}

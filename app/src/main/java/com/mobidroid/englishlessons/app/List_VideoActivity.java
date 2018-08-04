package com.mobidroid.englishlessons.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.item.KEY;
import com.mobidroid.englishlessons.item.ListVideoHolder;
import com.mobidroid.englishlessons.item.addVideo;

import javax.annotation.Nullable;

public class List_VideoActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "List_VideoActivity";
    private FirestoreRecyclerAdapter adapter;

    private String title;

    //  widget
    private RecyclerView recyclerView_list_video;
    private SwipeRefreshLayout swipeRefreshListVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list__video);

        init();

        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra(KEY.ID_COURSE);
            Log.d(TAG, "onCreate: "+title);

            queryData(title);
        }
    }

    private void init() {
        recyclerView_list_video = (RecyclerView) findViewById(R.id.recycler_view_list_video);

        swipeRefreshListVideo = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list_video);
        swipeRefreshListVideo.setOnRefreshListener(this);
        swipeRefreshListVideo.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void queryData(String title) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView_list_video.setLayoutManager(gridLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.VIDEO)
                .whereEqualTo("title_course", title);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: error: "+e.getMessage());
                    return;
                }else {
                    Log.d(TAG, "onEvent: ");
                }
            }
        });
//                .orderBy("time_created", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<addVideo> options = new FirestoreRecyclerOptions.Builder<addVideo>()
                .setQuery(query, addVideo.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<addVideo, ListVideoHolder>(options) {


            @NonNull
            @Override
            public ListVideoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_list_video, viewGroup, false);
                return new ListVideoHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ListVideoHolder holder, int position, @NonNull addVideo model) {
                holder.bind(model);
            }
        };

        recyclerView_list_video.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        try {
            if (!TextUtils.isEmpty(title)) {
                recreate();
                queryData(title);
                swipeRefreshListVideo.setRefreshing(false);
            }
        }catch (Exception e) {
            Log.e(TAG, "onRefresh error: "+e.getMessage());
        }
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
}

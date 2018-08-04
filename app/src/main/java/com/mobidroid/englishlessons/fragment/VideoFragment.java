package com.mobidroid.englishlessons.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.item.Check;
import com.mobidroid.englishlessons.item.Course;
import com.mobidroid.englishlessons.item.KEY;
import com.mobidroid.englishlessons.item.addVideo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private AppCompatSpinner spinner;
    private static final String TAG = "VideoFragment";
    private List<String> courseListTitle = new ArrayList<>();
    private List<String> courseListId = new ArrayList<>();
    private Set<String> setTitle = new HashSet<String>();
    private Set<String> setId = new HashSet<String>();
    private ArrayAdapter<String> adapter;
    private addVideo video;
    private List<Course> courseArrayList = new ArrayList<>();
    private int position;
//    private Uri uri = new Uri();
    private String uri_video = "";

    private TextInputLayout Til_first_ques, Til_second_ques, Til_third_ques, Til_title_video;
    private TextInputEditText Tie_first_ques, Tie_second_ques, Tie_third_ques, Tie_title_video;

    private TextInputLayout Til_first_answer, Til_second_answer, Til_third_answer;
    private TextInputEditText Tie_first_answer, Tie_second_answer, Tie_third_answer;

    private Button but_upload_video, but_get_video;
    private ProgressBar progress_video;
    private int VIDEO_REQUEST_CODE = 322;

    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public VideoFragment() {
        video = new addVideo();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        spinner = (AppCompatSpinner) view.findViewById(R.id.spinner_course);
        spinner.setOnItemSelectedListener(this);

        Til_first_ques    = (TextInputLayout) view.findViewById(R.id.til_first_ques);
        Til_second_ques   = (TextInputLayout) view.findViewById(R.id.til_second_ques);
        Til_third_ques    = (TextInputLayout) view.findViewById(R.id.til_third_ques);

        Tie_first_ques    = (TextInputEditText) view.findViewById(R.id.tie_first_quest);
        Tie_second_ques   = (TextInputEditText) view.findViewById(R.id.tie_second_quest);
        Tie_third_ques    = (TextInputEditText) view.findViewById(R.id.tie_third_quest);

        Til_first_answer  = (TextInputLayout) view.findViewById(R.id.til_first_answer);
        Til_second_answer = (TextInputLayout) view.findViewById(R.id.til_second_answer);
        Til_third_answer  = (TextInputLayout) view.findViewById(R.id.til_third_answer);

        Tie_first_answer  = (TextInputEditText) view.findViewById(R.id.tie_first_answer);
        Tie_second_answer = (TextInputEditText) view.findViewById(R.id.tie_second_answer);
        Tie_third_answer  = (TextInputEditText) view.findViewById(R.id.tie_third_answer);

        Til_title_video   = (TextInputLayout) view.findViewById(R.id.til_title_video);
        Tie_title_video   = (TextInputEditText) view.findViewById(R.id.tie_title_video);

        progress_video    = (ProgressBar) view.findViewById(R.id.progress_video);

        but_get_video     = (Button) view.findViewById(R.id.but_get_video);
        but_get_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(Intent.createChooser(intent,"Select Video"), VIDEO_REQUEST_CODE);
            }
        });

        but_upload_video  = (Button) view.findViewById(R.id.but_upload_video);
        but_upload_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkText();
            }
        });

        return view;
    }

    private void getCourse() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(KEY.COURSE)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                    Course course = document.toObject(Course.class);
                                    courseArrayList.add(course);
                                }

                                boolean sendSuccessful = loadData(courseArrayList);
                                if (sendSuccessful) {
                                    adapter = new ArrayAdapter<String>(getActivity(),
                                            android.R.layout.simple_spinner_item, courseListTitle);
                                    // Specify the layout to use when the list of choices appears
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    // Apply the adapter to the spinner
                                    spinner.setAdapter(adapter);
                                }

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }catch (Exception e) {
            Log.e(TAG, "getCourse: error "+e.getMessage());
        }
    }

    private boolean loadData(List<Course> courseArrayList) {
        try {
            if (courseArrayList.size() < 0) {
                Log.e(TAG, "setData: error no date");
                return false;
            }else {

                for (int i = 0; i < courseArrayList.size(); i++) {
                    setId.add(courseArrayList.get(i).getId());
                    Log.d(TAG, "setData id: "+courseArrayList.get(i).getId());

                    setTitle.add(courseArrayList.get(i).getTitle());
                    Log.d(TAG, "setData title: "+courseArrayList.get(i).getTitle());
                }

                courseListId = new ArrayList<>(setId);
                courseListTitle = new ArrayList<>(setTitle);
                return true;
            }
        }catch (Exception e) {
            Log.e(TAG, "setData exception: "+e.getMessage());
            return false;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getActivity(), adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemSelected: "+adapterView.getItemAtPosition(i));

        this.position = i;

        Log.d(TAG, "uploadVideo: course id: "+courseListId.get(i));
        Log.d(TAG, "uploadVideo: course title: "+courseListTitle.get(i));

        video.setId_course(courseListId.get(i));
        video.setTitle_course(adapterView.getItemAtPosition(i).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getCourse();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == VIDEO_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {

            if (data.getData() != null) {
                uri_video = data.getData().toString();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void checkText() {
        if (!TextUtils.isEmpty(Tie_first_ques.getText().toString())) {
            Til_first_ques.setErrorEnabled(false);

            if (!TextUtils.isEmpty(Tie_first_answer.getText().toString())) {
                Til_first_answer.setErrorEnabled(false);

                if (!TextUtils.isEmpty(Tie_second_ques.getText().toString())) {
                    Til_second_ques.setErrorEnabled(false);

                    if (!TextUtils.isEmpty(Tie_second_answer.getText().toString())) {
                        Til_second_answer.setErrorEnabled(false);

                        if (!TextUtils.isEmpty(Tie_third_ques.getText().toString())) {
                            Til_third_ques.setErrorEnabled(false);

                            if (!TextUtils.isEmpty(Tie_third_answer.getText().toString())) {
                                Til_third_answer.setErrorEnabled(false);

                                if (!TextUtils.isEmpty(uri_video)) {

                                    if (!TextUtils.isEmpty(video.getTitle_course())) {
                                        if (!TextUtils.isEmpty(Tie_title_video.getText().toString())) {
                                            Til_title_video.setErrorEnabled(false);

                                            uploadVideo(
                                                    Tie_title_video.getText().toString(),
                                                    Tie_first_ques.getText().toString(),
                                                    Tie_first_answer.getText().toString(),

                                                    Tie_second_ques.getText().toString(),
                                                    Tie_second_answer.getText().toString(),

                                                    Tie_third_ques.getText().toString(),
                                                    Tie_third_answer.getText().toString()
                                            );
                                        }else {
                                            Til_title_video.setError("you must select a title video");
                                        }
                                    }else {
                                        Toast.makeText(getActivity(), "you must select a title course", Toast.LENGTH_LONG).show();
                                    }
                                }else {
                                    Toast.makeText(getActivity(), "you must select a video", Toast.LENGTH_LONG).show();
                                }
                            }else {
                                Til_third_answer.setError("this field is required");
                            }
                        }else {
                            Til_third_ques.setError("this field is required");
                        }
                    }else {
                        Til_second_answer.setError("this field is required");
                    }
                }else {
                    Til_second_ques.setError("this field is required");
                }
            }else {
                Til_first_answer.setError("this field is required");
            }
        }else {
            Til_first_ques.setError("this field is required");
        }
    }

    private void uploadVideo(String title_video, String first_question, String first_answer, String second_question, String second_answer, String third_question, String third_answer) {
        progress_video.setVisibility(View.VISIBLE);

        video.setTitle_video(title_video);

        video.setFirst_question(first_question);
        video.setFirst_answer(first_answer);

        video.setSecond_question(second_question);
        video.setSecond_answer(second_answer);

        video.setThird_question(third_question);
        video.setThird_answer(third_answer);

        storageReference = FirebaseStorage.getInstance().getReference()
                .child(getString(R.string.app_name))
                .child(KEY.COURSE)
                .child(courseArrayList.get(position).getId())
                .child(courseArrayList.get(position).getTitle())
                .child(title_video.trim());

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .setContentLanguage("en")
                .setCustomMetadata("video course meta date", "MR nothing special here")
                .setCustomMetadata("video", "for course english")
                .build();

        UploadTask uploadTask = storageReference.putFile(Uri.parse(uri_video), metadata);
        uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
//                        Check.ToastMessage(getApplicationContext(), "upload image");
                    Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.upload_image));

                    ///**
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: uri: "+uri);
                            video.setDownload_url(uri.toString());
                                    //documentReference
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            final DocumentReference documentReference = db.collection(KEY.VIDEO)
                                    .document();

                            video.setId_video(documentReference.getId());

                                    documentReference.set(video)
                                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Updated FireStore");
//                                        Check.hideDialog(progress);
                                            Log.d(TAG, "onSuccess: "+documentReference.getId());
                                            progress_video.setVisibility(View.INVISIBLE);
                                            Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.successful_upload));
                                            emptying();

                                        }
                                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//                                Check.hideDialog(progress);
                                    Log.e(TAG, "onFailure: "+e.getMessage());
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progress_video.setVisibility(View.INVISIBLE);
                            Log.e(TAG, "onFailure: error: "+e.getMessage());
                        }
                    });



                }else {
//                        Check.ToastMessage(getApplicationContext(), "Error upload image");
                    Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                    progress_video.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void emptying() {
        Tie_title_video.setText("");

        Tie_first_ques.setText("");
        Tie_first_answer.setText("");

        Tie_second_ques.setText("");
        Tie_second_answer.setText("");

        Tie_third_ques.setText("");
        Tie_third_answer.setText("");
    }
}

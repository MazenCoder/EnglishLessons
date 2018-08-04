package com.mobidroid.englishlessons.fragment;


import android.arch.paging.PagedList;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.admin.AddCourseActivity;
import com.mobidroid.englishlessons.admin.AdminActivity;
import com.mobidroid.englishlessons.item.Check;
import com.mobidroid.englishlessons.item.Course;
import com.mobidroid.englishlessons.item.KEY;
import com.mobidroid.englishlessons.item.helperHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class CourseFragment extends Fragment {

    private static final String TAG = "CourseFragment";
    private int REQUEST_GALLERY_VIDEO = 23;

    //  widget
    private TextInputLayout Til_title_course;
    private TextInputEditText Tie_title_course;
    private RecyclerView recyclerView_course;
    private ProgressBar progressBar_course;

    private FirestoreRecyclerAdapter adapter;

    //  firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference = db
            .collection(KEY.COURSE)
            .document();

    public CourseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);

        Til_title_course = (TextInputLayout) view.findViewById(R.id.til_title_course);
        Tie_title_course = (TextInputEditText) view.findViewById(R.id.tie_title_course);

        recyclerView_course = (RecyclerView) view.findViewById(R.id.recyclerView_course);
        progressBar_course  = (ProgressBar) view.findViewById(R.id.progressBar_course);

        Button but_select_video = (Button) view.findViewById(R.id.select_video);
        but_select_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCourse();
            }
        });

        queryData();

        return view;
    }

    public void saveCourse() {
        if (!TextUtils.isEmpty(Tie_title_course.getText().toString())) {
            Til_title_course.setErrorEnabled(false);

            if(Check.mStoragePermissions){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,getString(R.string.select_image)), REQUEST_GALLERY_VIDEO);
            }else{
                Check.verifyStoragePermissions(getActivity(), TAG, REQUEST_GALLERY_VIDEO);
            }
        } else {
            Til_title_course.setError(getString(R.string.enter_title_course));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_GALLERY_VIDEO) {
            if (data.getData() != null) {
                Uri uri_image = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(uri_image.toString()));
                    Bitmap bitmap_result = rotateImageIfRequired(bitmap, uri_image);
                    uploadCourse(bitmap_result);
                } catch (Exception e) {
                    Log.e(TAG, "uploadCourse: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "onActivityResult: uri image is null");
            }
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = getActivity().getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void uploadCourse(Bitmap bitmap) {
        if(bitmap != null){
            Log.d(TAG, "getImageBitmap: got the image bitmap: ");

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.app_name))
                    .child(KEY.COURSE)
                    .child(documentReference.getId())
                    .child(Tie_title_course.getText().toString().toLowerCase().trim());
//                    .child(Tie_title_course.getText().toString().trim());

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en")
                    .setCustomMetadata("image course meta date", "MR nothing special here")
                    .setCustomMetadata("image", "for course english")
                    .build();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data, metadata);
            uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.ToastMessage(getApplicationContext(), "upload image");
                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.upload_image));
                    }else {
//                        Check.ToastMessage(getApplicationContext(), "Error upload image");
                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                    }
                }
            });

            // get image
            //Task<Uri> urlTask =
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Course course = new Course();
                        course.setTitle(Tie_title_course.getText().toString());
                        course.setTime_created(null);
                        course.setId(documentReference.getId());
                        course.setImage(downloadUri.toString());

                        db.collection(KEY.COURSE)
                                .document(documentReference.getId())
                                .set(course).addOnSuccessListener(getActivity(),
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
//                                Check.ToastMessage(getApplicationContext(), "Updated FireStore");
                                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.successful_upload));
                                        Tie_title_course.setText("");
                                    }
                                }).addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                                Log.e(TAG, "onFailure: "+e.getMessage());
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                    //Check.ToastMessage(getApplicationContext(), "Upload is " + progress + "% done");
//                    Toast.makeText(getApplicationContext(), "Upload is " + progress + "% done", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), getString(R.string.upload_is)+" "+ Integer.valueOf((int) progress) + "% "+getString(R.string.done), Toast.LENGTH_SHORT).show();
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                    Log.e(TAG, "onPaused: upload is paused");
                }
            });

        }
    }

    private void queryData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView_course.setLayoutManager(gridLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.COURSE)
                .orderBy("time_created", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Course> options = new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<Course, helperHolder>(options) {

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
//                        AdminActivity activity = (AdminActivity)context;
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        DialogCourseFragment dialogFragment = new DialogCourseFragment ();

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(KEY.COURSE, model);
                        dialogFragment.setArguments(bundle);

                        dialogFragment.show(fm, "Course Fragment");
                    }
                });
            }


        };

        recyclerView_course.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}

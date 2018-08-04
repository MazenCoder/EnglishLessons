package com.mobidroid.englishlessons.admin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.adapter.ImageAdapter;
import com.mobidroid.englishlessons.item.Check;
import com.mobidroid.englishlessons.item.Course;
import com.mobidroid.englishlessons.item.Images;
import com.mobidroid.englishlessons.item.KEY;
import com.mobidroid.englishlessons.item.helperHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddCourseActivity extends AppCompatActivity {

    //  widget
    private TextInputLayout Til_title_course;
    private TextInputEditText Tie_title_course;
    private RecyclerView recyclerView;
    private ViewPager viewPager;

    //  variable
    private int REQUEST_GALLERY_VIDEO = 23;
    private static final int PICKFILE_REQUEST_CODE = 832;//random number
    private boolean mStoragePermissions;
    private Images get_images;
    private int REQUEST_CODE = 33;
    private Map<String, String> uri_image = new HashMap<>();    // Map
    private ImageAdapter pagerAdapter;
    private FirestoreRecyclerAdapter<Images, helperHolder> adapter;
    private List<String> uriList = new ArrayList<>();
    private List<Images> imagesList = new ArrayList<>();

    //  firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notesCollectionRef;

    private DocumentReference documentReference = db
            .collection(KEY.COURSE)
            .document();
    private static final String TAG = "AddCourseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        init();
        verifyStoragePermissions();
//        queryData();
        getImages();
    }

    private void init() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        Til_title_course = (TextInputLayout) findViewById(R.id.til_title_course);
        Tie_title_course = (TextInputEditText) findViewById(R.id.tie_title_course);

        recyclerView     = (RecyclerView) findViewById(R.id.recycler_view_image_home);
    }

    public void selectVideo(View view) {
        if (!TextUtils.isEmpty(Tie_title_course.getText().toString())) {
            Til_title_course.setErrorEnabled(false);

            if(mStoragePermissions){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,getString(R.string.select_image)), REQUEST_GALLERY_VIDEO);
            }else{
                verifyStoragePermissions();
            }
        } else {
          Til_title_course.setError(getString(R.string.enter_title_course));
        }
    }

    public void verifyStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    AddCourseActivity.this, permissions, REQUEST_CODE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY_VIDEO) {
            if (data.getData() != null) {
                Uri uri_image = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , Uri.parse(uri_image.toString()));
                    Bitmap bitmap_result = rotateImageIfRequired(bitmap, uri_image);
                    uploadCourse(bitmap_result);
                }catch (Exception e) {
                    Log.e(TAG, "uploadCourse: "+e.getMessage());
                }
            }else {
                Log.e(TAG, "onActivityResult: uri image is null");
            }
        }

        else if(requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                Log.d(TAG, "onActivityResult: counter is: " + count);
                //List<Images> list_images = new ArrayList<>(); // display image recyclerView
                Uri[] uris = new Uri[count];
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    //Images images = new Images(imageUri); // instance AsdImage
                    //list_images.add(images); // add object Images

                    uris[i] = imageUri;

//                    sendUri(imageUri, String.valueOf(i));
                }

//                mOnPhotoReceived.getImagePath(uris);
                uploadImagesToFireStorage(uris);
                /**
                ListImageAdapter adapter = new ListImageAdapter(list_images);
                recyclerView.setAdapter(adapter);
                //mapImages.getObject(map); // Interface called method and set Object from HashMap

                GridLayoutManager manager = new GridLayoutManager(this, 3);
                recyclerView.setLayoutManager(manager);
                 */
            }
        }
    }

    private void uploadImagesToFireStorage(Uri... uris) {
        if (!uris.toString().equals("")) {
            for (int i = 0; i < uris.length; i++) {
                uploadImage(uris.clone()[i], i);
            }
        }
    }

    private void uploadImage(Uri uri, final int i) {
        Log.d(TAG, "uploadImage: number image "+String.valueOf(i));
//        Check.showDialog(progress);

        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , Uri.parse(uri.toString()));
            Bitmap bitmap_result = rotateImageIfRequired(bitmap, uri);


//            getApplicationContext().getContentResolver().notifyChange(uri, null);
//            ContentResolver cr = getApplicationContext().getContentResolver();
//            Bitmap bitmap_image = android.provider.MediaStore.Images.Media.getBitmap(cr, uri);
//            Bitmap bitmap_result = rotateImageIfRequired(getApplicationContext(), bitmap_image, uri);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap_result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.app_name))
                    .child(KEY.IMAGES)
//                    .child(documentReference.getId())
//                    .child(KEY.IMAGES)
                    .child("img_ads_"+i);

            UploadTask uploadTask = storageReference.putBytes(data);

            uploadTask.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.hideDialog(progress);
                        Log.d(TAG, "onComplete: is successful");
                        Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_image));
                    }else {
                        Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.failed));
                        //Check.hideDialog(progress);
                    }
                }
            });

//            final int finalI = i;
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        //Check.hideDialog(progress);
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

                        uri_image.put("uri_img_"+i, downloadUri.toString());
                        Images images = new Images();
                        images.setImages_uri_map(uri_image);
                        db.collection(KEY.IMAGES)
                                .document(KEY.IMAGES)
                        //documentReference
                                .set(images, SetOptions.merge())
                                .addOnSuccessListener(AddCourseActivity.this, new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Updated FireStore");
//                                        Check.hideDialog(progress);
                                        Log.d(TAG, "onSuccess: "+documentReference.getId());
//                                        for (String token : tokensAdmin) {
//                                            sendMessage(getString(R.string.new_ad_has_been_added),
//                                                    getString(R.string.by)+" "+advertise.getName(),
//                                                    token);
//                                        }
                                        getImages();
                                    }
                                }).addOnFailureListener(AddCourseActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                Check.hideDialog(progress);
                                Log.e(TAG, "onFailure: "+e.getMessage());
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
//                        Check.hideDialog(progress);
                    }
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                    Toast.makeText(getApplicationContext(), getString(R.string.upload_is)+" "+ Integer.valueOf((int) progress) + "% "+getString(R.string.done), Toast.LENGTH_SHORT).show();
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "onPaused: "+getString(R.string.upload_paused));
                    Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_paused));
//                    Check.hideDialog(progress);
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "onActivityResult: "+e.getMessage());
//            Check.hideDialog(progress);
        }

//        startActivity(new Intent(getApplicationContext(), MyAdsActivity.class));
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//        finish();
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = this.getContentResolver().openInputStream(selectedImage);
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
            uploadTask.addOnCompleteListener(AddCourseActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.ToastMessage(getApplicationContext(), "upload image");
                        Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_image));
                    }else {
//                        Check.ToastMessage(getApplicationContext(), "Error upload image");
                        Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.failed));
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
                        course.setImage(downloadUri.toString());

                        db.collection(KEY.COURSE)
                                .document(documentReference.getId())
                                .set(course).addOnSuccessListener(AddCourseActivity.this,
                                new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                Check.ToastMessage(getApplicationContext(), "Updated FireStore");
                                Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.successful_upload));
                                Tie_title_course.setText("");
                            }
                        }).addOnFailureListener(AddCourseActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.failed));
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
                    Toast.makeText(getApplicationContext(), getString(R.string.upload_is)+" "+ Integer.valueOf((int) progress) + "% "+getString(R.string.done), Toast.LENGTH_SHORT).show();
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

    public void getImages() {
        try {
            Log.d(TAG, "getImages: ");
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
                            Log.d(TAG, "onSuccess, images size : "+get_images.getImages_uri_map().size());
                            Set<Map.Entry<String, String>> value = get_images.getImages_uri_map().entrySet();
                            for (Map.Entry<String, String> entry : value) {
                                Log.d(TAG, "bindAds: get key" + entry.getKey() + "  get Values" + entry.getValue());
                                uri = get_images.getImages_uri_map().get(entry.getKey());
                                uriList.add(uri);
                                Log.d(TAG, "onSuccess: "+uri);
                            }
//                    LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
//                            LinearLayoutManager.VERTICAL, false);
                            GridLayoutManager manager = new GridLayoutManager(AddCourseActivity.this, 2);
                            recyclerView.setLayoutManager(manager);
                            pagerAdapter = new ImageAdapter(get_images);
                            recyclerView.setAdapter(pagerAdapter);
                            Log.d(TAG, "onSuccess: uris: "+uriList.size());
                        }
                    }
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "getImages: error "+e.getMessage());
        }
    }

    private void queryData() {
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

//        Map<String, String> images = new HashMap<>();
//        Set<Map.Entry<String, String>> value = images.entrySet();
//        for (Map.Entry<String, String> entry : value) {
//
//            Log.d(TAG, "bindAds: get key"+entry.getKey() +"  get Values"+ entry.getValue());
//            //uri = Uri.parse(ads.getImages_uri_map().get(entry.getKey()));
//        }

//        final Query query = FirebaseFirestore.getInstance()
//                .collection(KEY.IMAGES);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.IMAGES);
//                .orderBy("timestamp");



        FirestoreRecyclerOptions<Images> options = new FirestoreRecyclerOptions.Builder<Images>()
                .setQuery(query, Images.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Images, helperHolder>(options) {


            @NonNull
            @Override
            public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.image_row, viewGroup, false);
                return new helperHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull Images model) {
                //holder.bindAds(model);
            }

            @Override
            public void onDataChanged() {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Called when there is an error getting a query snapshot. You may want to update
                // your UI to display an error message to the user.
                // ...
                Log.e(TAG, "onError: "+e.getMessage());
                if (FirebaseAuth.getInstance().getUid() != null) {
                    //warningDialog();
                }else {
                    //warningDialogLogin();
                }
            }

        };

        query.addSnapshotListener(AddCourseActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                // Handle errors
                if (e != null) {
                    Log.w(TAG, "onEvent:error", e);
                    return;
                }

                // Dispatch the event
                if (queryDocumentSnapshots.isEmpty()) {
                    if (FirebaseAuth.getInstance().getUid() != null) {
                        //warningDialogPost();
                    }else {
                        //warningDialogLogin();
                    }
                }
//                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
//                    // Snapshot of the changed document
//                    DocumentSnapshot snapshot = change.getDocument();
//
//                    switch (change.getType()) {
//                        case ADDED:
//                            // TODO: handle document added
//                            break;
//                        case MODIFIED:
//                            // TODO: handle document modified
//                            break;
//                        case REMOVED:
//                            // TODO: handle document removed
//                            break;
//                    }
//                }
            }
        });

        recyclerView.setAdapter(adapter);
    }


    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public void uploadImages(View view) {
        if (mStoragePermissions) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        } else {
            verifyStoragePermissions();
        }
    }
}

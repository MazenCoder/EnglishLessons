package com.mobidroid.englishlessons.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.adapter.ImageAdapter;
import com.mobidroid.englishlessons.admin.AddCourseActivity;
import com.mobidroid.englishlessons.item.Check;
import com.mobidroid.englishlessons.item.Images;
import com.mobidroid.englishlessons.item.KEY;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {

    private boolean mStoragePermissions;
    private static final int PICKFILE_REQUEST_CODE = 833;//random number
    private static final String TAG = "ImageFragment";
    private Map<String, String> uri_image = new HashMap<>();

    private List<String> uriList = new ArrayList<>();
    private ImageAdapter pagerAdapter;

    private Images get_images;
    private RecyclerView recyclerView;

    //  firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference = db
            .collection(KEY.COURSE)
            .document();

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_image, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_image);
        Button but_upload_image = (Button) view.findViewById(R.id.but_upload_image);
        but_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });

        getImages();
        return view;
    }

    public void uploadImages() {
        if (Check.mStoragePermissions) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        } else {
            Check.verifyStoragePermissions(getActivity(), TAG, PICKFILE_REQUEST_CODE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICKFILE_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                Log.d(TAG, "onActivityResult: counter is: " + count);
                Uri[] uris = new Uri[count];
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    uris[i] = imageUri;

                }
                uploadImagesToFireStorage(uris);
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
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver() , Uri.parse(uri.toString()));
            Bitmap bitmap_result = rotateImageIfRequired(bitmap, uri);

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

            uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.hideDialog(progress);
                        Log.d(TAG, "onComplete: is successful");
                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.upload_image));
                    }else {
                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
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
                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Updated FireStore");
//                                        Check.hideDialog(progress);
                                        Log.d(TAG, "onSuccess: "+documentReference.getId());
                                        getImages();
                                    }
                                }).addOnFailureListener(getActivity(), new OnFailureListener() {
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
                    Toast.makeText(getContext(), getString(R.string.upload_is)+" "+ Integer.valueOf((int) progress) + "% "+getString(R.string.done), Toast.LENGTH_SHORT).show();
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "onPaused: "+getString(R.string.upload_paused));
                    Check.MessageSnackBarShort(getView().findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_paused));
//                    Check.hideDialog(progress);
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "onActivityResult: "+e.getMessage());
//            Check.hideDialog(progress);
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

                            GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
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

}

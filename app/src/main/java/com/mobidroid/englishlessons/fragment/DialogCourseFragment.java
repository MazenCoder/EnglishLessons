package com.mobidroid.englishlessons.fragment;


import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobidroid.englishlessons.R;
import com.mobidroid.englishlessons.item.Check;
import com.mobidroid.englishlessons.item.Course;
import com.mobidroid.englishlessons.item.KEY;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogCourseFragment extends DialogFragment {

    private TextInputLayout Til_dialog_course;
    private TextInputEditText Tie_dialog_course;
    private ImageView image_dialog_course;
    private Button but_dialog_delete, but_dialog_save;
    private static final String TAG = "DialogCourseFragment";

    private Course course;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DialogCourseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_course, container, false);

        Til_dialog_course = (TextInputLayout) view.findViewById(R.id.til_dialog_course);
        Tie_dialog_course = (TextInputEditText) view.findViewById(R.id.tie_dialog_course);

        image_dialog_course = (ImageView) view.findViewById(R.id.image_dialog_course);

        but_dialog_delete   = (Button) view.findViewById(R.id.but_dialog_course_delete);
        but_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        but_dialog_save     = (Button) view.findViewById(R.id.but_dialog_course_update);
        but_dialog_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCourse();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                course = (Course) bundle.getSerializable(KEY.COURSE);
                Tie_dialog_course.setText(course.getTitle());
                Picasso.get().load(course.getImage())
                        .fit()
                        .centerInside()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(image_dialog_course);
            }catch (Exception e) {
                Log.e(TAG, "onCreateView: "+e.getMessage());
            }
        } else {
            course = new Course();
        }
        return view;
    }

    private void alertDialog() {
        try {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            deleteCourse();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }catch (Exception e) {
            Log.e(TAG, "onClick: error "+e.getMessage());
        }
    }

    private void deleteCourse() {
        if (course.getId() != null) {
            try {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference photoRef = storage.getReferenceFromUrl(course.getImage());
                photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            db.collection(KEY.COURSE).document(course.getId())
                                    .delete()
                                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.successful_upload));
                                            dismiss();
                                        }
                                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                                }
                            });

                        }else {
                            Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                        }
                    }
                });

            }catch (Exception e) {
                Log.e(TAG, "modify: "+e.getMessage());
            }
        }
    }

    private void updateCourse() {
        if (course.getId() != null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("title", Tie_dialog_course.getText().toString());
            try {
                db.collection(KEY.COURSE).document(course.getId())
                        .update(hashMap)
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                            Check.ToastMessage(getActivity(), "Updated FireStore");
                                Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.successful_upload));
                                dismiss();
                            }
                        }).addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Check.MessageSnackBarShort(getActivity().findViewById(R.id.coordinator_admin), getString(R.string.failed));
                    }
                });

            }catch (Exception e) {
                Log.e(TAG, "modify: "+e.getMessage());
            }
        }
    }

}

package com.mobidroid.englishlessons.item;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.mobidroid.englishlessons.admin.AddCourseActivity;

public class Check {

    public static void MessageSnackBarShort(View viewById, String msg) {
        Snackbar.make(viewById, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static boolean mStoragePermissions;

    public static void verifyStoragePermissions(Activity context, String TAG, int REQUEST_CODE) {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(context,
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context,
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context,
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    context, permissions, REQUEST_CODE);
        }
    }
}

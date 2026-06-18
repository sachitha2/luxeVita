package com.example.ecostay.util;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecostay.R;
import com.google.android.material.appbar.MaterialToolbar;

public final class ToolbarUtils {

    private ToolbarUtils() {
    }

    public static void setupBackToolbar(AppCompatActivity activity, @StringRes int titleRes) {
        setupBackToolbar(activity, activity.getString(titleRes));
    }

    public static void setupBackToolbar(AppCompatActivity activity, CharSequence title) {
        MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
    }
}

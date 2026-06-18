package com.example.ecostay.util;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.ecostay.R;

public final class StatusUiUtils {

    private StatusUiUtils() {
    }

    public static void applyStatusChip(TextView textView, String status) {
        ChipStyle chipStyle = resolveChipStyle(status);
        textView.setText(status);
        textView.setBackgroundResource(chipStyle.backgroundRes);
        textView.setTextColor(ContextCompat.getColor(textView.getContext(), chipStyle.textColorRes));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        float density = textView.getResources().getDisplayMetrics().density;
        int padH = (int) (10 * density);
        int padV = (int) (4 * density);
        textView.setPadding(padH, padV, padH, padV);
    }

    private static ChipStyle resolveChipStyle(String status) {
        if (status == null) {
            return new ChipStyle(R.drawable.bg_chip_progress, R.color.chip_progress_text);
        }
        String normalized = status.trim().toLowerCase();
        if (normalized.contains("cancel")) {
            return new ChipStyle(R.drawable.bg_chip_cancelled, R.color.chip_cancelled_text);
        }
        if (normalized.contains("complete")) {
            return new ChipStyle(R.drawable.bg_chip_success, R.color.chip_success_text);
        }
        if (normalized.contains("pending")
                || normalized.equals("received")
                || normalized.contains("assign")) {
            return new ChipStyle(R.drawable.bg_chip_pending, R.color.chip_pending_text);
        }
        return new ChipStyle(R.drawable.bg_chip_progress, R.color.chip_progress_text);
    }

    private static final class ChipStyle {
        @DrawableRes final int backgroundRes;
        @ColorRes final int textColorRes;

        ChipStyle(@DrawableRes int backgroundRes, @ColorRes int textColorRes) {
            this.backgroundRes = backgroundRes;
            this.textColorRes = textColorRes;
        }
    }
}

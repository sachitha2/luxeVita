package com.example.ecostay.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class PhotoUtils {

    private PhotoUtils() {
    }

    public static String savePhotoFromUri(Context context, Uri uri) throws IOException {
        return savePhotoFromUri(context, uri, "repair_photos", "repair_" + System.currentTimeMillis() + ".jpg");
    }

    public static String saveProfilePhotoFromUri(Context context, int userId, Uri uri) throws IOException {
        return savePhotoFromUri(context, uri, "profile_photos", "profile_" + userId + ".jpg");
    }

    public static String saveFaqImageFromUri(Context context, int faqId, Uri uri) throws IOException {
        String fileName = faqId > 0
                ? "faq_" + faqId + ".jpg"
                : "faq_" + System.currentTimeMillis() + ".jpg";
        return savePhotoFromUri(context, uri, "faq_images", fileName);
    }

    public static String saveTipImageFromUri(Context context, int tipId, Uri uri) throws IOException {
        String fileName = tipId > 0
                ? "tip_" + tipId + ".jpg"
                : "tip_" + System.currentTimeMillis() + ".jpg";
        return savePhotoFromUri(context, uri, "tip_images", fileName);
    }

    public static String saveDeviceImageFromUri(Context context, int deviceId, Uri uri) throws IOException {
        String fileName = deviceId > 0
                ? "device_" + deviceId + ".jpg"
                : "device_" + System.currentTimeMillis() + ".jpg";
        return savePhotoFromUri(context, uri, "device_images", fileName);
    }

    public static void bindImage(ImageView imageView, String path) {
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                return;
            }
        }
        imageView.setVisibility(View.GONE);
        imageView.setImageDrawable(null);
    }

    private static String savePhotoFromUri(Context context, Uri uri, String folderName,
                                           String fileName) throws IOException {
        File directory = new File(context.getFilesDir(), folderName);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create photo directory");
        }

        File output = new File(directory, fileName);
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(output)) {
            if (input == null) {
                throw new IOException("Unable to read selected image");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        return output.getAbsolutePath();
    }

    public static void deletePhotoFile(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}

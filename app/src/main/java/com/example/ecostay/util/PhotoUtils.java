package com.example.ecostay.util;

import android.content.Context;
import android.net.Uri;

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

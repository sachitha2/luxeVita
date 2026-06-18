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
        File directory = new File(context.getFilesDir(), "repair_photos");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create photo directory");
        }

        File output = new File(directory, "repair_" + System.currentTimeMillis() + ".jpg");
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
}

package com.stho.mobicompass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Required additional actions to allow external applications to access the PDF file:
 *
 * 1. Create a filepath.xml resource
 *         <?xml version="1.0" encoding="utf-8"?>
 *         <paths>
 *             <external-path
 *                 name="external_files"
 *                 path="." />
 *         </paths>
 *
 * 2. Setup a provider-section referencing the filepath in AndroidManifest.xml
 *         <provider
 *             android:name="androidx.core.content.FileProvider"
 *             android:authorities="${applicationId}.provider"
 *             android:exported="false"
 *             android:grantUriPermissions="true">
 *             <meta-data
 *                 android:name="android.support.FILE_PROVIDER_PATHS"
 *                 android:resource="@xml/filepaths" />
 *         </provider>
 */
class DocumentationFileAdapter {

    private final Context context;

    DocumentationFileAdapter(Context context) {
        this.context = context;
    }

    @SuppressWarnings("SameParameterValue")
    void openPdf(String fileName) {
        try {
            File file = loadFile(fileName, true);
            String authority = BuildConfig.APPLICATION_ID + ".provider";
            Uri uri = FileProvider.getUriForFile(context, authority, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.d("PDF", ex.toString());
        }
    }

    void openLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(intent);
    }

    @SuppressWarnings({"SameParameterValue", "ResultOfMethodCallIgnored"})
    private File loadFile(String fileName, boolean overwrite) throws IOException {
        File file = new File(getRootDir(), fileName);
        if (file.exists() && overwrite) {
            file.delete();
        }
        if (!file.exists()) {
            createFileFromAssets(context, file);
        }
        return file;
    }

    private File getRootDir() {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    private static void createFileFromAssets(Context context, File file) throws IOException {
        InputStream asset = context.getAssets().open(file.getName());
        FileOutputStream outputStream = new FileOutputStream(file);
        int size = asset.available();
        byte[] buffer = new byte[size];
        int bytesRead = asset.read(buffer);
        asset.close();
        outputStream.write(buffer, 0, bytesRead);
        outputStream.close();
    }
}


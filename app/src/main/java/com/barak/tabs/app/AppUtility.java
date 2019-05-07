package com.barak.tabs.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Barak Halevi on 24/11/2018.
 */
public class AppUtility {



    public static final String DOWNLOAD_SERVICE_FILE_NAME = "rohksin.com.olaplay.Utility.DOWNLOAD_SERVICE_FILE_NAME";
    public static final String DOWNLOAD_SERVICE_URL = "rohksin.com.olaplay.Utility.DOWNLOAD_SERVICE_FILE_NAME.";

    public static final String FOLDER_NAME ="StreamingTora";



    public static void downLoadSongToexternalStorage(Context context, String url, String fileName)
    {

        Intent intent  = new Intent(context, DownloadToExtStrService.class);
        intent.putExtra(DOWNLOAD_SERVICE_URL, url);
        intent.putExtra(DOWNLOAD_SERVICE_FILE_NAME, fileName+".mp3");
        context.startService(intent);
    }




    public static void shareDownloadedSong(Context context,File file)
    {

        Uri uri = Uri.parse(file.getPath());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/mp3");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(share, "Share Sound File"));

    }


    public static File getMainExternalFolder()
    {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), FOLDER_NAME);
        if (!file.mkdirs()) {
            Log.e("Directory not created", "Directory not created");
        }
        return file;
    }


    public static boolean downloadFile(String sUrl, String fileName)
    {
        InputStream input = null;
        OutputStream output = null;
        if (!isExternalStorageWritable()){
            return false;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("Server returned HTTP",connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            int fileLength = connection.getContentLength();

            // goDownload the file
            input = connection.getInputStream();
            fileName = fileName.replace("\u0000","");
            File musicFile = new File(getMainExternalFolder(),fileName );
            output = new FileOutputStream(musicFile);

            byte data[] = new byte[4096];
            long total = 0;
            int count;

            Log.d("TOTAL SIZE",sUrl);

            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                /*
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                */
                total += count;

                Log.d("File size",total+"");
                // publishing the progress....
                //  if (fileLength > 0) // only if total length is known
                // publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {

            e.toString();
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return true;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
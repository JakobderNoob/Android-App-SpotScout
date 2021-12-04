package prime.mapapp.mapsappv2;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

class UpdateAppClass extends AsyncTask<String, String, String> {

    protected String doInBackground(String... f_url) {

        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            System.out.println("Jetzt bin ich hier");
            System.out.println(Environment.getExternalStorageDirectory().toString());

            try{
                File file = new File(Environment
                        .getExternalStorageDirectory().toString()
                        + "/Download/OpenThisFileToUpdate.apk");
                if(file.exists()){
                    boolean result = file.delete();
                    System.out.println("Application able to delete the file and result is: " + result);
                    file.delete();
                }else{
                    System.out.println("Application doesn't able to delete the file");
                }
            }catch (Exception e){
                Log.e("App", "Exception while deleting file " + e.getMessage());
            }
            String folder_main = "/Download/";

            File f = new File(Environment.getExternalStorageDirectory(), folder_main);
            if (!f.exists()) {
                f.mkdirs();
            }
            OutputStream output = new FileOutputStream(Environment
                    .getExternalStorageDirectory().toString()
                    + "/Download/OpenThisFileToUpdate.apk");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            System.out.println("Fertig!");


        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        MainActivity.readinstall = true;
        return null;
    }
}
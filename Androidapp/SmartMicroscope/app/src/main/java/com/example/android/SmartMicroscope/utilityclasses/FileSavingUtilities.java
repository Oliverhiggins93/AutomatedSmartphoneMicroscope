package com.example.android.SmartMicroscope.utilityclasses;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Strings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSavingUtilities {
    String saveDirectory;
    File picturefile;
    String sampleNumber;
    Context context;
    String filecountString;
    public FileSavingUtilities(String samplenumber, String savedirectory, Context thiscontext) {
        saveDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/" + sampleNumber;
        sampleNumber = samplenumber;
        saveDirectory = savedirectory;
        context = thiscontext;
    }

    public File calculateFileName(){
        File file = new File(saveDirectory);
        File[] list = file.listFiles();
        int filecount = 0;
        for (File f : list) {
            String name = f.getName();
            if (name.endsWith(".jpg") || name.endsWith(".png"))
                filecount++;
            System.out.println(filecount);
        }

        filecountString = Strings.padStart(String.valueOf(filecount), 8, '0');
        picturefile = new File(saveDirectory + '/' + sampleNumber + '_' + filecountString + ".jpg");

        if (picturefile == null) {
            return picturefile;
        }
        return picturefile;
    }
    // Upload file to storage and return a path.
    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i("getPath method in FileSavingUtilities", "Failed to upload a file");
        }
        return "";
    }

    public String getFilecountString() {
        return filecountString;
    }
//public void getSampleNumber() {
       // Toast.makeText(context.getApplicationContext(), "Sample Number set: " + sampleNumber, Toast.LENGTH_SHORT).show();
      //  return sampleNumber;
        //sampleNumTextView.setText("Sample Number:" + sampleNumber);
        //saveDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/" + sampleNumber;
        //picturefile = calculateFileName();

   // }

}

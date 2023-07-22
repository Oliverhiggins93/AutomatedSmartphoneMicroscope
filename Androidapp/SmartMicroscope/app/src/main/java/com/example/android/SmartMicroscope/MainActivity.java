package com.example.android.SmartMicroscope;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.android.SmartMicroscope.adapters.SectionsPageAdapter;
import com.google.common.base.Strings;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    Button openCameraButton, viewImagesButton, setSampleNumberButton, inferenceFromGalleryButton, loginButton, uvcCameraButton, preferencesButton, webCameraButton, webCameraButtonOld;
    EditText sampleNumEditText;
    TextView sampleNumTextView;
    public String sampleNumber = "00000000";
    String saveDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/" + sampleNumber;
    String rootDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope";
    String inferenceDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/inference";
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        requestLocationPermission();
        sharedPref = getBaseContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sampleNumber = sharedPref.getString("sampleNumber", "00000000");

        viewImagesButton = (Button) findViewById(R.id.button_mainmenuviewfile);
        setSampleNumberButton = (Button) findViewById(R.id.button_set_sample_number);
        sampleNumTextView = (TextView) findViewById(R.id.textView_sampleNum);
        sampleNumEditText = (EditText) findViewById(R.id.editText_sampleNum);
        //loginButton = (Button) findViewById(R.id.button_login);
        preferencesButton = (Button) findViewById(R.id.button_preferences);
        webCameraButton = (Button) findViewById(R.id.button_webcamera);
        sampleNumTextView.setText(sampleNumber);
        sampleNumEditText.setText(sampleNumber, TextView.BufferType.EDITABLE);
        setSampleNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSampleNumber();

            }
        });


        webCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openWebIntent = new Intent(MainActivity.this, WebCameraActivity.class);
                openWebIntent.putExtra("sampleNumber", sampleNumber); //Optional parameters
                MainActivity.this.startActivity(openWebIntent);

            }
        });


        //loginButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Intent loginOpen = new Intent(MainActivity.this, LoginActivity.class);
        //        //openCameraIntent.putExtra("sampleNumber", sampleNumber); //Optional parameters
        //        MainActivity.this.startActivity(loginOpen);
//
        //    }
        //});

        viewImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent folderBrowseOpen = new Intent(MainActivity.this, FolderBrowseActivity.class);
                //openCameraIntent.putExtra("sampleNumber", sampleNumber); //Optional parameters
                MainActivity.this.startActivity(folderBrowseOpen);

            }
        });
        preferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent preferences = new Intent(MainActivity.this, sharedpreferencesscreen.class);
                //openCameraIntent.putExtra("sampleNumber", sampleNumber); //Optional parameters
                MainActivity.this.startActivity(preferences);

            }
        });

    }


    public void getSampleNumber() {
        sampleNumber = Strings.padStart(sampleNumEditText.getText().toString(), 8, '0');
        Toast.makeText(this, "Sample Number set: " + sampleNumber, Toast.LENGTH_SHORT).show();


        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sampleNumber", sampleNumber);
        editor.commit();

        File rootdir = new File(rootDirectory);
        File inferenceDir = new File(inferenceDirectory);
        try {
            if (rootdir.mkdir()) {
                System.out.println("Sample Directory created");
            } else {
                System.out.println("Sample Directory exists or could not be created");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        try {
            if (inferenceDir.mkdir()) {
                System.out.println("Inference Directory created");
            } else {
                System.out.println("Inference Directory exists or could not be created");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }


        saveDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM  + "/smartmicroscope/" + sampleNumber;
        File dir = new File(saveDirectory);
        try {
            if (dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sampleNumTextView.setText("Sample Number: " + sampleNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

}

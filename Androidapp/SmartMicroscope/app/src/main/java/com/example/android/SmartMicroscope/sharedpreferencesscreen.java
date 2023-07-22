package com.example.android.SmartMicroscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URI;

public class sharedpreferencesscreen extends AppCompatActivity {
    public Button selectModel,openModelZoo, selectLabel, resetDefaults;
    TextView selectedModelTV, selectedLabelTV;
    EditText numberofstepsET, numberofimagesET, numberofimagesautofocusET;
    Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private static final int PICK_MODEL_FILE = 100;
    private static final int PICK_LABEL_FILE = 200;
    Uri selectedModelUri, selectedLabelUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharedpreferencesscreen);
        selectModel = (Button) findViewById(R.id.button_selectmodel);
        selectLabel = (Button) findViewById(R.id.button_selectlabel);
        openModelZoo = (Button) findViewById(R.id.button_openmodelzoo);
        resetDefaults = (Button) findViewById(R.id.button_resetDefaults);
        selectedModelTV = (TextView) findViewById(R.id.selected_model);
        selectedLabelTV = (TextView) findViewById(R.id.selected_label);
        numberofstepsET = (EditText) findViewById(R.id.stepstoscan_edittext);
        numberofimagesET = (EditText) findViewById(R.id.imagestocaptureperscan_edittext);
        numberofimagesautofocusET = (EditText) findViewById(R.id.imagestocaptureperautofocus);

        context = this;
        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        String modelLocation = sharedPref.getString("modelLocation", "No Model Selected");
        String labelLocation = sharedPref.getString("modelLabelFile", "No Label Selected");
        int numberStepsToScan = sharedPref.getInt("numberStepsToScan", 500);
        int numberImagesToScan = sharedPref.getInt("numberImagesToScan", 32);
        int numberImagesPerAutofocus = sharedPref.getInt("numberImagesPerAutofocus", 2);

        selectedModelTV.setText(modelLocation);
        selectedLabelTV.setText(labelLocation);
        numberofstepsET.setText(String.valueOf(numberStepsToScan));
        numberofimagesET.setText(String.valueOf(numberImagesToScan));
        numberofimagesautofocusET.setText(String.valueOf(numberImagesPerAutofocus));

        selectModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_LONG);

                openFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI(), PICK_MODEL_FILE);
            }
        });

        selectLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_LONG);

                openFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI(), PICK_LABEL_FILE);
            }
        });

        numberofstepsET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putInt("numberStepsToScan", Integer.parseInt(numberofstepsET.getText().toString()));
                editor.commit();
            }
        });

        numberofimagesET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(numberofimagesET.getText().toString().matches("")){
                    numberofimagesET.setText("32");
                }
                else{
                    editor.putInt("numberImagesToScan", Integer.parseInt(numberofimagesET.getText().toString()));
                    editor.commit();
                }

            }
        });

        resetDefaults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear().commit();
            }
        });

        numberofimagesautofocusET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(numberofimagesET.getText().toString().matches("")){
                    numberofimagesET.setText("2");
                }
                else{
                    editor.putInt("numberImagesPerAutofocus", Integer.parseInt(numberofimagesautofocusET.getText().toString()));
                    editor.commit();
                }

            }
        });

    }

    private void openFile(URI pickerInitialUri, int REQUEST_CODE) {
        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
       // intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("file/*");
        intent.putExtra("CONTENT_TYPE", "*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MODEL_FILE && resultCode == RESULT_OK && null != data) {

            selectedModelUri = data.getData();
            //String path = selectedModelUri.toString();
            String path = getRealPathFromURI(selectedModelUri);

            editor.putString("modelLocation", path);
            editor.commit();
            selectedModelTV.setText(path);
            //String[] filePathColumn = { MediaStore.Images.Media.DATA };
            //Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            //cursor.moveToFirst();
            //int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //String picturePath = cursor.getString(columnIndex);
            //cursor.close();
        }
        if (requestCode == PICK_LABEL_FILE && resultCode == RESULT_OK && null != data) {

            selectedLabelUri = data.getData();
            //String path = selectedLabelUri.toString();
            //File file = new File(selectedLabelUri.getPath());
            //String path = file.getAbsolutePath();
            String path = getRealPathFromURI(selectedLabelUri);
            editor.putString("modelLabelFile", path);
            editor.commit();
            selectedLabelTV.setText(path);
            //String[] filePathColumn = { MediaStore.Images.Media.DATA };
            //Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            //cursor.moveToFirst();
            //int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            //String picturePath = cursor.getString(columnIndex);
            //cursor.close();
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
package com.example.android.SmartMicroscope;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.SmartMicroscope.adapters.FolderListAdapter;
import com.example.android.SmartMicroscope.adapters.FolderListItem;
import com.example.android.SmartMicroscope.adapters.SQLDatabaseAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class FolderBrowseActivity extends AppCompatActivity {
    String rootDirectory;
    //ArrayList<String> sampleNumbers = new ArrayList<>();
    ArrayList<FolderListItem> sampleNumbers = new ArrayList<>();
    File[] list;
    private FirebaseAuth mAuth;
    private Toolbar mTopToolbar;
    private Handler progressBarHandler = new Handler();
    ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_browse);
        ListView folderList = (ListView) findViewById(R.id.folderBrowseList);

        rootDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope";
        createFileListSimple();
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        //ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, sampleNumbers);
        //
        FolderListAdapter adapter = new FolderListAdapter(this, R.layout.list_folder_layout, sampleNumbers);
        folderList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.folder_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_firebasesync) {
            //syncFirebase();
            new syncFirebaseAsync().execute();
            return true;
        }
        if (id == R.id.menu_filesystemsync) {
            //syncFirebase();
            syncFilesystem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void syncFilesystem(){
        SQLDatabaseAdapter sqlAdapter = new SQLDatabaseAdapter(this);
        sqlAdapter.syncFilesystem();
        finish();
        startActivity(getIntent());
    }

    private class syncFirebaseAsync extends AsyncTask<Void,Void,Void> {
        private int progressStatus=0;
        private Handler handler = new Handler();

        // Initialize a new instance of progress dialog
        private ProgressDialog pd = new ProgressDialog(FolderBrowseActivity.this);

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pd.setIndeterminate(false);

            // Set progress style horizontal
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            // Set the progress dialog background color
            //pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));

            // Make the progress dialog cancellable
            pd.setCancelable(true);
            // Set the maximum value of progress
            pd.setMax(100);
            // Finally, show the progress dialog
            pd.show();
        }

        @Override
        protected Void doInBackground(Void...args){
            // Set the progress status zero on each button click
            progressStatus = 0;
            mAuth = FirebaseAuth.getInstance();
            int Foldercount = 0;
            int filecount= 0;

            // Start the lengthy operation in a background thread
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (File f : list) {
                        //progresscount = progresscount + 1;

                        File sampleFolder = new File(String.valueOf(f));
                        File[] images = sampleFolder.listFiles();
                        for (File image : images)
                        {
                            // Update the progress bar
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pd.setMessage("Uploading file" + image + " of " + String.valueOf(images.length) + "from" + String.valueOf(f));
                                    // Update the progress status
                                    pd.setProgress(progressStatus);
                                    // If task execution completed
                                    if(progressStatus == list.length){
                                        // Dismiss/hide the progress dialog
                                        pd.dismiss();
                                    }
                                }
                            });


                            Uri imageUri = Uri.fromFile(image);
                            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getEmail()).child(sampleFolder.toString().split("/")[sampleFolder.toString().split("/").length - 1]).child( image.getName().split("/")[f.getName().split("/").length -1]);
                            fileRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String url = uri.toString();
                                            Log.d("DownloadUrl", url);
                                        }
                                    });
                                    fileRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("Uploading failed", e.toString());
                                        }
                                    });
                                }
                            });

                        }
                        progressStatus = progressStatus + 1;
                    };


                }
            }).start(); // Start the operation

            return null;
        }

        protected void onPostExecute(){
            // do something after async task completed.
        }
    }

    private void syncFirebase(){
        mAuth = FirebaseAuth.getInstance();
        progressBar = new ProgressDialog(this);
        int progresscount = 0;
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();


        for (File f : list) {
            //progresscount = progresscount + 1;

            File sampleFolder = new File(String.valueOf(f));
            File[] images = sampleFolder.listFiles();

            progressBar.show();
            for (File image : images)
            {
                progressBar.setMessage("Uploading file" + image + " of " + String.valueOf(images.length) + "from" + String.valueOf(f));

                Uri imageUri = Uri.fromFile(image);
                StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getEmail()).child(sampleFolder.toString().split("/")[sampleFolder.toString().split("/").length - 1]).child( image.getName().split("/")[f.getName().split("/").length -1] + getFileExtension(imageUri));
                fileRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                Log.d("DownloadUrl", url);
                            }
                        });
                        fileRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Uploading failed", e.toString());
                            }
                        });
                    }
                });

            }
        };
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void createFileList() {
        ArrayList<String> folderNumbersList = new ArrayList<>();
        SQLDatabaseAdapter sql = new SQLDatabaseAdapter(this);
        ArrayList<Map<String, String>> foldersMap = sql.getFolderDataSimple();

        for (int i=0; i <= foldersMap.size(); i=i+1) {
            try {
                String sampleNumber = foldersMap.get(i).get("SampleNumber");
                String formattedDate = foldersMap.get(i).get("DateCreated");
                String LocationCreated = foldersMap.get(i).get("LocationCreated");
                String NumberImages = foldersMap.get(i).get("NumberImages");
                String PositivityResult = foldersMap.get(i).get("PositivityResult");
                String SampleType = foldersMap.get(i).get("SampleType");


                if (!folderNumbersList.contains(sampleNumber)){
                    folderNumbersList.add(sampleNumber);
                    FolderListItem folderListItem = new FolderListItem(sampleNumber, formattedDate, LocationCreated, String.valueOf(NumberImages), PositivityResult, SampleType);
                    sampleNumbers.add(folderListItem);
                }
                }
            catch (Exception e) {
                FolderListItem folderListItem = new FolderListItem("0000000", "01/01/1990", "nolocation", String.valueOf(1), "No result", "No Sample Type");
            }
        }
    }

    public void createFileListSimple() {
        ArrayList<String> folderNumbersList = new ArrayList<>();
        SQLDatabaseAdapter sql = new SQLDatabaseAdapter(this);
        ArrayList<Map<String, String>> foldersMap = sql.getFolderDataSimple();

        for (int i=0; i <= foldersMap.size(); i=i+1) {
            try {
                String sampleNumber = foldersMap.get(i).get("SampleNumber");
                String formattedDate = foldersMap.get(i).get("DateCreated");
                String LocationCreated = foldersMap.get(i).get("LocationCreated");
                String NumberImages = foldersMap.get(i).get("NumberImages");
                String PositivityResult = foldersMap.get(i).get("PositivityResult");
                String SampleType = foldersMap.get(i).get("SampleType");

                if (!folderNumbersList.contains(sampleNumber)){
                    folderNumbersList.add(sampleNumber);
                    FolderListItem folderListItem = new FolderListItem(sampleNumber, formattedDate, LocationCreated, String.valueOf(NumberImages), PositivityResult, SampleType);
                    sampleNumbers.add(folderListItem);
                }
            }
            catch (Exception e) {
                FolderListItem folderListItem = new FolderListItem("0000000", "01/01/1990", "nolocation", String.valueOf(1), "No result", "No Sample Type");
            }
        }
    }
/*


    public void createFileList_old(){
        File folder = new File(rootDirectory);
        list = folder.listFiles();

            for (File f : list) {
                try {
                    String foldernameStem = f.getName().split("/")[f.getName().split("/").length -1];
                if ( !foldernameStem.equals("inference"))
                {
                String sampleNumber = f.getName();
                Date LastModDate = new Date(f.lastModified());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = dateFormat.format(LastModDate);
                String LocationCreated = "no location";

                File sampleFolder = new File(String.valueOf(f));
                File[] images = sampleFolder.listFiles();
                //int numberimages = 0;

                //for (int i = 0; i < images.length; i++) {
                //    if (!images[i].toString().contains("inference")) {
                //        numberimages++;
                //    }

                //}
                int numberimages = images.length;
                //long numberimages = images.length;

                FolderListItem folderListItem = new FolderListItem(sampleNumber, formattedDate, LocationCreated, String.valueOf(numberimages), "no result", "nosampletype");
                sampleNumbers.add(folderListItem);

                //sampleNumbers.add(name);
                //if (name.endsWith(".jpg") || name.endsWith(".png"))
                //    filecount++;
                //System.out.println(filecount);
            }
            }
                catch (Exception e){
                    FolderListItem folderListItem = new FolderListItem("0000000", "01/01/1990", "nolocation", String.valueOf(1), "no result");
                }
        }


        //picturefile = new File(saveDirectory + '/' + sampleNumber + '_' + filecount + ".jpg");

        //if (picturefile == null) {
        //    return picturefile;
        //}
        //return picturefile;
    }
     */
}
package com.example.android.SmartMicroscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.SmartMicroscope.adapters.FileListAdapterExpandable;
import com.example.android.SmartMicroscope.adapters.FileListItemExpandable;
import com.example.android.SmartMicroscope.adapters.SQLDatabaseAdapter;
import com.example.android.SmartMicroscope.env.BorderedText;
import com.example.android.SmartMicroscope.tflite.Detector;
import com.example.android.SmartMicroscope.tflite.TFLiteObjectDetectionAPIModel;
import com.example.android.SmartMicroscope.tracking.MultiBoxTracker;

import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileBrowseActivityExpandable extends AppCompatActivity {
    String folderDirectory;
    Intent folderIntent;
    List<FileListItemExpandable> eggsFoundList = new ArrayList<>();
    List<FileListItemExpandable> eggsNotFoundList = new ArrayList<>();
    List<FileListItemExpandable> notScannedList = new ArrayList<>();
    List<FileListItemExpandable> emptylist = new ArrayList<>();
    List<FileListItemExpandable> allImagesList = new ArrayList<>();
    final HashMap<String, List<FileListItemExpandable>> lstItemsGroup = new HashMap<>();
    final List<String> lstGroups = new ArrayList<>();
    Button detectEggsButton;
    Context mContext;
    FileListAdapterExpandable adapter;
    public com.example.android.SmartMicroscope.tflite.Detector detector;
    private MultiBoxTracker tracker;
    private BorderedText borderedText;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    Bitmap bitmap;
    TextView testResultTextview, testDateTextview, analysedDateTextview, filesamplenumberTextview, patientHeightTextview, patientWeightTextview, weightOfFecesTextview, eggsPerGramTextview;
    String sampleNumber, patientHeight, patientWeight, sampleType, dateAnalysed, positivityResult, weightOfFeces, eggsPerGram, eggCount;
    SQLDatabaseAdapter sql;
    ArrayList<Map<String, String>> filesMap;
    ProgressBar simpleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browse);
        folderIntent = getIntent();
        mContext = getApplicationContext();
        detectEggsButton = (Button) findViewById(R.id.button_detect_eggs);

        detectEggsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String date;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat;
                dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                date = dateFormat.format(calendar.getTime());

                analysedDateTextview.setText(date);
                detectEggsButton.setClickable(false);
                new Thread() {
                    public void run() {
                        //analyseFolderResnetNoCrop();
                        //analyseFolderResnetNoCropSupport();
                        analyseFolderEfficientDetNoCrop();
                        //analyseFolderEfficientDetTwoCrop();
                    }
                }.start();
                detectEggsButton.setClickable(true);
            }
        });
        simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);
        testResultTextview = (TextView) findViewById(R.id.testresult_textview);
        filesamplenumberTextview = (TextView) findViewById(R.id.filesampleNumberTextview);
        analysedDateTextview = (TextView) findViewById(R.id.dateanalysed_textview);
        patientHeightTextview = (TextView) findViewById(R.id.patientheight_textview);
        patientWeightTextview = (TextView) findViewById(R.id.patientweight_textview);
        weightOfFecesTextview = (TextView) findViewById(R.id.weightfeces_textview);
        eggsPerGramTextview = (TextView) findViewById(R.id.eggspergram_textview);
        ExpandableListView folderList = (ExpandableListView) findViewById(R.id.fileBrowserList);

        lstGroups.add("Egg found");
        lstGroups.add("Egg not found");
        lstGroups.add("Not yet analysed");

        folderDirectory = folderIntent.getStringExtra("folderName");
        String[] splitFilePath = folderDirectory.split("/");
        int filepathlength = splitFilePath.length;
        sampleNumber = splitFilePath[filepathlength - 1];
        filesamplenumberTextview.setText(sampleNumber);
        createFileList();

        adapter = new FileListAdapterExpandable(this, lstGroups, lstItemsGroup);
        folderList.setAdapter(adapter);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        folderList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                FileListItemExpandable itemToAnalyse = adapter.getFileListChild(groupPosition, childPosition);
                String fileLocation = itemToAnalyse.getFilelocation();
                //File imgFile = new  File(fileLocation);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


                Uri fileUri = Uri.fromFile(new File(fileLocation));
                //Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", new  File(fileLocation));

                intent.setDataAndType(fileUri, "image/jpeg");
                startActivity(intent);
                return false;
            }
        });

        simpleProgressBar.setProgress(0);
        simpleProgressBar.setMin(0);
        simpleProgressBar.setMax(100);
        simpleProgressBar.setIndeterminate(false);
    }

    public void createFileList() {
        ArrayList<String> fileList = new ArrayList<>();
        sql = new SQLDatabaseAdapter(this);
        filesMap = sql.getFileData(sampleNumber);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 10;

        for (Map<String, String> map : filesMap) {
            try {
                String formattedDate = map.get("DateCreated");
                String locationCreated = map.get("LocationCreated");
                String latitude = map.get("Latitude");
                String longitude = map.get("Longitude");
                String objectsDetected = map.get("ObjectsDetected");
                String filePath = map.get("FilePath");
                String ImageNumber = map.get("ImageNumber").substring(5);
                String UUID = map.get("UUID");
                patientHeight = map.get("PatientHeight");
                patientWeight = map.get("PatientWeight");
                sampleType = map.get("SampleType");
                dateAnalysed = map.get("DateAnalysed");
                positivityResult = map.get("PositivityResult");
                weightOfFeces = map.get("SampleWeight");
                eggsPerGram = map.get("EggsPerGram");
                eggCount = map.get("EggCount");

                Bitmap imageThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath.toString(), opts), 300, 300);
                FileListItemExpandable fileListItem = new FileListItemExpandable(ImageNumber, formattedDate, locationCreated, objectsDetected, imageThumbnail, filePath, dateAnalysed, UUID);
                //allImagesList.add(fileListItem);
                if (dateAnalysed.equalsIgnoreCase("Not yet analysed")) {
                    notScannedList.add(fileListItem);
                } else if (!dateAnalysed.equalsIgnoreCase("Not yet analysed") && objectsDetected.equalsIgnoreCase("No objects detected.")) {
                    eggsNotFoundList.add(fileListItem);
                } else {
                    eggsFoundList.add(fileListItem);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error occurred while populating", Toast.LENGTH_LONG);
            }
        }
        lstItemsGroup.put(lstGroups.get(0), eggsFoundList);
        lstItemsGroup.put(lstGroups.get(1), eggsNotFoundList);
        lstItemsGroup.put(lstGroups.get(2), notScannedList);

        //After the results are populated we update the positivity result and date analysed with the result of the first one (they should all be the same and we want to avoid doing it multiple times).
        testResultTextview.setText(positivityResult);
        analysedDateTextview.setText(dateAnalysed);
        patientWeightTextview.setText(patientWeight);
        patientHeightTextview.setText(patientHeight);
        weightOfFecesTextview.setText(weightOfFeces);

        //TODO: I still need to sort how the egg counts and egg per gram pull in and out of the database.

        if (!eggsPerGram.equals("") && !eggCount.equals("")) {
            Double eggsPerGramCalc = Integer.valueOf(eggCount) / Double.valueOf(weightOfFeces);
            eggsPerGramTextview.setText(eggsPerGramCalc.toString());
        }

    }

    void moveGroupsNotScanned() {
        for (int i = 0; i < eggsNotFoundList.size(); i++) {
            notScannedList.add(eggsNotFoundList.get(i));
        }
        for (int i = 0; i < eggsFoundList.size(); i++) {
            notScannedList.add(eggsFoundList.get(i));
        }
        eggsNotFoundList.clear();
        eggsFoundList.clear();

    }

    void analyseFolderResnetNoCrop() {

        moveGroupsNotScanned();
        ArrayList<Integer> childrenContainingEggs = new ArrayList<>();
        ArrayList<Integer> childrenContainingNoEggs = new ArrayList<>();
        String UUID;
        com.example.android.SmartMicroscope.customview.OverlayView trackingOverlay;

        final int TF_OD_API_INPUT_SIZE = 1024;
        int imageCropSize = 1024;
        final boolean TF_OD_API_IS_QUANTIZED = false;
        //This number sets a max object size. We know our eggs should not take up more than 100 pixels of our resized 640 x 640 image
        int maximumObjectSize = 4000;
        int minimumObjectSize = 0;
        final float MINIMUM_CONFIDENCE_TF_OD_API = 0.4f;
        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
        ArrayList<Bitmap> inferenceArray = new ArrayList<Bitmap>();
        Bitmap bitmap;
        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String TF_OD_API_MODEL_FILE = "model06052022_metadata_quant3.tflite";
        String labelLocation = sharedPref.getString("modelLabelFile", "schistolabelmap.txt");//"file:///android_asset/schistolabelmap.txt");

        String TF_OD_API_LABELS_FILE = labelLocation;

        moveGroupsNotScanned();
        tracker = new MultiBoxTracker(mContext);
        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, 0);
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                    this,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
        }
        //
        int groupLength = adapter.getChildrenCount(2);
        FileListItemExpandable itemToAnalyse;
        String fileLocation;
        Boolean objectIsDetected = false;
        for (int child = 0; child < groupLength; child++) {

            updateProgress(child, groupLength - 1);

            objectIsDetected = false;
            itemToAnalyse = adapter.getFileListChild(2, child);
            fileLocation = itemToAnalyse.getFilelocation();
            //Toast.makeText(mContext, String.valueOf(child), Toast.LENGTH_SHORT);
            File image = new File(fileLocation);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (height > width) {
                width = height;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true);
            Bitmap drawableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas drawOnBitmap = new Canvas(drawableBitmap);
            String resultstring = "";

            final List<Detector.Recognition> results = detector.recognizeImage(bitmap);
            final Canvas canvas = new Canvas(bitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            paint.setTextSize(10.0f);
            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            final List<Detector.Recognition> mappedRecognitions =
                    new LinkedList<Detector.Recognition>();
            for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                int objectWidth = (int) (location.right - location.left);
                int objectHeight = (int) (location.bottom - location.top);
                //Now we check if our confidence is above a reasonable cut off, and also that our object is the right size

                //objectIsDetected = true;
                //canvas.drawRect(location, paint);
                //result.setLocation(location);
                //mappedRecognitions.add(result);
                //resultstring = resultstring + "\n" + result.getTitle() + ": " + result.getConfidence() + " Left " + String.valueOf(location.left)+ " Right" + String.valueOf(location.right);
                if (location != null && result.getConfidence() >= minimumConfidence && objectWidth < maximumObjectSize && objectHeight < maximumObjectSize && objectWidth > minimumObjectSize && objectHeight > minimumObjectSize) {
                    objectIsDetected = true;
                    canvas.drawRect(location, paint);
                    result.setLocation(location);
                    mappedRecognitions.add(result);
                    resultstring = resultstring + "\n" + result.getTitle() + ": " + result.getConfidence();
                }
            }
            long currTimestamp = 0;
            //itemToAnalyse.setObjectsDetected(resultstring);
            tracker.trackResults(mappedRecognitions, currTimestamp);
            tracker.draw(canvas);
            //draw the new painted patch onto the original image
            drawOnBitmap.drawBitmap(Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true), 0, 0, null);
            //String inferenceFileName = fileLocation.replaceAll(".jpg", "");
            //inferenceFileName = inferenceFileName + "_inference.jpg";

            //this saves the annotated file - disabled for performance
            //String[] inferenceFileNameArray = fileLocation.split("/");
            //String inferenceFileName = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/inference/" + inferenceFileNameArray[inferenceFileNameArray.length - 1];
            //File file = new File(inferenceFileName);
            //if (file.exists()) {
            //    file.delete();
            //}
            //try {
            //    FileOutputStream out = new FileOutputStream(file);
            //    drawableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            //    out.flush();
            //    ;
            //    out.close();
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            //} catch (Exception e) {
            //    e.printStackTrace();
            //    itemToAnalyse.setObjectsDetected("No objects detected.");
            //    sql.updateImageData(itemToAnalyse.getUUID(), "No objects detected.", Calendar.getInstance().getTime().toString());
            //Toast.makeText( mContext, e.toString(), Toast.LENGTH_SHORT).show();
            //}

            if (objectIsDetected == true) {
                //if this is above minimum confidence add it to the list
                if (childrenContainingEggs.contains(child) == false) {
                    childrenContainingEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected(resultstring);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        testResultTextview.setText("Positive");
                        testResultTextview.setTextColor(Color.rgb(240, 0, 0));
                        //itemToAnalyse.setImageThumbnail(drawableBitmap);
                        //itemToAnalyse.setImageThumbnail(ThumbnailUtils.extractThumbnail(drawableBitmap, (int) width / 10,(int) height / 10));
                    }
                });

                sql.updateImageData(itemToAnalyse.getUUID(), resultstring, Calendar.getInstance().getTime().toString());
                sql.updateSamplePositivityResult(sampleNumber, "Positive");
            } else {
                if (childrenContainingNoEggs.contains(child) == false) {
                    childrenContainingNoEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected("No objects detected.");
                sql.updateImageData(itemToAnalyse.getUUID(), "No objects detected.", Calendar.getInstance().getTime().toString());
                //sql.updateSamplePositivityResult(sampleNumber, "Negative");
            }
        }
        //Here's where I am changing updating the lists by setting the childrenContainingEggs and childrenContainingNoEggs as my data for the drawers
        adapter.moveChildBetweenGroup(2, childrenContainingEggs, 0);
        adapter.moveChildBetweenGroup(2, childrenContainingNoEggs, 1);
        lstItemsGroup.replace(lstGroups.get(2), notScannedList, emptylist);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (childrenContainingEggs.size() == 0) {
                    testResultTextview.setText("Negative");
                    testResultTextview.setTextColor(Color.rgb(0, 0, 240));
                    sql.updateSamplePositivityResult(sampleNumber, "Negative");
                }
                Double eggsPerGramCalc = eggsFoundList.size() / Double.valueOf(weightOfFeces);
                eggsPerGramTextview.setText(eggsPerGramCalc.toString());
                detectEggsButton.setText("Detection complete");
            }
        });
    }
    void analyseFolderResnetNoCropSupport() {
        int maximumObjectSizeSchisto = 550;
        int minimumObjectSizeSchisto = 150;
        int maximumObjectSizeOther = 270;
        int minimumObjectSizeOther = 20;
        moveGroupsNotScanned();
        ArrayList<Integer> childrenContainingEggs = new ArrayList<>();
        ArrayList<Integer> childrenContainingNoEggs = new ArrayList<>();
        String UUID;
        com.example.android.SmartMicroscope.customview.OverlayView trackingOverlay;
        // Configuration values for the prepackaged SSD model.
        final int TF_OD_API_INPUT_SIZE = 1024; //SSDresnet is 640 x 640
        int imageCropSize = 1024;
        final boolean TF_OD_API_IS_QUANTIZED = false;
        int maximumObjectSize = 4000;
        int minimumObjectSize = 0;
        final float MINIMUM_CONFIDENCE_TF_OD_API = 0.01f;
        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
        ArrayList<Bitmap> inferenceArray = new ArrayList<Bitmap>();
        Bitmap bitmap;
        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String TF_OD_API_MODEL_FILE = "model06052022_metadata_quant3.tflite";
        String labelLocation = sharedPref.getString("modelLabelFile", "schistolabelmap.txt");


        String TF_OD_API_LABELS_FILE = labelLocation;

        moveGroupsNotScanned();
        tracker = new MultiBoxTracker(mContext);
        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, 0);
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);
        //
        int groupLength = adapter.getChildrenCount(2);
        FileListItemExpandable itemToAnalyse;
        String fileLocation;
        Boolean objectIsDetected = false;
        for (int child = 0; child < groupLength; child++) {

            updateProgress(child, groupLength - 1);

            objectIsDetected = false;
            itemToAnalyse = adapter.getFileListChild(2, child);
            fileLocation = itemToAnalyse.getFilelocation();
            //Toast.makeText(mContext, String.valueOf(child), Toast.LENGTH_SHORT);
            File image = new File(fileLocation);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (height > width) {
                width = height;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true);

            Bitmap drawableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas drawOnBitmap = new Canvas(drawableBitmap);
            String resultstring = "";

            //try {
            try{
                //ImageProcessor imageProcessor = new ImageProcessor.Builder().add(new NormalizeOp((float)127.5, (float)127.5)).build();
                ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setScoreThreshold(MINIMUM_CONFIDENCE_TF_OD_API).setMaxResults(10).build();
                ObjectDetector objectDetector = ObjectDetector.createFromFileAndOptions(mContext, TF_OD_API_MODEL_FILE, options);
                final List<Detector.Recognition> mappedRecognitions = new LinkedList<Detector.Recognition>();
                TensorImage imageTensor = TensorImage.fromBitmap(bitmap);
                //imageTensor = imageProcessor.process(imageTensor);
                List<Detection> results = objectDetector.detect(imageTensor);




            //final List<Detector.Recognition> results = detector.recognizeImage(bitmap);
            final Canvas canvas = new Canvas(bitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            paint.setTextSize(10.0f);
            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            //final List<Detector.Recognition> mappedRecognitions =
              //    new LinkedList<Detector.Recognition>();

            for (final Detection result : results) {
                final RectF location = result.getBoundingBox();
                int objectWidth = (int) (location.right - location.left);
                int objectHeight = (int) (location.bottom - location.top);
                Category detectedObjectClass = result.getCategories().get(0);
                String detectedObjectClassString = detectedObjectClass.getLabel().toString();

                if (detectedObjectClassString.contains("s.mansoni_egg") && objectWidth < maximumObjectSizeSchisto && objectHeight < maximumObjectSizeSchisto && objectWidth > minimumObjectSizeSchisto && objectHeight > minimumObjectSizeSchisto) {
                    objectIsDetected = true;
                    drawOnBitmap.drawRect(location, paint);
                    mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), location));
                    resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n";
                }
                else if (!detectedObjectClassString.contains("s.mansoni_egg")  && objectWidth < maximumObjectSizeOther && objectHeight < maximumObjectSizeOther && objectWidth > minimumObjectSizeOther && objectHeight > minimumObjectSizeOther) {
                    objectIsDetected = true;
                    drawOnBitmap.drawRect(location, paint);
                    mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), location));
                    resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n" + "Height: " + String.valueOf(objectHeight) + "\n" + "Width: " + String.valueOf(objectWidth);
                }
            }
            long currTimestamp = 0;
            //itemToAnalyse.setObjectsDetected(resultstring);
            tracker.trackResults(mappedRecognitions, currTimestamp);
            tracker.draw(canvas);
            //draw the detections onto the original image
            drawOnBitmap.drawBitmap(Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true), 0, 0, null);
            }
            catch(Exception e){

            }
            if (objectIsDetected == true) {
                //if this is above minimum confidence add it to the list
                if (childrenContainingEggs.contains(child) == false) {
                    childrenContainingEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected(resultstring);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        testResultTextview.setText("Positive");
                        testResultTextview.setTextColor(Color.rgb(240, 0, 0));
                        //itemToAnalyse.setImageThumbnail(drawableBitmap);
                        //itemToAnalyse.setImageThumbnail(ThumbnailUtils.extractThumbnail(drawableBitmap, (int) width / 10,(int) height / 10));
                    }
                });

                sql.updateImageData(itemToAnalyse.getUUID(), resultstring, Calendar.getInstance().getTime().toString());
                sql.updateSamplePositivityResult(sampleNumber, "Positive");
            } else {
                if (childrenContainingNoEggs.contains(child) == false) {
                    childrenContainingNoEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected("No objects detected.");
                sql.updateImageData(itemToAnalyse.getUUID(), "No objects detected.", Calendar.getInstance().getTime().toString());
                //sql.updateSamplePositivityResult(sampleNumber, "Negative");
            }
        }
        //Here's where I am changing updating the lists by setting the childrenContainingEggs and childrenContainingNoEggs as my data for the drawers
        adapter.moveChildBetweenGroup(2, childrenContainingEggs, 0);
        adapter.moveChildBetweenGroup(2, childrenContainingNoEggs, 1);
        lstItemsGroup.replace(lstGroups.get(2), notScannedList, emptylist);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (childrenContainingEggs.size() == 0) {
                    testResultTextview.setText("Negative");
                    testResultTextview.setTextColor(Color.rgb(0, 0, 240));
                    sql.updateSamplePositivityResult(sampleNumber, "Negative");
                }
                Double eggsPerGramCalc = eggsFoundList.size() / Double.valueOf(weightOfFeces);
                eggsPerGramTextview.setText(eggsPerGramCalc.toString());
                detectEggsButton.setText("Detection complete");
            }
        });
    }

    void analyseFolderEfficientDetNoCrop(){

    moveGroupsNotScanned();
    ArrayList<Integer> childrenContainingEggs = new ArrayList<>();
    ArrayList<Integer> childrenContainingNoEggs = new ArrayList<>();
    String UUID;
    com.example.android.SmartMicroscope.customview.OverlayView trackingOverlay;
    // Configuration values for the prepackaged SSD model.
    final int TF_OD_API_INPUT_SIZE = 640; //SSDresnet is 640 x 640
    int imageCropSize = 640;
    final boolean TF_OD_API_IS_QUANTIZED = false;
    //This number sets a max object size. We know our eggs should not take up more than 100 pixels of our resized 640 x 640 image
    //Hardcoding this is not a great way to do this, but it will do for now
    //The integer here means the maximum number of pixels an object can take up in either height or width, in the final image fed into the model
    //i.e 200 pixels of a 640 x 640 image is around 1/3 of the width and height of the image (1/9th of surface area)
    int maximumObjectSizeSchisto = 250;
    int minimumObjectSizeSchisto = 40;
    int maximumObjectSizeOther = 250;
    int minimumObjectSizeOther = 40;

    final float MINIMUM_CONFIDENCE_TF_OD_API = 0.2f;
    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
    ArrayList<Bitmap> inferenceArray = new ArrayList<Bitmap>();
    Bitmap bitmap;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

    String TF_OD_API_MODEL_FILE = "efficientdet.tflite";
    String labelLocation = sharedPref.getString("modelLabelFile", "labels.txt");

    String TF_OD_API_LABELS_FILE = labelLocation;

    moveGroupsNotScanned();
    tracker = new MultiBoxTracker(mContext);
    tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, 0);
    final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    final Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(2.0f);
    paint.setTextSize(10.0f);

    try{
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setScoreThreshold(MINIMUM_CONFIDENCE_TF_OD_API).setMaxResults(10).build();
        ObjectDetector objectDetector = ObjectDetector.createFromFileAndOptions(mContext, TF_OD_API_MODEL_FILE, options);
        int groupLength = adapter.getChildrenCount(2);
        FileListItemExpandable itemToAnalyse;
        String fileLocation;
        Boolean objectIsDetected = false;
        for(int child = 0; child<groupLength; child++){

            updateProgress(child, groupLength-1);

            objectIsDetected = false;
            itemToAnalyse = adapter.getFileListChild(2, child);
            fileLocation = itemToAnalyse.getFilelocation();
            //Toast.makeText(mContext, String.valueOf(child), Toast.LENGTH_SHORT);
            File image = new File(fileLocation);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (height > width) {
                width = height;
            }

            bitmap = Bitmap.createScaledBitmap(bitmap,TF_OD_API_INPUT_SIZE,TF_OD_API_INPUT_SIZE,true);

            Bitmap drawableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas drawOnBitmap = new Canvas(bitmap);
            String resultstring = "";

            final List<Detector.Recognition> mappedRecognitions =
                    new LinkedList<Detector.Recognition>();

            TensorImage imageTensor = TensorImage.fromBitmap(bitmap);
            List<Detection> results = objectDetector.detect(imageTensor);

            //
            for (final Detection result : results) {
                final RectF location = result.getBoundingBox();
                int objectWidth = (int) (location.right - location.left);
                int objectHeight = (int) (location.bottom - location.top);
                Category detectedObjectClass = result.getCategories().get(0);
                String detectedObjectClassString = detectedObjectClass.getLabel().toString();

                if (detectedObjectClassString.contains("s.mansoni_egg") && objectWidth < maximumObjectSizeSchisto && objectHeight < maximumObjectSizeSchisto && objectWidth > minimumObjectSizeSchisto && objectHeight > minimumObjectSizeSchisto) {
                    objectIsDetected = true;
                    drawOnBitmap.drawRect(location, paint);
                    mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), location));
                    resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n" + "Width: " + String.valueOf(objectWidth) + "\n" + "Height: " + String.valueOf(objectHeight) + "\n";
                }
                else if (!detectedObjectClassString.contains("s.mansoni_egg")  && objectWidth < maximumObjectSizeOther && objectHeight < maximumObjectSizeOther && objectWidth > minimumObjectSizeOther && objectHeight > minimumObjectSizeOther) {
                    objectIsDetected = true;
                    drawOnBitmap.drawRect(location, paint);
                    mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), location));
                    resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n" + "Height: " + String.valueOf(objectHeight) + "\n" + "Width: " + String.valueOf(objectWidth);
                }
            }

            long currTimestamp = 0;
            //itemToAnalyse.setObjectsDetected(resultstring);
            tracker.trackResults(mappedRecognitions, currTimestamp);
            tracker.draw(drawOnBitmap);

            if (objectIsDetected == true) {
                //if this is above minimum confidence add it to the list
                if (childrenContainingEggs.contains(child) == false) {
                    childrenContainingEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected(resultstring);
                itemToAnalyse.setImageThumbnail(bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        testResultTextview.setText("Positive");
                        testResultTextview.setTextColor(Color.rgb(240, 0, 0));
                    }
                });

                sql.updateImageData(itemToAnalyse.getUUID(), resultstring, Calendar.getInstance().getTime().toString());
                sql.updateSamplePositivityResult(sampleNumber, "Positive");
            } else {
                if (childrenContainingNoEggs.contains(child) == false) {
                    childrenContainingNoEggs.add(child);
                }
                itemToAnalyse.setObjectsDetected("No objects detected.");
                sql.updateImageData(itemToAnalyse.getUUID(), "No objects detected.", Calendar.getInstance().getTime().toString());
                //sql.updateSamplePositivityResult(sampleNumber, "Negative");
            }
        }
        //Here's where I am changing updating the lists by setting the childrenContainingEggs and childrenContainingNoEggs as my data for the drawers
        adapter.moveChildBetweenGroup(2,childrenContainingEggs, 0);
        adapter.moveChildBetweenGroup(2,childrenContainingNoEggs, 1);
        lstItemsGroup.replace(lstGroups.get(2), notScannedList, emptylist);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                if (childrenContainingEggs.size() == 0){
                    testResultTextview.setText("Negative");
                    testResultTextview.setTextColor(Color.rgb(0,0,240));
                    sql.updateSamplePositivityResult(sampleNumber, "Negative");
                }
                Double eggsPerGramCalc = eggsFoundList.size() / Double.valueOf(weightOfFeces);
                eggsPerGramTextview.setText(eggsPerGramCalc.toString());
                detectEggsButton.setText("Detection complete");
            }
        });

    }
    catch (Exception e){
        Log.e("Inference efficientdet", e.toString());
    }
}

    void analyseFolderEfficientDetTwoCrop(){

        moveGroupsNotScanned();
        ArrayList<Integer> childrenContainingEggs = new ArrayList<>();
        ArrayList<Integer> childrenContainingNoEggs = new ArrayList<>();
        String UUID;
        com.example.android.SmartMicroscope.customview.OverlayView trackingOverlay;
        // Configuration values for the prepackaged SSD model.
        final int TF_OD_API_INPUT_SIZE = 640; //SSDresnet is 640 x 640
        int imageCropSize = 640;
        final boolean TF_OD_API_IS_QUANTIZED = false;
        //This number sets a max object size. We know our eggs should not take up more than a certain percentage of our images
        //The integer here means the maximum number of pixels an object can take up in either height or width, in the final image fed into the model
        //i.e 200 pixels of a 640 x 640 image is around 1/3 of the width and height of the image (1/9th of surface area)
        int maximumObjectSizeSchisto = 550;
        int minimumObjectSizeSchisto = 150;
        int maximumObjectSizeOther = 270;
        int minimumObjectSizeOther = 20;

        final float MINIMUM_CONFIDENCE_TF_OD_API = 0.4f;
        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
        ArrayList<Bitmap> inferenceArray = new ArrayList<Bitmap>();
        Bitmap bitmap;
        SharedPreferences sharedPref;
        SharedPreferences.Editor editor;
        sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String TF_OD_API_MODEL_FILE = "10052022efficientdet.tflite";
        String labelLocation = sharedPref.getString("modelLabelFile", "10052022efficientdetlabels.txt");//"file:///android_asset/schistolabelmap.txt");


        String TF_OD_API_LABELS_FILE = labelLocation;

        moveGroupsNotScanned();
        tracker = new MultiBoxTracker(mContext);
        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, 0);
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        final Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(50.0f);
        paint.setTextSize(10.0f);

        try{
            ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setScoreThreshold(MINIMUM_CONFIDENCE_TF_OD_API).setMaxResults(10).build();
            ObjectDetector objectDetector = ObjectDetector.createFromFileAndOptions(mContext, TF_OD_API_MODEL_FILE, options);
            int groupLength = adapter.getChildrenCount(2);
            FileListItemExpandable itemToAnalyse;
            String fileLocation;
            Boolean objectIsDetected = false;
            for(int child = 0; child<groupLength; child++){

                updateProgress(child, groupLength-1);

                objectIsDetected = false;
                itemToAnalyse = adapter.getFileListChild(2, child);
                fileLocation = itemToAnalyse.getFilelocation();
                //Toast.makeText(mContext, String.valueOf(child), Toast.LENGTH_SHORT);
                File image = new File(fileLocation);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                if (height > width) {
                    width = height;
                }

                Bitmap croppedBitmap;
                Bitmap originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);

                Canvas drawOnBitmap = new Canvas(originalBitmap);
                String resultstring = "";

                final List<Detector.Recognition> mappedRecognitions =
                        new LinkedList<Detector.Recognition>();

                int[] cropxstart = {0, 1756};
                int[] cropxend = {2300, 4056};

                for (int crop = 0; crop<2; crop++){
                    croppedBitmap = Bitmap.createBitmap(originalBitmap, cropxstart[crop], 0, cropxend[crop] - cropxstart[crop], originalBitmap.getHeight());
                    TensorImage imageTensor = TensorImage.fromBitmap(croppedBitmap);
                    List<Detection> results = objectDetector.detect(imageTensor);

                    for (final Detection result : results) {
                        final RectF location = result.getBoundingBox();
                        final RectF locationCropped = new RectF((location.left + cropxstart[crop]), location.top, (location.right + cropxstart[crop]), location.bottom);
                        int objectWidth = (int) (location.right - location.left);
                        int objectHeight = (int) (location.bottom - location.top);
                        Category detectedObjectClass = result.getCategories().get(0);
                        String detectedObjectClassString = detectedObjectClass.getLabel().toString();

                        if (detectedObjectClassString.contains("s.mansoni_egg") && objectWidth < maximumObjectSizeSchisto && objectHeight < maximumObjectSizeSchisto && objectWidth > minimumObjectSizeSchisto && objectHeight > minimumObjectSizeSchisto) {
                            objectIsDetected = true;
                            drawOnBitmap.drawRect(locationCropped, paint);
                            mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), locationCropped));
                            resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n";
                        }
                        else if (!detectedObjectClassString.contains("s.mansoni_egg")  && objectWidth < maximumObjectSizeOther && objectHeight < maximumObjectSizeOther && objectWidth > minimumObjectSizeOther && objectHeight > minimumObjectSizeOther) {
                            objectIsDetected = true;
                            drawOnBitmap.drawRect(locationCropped, paint);
                            mappedRecognitions.add(new Detector.Recognition(detectedObjectClass.getLabel(), detectedObjectClass.getLabel(), detectedObjectClass.getScore(), locationCropped));
                            resultstring = resultstring + "\n" + detectedObjectClass.getLabel() + ": " + detectedObjectClass.getScore() + "\n" + "Top: " +  String.valueOf(location.top) + "\n" + "Left: " + String.valueOf(location.left) + "\n" + " Bottom: " + String.valueOf(location.bottom)+ "\n" + "Right: " + String.valueOf(location.right) + "\n" + "Height: " + String.valueOf(objectHeight) + "\n" + "Width: " + String.valueOf(objectWidth) + "\n";
                        }
                    }
                }

                long currTimestamp = 0;
                //itemToAnalyse.setObjectsDetected(resultstring);
                //tracker.trackResults(mappedRecognitions, currTimestamp);
                //tracker.draw(drawOnBitmap);
                //drawOnBitmap.drawBitmap();
                if (objectIsDetected == true) {
                    //if this is above minimum confidence add it to the list
                    if (childrenContainingEggs.contains(child) == false) {
                        childrenContainingEggs.add(child);
                    }
                    itemToAnalyse.setObjectsDetected(resultstring);

                    bitmap = Bitmap.createScaledBitmap(originalBitmap,TF_OD_API_INPUT_SIZE,TF_OD_API_INPUT_SIZE,true);
                    itemToAnalyse.setImageThumbnail(bitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            testResultTextview.setText("Positive");
                            testResultTextview.setTextColor(Color.rgb(240, 0, 0));
                        }
                    });

                    sql.updateImageData(itemToAnalyse.getUUID(), resultstring, Calendar.getInstance().getTime().toString());
                    sql.updateSamplePositivityResult(sampleNumber, "Positive");
                } else {
                    if (childrenContainingNoEggs.contains(child) == false) {
                        childrenContainingNoEggs.add(child);
                    }
                    itemToAnalyse.setObjectsDetected("No objects detected.");
                    sql.updateImageData(itemToAnalyse.getUUID(), "No objects detected.", Calendar.getInstance().getTime().toString());
                    //sql.updateSamplePositivityResult(sampleNumber, "Negative");
                }
            }
            //Here's where I am changing updating the lists by setting the childrenContainingEggs and childrenContainingNoEggs as my data for the drawers
            adapter.moveChildBetweenGroup(2,childrenContainingEggs, 0);
            adapter.moveChildBetweenGroup(2,childrenContainingNoEggs, 1);
            lstItemsGroup.replace(lstGroups.get(2), notScannedList, emptylist);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    if (childrenContainingEggs.size() == 0){
                        testResultTextview.setText("Negative");
                        testResultTextview.setTextColor(Color.rgb(0,0,240));
                        sql.updateSamplePositivityResult(sampleNumber, "Negative");
                    }
                    Double eggsPerGramCalc = eggsFoundList.size() / Double.valueOf(weightOfFeces);
                    eggsPerGramTextview.setText(eggsPerGramCalc.toString());
                    detectEggsButton.setText("Detection complete");
                }
            });

        }
        catch (Exception e){
            Log.e("Inference efficientdet", e.toString());
        }
    }

    public void updateProgress(int progressStatus, int maxProgress) {
        final String childString = Integer.toString(progressStatus);
        final int progressInt = progressStatus;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectEggsButton.setText( "Processing image: " + childString);
                simpleProgressBar.setMax(maxProgress);
                simpleProgressBar.setProgress(progressInt);
            }
        });
    }


}
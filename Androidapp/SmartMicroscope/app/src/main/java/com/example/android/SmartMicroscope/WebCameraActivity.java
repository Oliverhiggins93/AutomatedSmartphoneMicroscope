package com.example.android.SmartMicroscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.SmartMicroscope.adapters.SQLDatabaseAdapter;
import com.example.android.SmartMicroscope.customview.ScalingImageView;
import com.example.android.SmartMicroscope.utilityclasses.FileSavingUtilities;
import com.example.android.SmartMicroscope.utilityclasses.LocationOfTest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebCameraActivity extends AppCompatActivity {
    private static final String TAG = "Debug";
    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    private AlertDialog mDialog;
    public String saveDirectory, rootDirectory, locationString;
    public File picturefile;
    SQLDatabaseAdapter sqlAdapter;
    Integer numberStepsFocus = 50;
    Integer numberStepsToScan = 2000;
    Integer numberImagesPerScan = 64;
    Integer xmotor_location = 0;
    Integer ymotor_location = 0;
    Integer zmotor_location = 0;
    Integer numberImagesPerAutofocus = 2;
    Boolean currentlyMovingFlag = false;
    public String sampleNumber = "00000000";
    Button forwardButtonCam, backwardButtonCam, leftButtonCam, rightButtonCam, cameraButtonCam, setSampleNumberButton, focusPlusButtonCam, focusMinusButtonCam, scanButtonCam,autoFocusButton, xystepsplusButton, xystepsminusButton, focusstepsplusButton, focusstepsminusButton;
    TextView xyStepsTextview, focusStepsTextview, xMotorLocationTextview, yMotorLocationTextview, zMotorLocationTextview, scanningStatusTextview;
    int imageCaptureSize;
    int imagePreviewSize;
    Context context;
    SharedPreferences sharedPref;
    BitmapFactory.Options optsImageView = new BitmapFactory.Options();
    public static String EXTRA_ADDRESS = "device_address";
    byte[] imageByteArray;
    String address = "";

    FileSavingUtilities fileSavingUtility;

    Double latitude;
    Double longitude;
    SharedPreferences.Editor editor;

    private TextView serverStatus;
    private ScalingImageView cameraView;
    Bitmap bitmap, smallBitmap;

    // DEFAULT IP
    public static String SERVERIP = "0.0.0.0";

    // DESIGNATE A PORT
    public static final int CAMERAPORT = 8000;
    //public static final int COMMSPORT = 8001;

    private Handler handler = new Handler();

    private ServerSocket serverSocket, commsServerSocket;
    Socket commsSocket;
    private ScaleGestureDetector scaleGestureDetector;
    private float imageViewScaleFactor = 1.0f;

    Boolean saveBitmapFlag, largeImageSaved;
    Boolean connectedBoolean, keepTryingSocketBoolean;
    int downsampleBitmapPreview;
    int downsampleBitmapPreviewHeight;
    int downsampleBitmapPreviewWidth;
    Boolean updateImageViewBoolean;
    byte[] imageByteArrayPreview;



    private static final int INVALID_POINTER_ID = -1;

    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private float mLastGestureX;
    private float mLastGestureY;
    private int mActivePointerId = INVALID_POINTER_ID;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webcamera);
        ButterKnife.bind(this);
        initView();
        setViewButtons();
        context = this;
        Intent intent = getIntent();
        //cameraView.setScaleType();
        if (intent.hasExtra("sampleNumber")){
            sampleNumber = intent.getStringExtra("sampleNumber");
        }
        else {
            sampleNumber = "00000000";
        }
        saveDirectory = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/" + sampleNumber;
        fileSavingUtility = new FileSavingUtilities(sampleNumber, saveDirectory, context);
        sqlAdapter = new SQLDatabaseAdapter(this);


        LocationOfTest mLocationOfTest = new LocationOfTest(context);
        latitude = mLocationOfTest.getLatitude();
        longitude = mLocationOfTest.getLongitude();
        locationString = mLocationOfTest.getLocationNearestCity();

        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        numberStepsToScan = sharedPref.getInt("numberStepsToScan", 2050);
        numberImagesPerScan = sharedPref.getInt("numberImagesToScan", 12*12);
        xmotor_location = sharedPref.getInt("xmotor_location", 0);
        ymotor_location = sharedPref.getInt("ymotor_location", 0);
        zmotor_location = sharedPref.getInt("zmotor_location", 0);
        numberImagesPerAutofocus = sharedPref.getInt("numberImagesPerAutofocus", 2);
        xMotorLocationTextview.setText("X location: " + xmotor_location);
        yMotorLocationTextview.setText("Y location: " + ymotor_location);
        zMotorLocationTextview.setText("Z location: " + zmotor_location);
        xyStepsTextview.setText("XY steps: " + numberStepsToScan);
        focusStepsTextview.setText("Focus steps: " + numberStepsFocus);
        imageCaptureSize = sharedPref.getInt("captureresolution", 4056);

        setUiEnabled(false);

        int widthInputBitmap = 2028;
        int heightInputBitmap = 1520;
        bitmap = Bitmap.createBitmap(widthInputBitmap, heightInputBitmap, Bitmap.Config.ARGB_8888);
        downsampleBitmapPreview = 1;
        downsampleBitmapPreviewHeight = (int) heightInputBitmap / downsampleBitmapPreview;
        downsampleBitmapPreviewWidth = (int) widthInputBitmap / downsampleBitmapPreview;
        smallBitmap = Bitmap.createBitmap(downsampleBitmapPreviewWidth, downsampleBitmapPreviewHeight, Bitmap.Config.ARGB_8888);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        SERVERIP = getLocalIpAddress();
        saveBitmapFlag = false;
        Thread fst = new Thread(new ServerThread());
        fst.start();

        SaverThread saverThread = new SaverThread();
        Thread saverThreadStarter = new Thread(saverThread);
        saverThreadStarter.start();

        updateImageViewBoolean = false;

        // update = new updateImageViewThread();
        //Thread updateThread = new Thread(update);
        //updateThread.start();
    }

    public class updateImageViewThread implements Runnable {
        public void run() {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            while (true) {

                if (updateImageViewBoolean) {
                    if (imageByteArrayPreview.length < 1000000){
                        opts.inSampleSize = 1;
                        opts.inScaled = false;
                    }
                    else{
                        opts.inSampleSize = 2;
                        opts.inScaled = true;
                    }

                    smallBitmap = BitmapFactory.decodeByteArray(imageByteArrayPreview, 0, imageByteArrayPreview.length, opts);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.setImageBitmap(null);
                            cameraView.setImageBitmap(smallBitmap);
                            cameraView.invalidate();
                        }
                    });

                    try{
                        cameraView.postInvalidate();
                        updateImageViewBoolean = false;
                    }
                    catch (Exception e){

                    }
                }
            }
        }
    }

    private void updateImageView() {
        if (imageByteArrayPreview.length < 1000000){
            optsImageView.inSampleSize = 1;
            optsImageView.inScaled = false;
        }
        else{
            optsImageView.inSampleSize = 2;
            optsImageView.inScaled = true;
        }
        smallBitmap = BitmapFactory.decodeByteArray(imageByteArrayPreview, 0, imageByteArrayPreview.length, optsImageView);
        cameraView.setImageBitmap(null);
        //cameraView.setImageBitmap(smallBitmap);
        cameraView.setImageBitmap(BitmapFactory.decodeByteArray(imageByteArrayPreview, 0, imageByteArrayPreview.length, optsImageView));
        cameraView.invalidate();
        updateImageViewBoolean = false;
    }




    @Override
    protected void onResume() {
        super.onResume();
    }

    public class SaverThread implements Runnable{
        BitmapFactory.Options options = new BitmapFactory.Options();
        int saveImageWidth = 0;
        Bitmap saveImageHolder;
        public void run() {
            while(true) {
                if (saveBitmapFlag == true) {
                    saveImageHolder = BitmapFactory.decodeByteArray(imageByteArrayPreview, 0, imageByteArrayPreview.length, options);
                if (saveBitmapFlag == true && picturefile != null && currentlyMovingFlag == false){ // && options.outWidth == imageCaptureSize) {
                    //bitmap = BitmapFactory.decodeByteArray(imageByteArrayPreview, 0, imageByteArrayPreview.length);
                    saveBitmapFlag = false;
                    try (FileOutputStream out = new FileOutputStream(picturefile)) {
                        if (saveImageHolder != null) {
                            saveImageHolder.compress(Bitmap.CompressFormat.JPEG, 80, out);
                            out.flush();
                            out.close();
                        }
                        largeImageSaved = true;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(WebCameraActivity.this, "Saved: " + picturefile.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
                //else{
                    //try{
                        //DataOutputStream dout=new DataOutputStream(commsSocket.getOutputStream());
                        //dout.writeUTF("capture\n");
                        //Thread.sleep(500);
                        //dout.flush();0
                        //dout.close();
                    //}
                    //catch (Exception e){
                        //e.printStackTrace();
                    //}
                //}
            }

        }
    }



    public class ServerThread implements Runnable {
        //String line;
        public void run() {
            int noDataCounter = 0;
            keepTryingSocketBoolean = true;
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Listening on IP: " + SERVERIP);
                        }
                    });

                    while(keepTryingSocketBoolean){
                        serverSocket = new ServerSocket(CAMERAPORT);
                        commsSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(new BufferedInputStream(commsSocket.getInputStream()));
                        connectedBoolean = true;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                serverStatus.setText("Connected. Resolution: " + String.valueOf(sharedPref.getInt("captureresolution", 0)));
                                setUiEnabled(true);
                            }
                        });
                        while (connectedBoolean) {
                            if (in.available() > 0) {
                                noDataCounter = 0;
                                int imageSize = 0;
                                //byte[] imageByteArray;
                                try {
                                    imageSize = in.readInt();
                                    imageByteArray = new byte[imageSize];
                                    if (imageSize < 0) {
                                        continue;
                                    }
                                    in.readFully(imageByteArray);
                                    imageByteArrayPreview = imageByteArray;
                                    Log.i("imagelength", Integer.toString(imageByteArray.length));
                                    //updateImageViewBoolean = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateImageView();
                                        }
                                    });

                                    //Boolean = true;
                                    xmotor_location = in.readInt();
                                    ymotor_location = in.readInt();
                                    zmotor_location = in.readInt();
                                    currentlyMovingFlag = in.readBoolean();
                                    //saveBitmapFlag = in.readBoolean();


                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            xMotorLocationTextview.setText("X location: " + xmotor_location.toString());
                                            yMotorLocationTextview.setText("Y location: " + ymotor_location.toString());
                                            zMotorLocationTextview.setText("Z location: " + zmotor_location.toString());
                                        }
                                    });

                                } catch (EOFException eof) {
                                    //connectedBoolean = false;
                                    //in = new DataInputStream(new BufferedInputStream(serverSocket.accept().getInputStream()));
                                    connectedBoolean = false;
                                    try {
                                        serverSocket.close();
                                    } catch (Exception f) {
                                    }
                                } catch (OutOfMemoryError oom) {
                                    break;
                                } catch (NegativeArraySizeException neg) {
                                    break;
                                }
                                catch (Exception e) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                            try {
                                                serverSocket.close();
                                            } catch (Exception f) {
                                            }
                                            connectedBoolean = false;
                                        }
                                    });
                                    e.printStackTrace();
                                    connectedBoolean = false;
                                    try {
                                        serverSocket.close();
                                    } catch (Exception f) {
                                    }
                                }
                            }
                            else{
                                noDataCounter++;
                                Thread.sleep(1);
                                if (noDataCounter > 20000){
                                    try {
                                        serverSocket.close();
                                    } catch (Exception f) {
                                    }
                                    connectedBoolean = false;
                                    noDataCounter = 0;
                                }
                            }
                        }
                    }

                }
                else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setText("Error" + e.toString());
                        connectedBoolean = false;
                        try {
                            serverSocket.close();
                        } catch (Exception f) {
                        }
                    }
                });
                e.printStackTrace();
            }
        }
    }

    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().contains("192")) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // when a scale gesture is detected, use it to resize the image
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            return true;
        }
    }

    public void scanSlide(int numberImagesPerScan, int stepsToMove, int[] focusDepths){
        //int imagesPerX = Math.s;

        new Thread( new Runnable() { @Override public void run() {
            int numberImages = numberImagesPerScan;
            int numberImagesPerSide = (int) Math.sqrt(numberImagesPerScan);
            int imagesCaptured = 0;
            int startingPositionX = xmotor_location;
            int startingPositionY = ymotor_location;
            int startingPositionZ = zmotor_location;
            ArrayList<Integer> positionsToMoveX = new ArrayList<Integer>();
            ArrayList<Integer> positionsToMoveY = new ArrayList<Integer>();
            ArrayList<Integer> positionsToMoveZ = new ArrayList<Integer>();
            Boolean firstPositionDoNotWait = true;
            String message = sharedPref.getString("captureresolutionstring", "res 4056");
            //String message = "res 4056";
            sendStringSocket(message, 200);
            imageCaptureSize = sharedPref.getInt("captureresolution", 4056);
            //imageCaptureSize = 4056;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scanningStatusTextview.setText("Currently scanning: X: " + "0" + " Y: " + "0" + ". Progress: " + "0" + " / " + String.valueOf(numberImages));
                    xMotorLocationTextview.setText("X location: " + xmotor_location.toString());
                    yMotorLocationTextview.setText("Y location: " + ymotor_location.toString());
                    zMotorLocationTextview.setText("Z location: " + zmotor_location.toString());
                }
            });

            int firstImageLength = imageByteArrayPreview.length;
            for (int i = 0; i<numberImagesPerSide;i++){
                positionsToMoveX.add(xmotor_location + (stepsToMove * i));
                positionsToMoveY.add(ymotor_location + (stepsToMove * i));
            }

            for (int y = 0; y<numberImagesPerSide; y++){
                ymotor_location = positionsToMoveY.get(y);
                for (int x = 0; x<numberImagesPerSide; x++){
                    xmotor_location = positionsToMoveX.get(x);
                    message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + ((int) zmotor_location);
                    sendStringSocket(message, 200);
                    sleepThread(3000);
                    while(currentlyMovingFlag == true){
                        sleepThread(1000);
                    }

                    if (x !=0){
                        if (x % numberImagesPerAutofocus == 0){
                            Log.i("Autofocus", "autofocusing");
                            message = "autofocus_fine";
                            sendStringSocket(message, 200);
                            sleepThread(2000);
                            while(currentlyMovingFlag == true) {
                                sleepThread(1000);
                            }
                            //sleepThread(2000);
                        }
                    }
                    sleepThread(2000);

                    for (int fDepthIndex = 0; fDepthIndex < focusDepths.length; fDepthIndex++){
                        largeImageSaved = false;
                        takePicture();
                        sleepThread(1000);
                        while (largeImageSaved == false){
                            sleepThread(500);
                        }
                        imagesCaptured = imagesCaptured + 1;

                        //This code to update UI elements
                        int progressX = x;
                        int progressY = y;
                        int progressImagesCaptured = imagesCaptured;
                        int progressNumberImagesPerScan = numberImagesPerScan * focusDepths.length;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                scanningStatusTextview.setText("Currently scanning: X: " + progressX + " Y: " + progressY + ". Progress: " + progressImagesCaptured + " / " + progressNumberImagesPerScan);
                                xMotorLocationTextview.setText("X location: " + xmotor_location.toString());
                                yMotorLocationTextview.setText("Y location: " + ymotor_location.toString());
                                zMotorLocationTextview.setText("Z location: " + zmotor_location.toString());
                            }
                        });

                    }

                }
                Collections.reverse(positionsToMoveX);
            }
            sleepThread(500);
            xmotor_location = startingPositionX;
            ymotor_location = startingPositionY;
            zmotor_location = startingPositionZ;
            message = "max " + startingPositionX + ",may " + startingPositionY + ",maz " + startingPositionZ;
            sendStringSocket(message, 200);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    xMotorLocationTextview.setText("X location: " + xmotor_location.toString());
                    yMotorLocationTextview.setText("Y location: " + ymotor_location.toString());
                    zMotorLocationTextview.setText("Z location: " + zmotor_location.toString());
                    Toast.makeText(context, "Scan complete", Toast.LENGTH_LONG);
                    scanningStatusTextview.setText("Scanning complete");

                }
            });
        } } ).start();

    }

    private void initView() {
        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        connectedBoolean = false;
        keepTryingSocketBoolean = false;
        try {

            serverSocket.close();
            //commsServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectedBoolean = false;
        keepTryingSocketBoolean = false;
        try {
            serverSocket.close();
            //if (commsSocket != null){
                //commsSocket.close();
            //}

            //commsServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar_web, menu);
        return true;
    }

    private void sleepThread(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message;
        switch (item.getItemId()) {
            case R.id.menu_zerolocation:
                xmotor_location = 0;
                ymotor_location = 0;
                zmotor_location = 0;
                xMotorLocationTextview.setText("X location: " + xmotor_location);
                yMotorLocationTextview.setText("Y location: " + ymotor_location);
                zMotorLocationTextview.setText("Z location: " + zmotor_location);
                editor.putInt("xmotor_location", xmotor_location);
                editor.putInt("ymotor_location", ymotor_location);
                editor.putInt("zmotor_location", zmotor_location);
                editor.commit();
                message = "setzero";
                sendStringSocket(message, 200);
                break;

            case R.id.menu_gotozero:
                xmotor_location = 0;
                ymotor_location = 0;
                zmotor_location = 0;
                xMotorLocationTextview.setText("X location: " + xmotor_location);
                yMotorLocationTextview.setText("Y location: " + ymotor_location);
                zMotorLocationTextview.setText("Z location: " + zmotor_location);
                message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 200);
                editor.putInt("xmotor_location", xmotor_location);
                editor.putInt("ymotor_location", ymotor_location);
                editor.putInt("zmotor_location", zmotor_location);
                editor.commit();
                break;

            case R.id.menu_resolution_capture:
                showCaptureResolutionListDialog();
                break;
            case R.id.menu_resolution_preview:
                showPreviewResolutionListDialog();
                break;
            case R.id.menu_exposure_on:
                message = "aex a";
                sendStringSocket(message, 200);
                break;
            case R.id.menu_exposure_off:
                message = "aex off";
                sendStringSocket(message, 200);
                break;
            case R.id.menu_awb_on:
                message = "awb a";
                sendStringSocket(message, 200);
                break;
            case R.id.menu_awb_off:
                message = "awb off";
                sendStringSocket(message, 200);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCaptureResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WebCameraActivity.this);
        View rootView = LayoutInflater.from(WebCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        List<String> resolutions = resolutions = new ArrayList<>();
        resolutions.add("4056x3040");
        resolutions.add("2028x1520");
        resolutions.add("1362x1024");
        resolutions.add("1014x760");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(WebCameraActivity.this, android.R.layout.simple_list_item_1, resolutions);
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String resolution = (String) adapterView.getItemAtPosition(position);
                if(resolution == "4056x3040"){
                    String message = "res 4056";
                    sendStringSocket(message, 200);
                    imageCaptureSize = 4056;
                }
                if(resolution == "2028x1520") {
                    String message = "res 2028";
                    sendStringSocket(message, 200);
                    imageCaptureSize = 2028;
                }
                if(resolution == "1362x1024"){
                    String message = "res 1362";
                    sendStringSocket(message, 200);
                    imageCaptureSize = 1362;
                }
                if(resolution == "1014x760"){
                    String message = "res 1014";
                    sendStringSocket(message, 200);
                    imageCaptureSize = 1014;
                }
                editor.putInt("captureresolution", imageCaptureSize);
                editor.putString("captureresolutionstring", "res " + String.valueOf(imageCaptureSize));
                editor.commit();
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
        serverStatus.setText("Connected. Resolution: " + String.valueOf(sharedPref.getInt("captureresolution", 0)));
    }

    private void showPreviewResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WebCameraActivity.this);
        View rootView = LayoutInflater.from(WebCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        List<String> resolutions = resolutions = new ArrayList<>();
        resolutions.add("4056x3040");
        resolutions.add("2028x1520");
        resolutions.add("1362x1024");
        resolutions.add("1014x760");
        resolutions.add("507x380");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(WebCameraActivity.this, android.R.layout.simple_list_item_1, resolutions);
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String resolution = (String) adapterView.getItemAtPosition(position);
                if(resolution == "1014x760"){
                    String message = "pre 1014";
                    sendStringSocket(message, 200);
                }
                else if(resolution == "507x380"){
                    String message = "pre 507";
                    sendStringSocket(message, 200);
                }
                else if(resolution == "2028x1520"){
                    String message = "pre 2028";
                    sendStringSocket(message, 200);
                }
                else if(resolution == "4056x3040"){
                    String message = "pre 4056";
                    sendStringSocket(message, 200);
                }
                else if(resolution == "1362x1024"){
                    String message = "pre 1362";
                    sendStringSocket(message, 200);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
        serverStatus.setText("Connected. Resolution: " + String.valueOf(sharedPref.getInt("captureresolution", 0)));
    }

    private void takePicture() {
        sendStringSocket("capture", 100);
        picturefile = fileSavingUtility.calculateFileName();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        long id = sqlAdapter.insertData(UUID.randomUUID().toString(), sampleNumber, picturefile.getAbsolutePath(), Uri.fromFile(picturefile).toString(), dtf.format(LocalDateTime.now()).toString(), locationString, longitude, latitude, "No object detectred", "No result yet", "Not yet analysed", "Feces", "User", "70kg", "100cm", fileSavingUtility.getFilecountString(), "5", "", "", String.valueOf(1.12 * Math.pow(10,-6) / 10 ), String.valueOf(10));
        saveBitmapFlag = true;
        try{
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(picturefile));
        WebCameraActivity.this.sendBroadcast(intent);
        MediaScannerConnection.scanFile(WebCameraActivity.this,
                new String[]{picturefile.toString()}, null,
                (path, uri) -> Log.d("onScanCompleted", "Scanned " + path + " and uri " + uri));
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setViewButtons() {
        forwardButtonCam = (Button) findViewById(R.id.button_forward_cam);
        leftButtonCam = (Button) findViewById(R.id.button_left_cam);
        rightButtonCam = (Button) findViewById(R.id.button_right_cam);
        backwardButtonCam = (Button) findViewById(R.id.button_backward_cam);
        focusPlusButtonCam = (Button) findViewById(R.id.button_focusplus_cam);
        focusMinusButtonCam = (Button) findViewById(R.id.button_focusminus_cam);
        scanButtonCam = (Button) findViewById(R.id.button_scan_cam);
        cameraButtonCam = (Button) findViewById(R.id.button_capture_cam);
        setSampleNumberButton = (Button) findViewById(R.id.button_set_sample_number);
        autoFocusButton = (Button) findViewById(R.id.button_autofocus_cam);
        xystepsplusButton = (Button) findViewById(R.id.button_xysteps_plus);
        xystepsminusButton = (Button) findViewById(R.id.button_xysteps_minus);
        focusstepsplusButton = (Button) findViewById(R.id.button_focussteps_plus);
        focusstepsminusButton = (Button) findViewById(R.id.button_focussteps_minus);
        xyStepsTextview = (TextView) findViewById(R.id.textview_xysteps);
        focusStepsTextview = (TextView) findViewById(R.id.textview_focussteps);
        xMotorLocationTextview = (TextView) findViewById(R.id.textview_xmotorlocation);
        yMotorLocationTextview = (TextView) findViewById(R.id.textview_ymotorlocation);
        zMotorLocationTextview = (TextView) findViewById(R.id.textview_zmotorlocation);
        scanningStatusTextview = (TextView) findViewById(R.id.textview_scanningstatus);
        serverStatus = (TextView) findViewById(R.id.server_status);
        cameraView = (ScalingImageView) findViewById(R.id.camera_view);
        //mToolbar = (Toolbar) findViewById((R.id.toolbar));

        forwardButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (usbcontroller.connection != null){
                //    usbcontroller.backwards();
                //}
                //mTextureView.setLayoutParams(new FrameLayout.LayoutParams(6400, 4800));

                xmotor_location = xmotor_location + numberStepsToScan;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 200);
                editor.putInt("xmotor_location", xmotor_location);
                editor.commit();
                xMotorLocationTextview.setText("X location:" + xmotor_location);


            }
        });
        backwardButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xmotor_location = xmotor_location - numberStepsToScan;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 200);
                editor.putInt("xmotor_location", xmotor_location);
                editor.commit();
                xMotorLocationTextview.setText("X location:" + xmotor_location);
            }
        });
        leftButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ymotor_location = ymotor_location - numberStepsToScan;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 200);
                editor.putInt("ymotor_location", ymotor_location);
                editor.commit();
                yMotorLocationTextview.setText("Y location:" + ymotor_location);
            }
        });
        rightButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ymotor_location = ymotor_location + numberStepsToScan;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 200);
                editor.putInt("ymotor_location", ymotor_location);
                editor.commit();
                yMotorLocationTextview.setText("Y location:" + ymotor_location);
            }
        });
        cameraButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();

            }
        });
        focusPlusButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zmotor_location = zmotor_location + numberStepsFocus;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 0);
                editor.putInt("zmotor_location", zmotor_location);
                editor.commit();
                zMotorLocationTextview.setText("Z location: " + zmotor_location);
            }
        });
        focusMinusButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zmotor_location = zmotor_location - numberStepsFocus;
                String message = "max " + xmotor_location + ",may " + ymotor_location + ",maz " + zmotor_location;
                sendStringSocket(message, 0);
                editor.putInt("zmotor_location", zmotor_location);
                editor.commit();
                zMotorLocationTextview.setText("Z location: " + zmotor_location);
            }
        });
        autoFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "autofocus_coarse";
                sendStringSocket(message, 200);
                Toast.makeText(getApplicationContext(), "Focussing", Toast.LENGTH_LONG);
            }
        });
        xystepsplusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberStepsToScan = numberStepsToScan * 2;
                if (numberStepsToScan > 6000){
                    numberStepsToScan = 6000;
                }
                xyStepsTextview.setText("XY steps: " + numberStepsToScan);
            }
        });
        xystepsminusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberStepsToScan = (int) Math.ceil(((double) numberStepsToScan / 2));
                xyStepsTextview.setText("XY steps: " + numberStepsToScan);
            }
        });

        focusstepsplusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberStepsFocus = numberStepsFocus * 2;
                if (numberStepsFocus > 5000){
                    numberStepsFocus = 5000;
                }
                focusStepsTextview.setText("Focus steps: " + numberStepsFocus);
            }
        });
        focusstepsminusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberStepsFocus = (int) Math.ceil(((double) numberStepsFocus / 2));
                focusStepsTextview.setText("Focus steps: " + numberStepsFocus);
            }
        });

        scanButtonCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanSlide(numberImagesPerScan, numberStepsToScan, new int[] {0});
            }
        });

    }

    private void sendStringSocket(String message, int sleepMilliseconds){
        String messageLine = message + "\n";
        new Thread(new Runnable() {
            public void run(){
                try{
                    DataOutputStream dout=new DataOutputStream(commsSocket.getOutputStream());
                    dout.writeUTF(messageLine);
                    //dout.flush();
                    //dout.close();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setUiEnabled(boolean bool) {
        forwardButtonCam.setEnabled(bool);
        backwardButtonCam.setEnabled(bool);
        leftButtonCam.setEnabled(bool);
        rightButtonCam.setEnabled(bool);
        focusPlusButtonCam.setEnabled(bool);
        focusMinusButtonCam.setEnabled(bool);
        scanButtonCam.setEnabled(bool);
        autoFocusButton.setEnabled(bool);

    }
}

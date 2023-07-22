package com.example.android.SmartMicroscope.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLDatabaseAdapter {
    SQLDatabaseHelper sqlDatabaseHelper;
    Context context;
    SQLiteDatabase dbb;

    public SQLDatabaseAdapter(Context context)
    {
        context = context;
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
    }

    public void syncFilesystem(){
        dbb = sqlDatabaseHelper.getWritableDatabase();
        String[] columns = {sqlDatabaseHelper.UID,sqlDatabaseHelper.SampleNumber, sqlDatabaseHelper.FilePath, sqlDatabaseHelper.DateCreated, sqlDatabaseHelper.LocationCreated,sqlDatabaseHelper.PositivityResult, sqlDatabaseHelper.SampleType};
        Cursor cursor =dbb.query(sqlDatabaseHelper.TABLE_NAME,columns,null,null,sqlDatabaseHelper.SampleNumber,null,null);
        dbb.execSQL("DELETE FROM " + sqlDatabaseHelper.TABLE_NAME);
        while (cursor.moveToNext()) {
            String SampleNumber = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.FilePath));
            String FilePath = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.FilePath));
            File file = new File(FilePath);
            if (!file.exists()) {
                //Toast.makeText(this, file, Toast.LENGTH_SHORT);
                //dbb.delete()cursor.getPosition()
                //dbb.delete(sqlDatabaseHelper.TABLE_NAME, "FilePath=" + sqlDatabaseHelper.FilePath,null);
            }
        }
        //LocationOfTest mLocationOfTest = new LocationOfTest(context);
        Double latitude = 0.0;
        Double longitude = 0.0;
        String saveFolderString = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/smartmicroscope/";
        File saveFolder = new File(saveFolderString);
        File[] folders = saveFolder.listFiles();
        Integer count;
        for (File folder : folders) {
            count = 0;
            File[] imageFiles = folder.listFiles();
            if (folder.isDirectory()) {
                for (File imageFile : imageFiles){
                    String sampleNumber = folder.toString().split("/")[folder.toString().split("/").length -1];
                    String imageNumber = String.format("%8s", count.toString()).replace(' ', '0');
                    Date currentTime = Calendar.getInstance().getTime();
                    insertData(UUID.randomUUID().toString(), sampleNumber, imageFile.getAbsolutePath(), Uri.fromFile(imageFile).toString(), currentTime.toString(), "glasgow", latitude, longitude, "No object detectred", "No result yet", "Not yet analysed", "Feces", "User", "70kg", "100cm", imageNumber, "5", "0", "", "0", "10x");
                    count = count + 1;
                }
            }
        }
    }

    public long insertData(String UUID, String SampleNumber, String FilePath, String Uri, String DateCreated, String LocationCreated, Double Longitude, Double Latitude, String ObjectsDetected, String PositivityResult, String DateAnalysed, String SampleType, String TechnicianUsername, String PatientWeight, String PatientHeight, String ImageNumber, String SampleWeightGram, String EggsPerGram, String EggCount, String EffectivePixelSize, String OpticalMagnification)
    {
        dbb = sqlDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(sqlDatabaseHelper.UUID, UUID);
        contentValues.put(sqlDatabaseHelper.SampleNumber, SampleNumber);
        contentValues.put(sqlDatabaseHelper.FilePath, FilePath);
        contentValues.put(sqlDatabaseHelper.FileUri, Uri);
        contentValues.put(sqlDatabaseHelper.DateCreated, DateCreated);
        contentValues.put(sqlDatabaseHelper.LocationCreated, LocationCreated);
        contentValues.put(sqlDatabaseHelper.Latitude, Latitude);
        contentValues.put(sqlDatabaseHelper.Longitude, Longitude);
        contentValues.put(sqlDatabaseHelper.ObjectsDetected, ObjectsDetected);
        contentValues.put(sqlDatabaseHelper.PositivityResult, PositivityResult);
        contentValues.put(sqlDatabaseHelper.DateAnalysed, DateAnalysed);
        contentValues.put(sqlDatabaseHelper.SampleType, SampleType);
        contentValues.put(sqlDatabaseHelper.TechnicianUsername, TechnicianUsername);
        contentValues.put(sqlDatabaseHelper.PatientHeight, PatientHeight);
        contentValues.put(sqlDatabaseHelper.PatientWeight, PatientWeight);
        contentValues.put(sqlDatabaseHelper.ImageNumber, ImageNumber);
        contentValues.put(sqlDatabaseHelper.SampleWeight, SampleWeightGram);
        contentValues.put(sqlDatabaseHelper.EggsPerGram, EggsPerGram);
        contentValues.put(sqlDatabaseHelper.EggsPerGram, EggCount);
        contentValues.put(sqlDatabaseHelper.EffectivePixelSize, EffectivePixelSize);
        contentValues.put(sqlDatabaseHelper.OpticalMagnification, OpticalMagnification);
        long id = dbb.insert(sqlDatabaseHelper.TABLE_NAME, null , contentValues);
        return id;
    }

    public int updateImageData(String UUID, String ObjectsDetected, String DateAnalysed)
    {
        SQLiteDatabase db = sqlDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(sqlDatabaseHelper.ObjectsDetected, ObjectsDetected);
        contentValues.put(sqlDatabaseHelper.DateAnalysed, DateAnalysed);
        String[] whereArgs= {UUID};
        int count =db.update(sqlDatabaseHelper.TABLE_NAME,contentValues, sqlDatabaseHelper.UUID+" = ?",whereArgs );
        return count;
    }

    public int updateSamplePositivityResult(String SampleNumber, String PositivityResult)
    {
        SQLiteDatabase db = sqlDatabaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(sqlDatabaseHelper.PositivityResult, PositivityResult);
        String[] whereArgs= {SampleNumber};
        int count =db.update(sqlDatabaseHelper.TABLE_NAME,contentValues, sqlDatabaseHelper.SampleNumber +" = ?",whereArgs );
        return count;
    }
    public ArrayList<Map<String, String>> getFolderData()
    {
        SQLiteDatabase db = sqlDatabaseHelper.getReadableDatabase();
        String[] columns = {sqlDatabaseHelper.UID,sqlDatabaseHelper.SampleNumber, sqlDatabaseHelper.FilePath, sqlDatabaseHelper.DateCreated, sqlDatabaseHelper.LocationCreated,sqlDatabaseHelper.PositivityResult, sqlDatabaseHelper.SampleType};
        Cursor cursor =db.query(sqlDatabaseHelper.TABLE_NAME,columns,null,null,null,null,null);
        ArrayList<Map<String,String>> imageMapArray = new ArrayList<Map<String,String>>();

        while (cursor.moveToNext())
        {
            Map<String, String> imageMap = new HashMap<>();
            int cid =cursor.getInt(cursor.getColumnIndex(sqlDatabaseHelper.UID));
            String  SampleNumber =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleNumber));
            String  DateCreated = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.DateCreated));
            String  LocationCreated =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.LocationCreated));
            String  PositivityResult =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.PositivityResult));
            String  SampleType =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleType));
            String FilePath = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.FilePath));

            String WHERE = sqlDatabaseHelper.SampleNumber + " == '" + SampleNumber + "'" ;
            Long count = DatabaseUtils.queryNumEntries(db, "scannedImages", WHERE);
            imageMap.put("SampleNumber", SampleNumber);
            imageMap.put("DateCreated", DateCreated);
            imageMap.put("LocationCreated", LocationCreated);
            imageMap.put("NumberImages", String.valueOf(count));
            imageMap.put("PositivityResult", String.valueOf(PositivityResult));
            imageMap.put("SampleType", String.valueOf(SampleType));
            imageMap.put("FilePath", String.valueOf(FilePath));
            imageMapArray.add(imageMap);
        }
        return imageMapArray;
    }

    public ArrayList<Map<String, String>> getFolderDataSimple()
    {
        SQLiteDatabase db = sqlDatabaseHelper.getReadableDatabase();
        String[] columns = {sqlDatabaseHelper.UID,sqlDatabaseHelper.SampleNumber, sqlDatabaseHelper.FilePath, sqlDatabaseHelper.DateCreated, sqlDatabaseHelper.LocationCreated,sqlDatabaseHelper.PositivityResult, sqlDatabaseHelper.SampleType};
        Cursor cursor =db.query(sqlDatabaseHelper.TABLE_NAME,columns,null,null,sqlDatabaseHelper.SampleNumber,null,null);
        ArrayList<Map<String,String>> imageMapArray = new ArrayList<Map<String,String>>();

        while (cursor.moveToNext())
        {
            Map<String, String> imageMap = new HashMap<>();
            int cid =cursor.getInt(cursor.getColumnIndex(sqlDatabaseHelper.UID));
            String  SampleNumber =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleNumber));
            String  DateCreated = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.DateCreated));
            String  LocationCreated =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.LocationCreated));
            String  PositivityResult =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.PositivityResult));
            String  SampleType =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleType));
            String FilePath = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.FilePath));

            String WHERE = sqlDatabaseHelper.SampleNumber + " == '" + SampleNumber + "'" ;
            Long count = DatabaseUtils.queryNumEntries(db, "scannedImages", WHERE);
            imageMap.put("SampleNumber", SampleNumber);
            imageMap.put("DateCreated", DateCreated);
            imageMap.put("LocationCreated", LocationCreated);
            imageMap.put("NumberImages", String.valueOf(count));
            imageMap.put("PositivityResult", String.valueOf(PositivityResult));
            imageMap.put("SampleType", String.valueOf(SampleType));
            imageMap.put("FilePath", String.valueOf(FilePath));
            imageMapArray.add(imageMap);
        }
        return imageMapArray;
    }

    public ArrayList<Map<String, String>> getFileData(String SampleNumber)
    {
        SQLiteDatabase db = sqlDatabaseHelper.getReadableDatabase();
        String[] columns = {sqlDatabaseHelper.UID,sqlDatabaseHelper.UUID,sqlDatabaseHelper.SampleNumber, sqlDatabaseHelper.DateCreated, sqlDatabaseHelper.LocationCreated, sqlDatabaseHelper.Latitude, sqlDatabaseHelper.Longitude, sqlDatabaseHelper.PatientHeight, sqlDatabaseHelper.PatientWeight, sqlDatabaseHelper.SampleType, sqlDatabaseHelper.DateAnalysed, sqlDatabaseHelper.PositivityResult, sqlDatabaseHelper.ObjectsDetected, sqlDatabaseHelper.FilePath, sqlDatabaseHelper.ImageNumber, sqlDatabaseHelper.SampleWeight, sqlDatabaseHelper.EggsPerGram, sqlDatabaseHelper.EggCount, sqlDatabaseHelper.OpticalMagnification, sqlDatabaseHelper.EffectivePixelSize};
        String WHERE = sqlDatabaseHelper.SampleNumber + " == '" + SampleNumber + "'" ;
        Cursor cursor =db.query(sqlDatabaseHelper.TABLE_NAME,columns,WHERE, null, null, null, null);

        ArrayList<Map<String,String>> imageMapArray = new ArrayList<Map<String,String>>();

        while (cursor.moveToNext())
        {
            Map<String, String> imageMap = new HashMap<>();
            int cid =cursor.getInt(cursor.getColumnIndex(sqlDatabaseHelper.UID));
            String  DateCreated = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.DateCreated));
            String  UUID = cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.UUID));
            String  LocationCreated =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.LocationCreated));
            String  Latitude =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.Latitude));
            String  Longitude =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.Longitude));
            String  PatientHeight =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.PatientHeight));
            String  PatientWeight =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.PatientWeight));
            String  SampleType =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleType));
            String  DateAnalysed =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.DateAnalysed));
            String  PositivityResult =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.PositivityResult));
            String  ObjectsDetected =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.ObjectsDetected));
            String  FilePath =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.FilePath));
            String  ImageNumber =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.ImageNumber));
            String  SampleWeight =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.SampleWeight));
            String  EggsPerGram =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.EggsPerGram));
            String  EggCount =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.EggCount));
            String  EffectivePixelSize =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.EffectivePixelSize));
            String  OpticalMagnification =cursor.getString(cursor.getColumnIndex(sqlDatabaseHelper.OpticalMagnification));

            imageMap.put("SampleNumber", SampleNumber);
            imageMap.put("UUID", UUID);
            imageMap.put("DateCreated", DateCreated);
            imageMap.put("LocationCreated", LocationCreated);
            imageMap.put("Latitude", Latitude);
            imageMap.put("Longitude", Longitude);
            imageMap.put("PatientHeight", PatientHeight);
            imageMap.put("PatientWeight", PatientWeight);
            imageMap.put("SampleType", SampleType);
            imageMap.put("DateAnalysed", DateAnalysed);
            imageMap.put("PositivityResult", PositivityResult);
            imageMap.put("ObjectsDetected", ObjectsDetected);
            imageMap.put("FilePath", FilePath);
            imageMap.put("ImageNumber", ImageNumber);
            imageMap.put("SampleWeight", SampleWeight);
            imageMap.put("EggsPerGram", EggsPerGram);
            imageMap.put("EggCount", EggCount);
            imageMap.put("EffectivePixelSize", EffectivePixelSize);
            imageMap.put("OpticalMagnification", OpticalMagnification);

            imageMapArray.add(imageMap);
        }
        return imageMapArray;
    }



    //SQLDatabaseHelper class helps us store our information in a table

    class SQLDatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "smartmicroscope10";
        private  static final String TABLE_NAME = "scannedImages";
        private   int DATABASE_Version = 1;
        private   String UID ="_id";     // Column I (List ID)
        private   String UUID ="UUID";
        private  static final String SampleNumber ="SampleNumber";
        private  static final String ImageNumber ="ImageNumber";
        private  static final String SampleWeight ="SampleWeight";
        private  static final String EggsPerGram ="EggsPerGram";
        private   String FilePath ="FilePath";
        private   String FileUri = "FileUri";
        private   String DateCreated ="DateCreated";
        private   String LocationCreated ="LocationCreated";
        private   String Latitude ="Latitude";
        private   String Longitude ="Longitude";
        private   String ObjectsDetected ="ObjectsDetected";
        private   String PositivityResult ="PositivityResult";
        private   String DateAnalysed ="DateAnalysed";
        private   String SampleType ="SampleType";
        private   String TechnicianUsername ="TechnicianUsername";
        private   String PatientWeight ="PatientWeight";
        private   String PatientHeight ="PatientHeight";
        private   String EggCount ="EggCount";
        private String OpticalMagnification = "OpticalMagnification";
        private   String EffectivePixelSize ="EffectivePixelSize";// Last column
        private   String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+ " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+SampleNumber+" VARCHAR(255),"+UUID+" VARCHAR(225),"+FilePath+" VARCHAR(255),"+FileUri+" VARCHAR(255),"+DateCreated+" VARCHAR(255),"+LocationCreated+" VARCHAR(255),"+Latitude+" DOUBLE(3),"+Longitude+" Double(3),"+ObjectsDetected+" VARCHAR(255),"+PositivityResult+" VARCHAR(255),"+DateAnalysed+" VARCHAR(255),"+SampleType+" VARCHAR(255),"+TechnicianUsername+" VARCHAR(255),"+PatientWeight+" VARCHAR(255),"+PatientHeight+" VARCHAR(255),"+ImageNumber+" VARCHAR(255),"+SampleWeight+" VARCHAR(255),"+EggsPerGram+" VARCHAR(255),"+EggCount+" VARCHAR(255), "+EffectivePixelSize+" VARCHAR(255), "+OpticalMagnification+" VARCHAR(255));";
        SQLiteDatabase dbb;

        private String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;
        private Context context;

        public SQLDatabaseHelper(Context context) {//, String uuid, String sampleNumber, String filePath, String fileUri
            super(context, DATABASE_NAME, null, 1);
            dbb = getWritableDatabase();
            this.context = context;

            //UUID = uuid;
            //SampleNumber = sampleNumber;
            //FilePath = filePath;
            //FileUri = fileUri;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) {
                Toast.makeText(context, ""+e, Toast.LENGTH_LONG);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Toast.makeText(context,"SQLOnUpgrade", Toast.LENGTH_SHORT);
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }catch (Exception e) {
                Toast.makeText(context,""+e, Toast.LENGTH_SHORT);
            }
        }

    }
}

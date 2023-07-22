package com.example.android.SmartMicroscope.utilityclasses;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationOfTest extends Activity implements LocationListener {

    Context context;
    LocationManager mLocationManager;
    Location mLocation;
    String mlocationString;
    Double latitude;
    Double longitude;
    int attempts;

    public LocationOfTest(Context mcontext) {
        context = mcontext;
        getLocation();
    }

    public Location getLocation() {
        attempts = 0;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        while (true) {
            //mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mLocation = getLastKnownLocation();
            if (mLocation == null){
                attempts = attempts + 1;
                if (attempts > 50){
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    break;
                }
                continue;

            }
            else {
                break;
            }
        }

        if (mLocation != null && mLocation.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below

        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(context, "Please grant location permissions", Toast.LENGTH_LONG);
                mLocation = null;
            }
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                if (mLocation != null){
                    longitude = mLocation.getLongitude();
                    latitude = mLocation.getLatitude();
                }
            }
            else{
                mLocation = null;
                longitude = null;
                latitude = null;
            }

        }

        return mLocation;
    }
    public void onLocationChanged(Location location) {
        if (location != null) {
            //Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
        }
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public String getLocationNearestCity(){
        String finalAddress = "No location";
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
            String adminArea = address.get(0).getAdminArea();
            String subAdminArea = address.get(0).getSubAdminArea();

            finalAddress = address.get(0).getSubAdminArea();
            if (subAdminArea==null){
                finalAddress = adminArea;
            }
            else{
                finalAddress = subAdminArea + ", " + adminArea;
            }

        } catch (IOException e) {}
        catch (NullPointerException e) {}
        return finalAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}

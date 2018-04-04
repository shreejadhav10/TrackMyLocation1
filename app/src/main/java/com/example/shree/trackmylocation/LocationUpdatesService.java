package com.example.shree.trackmylocation;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.shree.trackmylocation.Model.Trip;
import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.TrackMyLocationContract;
import com.example.shree.trackmylocation.pref.Pref;
import com.facebook.stetho.inspector.database.SqliteDatabaseDriver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocationUpdatesService extends Service {
    private static final String TAG = "LocationUpdatesService";
    private IBinder mIBinder = new LocalBinder();
    private Random mRandom = new Random(1000);
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationUpdateCallbacks activityCallbacks;
    private long mTripId;
    private Trip mTrip;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Pref pref = new Pref(getApplicationContext());
        if (pref.isRecording()) {
            startLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * 10);
        locationRequest.setFastestInterval(1000 * 5);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Toast.makeText(LocationUpdatesService.this, "Update:-" + locationResult.getLastLocation().getLatitude(), Toast.LENGTH_SHORT).show();
            if (activityCallbacks != null) {
                activityCallbacks.onLocationUpdate(locationResult.getLastLocation());
                Observable.just(locationResult.getLocations())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new Consumer<List<Location>>() {
                            @Override
                            public void accept(List<Location> locations) throws Exception {
                                insertLocationDetails(locations);
                                updateTrip();
                            }
                        });
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

        }
    };

    private void updateTrip() {
        String maxSpeed = null, avgSpeed = null;
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().openDatabase();
        String sql = "SELECT MAX(" + TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED + ") as max_speed, AVG(" + TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED + ") as avg_speed "
                + " FROM " + TrackMyLocationContract.TripDetails.TABLE_NAME
                + " WHERE " + TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID + "=" + mTrip._id;
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            maxSpeed = cursor.getString(0);
            avgSpeed = cursor.getString(1);
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_MAX_SPEED, maxSpeed);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_AVG_SPEED, avgSpeed);
        sqliteDatabase.update(TrackMyLocationContract.Trip.TABLE_NAME, contentValues, "_id=" + mTrip._id, null);

        DatabaseManager.getInstance().closeDatabase();
    }

    private void insertLocationDetails(List<Location> locations) {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();
        try {
            ListIterator<Location> itr = locations.listIterator();
            while (itr.hasNext()) {
                Location location = itr.next();
                ContentValues contentValues = new ContentValues();
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID, mTrip._id);
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_LATITUDE, location.getLatitude());
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_LONGITUDE, location.getLongitude());
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED, location.getSpeed());
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_CURRENT_TIME, location.getTime());
                contentValues.put(TrackMyLocationContract.TripDetails.COLUMN_NAME_DISTANCE_COVERED, "");
                long insertId = sqLiteDatabase.insert(TrackMyLocationContract.TripDetails.TABLE_NAME, null, contentValues);
                Log.e(TAG, "insertLocationDetails: " + insertId);
            }
        } finally {
            DatabaseManager.getInstance().closeDatabase();
        }
    }

    public double GetDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);
        // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        // Distance in km
        return d;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mIBinder;

    }

    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationUpdatesService.this;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public int getRandomNumber() {
        return mRandom.nextInt(1);
    }

    public void setTripDetails(Trip trip) {
        mTrip = trip;
    }

    public void startTracking(Trip trip) {
        mTrip = trip;
        startLocationUpdates();
    }

    public void stopTracking() {
        stopLocationUpdates();
    }

    public void pauseTracking() {

    }

    public void setCallbacks(LocationUpdateCallbacks locationUpdateCallbacks) {
        this.activityCallbacks = locationUpdateCallbacks;
    }

    public interface LocationUpdateCallbacks {
        void onStartTracking();

        void onLocationUpdate(Location location);

        void onStopTracking();

        void onError();
    }
}

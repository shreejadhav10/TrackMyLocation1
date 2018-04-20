package com.example.shree.trackmylocation;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.shree.trackmylocation.Model.Trip;
import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.TrackMyLocationContract;
import com.example.shree.trackmylocation.pref.Pref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class LocationUpdatesService extends Service {
    private static final String TAG = "LocationUpdatesService";
    private IBinder mIBinder = new LocalBinder();
    private Random mRandom = new Random(1000);
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationUpdateCallbacks activityCallbacks;
    private long mTripId;
    private Trip mTrip;
    private Disposable mLapsedTimeObservable;
    private Pref mPref;
    private boolean isPaused = false;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = new Pref(getApplicationContext());
        registerReceiver(stopTrackingReceiver,new IntentFilter("ACTION_STOP"));
        registerReceiver(StartstopTrackingReceiver,new IntentFilter("ACTION_START_STOP"));
        if (mPref.isRecording()) {
            getTripDetails();
            startLocationUpdates();
            createLapsedTimer(mTrip.timeElapsed);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mIBinder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
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

    private void createNewTrip() {
        mTrip = new Trip();
        mTrip.tripName = "Trip_" + mPref.getTripNumber();
        mTrip.tripDate = new Date(System.currentTimeMillis());
        mTrip.startTime = new Date(System.currentTimeMillis());
        Date calendar = new Date();
        calendar.setHours(0);
        calendar.setMinutes(0);
        calendar.setSeconds(0);
        mTrip.timeElapsed = calendar;
        mTrip.insertIntoDb();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Toast.makeText(LocationUpdatesService.this, "Update:-" + locationResult.getLastLocation().getLatitude(), Toast.LENGTH_SHORT).show();
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

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

        }
    };

    private void updateTrip() {
        String maxSpeed = null, avgSpeed = null;
        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().openDatabase();
        String sql = "SELECT MAX(CAST(" + TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED + " AS real)" + ") as max_speed, AVG(CAST(" + TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED + " AS real)" + ") as avg_speed "
                + " FROM " + TrackMyLocationContract.TripDetails.TABLE_NAME
                + " WHERE " + TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID + "=" + mTrip._id;
        Cursor cursor = sqliteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            maxSpeed = cursor.getString(0);
            avgSpeed = cursor.getString(1);
        }
        cursor.close();

        String sqlGetLastTwoRecords = "SELECT latitude,longitude  FROM " + TrackMyLocationContract.TripDetails.TABLE_NAME
                + " WHERE " + TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID + "=" + mTrip._id + " ORDER BY _id";
        Cursor cursorDistance = sqliteDatabase.rawQuery(sqlGetLastTwoRecords, null);
        ArrayList<LatLong> latLongList = new ArrayList<>();
        if (cursorDistance.moveToFirst()) {
            do {
                if (cursorDistance.moveToFirst()) {
                    do {
                        LatLong latLong = new LatLong(cursorDistance.getFloat(0), cursorDistance.getFloat(1));
                        latLongList.add(latLong);
                    } while (cursorDistance.moveToNext());
                }
            } while (cursor.moveToNext());
        }
        cursorDistance.close();

        double distance = calculateDistance(latLongList);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_MAX_SPEED, maxSpeed);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_AVG_SPEED, avgSpeed);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_DISTANCE, distance);
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




    private float calculateDistance(ArrayList<LatLong> points) {
        float tempTotalDistance = 0.0f;
        for (int i = 0; i < points.size() - 1; i++) {
            LatLong pointA = points.get(i);
            LatLong pointB = points.get(i + 1);
            float[] results = new float[3];
            Location.distanceBetween(pointA.getLat(), pointA.getLng(), pointB.getLat(), pointB.getLng(), results);
            tempTotalDistance += results[0];
        }
        return tempTotalDistance;
    }

    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationUpdatesService.this;
        }

    }

    private void getTripDetails() {
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();
        String Sql = "SELECT * FROM " + TrackMyLocationContract.Trip.TABLE_NAME
                + " WHERE " + TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME + "='' OR " + TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME + " IS null";
        Cursor cursor = sqLiteDatabase.rawQuery(Sql, null);
        if (cursor.moveToFirst()) {
            mTrip = new Trip();
            mTrip.parseCursor(cursor);
        }
    }

    private void createLapsedTimer(final Date defaultDate) {
        mLapsedTimeObservable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (!isPaused) {
                            defaultDate.setSeconds(defaultDate.getSeconds() + 1);
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                            String strDate = formatter.format(new Date(defaultDate.getTime()));
                            updateElapsedTime(defaultDate);
                            //TODO
                            if (activityCallbacks != null) {
                                activityCallbacks.onLocationUpdate(strDate);
                            } else {
                                showNotification(strDate);
                            }
                        }
                    }
                });
    }

    private void showNotification(String strDate) {
        Intent intent = new Intent(getApplicationContext(), TrackActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1000, intent, FLAG_UPDATE_CURRENT);

        Intent playPauseIntent = new Intent("ACTION_START_STOP");

        Intent stopIntent = new Intent("ACTION_STOP");
        stopIntent.putExtra("action","action_stop");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1000, stopIntent, FLAG_UPDATE_CURRENT);

        int drawableId;
        String playTitle;
        if (!isPaused) {
            drawableId = android.R.drawable.ic_media_pause;
            playTitle = "Pause";
            playPauseIntent.putExtra("action", "action_pause");
        } else {
            drawableId = android.R.drawable.ic_media_play;
            playPauseIntent.putExtra("action", "action_resume");
            playTitle = "Resume";

        }
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1000, playPauseIntent, FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "1")
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setContentTitle("Tracking ...")
                .setContentText(strDate)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(drawableId, playTitle, playPausePendingIntent)
                .addAction(R.drawable.ic_stop_black_24dp, "Stop", stopPendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(1, mBuilder.build());
    }

    private void updateElapsedTime(Date defaultDate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_TIME_ELAPSED, defaultDate.getTime());
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();
        sqLiteDatabase.update(TrackMyLocationContract.Trip.TABLE_NAME, contentValues, "_id=" + mTrip._id, null);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void endTrip() {
        stopLocationUpdates();
        mLapsedTimeObservable.dispose();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME, new Date(System.currentTimeMillis()).getTime());
        SQLiteDatabase sqLiteDatabase = DatabaseManager.getInstance().openDatabase();
        sqLiteDatabase.update(TrackMyLocationContract.Trip.TABLE_NAME, contentValues, "_id=" + mTrip._id, null);
        DatabaseManager.getInstance().closeDatabase();
        mTrip = null;
        mPref.setIsRecording(false);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.cancel(1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stopTrackingReceiver);
        unregisterReceiver(StartstopTrackingReceiver);
    }


    public void startTracking() {
        isPaused = false;
        if (mTrip == null) {
            createNewTrip();
        }
        createLapsedTimer(mTrip.timeElapsed);
        startLocationUpdates();
    }


    public void pauseTracking() {
        isPaused = true;
        mLapsedTimeObservable.dispose();
        mTrip.timeElapsed.setSeconds(mTrip.timeElapsed.getSeconds());
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String strDate = formatter.format(new Date(mTrip.timeElapsed.getTime()));
        showNotification(strDate);
        stopLocationUpdates();
    }

    BroadcastReceiver stopTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("action")) {
                String action = intent.getExtras().getString("action");
                if (TextUtils.equals(action, "action_pause")) {
                    Toast.makeText(context, "On Pause", Toast.LENGTH_SHORT).show();
                    pauseTracking();
                } else if (TextUtils.equals(action, "action_resume")) {
                    Toast.makeText(context, "On Resume", Toast.LENGTH_SHORT).show();
                    startTracking();
                }else if (TextUtils.equals(action,"action_stop")){
                    Toast.makeText(context, "On Stop", Toast.LENGTH_SHORT).show();
                    endTrip();
                }

            }
        }
    };

    BroadcastReceiver StartstopTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("action")) {
                String action = intent.getExtras().getString("action");
                if (TextUtils.equals(action, "action_pause")) {
                    Toast.makeText(context, "On Pause", Toast.LENGTH_SHORT).show();
                    pauseTracking();
                } else if (TextUtils.equals(action, "action_resume")) {
                    Toast.makeText(context, "On Resume", Toast.LENGTH_SHORT).show();
                    startTracking();
                }else if (TextUtils.equals(action,"action_stop")){
                    Toast.makeText(context, "On Stop", Toast.LENGTH_SHORT).show();
                    endTrip();
                }

            }
        }
    };

    public void setCallbacks(LocationUpdateCallbacks locationUpdateCallbacks) {
        this.activityCallbacks = locationUpdateCallbacks;
    }

    public interface LocationUpdateCallbacks {
        void onStartTracking();

        void onLocationUpdate(String elapasedTime);

        void onPauseLocationUpdate();

        void onStopTracking();

        void onError();
    }
}

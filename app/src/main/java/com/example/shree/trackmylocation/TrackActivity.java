package com.example.shree.trackmylocation;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.shree.trackmylocation.Model.Trip;
import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.TrackMyLocationContract;
import com.example.shree.trackmylocation.pref.Pref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TrackActivity extends AppCompatActivity implements LocationUpdatesService.LocationUpdateCallbacks, View.OnClickListener {

    private LocationUpdatesService mUpdatesService;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String TAG = "TrackActivity";
    private FloatingActionButton mFabStart;
    private FloatingActionButton mFabStop;
    private Pref mPref;
    private Trip mTrip;
    private TextView txtElaspsedTime;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, TrackActivity.class);
        return intent;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        mPref=new Pref(this);
        initView();
        getLastKnownLocation();
        Intent intent = new Intent(this, LocationUpdatesService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (mPref.isRecording()){
            getTripDetails();
            setTripDetails();
        }
    }

    private void setTripDetails() {
        mFabStart.setTag("2");
        long elapsedTime=(new Date().getTime()-mTrip.startTime.getTime())/1000;
        Date elapsedDate=new Date(elapsedTime);
        elapsedDate.setYear(0);
        elapsedDate.setMonth(0);
        elapsedDate.setDate(0);
        elapsedDate.setHours(0);
        elapsedDate.setMinutes(0);
        elapsedDate.setSeconds((int)elapsedTime);

        createLapsedTimer(elapsedDate);
    }

    private void getTripDetails() {
        SQLiteDatabase sqLiteDatabase= DatabaseManager.getInstance().openDatabase();
        String Sql="SELECT * FROM "+ TrackMyLocationContract.Trip.TABLE_NAME
                +" WHERE "+TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME+"='' OR "+TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME+" IS null";
        Cursor cursor=sqLiteDatabase.rawQuery(Sql,null);
        if (cursor.moveToFirst()){
            mTrip=new Trip();
            mTrip.parseCursor(cursor);
        }
    }

    private void initView() {
        mFabStart = (FloatingActionButton) findViewById(R.id.fabStart);
        mFabStop = (FloatingActionButton) findViewById(R.id.fabStop);

        mFabStart.setOnClickListener(this);
        mFabStop.setOnClickListener(this);

        if (mPref.isRecording()){
            mFabStart.setImageDrawable(ContextCompat.getDrawable(this,android.R.drawable.ic_media_pause));
        }
        mFabStop.setVisibility(mPref.isRecording()?View.VISIBLE:View.GONE);
        txtElaspsedTime=(TextView) findViewById(R.id.txtElapsedTime);
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    Log.e(TAG, "onSuccess: " + location.toString());
                }
            }
        });
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdatesService = ((LocationUpdatesService.LocalBinder) service).getService();
            mUpdatesService.setCallbacks(TrackActivity.this);
            if (mTrip!=null){
                mUpdatesService.setTripDetails(mTrip);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUpdatesService = null;
        }
    };

    @Override
    public void onStartTracking() {

    }

    @Override
    public void onLocationUpdate(Location location) {
    }

    @Override
    public void onStopTracking() {

    }

    @Override
    public void onError() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabStart) {
            if (mUpdatesService != null) {
                if (TextUtils.equals(mFabStart.getTag().toString(),"1")){
                    mFabStart.setImageDrawable(ContextCompat.getDrawable(v.getContext(),android.R.drawable.ic_media_pause));
                    mFabStop.setVisibility(View.VISIBLE);
                    mFabStart.setTag("2");
                    mPref.setIsRecording(true);
                    if (mTrip==null){
                        createNewTrip();
                    }
                    mUpdatesService.startTracking(mTrip);
                    if (mTrip==null) throw new RuntimeException("Null");
                }else {
                    mUpdatesService.stopTracking();
                    mFabStart.setImageDrawable(ContextCompat.getDrawable(v.getContext(),android.R.drawable.ic_media_play));
                    mFabStart.setTag("1");
                }
            }
        }else if (v.getId()==R.id.fabStop){
            mPref.setIsRecording(false);
            endTrip();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void createNewTrip() {
        mTrip=new Trip();
        mTrip.tripName="Trip_"+mPref.getTripNumber();
        mTrip.tripDate=new Date(System.currentTimeMillis());
        mTrip.startTime=new Date(System.currentTimeMillis());
        mTrip.insertIntoDb();
        Date calendar = new Date();
        calendar.setHours(0);
        calendar.setMinutes(0);
        calendar.setSeconds(0);
        createLapsedTimer(calendar);
    }

    private void endTrip(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME,new Date(System.currentTimeMillis()).getTime());
        SQLiteDatabase sqLiteDatabase=DatabaseManager.getInstance().openDatabase();
        sqLiteDatabase.update(TrackMyLocationContract.Trip.TABLE_NAME,contentValues,"_id="+mTrip._id,null);
        DatabaseManager.getInstance().closeDatabase();
    }

    private void createLapsedTimer(final Date defaultDate){
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(defaultDate);

        io.reactivex.Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        defaultDate.setSeconds(defaultDate.getSeconds()+1);
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        String strDate = formatter.format(new Date(defaultDate.getTime()));
                        txtElaspsedTime.setText(strDate);
                    }
                });
    }
}

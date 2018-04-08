package com.example.shree.trackmylocation;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.shree.trackmylocation.pref.Pref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class TrackActivity extends AppCompatActivity implements LocationUpdatesService.LocationUpdateCallbacks, View.OnClickListener {

    private LocationUpdatesService mUpdatesService;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String TAG = "TrackActivity";
    private FloatingActionButton mFabStart;
    private FloatingActionButton mFabStop;
    private Pref mPref;
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
            mFabStart.setTag("2");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUpdatesService!=null){
            mUpdatesService.setCallbacks(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUpdatesService!=null){
            mUpdatesService.setCallbacks(TrackActivity.this);
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
    public void onLocationUpdate(String elapasedTime ) {
        txtElaspsedTime.setText(elapasedTime);
    }

    @Override
    public void onPauseLocationUpdate() {

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
                    mUpdatesService.startTracking();
                }else {
                    mUpdatesService.pauseTracking();
                    mFabStart.setImageDrawable(ContextCompat.getDrawable(v.getContext(),android.R.drawable.ic_media_play));
                    mFabStart.setTag("1");
                }
            }
        }else if (v.getId()==R.id.fabStop){
            mPref.setIsRecording(false);
            mUpdatesService.endTrip();
            setResult(RESULT_OK);
            finish();
        }
    }


}

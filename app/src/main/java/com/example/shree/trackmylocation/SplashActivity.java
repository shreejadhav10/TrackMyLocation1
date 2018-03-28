package com.example.shree.trackmylocation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.security.Permission;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        String[] permissions={Manifest.permission.ACCESS_FINE_LOCATION};
        int isGrantedFineLocation=ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int isGrantedCoarseLocation=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);
        if (isGrantedFineLocation== PackageManager.PERMISSION_DENIED||isGrantedCoarseLocation==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,permissions,1000);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1000&& grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Error!");
            builder.setMessage("Tracking depends upon location. Please enable location from the settings.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SplashActivity.this.finish();
                }
            });
            builder.create().show();
        }else {
            startActivity(TrackActivity.newInstance(this));
        }
    }
}

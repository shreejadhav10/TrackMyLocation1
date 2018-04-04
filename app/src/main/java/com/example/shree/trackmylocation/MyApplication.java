package com.example.shree.trackmylocation;

import android.app.Application;

import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.MyOpenHelpler;
import com.facebook.stetho.Stetho;

/**
 * Created by Shree on 3/31/2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyOpenHelpler myOpenHelpler=new MyOpenHelpler(getApplicationContext(),"TrackMyLocation.sqlite",null,1);
        DatabaseManager.initializeInstance(myOpenHelpler);
        if(BuildConfig.DEBUG){
            Stetho.initializeWithDefaults(getApplicationContext());
        }
    }
}

package com.example.shree.trackmylocation.pref;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Shree on 4/1/2018.
 */

public class Pref {

    private final SharedPreferences.Editor mEditor;
    private final SharedPreferences mSharedPreferences;

    public Pref(Context context){
        mSharedPreferences=context.getSharedPreferences("TrackLocation",Context.MODE_PRIVATE);
        mEditor=mSharedPreferences.edit();
    }
    public void setIsRecording(boolean isRecording){
        mEditor.putBoolean("IS_RECORDING",isRecording);
        mEditor.commit();
    }

    public boolean isRecording(){
        return mSharedPreferences.getBoolean("IS_RECORDING",false);
    }

    public void setIsRecordingPaused(boolean isRecordingPaused){
        mEditor.putBoolean("IS_RECORDING_PAUSED",isRecordingPaused);
        mEditor.commit();
    }

    public boolean isRecordingPaused(){
        return mSharedPreferences.getBoolean("IS_RECORDING_PAUSED",false);
    }

    public int getTripNumber(){
        int tripNumber =mSharedPreferences.getInt("TRIP_NUMBER",0)+1;
        mEditor.putInt("TRIP_NUMBER",tripNumber);
        mEditor.commit();
        return tripNumber;
    }

}

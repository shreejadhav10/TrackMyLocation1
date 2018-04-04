package com.example.shree.trackmylocation.Model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.TrackMyLocationContract;

import java.util.Date;

/**
 * Created by Shree on 4/1/2018.
 */

public class Trip {
    public long _id;
    public String tripName = "trips";
    public Date tripDate;
    public String distance;
    public String timeElapsed;
    public String maxSpeed;
    public String avgSpeed;
    public Date startTime;
    public Date endTime;

    public void parseCursor(Cursor cursor){
        _id=cursor.getLong(cursor.getColumnIndex(TrackMyLocationContract.Trip._ID));
        tripName=cursor.getString(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_NAME));
        tripDate=new Date(cursor.getLong(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_DATE)));
        distance=cursor.getString(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_DISTANCE));
        timeElapsed=cursor.getString(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_TIME_ELAPSED));
        maxSpeed=cursor.getString(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_MAX_SPEED));
        avgSpeed=cursor.getString(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_AVG_SPEED));
        startTime=new Date(cursor.getLong(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_START_TIME)));
        endTime=new Date(cursor.getLong(cursor.getColumnIndex(TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME)));
    }

    public long insertIntoDb(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_NAME,tripName);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_DATE,tripDate.getTime());
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_DISTANCE,distance);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_TIME_ELAPSED,timeElapsed);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_MAX_SPEED,maxSpeed);
        contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_AVG_SPEED,avgSpeed);
        if (startTime!=null){
            contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_START_TIME,startTime.getTime());
        }
        if (endTime!=null){
            contentValues.put(TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME,endTime.getTime());
        }
        SQLiteDatabase sqLiteDatabase= DatabaseManager.getInstance().openDatabase();
        _id=sqLiteDatabase.replace(TrackMyLocationContract.Trip.TABLE_NAME,null,contentValues);
        return _id;
    }


}

package com.example.shree.trackmylocation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Shree on 3/31/2018.
 */

public class MyOpenHelpler extends SQLiteOpenHelper {

    private static final String PRIMARY_KEY="PRIMARY KEY";
    private static final String VARCHAR="VARCHAR";
    private static final String INTEGER="INTEGER";
    private static final String SEP=" ";
    private static final String COMMA=", ";
    private static final String AUTOINCREMENT = " AUTOINCREMENT ";
    private static final String UNIQUE = " UNIQUE ";

    public final String tripTableSql="CREATE TABLE "+ TrackMyLocationContract.Trip.TABLE_NAME
            +"("+ TrackMyLocationContract.Trip._ID+SEP+INTEGER+SEP+PRIMARY_KEY+SEP+AUTOINCREMENT+UNIQUE+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_NAME+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_TRIP_DATE+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_DISTANCE+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_TIME_ELAPSED+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_MAX_SPEED+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_AVG_SPEED+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_START_TIME+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.Trip.COLUMN_NAME_END_TIME+SEP+VARCHAR
            +")";

    public final String tripDetailsTableSql="CREATE TABLE "+TrackMyLocationContract.TripDetails.TABLE_NAME
            +"("+ TrackMyLocationContract.TripDetails._ID+SEP+INTEGER+SEP+PRIMARY_KEY+SEP+AUTOINCREMENT+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_LATITUDE+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_LONGITUDE+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_SPEED+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_CURRENT_TIME+SEP+VARCHAR+COMMA
            +TrackMyLocationContract.TripDetails.COLUMN_NAME_DISTANCE_COVERED+SEP+VARCHAR
            +")";

    public MyOpenHelpler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(tripTableSql);
        db.execSQL(tripDetailsTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

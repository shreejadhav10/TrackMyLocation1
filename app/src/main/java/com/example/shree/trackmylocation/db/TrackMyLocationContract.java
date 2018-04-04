package com.example.shree.trackmylocation.db;

import android.provider.BaseColumns;


public class TrackMyLocationContract {


    //useful to download Facilities
    public static abstract class Trip implements BaseColumns {
        public static final String TABLE_NAME = "trips";
        public static final String COLUMN_NAME_TRIP_NAME= "trip_name";
        public static final String COLUMN_NAME_TRIP_DATE = "trip_date";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_TIME_ELAPSED = "time_elapsed";
        public static final String COLUMN_NAME_MAX_SPEED = "max_speed";
        public static final String COLUMN_NAME_AVG_SPEED = "avg_speed";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
    }

    //useful to download VendorDetailDepartment. Mapping with DepartmentFacility
    public static abstract class TripDetails implements BaseColumns {
        public static final String TABLE_NAME = "trip_details";
        public static final String COLUMN_NAME_TRIP_ID = "trip_id";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_SPEED = "speed";
        public static final String COLUMN_NAME_CURRENT_TIME = "current_time";
        public static final String COLUMN_NAME_DISTANCE_COVERED = "distance_covered";
    }


}
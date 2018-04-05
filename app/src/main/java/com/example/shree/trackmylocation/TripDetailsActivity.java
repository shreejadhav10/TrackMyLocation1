package com.example.shree.trackmylocation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.shree.trackmylocation.Model.Trip;
import com.example.shree.trackmylocation.db.DatabaseManager;
import com.example.shree.trackmylocation.db.TrackMyLocationContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TripDetailsActivity extends AppCompatActivity {
    private Trip trip;
    private TextView mTxtTitle,
            mTxtTripDate,
            mTxtDistanceCovered,
            mTxtTimeElapsed,
            mTxtMaxSpeed,
            mTxtAvgSpeed,
            mTxtStartTime,
            mTxtEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        initUI();
    }

    private void initUI() {
        trip = (Trip) getIntent().getSerializableExtra("TRIP_DETAILS");

        SQLiteDatabase sqliteDatabase = DatabaseManager.getInstance().openDatabase();
        String sqlGetLatLng = "SELECT latitude,longitude FROM " + TrackMyLocationContract.TripDetails.TABLE_NAME
                + " WHERE " + TrackMyLocationContract.TripDetails.COLUMN_NAME_TRIP_ID + "=" + trip._id;
        Cursor cursorDistance = sqliteDatabase.rawQuery(sqlGetLatLng, null);

        ArrayList<LatLong> latLongList = new ArrayList<>();
        if (cursorDistance.moveToFirst()) {
            do {

                LatLong latLong = new LatLong(cursorDistance.getFloat(0), cursorDistance.getFloat(1));
                latLongList.add(latLong);
            } while (cursorDistance.moveToNext());
        }

        float calculatedDistance = calculateDistance(latLongList);

        mTxtTitle = (TextView) findViewById(R.id.mTxtTitle);
        mTxtTripDate = (TextView) findViewById(R.id.mTxtTripDate);
        mTxtDistanceCovered = (TextView) findViewById(R.id.mTxtDistanceCovered);
        mTxtTimeElapsed = (TextView) findViewById(R.id.mTxtTimeElapsed);
        mTxtMaxSpeed = (TextView) findViewById(R.id.mTxtMaxSpeed);
        mTxtAvgSpeed = (TextView) findViewById(R.id.mTxtAvgSpeed);
        mTxtStartTime = (TextView) findViewById(R.id.mTxtStartTime);
        mTxtEndTime = (TextView) findViewById(R.id.mTxtEndTime);

        if (trip != null) {
            mTxtTitle.setText(trip.tripName);
            mTxtTripDate.setText(dateFormatter(trip.tripDate));
            mTxtDistanceCovered.setText(getDistanceInKiloMeter(calculatedDistance));
            mTxtTimeElapsed.setText(trip.timeElapsed);
            mTxtMaxSpeed.setText(getSpeedInKMH(trip.maxSpeed));
            mTxtAvgSpeed.setText(getSpeedInKMH(trip.avgSpeed));
            mTxtStartTime.setText(timeFormatter(trip.startTime));
            mTxtEndTime.setText(timeFormatter(trip.endTime));
        }
    }

    private String dateFormatter(Date date) {
        String strDate = "";
        if (date != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                strDate = formatter.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strDate;
    }

    private String timeFormatter(Date date) {
        String strDate = "";
        if (date != null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
                strDate = formatter.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strDate;
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

    private String getSpeedInKMH(String speed) {
        String final_speed_in_kmh = "";
        if (!TextUtils.isEmpty(speed)) {
            try {
                float f_speed = Float.parseFloat(speed);
                f_speed = f_speed * (18 / 5);
                final_speed_in_kmh = f_speed + " Km/h";
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return final_speed_in_kmh;
    }


    private String getDistanceInKiloMeter(float distance) {
        String distance_in_kmh = "";
        distance = distance / 1000;
        distance_in_kmh = distance + " Km";
        return distance_in_kmh;
    }
}

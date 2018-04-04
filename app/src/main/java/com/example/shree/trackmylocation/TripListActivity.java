package com.example.shree.trackmylocation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.shree.trackmylocation.Model.Trip;

public class TripListActivity extends AppCompatActivity implements TripsFragment.OnListFragmentInteractionListener{

    private RecyclerView mRecyclerView;

    public static Intent newInstance(Context context) {
       Intent intent=new Intent(context,TripListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trip List");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(TrackActivity.newInstance(view.getContext()),101);
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.contentFrameTripList, TripsFragment.newInstance(),TripsFragment.class.getSimpleName()).commit();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101&&resultCode==RESULT_OK){
            Toast.makeText(this, "New Trip added successfully", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().findFragmentById(R.id.contentFrameTripList).onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    public void onListFragmentInteraction(Trip item) {

    }
}

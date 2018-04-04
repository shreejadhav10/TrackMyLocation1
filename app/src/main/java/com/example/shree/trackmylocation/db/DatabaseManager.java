package com.example.shree.trackmylocation.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Smruti on 22/8/17.
 * To manage database connections
 */
public class DatabaseManager {

    private static AtomicInteger mOpenCounter=new AtomicInteger();
    private static DatabaseManager instance;
    private static MyOpenHelpler dbHelper;
    private SQLiteDatabase mDatabase;

    private DatabaseManager(){

    }

    public static synchronized void initializeInstance(MyOpenHelpler helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            dbHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        // Logger.printLog("db", "DB open called:-"+mOpenCounter.get());
        if(mOpenCounter.get()<0)mOpenCounter.set(0);

        if(mOpenCounter.incrementAndGet() == 1) {
            mDatabase = dbHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(instance!=null&&mOpenCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }

    public static synchronized MyOpenHelpler getDbHelper() {
        return dbHelper;
    }

    public static void resetDatabaseConnection(){
        mOpenCounter.set(0);
        instance=null;
        dbHelper=null;
    }

}


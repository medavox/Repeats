package com.medavox.repeats.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.medavox.repeats.application.Application;
import com.medavox.repeats.datamodels.CompletedDose;
import com.medavox.repeats.datamodels.IntendedDose;

import java.util.ArrayList;
import java.util.List;

public class BackendHelper extends SQLiteOpenHelper implements Backend {

/**Common column names between both tables*/
    private final String COMMON_COLUMN_QUANTITY = "quantity";
    private final String COMMON_COLUMN_DOSEID = "dose_id";
    private final String COMMON_COLUMN_ID = "id";

    /**
     * Table name and column names for doses_intended table
     */
    private final String DOSES_INTENDED_TABLE_NAME = "doses_intended";
    private final String DOSES_INTENDED_COLUMN_STARTTIME = "time_start";
    private final String DOSES_INTENDED_COLUMN_ENDTIME = "time_end";
    private final String DOSES_INTENDED_COLUMN_DUETIME = "time_due";

    private static BackendHelper helper;
    private static SQLiteDatabase writableDB;
    private static SQLiteDatabase readableDB;

    /**
     * Single access for database connection
     * @param context
     * @return
     */
    public static synchronized Backend getInstance(Context context)
    {
        if(helper == null)
        {
            helper = new BackendHelper(context);
            //EventBus.getDefault().register(helper);
        }

        if(writableDB == null)
        {
            writableDB = helper.getWritableDatabase();
        }

        if(readableDB == null)
        {
            readableDB = helper.getReadableDatabase();
        }

        return helper;
    }


    private BackendHelper(Context context) {
        super(context, Application.DATABASE_NAME, null, Application.DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableDosesIntended = "create table if not exists " + DOSES_INTENDED_TABLE_NAME + "( " +
                COMMON_COLUMN_ID + "  integer primary key autoincrement, " +
                DOSES_INTENDED_COLUMN_STARTTIME+" long not null," +
                DOSES_INTENDED_COLUMN_DUETIME + " long, " +//todo: do the bind null thing when needed
                DOSES_INTENDED_COLUMN_ENDTIME + " long not null, " +
                COMMON_COLUMN_QUANTITY + " int not null, " +
                COMMON_COLUMN_DOSEID + " int not null" +
                ");";

        Log.d("SQL", createTableDosesIntended);
        SQLiteStatement stmtDoseIntend = db.compileStatement(createTableDosesIntended);
        stmtDoseIntend.execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DOSES_INTENDED_TABLE_NAME);
        onCreate(db);

    }

    public int getIntendedDoseCount(){
        return getNumberOfDoses(DOSES_INTENDED_TABLE_NAME);
    }


    /**
     * Get count of all rows in table
     * @param tableName
     * Table to query
     * @return
     * Int of number of rows
     */
    private int getNumberOfDoses(String tableName) {

        String[] columnsToReturn = {"id"};
        SQLiteDatabase db = readableDB;
        Cursor dbCursor = db.query(tableName, columnsToReturn, null, null, null, null, null);
        int count = dbCursor.getCount();
        dbCursor.close();
        return count;
    }

    /***********************************************************************************************
     *
     *  CRUD functions for backend
     */

    public void addIntendedDose(IntendedDose iDose) {
        addIntendedDose(iDose, null);
    }

    /**
     * Insert a single IntendedDose row into the IntendedDose table
     * @param intendedDose
     * IntendedDose object to insert
     */
    private void addIntendedDose(IntendedDose intendedDose, SQLiteDatabase database) {
        SQLiteDatabase db = (database == null ? writableDB : database);
        String sql = "INSERT INTO " + DOSES_INTENDED_TABLE_NAME +
                " ("  + DOSES_INTENDED_COLUMN_STARTTIME +
        " , " + DOSES_INTENDED_COLUMN_ENDTIME +
        " , " + DOSES_INTENDED_COLUMN_DUETIME +
        " , " + COMMON_COLUMN_QUANTITY +
        " , " + COMMON_COLUMN_DOSEID
                + ") VALUES (?, ?, ?, ?, ?)";

        SQLiteStatement statement = db.compileStatement(sql);
        int i = 1;
        Log.i("insertIntendedDose", "timeStart: "+intendedDose.getTimeStart());
        statement.bindLong(i++, intendedDose.getTimeStart());// These match to the two question marks in the sql string
        statement.bindLong(i++, intendedDose.getTimeEnd());
        statement.bindLong(i++, intendedDose.getTimeDue());
        statement.bindDouble(i++, intendedDose.getQuantity());
        statement.bindDouble(i++, intendedDose.getDoseID());

        statement.executeInsert();
    }

    /**
     * Bulk insert of IntendedDose via a list
     * @param intendedDoses
     * List of IntendedDoses
     */
    public void addIntendedDoseList(List<IntendedDose> intendedDoses) {
        SQLiteDatabase db = writableDB;
        for(IntendedDose dose : intendedDoses) {
            addIntendedDose(dose, db);
        }
    }

    @Override
    public void addIntendedDoses(IntendedDose[] intendedDoses) {
        SQLiteDatabase db = writableDB;
        for(IntendedDose dose : intendedDoses) {
            addIntendedDose(dose, db);
        }
    }

    @Override
    public boolean hasNextDueDose() {
        return (getNextDueDose() != null);
    }

    @Override
    public boolean hasPlan() {
        return getIntendedDoseCount() > 0;
    }


    @Override
    public int deletePlan() {
        SQLiteDatabase db = readableDB;//does this need to be writable in order to delete rows?
        return db.delete(DOSES_INTENDED_TABLE_NAME, null, null);//todo:consider completed doses and plans architecture
    }

    /**
     * Get the next dose in the user's schedule
     * @return
     * IntendedDose object
     */
    /**Get the IntendedDose
     * whose EndTime has not passed,
     * which has the earliest start time,
     * and does not have a corresponding CompletedDose*/
    @Override
    public IntendedDose getNextDueDose() {
        IntendedDose iDose = null;
        String[] columnNames = {COMMON_COLUMN_DOSEID,
                COMMON_COLUMN_QUANTITY,
                DOSES_INTENDED_COLUMN_STARTTIME,
                DOSES_INTENDED_COLUMN_DUETIME,
                DOSES_INTENDED_COLUMN_ENDTIME};
        String[] selectionArg = {""+System.currentTimeMillis()};

        Cursor cursor = readableDB.query(DOSES_INTENDED_TABLE_NAME,
                columnNames,
                DOSES_INTENDED_COLUMN_ENDTIME+" > ?",//WHERE clause
                selectionArg,
                null,
                null,
                DOSES_INTENDED_COLUMN_STARTTIME+" ASC");//ORDER BY clause

        int startTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_STARTTIME);
        int endTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_ENDTIME);
        int dueTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_DUETIME);
        int quantityIndex = cursor.getColumnIndex(COMMON_COLUMN_QUANTITY);
        int doseIDIndex = cursor.getColumnIndex(COMMON_COLUMN_DOSEID);


        if(cursor.getCount() > 0) {
            //the first row SHOULD be the IntendedDose with the lowest (longest ago) start time,
            //because we ORDERed BY start time ASCending
            cursor.moveToFirst();

            //check that this IntendedDose doesn't have a corresponding CompletedDose
            //(ie that it's actually a CompletedDose whose IntendedDose.endTime hasn't passed yet)
            int doseID = cursor.getInt(doseIDIndex);
                iDose = new IntendedDose(doseID,
                        cursor.getLong(startTimeIndex),
                        cursor.getLong(endTimeIndex),
                        cursor.getLong(dueTimeIndex),
                        cursor.getInt(quantityIndex));

        }
        return iDose;
    }

    @Override
    public boolean hasIntendedDoseWithId(int id) {
        return hasDoseWithId(id, DOSES_INTENDED_TABLE_NAME);
    }

    private boolean hasDoseWithId(int id, String tableName) {
        String[] columnNames = {COMMON_COLUMN_DOSEID};
        String[] selectionArg = {""+id};
        Cursor cursor = readableDB.query(tableName,
                                        columnNames,
                                        COMMON_COLUMN_DOSEID+" = ?",
                                        selectionArg,
                                        null,
                                        null,
                                        null);
        return cursor.getCount() > 0;
    }

    /**
     * Return an IntendedDose from the database based on its DoseID
     * @param id
     * DoseID of IntendedDose object to retrieve
     * @return
     * IntendedDose object
     */
    public IntendedDose getIntendedDoseById(int id) {
        String selection = COMMON_COLUMN_DOSEID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor dbCursor = readableDB.query(DOSES_INTENDED_TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        int cursor_count = dbCursor.getCount();
        IntendedDose intendedDose = null;
        if(cursor_count>0) {
            if (dbCursor.moveToFirst()) {
                intendedDose = new IntendedDose(/*time_start*/dbCursor.getInt(5), dbCursor.getLong(1),
                        /*time_end*/dbCursor.getLong(2), /*time_due*/dbCursor.getLong(3),
                        /*quantity*/dbCursor.getInt(4) /*dose_id*/);
            }
        }
        dbCursor.close();
        return intendedDose;
    }
    @Override
    public List<IntendedDose> getAllIntendedDoses() {
        return getAllIntendedDoses(false);
    }

    public List<IntendedDose> getAllIntendedDoses(boolean excludeCompleted) {
        String[] columnsToReturn = {DOSES_INTENDED_COLUMN_STARTTIME,
                                    DOSES_INTENDED_COLUMN_ENDTIME,
                                    DOSES_INTENDED_COLUMN_DUETIME,
                COMMON_COLUMN_QUANTITY,
                COMMON_COLUMN_DOSEID};
        Cursor cursor = readableDB.query(DOSES_INTENDED_TABLE_NAME, columnsToReturn, null, null, null, null, null);
        int count = cursor.getCount();

        List<IntendedDose> doses = new ArrayList<IntendedDose>(count);

        if(count <= 0) {
            return doses;
        }

        int startTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_STARTTIME);
        int endTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_ENDTIME);
        int dueTimeIndex = cursor.getColumnIndex(DOSES_INTENDED_COLUMN_DUETIME);
        int quantityIndex = cursor.getColumnIndex(COMMON_COLUMN_QUANTITY);
        int doseIDIndex = cursor.getColumnIndex(COMMON_COLUMN_DOSEID);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int doseID = cursor.getInt(doseIDIndex);

            //only include IntendedDoses which don't have a corresponding CompeltedDose, if includeCompleted is false
            if(excludeCompleted) {
                continue;
            }
            long startTime = cursor.getLong(startTimeIndex);
            long endTime = cursor.getLong(endTimeIndex);
            long dueTime = cursor.getLong(dueTimeIndex);
            int quantity = cursor.getInt(quantityIndex);


            doses.add(new IntendedDose(doseID, startTime, endTime, dueTime, quantity));
        }

        cursor.close();
        return doses;
    }


//todo:figure if (and how) we should call this when application finishes (or is paused)
    @Override
    public void close() {
        writableDB.close();
        readableDB.close();
        //EventBus.getDefault().unregister(helper);
        super.close();
    }
}

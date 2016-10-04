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

/**
 * @author jamesburnstone
@date 09/08/2016
 */
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

    /**
     * Table name and column names for doses_completed table
     */
    private final String DOSES_COMPLETED_TABLE_NAME = "doses_completed";
    private final String DOSES_COMPLETED_COLUMN_EFFECTIVEDATE  = "effective_date";
    private final String DOSES_COMPLETED_COLUMN_STATUS = "status";
    private final String DOSES_COMPLETED_COLUMN_ERROR  = "error";

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

        String createTableDosesCompleted = "create table if not exists " + DOSES_COMPLETED_TABLE_NAME + "( " +
                COMMON_COLUMN_ID + "  integer primary key autoincrement, " +
                COMMON_COLUMN_DOSEID + " int not null, " +
                DOSES_COMPLETED_COLUMN_EFFECTIVEDATE + " long not null," +
                COMMON_COLUMN_QUANTITY + " int not null, " +
                DOSES_COMPLETED_COLUMN_STATUS + " varchar(32) not null, " +
                DOSES_COMPLETED_COLUMN_ERROR + " varchar(255) " +//optional
                ");";

        Log.d("SQL", createTableDosesIntended);
        SQLiteStatement stmtDoseComplete = db.compileStatement(createTableDosesCompleted);
        stmtDoseComplete.execute();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DOSES_INTENDED_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DOSES_COMPLETED_TABLE_NAME);

        onCreate(db);

    }

    public int getIntendedDoseCount(){
        return getNumberOfDoses(DOSES_INTENDED_TABLE_NAME);
    }

    public int getCompletedDoseCount(){
        return getNumberOfDoses(DOSES_COMPLETED_TABLE_NAME);
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
    public void addCompletedDoses(CompletedDose[] completedDoses) {
        for(CompletedDose dose : completedDoses) {
            addCompletedDose(dose);
        }
    }

    @Override
    public void addIntendedDoses(IntendedDose[] intendedDoses) {
        SQLiteDatabase db = writableDB;
        for(IntendedDose dose : intendedDoses) {
            addIntendedDose(dose, db);
        }
    }

    /**
     * Insert a completedDose object
     * @param completedDose
     * CompletedDose object to insert */
    //todo? update network platform, when this method is called
    public void addCompletedDose(CompletedDose completedDose) {
        SQLiteDatabase db = writableDB;
        String sql = "INSERT INTO " + DOSES_COMPLETED_TABLE_NAME +
                " (" + COMMON_COLUMN_DOSEID +
                " , " + DOSES_COMPLETED_COLUMN_EFFECTIVEDATE +
                " , " + COMMON_COLUMN_QUANTITY +
                " , " + DOSES_COMPLETED_COLUMN_STATUS +
                " , " + DOSES_COMPLETED_COLUMN_ERROR
                + ") VALUES (?, ?, ?, ?, ?)";

        SQLiteStatement statement = db.compileStatement(sql);
        int i = 1;//sql indices start at 1
        statement.bindDouble(i++, completedDose.getDoseID()); // These match to the question marks in the sql string
        statement.bindLong(i++, completedDose.getEffectiveDate());
        statement.bindDouble(i++, completedDose.getQuantity());
        statement.bindString(i++, completedDose.getStatus());
        if(completedDose.getError() != null) {
            statement.bindString(i++, completedDose.getError());
        }
        else {
            statement.bindNull(i++);
        }
        Log.i("BackendHelper", "inserting "+completedDose);
        statement.executeInsert();
    }

    /**
     * Bulk insert of competedDoses via a list
     * @param completedDoses
     * List of CompletedDoses
     */
    public void addCompletedDoseList(List<CompletedDose> completedDoses) {
        for(CompletedDose dose : completedDoses){
            addCompletedDose(dose);
        }
    }

    /**
     * Return the last completedDose. This will be the previous dose the user interacted with
     * @return
     * CompletedDose object
     */
    public CompletedDose getPreviousDoseCompleted() {
        SQLiteDatabase db = readableDB;
        Cursor dbCursor = db.query(DOSES_COMPLETED_TABLE_NAME, null, null, null, null, null, DOSES_COMPLETED_COLUMN_EFFECTIVEDATE + " DESC", "1");
        int cursor_count = dbCursor.getCount();
        CompletedDose completedDose = null;
        if(cursor_count>0) {
            if (dbCursor.moveToFirst()) {
                completedDose = new CompletedDose(/*dose_id*/dbCursor.getInt(1),
                        /*effectiveDate*/dbCursor.getLong(2), /*quantity*/dbCursor.getInt(3),
                        /*status*/dbCursor.getString(4), /*error*/dbCursor.getString(5));
            }
        }
        dbCursor.close();
        return completedDose;
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
        return db.delete(DOSES_INTENDED_TABLE_NAME, null, null)
                + db.delete(DOSES_COMPLETED_TABLE_NAME, null, null);//todo:consider completed doses and plans architecture
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
            if(!hasCompletedDoseWithId(doseID)) {
                iDose = new IntendedDose(doseID,
                        cursor.getLong(startTimeIndex),
                        cursor.getLong(endTimeIndex),
                        cursor.getLong(dueTimeIndex),
                        cursor.getInt(quantityIndex));
            }

        }
        return iDose;
    }
/*
    public IntendedDose getNextDueDose() {
        IntendedDose intendedDose = null;
        //get dose id of most recent previous dose
        CompletedDose previousDose = getPreviousDoseCompleted();

        int nextDoseId = 1;
        if(previousDose != null){
            int previousDoseId = previousDose.getDoseID();
            nextDoseId = previousDoseId + 1;
        }

        int intendedDoses = getIntendedDoseCount();
        //check intendedDose with id nextDoseId exists.
        if(nextDoseId<=intendedDoses){//assumes dose IDs are incremental from 1
            intendedDose = getIntendedDoseById(nextDoseId);
        }
        return intendedDose;
    }
*/
    @Override
    public boolean hasCompletedDoseWithId(int id) {
        return hasDoseWithId(id, DOSES_COMPLETED_TABLE_NAME);
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
    /**
     * Return a CompletedDose from the database based on its DoseID
     * @param id
     * DoseID of CompletedDose object to retrieve
     * @return
     * CompletedDose object
     */
    public CompletedDose getCompletedDoseById(int id) {
        SQLiteDatabase db = readableDB;
        String selection = COMMON_COLUMN_DOSEID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor dbCursor = db.query(DOSES_COMPLETED_TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        int cursor_count = dbCursor.getCount();
        CompletedDose completedDose = null;
        if(cursor_count>0) {
            if (dbCursor.moveToFirst()) {//todo: retrieve most recent CompletedDose for this DoseID, rather than the first in the table
                completedDose = new CompletedDose(/*dose_id*/dbCursor.getInt(1),
                        /*effectiveDate*/dbCursor.getLong(2), /*quantity*/dbCursor.getInt(3),
                        /*status*/dbCursor.getString(4), /*error*/dbCursor.getString(5));
            }
        }
        dbCursor.close();
        return completedDose;
    }

    @Override
    public List<CompletedDose> getAllCompletedDoses() {
        String[] columnsToReturn = {COMMON_COLUMN_DOSEID,
                COMMON_COLUMN_QUANTITY,
                DOSES_COMPLETED_COLUMN_EFFECTIVEDATE,
                DOSES_COMPLETED_COLUMN_STATUS,
                DOSES_COMPLETED_COLUMN_ERROR};

        Cursor cursor = readableDB.query(DOSES_COMPLETED_TABLE_NAME, columnsToReturn, null, null, null, null, null);
        int count = cursor.getCount();

        List<CompletedDose> doses = new ArrayList<CompletedDose>(count);

        if(count <= 0) {
            return doses;
        }

        int doseIDIndex = cursor.getColumnIndex(COMMON_COLUMN_DOSEID);
        int quantityIndex = cursor.getColumnIndex(COMMON_COLUMN_QUANTITY);
        int effectiveTimeIndex = cursor.getColumnIndex(DOSES_COMPLETED_COLUMN_EFFECTIVEDATE);
        int statusIndex = cursor.getColumnIndex(DOSES_COMPLETED_COLUMN_STATUS);
        int errorIndex = cursor.getColumnIndex(DOSES_COMPLETED_COLUMN_ERROR);


        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int doseID = cursor.getInt(doseIDIndex);
            int quantity = cursor.getInt(quantityIndex);
            long effectiveTime = cursor.getLong(effectiveTimeIndex);
            String status = cursor.getString(statusIndex);
            String errorMessage = cursor.getString(errorIndex);

            doses.add(new CompletedDose(doseID, effectiveTime, quantity, status, errorMessage));
        }

        cursor.close();
        return doses;
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
            if(excludeCompleted && hasCompletedDoseWithId(doseID)) {
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

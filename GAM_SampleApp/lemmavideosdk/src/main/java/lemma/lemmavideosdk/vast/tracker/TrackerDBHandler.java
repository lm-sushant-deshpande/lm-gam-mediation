package lemma.lemmavideosdk.vast.tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TrackerDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "trackers";
    private static final String TABLE_TRACKER = "tracker_queue";

    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";
    private static final String KEY_DIRTY_FLG = "is_dirty";

    public TrackerDBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public TrackerDBHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public static String strJoin(List aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.size(); i < il; i++) {
            if (i > 0) {
                sbStr.append(sSep);
            }
            sbStr.append(aArr.get(i));
        }
        return sbStr.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TRACKERS_TABLE = "CREATE TABLE " + TABLE_TRACKER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_URL + " TEXT,"
                + KEY_DIRTY_FLG + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_TRACKERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // code to add the new entry
    public void add(String trackerUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, trackerUrl);
        values.put(KEY_DIRTY_FLG, 0);

        // Inserting Row
        db.insert(TABLE_TRACKER, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public ArrayList popAll() {

        List<Integer> trackerIdList = new ArrayList<Integer>();

        ArrayList<String> trackerList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TRACKER + " LIMIT 100";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                Integer trackerId = cursor.getInt(0);
                trackerIdList.add(trackerId);

                String trackerUrl = cursor.getString(1);
                trackerList.add(trackerUrl);

            } while (cursor.moveToNext());
        }
        deleteRecords(trackerIdList);
        return trackerList;
    }

    public void deleteRecords(List<Integer> trackerIdList) {
        if (trackerIdList.size() > 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            String whereClause = KEY_ID + " IN (" + strJoin(trackerIdList, ",") + ");";
            db.delete(TABLE_TRACKER, whereClause, null);
            db.close();
        }

    }


}

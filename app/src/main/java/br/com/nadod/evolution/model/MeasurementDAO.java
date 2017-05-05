package br.com.nadod.evolution.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.db.Database;
import br.com.nadod.evolution.utils.Utils;

public class MeasurementDAO {
    private Database dbHelper;
    private SQLiteDatabase db;

    public MeasurementDAO (Context context) { dbHelper = Database.getInstance(context);}

    public Long insert(Measurement measurement)
    {
        Long code = -1L;
        try {
            ContentValues values = new ContentValues();
            values.put("user_uid", measurement.getUser_uid());
            values.put("measure_id", measurement.getMeasure_id());
            values.put("value", measurement.getValue());
            values.put("date", measurement.getDate());

            db = dbHelper.getWritableDatabase();
            code = db.insert(Utils.TABLE_MEASUREMENT, null, values);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return code;
    }

    public Long insertOrReplaceAll(List<Measurement> measurementList)
    {
        Long code = -1L;
        db = dbHelper.getWritableDatabase();
        try {
            for (Measurement measurement : measurementList) {
                ContentValues values = new ContentValues();
                values.put("id", measurement.getId());
                values.put("user_uid", measurement.getUser_uid());
                values.put("measure_id", measurement.getMeasure_id());
                values.put("value", measurement.getValue());
                values.put("date", measurement.getDate());
                code = db.replaceOrThrow(Utils.TABLE_MEASUREMENT, null, values);
            }
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        return code;
    }

    public void insertAll(Map<Integer, List<Measurement>> measurementMap)
    {
//        db = dbHelper.getWritableDatabase();
//        String values = "";
        for (List<Measurement> measurementList : measurementMap.values()) {
            for (Measurement measurement : measurementList) {
                insert(measurement);
//                String value = "(" + measurement.getUser_uid() + "," +
//                        String.valueOf(measurement.getMeasure_id()) + "," +
//                        String.valueOf(measurement.getValue()) + "," +
//                        String.valueOf(measurement.getDate()) + "),";
//                values += value;
            }
        }
//        values = values.substring(0, values.length()-1);
//
//        try {
//            db.execSQL("INSERT INTO " + Utils.TABLE_MEASUREMENT +
//                    " (user_uid, measure_id, value, date) VALUES " + values);
//        } catch (SQLException ex) {
//            Log.e("TAG", String.valueOf(ex));
//        }
    }

    public void updateAll(Map<Integer, List<Measurement>> measurementMap)
    {
        for (List<Measurement> measurementList : measurementMap.values()) {
            for (Measurement measurement : measurementList) {
                update(measurement);
            }
        }
    }

    public int update(Measurement measurement) {
        ContentValues values = new ContentValues();

        values.put("measure_id", measurement.getMeasure_id());
        values.put("value", measurement.getValue());
        values.put("date", measurement.getDate());

        db = dbHelper.getWritableDatabase();
        return db.update(Utils.TABLE_MEASUREMENT, values, "id=?",
                new String[]{String.valueOf(measurement.getId())});
    }

    public int delete(int id) {
        db = dbHelper.getWritableDatabase();
        return db.delete(Utils.TABLE_MEASUREMENT, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Measurement> selectAllByMeasure(int measure_id, String userId){
        List<Measurement> measurementList = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASUREMENT +
                        " WHERE measure_id=? AND user_uid=?;",
                new String[]{String.valueOf(measure_id), userId});

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measurement measurement;
                    do {
                        String user_uid = cursor.getString(cursor.getColumnIndex("user_uid"));
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        int measureId = cursor.getInt(cursor.getColumnIndex("measure_id"));
                        float value = cursor.getFloat(cursor.getColumnIndex("value"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        measurement = new Measurement();
                        measurement.setUser_uid(user_uid);
                        measurement.setId(id);
                        measurement.setMeasureId(measureId);
                        measurement.setValue(value);
                        measurement.setDate(date);
                        measurementList.add(measurement);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return measurementList;
    }

    public HashMap<Integer, Measurement> selectAllByDate(long dateParam, String userId){
        HashMap<Integer, Measurement> measurementList = new HashMap<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASUREMENT +
                        " WHERE date=? AND user_uid=?;",
                new String[]{String.valueOf(dateParam), userId});

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measurement measurement;
                    do {
                        String user_uid = cursor.getString(cursor.getColumnIndex("user_uid"));
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        int measureId = cursor.getInt(cursor.getColumnIndex("measure_id"));
                        float value = cursor.getFloat(cursor.getColumnIndex("value"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        measurement = new Measurement();
                        measurement.setId(id);
                        measurement.setUser_uid(user_uid);
                        measurement.setMeasureId(measureId);
                        measurement.setValue(value);
                        measurement.setDate(date);
                        measurementList.put(measureId, measurement);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return measurementList;
    }

    public List<Measurement> selectAll(String userUid) {
        List<Measurement> measurementList = new ArrayList<>();

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASUREMENT + " WHERE user_uid=?;",
                new String[]{userUid});

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measurement measurement;
                    do {
                        String user_uid = cursor.getString(cursor.getColumnIndex("user_uid"));
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        int measureId = cursor.getInt(cursor.getColumnIndex("measure_id"));
                        float value = cursor.getFloat(cursor.getColumnIndex("value"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        measurement = new Measurement();
                        measurement.setId(id);
                        measurement.setUser_uid(user_uid);
                        measurement.setMeasureId(measureId);
                        measurement.setValue(value);
                        measurement.setDate(date);
                        measurementList.add(measurement);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return measurementList;
    }
}

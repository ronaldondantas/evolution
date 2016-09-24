package br.com.nadod.evolution.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
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
        Long code;
        try {
            ContentValues values = new ContentValues();
            values.put("measure_id", measurement.getMeasure_id());
            values.put("value", measurement.getValue());
            values.put("date", measurement.getDate());

            db = dbHelper.getWritableDatabase();
            code = db.insert(Utils.TABLE_MEASUREMENT, null, values);
        } finally {
            db.close();
        }
        return code;
    }

    public void insertAll(Map<Integer, List<Measurement>> measurementMap)
    {
        db = dbHelper.getWritableDatabase();
        String values = "";
        for (List<Measurement> measurementList : measurementMap.values()) {
            for (Measurement measurement : measurementList) {
                String value = "(" + String.valueOf(measurement.getMeasure_id()) + "," +
                        String.valueOf(measurement.getValue()) + "," +
                        String.valueOf(measurement.getDate()) + "),";
                values += value;
            }
        }
        values = values.substring(0, values.length()-1);

        try {
            db.execSQL("INSERT INTO " + Utils.TABLE_MEASUREMENT +
                    " (measure_id, value, date) VALUES " + values);
        } catch (SQLException ex) {
            Log.e("TAG", String.valueOf(ex));
        } finally {
            db.close();
        }
    }

    public List<Measurement> selectAllByMeasure(int measure_id){
        List<Measurement> measurementList = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASUREMENT +
                " WHERE measure_id=?;", new String[]{String.valueOf(measure_id)});

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measurement measurement;
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        int measureId = cursor.getInt(cursor.getColumnIndex("measure_id"));
                        float value = cursor.getFloat(cursor.getColumnIndex("value"));
                        long date = cursor.getLong(cursor.getColumnIndex("date"));

                        measurement = new Measurement();
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
}

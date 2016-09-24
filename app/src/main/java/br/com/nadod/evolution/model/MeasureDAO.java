package br.com.nadod.evolution.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import br.com.nadod.evolution.db.Database;
import br.com.nadod.evolution.utils.Utils;

public class MeasureDAO {
    private Database dbHelper;
    private SQLiteDatabase db;

    public MeasureDAO (Context context) {
        dbHelper = Database.getInstance(context);
        Measure measure = new Measure();
        measure.setId(1);
        measure.setName("weight");
        measure.setDescription("Peso");
        if (!exists(measure.getId())) insert(measure);

        measure = new Measure();
        measure.setId(2);
        measure.setName("waist");
        measure.setDescription("Cintura");
        if (!exists(measure.getId())) insert(measure);
    }

    public boolean exists(int id){
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT 1 FROM " + Utils.TABLE_MEASURE + " WHERE id= ?;",
                new String[]{String.valueOf(id)});

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    public Long insert(Measure measure)
    {
        Long code;
        try {
            ContentValues values = new ContentValues();
            values.put("name", measure.getName());
            values.put("description", measure.getDescription());

            db = dbHelper.getWritableDatabase();
            code = db.insert(Utils.TABLE_MEASURE, null, values);
        } finally {
            db.close();
        }
        return code;
    }

    public List<Measure> selectAll(){
        List<Measure> measureList = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASURE, null);

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measure measure;
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String description = cursor.getString(cursor.getColumnIndex("description"));

                        measure = new Measure();
                        measure.setId(id);
                        measure.setName(name);
                        measure.setDescription(description);
                        measureList.add(measure);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return measureList;
    }
}

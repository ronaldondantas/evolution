package br.com.nadod.evolution.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.com.nadod.evolution.activity.MeasurementActivity;
import br.com.nadod.evolution.db.Database;
import br.com.nadod.evolution.singleton.UserSingleton;
import br.com.nadod.evolution.utils.EvolutionFirebase;
import br.com.nadod.evolution.utils.Utils;

public class MeasureDAO {
    private Database dbHelper;
    private SQLiteDatabase db;

    public MeasureDAO(Context context) {
        dbHelper = Database.getInstance(context);

        boolean insertOk = true;

        List<Measure> measureList = new ArrayList<>();

        Measure measure = new Measure();
        measure.setId(1);
        measure.setName("weight");
        measure.setDescription("Peso");
        measure.setUser_uid(UserSingleton.getInstance(context).getUid());
        if (!exists(measure.getId())) insertOk = insert(measure) == -1;

        if (insertOk) {
            measureList.add(measure);

            measure = new Measure();
            measure.setId(2);
            measure.setName("waist");
            measure.setDescription("Cintura");
            measure.setUser_uid(UserSingleton.getInstance(context).getUid());
            if (!exists(measure.getId())) insertOk = insert(measure) == -1;
        }

        if (insertOk) {
            measureList.add(measure);
            EvolutionFirebase.postMeasure(UserSingleton.getInstance(context).getUid(),
                    new Gson().toJson(measureList));
        }
    }

    public boolean exists(int id){
        db = dbHelper.getReadableDatabase();
        boolean exists = false;

        Cursor cursor = db.rawQuery("SELECT 1 FROM " + Utils.TABLE_MEASURE +
                " WHERE id=?;", new String[]{String.valueOf(id)});

        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        return exists;
    }

    public Long insert(Measure measure)
    {
        Long code = -1L;
        try {
            ContentValues values = new ContentValues();
            values.put("user_uid", measure.getUser_uid());
            values.put("name", measure.getName());
            values.put("description", measure.getDescription());

            db = dbHelper.getWritableDatabase();
            code = db.insert(Utils.TABLE_MEASURE, null, values);
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        return code;
    }

    public Long insertOrReplaceAll(List<Measure> measureList)
    {
        Long code = -1L;
        db = dbHelper.getWritableDatabase();
        try {
            for (Measure measure : measureList) {
                ContentValues values = new ContentValues();
                values.put("id", measure.getId());
                values.put("user_uid", measure.getUser_uid());
                values.put("name", measure.getName());
                values.put("description", measure.getDescription());
                code = db.replaceOrThrow(Utils.TABLE_MEASURE, null, values);
            }
        } catch (Exception e){
            Crashlytics.logException(e);
        }
        return code;
    }

    public List<Measure> selectAll(String user_id){
        List<Measure> measureList = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utils.TABLE_MEASURE + " WHERE user_uid=?",
                new String[]{user_id});

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    Measure measure;
                    do {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String description = cursor.getString(cursor.getColumnIndex("description"));
                        String userUid = cursor.getString(cursor.getColumnIndex("user_uid"));

                        measure = new Measure();
                        measure.setUser_uid(userUid);
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

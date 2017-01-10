package br.com.nadod.evolution.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.nadod.evolution.utils.Utils;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "evolution.db";
    private static final int DATABASE_VERSION = 1;

    private static Database database = null;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Utils.TABLE_MEASURE + "(id INTEGER PRIMARY KEY, " +
                "name TEXT, description TEXT);");

        db.execSQL("CREATE TABLE " + Utils.TABLE_MEASUREMENT + "(id INTEGER PRIMARY KEY, " +
                "measure_id INTEGER, value REAL, date INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static Database getInstance(Context ctx) {
        if (database == null) {
            database = new Database(ctx);
        }
        return database;
    }
}
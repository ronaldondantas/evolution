package br.com.nadod.evolution.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.com.nadod.evolution.model.User;

public class EvolutionFirebase {
    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static void postUser(String user_uid, User user) {
        mDatabase.child("users").child(user_uid).setValue(user);
    }

    public static void postMeasure(String user_uid, String value) {
        mDatabase.child("measures").child(user_uid).setValue(value);
    }

    public static void postMeasurement(String user_uid, String value) {
        mDatabase.child("measurements").child(user_uid).setValue(value);
    }
}

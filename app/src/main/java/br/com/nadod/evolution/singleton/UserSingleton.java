package br.com.nadod.evolution.singleton;

import android.content.Context;
import android.content.SharedPreferences;

import br.com.nadod.evolution.R;

public class UserSingleton {
    private static UserSingleton userSingleton = null;
    private String uid = "";
    private Context context;

    public UserSingleton(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        uid = sharedPreferences.getString("UID", "");
    }

    public static UserSingleton getInstance(Context context) {
        if (userSingleton == null) {
            userSingleton = new UserSingleton(context);
        }
        return userSingleton;
    }

    public void setUid(String uid) {
        context.getSharedPreferences(context.getString(R.string.preference_file_key),
                context.MODE_PRIVATE).edit()
                .putString("UID", uid)
                .apply();
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}

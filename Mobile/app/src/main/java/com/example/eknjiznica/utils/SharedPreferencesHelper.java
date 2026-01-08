package com.example.eknjiznica.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.eknjiznica.models.LoginResponse;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "EKnjiznicaPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences prefs;
    private Gson gson;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveLoginResponse(LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, response.getToken());
        editor.putString(KEY_EMAIL, response.getEmail());
        editor.putString(KEY_USER_ID, response.getUserId());
        
        if (response.getRoles() != null) {
            String rolesJson = gson.toJson(response.getRoles());
            editor.putString(KEY_ROLES, rolesJson);
        }
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public List<String> getRoles() {
        String rolesJson = prefs.getString(KEY_ROLES, null);
        if (rolesJson == null) {
            return null;
        }
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(rolesJson, type);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isLibrarian() {
        List<String> roles = getRoles();
        return roles != null && roles.contains("Librarian");
    }

    public boolean isMember() {
        List<String> roles = getRoles();
        return roles != null && roles.contains("Member");
    }

    public void clear() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }
}

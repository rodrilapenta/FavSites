package ar.com.rodrilapenta.multiapptabs.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInAccountCreator;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arielverdugo on 5/7/17.
 */

public class SessionManager {

    private static SharedPreferences prefs;
    private static SessionManager instance;
    private static final String PREFS_NAME = "_SESSION_", EMAIL = "_EMAIL_";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void login(GoogleSignInAccount acct) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userGoogleId", acct.getId());
        editor.putString("userGoogleName", acct.getGivenName());
        editor.putString("userGoogleEmail", acct.getEmail());
        editor.putString("userGooglePhotoUrl", acct.getPhotoUrl().toString());
        editor.apply();
    }

    public Map<String, ?> getLoggedUserData() {
        return prefs.getAll();
    }

    public void logout(Context context) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("userGoogleId");
        editor.remove("userGoogleName");
        editor.remove("userGoogleEmail");
        editor.remove("userGooglePhotoUrl");

        editor.apply();

        ((Activity) context).finish();
    }
}
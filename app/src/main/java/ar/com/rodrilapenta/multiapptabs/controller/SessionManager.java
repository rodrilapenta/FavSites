package ar.com.rodrilapenta.multiapptabs.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ar.com.rodrilapenta.multiapptabs.db.model.WebInstance;

/**
 * Created by arielverdugo on 5/7/17.
 */

public class SessionManager {

    private static SharedPreferences prefs;
    private static SessionManager instance;
    private DatabaseReference databaseReference;
    private static final String PREFS_NAME = "_SESSION_", EMAIL = "_EMAIL_";



    private ChildEventListener webInstancesEventListener;

    public DatabaseReference getUserFirebaseDatabaseStructure() {
        return databaseReference;
    }



    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void login(GoogleSignInAccount acct, final FirebaseDatabase firebaseDatabase) {
        SharedPreferences.Editor editor = prefs.edit();acct.get
        editor.putString("userGoogleId", acct.getId());
        editor.putString("userGoogleName", acct.getGivenName());
        editor.putString("userGoogleEmail", acct.getEmail());
        editor.putString("userGooglePhotoUrl", acct.getPhotoUrl().toString());

        final String ACCOUNT_ID = acct.getId();

        databaseReference = firebaseDatabase.getReference(ACCOUNT_ID);
        if(databaseReference == null) {
            firebaseDatabase.getReference().setValue(ACCOUNT_ID);
            databaseReference = firebaseDatabase.getReference(ACCOUNT_ID);
        }
    }

    public Map<String, ?> getLoggedUserData() {
        return prefs.getAll();
    }

    public void addWebInstance(WebInstance wi) {
        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(wi);
    }

    public void setWebInstancesEventListener(ChildEventListener webInstancesEventListener) {
        this.webInstancesEventListener = webInstancesEventListener;
    }

    public void logout(Context context) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("userGoogleId");
        editor.remove("userGoogleName");
        editor.remove("userGoogleEmail");
        editor.remove("userGooglePhotoUrl");

        ((Activity) context).finish();
    }
}
package ar.com.rodrilapenta.favsites.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import ar.com.rodrilapenta.favsites.db.model.WebInstance;

public class SessionManager {

    private static SharedPreferences prefs;
    private static SessionManager instance;
    private DatabaseReference databaseReference;
    private static final String PREFS_NAME = "_SESSION_", EMAIL = "_EMAIL_";

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

    public Boolean getFingerprintUsage() {
        return prefs.getBoolean("useFingerprint", false);
    }

    public void setFingerprintUsage(Boolean fingerprintUsage) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("useFingerprint", fingerprintUsage);
        editor.commit();
    }

    public void login(GoogleSignInAccount acct, final FirebaseDatabase firebaseDatabase) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userGoogleId", acct.getId());
        editor.putString("userGoogleName", acct.getGivenName());
        editor.putString("userGoogleEmail", acct.getEmail());
        editor.putString("userGooglePhotoUrl", acct.getPhotoUrl().toString());

        editor.apply();

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
        wi.setFirebaseId(key);
        databaseReference.child(key).setValue(wi);
    }

    public void deleteWebInstanceByFireabseId(String firebaseId) {
        if(firebaseId.isEmpty()) {
            throw new RuntimeException("El firebaseId no puede ser vacío");
        }
        databaseReference.child(firebaseId).removeValue();
    }

    public void logout(Context context) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("userGoogleId");
        editor.remove("userGoogleName");
        editor.remove("userGoogleEmail");
        editor.remove("userGooglePhotoUrl");
        editor.remove("useFingerprint");
        databaseReference = null;

        ((Activity) context).finish();
    }
}
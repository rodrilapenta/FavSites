package ar.com.rodrilapenta.favsites.activities;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import ar.com.rodrilapenta.favsites.R;
import ar.com.rodrilapenta.favsites.controller.SessionManager;

public class UserSettingsActivity extends PreferenceActivity {
    private SessionManager sessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_preferences);
        sessionManager = SessionManager.getInstance(this);

        CheckBoxPreference userFingerprintSetting = (CheckBoxPreference)findPreference("userFingerprintSetting");

        FingerprintManager fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        userFingerprintSetting.setEnabled(fingerprintManager.hasEnrolledFingerprints());
        userFingerprintSetting.setChecked(sessionManager.getFingerprintUsage());
        userFingerprintSetting.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                sessionManager.setFingerprintUsage((Boolean) newValue);
                return true;
            }
        });

    }
}
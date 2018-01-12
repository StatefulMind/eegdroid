package de.uni_osnabrueck.traumschreiber.somnium;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * Fragment that manages user preferences
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_SERVER_ADDRESS = "serverAddress";
    public static final String KEY_PREF_GENDER = "gender";
    public static final String KEY_PREF_BIRTH_YEAR = "birth_year";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        //Load the preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);

        // display current preference values in the UI
        EditTextPreference editTextServerCustom = (EditTextPreference) findPreference(KEY_PREF_SERVER_ADDRESS);
        editTextServerCustom.setSummary(editTextServerCustom.getText());
        NumberPickerPreference pickerYear = (NumberPickerPreference) findPreference("birth_year");
        pickerYear.setSummary(String.valueOf(pickerYear.getValue()));

    }

    // Listen for settings changes and update the UI
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_GENDER)) {
            Preference serverAddressPref = findPreference(key);
            //Set UI to display updated summary
            serverAddressPref.setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals(KEY_PREF_BIRTH_YEAR)) {
            Preference birthYearPref = findPreference(key);
            birthYearPref.setSummary(String.valueOf(sharedPreferences.getInt(key, 1980)));
        }

        if (key.equals(KEY_PREF_SERVER_ADDRESS)) {
            Preference serverAddress = findPreference(KEY_PREF_SERVER_ADDRESS);
            serverAddress.setSummary(String.valueOf(sharedPreferences.getString(KEY_PREF_SERVER_ADDRESS, "")));
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Server Address Updated", Toast.LENGTH_LONG);
            toast.show();
            // redraw the UI !
            this.onCreate(null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}

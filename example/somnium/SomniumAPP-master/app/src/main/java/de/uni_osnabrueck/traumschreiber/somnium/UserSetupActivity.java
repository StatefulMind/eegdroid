package de.uni_osnabrueck.traumschreiber.somnium;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Activity that on first launch handles user input for USER INFORMATION and displays some warning
 */

public class UserSetupActivity extends AppCompatActivity {

    static boolean mYearPicked;
    SharedPreferences prefs;
    NumberPicker mNumberPicker;
    boolean mGenderPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);
        // get the preference Manager to later save info
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //set up the Number Picker & set up the listener
        mNumberPicker = (NumberPicker) findViewById(R.id.setupNumberPicker);
        mNumberPicker.setMinValue(1915);
        mNumberPicker.setMaxValue(2015);
        mNumberPicker.setWrapSelectorWheel(true);
        mNumberPicker.setValue(1980);
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                UserSetupActivity.mYearPicked = true;
            }
        });

    }

    //handle the gender UI
    public void onGenderRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        SharedPreferences.Editor editPrefs = prefs.edit();
        //Check which gender one was checked
        switch (view.getId()) {

            case R.id.radioFemale:
                if (checked)

                    editPrefs.putString("gender", "Female");

                break;
            case R.id.radioMale:
                if (checked)

                    editPrefs.putString("gender", "Male");

                break;
            case R.id.radioOther:
                if (checked)

                    editPrefs.putString("gender", "Intersex");

                break;
        }
        //apply changes at communicate that gender was picked
        editPrefs.apply();
        mGenderPicked = true;
    }


    /**
     * confirm data was pressed, check for permissions & if user data was set if so proceed to main
     *
     * @param view
     */
    public void confirmData(View view) {

        //save the year of birth to preferences
        SharedPreferences.Editor editPrefs = prefs.edit();
        editPrefs.putInt("birth_year", mNumberPicker.getValue());
        editPrefs.apply();

        // first time ask for the location permissions
        getPermission();

        // if user data is set proceed to main activity else prompt the user to action
        if (mGenderPicked && mYearPicked) {
            // goto main Activity

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finishAfterTransition();

        } else if (mGenderPicked) {
            Toast.makeText(UserSetupActivity.this, "Please select your year of birth!", Toast.LENGTH_LONG).show();
        } else if (mYearPicked) {
            Toast.makeText(UserSetupActivity.this, "Please select your gender!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(UserSetupActivity.this, "Please select your gender and year of birth!", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * on devices with android > 6.0 check and get the location permissions which are necessary for
     * BLE use
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void getPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "This permission is needed to use BLE!", Toast.LENGTH_LONG).show();
            }

            // showing the request to the user
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // check what the user did with the request
        if (requestCode == 1) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Permission denied - BLE search disabled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

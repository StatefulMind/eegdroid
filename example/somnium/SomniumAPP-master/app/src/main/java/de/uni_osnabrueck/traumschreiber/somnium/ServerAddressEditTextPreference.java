package de.uni_osnabrueck.traumschreiber.somnium;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Customizes the serverAddress EditTextPreference inside the Preferences such that it can be reset
 */
public class ServerAddressEditTextPreference extends EditTextPreference {

    public ServerAddressEditTextPreference(Context context) {
        super(context);
    }

    public ServerAddressEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ServerAddressEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Handel's the cancel button press to reset server address
     *
     * @param dialog
     * @param which
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {

            case DialogInterface.BUTTON_NEGATIVE:
                SharedPreferences sharedPreferences = getSharedPreferences();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("serverAddress", "https://www.traumschreiber.uni-osnabrueck.de");
                editor.apply();
                break;
        }
        super.onClick(dialog, which);
    }

}

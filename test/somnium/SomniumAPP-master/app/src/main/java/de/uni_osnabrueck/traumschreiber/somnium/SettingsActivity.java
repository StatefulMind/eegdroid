package de.uni_osnabrueck.traumschreiber.somnium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Underlying Activity for the Settings Fragment
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Find the toolbar view in the activity's layout xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.SettingsToolbar);
        // Set the Toolbar as ActionBar for this activity
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);
        //display back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_frame, new SettingsFragment())
                .commit();
    }


    // helpful to properly show back transition animation
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Reverses the Activity Scene entry Transition and triggers the calling Activity to reverse its exit Transition
                finishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

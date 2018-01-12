package de.uni_osnabrueck.traumschreiber.somnium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * General ABOUT Activity to display information to the user.
 * UI hookups and GUI handling
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_alp);

        // making the link inside the TextView work
        TextView mLink = (TextView) findViewById(R.id.uniLink);
        if (mLink != null) {
            mLink.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // Find the toolbar view in the activity's layout xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.aboutToolbar);
        // Set the Toolbar as ActionBar for this activity
        toolbar.setTitle("About");
        setSupportActionBar(toolbar);
        //display back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Display current app version in this Activity.
        TextView versionNumber = (TextView) findViewById(R.id.appVersionNumber);
        versionNumber.setText("V " + BuildConfig.VERSION_NAME);

    }

    // necessary to properly show back transition animation in toolbar
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

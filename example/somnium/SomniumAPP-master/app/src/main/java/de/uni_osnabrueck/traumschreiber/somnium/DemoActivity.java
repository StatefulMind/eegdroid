package de.uni_osnabrueck.traumschreiber.somnium;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class DemoActivity extends AppCompatActivity {

    /**
     * This Activity/Class is intended to provide the functionality to test the real Traumschreiber in future releases.
     * i.e. blinking LED's
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        // Find the toolbar view in the activity's layout xml file
        Toolbar toolbar = (Toolbar) findViewById(R.id.demoToolbar);
        // Set the Toolbar as ActionBar for this activity
        toolbar.setTitle("Traumschreiber Demo");
        setSupportActionBar(toolbar);
        //display back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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

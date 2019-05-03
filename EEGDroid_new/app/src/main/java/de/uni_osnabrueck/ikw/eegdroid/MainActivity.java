package de.uni_osnabrueck.ikw.eegdroid;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent placeholder;
    private TextView mConnectionState;
    private boolean deviceConnected;
    private static File dirSessions;
    private ManageSessions ManageSessions = new ManageSessions();
    private String nameDir = "/sessions_EEG";
    private Uri dirUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // File object to save the directory to save the EEG recordings
        dirSessions = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + nameDir);
        ManageSessions.createDirectory(dirSessions);
        dirUri = Uri.parse(Environment.getExternalStorageDirectory() + "/sessions_EEG/"); //Uri to open the folder with sessions //ToDo Make relative
        Log.d("Main", dirUri.getPath());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mConnectionState = (TextView) findViewById(R.id.connection_state_main);
        mConnectionState.setText(R.string.no_device);
        deviceConnected = false;

        //Change the button of the menu
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_bluetooth_white_24dp);
        toolbar.setOverflowIcon(drawable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Check which request we're responding to
        if (requestCode == 1200) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected
                placeholder = intent;
                mConnectionState.setText(R.string.device_found);
                deviceConnected = true;
            }
        }
  }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.scan) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            startActivityForResult(intent,1200);
        }
        if (id == R.id.disconnect) {
            //mBluetoothLeService.disconnect(); //FIX THIS
            //mConnected = false; //FIX THIS
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.record) {
            if (deviceConnected == true){
                //Intent intent = new Intent(this, Record.class);
                startActivity(placeholder);
            } else {
                Toast.makeText(this, "Please connect a device first.", Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.display) {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(dirUri, "resource/folder");
            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                startActivity(intent);
            } else {
                intent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.nbu.files"); //We overwrite here the last intent
                intent.setAction("ACTION_VIEW");
                intent.setDataAndType(dirUri, "resource/folder");
                Intent intent1 = Intent.createChooser(intent, "Open With");
                startActivity(intent1);
            }

        } else if (id == R.id.manage) {
            Intent intent = new Intent(this, ManageSessions.class);
            intent.putExtra("dirString", dirSessions.getPath());
            startActivity(intent);

        } else if (id == R.id.tfanalysis) {
            Intent intent = new Intent(this, TFAnalysis.class);
            startActivity(intent);

        } else if (id == R.id.epibot) {
            Intent intent = new Intent(this, Epibot.class);
            startActivity(intent);

        } else if (id == R.id.user_details) {

        } else if (id == R.id.settings) {
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static File getDirSessions() { return dirSessions; }



}
package de.uni_osnabrueck.ikw.eegdroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.uni_osnabrueck.ikw.eegdroid.utilities.SessionAdapter;

public class ManageSessions extends AppCompatActivity {

    //List <Files> to save the current state directory
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String dirSessions;
    private File[] listOfFiles;
    private DividerItemDecoration mDividerItemDecoration;
    private Boolean manageMode = false;
    private View.OnClickListener onClickListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sessions);
        readDirectory(MainActivity.getDirSessions());

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SessionAdapter(listOfFiles, getApplicationContext());
        recyclerView.setAdapter(adapter);

        // Add line between items of RecyclerView
        mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), 1);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        // Receive the directory of the EEG Sessions
        Intent intent = getIntent();
        dirSessions = intent.getExtras().getString("dirString");
        listOfFiles = new File(dirSessions).listFiles();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.send_session:
//                newGame();
                return true;
            case R.id.rename_session:
//                showHelp();
                return true;
            case R.id.delete_session:
//                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createDirectory(File dir){
        if (dir.exists() == false){
            dir.mkdirs(); // creates needed dirs
        }
    }

    //Returns a list of recordings in directory
    public void readDirectory(File dir){
        listOfFiles = dir.listFiles();
        //Add if here?
    }

    public void saveSession(){

    }

    public void openSession(){

    }

    public void renameSession(){

    }

    public void deleteSession(){

    }


}

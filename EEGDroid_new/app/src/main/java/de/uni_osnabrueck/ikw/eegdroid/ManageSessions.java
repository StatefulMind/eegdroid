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

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import de.uni_osnabrueck.ikw.eegdroid.utilities.SessionAdapter;

public class ManageSessions extends AppCompatActivity {

    //List <Files> to save the current state directory
    private RecyclerView recyclerView;
    private SessionAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String dirSessions;
    private ArrayList<File> arrayListOfFiles;
    private DividerItemDecoration mDividerItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sessions);
        readDirectory(MainActivity.getDirSessions());

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SessionAdapter(arrayListOfFiles, getApplicationContext());
        recyclerView.setAdapter(adapter);


        // Add line between items of RecyclerView
        mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), 1);
        recyclerView.addItemDecoration(mDividerItemDecoration);

        // Receive the directory of the EEG Sessions
        Intent intent = getIntent();
        dirSessions = intent.getExtras().getString("dirString");



    }

    @Override
    protected void onResume(){
        super.onResume();
        //while(true){Log.d("status", "onResume");}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        //menu.setGroupVisible(0,false); //Hides the menu while no session is selected
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
//                int position = adapter.getSelectedPos();
//                arrayListOfFiles.get(position).renameTo();

//                showHelp();
                return true;
            case R.id.delete_session:
                int position = adapter.getSelectedPos();
                arrayListOfFiles.get(position).delete();
                arrayListOfFiles.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.resetSelectedPos();
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
        arrayListOfFiles = new ArrayList<>(Arrays.asList(dir.listFiles()));
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

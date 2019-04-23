package de.uni_osnabrueck.ikw.eegdroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import de.uni_osnabrueck.ikw.eegdroid.utilities.SessionAdapter;

public class ManageSessions extends AppCompatActivity {

    //List <Files> to save the current state directory
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> planetList = new ArrayList();
    private String dirSessions;
    private File[] listOfFiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sessions);
        readDirectory(MainActivity.getDirSessions());

        planetList.add("Mercury");
        planetList.add("Venus");

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new SessionAdapter(planetList, getApplicationContext());
        recyclerView.setAdapter(adapter);

        // Receive the directory of the EEG Sessions
        Intent intent = getIntent();
        dirSessions = intent.getExtras().getString("dirString");
        listOfFiles = new File(dirSessions).listFiles();
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

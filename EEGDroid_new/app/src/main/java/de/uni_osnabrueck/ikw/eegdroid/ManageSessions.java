package de.uni_osnabrueck.ikw.eegdroid;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;

public class ManageSessions extends AppCompatActivity {

    //List <Files> to save the current state directory
    private File[] listOfFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_sessions);
        readDirectory(MainActivity.getDirSessions());
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

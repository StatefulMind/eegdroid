package de.uni_osnabrueck.ikw.eegdroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import android.text.TextUtils;

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

        final int position = adapter.getSelectedPos();


        switch (item.getItemId()) {
            case R.id.send_session:
//                newGame();
                return true;
            case R.id.rename_session:

                //Add here dialog to take the new name
                //Prevent someone to call it the same
                //Keep date and time
                final String renamedName;
                String child = Integer.toString(new Random().nextInt(505));
                File newName = new File(dirSessions, "renamed"+child+".csv");
                arrayListOfFiles.get(position).renameTo(newName);
                arrayListOfFiles.set(position, newName);
                adapter.notifyItemChanged(position);
                adapter.resetSelectedPos();
                return true;

            case R.id.delete_session:

                //Handles the Dialog to confirm the file delete
                AlertDialog.Builder alert = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_title)
                        .setMessage(getResources().getString(R.string.confirmation_delete)+ " " + arrayListOfFiles.get(position).getName() + "?" );
                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        arrayListOfFiles.get(position).delete();
                        arrayListOfFiles.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.resetSelectedPos();
                    }
                });
                alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // close dialog
                        dialog.cancel();
                    }
                });
                alert.show();
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

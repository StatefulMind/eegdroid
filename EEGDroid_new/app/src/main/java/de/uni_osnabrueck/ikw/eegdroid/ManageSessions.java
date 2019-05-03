package de.uni_osnabrueck.ikw.eegdroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        final int position = adapter.getSelectedPos();

        //Handles if no session has been selected
        if (position == -1) {
            Toast.makeText(getApplicationContext(), R.string.warning_select_session, Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);

        } else {

            switch (item.getItemId()) {

                case R.id.send_session:

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, TextUtils.concat("EEG Session:", " ", arrayListOfFiles.get(position).getName()));
                    Log.d("Auth", getApplicationContext().getPackageName().toString());
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", arrayListOfFiles.get(position));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    adapter.resetSelectedPos();
                    return true;

                case R.id.rename_session:

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog_string, null);
                    AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
                    alertDialogBuilderUserInput.setView(mView);
                    final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.input_dialog_string_Input);

                    alertDialogBuilderUserInput
                            .setCancelable(false)
                            .setTitle(R.string.rename_title)
                            .setMessage(getResources().getString(R.string.ask_new_name) + " " + arrayListOfFiles.get(position).getName() + ":")
                            .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    File newName = new File(dirSessions, userInputDialogEditText.getText().toString() + ".csv");
                                    //Check if exist another file with this name
                                    if (arrayListOfFiles.contains(newName)) {
                                        Toast.makeText(getApplicationContext(), R.string.warning_rename, Toast.LENGTH_LONG).show();
                                        dialogBox.cancel();
                                        adapter.resetSelectedPos();
                                    } else {
                                        arrayListOfFiles.get(position).renameTo(newName);
                                        arrayListOfFiles.set(position, newName);
                                        adapter.notifyItemChanged(position);
                                        adapter.resetSelectedPos();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                            adapter.resetSelectedPos();
                                        }
                                    });

                    AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                    alertDialogAndroid.show();
                    return true;

                case R.id.delete_session:

                    //Handles the Dialog to confirm the file delete
                    AlertDialog.Builder alert = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_title)
                            .setMessage(getResources().getString(R.string.confirmation_delete) + " " + arrayListOfFiles.get(position).getName() + "?");
                    alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            arrayListOfFiles.get(position).delete();
                            arrayListOfFiles.remove(position);
                            adapter.notifyItemRemoved(position);
                            //adapter.resetSelectedPos();
                        }
                    });
                    alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // close dialog
                            dialog.cancel();
                        }
                    });
                    alert.show();
                    adapter.resetSelectedPos();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    public void createDirectory(File dir){
        if (!dir.exists()){
             dir.mkdirs(); // creates needed dirs
        }
    }

    //Returns a list of recordings in directory
    public void readDirectory(File dir){
        arrayListOfFiles = new ArrayList<>(Arrays.asList(dir.listFiles()));
        //Add if here?
    }


}

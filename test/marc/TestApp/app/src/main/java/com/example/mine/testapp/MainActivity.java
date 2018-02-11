package com.example.mine.testapp;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import android.widget.Toast;
import joinery.DataFrame;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager am = getAssets();
        DataFrame<Object> df;
        try {
            InputStream is = am.open("eeg_data_sample.csv");
            InputStreamReader isr = new InputStreamReader(is);
            // URI format: "android.resource://[package]/[res id]"
            CSVReader reader = new CSVReader(isr);
            List myEntries = reader.readAll();
            df = DataFrame.readCsv(is);
//            Toast.makeText( // Great for displaying error as a notification on the app
//                    this,
//                    df.col(0).toString(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
//            String temp = getResources().openRawResource(R.raw.eeg_data_sample).toString();
            e.printStackTrace();
            Toast.makeText( // Great for displaying error as a notification on the app
                    this,
                    "The specified file was not found.", Toast.LENGTH_SHORT).show();
        }
    }
}

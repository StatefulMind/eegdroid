package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.IOException;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Dataset;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        eeg_data_sample.csv
        ===================
        (Stored on 'raw' folder)
        This data arises from a large study to examine EEG correlates of genetic predisposition
        to alcoholism. It contains measurements from 64 electrodes placed on subject's scalps
        which were sampled at 256 Hz (3.9-msec epoch) for 1 second.
         */
        // Create SparkSession object aka spark
        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL Example")
                .getOrCreate();

        // Create Schema for Row with StructType
        StructType schema = new StructType()
                .add("trial", "int")
                .add("sensor", "string")
                .add("channel", "int")
                .add("voltage", "long");

        // Create dataframe from CSV file and apply schema to it
        Dataset<Row> df = spark.read()
                .option("mode", "DROPMALFORMED")
                .schema(schema)
                .csv("eeg_data_sample.csv");


//        try {
//            // TODO: read eeg_data_sample.txt and assign values as DataPoints
//            DataFrame<Object> df = DataFrame.readCsv(getResources().openRawResource(R.raw.eeg_data_sample));
//        } catch (IOException e) {
//
//        }
    }
}

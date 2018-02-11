package com.example.eegdroid;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import joinery.DataFrame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by mvidal on 04/01/18.
 */

public class LiveView extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_graphview, container, false);
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
//        InputStream is = getResources().openRawResource(R.raw.eeg_data_sample);
//        try {
//            // TODO: read eeg_data_sample.txt and assign values as DataPoints
//            DataFrame<Object> df = DataFrame.readCsv(getResources().openRawResource(R.raw.eeg_data_sample));
//        } catch (IOException e) {
//
//        }
//        ArrayList sensorsValues = new ArrayList();
//        for(int i = 1; i <= 8; i++)
//            sensorsValues.add('s' + i);
        //Generates 10 Random Numbers in the range 1 -20
//        for(int i = 0; i < numbers.length; i++) {
//            numbers[i] = (int)(Math.random()*20 + 1);
//        }//end for loop
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
        return view;
    }
}

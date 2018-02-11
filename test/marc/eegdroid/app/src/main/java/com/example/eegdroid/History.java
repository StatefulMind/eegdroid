package com.example.eegdroid;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;

/**
 * Created by mvidal on 04/01/18.
 */

public class History extends Fragment{

    View myView;
    TextView integerTextView, stringTextView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.content_history, container, false);

        integerTextView = (TextView) getView().findViewById(R.id.textView1);
        stringTextView = (TextView) getView().findViewById(R.id.textView2);

        //Initializing integer array list;
        List<Integer> integerData = new ArrayList<Integer>();

        //Initializing string array list;
        List<String> stringData = new ArrayList<String>();


        //Dynamically adding values to integer array list.

        integerData.add(1);
        integerData.add(2);
        integerData.add(3);
        integerData.add(4);
        integerData.add(5);
        integerData.add(6);
        integerData.add(7);

        //Dynamically adding values to string array list.

        stringData.add("ONE");
        stringData.add("TWO");
        stringData.add("THREE");
        stringData.add("Four");
        stringData.add("Five");
        stringData.add("Six");
        stringData.add("Seven");


        //Printing integer array list values on screen.

        for (int i = 0; i < integerData.size(); i++) {

            integerTextView.setText(integerTextView.getText() + " " + integerData.get(i) + " , ");
        }

        //Printing string array list values on screen.

        for (int i = 0; i < stringData.size(); i++) {

            stringTextView.setText(stringTextView.getText() + stringData.get(i) + " , ");
        }
        return myView;

    }
}

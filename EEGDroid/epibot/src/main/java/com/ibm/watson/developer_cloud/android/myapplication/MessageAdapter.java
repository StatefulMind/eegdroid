package com.ibm.watson.developer_cloud.android.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


public class MessageAdapter extends ArrayAdapter<String[]>  {

    private static final String TAG = "MessageAdapter";

    private Context mContext;
    int mResource;

    public MessageAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String[]> objects) {
        super(context, resource, objects);
        //this.mContext = mContext;
        mContext = context;
        mResource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        String user = getItem(position)[0];
        String msj = getItem(position)[1];


        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvUser = (TextView) convertView.findViewById(R.id.UserTextView);
        TextView tvMsj = (TextView) convertView.findViewById(R.id.MessageTextView);

        tvUser.setText(user);
        tvMsj.setText(msj);

        return convertView;

    }
}

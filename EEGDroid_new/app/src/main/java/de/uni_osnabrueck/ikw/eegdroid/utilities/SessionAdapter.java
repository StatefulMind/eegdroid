package de.uni_osnabrueck.ikw.eegdroid.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

import de.uni_osnabrueck.ikw.eegdroid.R;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.PlanetViewHolder> {

    File[] fileList;

    public SessionAdapter(File[] fileList, Context context) {
        this.fileList = fileList;
    }

    @Override
    public SessionAdapter.PlanetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.session_row,parent,false);
        PlanetViewHolder viewHolder = new PlanetViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SessionAdapter.PlanetViewHolder holder, int position) {

        BasicFileAttributes attrs;
        Path path = fileList[position].toPath();

        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
        }catch(IOException ex) {
            attrs = null;
        }
        holder.name.setText(fileList[position].getName());
        float kbs = attrs.size()/1000;
        holder.kbs.setText(Float.toString(kbs));

        FileTime creationTime = attrs.creationTime();
        creationTime.toInstant();

        String stringCreation = creationTime.toString();

        holder.date.setText(stringCreation.substring(0,10));
        holder.hour.setText(stringCreation.substring(11,19));
    }

    @Override
    public int getItemCount() {
        return fileList.length;
    }

    public static class PlanetViewHolder extends RecyclerView.ViewHolder{
        protected TextView name;
        protected TextView kbs;
        protected TextView date;
        protected TextView hour;

        public PlanetViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.text_id);
            kbs = (TextView) itemView.findViewById(R.id.session_kbs);
            date = (TextView) itemView.findViewById(R.id.session_date);
            hour = (TextView) itemView.findViewById(R.id.session_hour);
        }
    }
}

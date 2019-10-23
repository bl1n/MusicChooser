package com.example.musicchooser;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.AudioHolder> {

    private List<Track> mTracks = new ArrayList<>();


    public void addTrack(Track aTrack){
        mTracks.add(aTrack);
        notifyItemInserted(mTracks.size()-1);
    }

    @NonNull
    @Override
    public AudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int position) {
        Track track = mTracks.get(position);
        ((TextView)holder.itemView.findViewById(R.id.trackName)).setText(track.getName());
        holder.itemView.setOnClickListener(v->{
            Intent intent  = new Intent(holder.itemView.getContext(), AudioPlayerActivity.class);
            intent.putExtra("path", track.getPath());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    class AudioHolder extends RecyclerView.ViewHolder{

        public AudioHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}

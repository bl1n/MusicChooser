package com.example.musicchooser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.AudioHolder> {

    public List<Track> getTracks() {
        return mTracks;
    }

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
            EventBus.getDefault().post(new OpenAudioPlayerEvent(track.getPath()));
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

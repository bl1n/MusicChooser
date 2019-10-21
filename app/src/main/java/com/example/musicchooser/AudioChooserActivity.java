package com.example.musicchooser;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class AudioChooserActivity extends AppCompatActivity {

    public static final int SELECT_MUSIC = 101;
    private static final String TAG = "AudioChooserActivity";

    MediaPlayer mediaPlayer;
    private Uri selectedMusicUri;
    private Button bPlay;
    private Button bPause;
    private Button bStop;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Music"), SELECT_MUSIC);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);


        bStop = findViewById(R.id.bStop);
        bPlay = findViewById(R.id.bPlay);
        bPause = findViewById(R.id.bPause);
        bPause.setEnabled(false);

        bPlay.setOnClickListener(v -> {
            releaseMP();
            if (!mediaPlayer.isPlaying()) {
                if (selectedMusicUri != null) {
                    try {
                        mediaPlayer.setDataSource(getApplicationContext(), selectedMusicUri);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mediaPlayer.start();
            }
            bPlay.setEnabled(false);
            bStop.setEnabled(true);
            bPause.setEnabled(true);
        });
        bPause.setOnClickListener(v -> {
            if(!isPaused){
                mediaPlayer.pause();
                isPaused = true;
                bPause.setText(getResources().getString(R.string.Resume));
            } else {
                mediaPlayer.start();
                isPaused = false;
                bPause.setText(getResources().getString(R.string.Pause));
            }
        });

        bStop.setOnClickListener(v -> {
            mediaPlayer.stop();
            bPlay.setEnabled(true);
            bStop.setEnabled(false);
            bPause.setEnabled(false);

        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_MUSIC) {
                selectedMusicUri = data.getData();
                if (selectedMusicUri != null) {
                    String[] split = selectedMusicUri.toString().split("%2F");
                    String trackName = split[split.length - 1];
                    trackName = trackName.replaceAll("%20", " ");
                    ((TextView) findViewById(R.id.trackName)).setText(trackName);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

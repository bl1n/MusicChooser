package com.example.musicchooser;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {

    private static final String TAG = "AudioPlayerActivity";

    MediaPlayer mediaPlayer;
    private Button bPlay;
    private Button bPause;
    private Button bStop;
    private Button bForward;
    private Button bBackward;

    private boolean isPaused = false;
    private TextView mTrackName;
    private SeekBar mSeekBar;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;
    private File mFile;
    private String mPath;

    public AudioPlayerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        bindAudio();

        Handler handler = new Handler();
        AudioPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    mSeekBar.setProgress((currentPosition * 100) / mediaPlayer.getDuration());
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void init() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mTrackName = findViewById(R.id.trackName);
        bStop = findViewById(R.id.bStop);
        bPlay = findViewById(R.id.bPlay);
        bPause = findViewById(R.id.bPause);
        bPause.setEnabled(false);
        mSeekBar = findViewById(R.id.seekBar);
        bForward = findViewById(R.id.bForw);
        bBackward = findViewById(R.id.bBack);
        mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setAudioProgress(seekBar.getProgress());
            }
        };
    }

    private void setAudioProgress(int progress) {
        if (mediaPlayer != null) {
            int newPosition = (progress * mediaPlayer.getDuration()) / 100;
            mediaPlayer.seekTo(newPosition);
        }
    }

    private void startPlaying(File aFile) {
        try {
            mediaPlayer.setDataSource(aFile.getAbsolutePath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            bPlay.setEnabled(false);
            bStop.setEnabled(true);
            bPause.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bindAudio() {
        mPath = getIntent().getStringExtra("path");
        if (mPath != null) {
            mFile = new File(mPath);
            mTrackName.setText(mFile.getName());
            startPlaying(mFile);
            bPlay.setOnClickListener(v -> startPlaying(mFile));
            bPause.setOnClickListener(v -> {
                if (!isPaused) {
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
                releaseMP();
                startActivity(
                        new Intent(this, ListOfSongsActivity.class)
                );
            });
            bForward.setOnClickListener(v -> {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
            });
            bBackward.setOnClickListener(v -> {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
            });


            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        }

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

    @Override
    protected void onDestroy() {
        releaseMP();
        super.onDestroy();
    }
}

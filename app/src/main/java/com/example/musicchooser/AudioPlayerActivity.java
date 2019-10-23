package com.example.musicchooser;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {

    public static final int SELECT_MUSIC = 101;
    private static final String TAG = "AudioPlayerActivity";

    MediaPlayer mediaPlayer;
    private Uri selectedMusicUri;
    private Button bPlay;
    private Button bPause;
    private Button bStop;
    private Button mForward;
    private Button mBackward;

    private boolean isPaused = false;
    private File file;
    private TextView mTrackName;
    private SeekBar mSeekBar;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;

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
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
    }

    private void setAudioProgress(int progress) {
        if (mediaPlayer != null && selectedMusicUri != null) {
            int newPosition = (progress * mediaPlayer.getDuration()) / 100;
            mediaPlayer.seekTo(newPosition);
        }
    }

    private void bindAudio() {
        String path = getIntent().getStringExtra("path");
        if(path !=null){
            File file = new File(path);
            mTrackName.setText(file.getName());
            try {
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                bPlay.setEnabled(false);
                bStop.setEnabled(true);
                bPause.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bPlay.setOnClickListener(v -> {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(file.getAbsolutePath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        bPlay.setEnabled(false);
                        bStop.setEnabled(true);
                        bPause.setEnabled(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
            });
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
                bPlay.setEnabled(true);
                bStop.setEnabled(false);
                bPause.setEnabled(false);
            });

        }

    }


    @Override
    public void onBackPressed() {
        releaseMP();
        super.onBackPressed();
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

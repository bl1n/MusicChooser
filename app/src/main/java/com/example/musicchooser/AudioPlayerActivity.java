package com.example.musicchooser;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class AudioPlayerActivity extends AppCompatActivity {

    private static final String TAG = "AudioPlayerActivity";
    private static final float SHAKE_TRESHOLD = 1500;

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

    private SensorManager mSensorManager;
    private Sensor mSensorAccelerator;
    private long lastUpdate;
    private float lastX;
    private float lastY;
    private float lastZ;

    private boolean isCovered = false;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: {
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    long currentTime = System.currentTimeMillis();
                    long dt = currentTime - lastUpdate;
                    if (dt > 100) {
                        lastUpdate = currentTime;
                        float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / dt * 10000;
                        if (speed > SHAKE_TRESHOLD) {
                            randomTrack();
                        }
                        lastY = y;
                        lastX = x;
                        lastZ = z;
                    }

                    //если телефон повернут вниз экраном и прижат то  пауза
                    // -8 для неровных поверхностей.
                    if(event.values[2]<-8 && isCovered){
                        mediaPlayer.pause();
                    }

                    //если телефон не прижат, плеер не null, не работает и не нажата пауза, то start
                    if(!isCovered && mediaPlayer != null && !mediaPlayer.isPlaying() && !isPaused ){
                        mediaPlayer.start();
                    }
                    break;
                }
                case Sensor.TYPE_PROXIMITY: {
                    float cm = event.values[0];
                    isCovered = cm == 0;
                    break;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Sensor mProximitySensor;

    public AudioPlayerActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        bindAudio();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorAccelerator = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
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

    private void randomTrack() {
        ArrayList<String> paths = getIntent().getStringArrayListExtra("paths");
        Toast.makeText(this, "shaked", Toast.LENGTH_SHORT).show();

        Toast.makeText(this, "shaked" + paths.size(), Toast.LENGTH_SHORT).show();
        if (paths.size() > 0) {
            Random random = new Random();
//            File file = new File(paths.get(0));
            File file = new File(paths.get(random.nextInt(paths.size() - 1)));
            mTrackName.setText(file.getName());
            releaseMP();
            mediaPlayer = new MediaPlayer();
            startPlaying(file);
            Toast.makeText(this, "shaked", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensorAccelerator, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorEventListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
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

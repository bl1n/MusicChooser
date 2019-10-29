package com.example.musicchooser;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class ListOfSongsActivity extends AppCompatActivity {
    private static final String TAG = "ListOfSongsActivity";
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 101;

    private RecyclerView mRecyclerView;
    private TrackAdapter mTrackAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);

        mRecyclerView  = findViewById(R.id.recycler);
        mTrackAdapter = new TrackAdapter();
        mRecyclerView.setAdapter(mTrackAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (!checkReadExternalPermission())
            requestPermissionForReadExternalStorage();
        else{
            listFilesWithSubFolders(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
        }





    }
    private void requestPermissionForReadExternalStorage() {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(R.string.permissions_request_message)
                .setPositiveButton(R.string.ok, (dialogInterface, i) ->
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_READ_EXTERNAL_STORAGE))
                .create()
                .show();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (permissions[0].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PERMISSION_DENIED) {
                Toast.makeText(this, R.string.back_off, Toast.LENGTH_LONG).show();
                finish();
            }else{

                listFilesWithSubFolders(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
            }
        }
    }
    private boolean checkReadExternalPermission() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void listFilesWithSubFolders(File path) {
        Log.d(TAG, path.getAbsolutePath());
        File root = new File(path.getAbsolutePath());
        File[] list = root.listFiles();
        if (list == null) return;
        for (File f : list) {
            if (f.isDirectory()) {
                listFilesWithSubFolders(f);
            } else {
                String trackName = f.getName().toLowerCase();
                //Вероятно, есть более удобный способ отфильтровать.
                //Стоит покапаться в фрэймфорке.
                if (trackName.contains(".mp3")
                        || trackName.contains(".m4a")
                        || trackName.contains(".wav")
                        || trackName.contains(".flac")
                        || trackName.contains(".ogg")
                )  {
                    mTrackAdapter.addTrack(new Track(f.getName(), f.getAbsolutePath()));
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openTrack(OpenAudioPlayerEvent aEvent){
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        ArrayList<String> paths = new ArrayList<>();
        for(Track track: mTrackAdapter.getTracks()){
            paths.add(track.getPath());
        }
        intent.putStringArrayListExtra("paths", paths);
        intent.putExtra("path", aEvent.getPath());
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}

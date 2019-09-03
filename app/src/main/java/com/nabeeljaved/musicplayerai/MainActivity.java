package com.nabeeljaved.musicplayerai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String[] itemsAll;
    private ListView mSongsList;
    private ArrayList<File> audioSongs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mSongsList = findViewById(R.id.songsList);

        appExternalStoragePermission();

    }

    public void appExternalStoragePermission()
    {
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        displayAudioSongsName();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    public ArrayList<File> readOnlyAudioSongs(File file)
    {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] allFiles = file.listFiles();

        for(File indivisualFile : allFiles)
        {
            if(indivisualFile.isDirectory() && !indivisualFile.isHidden())
            {
                arrayList.addAll(readOnlyAudioSongs(indivisualFile));
            }else{
                if(indivisualFile.getName().endsWith(".mp3") || (indivisualFile.getName().endsWith(".aac")) || (indivisualFile.getName().endsWith(".wav")) || (indivisualFile.getName().endsWith(".wma")))
                {
                    arrayList.add(indivisualFile);
                }
            }
        }

        return arrayList;
    }


    private void displayAudioSongsName()
    {
        audioSongs = readOnlyAudioSongs(Environment.getExternalStorageDirectory());
        itemsAll = new String[audioSongs.size()];

        for(int songCounter=0; songCounter<audioSongs.size(); songCounter++)
        {
            itemsAll[songCounter] = audioSongs.get(songCounter).getName();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, itemsAll);
        mSongsList.setAdapter(arrayAdapter);

        mSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String namesong = mSongsList.getItemAtPosition(position).toString();

                Intent intent = new Intent(MainActivity.this, SmartPlayerActivity.class);
                intent.putExtra("song", audioSongs);
                intent.putExtra("name", namesong);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });

    }


}

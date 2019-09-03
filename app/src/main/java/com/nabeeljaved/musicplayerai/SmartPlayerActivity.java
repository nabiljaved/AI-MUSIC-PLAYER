package com.nabeeljaved.musicplayerai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.SettingInjectorService;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SmartPlayerActivity extends AppCompatActivity {

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    String keeper = "";

    private ImageView pausePlayBtn, nextbtn, previewbtn;
    private TextView songNameText;
    private ImageView imageView;
    private RelativeLayout lowerRelativeLayout;
    private Button voiceEnabledButton;
    private String mode = "ON";

    private MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<File> mysongs;
    private String mysongName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CheckVoiceCommandPermission();

        pausePlayBtn = findViewById(R.id.play_pause_button);
        nextbtn = findViewById(R.id.next_btn);
        previewbtn = findViewById(R.id.previous_btn);
        imageView = findViewById(R.id.logo);


        lowerRelativeLayout = findViewById(R.id.lower);
        voiceEnabledButton= findViewById(R.id.voice_enabled_button);
        songNameText = findViewById(R.id.songName);


        parentRelativeLayout = findViewById(R.id.ParentRelativeLayout);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(SmartPlayerActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        ValidateReceiveValuesStartPlaying();
        imageView.setBackgroundResource(R.drawable.four);



        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle bundle)
            {
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matchesFound != null)
                {
                    if(mode.equals("ON"))
                    {
                        keeper= matchesFound.get(0);

                        if(keeper.equals("stop") || keeper.equals("stop the song"))
                        {
                            playpausesong();
                            Toast.makeText(SmartPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();
                        }else if(keeper.equals("play the song") || (keeper.equals("play")))
                        {
                            playpausesong();
                            Toast.makeText(SmartPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();
                        }else if(keeper.equals("play next song") || (keeper.equals("next")))
                        {
                            playnextSong();
                            Toast.makeText(SmartPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();
                        }else if(keeper.equals("play previous song") || (keeper.equals("previous")))
                        {
                            playPreviousSong();
                            Toast.makeText(SmartPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(SmartPlayerActivity.this, "Invalid Command = ", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });


        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper = "";
                        break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }

                return false;
            }
        });

        voiceEnabledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("ON"))
                {
                    mode = "OFF";
                    voiceEnabledButton.setText("VOICE ENABLED MODE - OFF");
                    lowerRelativeLayout.setVisibility(View.VISIBLE);
                }else{
                        mode = "ON";
                        voiceEnabledButton.setText("VOICE ENABLED MODE - ON");
                        lowerRelativeLayout.setVisibility(View.GONE);

                }
            }
        });


        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playpausesong();
            }
        });

        previewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.getCurrentPosition() > 0)
                {
                    playPreviousSong();
                }
            }
        });

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.getCurrentPosition() > 0)
                {
                    playnextSong();
                }
            }
        });

    }

    private void ValidateReceiveValuesStartPlaying()
    {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mysongs = (ArrayList) bundle.getParcelableArrayList("song");
        mysongName = mysongs.get(position).getName();
        String SongName = intent.getStringExtra("name");
        songNameText.setText(SongName);
        songNameText.setSelected(true);

        position = bundle.getInt("position", 0);
        Uri uri = Uri.parse(mysongs.get(position).toString());

        mediaPlayer = MediaPlayer.create(SmartPlayerActivity.this, uri);
        mediaPlayer.start();
    }

    private void CheckVoiceCommandPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(!(ContextCompat.checkSelfPermission(SmartPlayerActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);


            }


        }
    }

    private void playpausesong()
    {
        imageView.setBackgroundResource(R.drawable.four);
        if(mediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.play);
            mediaPlayer.pause();
        }else{
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.four);
        }
    }

    private void playnextSong()
    {
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.release();

        position = ((position +1)%mysongs.size());

        Uri uri = Uri.parse(mysongs.get(position).toString());
        mediaPlayer = mediaPlayer.create(SmartPlayerActivity.this, uri);

        mysongName = mysongs.get(position).toString();
        songNameText.setText(mysongName);
        mediaPlayer.start();

        if(mediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.pause();
        }
        if(!mediaPlayer.isPlaying())
        {
        pausePlayBtn.setImageResource(R.drawable.pause);
        mediaPlayer.start();
        }
        else{
            pausePlayBtn.setImageResource(R.drawable.play);

            imageView.setBackgroundResource(R.drawable.four);
        }
    }

    private void playPreviousSong()
    {
        mediaPlayer.pause();
        mediaPlayer.stop();
        mediaPlayer.release();

        position = ((position -1) <0 ? (mysongs.size()-1) : (position-1));

        Uri uri = Uri.parse(mysongs.get(position).toString());
        mediaPlayer = mediaPlayer.create(SmartPlayerActivity.this, uri);

        mysongName = mysongs.get(position).toString();
        songNameText.setText(mysongName);
        mediaPlayer.start();

        if(mediaPlayer.isPlaying())
        {
            pausePlayBtn.setImageResource(R.drawable.pause);
            mediaPlayer.pause();
        }
        if(!mediaPlayer.isPlaying())
        {
        pausePlayBtn.setImageResource(R.drawable.pause);
        mediaPlayer.start();
        }
    else{
            pausePlayBtn.setImageResource(R.drawable.play);

            imageView.setBackgroundResource(R.drawable.four);
        }

    }

}

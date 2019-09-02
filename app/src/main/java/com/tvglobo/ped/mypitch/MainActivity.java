package com.tvglobo.ped.mypitch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import com.softmoore.android.graphlib.Function;
import com.softmoore.android.graphlib.Graph;
import com.softmoore.android.graphlib.GraphView;
import com.softmoore.android.graphlib.Label;
import com.softmoore.android.graphlib.Point;

public class MainActivity extends AppCompatActivity {

    AudioDispatcher dispatcher = null;

    String[] notes = {"C2","C#2/Db2","D2","D#2/Eb2","E2","F2","F#2/Gb2","G2","G#2/Ab2","A2","A#2/Bb2","B2","C3","C#3/Db3","D3","D#3/Eb3","E3","F3","F#3/Gb3","G3","G#3/Ab3","A3","A#3/Bb3","B3","C4","C#4/Db4","D4","D#4/Eb4","E4","F4","F#4/Gb4","G4","G#4/Ab4","A4","A#4/Bb4","B4","C5","C#5/Db5","D5","D#5/Eb5","E5","F5","F#5/Gb5","G5","G#5/Ab5","A5","A#5/Bb5","B5"};
    Float[] freqs = {65.41f, 69.30f, 73.42f, 77.78f, 82.41f, 87.31f, 92.50f, 98.00f, 103.83f, 110.00f, 116.54f, 123.47f, 130.81f, 138.59f, 146.83f, 155.56f, 164.81f, 174.61f, 185.00f, 196.00f, 207.65f, 220.00f, 233.08f, 246.94f, 261.63f, 277.18f, 293.66f, 311.13f, 329.63f, 349.23f, 369.99f, 392.00f, 415.30f, 440.00f, 466.16f, 493.88f, 523.25f, 554.37f, 587.33f, 622.25f, 659.25f, 698.46f, 739.99f, 783.99f, 830.61f, 880.00f, 932.33f, 987.77f};
    TextView pitchText;
    TextView noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteText = (TextView) findViewById(R.id.text_view_note);
        pitchText = (TextView) findViewById(R.id.text_view_pitch);

        requestAudioPermissions();
    }
    private void getPitch() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }
    private String searchNote(float freq) {
        if (freq < 63.575 || freq > 1017.135)
            return "";

        for (int i = 1; i < freqs.length-1; ++i) {
            if(freq >= (freqs[i-1] + freqs[i])/2 && freq <= (freqs[i] + freqs[i+1])/2) {
                return notes[i];
            }
        }

        int index = 1;
        if (freq >= 63.575 && freq <= (freqs[index] + freqs[index+1])/2)
            return notes[0];
        index = freqs.length-1;
        if (freq >= (freqs[index-1] + freqs[index])/2 && freq <= 1017.135)
            return notes[index];
        return "";
    }
    public void processPitch(float pitchInHz) {

        pitchText.setText("" + pitchInHz);

        String note = searchNote(pitchInHz);
        if (!note.isEmpty()) {
            noteText.setText(note);
        }
    }

    //Create placeholder for user's consent to record_audio permission.
//This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            getPitch();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    getPitch();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}

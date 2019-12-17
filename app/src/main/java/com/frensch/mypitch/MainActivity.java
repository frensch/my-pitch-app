package com.frensch.mypitch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String NOTE_PITCH = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAudioPermissions();

        ConstraintLayout buttonC2 = findViewById(R.id.note_c2);
        buttonC2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTuneTest("C2");
            }
        });
        ConstraintLayout buttonC3 = findViewById(R.id.note_c3);
        buttonC3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTuneTest("C3");
            }
        });
        ConstraintLayout buttonC4 = findViewById(R.id.note_c4);
        buttonC4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTuneTest("C4");
            }
        });
        ConstraintLayout buttonC5 = findViewById(R.id.note_c5);
        buttonC5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTuneTest("C5");
            }
        });

    }

    private void startTuneTest(String note) {
        Intent intent = new Intent(this, TuneTest.class);
        String message = note;
        intent.putExtra(NOTE_PITCH, message);
        startActivity(intent);
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
            //startTuneTest("c2");
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
                    //startTuneTest();
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

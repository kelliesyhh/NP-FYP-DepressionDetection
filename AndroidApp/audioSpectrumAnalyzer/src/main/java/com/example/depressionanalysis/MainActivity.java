package com.example.depressionanalysis;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.regex.Pattern;

import com.example.R;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

//    private MediaRecorder mRecorder;
//    private static final String LOG_TAG = "AudioRecording";
//    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    Audio_Record recorder1 = new Audio_Record();

    private Button btnSession;
    private Button btnApiTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //START OF FIREBASE INIT
////        FirebaseApp.initializeApp(this);
//
//        // Access a Cloud Firestore instance from your Activity
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // Create a new user with a first and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//        user.put("last", "Lovelace");
//        user.put("born", 1815);
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("FireStore", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("FireStore", "Error adding document", e);
//                    }
//                });
//
//        // Create a new user with a first, middle, and last name
//        user = new HashMap<>();
//        user.put("first", "Alan");
//        user.put("middle", "Mathison");
//        user.put("last", "Turing");
//        user.put("born", 1912);
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("FireStore", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("FireStore", "Error adding document", e);
//                    }
//                });
//
//        db.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d("FireStore", document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.w("FireStore", "Error getting documents.", task.getException());
//                        }
//                    }
//                });
//
//        // END OF FIRESTORE INIT

        btnSession = (Button) findViewById(R.id.btnSession);
        btnSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validation = true;
                EditText txtID = (EditText) findViewById(R.id.txtID);
                EditText txtName = (EditText) findViewById(R.id.txtName);
                TextView lblID = (TextView) findViewById(R.id.lblID);
                TextView lblError = (TextView) findViewById(R.id.lblError);
                TextView lblName = (TextView) findViewById(R.id.lblName);
                String errStr = "";
                Pattern sgpID = Pattern.compile("(?i)^[STFG]\\d{7}[A-Z]$");

                //Validate Name
                if (txtName.getText().toString().isEmpty()) {
                    validation = false;
                    errStr = errStr + "Name cannot be left blank. ";
                    lblName.setTextColor(Color.RED);
                }

                //Validate ID
                if (!sgpID.matcher(txtID.getText()).matches() && !txtID.getText().equals("S1234567A")){
                    validation = false;
                    lblID.setTextColor(Color.RED);
                    errStr = errStr + "Invalid input for ID.";
                }

                //Check Validation
                if (!validation) {
                    lblError.setText(errStr);
                    lblError.setVisibility(View.VISIBLE);
                } else {
                    launchSession(txtName.getText().toString(), txtID.getText().toString(), v);
                }


//                txtID.ErrorEnabled = true;
//                txtID.Error = "Error message.";
            }
        });

        btnApiTest = (Button) findViewById(R.id.btnApiTest);
        btnApiTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(),TestAPI.class));
                }
            });

        //Assigns ToggleButton from View to var toggle
        final ToggleButton toggle = findViewById(R.id.toggleButton);
        toggle.setVisibility(View.INVISIBLE);
        //Assigns ProgressBar from View to var progress
        final ProgressBar progress = findViewById(R.id.progressBar);
        if(!CheckPermissions()){
                //Toast.makeText(getApplicationContext(), "Please enable permissions to continue", Toast.LENGTH_LONG).show();
                toggle.setEnabled(false);
                RequestPermissions();
        } else {
            toggle.setEnabled(true);

//            AudioRecord recorder = new AudioRecord(, );
//            recorder.getAudioSource()

//            MediaRecorder recorder = new MediaRecorder();
//
//            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//            //mFileName = Environment.getExternalStorageDirectory().toString();
//            mFileName += "/AudioRecording.3gp";

            //Listener on the button
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        //Enabled
                        // Shows recording Indicator
                        progress.setVisibility(View.VISIBLE);
                        recorder1.startRecording();
                        //if(CheckPermissions()) {


                        //Recorder
//                        mRecorder = new MediaRecorder();
//                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                        mRecorder.setOutputFile(mFileName);
//                        try {
//                            mRecorder.prepare();
//                        } catch (IOException e) {
//                            Log.e(LOG_TAG, "prepare() failed");
//                        }
//                        mRecorder.start();

                        //Notification to show recording started
                        Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
//                        }
//                    else
//                        {
//                            //RequestPermissions();
//                            progress.setVisibility(View.INVISIBLE);
//                            Toast.makeText(getApplicationContext(), "Please enable permissions to continue", Toast.LENGTH_LONG).show();
//                        }
                    } else {
                        //Disabled

                        //Stops Recording Indicator
                        progress.setVisibility(View.INVISIBLE);

                        recorder1.stopRecording();

                        //Recorder
//                        mRecorder.stop();
//                        mRecorder.release();
//                        mRecorder = null;

                        //Notification to show recording stopped
                        Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void launchSession (String usrName, String usrId, View view) {
        Intent intent = new Intent(this, Recording.class);
        intent.putExtra("userName", usrName);
        intent.putExtra("userID", usrId);
        boolean permissions = CheckPermissions();
        if(!permissions) {
            RequestPermissions();
        } else if (permissions){
            startActivity(intent);
        } else {
            Snackbar.make(view, "Permissions not granted. Please enable permissions and try again.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public boolean CheckPermissions() {
        int resultRW = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int resultRec = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int resultOutputStream = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        boolean value = resultRW == PackageManager.PERMISSION_GRANTED && resultRec == PackageManager.PERMISSION_GRANTED && resultOutputStream == PackageManager.PERMISSION_GRANTED;
        return value;
    }

    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, INTERNET}, REQUEST_AUDIO_PERMISSION_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
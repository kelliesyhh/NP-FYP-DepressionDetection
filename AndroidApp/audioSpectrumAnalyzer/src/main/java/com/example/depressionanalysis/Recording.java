package com.example.depressionanalysis;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import cz.msebera.android.httpclient.Header;
import com.example.R;
import com.example.audioanalyzer.AnalyzerActivity;

public class Recording extends AppCompatActivity {

    Audio_Record recorder = new Audio_Record();
    ConcurrentLinkedQueue<AudioBlock> blockQueue = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<AudioFrame> frameQueue = new ConcurrentLinkedQueue<>();
    //Queue<AudioBlock> blockQueue = new ConcurrentLinkedQueue<>();
    //Queue<AudioFrame> frameQueue = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<AudioFrame> completedFrames = new ConcurrentLinkedQueue<>();
//    SmileJNI testOPS = new SmileJNI();

    boolean end = false;
    boolean updateWave = true;

    private ListView lstResults2;
    public ConcurrentLinkedQueue<JSONObject> myResponses = new ConcurrentLinkedQueue<>();
    private ArrayAdapter<String> listAdapter;  // = new ArrayList<>();


//    Runnable processing = new Runnable() {
//        public void run() {
//            // a potentially time consuming task
//            int blkCounter = 0;
//            while(!blockQueue.isEmpty() || !end) {
////            while (!blockQueue.isEmpty()) {
//                blkCounter++;
//                AudioBlock aBlock = blockQueue.poll();
//                AudioFrame aFrame = new AudioFrame();
//
//                //get current frame
//                if (!frameQueue.isEmpty()) {
//                    aFrame = frameQueue.poll();
//                }
//
//                //if frame full
//                if (!aFrame.addBlock(aBlock)) {
//                    completedFrames.add(aFrame);
//                    AudioFrame newFrame = new AudioFrame(aFrame);
////                    Log.d("procFrame", "New Frame Created");
//                    if(newFrame.addBlock(aBlock)){
////                        Log.d("procFrame", "New Frame Created");
//                    } else {
////                      completedFrames.add(newFrame);
//                        Log.e("procFrame", "Error: New Frame Full.");
//                    }
//                    frameQueue.add(newFrame);
//                } else {
//                    //ADD FRAME BACK INTO QUEUE TO BE PROCESSED
//                    frameQueue.add(aFrame);
//                }
//
//            }
//
//            Log.d("procFrame", "Processing Frames Completed.\nNo. of Blocks in Queue: "+ blockQueue.size() +"\nNo. of Blocks Processed: " + blkCounter + "\nNo. of Frames in Queue: " + frameQueue.size() + "(" + (frameQueue.size() > 0 ? frameQueue.peek().noOfBlocks() : "0") + ")"+ "\nNo. of Completed Frames: " + completedFrames.size() + "\nAccuracy Check: " + (5 + (2 * (completedFrames.size()-1))));
////
//// Log.d("procFrame", "Processing Frames Completed. No. of Blocks Processed: " + blkCounter);
////            Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
////            Toast.makeText(getApplicationContext(),"Processing Frames Completed. No. of Blocks Processed: " + blkCounter, Toast.LENGTH_LONG).show();
////            int endCount = 0;
////            while(!completedFrames.isEmpty()){
////                AudioFrame a = completedFrames.poll();
////                System.out.println(endCount + ": " + a.getContent());
////                endCount++;
////            }
//        }
//    };
//
//    Thread procFrame = new Thread(processing);

    private Thread recordingThread = null;
    int BufferElements2Rec = 1024;
    //    int BufferElements2Rec = 441;
    int BytesPerElement = 2;
    Date now = new Date();
    AmplitudeWaveFormView myWave;
    //    Switch toggleWave;
    Toast genericToast;
    TextView lblResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_recording);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setTitle("Assessment (" + intent.getStringExtra("userID") + ")");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Title and subtitle
//        toolbar.setTitle(R.string.about_toolbar_title);
//        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView lblName = (TextView) findViewById(R.id.lblName);
        TextView lblID = (TextView) findViewById(R.id.lblID);
        TextView lblDate = (TextView) findViewById(R.id.lblDate);
        final TextView lblStartTime = (TextView) findViewById(R.id.lblStartTime);
        TextView lblElapsedTime = (TextView) findViewById(R.id.lblElapsedTime);
        myWave = (AmplitudeWaveFormView) findViewById(R.id.amplitudeWaveFormView);
        lblResults = (TextView) findViewById(R.id.lblResults);
//        lstResults2 = (ListView) findViewById(R.id.lstResults2);
//        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//        toggleWave = (Switch) findViewById(R.id.switchWave);
//        toggleWave.setChecked(updateWave);
//
//        toggleWave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                updateWave = toggleWave.isChecked();
//            }
//        });


        lblName.setText("Name: " + intent.getStringExtra("userName"));
        PredictiveIndex.setUSER(intent.getStringExtra("userID"));
        PredictiveIndex.setSESSION_ID();
        lblID.setText("ID/FIN: " + intent.getStringExtra("userID"));

        String[] month = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String[] day = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String dateStr = day[now.getDay()] + ", " + now.getDate() + " " + month[now.getMonth()] + " " + (now.getYear() + 1900);
        lblDate.setText("Date: " + dateStr);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2pass = new Intent(getApplicationContext(), AnalyzerActivity.class);
                intent2pass.putExtra("bSaveWav", true);
                
                String usrName = getIntent().getStringExtra("userName");
                String usrId = getIntent().getStringExtra("userID");
                intent2pass.putExtra("userName", usrName);
                intent2pass.putExtra("userID", usrId);
                
                startActivity(intent2pass);
                
                if (!recorder.isRecording) {

                    //STARTS THE RECORDING PROCESS

                    //Initialize Queues
//                    blockQueue = new ConcurrentLinkedQueue<>();
//                    frameQueue = new ConcurrentLinkedQueue<>();
//                    completedFrames = new ConcurrentLinkedQueue<>();

                    //Enable Frame Creation
//                    procFrame.start();
                    end = false;

                    fab.setImageResource(R.drawable.ic_pause);
                    //recorder.startRecording();
                    final String filename = getIntent().getStringExtra("userID") + "_" + (now.toGMTString().replaceAll(" ", "_").toLowerCase());


                    recordingThread = new Thread(new Runnable() {
                        public void run() {
                            //processAudioData(myWave, filename);
                            processAudioData(filename);
                        }
                    }, "AudioRecorder Thread");
                    recordingThread.start();

                    // myWave.start();

                  //  String startTime = "" + recorder.startTime.getHours() + ":" + recorder.startTime.getMinutes() + ":" + recorder.startTime.getSeconds();

       //             lblStartTime.setText("Start Time: " + startTime);

                    //Notification to show recording started
                    //Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
                    Snackbar.make(view, "Recording Started", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                } else {

                    //STOPS THE RECORDING PROCESS
                    fab.setImageResource(R.drawable.ic_record_audio);
                    fab.hide();
                    fab.setClickable(false);
                    //recorder.stopRecording();
                    //myWave.stop();
                    end = true;
                    postCall();
                    //Notification to show recording stopped
                    //Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
                    Snackbar.make(view, "Recording Stopped", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button btnSpectrogram = findViewById(R.id.btnSpectrogram);
        btnSpectrogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AnalyzerActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
//        if (recorder.isRecording) {
        new AlertDialog.Builder(this)
//                .setIcon(android.R.drawable.ic_dialog_alert)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Ending Session")
                .setMessage("Are you sure you want to end session for " + getIntent().getStringExtra("userName") + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (recorder.isRecording) {
                            recorder.stopRecording();
                            myWave.stop();
                        }
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
//        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        if(keyCode == KeyEvent.KEYCODE_BACK) {
//            onBackPressed();
//        }
//        return super.onKeyDown(keyCode, event);
//
//    }

    private void updateElapsed(Date startTime) {
        Date currentTime = new Date();
//        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
//        formatter.format(new Date();
        TextView lblElapsedTime = (TextView) findViewById(R.id.lblElapsedTime);
        long offset = currentTime.getTime() - startTime.getTime();

//        long millis = offset % 1000;
        long second = (offset / 1000) % 60;
        long minute = (offset / (1000 * 60)) % 60;
        long hour = (offset / (1000 * 60 * 60)) % 24;

//        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        String time = String.format("%02d:%02d:%02d", hour, minute, second);
        lblElapsedTime.setText("Elapsed Time: " + time);
    }


    /*private void processAudioData(AmplitudeWaveFormView myWave, String filename) {
//        genericToast = Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_LONG);
//        genericToast.show();
        short sData[] = new short[BufferElements2Rec];
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/DepressionAnalysis";
        File projDir = new File(filePath);
        projDir.mkdir();
        filePath += "/" + filename + ".pcm";
        System.out.println("Filepath: " + filePath);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
            System.out.println(os.getChannel().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (recorder.isRecording) {
            double sum = 0;
            updateElapsed(recorder.startTime);
            //gets the voice output from microphone to byte format
            int readSize = recorder.recorder.read(sData, 0, BufferElements2Rec);

            //Create Audio Block
//            AudioBlock newBlock = new AudioBlock(sData);
//            blockQueue.add(newBlock);
//            blockQueue.add(new AudioBlock(sData));

//            myWave.updateAmplitude(50);

            for (int i = 0; i < readSize; i++) {
//                try {
//                    output.writeShort(sData [i]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                sum += sData[i] * sData[i];
            }

            if (readSize > 0 && updateWave == true) {
                final double amplitude = sum / readSize;
                myWave.updateAmplitude((float) (Math.sqrt(amplitude)));
//                myWave.updateAmplitude((float) (amplitude/30));
            }


//            StringBuilder sb = new StringBuilder();
            try {
                byte bData[] = recorder.short2byte(sData);
//                for (byte b : bData) {
//                    sb.append(String.format("%02X", b));
//                }
//                os.write(bData, 0, readSize);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if(recorder.isRecording) {
//                System.out.println(sb.toString());
//            }
        }

        try {
            os.close();
            System.out.println("File saved: " + filePath + "/" + filename);
            PredictiveIndex.setSAVED_FILE(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private void postCall() {
        RequestParams reqParam = new RequestParams();
//        reqParam.add("username", "aaa");
//        reqParam.add("password", "aaa@123");
//        JSONArray myResponse;

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/DepressionAnalysis";
//        filePath += "/S1234567A_26_feb_2019_08:20:00_gmt.pcm";
        filePath = PredictiveIndex.getSAVED_FILE();
        System.out.println(filePath);

        File theFile = new File(filePath);
        try {
            reqParam.put("file", theFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url;
        url = "/users/" + PredictiveIndex.getUSER() + "/analysis/" + PredictiveIndex.getSESSION_ID();
//        url = "uploader";

        restClient.post(url, reqParam, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("API", "Post : " + response);
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
//                    myResponses.add(new JSONArray(response.toString()));
//                    JSONArray myJSONArray = new JSONArray(response.toString());
//                    System.out.println(response.toString());
                    myResponses.add(serverResp);
                    getResponses();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                System.out.println("MyResponse:" + response.toString());
//                ((PredictiveIndex) this.getApplicationContext()).setSomeVariable("foo"); //set
//                String s = ((PredictiveIndex) this.getApplication()).getSomeVariable(); //get
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline
                System.out.println("Status: " + statusCode + "\nHeaders: " + headers + "\nResponse: " + timeline);
//                myResponses.add(timeline);
                getResponses();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String str, Throwable throwable) {
                // Pull out the first event on the public timeline
                System.out.println("Status: " + statusCode + "\nHeaders: " + headers + "\nResponse: " + str);
//                myResponses.add(new JSONArray(str));
//                getResponses();
            }
        });
    }

    public void getResponses() {
        while (!myResponses.isEmpty()) {
//            JSONArray jsonArray = myResponses.poll();
            JSONObject jsonArray = myResponses.poll();
//            System.out.println("JSONARRAY: " + jsonArray.toString());
            if (jsonArray != null) {
                System.out.println("getResponses Called");
//                if(jsonArray.toString().equals("{\"Prediction\":\"0\"}"))
                try {
                    double prediction = jsonArray.getDouble("Prediction");
                    lblResults.setText("Patient shows " + String.format("%.2f", prediction) + "% signs of Depression");
                } catch (Exception e) {
                    e.printStackTrace();
                    lblResults.setText("Error in API Response");
                }

//                    listAdapter.add("Patient does not show signs of depressions");
//                else
//                    lblResults.setText("Patient shows signs of depressions");
//                    listAdapter.add("Patient shows signs of depressions");
//                lstResults2.setAdapter(listAdapter);
            }
        }
    }
    
    private void processAudioData(String filename) {
//        genericToast = Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_LONG);
//        genericToast.show();
        short[] sData = new short[BufferElements2Rec];
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/DepressionAnalysis";
        File projDir = new File(filePath);
        projDir.mkdir();
        filePath += "/" + filename + ".pcm";
        System.out.println("Filepath: " + filePath);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
            System.out.println(os.getChannel().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        while (recorder.isRecording) {
            double sum = 0;
            updateElapsed(recorder.startTime);
            //gets the voice output from microphone to byte format
            int readSize = recorder.recorder.read(sData, 0, BufferElements2Rec);
            
            //Create Audio Block
//            AudioBlock newBlock = new AudioBlock(sData);
//            blockQueue.add(newBlock);
//            blockQueue.add(new AudioBlock(sData));

//            myWave.updateAmplitude(50);
            
            for (int i = 0; i < readSize; i++) {
//                try {
//                    output.writeShort(sData [i]);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                sum += sData[i] * sData[i];
            }
            
            if (readSize > 0 && updateWave) {
                final double amplitude = sum / readSize;
            }


//            StringBuilder sb = new StringBuilder();
            try {
                byte[] bData = recorder.short2byte(sData);
//                for (byte b : bData) {
//                    sb.append(String.format("%02X", b));
//                }
//                os.write(bData, 0, readSize);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if(recorder.isRecording) {
//                System.out.println(sb.toString());
//            }
        }
        
        try {
            os.close();
            System.out.println("File saved: " + filePath + "/" + filename);
            PredictiveIndex.setSAVED_FILE(filePath);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}



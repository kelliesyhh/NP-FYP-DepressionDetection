package com.example.depressionanalysis;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import com.example.R;

public class Audio_Record extends Activity {
    private static final int RECORDER_SAMPLERATE = 8000; //44100 -> Good For Music / 8000 -> good for speech
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    protected AudioRecord recorder = null;
    private Thread recordingThread = null;
    protected boolean isRecording = false;
    protected Date startTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        setButtonHandlers();
//        enableButtons(false);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }

//    private void setButtonHandlers() {
//        ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
//        ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
//    }
//
//    private void enableButton(int id, boolean isEnable) {
//        ((Button) findViewById(id)).setEnabled(isEnable);
//    }
//
//    private void enableButtons(boolean isRecording) {
//        enableButton(R.id.btnStart, !isRecording);
//        enableButton(R.id.btnStop, isRecording);
//    }

//    int BufferElements2Rec = 441; // for smaller sample size
    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    int numberOfReadBytes = 0;
    byte audioBuffer[] = new byte[BufferElements2Rec];

    protected void startRecording() {

        System.out.println("Recording Started");
        startTime = new Date();
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
//        recordingThread = new Thread(new Runnable() {
//            public void run() {
//                writeAudioDataToFile();
//            }
//        }, "AudioRecorder Thread");
//        recordingThread.start();
    }

    //convert short to byte
    protected byte[] short2byte(short[] sData) {
        int shortArrSize = sData.length;
        byte[] bytes = new byte[shortArrSize * 2];
        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte
//        String filePath = "voiceRecording.pcm";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/VoiceRecording.pcm";
        System.out.println("FilePath: " + filePath);
        PredictiveIndex.setSAVED_FILE(filePath);
//        String filePath = "/sdcard/voice8K16bitmono.pcm";
        short sData[] = new short[BufferElements2Rec];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
            System.out.println(os.getChannel().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            System.out.println("Short writing to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            System.out.println("Recording Stopped");
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

//    private View.OnClickListener btnClick = new View.OnClickListener() {
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.btnStart: {
//                    enableButtons(true);
//                    startRecording();
//                    break;
//                }
//                case R.id.btnStop: {
//                    enableButtons(false);
//                    stopRecording();
//                    break;
//                }
//            }
//        }
//    };

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            finish();
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
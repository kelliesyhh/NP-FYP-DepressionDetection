package com.example.audioanalyzer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.audiofx.AutomaticGainControl;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import com.example.R;
import com.example.depressionanalysis.PredictiveIndex;

import static com.example.audioanalyzer.AnalyzerActivity.bSaveWav;
import static com.example.audioanalyzer.AnalyzerActivity.startRecording;
import static com.example.animation.LoginActivity.username;

class SamplingLoop extends Thread {
    private final String TAG = "SamplingLoop";
    private volatile boolean isRunning = false;
    private volatile boolean isPaused1 = false;
    private STFT stft;   // use with care
    private final AnalyzerParameters analyzerParam;

    private SineGenerator sineGen1;
    private SineGenerator sineGen2;
    private double[] spectrumDBcopy;   // XXX, transfers data from SamplingLoop to AnalyzerGraphic

    private final AnalyzerActivity activity;

    volatile double wavSecRemain;
    volatile double wavSec = 0;
    private Thread recordingThread = null;
    private Date now = new Date();
    static boolean sendToServer = false;

    SamplingLoop(AnalyzerActivity _activity, AnalyzerParameters _analyzerParam) {
        activity = _activity;
        analyzerParam = _analyzerParam;

        isPaused1 = ((SelectorText) activity.findViewById(R.id.button_recording)).getValue().equals("stop");
        // Signal sources for testing
        double fq0 = Double.parseDouble(activity.getString(R.string.test_signal_1_freq1));
        double amp0 = Math.pow(10, 1 / 20.0 * Double.parseDouble(activity.getString(R.string.test_signal_1_db1)));
        double fq1 = Double.parseDouble(activity.getString(R.string.test_signal_2_freq1));
        double amp1 = Math.pow(10, 1 / 20.0 * Double.parseDouble(activity.getString(R.string.test_signal_2_db1)));
        double fq2 = Double.parseDouble(activity.getString(R.string.test_signal_2_freq2));
        double amp2 = Math.pow(10, 1 / 20.0 * Double.parseDouble(activity.getString(R.string.test_signal_2_db2)));
        if (analyzerParam.audioSourceId == 1000) {
            sineGen1 = new SineGenerator(fq0, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp0);
        } else {
            sineGen1 = new SineGenerator(fq1, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp1);
        }
        sineGen2 = new SineGenerator(fq2, analyzerParam.sampleRate, analyzerParam.SAMPLE_VALUE_MAX * amp2);
    }

    private void SleepWithoutInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private double baseTimeMs = SystemClock.uptimeMillis();

    private void LimitFrameRate(double updateMs) {
        // Limit the frame rate by wait `delay' ms.
        baseTimeMs += updateMs;
        long delay = (int) (baseTimeMs - SystemClock.uptimeMillis());
//      Log.i(TAG, "delay = " + delay);
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Log.i(TAG, "Sleep interrupted");  // seems never reached
            }
        } else {
            baseTimeMs -= delay;  // get current time
            // Log.i(TAG, "time: cmp t="+Long.toString(SystemClock.uptimeMillis())
            //            + " v.s. t'=" + Long.toString(baseTimeMs));
        }
    }

    private double[] mdata;

    // Generate test data.
    private int readTestData(short[] a, int offsetInShorts, int sizeInShorts, int id) {
        if (mdata == null || mdata.length != sizeInShorts) {
            mdata = new double[sizeInShorts];
        }
        Arrays.fill(mdata, 0.0);
        switch (id - 1000) {
            case 1:
                sineGen2.getSamples(mdata);
                // No break, so values of mdata added.
            case 0:
                sineGen1.addSamples(mdata);
                for (int i = 0; i < sizeInShorts; i++) {
                    a[offsetInShorts + i] = (short) Math.round(mdata[i]);
                }
                break;
            case 2:
                for (int i = 0; i < sizeInShorts; i++) {
                    a[i] = (short) (analyzerParam.SAMPLE_VALUE_MAX * (2.0 * Math.random() - 1));
                }
                break;
            default:
                Log.w(TAG, "readTestData(): No this source id = " + analyzerParam.audioSourceId);
        }
        // Block this thread, so that behave as if read from real device.
        LimitFrameRate(1000.0 * sizeInShorts / analyzerParam.sampleRate);
        return sizeInShorts;
    }

    @Override
    public void run() {
        AudioRecord record;

        long tStart = SystemClock.uptimeMillis();
        try {
            activity.graphInit.join();  // TODO: Seems not working as intended....
        } catch (InterruptedException e) {
            Log.w(TAG, "run(): activity.graphInit.join() failed.");
        }
        long tEnd = SystemClock.uptimeMillis();
        if (tEnd - tStart < 500) {
            Log.i(TAG, "wait more.." + (500 - (tEnd - tStart)) + " ms");
            // Wait until previous instance of AudioRecord fully released.
            SleepWithoutInterrupt(500 - (tEnd - tStart));
        }

        int minBytes = AudioRecord.getMinBufferSize(analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (minBytes == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "SamplingLoop::run(): Invalid AudioRecord parameter.\n");
            return;
        }

        /*
          Develop -> Reference -> AudioRecord
             Data should be read from the audio hardware in chunks of sizes
             inferior to the total recording buffer size.
         */
        // Determine size of buffers for AudioRecord and AudioRecord::read()
        int readChunkSize = analyzerParam.hopLen;  // Every hopLen one fft result (overlapped analyze window)
        readChunkSize = Math.min(readChunkSize, 2048);  // read in a smaller chunk, hopefully smaller delay
        int bufferSampleSize = Math.max(minBytes / analyzerParam.BYTE_OF_SAMPLE, analyzerParam.fftLen / 2) * 2;
        // tolerate up to about 1 sec.
        bufferSampleSize = (int) Math.ceil(1.0 * analyzerParam.sampleRate / bufferSampleSize) * bufferSampleSize;

        // Use the mic with AGC turned off. e.g. VOICE_RECOGNITION for measurement
        // The buffer size here seems not relate to the delay.
        // So choose a larger size (~1sec) so that overrun is unlikely.
        try {
            if (analyzerParam.audioSourceId < 1000) {
                record = new AudioRecord(analyzerParam.audioSourceId, analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize);
            } else {
                record = new AudioRecord(analyzerParam.RECORDER_AGC_OFF, analyzerParam.sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Fail to initialize recorder.");
            activity.analyzerViews.notifyToast("Illegal recorder argument. (change source)");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Check Auto-Gain-Control status.
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl agc = AutomaticGainControl.create(
                        record.getAudioSessionId());
                if (agc.getEnabled())
                    Log.i(TAG, "SamplingLoop::Run(): AGC: enabled.");
                else
                    Log.i(TAG, "SamplingLoop::Run(): AGC: disabled.");
            } else {
                Log.i(TAG, "SamplingLoop::Run(): AGC: not available.");
            }
        }

        Log.i(TAG, "SamplingLoop::Run(): Starting recorder... \n" +
                "  source          : " + analyzerParam.getAudioSourceName() + "\n" +
                String.format("  sample rate     : %d Hz (request %d Hz)\n", record.getSampleRate(), analyzerParam.sampleRate) +
                String.format("  min buffer size : %d samples, %d Bytes\n", minBytes / analyzerParam.BYTE_OF_SAMPLE, minBytes) +
                String.format("  buffer size     : %d samples, %d Bytes\n", bufferSampleSize, analyzerParam.BYTE_OF_SAMPLE * bufferSampleSize) +
                String.format("  read chunk size : %d samples, %d Bytes\n", readChunkSize, analyzerParam.BYTE_OF_SAMPLE * readChunkSize) +
                String.format("  FFT length      : %d\n", analyzerParam.fftLen) +
                String.format("  nFFTAverage     : %d\n", analyzerParam.nFFTAverage));
        analyzerParam.sampleRate = record.getSampleRate();

        if (record.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "SamplingLoop::run(): Fail to initialize AudioRecord()");
            activity.analyzerViews.notifyToast("Fail to initialize recorder.");
            // If failed somehow, leave user a chance to change preference.
            return;
        }

        short[] audioSamples = new short[readChunkSize];
        int numOfReadShort;

        stft = new STFT(analyzerParam);
        stft.setAWeighting(analyzerParam.isAWeighting);
        if (spectrumDBcopy == null || spectrumDBcopy.length != analyzerParam.fftLen / 2 + 1) {
            spectrumDBcopy = new double[analyzerParam.fftLen / 2 + 1];
        }

        RecorderMonitor recorderMonitor = new RecorderMonitor(analyzerParam.sampleRate, bufferSampleSize, "SamplingLoop::run()");
        recorderMonitor.start();

//      FPSCounter fpsCounter = new FPSCounter("SamplingLoop::run()");
        WavWriter wavWriter = new WavWriter(analyzerParam.sampleRate);

        /*boolean bSaveWavLoop = activity.bSaveWav;  // change of bSaveWav during loop will only affect next enter.*/
        boolean bSaveWavLoop = bSaveWav;
        if (bSaveWavLoop) {
            wavWriter.start();
            wavSecRemain = wavWriter.secondsLeft();
            wavSec = 0;

            if (startRecording) {
                // Start recording
                try {
                    record.startRecording();
                    isRunning = true;
                    PredictiveIndex.setUSER(username);
                    PredictiveIndex.setSESSION_ID();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Fail to start recording.");
                    activity.analyzerViews.notifyToast("Fail to start recording.");
                    return;
                }
            }
        }
        System.out.println("recordingThread running");
        String filename = username + "_" + (now.toGMTString().replaceAll(" ", "_").toLowerCase());
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/DepressionAnalysis";
        File projDir = new File(filePath);
        projDir.mkdir();
        filePath += "/" + filename + ".wav";
        //+ ".pcm";
        System.out.println("Filepath: " + filePath);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
            System.out.println(os.getChannel().toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Main loop
        // When running in this loop (including when paused), you can not change properties
        // related to recorder: e.g. audioSourceId, sampleRate, bufferSampleSize
        // TODO: allow change of FFT length on the fly.
        while (isRunning) {
            // Read data
            numOfReadShort = record.read(audioSamples, 0, readChunkSize);   // pulling
            if (recorderMonitor.updateState(numOfReadShort)) {  // performed a check
                if (recorderMonitor.getLastCheckOverrun())
                    activity.analyzerViews.notifyOverrun();
                if (bSaveWavLoop)
                    wavSecRemain = wavWriter.secondsLeft();
            }
            if (bSaveWavLoop) {
                wavWriter.pushAudioShort(audioSamples, numOfReadShort);  // Maybe move this to another thread?
                wavSec = wavWriter.secondsWritten();
                activity.analyzerViews.updateRec(wavSec);
            }
            if (isPaused1) {
//          fpsCounter.inc();
                // keep reading data, for overrun checker and for write wav data
                record.stop();
                record.release();
                wavWriter.postCall();
                //continue;
            }
            stft.feedData(audioSamples, numOfReadShort);

            // If there is new spectrum data, do plot
            if (stft.nElemSpectrumAmp() >= analyzerParam.nFFTAverage) {
                // Update spectrum or spectrogram
                final double[] spectrumDB = stft.getSpectrumAmpDB();
                System.arraycopy(spectrumDB, 0, spectrumDBcopy, 0, spectrumDB.length);
                activity.analyzerViews.update(spectrumDBcopy);
//          fpsCounter.inc();

                stft.calculatePeak();
                activity.maxAmpFreq = stft.maxAmpFreq;
                activity.maxAmpDB = stft.maxAmpDB;

                // get RMS
                activity.dtRMS = stft.getRMS();
                activity.dtRMSFromFT = stft.getRMSFromFT();
            }
            try {
                byte[] bData = short2byte(audioSamples);

                os.write(bData, 0, (readChunkSize * analyzerParam.BYTE_OF_SAMPLE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
            System.out.println("File saved: " + filePath);
            PredictiveIndex.setSAVED_FILE(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
		/*
		int finalReadChunkSize = readChunkSize;
		recordingThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("recordingThread running");
				String filename = username + "_" + (now.toGMTString().replaceAll(" ", "_").toLowerCase());
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
				
				while (isRunning) {
					try {
						byte[] bData = short2byte(audioSamples);
//                for (byte b : bData) {
//                    sb.append(String.format("%02X", b));
//                }
//                os.write(bData, 0, readSize);
						os.write(bData, 0, finalReadChunkSize);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try {
					os.close();
					System.out.println("File saved: " + filePath);
					PredictiveIndex.setSAVED_FILE(filePath);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "AudioRecorder Thread");
		recordingThread.start();*/
        Log.i(TAG, "SamplingLoop::Run(): Actual sample rate: " + recorderMonitor.getSampleRate());
        Log.i(TAG, "SamplingLoop::Run(): Stopping and releasing recorder.");
//		record.stop();
//		record.release();
        //wavWriter.postCall();
        sendToServer = true;
        if (bSaveWavLoop) {
            Log.i(TAG, "SamplingLoop::Run(): Ending saved wav.");
            wavWriter.stop();
            activity.analyzerViews.notifyWAVSaved(wavWriter.relativeDir);
        }
    }

    void setAWeighting(boolean isAWeighting) {
        if (stft != null) {
            stft.setAWeighting(isAWeighting);
        }
    }

    void setPause(boolean pause) {
        this.isPaused1 = pause;
    }

    boolean getPause() {
        return this.isPaused1;
    }

    void finish() {
        isRunning = false;
        interrupt();
    }

    private final String filename = username + "_" + (now.toGMTString().replaceAll(" ", "_").toLowerCase());

    /*	private void processAudioData(short[] audioSamples, String filename, int readChunkSize) {
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

            while (isRunning) {
                try {
                    byte[] bData = short2byte(audioSamples);
    //                for (byte b : bData) {
    //                    sb.append(String.format("%02X", b));
    //                }
    //                os.write(bData, 0, readSize);
                    os.write(bData, 0, readChunkSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                os.close();
                System.out.println("File saved: " + filePath);
                PredictiveIndex.setSAVED_FILE(filePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrSize = sData.length;
        byte[] bytes = new byte[shortArrSize * 2];
        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
}
package com.example.audioanalyzer;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import cz.msebera.android.httpclient.Header;
import com.example.depressionanalysis.PredictiveIndex;
import com.example.depressionanalysis.restClient;

public class WavWriter {
	private static final String TAG = "WavWriter";
	private File outPath;
	private String filePath = PredictiveIndex.getSAVED_FILE();
	private OutputStream out;
	private byte[] header = new byte[44];
	final String relativeDir = "/Recorder";

	private int channels = 1;
	private byte RECORDER_BPP = 16;  // bits per sample
	private int byteRate;            // Average bytes per second
	private int totalDataLen = 0;   // (file size) - 8
	private int totalAudioLen = 0;   // bytes of audio raw data
	private int framesWritten = 0;

	public static ConcurrentLinkedQueue<JSONObject> myResponses = new ConcurrentLinkedQueue<>();

	WavWriter(int sampleRate) {
		byteRate = sampleRate * RECORDER_BPP / 8 * channels;

		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1, PCM/uncompressed
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (sampleRate & 0xff);
		header[25] = (byte) ((sampleRate >> 8) & 0xff);
		header[26] = (byte) ((sampleRate >> 16) & 0xff);
		header[27] = (byte) ((sampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);              // Average bytes per second
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (channels * RECORDER_BPP / 8);  // Block align (number of bytes per sample slice)
		header[33] = 0;
		header[34] = RECORDER_BPP;                          // bits per sample (Significant bits per sample)
		header[35] = 0;                                     // Extra format bytes
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
	}

	private static final int version = android.os.Build.VERSION.SDK_INT;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	double secondsLeft() {
		long byteLeft;
		if (version >= 9) {
			byteLeft = outPath.getFreeSpace();  // Need API level 9
		} else {
			StatFs statFs = new StatFs(outPath.getAbsolutePath());
			byteLeft = (statFs.getAvailableBlocks() * (long) statFs.getBlockSize());
		}
		if (byteRate == 0 || byteLeft == 0) {
			return 0;
		}
		return (double) byteLeft / byteRate;
	}

	boolean start() {
		if (!isExternalStorageWritable()) {
			return false;
		}
		File path = new File(Environment.getExternalStorageDirectory().getPath() + relativeDir);
		if (!path.exists() && !path.mkdirs()) {
			Log.e(TAG, "Failed to make directory: " + path.toString());
			return false;
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss.SSS's'", Locale.US);
		String nowStr = df.format(new Date());
		//outPath = new File(path, "rec" + nowStr + ".wav");
		outPath = new File(filePath);

		try {
			out = new FileOutputStream(outPath);
			out.write(header, 0, 44);
			// http://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory%28java.lang.String%29
		} catch (IOException e) {
			Log.w(TAG, "start(): Error writing " + outPath, e);
			out = null;
		}
		return true;
	}

	void stop() {
		if (out == null) {
			Log.w(TAG, "stop(): Error closing " + outPath + "  null pointer");
			return;
		}
		try {
			out.close();
		} catch (IOException e) {
			Log.w(TAG, "stop(): Error closing " + outPath, e);
		}
		out = null;
		// Modify totalDataLen and totalAudioLen to match data
		RandomAccessFile raf;
		try {
			totalAudioLen = framesWritten * RECORDER_BPP / 8 * channels;
			totalDataLen = header.length + totalAudioLen - 8;
			raf = new RandomAccessFile(outPath, "rw");
			raf.seek(4);
			raf.write((byte) ((totalDataLen) & 0xff));
			raf.write((byte) ((totalDataLen >> 8) & 0xff));
			raf.write((byte) ((totalDataLen >> 16) & 0xff));
			raf.write((byte) ((totalDataLen >> 24) & 0xff));
			raf.seek(40);
			raf.write((byte) ((totalAudioLen) & 0xff));
			raf.write((byte) ((totalAudioLen >> 8) & 0xff));
			raf.write((byte) ((totalAudioLen >> 16) & 0xff));
			raf.write((byte) ((totalAudioLen >> 24) & 0xff));
			raf.close();
		} catch (IOException e) {
			Log.w(TAG, "stop(): Error modifying " + outPath, e);
		}
	}

	private byte[] byteBuffer;

	// Assume RECORDER_BPP == 16 and channels == 1
	void pushAudioShort(short[] ss, int numOfReadShort) {
		if (out == null) {
			Log.w(TAG, "pushAudioShort(): Error writing " + outPath + "  null pointer");
			return;
		}
		if (byteBuffer == null || byteBuffer.length != ss.length * 2) {
			byteBuffer = new byte[ss.length * 2];
		}
		for (int i = 0; i < numOfReadShort; i++) {
			byteBuffer[2 * i] = (byte) (ss[i] & 0xff);
			byteBuffer[2 * i + 1] = (byte) ((ss[i] >> 8) & 0xff);
		}
		framesWritten += numOfReadShort;
		try {
			out.write(byteBuffer, 0, numOfReadShort * 2);
			// if use out.write(byte), then a lot of GC will generated
		} catch (IOException e) {
			Log.w(TAG, "pushAudioShort(): Error writing " + outPath, e);
			out = null;
		}
	}

	double secondsWritten() {
		return (double) framesWritten / (byteRate * 8 / RECORDER_BPP / channels);
	}

	/* Checks if external storage is available for read and write */
	boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();  // Need API level 8
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	String getPath() {
		return outPath.getPath();
	}

	public void postCall() {
		RequestParams reqParam = new RequestParams();
//        reqParam.add("username", "aaa");
//        reqParam.add("password", "aaa@123");
//        JSONArray myResponse;

		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		filePath += "/DepressionAnalysis";
//        filePath += "/S1234567A_26_feb_2019_08:20:00_gmt.pcm";
		filePath = PredictiveIndex.getSAVED_FILE();
		System.out.println("postCall() print: " + filePath);

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
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
				// Pull out the first event on the public timeline
				System.out.println("Status: " + statusCode + "\nHeaders: " + headers + "\nResponse: " + timeline);
				getResponses();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String str, Throwable throwable) {
				// Pull out the first event on the public timeline
				System.out.println("Status: " + statusCode + "\nHeaders: " + headers + "\nResponse: " + str);
			}

			@Override
			public boolean getUseSynchronousMode() {
				return false;
			}
		});
	}
	public static double dispPrediction;
	public static boolean receivedPrediction = false;
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
					System.out.println("Patient shows " + String.format("%.2f", prediction) + "% signs of Depression");
					dispPrediction = prediction;
					receivedPrediction = true;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error in API Response");
				}
			}
		}
	}

}
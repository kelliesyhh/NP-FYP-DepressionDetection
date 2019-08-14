package com.example.depressionanalysis;

import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import cz.msebera.android.httpclient.Header;
import com.example.R;

//


public class TestAPI extends AppCompatActivity {
    private Button btnGET;
    private Button btnPOST;
//    private ToggleButton toggleUpdate;
    private ListView lstResults;
//    public ConcurrentLinkedQueue<JSONArray> myResponses = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<JSONObject> myResponses = new ConcurrentLinkedQueue<>();
    private ArrayAdapter<String> listAdapter ;  // = new ArrayList<>();
//    private Handler h;

//    AsyncHttpClient client = new AsyncHttpClient();

//    Runnable updateList = new Runnable() {
//        public void run() {
//            while(true) {
//                getResponses();
//            }
//        }
//    };
//
//    Thread procList = new Thread(updateList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_api);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnGET = (Button) findViewById(R.id.btnGet);
        btnPOST = (Button) findViewById(R.id.btnPost);
//        toggleUpdate = (ToggleButton) findViewById(R.id.toggleUpdate);
        lstResults = (ListView) findViewById(R.id.lstResults);


        btnGET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCall(v);
            }
        });

        btnPOST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCall(v);
            }
        });

//        listAdapter = new ArrayAdapter<String>(this, R.layout.simple_row);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
//        procList.start();


    }

    public interface Callback<T> {
        void onResponse(T response);
    }

    private void getCall(View v) {
        RequestParams reqParam = new RequestParams();
//        reqParam.add("username", "aaa");
//        reqParam.add("password", "aaa@123");
//        JSONArray myResponse;


//        restClient.post("files/upload", reqParam, new JsonHttpResponseHandler(){
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // If the response is JSONObject instead of expected JSONArray
//                Log.d("asd", "---------------- this is response : " + response);
//                try {
//                    JSONObject serverResp = new JSONObject(response.toString());
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
////                System.out.println("MyResponse:" + response.toString());
////                ((PredictiveIndex) this.getApplicationContext()).setSomeVariable("foo"); //set
////                String s = ((PredictiveIndex) this.getApplication()).getSomeVariable(); //get
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
//                // Pull out the first event on the public timeline
//                System.out.println("Status: " + statusCode + "\nHeaders: "+ headers + "\nResponse: " + timeline);
//                myResponses.add(timeline);
//                getResponses();
//            }
//        });

//        restClient.get("/api/SampleModels/", reqParam, new JsonHttpResponseHandler() {
        restClient.get("/users/DepressionAnalysis", reqParam, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("API", "GET : " + response);
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
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
                System.out.println("Status: " + statusCode + "\nHeaders: "+ headers + "\nResponse: " + timeline);
//                myResponses.add(timeline);
                getResponses();
            }

        });
    }

    private void postCall(View v){
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
        url = "/users/"+ PredictiveIndex.getUSER() +"/analysis/" + PredictiveIndex.getSESSION_ID();
//        url = "uploader";

        restClient.post(url, reqParam, new JsonHttpResponseHandler(){
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
                System.out.println("Status: " + statusCode + "\nHeaders: "+ headers + "\nResponse: " + timeline);
//                myResponses.add(timeline);
                getResponses();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String str, Throwable throwable) {
                // Pull out the first event on the public timeline
                System.out.println("Status: " + statusCode + "\nHeaders: "+ headers + "\nResponse: " + str);
//                myResponses.add(new JSONArray(str));
//                getResponses();
            }
        });



//        restClient.post("files/upload", reqParam, new JsonHttpResponseHandler(){
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                // If the response is JSONObject instead of expected JSONArray
//                Log.d("asd", "---------------- this is response : " + response);
//                try {
//                    JSONObject serverResp = new JSONObject(response.toString());
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
////                System.out.println("MyResponse:" + response.toString());
////                ((PredictiveIndex) this.getApplicationContext()).setSomeVariable("foo"); //set
////                String s = ((PredictiveIndex) this.getApplication()).getSomeVariable(); //get
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
//                // Pull out the first event on the public timeline
//                System.out.println("Status: " + statusCode + "\nHeaders: "+ headers + "\nResponse: " + timeline);
//                myResponses.add(timeline);
//                getResponses();
//            }
//        });

    }

    public void getResponses(){
        while(!myResponses.isEmpty()){
//            JSONArray jsonArray = myResponses.poll();
            JSONObject jsonArray = myResponses.poll();
            if(jsonArray != null) {
                System.out.println("getResponses Called");
                listAdapter.add(jsonArray.toString());
                lstResults.setAdapter(listAdapter);
            }
        }
    }



}

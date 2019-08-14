package com.example.animation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.R;

import static com.example.audioanalyzer.WavWriter.dispPrediction;

public class HistoryActivity extends AppCompatActivity {
	
	Button btnInsert, btnClear, btnCalendar;
	EditText editTxtY;
	GraphView graphView;
	TextView txtViewDate, txtViewTime, txtPercentage;
	ProgressBar progressBar;
	
	DatabaseHandler dbHandler;
	SQLiteDatabase sqLiteDatabase;
	
	LineGraphSeries<DataPoint> dataSeries = new LineGraphSeries<>(new DataPoint[0]);
	
	@SuppressLint("SimpleDataFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		
		// generate Dates
//        Calendar calendar = Calendar.getInstance();
//        Date d1 = calendar.getTime();
//        calendar.add(Calendar.DATE, 1);
//        Date d2 = calendar.getTime();
//        calendar.add(Calendar.DATE, 1);
//        Date d3 = calendar.getTime();
		
		graphView = (GraphView) findViewById(R.id.graph);
		dbHandler = new DatabaseHandler(this);
//        btnInsert = (Button) findViewById(R.id.btnInsert);
//        editTxtY = (EditText) findViewById(R.id.editTxtY);
//        btnClear = (Button) findViewById(R.id.btnClear);
		sqLiteDatabase = dbHandler.getWritableDatabase();
		txtViewDate = (TextView) findViewById(R.id.txtViewDate);
		txtViewTime = (TextView) findViewById(R.id.txtViewTime);
		txtPercentage = (TextView) findViewById(R.id.txtPercentage);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		btnCalendar = (Button) findViewById(R.id.btnCalendar);

//        dataSeries.resetData(grabData()); // make sure that when you start, you have data displayed
		
		graphView.getViewport().setScrollable(true);
		graphView.getViewport().setScalable(true);
		graphView.addSeries(dataSeries);
		graphView.getGridLabelRenderer().setNumHorizontalLabels(3);
		graphView.getGridLabelRenderer().setHumanRounding(false);
		graphView.setBackgroundColor(Color.rgb(192, 236, 255));
		
		dataSeries.setDrawDataPoints(true);
		dataSeries.setDataPointsRadius(5);
		
		// set manual X bounds
//        graphView.getViewport().setXAxisBoundsManual(true);
//        graphView.getViewport().setMinX(0);
//        graphView.getViewport().setMaxX(100);
		
		// set manual Y bounds
		graphView.getViewport().setYAxisBoundsManual(true);
		graphView.getViewport().setMinY(0);
		graphView.getViewport().setMaxY(100);
		
		String todayDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
		txtViewDate.setText(todayDate);
		
		if (dispPrediction < 10.0) {
			txtPercentage.setText(String.format("%.2f", dispPrediction) + "%");
		} else {
			txtPercentage.setText(String.format("%.1f", dispPrediction) + "%");
		}
		progressBar.setProgress((int) dispPrediction);
		
		insertData(dispPrediction);
		clearData();
		
		btnCalendar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
				startActivity(intent);
			}
		});
	}
	
	public void insertData(double pred) {
		//   btnInsert.setOnClickListener(new View.OnClickListener() {
		//        @Override
		//  public void onClick(View view) {
		long xValue = new Date().getTime();
		int yValue = (int) pred;
		
		dbHandler.insertToData(xValue, yValue);
		
		dataSeries.resetData(grabData());
		graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					txtViewTime.setText(sdf.format(new Date((long) value)));
					return sdf.format(new Date((long) value));
				} else {
					txtViewTime.setText(super.formatLabel(value, isValueX));
					return super.formatLabel(value, isValueX);
				}
			}
		});
		//    }
		//});
	}
	
	public DataPoint[] grabData() {
		String[] column = {"xValue, yValue"};
		@SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.query("Table2", column, null, null, null, null, null);
		
		DataPoint[] dataPoints = new DataPoint[cursor.getCount()];
		
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			dataPoints[i] = new DataPoint(cursor.getLong(0), cursor.getInt(1));
		}
		return dataPoints;
	}
	
	public void clearData() {
//        btnClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
		dbHandler.onUpgrade(sqLiteDatabase, 4, 5);
//            }
//        });
	}
	
	public void clearDatabase(String TABLE_NAME) {
		String clearDBQuery = "DELETE FROM " + TABLE_NAME;
		sqLiteDatabase.execSQL(clearDBQuery);
	}
	
}
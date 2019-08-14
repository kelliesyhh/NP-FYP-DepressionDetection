package com.example.depressionanalysis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.google.common.collect.EvictingQueue;
import com.google.common.primitives.Floats;

import com.example.R;

public class AmplitudeWaveFormView extends View {
    private static final String TAG = AmplitudeWaveFormView.class.getSimpleName();

    private static final int MAX_AMPLITUDE = 32767;
    private static final int SAMPLES_PER_SCREEN = 100;
    private float mAmplitude = 0;

    private Paint mRecordingPaint, mNotRecordingPaint;
    private int height = -1;
    private int width = -1;
    private boolean mIsStarted;

    private float[] lastPoints;

    private int oldWidth = -1, oldHeight = -1;
    private int mCurrentSample;
    private float amplitudeDivisor = 1;
    private float lx,ly, deltaX;


    private EvictingQueue<Float> mPointQueue;


    private int recordColor;

    private int notRecordingColor;


    public AmplitudeWaveFormView(Context context) {
        super(context);
        init();
    }

    public AmplitudeWaveFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmplitudeWaveFormView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void start() {
        mIsStarted = true;
    }


    public void stop() {
        mIsStarted = false;
    }
    public void updateAmplitude(float amplitude) {
        mAmplitude = amplitude;
        postInvalidate();
    }

    private void init() {
        recordColor = getResources().getColor(R.color.colorAccent);
        notRecordingColor = getResources().getColor(R.color.colorPrimary);
        mRecordingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRecordingPaint.setStyle(Paint.Style.STROKE);
        mRecordingPaint.setStrokeWidth(5);
        mRecordingPaint.setColor(recordColor);

        mNotRecordingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNotRecordingPaint.setStyle(Paint.Style.STROKE);
        mNotRecordingPaint.setStrokeWidth(5);
        mNotRecordingPaint.setColor(notRecordingColor);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        width = w;
        ly = height;
        lx = width;
        deltaX =  (float)width / (float)SAMPLES_PER_SCREEN;
        amplitudeDivisor = ((float) MAX_AMPLITUDE / (float) height);

        mPointQueue = EvictingQueue.create(SAMPLES_PER_SCREEN * 4);
        if (lastPoints != null && lastPoints.length > 0) {
            float xScale = (float) width/oldWidth;
            float yScale = (float) height/oldHeight;
            Matrix matrix = new Matrix();
            matrix.setScale(xScale, yScale);
            matrix.mapPoints(lastPoints);
            mPointQueue.addAll(Floats.asList(lastPoints));
            ly = lastPoints[lastPoints.length-1];
            lx= lastPoints[lastPoints.length -2];
            lastPoints = null;
        }

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentSample = bundle.getInt("sample");
            lastPoints = bundle.getFloatArray("lines");
            oldWidth = bundle.getInt("oldWidth");
            oldHeight = bundle.getInt("oldHeight");
            state = ((Bundle) state).getParcelable("parent");

        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putFloatArray("lines", Floats.toArray(mPointQueue));
        bundle.putInt("sample", mCurrentSample);
        bundle.putParcelable("parent", super.onSaveInstanceState());
        bundle.putInt("oldWidth", width);
        bundle.putInt("oldHeight", height);
        return bundle;
    }


    @Override
    public void onDraw(Canvas canvas) {

        if (mIsStarted) {
            float x = lx + deltaX;
            float y = height - (mAmplitude / amplitudeDivisor);
            mPointQueue.add(lx);
            mPointQueue.add(ly);
            mPointQueue.add(x);
            mPointQueue.add(y);
            lastPoints = Floats.toArray(mPointQueue);
            lx = x;
            ly = y;
        }
        if (lastPoints != null && lastPoints.length > 0) {
            int len = mPointQueue.size() / 4 >= SAMPLES_PER_SCREEN ? SAMPLES_PER_SCREEN * 4 : mPointQueue.size();
            float translateX = width - lastPoints[lastPoints.length - 2];
            canvas.translate(translateX, 0);
            canvas.drawLines(lastPoints, 0, len, mRecordingPaint);
        }

        if (mCurrentSample <= SAMPLES_PER_SCREEN) {
            drawNotRecordingLine(canvas);
        }
        mCurrentSample++;
    }

    private void drawNotRecordingLine(Canvas canvas) {
        canvas.drawLine(0,height, width, height, mNotRecordingPaint);
    }
}
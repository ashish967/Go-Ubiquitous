package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AnalogWatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = AnalogWatchFaceService.class.getSimpleName();
    Paint mHourPaint;
    Paint mDayPaint;


    private SimpleDateFormat formatTimeHour= new SimpleDateFormat("HH:");
    private SimpleDateFormat formatTimeMin= new SimpleDateFormat("mm");

    private SimpleDateFormat formatDay=new SimpleDateFormat("EEE, MMM d yyyy");


    private Calendar mDate;

    private Engine mEngine;
    private String mMin,mMax;
    private int mWeather;

    Bitmap mBitmap;

    private int mBackgroundColor,mDividerColor;



    @Override
    public void onCreate() {
        super.onCreate();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,new IntentFilter(DataLayerListenerService.NOTIFY_WEATHER_CHANGE));

        Intent intent= new Intent(getApplicationContext(),SunshineSyncWearable.class);
        getApplicationContext().startService(intent);

    }

    @Override
    public void onDestroy() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        mEngine= new Engine();

        updateDrawVariables();
        return mEngine;
    }

    private void updateDrawVariables() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mMin=prefs.getString(Utility.MIN_TEMP_KEY,"-");
        mMax= prefs.getString(Utility.MAX_TEMP_KEY,"-");
        mWeather= prefs.getInt(Utility.WEATHER_KEY,200);
        mBackgroundColor= ContextCompat.getColor(getApplicationContext(),R.color.app_color);
        mDividerColor= ContextCompat.getColor(getApplicationContext(),R.color.divider_color);
        mBitmap= BitmapFactory.decodeResource(getResources(),Utility.getArtResourceForWeatherCondition(mWeather));

        Log.d(TAG,"Updating drawing variables");
    }


    BroadcastReceiver mReceiver =new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {


           updateCount();
       }
   };

    private void updateCount() {

        updateDrawVariables();
        if(mEngine!=null){

            mEngine.invalidate();
        }

    }




    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            /* initialize your watch face */
            mHourPaint= new Paint();
            mHourPaint.setColor(Color.WHITE);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mDayPaint =new Paint();
            mDayPaint.setColor(mDividerColor);
            mDayPaint.setTextSize(20);
            mDayPaint.setTextAlign(Paint.Align.CENTER);
            mDate= Calendar.getInstance();

            updateDrawVariables();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /* the time changed */
            mDate= Calendar.getInstance();

            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /* draw your watch face */

            // draw blue background

            canvas.drawARGB(Color.alpha(mBackgroundColor),Color.red(mBackgroundColor),
                    Color.green(mBackgroundColor),Color.blue(mBackgroundColor));


            String timeH =formatTimeHour.format(mDate.getTime());
            String timeM= formatTimeMin.format(mDate.getTime());

            int height= canvas.getHeight();
            int width= canvas.getWidth();

            // drawing hour and minute
            mHourPaint.setColor(Color.WHITE);
            mHourPaint.setTextSize(40);
            mHourPaint.setTypeface(Typeface.DEFAULT);
            float lengthMinute = mHourPaint.measureText(timeM,0,timeM.length());

            mHourPaint.setTypeface(Typeface.DEFAULT_BOLD);
            float lengthHour=mHourPaint.measureText(timeH,0,timeH.length());


            int startX= (int) ((width-(lengthHour+lengthMinute))/2);

            Rect textBound = new Rect();
            mHourPaint.getTextBounds(timeH,0,timeH.length(),textBound);
            int startY= (int) (0.35*height);
            Log.d(TAG,"Start Y"+startY);

            canvas.drawText(timeH,0,timeH.length(),startX,startY,mHourPaint);
            startX+=lengthHour;

            mHourPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(timeM,0,timeM.length(),startX,startY,mHourPaint);


            Log.d(TAG,"Start Y"+startY+" height "+height+" bounds "+textBound.height());

            startY+= textBound.height();
            Log.d(TAG,"Start Y"+startY);


            String day=formatDay.format(mDate.getTime());
            canvas.drawText(day,width/2, startY, mDayPaint);

            mDayPaint.getTextBounds(day,0,day.length(),textBound);

            startY+= textBound.height();
            Log.d(TAG,"Start Y"+startY);

            canvas.drawLine((0.35f*width),startY,(0.65f*width),startY,mDayPaint);


            startY+=(height*0.1);
            Log.d(TAG,"Start Y"+startY);

            int bitmapSize= (int) (width*0.15);
            if(mBitmap!=null)
            canvas.drawBitmap(mBitmap,null,new Rect((int) (width*0.40-bitmapSize),startY, (int) (width*0.40),startY+bitmapSize),mDayPaint);
//            canvas.drawRect(new Rect((int) (width*0.4-bitmapSize),startY, (int) (width*0.4),startY+bitmapSize),mDayPaint);

            mHourPaint.setTextSize(30);
            mHourPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mHourPaint.getTextBounds(mMin,0,mMin.length(),textBound);
            startY+=(bitmapSize/2+textBound.height()/2);
            Log.d(TAG,"Start Y"+startY);

            canvas.drawText(mMin,width*0.5f, startY, mHourPaint);

            mHourPaint.setTypeface(Typeface.DEFAULT);

            canvas.drawText(mMax,width*0.52f+textBound.width(), startY, mHourPaint);



        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }
    }
}

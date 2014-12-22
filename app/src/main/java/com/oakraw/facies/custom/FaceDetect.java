package com.oakraw.facies.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

import com.oakraw.facies.FaceSelector;

import java.util.ArrayList;

/**
 * Created by Rawipol on 12/19/14 AD.
 */
public class FaceDetect{

    private int imageWidth, imageHeight;
    private int numberOfFace = 15;
    private FaceDetector myFaceDetect;
    private FaceDetector.Face[] myFace;
    float myEyesDistance;
    int numberOfFaceDetected;
    Bitmap myBitmap;

    ArrayList<Bitmap> crop_faces = new ArrayList<Bitmap>();
    ArrayList<Bitmap> erode_faces = new ArrayList<Bitmap>();
    ArrayList<Coordinates> coor_faces = new ArrayList<Coordinates>();
    private Bitmap processBitmap;


    public FaceDetect(Context context, Bitmap original) {

        //BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
        //BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
        myBitmap = original;
        imageWidth = myBitmap.getWidth();
        imageHeight = myBitmap.getHeight();
        myFace = new FaceDetector.Face[numberOfFace];
        myFaceDetect = new FaceDetector(imageWidth, imageHeight,
                numberOfFace);
        numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);


    }

    public Bitmap draw() {

        Bitmap  bitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(myBitmap, 0, 0, null);

        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);

        for (int i = 0; i < numberOfFaceDetected; i++) {
            FaceDetector.Face face = myFace[i];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            myEyesDistance = face.eyesDistance();
            //listener.onProgress("cropping face "+(i+1));


            Log.d("oakTag", "x:" + myMidPoint.x + " y:" + myMidPoint.y);

            canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2),
                    (int) (myMidPoint.y - myEyesDistance * 2),
                    (int) (myMidPoint.x + myEyesDistance * 2),
                    (int) (myMidPoint.y + myEyesDistance * 2), myPaint);

            //Calculate area to crop face
            int start_x = (int) (myMidPoint.x - myEyesDistance * 2);
            int start_y = (int) (myMidPoint.y - myEyesDistance * 2);
            int width = (int) myEyesDistance * 4;
            int height = (int) myEyesDistance * 4;

            if(myMidPoint.x - myEyesDistance * 2 < 0){
                start_x = 0;
            }

            if(myMidPoint.y - myEyesDistance * 2 < 0){
                start_y = 0;
            }

            if(myMidPoint.x + myEyesDistance * 2 > imageWidth){
                width = (int)myEyesDistance * 2 + imageWidth - (int)myMidPoint.x;
            }

            if(myMidPoint.y + myEyesDistance * 2 > imageHeight){
                height = (int)myEyesDistance * 2 + imageHeight - (int)myMidPoint.y;
            }

            try {
                Bitmap faceCrop = Bitmap.createBitmap(myBitmap, start_x,
                        start_y,
                        width,
                        height);

                crop_faces.add(faceCrop);
                //listener.onProgress("eroding face "+(i+1));
                erode_faces.add((new FaceSelector(faceCrop)).getResult());
                coor_faces.add(new Coordinates(start_x, start_y, width, height));
            }catch(Exception e){
                int sum = start_x + height;
                Log.e("oakTag", imageWidth +" "+ sum);
            }
        }

        processBitmap = bitmap;

        return bitmap;
    }

    public Bitmap drawFaceFocus(int x, int y, int w, int h) {

        Bitmap  bitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(processBitmap, 0, 0, null);

        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(3);

            canvas.drawRect(x,y,x+w,y+h,myPaint);


            //Calculate area to crop face
            int start_x = x;
            int start_y = y;
            int width = w;
            int height = h;

            Log.e("oakTag", "2. x:"+x+" y:"+y+" w:"+w+" h:"+h);


        if(x < 0){
                start_x = 0;
            }

            if(y < 0){
                start_y = 0;
            }

            if(x + w > myBitmap.getWidth()){
                width = myBitmap.getWidth() - x;
            }

            if(y + h > myBitmap.getHeight()){
                height = myBitmap.getHeight() - y;
            }

            try {
                Bitmap faceCrop = Bitmap.createBitmap(myBitmap, start_x,
                        start_y,
                        width,
                        height);
                Log.e("oakTag", "3. x:"+start_x+" y:"+start_y+" w:"+width+" h:"+height);


                crop_faces.add(faceCrop);
                Log.e("mixth","add");

                erode_faces.add((new FaceSelector(faceCrop)).getResult());
                coor_faces.add(new Coordinates(start_x, start_y, width, height));
            }catch(Exception e){
                int sum = start_x + height;
                Log.e("oakTag", "4. x:"+x+" y:"+y+" w:"+w+" h:"+h);

            }


        processBitmap = bitmap;

        return bitmap;
    }

    public ArrayList<Bitmap> getCrop_faces() {
        return crop_faces;
    }

    public ArrayList<Coordinates> getCoor_faces() {
        return coor_faces;
    }

    public ArrayList<Bitmap> getErode_faces() {
        return erode_faces;
    }

    public interface OnProgressListener
    {
        public void onProgress(String Message);
    }

    private OnProgressListener listener;

    public void setOnProgressListener(OnProgressListener listener)
    {
        this.listener = listener;
    }
}
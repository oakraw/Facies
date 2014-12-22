package com.oakraw.facies;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.animation.AnimatorSet;
import com.oakraw.facies.adapter.FaceAdapter;
import com.oakraw.facies.custom.FaceDetect;
import com.oakraw.facies.singlefinger.SingleFingerView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sephiroth.android.library.widget.HListView;


public class MainActivity extends Activity{

    private ImageView image;
    private ImageButton add_img_btn;

    private static int RESULT_LOAD_IMAGE = 1;
    private FrameLayout img_panel;
    private HListView listView;
    private ImageButton add_face;
    private FaceDetect faceDetect;
    private int img_panel_width;
    private int img_panel_height;
    private int original_img_width;
    private int original_img_height;
    private boolean isImgPotrait;
    private FaceAdapter face_adapter;
    private Bitmap selected_face;
    private float select_area_x;
    private float select_area_y;
    private float select_area_w;
    private float select_area_h;
    private float ratio_img2dis;
    private float ratio_dis2img;

    private ImageButton remove_face;
    private boolean isRemoveMode = false;
    private AnimatorSet slide_down;
    private AnimatorSet slide_up;
    private boolean isShowSticker = false;
    private CircleImageView preview_selected;
    private AnimationManager listview_anim;
    private AnimationManager preview_selected_anim;
    private Bitmap original_image;
    private Bitmap stickerBitmap;
    private Bitmap saveBitmap;
    private ProgressDialog ringProgressDialog;
    private String imageResultPath;
    private ImageButton save_btn;
    private boolean isShowPreviewFace = false;
    private FloatingActionButton done_btn;
    private AnimationManager done_btn_anim;
    private LinearLayout customize_panel;
    private SingleFingerView new_scope;
    private FloatingActionButton cancel_btn;
    private AnimationManager cancel_btn_anim;
    private AnimationManager customize_panel_anim;
    private FloatingActionButton share_btn;
    private AnimationManager share_btn_anim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_panel = (FrameLayout)findViewById(R.id.img_panel);
        customize_panel = (LinearLayout)findViewById(R.id.customize_panel);
        image = (ImageView) findViewById(R.id.image);
        add_img_btn = (ImageButton) findViewById(R.id.add_img_btn);
        add_face = (ImageButton) findViewById(R.id.add_face);
        remove_face = (ImageButton) findViewById(R.id.remove_face);
        done_btn = (FloatingActionButton) findViewById(R.id.done_btn);
        cancel_btn = (FloatingActionButton) findViewById(R.id.cancel_btn);
        share_btn = (FloatingActionButton) findViewById(R.id.share_btn);
        save_btn = (ImageButton) findViewById(R.id.save);
        listView = (HListView) findViewById(R.id.panel);
        listView.setSelector(R.drawable.button_press_v2);
        preview_selected = (CircleImageView) findViewById(R.id.preview_selected);

        //initialize animation
        listview_anim = new AnimationManager(this, listView, 0, 120);
        customize_panel_anim = new AnimationManager(this, customize_panel, 0, 120);
        share_btn_anim = new AnimationManager(this, share_btn, 0, 120);
        preview_selected_anim = new AnimationManager(this, preview_selected, 0, -120);
        done_btn_anim = new AnimationManager(this, done_btn, 0, -120);
        cancel_btn_anim = new AnimationManager(this, cancel_btn, 0, -120);

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new_scope != null) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)new_scope.getPhoto().getLayoutParams();
                    int x = params.leftMargin;
                    int y = params.topMargin;
                    int w = new_scope.getPhoto().getWidth();
                    int h = new_scope.getPhoto().getHeight();
                    Log.e("oakTag", "1. x:"+x+" y:"+y+" w:"+w+" h:"+h);


                    new processAddMoreFace().execute(new Integer[]{x,y,w,h});

                }
                img_panel.removeViewAt(img_panel.getChildCount() - 1);
                done_btn_anim.slideDown();
                cancel_btn_anim.slideDown();
                customize_panel_anim.slideUp();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_panel.removeViewAt(img_panel.getChildCount() - 1);
                done_btn_anim.slideDown();
                cancel_btn_anim.slideDown();
                customize_panel_anim.slideUp();
            }
        });

        share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
                share_btn_anim.slideDown();
            }
        });



        add_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);

                if(isShowSticker){
                    listview_anim.slideDown();
                    isShowSticker = !isShowSticker;
                }

                if(isRemoveMode){
                    remove_face.setBackground(getResources().getDrawable(R.drawable.button_press));
                    isRemoveMode = !isRemoveMode;
                }
                if(isShowPreviewFace) {
                    preview_selected_anim.slideDown();
                    selected_face = null;
                    isShowPreviewFace = false;
                }

            }
        });

        add_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowSticker){
                    //listView.setVisibility(View.INVISIBLE);
                    listview_anim.slideDown();
                }else{
                    listView.setVisibility(View.VISIBLE);
                    listview_anim.slideUp();
                }

                isShowSticker = !isShowSticker;

                if(isRemoveMode){
                    remove_face.setBackground(getResources().getDrawable(R.drawable.button_press));
                    isRemoveMode = !isRemoveMode;
                }

            }
        });

        remove_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRemoveMode){
                    if(isShowSticker){
                        listview_anim.slideDown();
                        isShowSticker = !isShowSticker;
                    }
                    if(isShowPreviewFace) {
                        preview_selected_anim.slideDown();
                        selected_face = null;
                        isShowPreviewFace = false;
                    }
                    remove_face.setBackgroundColor(getResources().getColor(R.color.accent));
                }else{
                    remove_face.setBackground(getResources().getDrawable(R.drawable.button_press));
                }
                isRemoveMode = !isRemoveMode;
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(original_image != null) {
                    if (isShowSticker) {
                        listview_anim.slideDown();
                        isShowSticker = !isShowSticker;
                    }

                    if (isRemoveMode) {
                        remove_face.setBackground(getResources().getDrawable(R.drawable.button_press));
                        isRemoveMode = !isRemoveMode;
                    }
                    if (isShowPreviewFace) {
                        preview_selected_anim.slideDown();
                        selected_face = null;
                        isShowPreviewFace = false;
                    }

                    new save().execute("");
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        try {
            //Uri selectedImageUri = data.getData();

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                    Uri selectedImageUri = data.getData();
                    if (Build.VERSION.SDK_INT < 19) {
                        String selectedImagePath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                        processFace(bitmap);

                    }
                    else {
                        ParcelFileDescriptor parcelFileDescriptor;
                        try {
                            parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
                            BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,BitmapFactoryOptionsbfo);
                            parcelFileDescriptor.close();
                            processFace(image);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private void processFace(Bitmap bp){
        //store original image for final process
        original_image = bp;

        new process().execute("");

    }

    //use to convert scale of image pixel to display pixel or display pixel to image pixel
    private float convertX_dis2img(float x_display){
        if(isImgPotrait){
            int leftMargin = (int)image.getX();
            return (x_display - leftMargin) * original_img_height/img_panel_height;
        }else{
            return x_display * original_img_width/img_panel_width;
        }

    }

    private float convertY_dis2img(float y_display){
        if(isImgPotrait){
            return y_display * original_img_height/img_panel_height;
        }else{
            int topMargin = (int)image.getY();
            return (y_display - topMargin)*original_img_width/img_panel_width;
        }

    }

    private float convertX_img2dis(float x_img){
        if(isImgPotrait){
            int leftMargin = (int)image.getX();
            return leftMargin + (x_img * img_panel_height/original_img_height);
        }else{
            return x_img * img_panel_width/original_img_width;
        }

    }

    private float convertY_img2dis(float y_img){
        if(isImgPotrait){
            return y_img * img_panel_height/original_img_height;
        }else{
            int topMargin = (int)image.getY();
            return topMargin+(y_img*img_panel_width/original_img_width);
        }

    }

    //Check coordinates is in area of face isn't it

    private boolean isFaceArea(float x, float y){
        for(int i = 0 ;i < faceDetect.getCoor_faces().size();i++){
            float area_x = faceDetect.getCoor_faces().get(i).getX();
            float area_y = faceDetect.getCoor_faces().get(i).getY();
            float area_w = faceDetect.getCoor_faces().get(i).getWidth();
            float area_h = faceDetect.getCoor_faces().get(i).getHeight();
            if(x > area_x && x < area_x + area_w &&
               y > area_y && y < area_y + area_h){
                select_area_x = area_x;
                select_area_y = area_y;
                select_area_w = area_w;
                select_area_h = area_h;
                return true;
            }
        }
        return false;
    }

    // use to flip bitmap in horizontal
    public Bitmap flipImage(Bitmap src) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    //first process -> find the faces on image and erode it.
    private class process extends AsyncTask<String, Void, String> {

        private Bitmap bp;

        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(MainActivity.this, null, "Processing...", true);
            ringProgressDialog.setCancelable(true);

        }
        @Override
        protected String doInBackground(String... param) {
            try {
                faceDetect = new FaceDetect(getApplicationContext(), original_image);

                bp = faceDetect.draw();
                original_img_width = bp.getWidth();
                original_img_height = bp.getHeight();

                img_panel_width = img_panel.getWidth();
                img_panel_height = img_panel.getHeight();

                //scale bitmap to fit screen
                if (bp.getWidth() >= bp.getHeight()) {
                    int width = img_panel.getWidth();
                    bp = Bitmap.createScaledBitmap(bp, width, bp.getHeight() * width / bp.getWidth(), false);
                    isImgPotrait = false;
                    ratio_img2dis = (float)img_panel_width / (float)original_img_width;
                    ratio_dis2img = (float)original_img_width / (float)img_panel_width;
                } else {
                    int height = img_panel.getHeight();
                    bp = Bitmap.createScaledBitmap(bp, bp.getWidth() * height / bp.getHeight(), height, false);
                    isImgPotrait = true;
                    ratio_img2dis = (float)img_panel_height / (float)original_img_height;
                    ratio_dis2img = (float)original_img_height / (float)img_panel_height;


                }
            }catch(Exception e){

            }


            return "Processing...";
        }
        @Override
        protected void onPostExecute(String result) {
            imageResultPath = null;
            final int count = img_panel.getChildCount();
            //remove all face that was added in image
            if (count > 6) {
                for (int i = count - 1; i >= 6; i--) {
                    img_panel.removeViewAt(i);
                }
            }

            image.setImageBitmap(bp);

            face_adapter = new FaceAdapter(getApplicationContext(), faceDetect.getCrop_faces());
            face_adapter.setOnSelectedListener(new FaceAdapter.OnSelectedListener() {
                @Override
                public void onSelected(int index) {
                    //if select add face
                    if (index == faceDetect.getCrop_faces().size()) {
                        Log.e("oakTag", "add");
                        new_scope = new SingleFingerView(getApplicationContext());
                        new_scope.setImage(0);
                        img_panel.addView(new_scope);
                        done_btn.setVisibility(View.VISIBLE);
                        done_btn_anim.slideUp();
                        cancel_btn.setVisibility(View.VISIBLE);
                        cancel_btn_anim.slideUp();
                        customize_panel_anim.slideDown();

                        if(isShowPreviewFace) {
                            preview_selected_anim.slideDown();
                            selected_face = null;
                            isShowPreviewFace = false;
                        }
                        listview_anim.slideDown();
                        isShowSticker = !isShowSticker;
                    } else {
                        selected_face = faceDetect.getCrop_faces().get(index);
                        preview_selected.setImageBitmap(selected_face);
                        selected_face = faceDetect.getErode_faces().get(index);
                        preview_selected.setVisibility(View.VISIBLE);
                        preview_selected_anim.slideUp();
                        isShowPreviewFace = true;
                    }
                }
            });
            listView.setAdapter(face_adapter);

            /*Set Listener for image panel*/
            img_panel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float x = event.getX();
                    float y = event.getY();

                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            int topMargin = (int)image.getY();
                            //Log.d("oakTag","Touch img --> x:"+x+" y:"+y );
                            final ImageView iv = new ImageView(getApplicationContext());
                            iv.setImageBitmap(selected_face);

                            if(isFaceArea(convertX_dis2img(x),convertY_dis2img(y)) /*&& selected_face != null*/){
                                float w = select_area_w * ratio_img2dis;
                                float h = select_area_h * ratio_img2dis;
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)w, (int)h);
                                params.leftMargin = (int)convertX_img2dis(select_area_x);
                                params.topMargin = (int)convertY_img2dis(select_area_y);
                                iv.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {
                                        switch (event.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                if(isRemoveMode){
                                                    img_panel.removeView(v);
                                                }else{
                                                    iv.setImageBitmap(flipImage(((BitmapDrawable)iv.getDrawable()).getBitmap()));
                                                }
                                                break;
                                        }
                                        return true;
                                    }
                                });
                                img_panel.addView(iv, params);
                                Log.d("oakTag","true" );

                            }
                            else{
                                Log.d("oakTag","false" );

                            }


                            break;
                    }
                    return true;
                }
            });
            ringProgressDialog.dismiss();
        }

    }

    // process when add more face (custom mode)
    private class processAddMoreFace extends AsyncTask<Integer, Void, String> {

        private Bitmap bp;

        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(MainActivity.this, null, "Processing...", true);
            ringProgressDialog.setCancelable(true);

        }
        @Override
        protected String doInBackground(Integer... param) {
            try {

                Log.e("oakTag", "1. x:"+param[0]+" y:"+param[1]+" w:"+param[2]+" h:"+param[3]);

                bp = faceDetect.drawFaceFocus(
                        (int)convertX_dis2img(param[0]),
                        (int)convertY_dis2img(param[1]),
                        Math.round(param[2] * ratio_dis2img),
                        Math.round(param[3] * ratio_dis2img)
                );
                new_scope = null;

                //scale bitmap to fit screen
                if (bp.getWidth() >= bp.getHeight()) {
                    int width = img_panel.getWidth();
                    bp = Bitmap.createScaledBitmap(bp, width, bp.getHeight() * width / bp.getWidth(), false);
                    isImgPotrait = false;
                    ratio_img2dis = (float)img_panel_width / (float)original_img_width;
                    ratio_dis2img = (float)original_img_width / (float)img_panel_width;
                } else {
                    int height = img_panel.getHeight();
                    bp = Bitmap.createScaledBitmap(bp, bp.getWidth() * height / bp.getHeight(), height, false);
                    isImgPotrait = true;
                    ratio_img2dis = (float)img_panel_height / (float)original_img_height;
                    ratio_dis2img = (float)original_img_height / (float)img_panel_height;


                }
            }catch(Exception e){
                Log.e("oakTag", e.toString());
            }


            return "Processing...";
        }
        @Override
        protected void onPostExecute(String result) {

            image.setImageBitmap(bp);

            LinearLayout.LayoutParams img_params = (LinearLayout.LayoutParams)img_panel.getLayoutParams();
            img_panel_width = img_panel.getWidth();
            img_panel_height = img_panel.getHeight();

            //listView.setAdapter(null);

            face_adapter = new FaceAdapter(getApplicationContext(), faceDetect.getCrop_faces());
            face_adapter.setOnSelectedListener(new FaceAdapter.OnSelectedListener() {
                @Override
                public void onSelected(int index) {
                    //if select add face
                    if (index == faceDetect.getCrop_faces().size()) {
                        new_scope = new SingleFingerView(getApplicationContext());
                        new_scope.setImage(0);
                        img_panel.addView(new_scope);
                        done_btn.setVisibility(View.VISIBLE);
                        done_btn_anim.slideUp();
                        cancel_btn.setVisibility(View.VISIBLE);
                        cancel_btn_anim.slideUp();
                        customize_panel_anim.slideDown();

                        if(isShowPreviewFace) {
                            preview_selected_anim.slideDown();
                            selected_face = null;
                            isShowPreviewFace = false;
                        }
                        listview_anim.slideDown();
                        isShowSticker = !isShowSticker;
                    }  else {
                        selected_face = faceDetect.getCrop_faces().get(index);
                        preview_selected.setImageBitmap(selected_face);
                        selected_face = faceDetect.getErode_faces().get(index);
                        preview_selected.setVisibility(View.VISIBLE);
                        preview_selected_anim.slideUp();
                        isShowPreviewFace = true;
                    }
                }
            });
            listView.setAdapter(face_adapter);



            ringProgressDialog.dismiss();
        }

    }

    //final process merge original image with all faces
    private class save extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(MainActivity.this, null, "Saving...", true);
            ringProgressDialog.setCancelable(true);

        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected String doInBackground(String... param) {

            for (int i = 6; i < img_panel.getChildCount(); i++) {
                ImageView currentSticker = (ImageView) img_panel.getChildAt(i);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) currentSticker.getLayoutParams();
                FrameLayout.LayoutParams frame = (FrameLayout.LayoutParams) image.getLayoutParams();

                int minus = (img_panel.getHeight() - image.getHeight()) / 2;

                float scaleW = original_image.getWidth() / image.getWidth();

                float scaleSize;
                if (image.getWidth() >= image.getHeight()) {
                    scaleSize =  (float) original_image.getWidth() / image.getWidth();
                } else {
                    scaleSize = (float) original_image.getHeight() / image.getHeight();
                }

                int left = (int) Math.abs(Math.abs(params.leftMargin - image.getX()) * scaleSize);
                int top = (int) Math.abs(Math.abs(params.topMargin - image.getY()) * scaleSize);


                int w = Math.round(currentSticker.getWidth() * scaleSize);
                int h = Math.round(currentSticker.getHeight() * scaleSize);



                stickerBitmap = ((BitmapDrawable) currentSticker.getDrawable()).getBitmap();


                int newW;
                int newH;
                int newLeft;
                int newTop;


                stickerBitmap = Bitmap.createScaledBitmap(stickerBitmap, w, h, false);

                if(currentSticker.getRotation() == 0){
                    newLeft = left;
                    newTop = top;


                }
                else{

                    float degrees = currentSticker.getRotation();

                    float totalRotated = degrees % 360;
                    // precompute some trig functions
                    double radians = Math.toRadians(totalRotated);
                    double sin = Math.abs(Math.sin(radians));
                    double cos = Math.abs(Math.cos(radians));
                    // figure out total width and height of new bitmap
                    newW = (int) Math.round(w * cos + h * sin);
                    newH = (int) Math.round(w * sin + h * cos);
                    // set up matrix
                    Matrix matrix = new Matrix();
                    matrix.setRotate(totalRotated);
                    // create new bitmap by rotating mBitmap

                    stickerBitmap = Bitmap.createScaledBitmap(stickerBitmap,w, h, false);
                    stickerBitmap = Bitmap.createBitmap(stickerBitmap, 0, 0, w, h, matrix, true);



                    newLeft = left - Math.abs(((newW - w)/2));
                    newTop= top - Math.abs(((newH - h)/2));
                }

                Log.d("myScale",newLeft+" "+newTop+" "+left+" "+top);


                if(saveBitmap == null)
                    saveBitmap = getBitmapOverlay(original_image, stickerBitmap, newLeft, newTop);
                else
                    saveBitmap = getBitmapOverlay(saveBitmap, stickerBitmap, newLeft, newTop);

            }


            if(saveBitmap != null)
                saveImageToGallery(saveBitmap);
            else
                saveImageToGallery(original_image);

            saveBitmap = null;

            return "Saving...";
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(String result) {

            ringProgressDialog.dismiss();
            if(imageResultPath != null) {
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                share_btn.setVisibility(View.VISIBLE);
                share_btn_anim.slideUp();
            }
        }


        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public void saveImageToGallery(Bitmap bitmap) {

        FileOutputStream outStream = null;

        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/FaceSwap");
            dir.mkdirs();

            String fileName = String.format("face_swap_%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);

            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);

            outStream.close();
            imageResultPath = outFile.getAbsolutePath();



            Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(outFile));
            sendBroadcast(mediaScanIntent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ringProgressDialog.dismiss();
            //Toast.makeText(this, "something went wrong please try again", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            ringProgressDialog.dismiss();
            //Toast.makeText(this,"something went wrong please try again",Toast.LENGTH_SHORT).show();
        }

    }

    public void share() {
        if (imageResultPath != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("image/jpeg");
            i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imageResultPath));
            i.putExtra(Intent.EXTRA_TEXT,"#facies #faceswap");
            startActivity(Intent.createChooser(i, "Share Image"));
        } else {
            Toast.makeText(this, "Please save an image", Toast.LENGTH_SHORT).show();
        }
    }

    //merge 2 Bitmaps
    public static Bitmap getBitmapOverlay(Bitmap bmp1, Bitmap bmp2, int left, int top) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, 0, 0, null);
        canvas.drawBitmap(bmp2, left, top, null);
        return bmOverlay;
    }
}


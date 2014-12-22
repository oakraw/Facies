package com.oakraw.facies.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.oakraw.facies.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rawipol on 12/20/14 AD.
 */
public class FaceAdapter extends BaseAdapter {

    ArrayList<Bitmap> all_faces;
    Context mContext;

    public FaceAdapter(Context context,ArrayList<Bitmap> all_faces) {
        this.all_faces = all_faces;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return all_faces.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.circular_face, parent, false);

        CircleImageView circleImageView = (CircleImageView) view.findViewById(R.id.face_image);
        if(position == all_faces.size()){
           /* FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)circleImageView.getLayoutParams();
            params.height = (int)AnimationManager.dipToPixels(40f);
            params.width = (int)AnimationManager.dipToPixels(40f);
            circleImageView.setLayoutParams(params);*/
            circleImageView.setBorderWidth(0);
            circleImageView.setImageResource(R.drawable.ic_add_more_face);
        }
        else {
            circleImageView.setImageBitmap(all_faces.get(position));
        }



        ((FrameLayout) view.findViewById(R.id.out)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelected(position);
            }
        });


        return view;
    }

    public interface OnSelectedListener
    {
        public void onSelected(int index);
    }

    private OnSelectedListener listener;

    public void setOnSelectedListener(OnSelectedListener listener)
    {
        this.listener = listener;
    }
}

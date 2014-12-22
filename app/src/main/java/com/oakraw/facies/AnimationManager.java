package com.oakraw.facies;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Rawipol on 12/21/14 AD.
 */
public class AnimationManager {

    private final AnimatorSet slide_down;
    private final AnimatorSet slide_up;
    private Object target_view;
    private static Context mContext;

    public AnimationManager(Context context ,Object target_view, int from_pos, int to_pos) {
        this.target_view = target_view;
        this.mContext = context;
        //Easing Animation
        slide_down = new AnimatorSet();
        slide_down.playTogether(Glider.glide(Skill.CircEaseOut, 1200, ObjectAnimator.ofFloat(target_view, "translationY", dipToPixels(from_pos), dipToPixels(to_pos))));
        slide_down.setDuration(500);
        slide_up = new AnimatorSet();
        slide_up.playTogether(Glider.glide(Skill.CircEaseOut, 1200, ObjectAnimator.ofFloat(target_view, "translationY", dipToPixels(to_pos), dipToPixels(from_pos))));
        slide_up.setDuration(500);

    }

    public static float dipToPixels(float dipValue) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public void slideUp(){
        slide_up.start();
    }

    public void slideDown(){
        slide_down.start();
    }
}

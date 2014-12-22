package com.oakraw.facies.singlefinger;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

class ViewOnTouchListener implements View.OnTouchListener {
    Point pushPoint;
    int lastImgLeft;
    int lastImgTop;
    FrameLayout.LayoutParams viewLP;
    FrameLayout.LayoutParams pushBtnLP;
    private FrameLayout.LayoutParams delBtnLP;
    int lastPushBtnLeft;
    int lastPushBtnTop;
    private View mPushView;
    private View mDeleteView;
    private Point delPoint;
    private int lastDelBtnLeft;
    private int lastDelBtnTop;
    private boolean isClick;
    private boolean isShowBtn = true;


    ViewOnTouchListener(View mPushView, View mDeleteView) {
        this.mPushView = mPushView;
        this.mDeleteView = mDeleteView;


    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isClick = true;
                if (null == viewLP) {
                    viewLP = (FrameLayout.LayoutParams) view.getLayoutParams();
                }
                if (null == pushBtnLP) {
                    pushBtnLP = (FrameLayout.LayoutParams) mPushView.getLayoutParams();
                }
                if (null == delBtnLP) {
                    delBtnLP = (FrameLayout.LayoutParams) mDeleteView.getLayoutParams();
                }
                pushPoint = getRawPoint(event);
                delPoint = getRawPoint(event);
                lastImgLeft = viewLP.leftMargin;
                lastImgTop = viewLP.topMargin;
                lastPushBtnLeft = pushBtnLP.leftMargin;
                lastPushBtnTop = pushBtnLP.topMargin;
                lastDelBtnLeft = delBtnLP.leftMargin;
                lastDelBtnTop = delBtnLP.topMargin;
                break;
            case MotionEvent.ACTION_MOVE:
                //push
                isClick = false;
                Point newPoint = getRawPoint(event);
                float moveX = newPoint.x - pushPoint.x;
                float moveY = newPoint.y - pushPoint.y;

                viewLP.leftMargin = (int) (lastImgLeft + moveX);
                viewLP.topMargin = (int) (lastImgTop + moveY);
                view.setLayoutParams(viewLP);

                pushBtnLP.leftMargin = (int) (lastPushBtnLeft + moveX);
                pushBtnLP.topMargin = (int) (lastPushBtnTop + moveY);
                mPushView.setLayoutParams(pushBtnLP);

                //del
                Point newdelPoint = getRawPoint(event);
                float movedelX = newdelPoint.x - delPoint.x;
                float movedelY = newdelPoint.y - delPoint.y;

                delBtnLP.leftMargin = (int) (lastDelBtnLeft + movedelX);
                delBtnLP.topMargin = (int) (lastDelBtnTop + movedelY);
                mDeleteView.setLayoutParams(delBtnLP);

                break;

            case MotionEvent.ACTION_UP:
                if(isClick){
                    if(isShowBtn){
                        mPushView.setVisibility(View.INVISIBLE);
                        mDeleteView.setVisibility(View.INVISIBLE);
                    }else{
                        mPushView.setVisibility(View.VISIBLE);
                        mDeleteView.setVisibility(View.VISIBLE);
                    }
                    isShowBtn = !isShowBtn;
                    isClick = false;
                }
                break;

        }
        return false;
    }


    private Point getRawPoint(MotionEvent event) {
        return new Point((int) event.getRawX(), (int) event.getRawY());
    }
}

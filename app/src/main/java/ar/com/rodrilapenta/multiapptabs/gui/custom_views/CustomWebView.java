package ar.com.rodrilapenta.multiapptabs.gui.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import ar.com.rodrilapenta.multiapptabs.R;

/**
 * Created by rodri on 3/06/2018.
 */

public class CustomWebView extends WebView {
    Context context;
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        gestureDetector = new GestureDetector(context, sogl);
        scaleGestureDetector = new ScaleGestureDetector(context, sosgl);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        boolean handled = gestureDetector.onTouchEvent(ev);
        scaleGestureDetector.onTouchEvent(ev);

        return handled;
    }

    @Override
    public void goBack() {
        if(getUrl() != null && canGoBack()) {
            setVisibility(View.INVISIBLE);
            animate(this, true);
            super.goBack();
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void goForward() {
        if(getUrl() != null && canGoForward()) {
            setVisibility(View.INVISIBLE);
            animate(this, false);
            super.goForward();
            setVisibility(View.VISIBLE);
        }
    }

    private void animate(final WebView view, Boolean back) {
        Animation anim;

        if(back) anim = AnimationUtils.loadAnimation(context,
                R.anim.anim_right_to_left);
        else anim = AnimationUtils.loadAnimation(context,
                R.anim.anim_left_to_right);
        view.startAnimation(anim);
    }

    GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = true;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            goBack();
                        } else {
                            goForward();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    };

    ScaleGestureDetector.SimpleOnScaleGestureListener sosgl = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){

            /*mScaleFactor *= scaleGestureDetector.getScaleFactor();

            mScaleFactor = Math.max(0.1f,

                    Math.min(mScaleFactor, 10.0f));

            mImageView.setScaleX(mScaleFactor);

            mImageView.setScaleY(mScaleFactor);*/



            return true;

        }
    };

    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }

    public void setScaleGestureDetector(ScaleGestureDetector scaleGestureDetector) {
        this.scaleGestureDetector = scaleGestureDetector;
    }
}
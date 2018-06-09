package ar.com.rodrilapenta.favsites.gui.custom_views;

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

import ar.com.rodrilapenta.favsites.R;

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
            return true;
        }
    };

    ScaleGestureDetector.SimpleOnScaleGestureListener sosgl = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
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
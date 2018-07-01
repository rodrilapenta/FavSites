package ar.com.rodrilapenta.favsites.gui.custom_views;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ar.com.rodrilapenta.favsites.R;

/**
 * Created by rodri on 3/06/2018.
 */

public class CustomWebView extends WebView {
    Context context;
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;
    private String title;
    private Toolbar toolbar;

    public CustomWebView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        toolbar = findViewById(R.id.toolbar);
        this.context = context;
        gestureDetector = new GestureDetector(context, sogl);
        scaleGestureDetector = new ScaleGestureDetector(context, sosgl);
        getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");

        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished (WebView view, String url) {
                toolbar.setTitle(view.getTitle());

                view.evaluateJavascript(
                "(function() { " +
                        "var metas = document.getElementsByTagName('meta'); \n" +
                        "\n" +
                        "   for (var i=0; i<metas.length; i++) { \n" +
                        "      if (metas[i].getAttribute(\"name\") == \"theme-color\") { \n" +
                        "         return metas[i].getAttribute(\"content\"); \n" +
                        "      } \n" +
                        "   }})();",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String backgroundColor) {
                        if(!backgroundColor.equals("null")) {
                            backgroundColor = backgroundColor.replace("\"", "").toUpperCase();
                            System.out.println("HTML COLOR: " + backgroundColor);
                            switch (backgroundColor) {
                                case "#FFFFFF":
                                    toolbar.setBackgroundColor(Color.parseColor("#000000"));
                                    ((Activity) context).getWindow().setStatusBarColor(Color.parseColor("#000000"));
                                    break;
                                default:
                                    toolbar.setBackgroundColor(Color.parseColor(backgroundColor.replace("\"", "")));
                                    ((Activity) context).getWindow().setStatusBarColor(Color.parseColor(backgroundColor.replace("\"", "")));
                                    break;
                            }
                        }
                        else {
                            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            ((Activity) context).getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                        }
                    }
                });
            }



        });

        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                toolbar.setTitle("Cargando...");
            }
        });
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }
}
package ar.com.rodrilapenta.multiapptabs.controller.asynctasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rodri on 3/06/2018.
 */

public class ImageLoadTask extends AsyncTask<Void, Void, Object> {

    private String url;
    private ImageView imageView;
    private Boolean roundedCorners;

    public ImageLoadTask(String url, ImageView imageView, Boolean roundedCorners) {
        this.url = url;
        this.imageView = imageView;
        this.roundedCorners = roundedCorners;
    }

    @Override
    protected Object doInBackground(Void... params) {
        try {
            URL urlConnection = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlConnection
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            if(roundedCorners) {
                float cornerRadius = 250.0f;

                /*
                    RoundedBitmapDrawable
                        A Drawable that wraps a bitmap and can be drawn with rounded corners. You
                        can create a RoundedBitmapDrawable from a file path, an input stream, or
                        from a Bitmap object.
                */
                /*
                    RoundedBitmapDrawableFactory
                        Constructs RoundedBitmapDrawable objects, either from Bitmaps directly, or
                        from streams and files.

                    public static RoundedBitmapDrawable create (Resources res, Bitmap bitmap)
                        Returns a new drawable by creating it from a bitmap, setting initial target
                        density based on the display metrics of the resources.
                */
                // Initialize a new RoundedBitmapDrawable object to make ImageView rounded corners
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(
                        Resources.getSystem(),
                        bitmap
                );

                // Set the RoundedBitmapDrawable corners radius
                roundedBitmapDrawable.setCornerRadius(cornerRadius);

                /*
                    setAntiAlias(boolean aa)
                        Enables or disables anti-aliasing for this drawable.
                */
                roundedBitmapDrawable.setAntiAlias(true);

                return roundedBitmapDrawable;
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if(result instanceof RoundedBitmapDrawable) {
            imageView.setImageDrawable((RoundedBitmapDrawable) result);
        }
        else {
            imageView.setImageBitmap((Bitmap) result);
        }
    }

}

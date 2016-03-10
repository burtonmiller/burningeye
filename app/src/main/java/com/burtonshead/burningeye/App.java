package com.burtonshead.burningeye;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.logic.Properties;
import com.burtonshead.burningeye.logic.Settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class App extends Application
{

    public static App mInstance;

    public static final float SCALE_CONSTANT = 0.44444444f;
    public static final float SCALE_TOLERANCE = 0.05f;

    private HashMap<Integer, Bitmap> mBitmaps;
    private MediaPlayer mBkgMusic;
    private HashMap<Integer, Bitmap> mExternalBitmaps;
    private HashMap<Bitmap, Integer> mKeys;
    private Properties mProps;
    private Settings mSettings;
    private static float mScreenWidth = 0;
    private static float mScreenHeight = 0;
    private static float mScaleFactor = 1.0f;

    private static float mTargetBitmapDensity = 1.0f; // mScreenHeight / SCALE_CONSTANT;


    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        this.mProps = new Properties();
        this.mSettings = new Settings(this);
        this.mSettings.load();
        this.mBitmaps = new HashMap();
        this.mKeys = new HashMap();
        this.mExternalBitmaps = new HashMap();
        this.mBkgMusic = MediaPlayer.create(this, R.raw.bkg_music_3);
        this.mBkgMusic.setLooping(true);
        this.mBkgMusic.setVolume(Eye.SPEED_FAST, Eye.SPEED_FAST);
        this.mProps.getGameType();

        // setup screen dims for scaling
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenHeight = Math.min(size.x, size.y);
        mScreenWidth = Math.max(size.x, size.y);

        // determine scale factor
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        // determine target bitmap density
        mTargetBitmapDensity = mScreenHeight * SCALE_CONSTANT;

        // determine scaling for other operations (not bitmaps)
        mScaleFactor = mTargetBitmapDensity / metrics.densityDpi;
    }

    public void onTerminate()
    {
        this.mSettings.store();
        releaseAllBitmaps(true);
        this.mBkgMusic.stop();
        this.mBkgMusic.release();
        super.onTerminate();
    }

    public void onLowMemory()
    {
        super.onLowMemory();
    }

    public static Properties getProps()
    {
        return mInstance.mProps;
    }

    public static Settings getSettings()
    {
        return mInstance.mSettings;
    }

    public static float getScreenWidth()
    {
        return mScreenWidth;
    }

    public static float getScreenHeight()
    {
        return mScreenHeight;
    }

    public static float getScaleFactor()
    {
        return mScaleFactor;
    }

    public Bitmap getBitmap(int resID)
    {
        Bitmap b = mBitmaps.get(Integer.valueOf(resID));
        if (b != null)
        {
            return b;
        }

        Bitmap sb = scaleBitmapforScreen(resID);

        mBitmaps.put(Integer.valueOf(resID), sb);
        mKeys.put(sb, Integer.valueOf(resID));
        return sb;
    }

    private Bitmap scaleBitmapforScreen(int resID)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inDensity = (int) mTargetBitmapDensity;
        options.inTargetDensity = (int) mTargetBitmapDensity;
        options.inJustDecodeBounds = false;
        options.inMutable = false;
        options.inScaled = true;
        options.inPreferQualityOverSpeed = true;

        Bitmap b = BitmapFactory.decodeResource(getResources(), resID, options);

        return b;
    }

//    public float getBitmapScale(Bitmap b)
//    {
//        int bitmapDensity = b.getDensity();
//
//        float scale = SCALE_CONSTANT / (bitmapDensity/mScreenHeight);
//
//        return scale;
//    }

//    private Bitmap scaleBitmapforScreen(int resID)
//    {
//        Bitmap b = BitmapFactory.decodeResource(getResources(), resID);
//
//        float scale = getBitmapScale(b);
//
//        // if bitmap is within tolerance, do not scale
//        if (scale < (1 + SCALE_TOLERANCE) && scale > (1 - SCALE_TOLERANCE))
//        {
//            return b;
//        }
//
//        // scale the bitmap to fit the device
//        Bitmap sb = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * scale), (int) (b.getHeight() * scale), true);
//
//        // cleanup original bitmap
//        b.recycle();
//
//        return sb;
//        //return b;
//    }

    public void releaseBitmap(int resID)
    {
        Bitmap b = (Bitmap) this.mBitmaps.remove(Integer.valueOf(resID));
        if (b != null)
        {
            b.recycle();
            this.mKeys.remove(b);
        }
    }

    public void releaseBitmap(Bitmap b)
    {
        if (b != null)
        {
            b.recycle();
            Integer key = (Integer) this.mKeys.remove(b);
            if (key != null)
            {
                this.mBitmaps.remove(key);
            }
        }
    }

    public void releaseAllBitmaps(boolean unlock)
    {
        Vector<Integer> keys = new Vector<>();
        keys.addAll(this.mBitmaps.keySet());
        Iterator<Integer> it = keys.iterator();
        while (it.hasNext())
        {
            releaseBitmap((it.next()));
        }
    }

    public synchronized Bitmap getExternalBitmap(int resID)
    {
        Bitmap b;
        b = (Bitmap) this.mBitmaps.get(Integer.valueOf(resID));
        if (b == null)
        {
            b = BitmapFactory.decodeResource(getResources(), resID);
            this.mExternalBitmaps.put(Integer.valueOf(resID), b);
        }
        return b;
    }

    public synchronized void releaseExternalBitmap(int resID)
    {
        Bitmap b = (Bitmap) this.mExternalBitmaps.remove(Integer.valueOf(resID));
        if (b != null)
        {
            b.recycle();
        }
    }

    public synchronized void playBkgMusic()
    {
        this.mBkgMusic.start();
    }

    public synchronized void pauseBkgMusic()
    {
        if (this.mBkgMusic.isPlaying())
        {
            this.mBkgMusic.pause();
        }
    }
}

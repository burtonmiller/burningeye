package com.burtonshead.burningeye;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.WindowManager;

import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.logic.Properties;
import com.burtonshead.burningeye.logic.Settings;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class App extends Application {
    public static App mInstance;
    private HashMap<Integer, Bitmap> mBitmaps;
    private MediaPlayer mBkgMusic;
    private HashMap<Integer, Bitmap> mExternalBitmaps;
    private HashMap<Bitmap, Integer> mKeys;
    private Properties mProps;
    private Settings mSettings;
    private static float mScreenWidth = 0;
    private static float mScreenHeight = 0;



    public void onCreate() {
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
    }

    public void onTerminate() {
        this.mSettings.store();
        releaseAllBitmaps(true);
        this.mBkgMusic.stop();
        this.mBkgMusic.release();
        super.onTerminate();
    }

    public void onLowMemory() {
        super.onLowMemory();
    }

    public static Properties getProps() {
        return mInstance.mProps;
    }

    public static Settings getSettings() {
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

    public Bitmap getBitmap(int resID) {
        Bitmap b = mBitmaps.get(Integer.valueOf(resID));
        if (b != null) {
            return b;
        }

        Bitmap sb = scaleBitmapforScreen(resID);

        mBitmaps.put(Integer.valueOf(resID), sb);
        mKeys.put(sb, Integer.valueOf(resID));
        return sb;
    }

    private Bitmap scaleBitmapforScreen(int resID)
    {
        Bitmap b = BitmapFactory.decodeResource(getResources(), resID);

//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) mScreenWidth, (int) mScreenHeight, true);
//        b.recycle();
//
//        return scaledBitmap;

        return b;
    }

    public void releaseBitmap(int resID) {
        Bitmap b = (Bitmap) this.mBitmaps.remove(Integer.valueOf(resID));
        if (b != null) {
            b.recycle();
            this.mKeys.remove(b);
        }
    }

    public void releaseBitmap(Bitmap b) {
        if (b != null) {
            b.recycle();
            Integer key = (Integer) this.mKeys.remove(b);
            if (key != null) {
                this.mBitmaps.remove(key);
            }
        }
    }

    public void releaseAllBitmaps(boolean unlock) {
        Vector<Integer> keys = new Vector();
        keys.addAll(this.mBitmaps.keySet());
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            releaseBitmap(((Integer) it.next()).intValue());
        }
    }

    public synchronized Bitmap getExternalBitmap(int resID) {
        Bitmap b;
        b = (Bitmap) this.mBitmaps.get(Integer.valueOf(resID));
        if (b == null) {
            b = BitmapFactory.decodeResource(getResources(), resID);
            this.mExternalBitmaps.put(Integer.valueOf(resID), b);
        }
        return b;
    }

    public synchronized void releaseExternalBitmap(int resID) {
        Bitmap b = (Bitmap) this.mExternalBitmaps.remove(Integer.valueOf(resID));
        if (b != null) {
            b.recycle();
        }
    }

    public synchronized void playBkgMusic() {
        this.mBkgMusic.start();
    }

    public synchronized void pauseBkgMusic() {
        if (this.mBkgMusic.isPlaying()) {
            this.mBkgMusic.pause();
        }
    }
}

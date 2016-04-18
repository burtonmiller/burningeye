package com.burtonshead.burningeye.logic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.gamespace.City;
import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.gamespace.GameSpace;
import com.burtonshead.burningeye.gamespace.Saucer;
import com.burtonshead.burningeye.misc.FPoint;
import com.burtonshead.burningeye.powerup.Powerup;

import java.util.Iterator;

public class GameSurface extends SurfaceView implements Callback
{
    private Bitmap[] mBeamBitmaps;
    private Matrix mBeamMatrix;
    private Bitmap mBkgBitmap;
    private Paint mBkgPaint;
    private Canvas mCanvas;
    private FPoint mCenter;
    private float mCenterBeamOffset;
    private float mCityOffsetX;
    private float mCityOffsetY;
    private Activity mContext;
    private Paint mDefaultPaint;
    private Eye mEye;
    private Bitmap mEyeBitmap;
    private float mEyeCenterXOffset;
    private float mEyeCenterYOffset;
    private Bitmap mEyeOverlay;
    private Bitmap[] mFocusBitmaps;
    private int mFocusIndex;
    private float mHeight;
    private SurfaceHolder mHolder;
    private boolean mInit;
    private float mMapXOffset;
    private float mMapYOffset;
    private DisplayMetrics mMetrics;
    private Matrix mOffsetMatrix;
    private Matrix mScaleMatrix;
    private Bitmap mScaledBeamBitmap;
    private Canvas mScaledBeamCanvas;
    private Paint mShadowPaint;
    private int mSlowOffset;
    private Bitmap mSlowPowerupBitmap;
    private boolean mSurfaceReady;
    private float mWidth;
    private float mXEye;
    private float mXEyeOffset;
    private float mXEyeOverlayPos;
    private float mYEye;
    private float mYEyeOffset;
    private float mYEyeOverlayPos;

    public GameSurface(Context context)
    {
        super(context);
        this.mInit = false;
        this.mSurfaceReady = false;
        this.mContext = null;
        this.mBkgBitmap = null;
        this.mBkgPaint = null;
        this.mDefaultPaint = null;
        this.mShadowPaint = null;
        this.mHeight = 320.0f;
        this.mWidth = 480.0f;
        this.mCenter = new FPoint(240.0f, 160.0f);
        this.mEye = null;
        this.mEyeBitmap = null;
        this.mXEyeOffset = 0.0f;
        this.mYEyeOffset = 0.0f;
        this.mEyeCenterXOffset = 0.0f;
        this.mEyeCenterYOffset = 0.0f;
        this.mXEye = 0.0f;
        this.mYEye = 0.0f;
        this.mEyeOverlay = null;
        this.mXEyeOverlayPos = 0.0f;
        this.mYEyeOverlayPos = 0.0f;
        this.mBeamBitmaps = null;
        this.mScaledBeamBitmap = null;
        this.mScaledBeamCanvas = null;
        this.mCenterBeamOffset = 15.0f;
        this.mFocusBitmaps = null;
        this.mFocusIndex = 0;
        this.mMapXOffset = 0.0f;
        this.mMapYOffset = 0.0f;
        this.mCityOffsetX = 0.0f;
        this.mCityOffsetY = 0.0f;
        this.mSlowOffset = 0;
    }

    public GameSurface(Context context, AttributeSet attr)
    {
        super(context, attr);
        this.mInit = false;
        this.mSurfaceReady = false;
        this.mContext = null;
        this.mBkgBitmap = null;
        this.mBkgPaint = null;
        this.mDefaultPaint = null;
        this.mShadowPaint = null;
        this.mHeight = 320.0f;
        this.mWidth = 480.0f;
        this.mCenter = new FPoint(240.0f, 160.0f);
        this.mEye = null;
        this.mEyeBitmap = null;
        this.mXEyeOffset = 0.0f;
        this.mYEyeOffset = 0.0f;
        this.mEyeCenterXOffset = 0.0f;
        this.mEyeCenterYOffset = 0.0f;
        this.mXEye = 0.0f;
        this.mYEye = 0.0f;
        this.mEyeOverlay = null;
        this.mXEyeOverlayPos = 0.0f;
        this.mYEyeOverlayPos = 0.0f;
        this.mBeamBitmaps = null;
        this.mScaledBeamBitmap = null;
        this.mScaledBeamCanvas = null;
        this.mCenterBeamOffset = 15.0f;
        this.mFocusBitmaps = null;
        this.mFocusIndex = 0;
        this.mMapXOffset = 0.0f;
        this.mMapYOffset = 0.0f;
        this.mCityOffsetX = 0.0f;
        this.mCityOffsetY = 0.0f;
        this.mSlowOffset = 0;
    }

    public GameSurface(Context context, AttributeSet attr, int defStyle)
    {
        super(context, attr, defStyle);
        this.mInit = false;
        this.mSurfaceReady = false;
        this.mContext = null;
        this.mBkgBitmap = null;
        this.mBkgPaint = null;
        this.mDefaultPaint = null;
        this.mShadowPaint = null;
        this.mHeight = 320.0f;
        this.mWidth = 480.0f;
        this.mCenter = new FPoint(240.0f, 160.0f);
        this.mEye = null;
        this.mEyeBitmap = null;
        this.mXEyeOffset = 0.0f;
        this.mYEyeOffset = 0.0f;
        this.mEyeCenterXOffset = 0.0f;
        this.mEyeCenterYOffset = 0.0f;
        this.mXEye = 0.0f;
        this.mYEye = 0.0f;
        this.mEyeOverlay = null;
        this.mXEyeOverlayPos = 0.0f;
        this.mYEyeOverlayPos = 0.0f;
        this.mBeamBitmaps = null;
        this.mScaledBeamBitmap = null;
        this.mScaledBeamCanvas = null;
        this.mCenterBeamOffset = 15.0f;
        this.mFocusBitmaps = null;
        this.mFocusIndex = 0;
        this.mMapXOffset = 0.0f;
        this.mMapYOffset = 0.0f;
        this.mCityOffsetX = 0.0f;
        this.mCityOffsetY = 0.0f;
        this.mSlowOffset = 0;
    }

    public void setMapImg(int resID)
    {
        if (this.mBkgBitmap != null)
        {
            App.sApp.releaseBitmap(this.mBkgBitmap);
        }
        this.mBkgBitmap = App.sApp.getBitmap(resID);
    }

    public void setMapImg(Bitmap b)
    {

    }

    public void updateGraphics()
    {
        if (this.mSurfaceReady)
        {
            this.mCanvas = this.mHolder.lockCanvas();
            drawEye();
            drawBkg();
            drawCities();
            drawSaucerShadows();
            drawEyeBeam();
            drawSaucerBeams();
            drawSaucers();
            drawEffects();
            this.mHolder.unlockCanvasAndPost(this.mCanvas);
        }
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        this.mSurfaceReady = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        this.mSurfaceReady = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    public void init(Activity context)
    {
        this.mEyeBitmap = null;
        this.mEyeOverlay = null;
        this.mBeamBitmaps = null;
        this.mScaledBeamBitmap = null;
        this.mFocusBitmaps = null;
        this.mSlowPowerupBitmap = null;
        this.mContext = context;
        this.mMetrics = new DisplayMetrics();
        this.mContext.getWindowManager().getDefaultDisplay().getMetrics(this.mMetrics);
        this.mHeight = (float) this.mMetrics.heightPixels;
        this.mWidth = (float) this.mMetrics.widthPixels;
        this.mCenter = new FPoint(this.mWidth / Eye.Y_TOLERANCE, this.mHeight / Eye.Y_TOLERANCE);
        this.mCenterBeamOffset = 15.0f * this.mMetrics.density;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mSurfaceReady = false;
        this.mDefaultPaint = new Paint();
        this.mShadowPaint = new Paint();
        this.mShadowPaint.setColor(805306368);
        this.mEye = GameLogic.mInstance.getEye();
        this.mBkgBitmap = App.sApp.getBitmap(R.drawable.map_1);
        this.mMapXOffset = (float) ((-(this.mBkgBitmap.getWidth() - this.mMetrics.widthPixels)) / 2);
        this.mMapYOffset = (float) ((-(this.mBkgBitmap.getHeight() - this.mMetrics.heightPixels)) / 2);
        this.mEyeBitmap = this.mEye.getEyeBitmap();
        this.mSlowPowerupBitmap = App.sApp.getBitmap(R.drawable.slow_bkg);
        this.mBeamMatrix = new Matrix();
        this.mScaleMatrix = new Matrix();
        this.mOffsetMatrix = new Matrix();
        Bitmap cityBitmap = App.sApp.getBitmap(R.drawable.city_100);
        this.mCityOffsetX = (float) (cityBitmap.getWidth() / 2);
        this.mCityOffsetY = (float) (cityBitmap.getHeight() / 2);
        this.mEyeOverlay = App.sApp.getBitmap(R.drawable.eye_overlay);
        this.mXEyeOverlayPos = (float) ((this.mMetrics.widthPixels - this.mEyeOverlay.getWidth()) / 2);
        this.mYEyeOverlayPos = (float) ((this.mMetrics.heightPixels - this.mEyeOverlay.getHeight()) / 2);
        loadBeamBitmaps();
        this.mXEyeOffset = ((this.mWidth * 5.0f) / 12.0f) - ((float) (this.mEyeBitmap.getWidth() / 2));
        this.mYEyeOffset = ((this.mHeight * 5.0f) / 12.0f) - ((float) (this.mEyeBitmap.getHeight() / 2));
        this.mEyeCenterXOffset = (float) (this.mEyeBitmap.getWidth() / 2);
        this.mEyeCenterYOffset = (float) (this.mEyeBitmap.getHeight() / 2);
    }

    public void loadBeamBitmaps()
    {
        this.mCenterBeamOffset = this.mEye.mRadius;
        this.mBeamBitmaps = this.mEye.getBeamBitmaps();
        if (this.mScaledBeamBitmap != null)
        {
            this.mScaledBeamBitmap.recycle();
            this.mScaledBeamBitmap = null;
        }
        this.mScaledBeamBitmap = Bitmap.createScaledBitmap(this.mBeamBitmaps[0], (int) (500.0f * this.mMetrics.density), (int) (((float) this.mBeamBitmaps[0].getHeight()) * this.mEye.mRadiusScale), true);
        this.mScaledBeamCanvas = new Canvas(this.mScaledBeamBitmap);
        this.mFocusBitmaps = this.mEye.getFocusBitmaps();
    }

    private void drawBkg()
    {
        //mCanvas.drawBitmap(this.mBkgBitmap, this.mMapXOffset, this.mMapYOffset, this.mBkgPaint);

        mCanvas.drawBitmap(
                this.mBkgBitmap,
                new Rect(0, 0, (int)mBkgBitmap.getWidth(), (int)mBkgBitmap.getHeight()),
                new Rect(0, 0, (int)App.getScreenWidth(), (int)App.getScreenHeight()),
                mBkgPaint);
    }

    private void drawEye()
    {
        this.mDefaultPaint.setColor(-1);
        float m = Math.abs(Math.abs(((this.mEye.mBeamX * Eye.Y_TOLERANCE) - this.mWidth) / this.mWidth) - 1.0f);
        float y = (((0.7f + m) / Eye.Y_TOLERANCE) * this.mEye.mBeamY) + ((((1.0f - m) + 0.3f) / Eye.Y_TOLERANCE) * (this.mHeight / Eye.Y_TOLERANCE));
        this.mXEye = (((0.9f * this.mEye.mBeamX) + (0.1f * (this.mWidth / Eye.Y_TOLERANCE))) / 6.0f) + this.mXEyeOffset;
        this.mYEye = (y / 6.0f) + this.mYEyeOffset;
        this.mCanvas.drawBitmap(this.mEye.getEyeBitmap(), this.mXEye, this.mYEye, this.mBkgPaint);
        this.mCanvas.drawBitmap(this.mEyeOverlay, this.mXEyeOverlayPos, this.mYEyeOverlayPos, this.mBkgPaint);
    }

    private void drawEyeBeamWorking()
    {
        if (this.mEye.mReload)
        {
            this.mEye.mReload = false;
            loadBeamBitmaps();
        }
        float x = this.mXEye + this.mEyeCenterXOffset;
        float y = this.mYEye + this.mEyeCenterYOffset;
        float width = 100.0f * this.mMetrics.density;
        y -= this.mCenterBeamOffset;
        float beamX = this.mEye.mBeamX;
        float beamY = this.mEye.mBeamY - this.mEye.mRadius;
        float a = (float) Math.hypot((double) (x - beamX), (double) (y - beamY));
        float b = width;
        float c = (float) Math.hypot((double) (beamX - (x + width)), (double) (beamY - y));
        float angle = (float) Math.toDegrees(Math.acos((double) ((((a * a) + (b * b)) - (c * c)) / ((Eye.Y_TOLERANCE * a) * b))));
        if (beamY < y)
        {
            angle *= -1.0f;
        }
        this.mScaleMatrix.setScale(a / width, this.mEye.mRadiusScale);
        this.mScaledBeamBitmap.eraseColor(0);
        Canvas canvas = this.mScaledBeamCanvas;
        Bitmap[] bitmapArr = this.mBeamBitmaps;
        canvas.drawBitmap(bitmapArr[(int) (Math.random() * 6.0d)], this.mScaleMatrix, this.mDefaultPaint);
        this.mOffsetMatrix.setTranslate(x, y);
        this.mBeamMatrix.setRotate(angle, 0.0f, this.mCenterBeamOffset);
        this.mBeamMatrix.setConcat(this.mOffsetMatrix, this.mBeamMatrix);
        this.mCanvas.drawBitmap(this.mScaledBeamBitmap, this.mBeamMatrix, this.mDefaultPaint);
        float focusOffset = this.mEye.mRadius * Eye.Y_TOLERANCE;

        canvas = this.mCanvas;
        bitmapArr = this.mFocusBitmaps;
        int i = this.mFocusIndex;
        Eye eye = this.mEye;
        canvas.drawBitmap(bitmapArr[i], eye.mBeamX - focusOffset, eye.mBeamY - focusOffset, this.mDefaultPaint);
        this.mFocusIndex = (this.mFocusIndex + 1) % 4;
        this.mDefaultPaint.setColor(-1);
    }

    private void drawEyeBeam()
    {
        if (mEye.mReload)
        {
            mEye.mReload = false;
            loadBeamBitmaps();
        }

        float focusOffset = mEye.mRadius * 2f;

        float x = mXEye + mEyeCenterXOffset;
        float y = mYEye + mEyeCenterYOffset;
        float width = 100.0f * mMetrics.density;
        //width = 100f * App.getScaleFactor(); //???
        y -= mCenterBeamOffset;
        float beamX = mEye.mBeamX;
        float beamY = mEye.mBeamY - mEye.mRadius;
        float a = (float) Math.hypot((double) (x - beamX), (double) (y - beamY));
        float b = width;
        float c = (float) Math.hypot((double) (beamX - (x + width)), (double) (beamY - y));
        float angle = (float) Math.toDegrees(Math.acos((double) ((((a * a) + (b * b)) - (c * c)) / ((Eye.Y_TOLERANCE * a) * b))));
        if (beamY < y)
        {
            angle *= -1.0f;
        }
        // pick a random bitmap from the beam bitmaps
        int beamIndex = (int) (Math.random() * 6.0d);

        // scale the width
        mScaledBeamBitmap.eraseColor(0);
        Canvas canvas = mScaledBeamCanvas;
        int beamHeight = mBeamBitmaps[beamIndex].getHeight();
        int beamWidth = mBeamBitmaps[beamIndex].getWidth();
        int newBeamHeight = (int) (mEye.mRadius * 2);
        int newBeamWidth = (int) Math.max(1, a);
        //Log.i("GameSurface", "drawEyeBeam: w = " + beamWidth + ", h = " + beamHeight + ", nw = " + newBeamWidth + ", nh = " + newBeamHeight);
        if (beamWidth != newBeamWidth || beamHeight != newBeamHeight)
        {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap
                    (mBeamBitmaps[beamIndex], newBeamWidth, newBeamHeight, true);
            canvas.drawBitmap(resizedBitmap, 0, 0, mDefaultPaint);
            resizedBitmap.recycle();
        }
        else
        {
            canvas.drawBitmap(mBeamBitmaps[beamIndex], 0, 0, mDefaultPaint);
        }

        mOffsetMatrix.setTranslate(x, y);
        mBeamMatrix.setRotate(angle, 0.0f, mCenterBeamOffset);
        mBeamMatrix.setConcat(mOffsetMatrix, mBeamMatrix);
        mCanvas.drawBitmap(mScaledBeamBitmap, mBeamMatrix, mDefaultPaint);

        canvas = mCanvas;
        int i = mFocusIndex;
        Eye eye = mEye;
        canvas.drawBitmap(mFocusBitmaps[i], eye.mBeamX - focusOffset, eye.mBeamY - focusOffset, mDefaultPaint);
        mFocusIndex = (mFocusIndex + 1) % 4;
        mDefaultPaint.setColor(-1);
    }


    private void drawCities()
    {
        Iterator it = GameSpace.mInstance.getCities().iterator();
        while (it.hasNext())
        {
            City c = (City) it.next();
            this.mCanvas.drawBitmap(c.getBitmap(), c.mPosition.x - this.mCityOffsetX, c.mPosition.y - this.mCityOffsetY, this.mDefaultPaint);
        }
    }

    private void drawSaucerShadows()
    {
        Iterator it = GameSpace.mInstance.getSaucers().iterator();
        while (it.hasNext())
        {
            Saucer s = (Saucer) it.next();
            float radius = ((float) s.mRadius) * this.mMetrics.density;
            float xPos = s.mPosition.x + ((radius * Eye.Y_TOLERANCE) * (-((s.mPosition.x - this.mCenter.x) / this.mCenter.x)));
            float yPos = s.mPosition.y + ((radius * Eye.Y_TOLERANCE) * (-((s.mPosition.y - this.mCenter.y) / this.mCenter.y)));
            radius *= 0.85f;
            this.mCanvas.drawCircle(xPos, yPos, radius, this.mShadowPaint);
            this.mCanvas.drawCircle(xPos, yPos, 0.9f * radius, this.mShadowPaint);
        }
    }

    private void drawSaucerBeams()
    {
        Iterator it = GameSpace.mInstance.getSaucers().iterator();
        while (it.hasNext())
        {
            Saucer s = (Saucer) it.next();
            if (s.getState() == 2 && s.mTargets != null)
            {
                int color = s.getBeamColor();
                Iterator it2 = s.mTargets.iterator();
                while (it2.hasNext())
                {
                    City t = (City) it2.next();
                    this.mDefaultPaint.setColor(-570425345);
                    this.mCanvas.drawCircle(t.mPosition.x, t.mPosition.y, 8.0f, this.mDefaultPaint);
                    this.mDefaultPaint.setColor(color);
                    this.mCanvas.drawCircle(t.mPosition.x, t.mPosition.y, (float) s.getBeamTargetRadius(), this.mDefaultPaint);
                    this.mDefaultPaint.setColor(1140850688 + color);
                    this.mDefaultPaint.setStyle(Style.STROKE);
                    this.mDefaultPaint.setStrokeWidth(1.0f);
                    this.mCanvas.drawCircle(t.mPosition.x, t.mPosition.y, (float) s.getBeamTargetRadius(), this.mDefaultPaint);
                    this.mDefaultPaint.setStyle(Style.FILL);
                    this.mDefaultPaint.setColor(-570425345);
                    this.mDefaultPaint.setStrokeWidth((float) s.getInnerBeamWidth());
                    this.mCanvas.drawLine(s.mPosition.x, s.mPosition.y, t.mPosition.x, t.mPosition.y, this.mDefaultPaint);
                    this.mDefaultPaint.setColor(color);
                    this.mDefaultPaint.setStrokeWidth(16.0f);
                    this.mCanvas.drawLine(s.mPosition.x, s.mPosition.y, t.mPosition.x, t.mPosition.y, this.mDefaultPaint);
                    this.mDefaultPaint.setColor(-1);
                }
            }
        }
    }

    private void drawSaucers()
    {
        Iterator it = GameSpace.mInstance.getSaucers().iterator();
        while (it.hasNext())
        {
            Saucer s = (Saucer) it.next();
            Bitmap b;
            if (s.getState() == 11)
            {
                float percentLeft = s.getExplodePercentLeft();
                if (percentLeft > 0.0f)
                {
                    float innerRadius = ((float) s.mRadius) * percentLeft;
                    float outerRadius = ((float) s.mRadius) * (1.0f + (0.5f * percentLeft));
                    this.mDefaultPaint.setColor(-18);
                    this.mDefaultPaint.setAlpha((int) (200.0f * percentLeft));
                    this.mCanvas.drawCircle(s.mPosition.x, s.mPosition.y, innerRadius, this.mDefaultPaint);
                    this.mDefaultPaint.setAlpha((int) (100.0f * percentLeft));
                    this.mCanvas.drawCircle(s.mPosition.x, s.mPosition.y, outerRadius, this.mDefaultPaint);
                    this.mDefaultPaint.setAlpha((int) (64.0f + (191.0f * percentLeft)));
                    b = s.getBitmap();
                    this.mCanvas.drawBitmap(b, s.mPosition.x - ((float) (b.getWidth() / 2)), s.mPosition.y - ((float) (b.getHeight() / 2)), this.mDefaultPaint);
                    this.mDefaultPaint.setAlpha(255);
                }
            } else if (s.getState() != 10 || s.mTimeLeft <= 0)
            {
                b = s.getBitmap();
                this.mCanvas.drawBitmap(b, s.mPosition.x - ((float) (b.getWidth() / 2)), s.mPosition.y - ((float) (b.getHeight() / 2)), this.mDefaultPaint);
                this.mDefaultPaint.setAlpha(255);
            } else
            {
                int alpha = 255 - ((int) (((double) s.mTimeLeft) * 0.1275d));
                this.mDefaultPaint.setAlpha(alpha);
                Bitmap b1 = s.getBitmap();
                this.mCanvas.drawBitmap(b1, s.mPosition.x - ((float) (b1.getWidth() / 2)), s.mPosition.y - ((float) (b1.getHeight() / 2)), this.mDefaultPaint);
                this.mDefaultPaint.setAlpha(255 - alpha);
                Bitmap b2 = s.getDimShiftBitmap();
                this.mCanvas.drawBitmap(b2, s.mPosition.x - ((float) (b2.getWidth() / 2)), s.mPosition.y - ((float) (b2.getHeight() / 2)), this.mDefaultPaint);
            }
        }
    }

    private void drawEffects()
    {
        Powerup p = GameLogic.mInstance.mActivePowerup;
        if (p != null)
        {
            switch (p.mType)
            {
                case Powerup.POWERUP_SLOW /*7*/:
                    mSlowOffset = (this.mSlowOffset + 1) % 20;
                    float scalingFactor = GameLogic.mMetrics.density;
                    float scaledOffset = mSlowOffset * scalingFactor;
                    float maxOffset = 20 * scalingFactor;
                    //mCanvas.drawBitmap(this.mSlowPowerupBitmap, (float) (-this.mSlowOffset), 0.0f, this.mBkgPaint);

                    mCanvas.drawBitmap(
                            this.mSlowPowerupBitmap,
                            new Rect(0, 0, (int)mSlowPowerupBitmap.getWidth(), (int)mSlowPowerupBitmap.getHeight()),
                            new Rect(-(int)scaledOffset, 0, (int)(App.getScreenWidth() + maxOffset - scaledOffset), (int)App.getScreenHeight()),
                            mBkgPaint);
                default:
            }
        }
    }
}

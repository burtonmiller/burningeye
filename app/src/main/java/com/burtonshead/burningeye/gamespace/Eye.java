package com.burtonshead.burningeye.gamespace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.logic.GameLogic;

public class Eye {
    private static final float[] BEAM_DMG;

    private static final float[] BASE_BEAM_RADIUS;
    private static float[] BEAM_RADIUS = null;

    public static final float BEAM_RADIUS_DEFAULT;

    public static final int BEAM_NORMAL = 0;
    public static final int BEAM_SHOCK = 1;
    public static final int BEAM_FIRE = 2;
    public static final int BEAM_BLACKHOLE = 3;
    public static final int BEAM_WIDE = 4;
    public static final int BEAM_XWIDE = 5;

    public static final float SPEED_FAST = 0.4f;
    public static final float SPEED_NORMAL = 0.25f;
    public static final float SPEED_SLOW = 0.15f;

    private static int MAX_X = 0;
    private static int MAX_Y = 0;
    private static float MAX_MOVE = 0.0f;

    public static final float X_TOLERANCE = 2.0f;
    public static final float Y_TOLERANCE = 2.0f;

    private Bitmap[] mBeamBitmaps;
    public int mBeamType;
    public float mBeamX;
    public float mBeamY;
    private Context mContext;
    public float mDamage;
    private float mEyeBitmapIndex;
    private Bitmap[] mEyeBitmaps;
    public float mEyeSpeed;
    private Bitmap[] mFocusBitmaps;
    private DisplayMetrics mMetrics;
    public float mRadius;
    public float mRadiusScale;
    public boolean mReload;
    public float testOrientX;
    public float testOrientY;
    public float testOrientZ;

    static {
        BEAM_DMG = new float[]{1.25f, 2.5f, 5.0f, 25.0f, 1.5f, 1.75f};
        BASE_BEAM_RADIUS = new float[]{15.0f, 15.0f, 15.0f, 15.0f, 30.0f, 45.0f};
        BEAM_RADIUS = new float[6];
        BEAM_RADIUS_DEFAULT = BEAM_RADIUS[BEAM_NORMAL];
    }

    public Eye(Context c) {
        mReload = false;
        mBeamType = BEAM_NORMAL;
        mRadius = BEAM_RADIUS[BEAM_NORMAL];
        mRadiusScale = 1.0f;
        mEyeSpeed = SPEED_NORMAL;
        mDamage = BEAM_DMG[BEAM_NORMAL];
        testOrientX = BEAM_RADIUS_DEFAULT;
        testOrientY = BEAM_RADIUS_DEFAULT;
        testOrientZ = BEAM_RADIUS_DEFAULT;
        mEyeBitmapIndex = BEAM_RADIUS_DEFAULT;
        mContext = c;
        mMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        MAX_X = mMetrics.widthPixels;
        MAX_Y = (int) (mMetrics.heightPixels - (mRadius * 2));
        mBeamX = (float) (MAX_X / 2);
        mBeamY = (float) (MAX_Y / 2);
        for (int i = BEAM_NORMAL; i < BEAM_RADIUS.length; i += BEAM_SHOCK) {
            BEAM_RADIUS[i] = BASE_BEAM_RADIUS[i] * mMetrics.density;
        }
        MAX_MOVE = 10.0f * mMetrics.density;
        mBeamBitmaps = new Bitmap[6];
        mFocusBitmaps = new Bitmap[BEAM_WIDE];
        mEyeBitmaps = new Bitmap[BEAM_WIDE];
        setBeamType(BEAM_NORMAL);
        loadEyeBitmaps();
    }

    public void recenter() {
        mBeamX = GameLogic.mMetrics.widthPixels / 2;
        mBeamY = GameLogic.mMetrics.heightPixels / 2;
    }

    public void setSpeed(float speed) {
        mEyeSpeed = speed;
    }

    public void setBeamType(int t) {
        mBeamType = t;
        mRadius = BEAM_RADIUS[mBeamType] * App.getScaleFactor(); //???
        mRadiusScale = BEAM_RADIUS[mBeamType] / BEAM_RADIUS[BEAM_NORMAL];
        MAX_Y = (int) (mMetrics.heightPixels - (mRadius * 2));
        mDamage = BEAM_DMG[mBeamType];
        loadEyeBitmaps();
    }

    public Bitmap getEyeBitmap() {
        float step = SPEED_FAST * GameLogic.mStepMult;
        mEyeBitmapIndex = (mEyeBitmapIndex + step) % 4.0f;
        //Log.i("getEyeBitmap", "*** " + step + " ****");
        return mEyeBitmaps[(int) mEyeBitmapIndex];
    }

    public Bitmap[] getBeamBitmaps() {
        for (int i = BEAM_NORMAL; i < mBeamBitmaps.length; i += BEAM_SHOCK) {
            App.sApp.releaseBitmap(mBeamBitmaps[i]);
        }
        switch (mBeamType) {
            case BEAM_SHOCK /*1*/:
                mBeamBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_yellow_1);
                mBeamBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_yellow_2);
                mBeamBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_yellow_3);
                mBeamBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_yellow_4);
                mBeamBitmaps[BEAM_WIDE] = App.sApp.getBitmap(R.drawable.beam_yellow_5);
                mBeamBitmaps[BEAM_XWIDE] = App.sApp.getBitmap(R.drawable.beam_yellow_6);
                break;
            case BEAM_FIRE /*2*/:
                mBeamBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_red_1);
                mBeamBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_red_2);
                mBeamBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_red_3);
                mBeamBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_red_4);
                mBeamBitmaps[BEAM_WIDE] = App.sApp.getBitmap(R.drawable.beam_red_5);
                mBeamBitmaps[BEAM_XWIDE] = App.sApp.getBitmap(R.drawable.beam_red_6);
                break;
            case BEAM_BLACKHOLE /*3*/:
                mBeamBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_black_1);
                mBeamBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_black_2);
                mBeamBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_black_3);
                mBeamBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_black_4);
                mBeamBitmaps[BEAM_WIDE] = App.sApp.getBitmap(R.drawable.beam_black_5);
                mBeamBitmaps[BEAM_XWIDE] = App.sApp.getBitmap(R.drawable.beam_black_6);
                break;
            default:
                mBeamBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_blue_1);
                mBeamBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_blue_2);
                mBeamBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_blue_3);
                mBeamBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_blue_4);
                mBeamBitmaps[BEAM_WIDE] = App.sApp.getBitmap(R.drawable.beam_blue_5);
                mBeamBitmaps[BEAM_XWIDE] = App.sApp.getBitmap(R.drawable.beam_blue_6);
                break;
        }
        return mBeamBitmaps;
    }

    public Bitmap[] getFocusBitmaps() {
        int i;
        Bitmap[] oldBitmaps = new Bitmap[mFocusBitmaps.length];
        for (i = BEAM_NORMAL; i < oldBitmaps.length; i += BEAM_SHOCK) {
            oldBitmaps[i] = mFocusBitmaps[i];
        }
        Bitmap[] temp = new Bitmap[BEAM_WIDE];
        switch (mBeamType) {
            case BEAM_SHOCK /*1*/:
                temp[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_focus_yellow_1);
                temp[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_focus_yellow_2);
                temp[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_focus_yellow_3);
                temp[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_focus_yellow_4);
                break;
            case BEAM_FIRE /*2*/:
                temp[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_focus_red_1);
                temp[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_focus_red_2);
                temp[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_focus_red_3);
                temp[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_focus_red_4);
                break;
            case BEAM_BLACKHOLE /*3*/:
                temp[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_focus_black_1);
                temp[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_focus_black_2);
                temp[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_focus_black_3);
                temp[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_focus_black_4);
                break;
            default:
                temp[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.beam_focus_blue_1);
                temp[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.beam_focus_blue_2);
                temp[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.beam_focus_blue_3);
                temp[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.beam_focus_blue_4);
                break;
        }
        int dim = (int) (mRadius * 4.0f);
        for (i = 0; i < temp.length; i++) {
            mFocusBitmaps[i] = Bitmap.createScaledBitmap(temp[i], dim, dim, true);
            App.sApp.releaseBitmap(temp[i]);
        }
        for (i = 0; i < oldBitmaps.length; i++) {
            if (oldBitmaps[i] != null) {
                oldBitmaps[i].recycle();
            }
        }
        return mFocusBitmaps;
    }

    public void loadEyeBitmaps() {
        int i;
        if (mEyeBitmaps == null) {
            mEyeBitmaps = new Bitmap[BEAM_WIDE];
        }
        Bitmap[] temp = new Bitmap[BEAM_WIDE];
        for (i = BEAM_NORMAL; i < mEyeBitmaps.length; i += BEAM_SHOCK) {
            temp[i] = mEyeBitmaps[i];
        }
        switch (mBeamType) {
            case BEAM_SHOCK /*1*/:
                mEyeBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.eye_yellow_1);
                mEyeBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.eye_yellow_2);
                mEyeBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.eye_yellow_3);
                mEyeBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.eye_yellow_4);
                break;
            case BEAM_FIRE /*2*/:
                mEyeBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.eye_red_1);
                mEyeBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.eye_red_2);
                mEyeBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.eye_red_3);
                mEyeBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.eye_red_4);
                break;
            case BEAM_BLACKHOLE /*3*/:
                mEyeBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.eye_black_1);
                mEyeBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.eye_black_2);
                mEyeBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.eye_black_3);
                mEyeBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.eye_black_4);
                break;
            default:
                mEyeBitmaps[BEAM_NORMAL] = App.sApp.getBitmap(R.drawable.eye_blue_1);
                mEyeBitmaps[BEAM_SHOCK] = App.sApp.getBitmap(R.drawable.eye_blue_2);
                mEyeBitmaps[BEAM_FIRE] = App.sApp.getBitmap(R.drawable.eye_blue_3);
                mEyeBitmaps[BEAM_BLACKHOLE] = App.sApp.getBitmap(R.drawable.eye_blue_4);
                break;
        }
        for (i = BEAM_NORMAL; i < temp.length; i += BEAM_SHOCK) {
            if (temp[i] != mEyeBitmaps[i]) {
                App.sApp.releaseBitmap(temp[i]);
            }
        }
    }

    public void moveBeam(float orientX, float orientY, float stepMult) {
        float m;
        float move = (mEyeSpeed * stepMult) * mMetrics.density;
        float lastOrientX = calcCalibratedValue(orientX);
        float lastOrientY = calcCalibratedValue(orientY);
        if (lastOrientY < -2.0f || lastOrientY > Y_TOLERANCE) {
            m = move * lastOrientY;
            if (m > MAX_MOVE) {
                m = MAX_MOVE;
            } else if (m < (-MAX_MOVE)) {
                m = -MAX_MOVE;
            }
            mBeamY -= m;
        }
        if (lastOrientX < -2.0f || lastOrientX > Y_TOLERANCE) {
            m = move * lastOrientX;
            if (m > MAX_MOVE) {
                m = MAX_MOVE;
            } else if (m < (-MAX_MOVE)) {
                m = -MAX_MOVE;
            }
            mBeamX += m;
        }
        if (mBeamY < BEAM_RADIUS_DEFAULT) {
            mBeamY = BEAM_RADIUS_DEFAULT;
        } else if (mBeamY > ((float) MAX_Y)) {
            mBeamY = (float) MAX_Y;
        }
        if (mBeamX < BEAM_RADIUS_DEFAULT) {
            mBeamX = BEAM_RADIUS_DEFAULT;
        } else if (mBeamX > ((float) MAX_X)) {
            mBeamX = (float) MAX_X;
        }
    }

    private float calcCalibratedValue(float v) {
        if (v > 180.0f) {
            return v - 0.012451172f;
        }
        if (v < -180.0f) {
            return v + 360.0f;
        }
        return v;
    }
}

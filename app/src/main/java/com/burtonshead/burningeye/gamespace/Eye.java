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
        this.mReload = false;
        this.mBeamType = BEAM_NORMAL;
        this.mRadius = BEAM_RADIUS[BEAM_NORMAL];
        this.mRadiusScale = 1.0f;
        this.mEyeSpeed = SPEED_NORMAL;
        this.mDamage = BEAM_DMG[BEAM_NORMAL];
        this.testOrientX = BEAM_RADIUS_DEFAULT;
        this.testOrientY = BEAM_RADIUS_DEFAULT;
        this.testOrientZ = BEAM_RADIUS_DEFAULT;
        this.mEyeBitmapIndex = BEAM_RADIUS_DEFAULT;
        this.mContext = c;
        this.mMetrics = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(this.mMetrics);
        MAX_X = this.mMetrics.widthPixels;
        MAX_Y = this.mMetrics.heightPixels;
        this.mBeamX = (float) (MAX_X / BEAM_FIRE);
        this.mBeamY = (float) (MAX_Y / BEAM_FIRE);
        for (int i = BEAM_NORMAL; i < BEAM_RADIUS.length; i += BEAM_SHOCK) {
            BEAM_RADIUS[i] = BASE_BEAM_RADIUS[i] * this.mMetrics.density;
        }
        MAX_MOVE = 10.0f * this.mMetrics.density;
        this.mBeamBitmaps = new Bitmap[6];
        this.mFocusBitmaps = new Bitmap[BEAM_WIDE];
        this.mEyeBitmaps = new Bitmap[BEAM_WIDE];
        setBeamType(BEAM_NORMAL);
        loadEyeBitmaps();
    }

    public void recenter() {
        this.mBeamX = GameLogic.mMetrics.widthPixels / 2;
        this.mBeamY = GameLogic.mMetrics.heightPixels / 2;
    }

    public void setSpeed(float speed) {
        this.mEyeSpeed = speed;
    }

    public void setBeamType(int t) {
        this.mBeamType = t;
        this.mRadius = BEAM_RADIUS[this.mBeamType] * App.getScaleFactor(); //???
        this.mRadiusScale = BEAM_RADIUS[this.mBeamType] / BEAM_RADIUS[BEAM_NORMAL];
        this.mDamage = BEAM_DMG[this.mBeamType];
        loadEyeBitmaps();
    }

    public Bitmap getEyeBitmap() {
        float step = SPEED_FAST * GameLogic.mStepMult;
        this.mEyeBitmapIndex = (this.mEyeBitmapIndex + step) % 4.0f;
        //Log.i("getEyeBitmap", "*** " + step + " ****");
        return this.mEyeBitmaps[(int) this.mEyeBitmapIndex];
    }

    public Bitmap[] getBeamBitmaps() {
        for (int i = BEAM_NORMAL; i < this.mBeamBitmaps.length; i += BEAM_SHOCK) {
            App.mInstance.releaseBitmap(this.mBeamBitmaps[i]);
        }
        switch (this.mBeamType) {
            case BEAM_SHOCK /*1*/:
                this.mBeamBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_yellow_1);
                this.mBeamBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_yellow_2);
                this.mBeamBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_yellow_3);
                this.mBeamBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_yellow_4);
                this.mBeamBitmaps[BEAM_WIDE] = App.mInstance.getBitmap(R.drawable.beam_yellow_5);
                this.mBeamBitmaps[BEAM_XWIDE] = App.mInstance.getBitmap(R.drawable.beam_yellow_6);
                break;
            case BEAM_FIRE /*2*/:
                this.mBeamBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_red_1);
                this.mBeamBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_red_2);
                this.mBeamBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_red_3);
                this.mBeamBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_red_4);
                this.mBeamBitmaps[BEAM_WIDE] = App.mInstance.getBitmap(R.drawable.beam_red_5);
                this.mBeamBitmaps[BEAM_XWIDE] = App.mInstance.getBitmap(R.drawable.beam_red_6);
                break;
            case BEAM_BLACKHOLE /*3*/:
                this.mBeamBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_black_1);
                this.mBeamBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_black_2);
                this.mBeamBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_black_3);
                this.mBeamBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_black_4);
                this.mBeamBitmaps[BEAM_WIDE] = App.mInstance.getBitmap(R.drawable.beam_black_5);
                this.mBeamBitmaps[BEAM_XWIDE] = App.mInstance.getBitmap(R.drawable.beam_black_6);
                break;
            default:
                this.mBeamBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_blue_1);
                this.mBeamBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_blue_2);
                this.mBeamBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_blue_3);
                this.mBeamBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_blue_4);
                this.mBeamBitmaps[BEAM_WIDE] = App.mInstance.getBitmap(R.drawable.beam_blue_5);
                this.mBeamBitmaps[BEAM_XWIDE] = App.mInstance.getBitmap(R.drawable.beam_blue_6);
                break;
        }
        return this.mBeamBitmaps;
    }

    public Bitmap[] getFocusBitmaps() {
        int i;
        Bitmap[] oldBitmaps = new Bitmap[this.mFocusBitmaps.length];
        for (i = BEAM_NORMAL; i < oldBitmaps.length; i += BEAM_SHOCK) {
            oldBitmaps[i] = this.mFocusBitmaps[i];
        }
        Bitmap[] temp = new Bitmap[BEAM_WIDE];
        switch (this.mBeamType) {
            case BEAM_SHOCK /*1*/:
                temp[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_focus_yellow_1);
                temp[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_focus_yellow_2);
                temp[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_focus_yellow_3);
                temp[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_focus_yellow_4);
                break;
            case BEAM_FIRE /*2*/:
                temp[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_focus_red_1);
                temp[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_focus_red_2);
                temp[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_focus_red_3);
                temp[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_focus_red_4);
                break;
            case BEAM_BLACKHOLE /*3*/:
                temp[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_focus_black_1);
                temp[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_focus_black_2);
                temp[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_focus_black_3);
                temp[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_focus_black_4);
                break;
            default:
                temp[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.beam_focus_blue_1);
                temp[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.beam_focus_blue_2);
                temp[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.beam_focus_blue_3);
                temp[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.beam_focus_blue_4);
                break;
        }
        int dim = (int) (this.mRadius * 4.0f);
        for (i = 0; i < temp.length; i++) {
            this.mFocusBitmaps[i] = Bitmap.createScaledBitmap(temp[i], dim, dim, true);
            App.mInstance.releaseBitmap(temp[i]);
        }
        for (i = 0; i < oldBitmaps.length; i++) {
            if (oldBitmaps[i] != null) {
                oldBitmaps[i].recycle();
            }
        }
        return this.mFocusBitmaps;
    }

    public void loadEyeBitmaps() {
        int i;
        if (this.mEyeBitmaps == null) {
            this.mEyeBitmaps = new Bitmap[BEAM_WIDE];
        }
        Bitmap[] temp = new Bitmap[BEAM_WIDE];
        for (i = BEAM_NORMAL; i < this.mEyeBitmaps.length; i += BEAM_SHOCK) {
            temp[i] = this.mEyeBitmaps[i];
        }
        switch (this.mBeamType) {
            case BEAM_SHOCK /*1*/:
                this.mEyeBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.eye_yellow_1);
                this.mEyeBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.eye_yellow_2);
                this.mEyeBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.eye_yellow_3);
                this.mEyeBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.eye_yellow_4);
                break;
            case BEAM_FIRE /*2*/:
                this.mEyeBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.eye_red_1);
                this.mEyeBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.eye_red_2);
                this.mEyeBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.eye_red_3);
                this.mEyeBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.eye_red_4);
                break;
            case BEAM_BLACKHOLE /*3*/:
                this.mEyeBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.eye_black_1);
                this.mEyeBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.eye_black_2);
                this.mEyeBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.eye_black_3);
                this.mEyeBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.eye_black_4);
                break;
            default:
                this.mEyeBitmaps[BEAM_NORMAL] = App.mInstance.getBitmap(R.drawable.eye_blue_1);
                this.mEyeBitmaps[BEAM_SHOCK] = App.mInstance.getBitmap(R.drawable.eye_blue_2);
                this.mEyeBitmaps[BEAM_FIRE] = App.mInstance.getBitmap(R.drawable.eye_blue_3);
                this.mEyeBitmaps[BEAM_BLACKHOLE] = App.mInstance.getBitmap(R.drawable.eye_blue_4);
                break;
        }
        for (i = BEAM_NORMAL; i < temp.length; i += BEAM_SHOCK) {
            if (temp[i] != this.mEyeBitmaps[i]) {
                App.mInstance.releaseBitmap(temp[i]);
            }
        }
    }

    public void moveBeam(float orientX, float orientY, float stepMult) {
        float m;
        float move = (this.mEyeSpeed * stepMult) * this.mMetrics.density;
        float lastOrientX = calcCalibratedValue(orientX);
        float lastOrientY = calcCalibratedValue(orientY);
        if (lastOrientY < -2.0f || lastOrientY > Y_TOLERANCE) {
            m = move * lastOrientY;
            if (m > MAX_MOVE) {
                m = MAX_MOVE;
            } else if (m < (-MAX_MOVE)) {
                m = -MAX_MOVE;
            }
            this.mBeamY -= m;
        }
        if (lastOrientX < -2.0f || lastOrientX > Y_TOLERANCE) {
            m = move * lastOrientX;
            if (m > MAX_MOVE) {
                m = MAX_MOVE;
            } else if (m < (-MAX_MOVE)) {
                m = -MAX_MOVE;
            }
            this.mBeamX += m;
        }
        if (this.mBeamY < BEAM_RADIUS_DEFAULT) {
            this.mBeamY = BEAM_RADIUS_DEFAULT;
        } else if (this.mBeamY > ((float) MAX_Y)) {
            this.mBeamY = (float) MAX_Y;
        }
        if (this.mBeamX < BEAM_RADIUS_DEFAULT) {
            this.mBeamX = BEAM_RADIUS_DEFAULT;
        } else if (this.mBeamX > ((float) MAX_X)) {
            this.mBeamX = (float) MAX_X;
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

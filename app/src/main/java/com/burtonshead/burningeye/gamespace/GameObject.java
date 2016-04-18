package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;
import android.util.Log;
import com.burtonshead.burningeye.logic.GameLogic;
import com.burtonshead.burningeye.misc.FPoint;
import java.util.Vector;
import org.json.JSONObject;

public abstract class GameObject {
    public static final int APPEAR_TIME = 2000;
    public static final int CITY_NA = 100;
    protected static String DEST_X_KEY = null;
    protected static String DEST_Y_KEY = null;
    protected static String HP_KEY = null;
    protected static String LAST_STATE_KEY = null;
    protected static String NEXT_X_KEY = null;
    protected static String NEXT_Y_KEY = null;
    protected static String OBJECT_TYPE_KEY = null;
    public static final int OBJECT_UNDEFINED = -1;
    public static final int OBSTACLE_DEFAULT = 200;
    protected static String POS_X_KEY = null;
    protected static String POS_Y_KEY = null;
    public static final int SAUCER_AQUA_X = 10;
    public static final int SAUCER_BLACK_X = 8;
    public static final int SAUCER_BLUE = 1;
    public static final int SAUCER_DARKBLUE = 7;
    public static final int SAUCER_FACET_X = 13;
    public static final int SAUCER_GLASS_X = 11;
    public static final int SAUCER_GREEN = 0;
    public static final int SAUCER_GREEN_X = 12;
    public static final int SAUCER_ORANGE = 5;
    public static final int SAUCER_PURPLE = 4;
    public static final int SAUCER_RED = 2;
    public static final int SAUCER_SHIMMER_X = 14;
    public static final int SAUCER_WHITE = 3;
    public static final int SAUCER_YELLOW = 6;
    public static final int STATE_APPEAR = 10;
    public static final int STATE_ATTACK = 2;
    public static final int STATE_DEAD = 20;
    public static final int STATE_EXPLODE = 11;
    public static final int STATE_HIT = 3;
    protected static String STATE_KEY = null;
    public static final int STATE_MOVE = 1;
    public static final int STATE_STATIONARY = 0;
    protected static String TIME_LEFT_KEY;
    protected static GameSpace mGameSpace;
    protected static Vector<Integer> mSoundQ;
    public FPoint mDest;
    public int mExplodeTime;
    public float mHP;
    protected int mLastState;
    public FPoint mNext;
    public FPoint mPosition;
    public int mRadius;
    public float mSpeed;
    protected int mState;
    protected boolean mStateChanged;
    public long mTimeLeft;

    public abstract Bitmap getBitmap();

    public GameObject() {
        this.mExplodeTime = 500;
        this.mSpeed = 1.0f;
        this.mHP = 1.0f;
        this.mPosition = new FPoint(0.0f, 0.0f);
        this.mDest = new FPoint(0.0f, 0.0f);
        this.mNext = new FPoint(0.0f, 0.0f);
        this.mTimeLeft = 0;
        this.mState = SAUCER_GREEN;
        this.mLastState = SAUCER_GREEN;
        this.mStateChanged = false;
    }

    public boolean setState(int newState) {
        if (newState == this.mState) {
            return false;
        }
        if (this.mState == STATE_EXPLODE && newState != STATE_DEAD) {
            return false;
        }
        if (this.mState == STATE_DEAD) {
            return false;
        }
        if (this.mState != STATE_HIT || this.mHP > 0.0f) {
            this.mState = newState;
        } else {
            this.mState = STATE_EXPLODE;
        }
        if (this.mState == STATE_EXPLODE) {
            this.mTimeLeft = (long) this.mExplodeTime;
        } else if (this.mState == STATE_APPEAR) {
            this.mTimeLeft = 2000;
        }
        playSounds();
        return true;
    }

    public int getState() {
        return this.mState;
    }

    public void update() {
        this.mStateChanged = this.mLastState != this.mState;
        this.mLastState = this.mState;
    }

    public void playSounds() {
    }

    public static int getNextSound() {
        if (mSoundQ.isEmpty()) {
            return OBJECT_UNDEFINED;
        }
        return ((Integer) mSoundQ.remove(SAUCER_GREEN)).intValue();
    }

    public boolean arrived() {
        return this.mPosition.distanceFrom(this.mDest) < 3.0f;
    }

    public void inflictDamage(float damage) {
        this.mHP -= (GameLogic.mTimeDiff / 1000.0f) * damage;
    }

    public void calcNextPos() {
        FPoint.getPointOnLine(this.mPosition, this.mDest, GameLogic.mStepMult * (this.mSpeed * GameLogic.mInstance.getSpeedMult()), this.mNext);
    }

    public void move() {
        this.mPosition.x = this.mNext.x;
        this.mPosition.y = this.mNext.y;
    }

    public float getExplodePercentLeft() {
        //Log.i("***** GameObject.getExplodePercentLeft *****", "mTimeLeft = " + this.mTimeLeft);
        return ((float) this.mTimeLeft) / ((float) this.mExplodeTime);
    }

    public static GameObject build(JSONObject j) {
        GameObject o = null;
        try {
            switch (j.getInt(OBJECT_TYPE_KEY)) {
                case SAUCER_GREEN /*0*/:
                    o = new GreenSaucer(0.0f, 0.0f);
                    break;
                case STATE_MOVE /*1*/:
                    o = new BlueSaucer(0.0f, 0.0f);
                    break;
                case STATE_ATTACK /*2*/:
                    o = new RedSaucer(0.0f, 0.0f);
                    break;
                case STATE_HIT /*3*/:
                    o = new WhiteSaucer(0.0f, 0.0f);
                    break;
                case SAUCER_PURPLE /*4*/:
                    o = new PurpleSaucer(0.0f, 0.0f);
                    break;
                case SAUCER_ORANGE /*5*/:
                    o = new OrangeSaucer(0.0f, 0.0f);
                    break;
                case SAUCER_YELLOW /*6*/:
                    o = new YellowSaucer(0.0f, 0.0f);
                    break;
                case SAUCER_DARKBLUE /*7*/:
                    o = new DarkBlueSaucer(0.0f, 0.0f);
                    break;
                case CITY_NA /*100*/:
                    o = new City();
                    break;
                default:
                    o = new Obstacle(STATE_APPEAR, null);
                    break;
            }
            o.load(j);
        } catch (Exception e) {
            Log.e("GameObject.build", e.toString());
        }
        return o;
    }

    public void load(JSONObject j) {
        try {
            this.mPosition.x = (float) j.getDouble(POS_X_KEY);
            this.mPosition.y = (float) j.getDouble(POS_Y_KEY);
            this.mDest.x = (float) j.getDouble(DEST_X_KEY);
            this.mDest.y = (float) j.getDouble(DEST_Y_KEY);
            this.mNext.x = (float) j.getDouble(NEXT_X_KEY);
            this.mNext.y = (float) j.getDouble(NEXT_Y_KEY);
            this.mHP = (float) j.getDouble(HP_KEY);
            this.mState = j.getInt(STATE_KEY);
            this.mLastState = j.getInt(LAST_STATE_KEY);
            this.mTimeLeft = j.getLong(TIME_LEFT_KEY);
            this.mStateChanged = true;
        } catch (Exception e) {
            Log.e("GameObject.load", e.toString());
        }
    }

    public JSONObject store() {
        JSONObject j = new JSONObject();
        int objectType = OBJECT_UNDEFINED;
        try {
            if (this instanceof GreenSaucer) {
                objectType = SAUCER_GREEN;
            } else if (this instanceof BlueSaucer) {
                objectType = STATE_MOVE;
            } else if (this instanceof RedSaucer) {
                objectType = STATE_ATTACK;
            } else if (this instanceof WhiteSaucer) {
                objectType = STATE_HIT;
            } else if (this instanceof PurpleSaucer) {
                objectType = SAUCER_PURPLE;
            } else if (this instanceof OrangeSaucer) {
                objectType = SAUCER_ORANGE;
            } else if (this instanceof YellowSaucer) {
                objectType = SAUCER_YELLOW;
            } else if (this instanceof DarkBlueSaucer) {
                objectType = SAUCER_DARKBLUE;
            } else if (this instanceof City) {
                objectType = CITY_NA;
            } else if (this instanceof Obstacle) {
                objectType = OBSTACLE_DEFAULT;
            }
            j.put(OBJECT_TYPE_KEY, objectType);
            j.put(POS_X_KEY, (double) this.mPosition.x);
            j.put(POS_Y_KEY, (double) this.mPosition.y);
            j.put(DEST_X_KEY, (double) this.mDest.x);
            j.put(DEST_Y_KEY, (double) this.mDest.y);
            j.put(NEXT_X_KEY, (double) this.mNext.x);
            j.put(NEXT_Y_KEY, (double) this.mNext.y);
            j.put(HP_KEY, (double) this.mHP);
            j.put(STATE_KEY, this.mState);
            j.put(LAST_STATE_KEY, this.mLastState);
            j.put(TIME_LEFT_KEY, this.mTimeLeft);
        } catch (Exception e) {
            Log.e("GameObject.store", e.toString());
        }
        return j;
    }

    static {
        OBJECT_TYPE_KEY = "object_type";
        POS_X_KEY = "pos_x";
        POS_Y_KEY = "pos_y";
        HP_KEY = "hp";
        STATE_KEY = "state";
        LAST_STATE_KEY = "last_state";
        DEST_X_KEY = "dest_x";
        DEST_Y_KEY = "dest_y";
        NEXT_X_KEY = "next_x";
        NEXT_Y_KEY = "next_y";
        TIME_LEFT_KEY = "time_left";
        mSoundQ = new Vector();
    }

    protected void addSound(int resID) {
        mSoundQ.add(Integer.valueOf(resID));
    }
}

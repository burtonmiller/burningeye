package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.logic.GameLogic;

import org.apache.commons.lang.time.FastDateFormat;

public class City extends GameObject
{
    private static final int BASE_HP = 15;
    public Saucer mAttacker;
    private Bitmap[] mBitmaps;
    private Bitmap[] mExpBitmaps;

    public City()
    {
        this.mExplodeTime = 850;
        restoreHP();
    }

    public void restoreHP()
    {
        this.mHP = 15.0f;
    }

    public Bitmap getBitmap()
    {
        if (this.mBitmaps == null)
        {
            this.mBitmaps = new Bitmap[5];
            this.mBitmaps[4] = App.mInstance.getBitmap(R.drawable.city_100);
            this.mBitmaps[3] = App.mInstance.getBitmap(R.drawable.city_80);
            this.mBitmaps[2] = App.mInstance.getBitmap(R.drawable.city_60);
            this.mBitmaps[1] = App.mInstance.getBitmap(R.drawable.city_40);
            this.mBitmaps[0] = App.mInstance.getBitmap(R.drawable.city_20);
            this.mExpBitmaps = new Bitmap[3];
            this.mExpBitmaps[0] = App.mInstance.getBitmap(R.drawable.city_exp_1);
            this.mExpBitmaps[1] = App.mInstance.getBitmap(R.drawable.city_exp_2);
            this.mExpBitmaps[2] = App.mInstance.getBitmap(R.drawable.city_exp_3);
        }
        float percent = Math.max(this.mHP, 0.0f) / 15.1f;
        switch (getState())
        {
            case GameObject.STATE_EXPLODE /*11*/:
                return this.mExpBitmaps[(int) (Math.random() * 3.0d)];
            default:
                return this.mBitmaps[(int) (5.0f * percent)];
        }
    }

    public void playSounds()
    {
        if (getState() == 11)
        {
            addSound(R.raw.scream_sound);
        }
    }

    //??? added breaks
//    public static final int STATE_APPEAR = 10;
//    public static final int STATE_ATTACK = 2;
//    public static final int STATE_DEAD = 20;
//    public static final int STATE_EXPLODE = 11;
//    public static final int STATE_HIT = 3;
//    protected static String STATE_KEY = null;
//    public static final int STATE_MOVE = 1;
//    public static final int STATE_STATIONARY = 0;

    public void update()
    {
        super.update();
        switch (getState())
        {
            case GameObject.STATE_STATIONARY:
                if (mGameSpace.mHitCities.contains(this))
                {
                    setState(3);
                }
                break;
            case GameObject.STATE_HIT:
                if (this.mHP <= 0.0f)
                {
                    setState(11);
                } else if (mGameSpace.mHitCities.contains(this))
                {
                    setState(0);
                }
                break;
            case GameObject.STATE_EXPLODE /*11*/:
                this.mTimeLeft = (long) (((float) this.mTimeLeft) - GameLogic.mTimeDiff);
                if (this.mTimeLeft <= 0)
                {
                    setState(20);
                }
                break;
            case GameObject.STATE_DEAD /*20*/:
                mGameSpace.addDeadCity(this);
                break;
            default:
        }
    }

    public void inflictDamage(float damage)
    {
        super.inflictDamage(damage);
    }
}

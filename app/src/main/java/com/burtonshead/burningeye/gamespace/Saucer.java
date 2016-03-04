package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.logic.GameLogic;
import com.burtonshead.burningeye.powerup.Powerup;

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.time.FastDateFormat;

public abstract class Saucer extends GameObject
{
    protected Bitmap[] mAttackBitmaps;
    public int mBeamColor;
    public float mBeamTargetRadius;
    protected float mBitmapAttackIndex;
    protected int mBitmapNormalIndex;
    public float mChange;
    public float mChangeMult;
    public float mDamage;
    protected boolean mHasReward;
    protected Bitmap[] mHitBitmaps;
    public int mInnerBeamWidth;
    protected Bitmap[] mNormalBitmaps;
    protected float mPulseStep;
    public float mRange;
    protected float mRewardChance;
    protected int mRewardScale;
    private float mStep;
    public Vector<City> mTargets;
    public int mValue;

    public abstract Bitmap getDimShiftBitmap();

    public Saucer(int radius, float x, float y, float speed, float damage, float range, float hp, float rewardChance, int rewardScale, int value)
    {
        this.mDamage = 1.0f;
        this.mRange = 100.0f;
        this.mBeamColor = -1435445248;
        this.mInnerBeamWidth = 5;
        this.mBeamTargetRadius = 8.0f;
        this.mChangeMult = 1.0f;
        this.mChange = 0.1f;
        this.mValue = 0;
        this.mTargets = new Vector();
        this.mRewardChance = 0.0f;
        this.mRewardScale = 0;
        this.mBitmapAttackIndex = 0.0f;
        this.mBitmapNormalIndex = 0;
        this.mPulseStep = 0.33f;
        this.mStep = 0.0f;
        this.mHasReward = false;
        this.mRadius = radius;
        this.mPosition.x = x;
        this.mPosition.y = y;
        this.mSpeed = speed;
        this.mDamage = damage;
        this.mRange = GameLogic.mMetrics.density * range;
        this.mHP = hp;
        this.mRewardChance = rewardChance;
        this.mRewardScale = rewardScale;
        this.mValue = value;
        if (App.getProps().getGameType() == 1)
        {
            this.mRewardScale = Math.max(this.mRewardScale, 20);
        }
    }

    public Bitmap getBitmap()
    {
        if (this.mStep >= 1.0f)
        {
            this.mStep = 0.0f;
        }
        this.mStep += GameLogic.mStepMult;
        if (this.mStep > 1.0f)
        {
            this.mStep = 1.0f;
        }
        if (this.mState == 3)
        {
            return this.mHitBitmaps[(int) (Math.random() * 3.0d)];
        }
        if (this.mState == 11)
        {
            return getDimShiftBitmap();
        }
        if (this.mState == 20)
        {
            return App.mInstance.getBitmap(R.drawable.empty_image);
        }
        if (this.mState == 2)
        {
            this.mBitmapAttackIndex = (this.mBitmapAttackIndex + (this.mPulseStep * this.mStep)) % ((float) this.mAttackBitmaps.length);
            int index = (int) this.mBitmapAttackIndex;
            if (index == this.mAttackBitmaps.length - 1)
            {
                this.mPulseStep = -Math.abs(this.mPulseStep);
            } else if (index == 0)
            {
                this.mPulseStep = Math.abs(this.mPulseStep);
            }
            return this.mAttackBitmaps[index];
        }
        this.mBitmapNormalIndex = (int) ((((float) this.mBitmapNormalIndex) + (this.mStep * 1.0f)) % ((float) this.mNormalBitmaps.length));
        return this.mNormalBitmaps[this.mBitmapNormalIndex];
    }

    public void playSounds()
    {
        switch (getState())
        {
            case GameObject.STATE_APPEAR /*10*/:
                addSound(R.raw.appear_sound);
            case GameObject.STATE_EXPLODE /*11*/:
                calcReward();
                addSound(hasReward() ? R.raw.power_up_sound : R.raw.explosion_sound);
            default:
        }
    }

    public boolean hasReward()
    {
        return this.mHasReward;
    }

    public void calcReward()
    {
        this.mHasReward = Math.random() <= ((double) this.mRewardChance);
    }

    public Powerup getReward()
    {
        int r = (int) (Math.random() * ((double) this.mRewardScale));
        if (r <= 10)
        {
            return new Powerup(Powerup.POWERUP_DMG_SHOCK);
        }
        if (r < 20)
        {
            return new Powerup(Powerup.POWERUP_WIDE);
        }
        if (r < 30)
        {
            return new Powerup(Powerup.POWERUP_SLOW);
        }
        if (r < 40)
        {
            return new Powerup(Powerup.POWERUP_DMG_FIRE);
        }
        if (r < 50)
        {
            return new Powerup(Powerup.POWERUP_BOMB);
        }
        if (r < 60)
        {
            return new Powerup(Powerup.POWERUP_XWIDE);
        }
        return new Powerup(Powerup.POWERUP_DMG_BLACKHOLE);
    }

    public void update()
    {
        super.update();
        switch (getState())
        {
            case STATE_STATIONARY:
                if (this.mHP > 0.0f)
                {
                    setState(STATE_MOVE);
                    mGameSpace.randomLocation(this.mDest, (float) this.mRadius);
                }
                break;
            case STATE_MOVE:
                if (arrived())
                {
                    Vector<City> targets = mGameSpace.findTarget(this);
                    if (targets != null)
                    {
                        this.mTargets = targets;
                        setState(STATE_ATTACK);
                        return;
                    }
                    mGameSpace.randomLocation(this.mDest, (float) this.mRadius);
                    return;
                }
                calcNextPos();
                if (mGameSpace.collideSaucer(this))
                {
                    setState(STATE_STATIONARY);
                } else
                {
                    move();
                }
                break;
            case STATE_ATTACK:
                Iterator it = this.mTargets.iterator();
                while (it.hasNext())
                {
                    City t = (City) it.next();
                    if (t.getState() == 11 || t.getState() == 20)
                    {
                        this.mTargets.remove(t);
                    } else
                    {
                        mGameSpace.addHitCity(t);
                        t.inflictDamage(this.mDamage);
                    }
                }
                if (this.mTargets.isEmpty())
                {
                    setState(STATE_STATIONARY);
                }
                break;
            case STATE_HIT:
                this.mTargets.clear();
                if (this.mHP <= 0.0f)
                {
                    setState(STATE_EXPLODE);
                }
                break;
            case STATE_APPEAR:
                this.mTimeLeft = (long) (((float) this.mTimeLeft) - GameLogic.mTimeDiff);
                if (this.mTimeLeft <= 0)
                {
                    setState(STATE_STATIONARY);
                }
                break;
            case STATE_EXPLODE:
                this.mTimeLeft = (long) (((float) this.mTimeLeft) - GameLogic.mTimeDiff);
                if (this.mTimeLeft <= 0)
                {
                    setState(STATE_DEAD);
                }
                break;
            case STATE_DEAD:
                GameLogic.mInstance.addScore((long) this.mValue);
                mGameSpace.addDeadSaucer(this);
                break;
            default:
        }
    }

    public void inflictDamage(float damage)
    {
        if (this.mState == 10)
        {
            damage *= Eye.Y_TOLERANCE;
        }
        super.inflictDamage(damage);
    }

    public int getBeamColor()
    {
        return this.mBeamColor;
    }

    public int getInnerBeamWidth()
    {
        if (this.mChangeMult > Eye.Y_TOLERANCE || this.mChangeMult < 1.0f)
        {
            this.mChange = -this.mChange;
        }
        this.mChangeMult += this.mChange;
        int width = (int) (((float) this.mInnerBeamWidth) * this.mChangeMult);
        width *= GameLogic.mMetrics.density;

        return width;
    }

    public int getBeamTargetRadius()
    {
        int radius = (int) (((double) (this.mBeamTargetRadius * this.mChangeMult)) * 1.5d);
        radius *= GameLogic.mMetrics.density;

        return radius;
    }
}

package com.burtonshead.burningeye.powerup;

import android.util.Log;

import com.burtonshead.burningeye.R;

import org.json.JSONObject;

public class Powerup
{
    public static final int POWERUP_NONE = 0;
    public static final int POWERUP_DMG_SHOCK = 1;
    public static final int POWERUP_DMG_FIRE = 2;
    public static final int POWERUP_DMG_BLACKHOLE = 3;
    public static final int POWERUP_WIDE = 4;
    public static final int POWERUP_XWIDE = 5;
    public static final int POWERUP_BOMB = 6;
    public static final int POWERUP_SLOW = 7;

    public int mTimeLeft;
    public int mType;

    public Powerup(int pType)
    {
        mTimeLeft = POWERUP_NONE;
        mType = pType;
        switch (this.mType)
        {
            case POWERUP_BOMB /*6*/:
                mTimeLeft = 2000;
                break;
            case POWERUP_SLOW /*7*/:
                mTimeLeft = 5000;
                break;
            default:
                mTimeLeft = 10000;
        }
    }

    public Powerup(JSONObject j)
    {
        this.mTimeLeft = POWERUP_NONE;
        load(j);
    }

    public int getDrawableID()
    {
        switch (this.mType)
        {
            case POWERUP_DMG_FIRE /*2*/:
                return R.drawable.powerup_dmg_fire_button;
            case POWERUP_DMG_BLACKHOLE /*3*/:
                return R.drawable.powerup_dmg_blackhole_button;
            case POWERUP_WIDE /*4*/:
                return R.drawable.powerup_wide_button;
            case POWERUP_XWIDE /*5*/:
                return R.drawable.powerup_xwide_button;
            case POWERUP_BOMB /*6*/:
                return R.drawable.powerup_bomb_button;
            case POWERUP_SLOW /*7*/:
                return R.drawable.powerup_slow_button;
            default:
                return R.drawable.powerup_dmg_shock_button;
        }
    }

    public void load(JSONObject j)
    {
        try
        {
            this.mType = j.getInt(POWERUP_TYPE_KEY);
            this.mTimeLeft = j.getInt(POWERUP_TIME_LEFT_KEY);
        }
        catch (Exception e)
        {
            Log.e("Powerup.load", e.toString());
        }
    }

    public JSONObject store()
    {
        JSONObject j = new JSONObject();
        try
        {
            j.put(POWERUP_TYPE_KEY, this.mType);
            j.put(POWERUP_TIME_LEFT_KEY, this.mTimeLeft);
        }
        catch (Exception e)
        {
            Log.e("Powerup.store", e.toString());
        }
        return j;
    }

    //*** NON-PUBLIC ***

    private static final String POWERUP_TIME_LEFT_KEY = "powerup_time_left_key";
    private static final String POWERUP_TYPE_KEY = "powerup_type";


}

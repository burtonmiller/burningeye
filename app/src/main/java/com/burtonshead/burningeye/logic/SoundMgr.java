package com.burtonshead.burningeye.logic;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by burton on 3/7/16.
 */
class SoundMgr
{
    private MediaPlayer mLoopPaused;
    private HashMap<Integer, MediaPlayer> mLoops;
    private boolean mPaused;
    private HashMap<Integer, Integer> mSoundMap;
    private SoundPool mSoundPool;
    private HashMap<Integer, Float> mSoundVol;

    private Context mContext;

    public SoundMgr(Context c)
    {
        mContext = c;
        mSoundMap = new HashMap<>();
        mSoundVol = new HashMap<>();
        mLoops = new HashMap<>();
        mPaused = false;
        mSoundPool = new SoundPool(50, GameLogic.STATE_OVER, GameLogic.STATE_NEW);
    }

    public void loadSound(int resID, float vol)
    {
        int id = mSoundPool.load(mContext, resID, GameLogic.STATE_RESUME);
        mSoundMap.put(resID, id);
        mSoundVol.put(id, vol);
    }

    public void playSound(int resID)
    {
        int id = mSoundMap.get(resID);
        float vol = mSoundVol.get(id);
        mSoundPool.play(id, vol, vol, GameLogic.STATE_RESUME, GameLogic.STATE_NEW, 1.0f);
    }

    public void loadLoop(int resID, float vol)
    {
        MediaPlayer m = MediaPlayer.create(mContext, resID);
        m.setLooping(true);
        m.setVolume(vol, vol);
        mLoops.put(resID, m);
    }

    public void startLoop(int resID)
    {
        MediaPlayer loop = mLoops.get(resID);
        if (mPaused)
        {
            mLoopPaused = loop;
            return;
        }
        for (MediaPlayer m : mLoops.values())
        {
            if (!loop.equals(m) && m.isPlaying())
            {
                m.pause();
            }
        }
        loop.start();
    }

    public void stopLoop(int resID)
    {
        MediaPlayer loop = mLoops.get(resID);
        if (mLoopPaused.equals(loop))
        {
            mLoopPaused = null;
        }
        loop.pause();
    }

    public void pauseLoops()
    {
        mPaused = true;
        for (MediaPlayer m : mLoops.values())
        {
            if (m.isPlaying())
            {
                m.pause();
                mLoopPaused = m;
            }
        }
    }

    public void resumeLoops()
    {
        mPaused = false;
        if (mLoopPaused != null)
        {
            mLoopPaused.start();
            mLoopPaused = null;
        }
    }

    public void cleanup()
    {
        pauseLoops();
        mSoundMap.clear();
        mSoundPool.release();
        for (MediaPlayer m : mLoops.values())
        {
            m.stop();
            m.release();
        }
        mLoops.clear();
        mPaused = false;
        mLoopPaused = null;
    }
}

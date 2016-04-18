package com.burtonshead.burningeye.misc;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import java.util.HashMap;

public class SoundEffects {
    private Context context;
    private boolean released;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;
    private float volume;

    public SoundEffects(int size, Context context) {
        this.released = false;
        this.context = context;
        this.soundPool = new SoundPool(size + 20, 3, 40);
        this.soundPoolMap = new HashMap();
        AudioManager mgr = (AudioManager) context.getSystemService("audio");
        this.volume = 0.5f;
    }

    public void addSound(int resid) {
        int soundId = this.soundPool.load(this.context, resid, 1);
        this.soundPoolMap.put(Integer.valueOf(resid), Integer.valueOf(soundId));
        this.soundPool.setLoop(soundId, 1);
    }

    public void addLoopSound(int resid) {
        int soundId = this.soundPool.load(this.context, resid, 1);
        this.soundPoolMap.put(Integer.valueOf(resid), Integer.valueOf(soundId));
        this.soundPool.setLoop(soundId, -1);
    }

    public void play(int resid) {
        //Log.i("SoundEffects", "Playing: " + resid);
        int soundId = ((Integer) this.soundPoolMap.get(Integer.valueOf(resid))).intValue();
        this.soundPool.setLoop(soundId, 1);
        this.soundPool.play(soundId, this.volume, this.volume, 1, 0, 1.0f);
    }

    public void playLoop(int resid) {
        int soundId = ((Integer) this.soundPoolMap.get(Integer.valueOf(resid))).intValue();
        this.soundPool.setLoop(soundId, -1);
        this.soundPool.play(soundId, this.volume, this.volume, 1, 0, 1.0f);
    }

    public void stop(int resid) {
        int soundId = ((Integer) this.soundPoolMap.get(Integer.valueOf(resid))).intValue();
        this.soundPool.setLoop(soundId, 0);
        this.soundPool.setVolume(soundId, 0.0f, 0.0f);
    }

    public void release() {
        if (!this.released) {
            this.released = true;
            this.soundPool.release();
        }
    }
}

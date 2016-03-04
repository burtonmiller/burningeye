package com.burtonshead.burningeye.logic;

import android.util.Log;
import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.gamespace.BlueSaucer;
import com.burtonshead.burningeye.gamespace.DarkBlueSaucer;
import com.burtonshead.burningeye.gamespace.GreenSaucer;
import com.burtonshead.burningeye.gamespace.OrangeSaucer;
import com.burtonshead.burningeye.gamespace.PurpleSaucer;
import com.burtonshead.burningeye.gamespace.RedSaucer;
import com.burtonshead.burningeye.gamespace.Saucer;
import com.burtonshead.burningeye.gamespace.WhiteSaucer;
import com.burtonshead.burningeye.gamespace.YellowSaucer;
import com.burtonshead.burningeye.powerup.Powerup;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameLevel {
    private static int MAX_GAME_WAVE;
    private static int MAX_GAME_ZONE;
    private int mGamePass;
    private int mGameWave;
    private int mGameZone;
    public long mInterval;
    public long mLastDeploy;
    public int mLimit;
    private int[] mSaucerChance;
    public int mSaucerIndex;
    public int mSaucers;
    public long mScore;
    public float mSpeedBase;
    private int mWaveCount;
    private boolean mZoneChange;

    static {
        MAX_GAME_ZONE = 7;
        MAX_GAME_WAVE = 10;
    }

    public GameLevel() {
        this.mSaucerIndex = 0;
        this.mLastDeploy = 0;
        this.mSpeedBase = 0.0f;
        this.mScore = 0;
        this.mGameZone = 1;
        this.mGameWave = 1;
        this.mGamePass = 1;
        this.mZoneChange = true;
        this.mWaveCount = 1;
        this.mLastDeploy = System.currentTimeMillis();
        if (App.getProps().getGameType() == 1) {
            MAX_GAME_ZONE = 1;
            MAX_GAME_WAVE = 10000000;
        }
    }

    public GameLevel(JSONObject gameLevel) {
        this();
        load(gameLevel);
    }

    public void load(JSONObject j) {
        try {
            this.mSaucerIndex = j.getInt("saucer_index");
            this.mSaucers = j.getInt("saucers");
            this.mInterval = j.getLong("interval");
            this.mLastDeploy = 0;
            this.mLimit = j.getInt("limit");
            JSONArray saucerChanceList = j.getJSONArray("saucer_chance");
            int length = saucerChanceList.length();
            this.mSaucerChance = new int[length];
            for (int i = 0; i < length; i++) {
                this.mSaucerChance[i] = saucerChanceList.getInt(i);
            }
            this.mGameZone = j.getInt("game_zone");
            this.mGameWave = j.getInt("game_wave");
            this.mGamePass = j.getInt("game_pass");
            this.mZoneChange = false;
            this.mWaveCount = j.getInt("wave_count");
            this.mSpeedBase = (float) j.getDouble("base_speed");
            this.mScore = j.getLong("score");
        } catch (Exception e) {
            Log.e("GameLevel.load", e.getMessage());
        }
    }

    public JSONObject store() {
        JSONObject j = new JSONObject();
        try {
            j.put("saucer_index", this.mSaucerIndex);
            j.put("saucers", this.mSaucers);
            j.put("interval", this.mInterval);
            j.put("limit", this.mLimit);
            JSONArray sList = new JSONArray();
            for (int i = 0; i < this.mSaucerChance.length; i++) {
                sList.put(i, this.mSaucerChance[i]);
            }
            j.put("saucer_chance", sList);
            j.put("game_zone", this.mGameZone);
            j.put("game_wave", this.mGameWave);
            j.put("game_pass", this.mGamePass);
            j.put("wave_count", this.mWaveCount);
            j.put("base_speed", (double) this.mSpeedBase);
            j.put("score", this.mScore);
        } catch (Exception e) {
            Log.e("GameLevel.ctr", e.getMessage());
        }
        return j;
    }

    public void advanceLevel() {
        this.mZoneChange = false;
        int maxWave = MAX_GAME_WAVE * (this.mGameZone + (MAX_GAME_ZONE * (this.mGamePass - 1)));
        this.mGameWave++;
        if (this.mGameWave > maxWave) {
            this.mZoneChange = true;
            this.mGameWave = 1;
            this.mGameZone++;
        }
        if (this.mGameZone > MAX_GAME_ZONE) {
            this.mGameZone = 1;
            this.mGamePass++;
        }
        this.mWaveCount++;
        int rand = (int) (Math.random() * 10.0d);
        if (rand < 2 && this.mSaucers < 20) {
            this.mSaucers++;
        } else if (rand == 2 && this.mLimit < 10) {
            this.mLimit++;
        } else if (rand == 3 && ((double) this.mInterval) > 0.1d) {
            this.mInterval = (long) (((double) this.mInterval) * 0.9d);
        } else if (rand != 4 || ((double) this.mSpeedBase) >= 3.0d) {
            int baseIndex = 0;
            int maxIndex = App.getProps().getGameType() == 0 ? this.mSaucerChance.length - 1 : 4;
            while (this.mSaucerChance[baseIndex] <= 3 && baseIndex < maxIndex) {
                baseIndex++;
            }
            int base = this.mSaucerChance[baseIndex];
            int change = 0;
            if (base > 10) {
                change = 5;
            } else if (base > 3) {
                change = 1;
            }
            int[] iArr = this.mSaucerChance;
            iArr[baseIndex] = iArr[baseIndex] - change;
            int range = maxIndex - baseIndex;
            for (int i = 0; i < change; i++) {
                int index = ((int) (Math.abs(((Math.random() * ((double) range)) + (Math.random() * ((double) range))) - ((double) range)) + 1.0d)) + baseIndex;
                iArr = this.mSaucerChance;
                iArr[index] = iArr[index] + 1;
            }
        } else {
            this.mSpeedBase = (float) (((double) this.mSpeedBase) + 0.2d);
        }
        this.mSaucerIndex = 0;
    }

    public boolean saucersComplete() {
        return this.mSaucerIndex >= this.mSaucers;
    }

    public boolean saucerReady() {
        if (saucersComplete()) {
            return false;
        }
        return System.currentTimeMillis() - this.mLastDeploy > this.mInterval;
    }

    public Saucer newSaucer() {
        int index = (int) (Math.random() * 100.0d);
        int total = 0;
        int saucerType = -1;
        for (int i = 0; i < this.mSaucerChance.length && saucerType == -1; i++) {
            total += this.mSaucerChance[i];
            if (index <= total) {
                saucerType = i;
            }
        }
        this.mSaucerIndex++;
        switch (saucerType) {
            case FastDateFormat.LONG /*1*/:
                return new BlueSaucer(0.0f, 0.0f);
            case FastDateFormat.MEDIUM /*2*/:
                return new RedSaucer(0.0f, 0.0f);
            case FastDateFormat.SHORT /*3*/:
                return new WhiteSaucer(0.0f, 0.0f);
            case DateUtils.RANGE_WEEK_CENTER /*4*/:
                return new PurpleSaucer(0.0f, 0.0f);
            case DateUtils.RANGE_MONTH_SUNDAY /*5*/:
                return new OrangeSaucer(0.0f, 0.0f);
            case DateUtils.RANGE_MONTH_MONDAY /*6*/:
                return new YellowSaucer(0.0f, 0.0f);
            case Powerup.POWERUP_SLOW /*7*/:
                return new DarkBlueSaucer(0.0f, 0.0f);
            default:
                return new GreenSaucer(0.0f, 0.0f);
        }
    }

    public int getGameZone() {
        return this.mGameZone;
    }

    public int getGameWave() {
        return this.mGameWave;
    }

    public int getGamePass() {
        return this.mGamePass;
    }

    public boolean getZoneChange() {
        return this.mZoneChange;
    }

    public int getWaveCount() {
        return this.mWaveCount;
    }
}

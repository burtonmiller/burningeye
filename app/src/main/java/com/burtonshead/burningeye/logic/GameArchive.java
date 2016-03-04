package com.burtonshead.burningeye.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.gamespace.GameSpace;
import com.burtonshead.burningeye.powerup.Powerup;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameArchive {
    private static final String ACTIVE_POWERUP = "active_powerup";
    private static final String EYE_POS = "eye_position";
    private static final String GAME_LEVEL_OBJ = "game_level";
    private static final String GAME_SPACE_OBJ = "game_space";
    private static final String POWERUPS = "powerups";
    private static final String PREFS = "burning_eye_prefs";
    private static final String SAVED_GAME = "saved_game";
    private JSONObject mActivePowerup;
    private JSONArray mEyePos;
    private JSONObject mJSON;
    private JSONObject mLevelJSON;
    private JSONArray mPowerups;
    private SharedPreferences mPrefs;
    private JSONObject mSpaceJSON;

    public GameArchive(Context c) {
        this.mPrefs = c.getSharedPreferences(PREFS, 0);
    }

    public boolean isLoaded() {
        return this.mJSON != null;
    }

    public void load() {
        clear();
        try {
            String savedGame = this.mPrefs.getString(SAVED_GAME, null);
            if (savedGame != null) {
                this.mJSON = new JSONObject(savedGame);
                unpackJSON();
            }
        } catch (Exception e) {
            Log.e("GameArchive.load", e.toString());
            this.mJSON = null;
        }
    }

    public void store() {
        packJSON();
        if (this.mJSON != null) {
            Editor editor = this.mPrefs.edit();
            editor.putString(SAVED_GAME, this.mJSON.toString());
            editor.commit();
        }
    }

    public void saveEyePos(Eye e) {
        try {
            this.mEyePos = new JSONArray();
            this.mEyePos.put((double) e.mBeamX);
            this.mEyePos.put((double) e.mBeamY);
        } catch (Exception e2) {
            Log.e("GameArchive.setEyePos", e2.toString());
        }
    }

    public void restoreEyePos(Eye e) {
        try {
            e.mBeamX = (float) this.mEyePos.getDouble(0);
            e.mBeamY = (float) this.mEyePos.getDouble(1);
        } catch (Exception e2) {
            Log.e("GameArchive.restoreEyePos", e2.toString());
        }
    }

    public void savePowerups(Vector<Powerup> v) {
        try {
            this.mPowerups = new JSONArray();
            for (int i = 0; i < v.size(); i++) {
                this.mPowerups.put(((Powerup) v.get(i)).store());
            }
        } catch (Exception e) {
            Log.e("GameArchive.savePowerups", e.toString());
        }
    }

    public void restorePowerups(Vector<Powerup> v) {
        try {
            v.clear();
            for (int i = 0; i < this.mPowerups.length(); i++) {
                v.add(new Powerup(this.mPowerups.getJSONObject(i)));
            }
        } catch (Exception e) {
            Log.e("GameArchive.restorePowerups", e.toString());
        }
    }

    public void saveActivePowerup(Powerup p) {
        if (p == null) {
            this.mActivePowerup = null;
            return;
        }
        try {
            this.mActivePowerup = p.store();
        } catch (Exception e) {
            Log.e("GameArchive.savePowerups", e.toString());
        }
    }

    public Powerup restoreActivePowerup() {
        try {
            if (this.mActivePowerup != null) {
                return new Powerup(this.mActivePowerup);
            }
        } catch (Exception e) {
            Log.e("GameArchive.restoreActivePowerup", e.toString());
        }
        return null;
    }

    public void saveGameLevel(GameLevel g) {
        this.mLevelJSON = g.store();
    }

    public GameLevel restoreGameLevel() {
        return new GameLevel(this.mLevelJSON);
    }

    public void saveGameSpace(GameSpace g) {
        this.mSpaceJSON = g.store();
    }

    public GameSpace restoreGameSpace(GameSpace g) {
        g.load(this.mSpaceJSON);
        return g;
    }

    public void clear() {
        this.mJSON = null;
        this.mLevelJSON = null;
        this.mSpaceJSON = null;
    }

    public void erase() {
        Editor editor = this.mPrefs.edit();
        editor.remove(SAVED_GAME);
        editor.commit();
        clear();
    }

    private void unpackJSON() {
        String str = ACTIVE_POWERUP;
        if (this.mJSON != null) {
            try {
                this.mEyePos = this.mJSON.getJSONArray(EYE_POS);
                this.mPowerups = this.mJSON.getJSONArray(POWERUPS);
                if (this.mJSON.has(ACTIVE_POWERUP)) {
                    this.mActivePowerup = this.mJSON.getJSONObject(ACTIVE_POWERUP);
                } else {
                    this.mActivePowerup = null;
                }
                this.mLevelJSON = this.mJSON.getJSONObject(GAME_LEVEL_OBJ);
                this.mSpaceJSON = this.mJSON.getJSONObject(GAME_SPACE_OBJ);
            } catch (Exception e) {
                Log.e("GameArchive.unpackJSON", e.toString());
            }
        }
    }

    private void packJSON() {
        this.mJSON = null;
        if (this.mLevelJSON != null && this.mSpaceJSON != null) {
            try {
                this.mJSON = new JSONObject();
                this.mJSON.put(EYE_POS, this.mEyePos);
                this.mJSON.put(POWERUPS, this.mPowerups);
                if (this.mActivePowerup != null) {
                    this.mJSON.put(ACTIVE_POWERUP, this.mActivePowerup);
                }
                this.mJSON.put(GAME_LEVEL_OBJ, this.mLevelJSON);
                this.mJSON.put(GAME_SPACE_OBJ, this.mSpaceJSON);
            } catch (Exception e) {
                Log.e("GameArchive.packJSON", e.toString());
            }
        }
    }
}

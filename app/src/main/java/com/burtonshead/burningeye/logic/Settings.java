package com.burtonshead.burningeye.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;


public class Settings {
    private static final String HIGH_SCORES = "high_scores";
    private static final String PREFS = "burning_eye_prefs";
    private Vector<HighScore> mHighScores;
    private SharedPreferences mPrefs;

    /* renamed from: com.burtonshead.burningeye.logic.Settings.1 */
    class scoreComparator implements Comparator<HighScore>
    {
        scoreComparator() {
        }

        public int compare(HighScore object1, HighScore object2) {
            if (object1 == null) {
                return -1;
            }
            if (object2 == null) {
                return 1;
            }
            return (int) (object2.score - object1.score);
        }
    }

    public Settings(Context c) {
        this.mHighScores = new Vector();
        this.mPrefs = c.getSharedPreferences(PREFS, 0);
    }

    public Vector<HighScore> getHighScores() {
        return (Vector) this.mHighScores.clone();
    }

    public void addHighScore(HighScore hs) {
        this.mHighScores.add(hs);
        Collections.sort(this.mHighScores, new scoreComparator());
        while (this.mHighScores.size() > 10) {
            this.mHighScores.removeElementAt(10);
        }
        store();
    }

    public void load() {
        clear();
        try {
            String scores = this.mPrefs.getString(HIGH_SCORES, null);
            if (scores != null) {
                JSONArray scoreList = new JSONArray(scores);
                for (int i = 0; i < scoreList.length(); i++) {
                    JSONObject hs = scoreList.getJSONObject(i);
                    HighScore highScore = new HighScore();
                    highScore.score = hs.getLong("score");
                    highScore.name = hs.getString("name");
                    this.mHighScores.add(highScore);
                }
            }
            while (this.mHighScores.size() < 10) {
                HighScore hs2 = new HighScore();
                hs2.score = 0;
                hs2.name = "unclaimed glory";
                this.mHighScores.add(hs2);
            }
        } catch (Exception e) {
            Log.e("Scores.load", e.toString());
        }
    }

    public void store() {
        try {
            JSONArray scoreList = new JSONArray();
            Iterator it = this.mHighScores.iterator();
            while (it.hasNext()) {
                HighScore h = (HighScore) it.next();
                JSONObject hs = new JSONObject();
                hs.put("score", h.score);
                hs.put("name", h.name);
                scoreList.put(hs);
            }
            Editor editor = this.mPrefs.edit();
            editor.putString(HIGH_SCORES, scoreList.toString());
            editor.commit();
        } catch (Exception e) {
            Log.e("Scores.store", e.toString());
        }
    }

    private void clear() {
        this.mHighScores.clear();
    }
}

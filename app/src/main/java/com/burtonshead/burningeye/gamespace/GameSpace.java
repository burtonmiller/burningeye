package com.burtonshead.burningeye.gamespace;

import android.util.Log;

import com.burtonshead.burningeye.logic.GameLogic;
import com.burtonshead.burningeye.misc.FPoint;
import com.burtonshead.burningeye.powerup.Powerup;

import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class GameSpace
{
    private static final float BOUNDARY_OFFSET = 15.0f;
    private static final String CITY_KEY = "cities";
    private static final String OBSTACLE_KEY = "obstacles";
    private static final String SAUCER_KEY = "saucers";
    public static GameSpace mInstance;
    private Vector<City> mCities;
    private Vector<City> mDeadCities;
    private Vector<Saucer> mDeadSaucers;
    private float mHeight;
    Vector<City> mHitCities;
    private Vector<Obstacle> mObstacles;
    public float mSaucerXOffset;
    public float mSaucerYOffset;
    private Vector<Saucer> mSaucers;
    private Vector<City> mTargetBuffer;
    private float mWidth;

    static
    {
        mInstance = null;
    }

    public GameSpace(float h, float w)
    {
        this.mSaucerXOffset = 0.0f;
        this.mSaucerYOffset = 0.0f;
        this.mWidth = 100.0f;
        this.mHeight = 100.0f;
        this.mSaucers = new Vector();
        this.mCities = new Vector();
        this.mObstacles = new Vector();
        this.mDeadSaucers = new Vector();
        this.mDeadCities = new Vector();
        this.mTargetBuffer = new Vector(15);
        this.mHitCities = new Vector();
        this.mHeight = h;
        this.mWidth = w;
        mInstance = this;
        GameObject.mGameSpace = this;
    }

    public void load(JSONObject contents)
    {
        this.mSaucers.clear();
        this.mCities.clear();
        this.mObstacles.clear();
        try
        {
            int i;
            JSONArray saucers = contents.getJSONArray(SAUCER_KEY);
            JSONArray cities = contents.getJSONArray(CITY_KEY);
            JSONArray obstacles = contents.getJSONArray(OBSTACLE_KEY);
            for (i = 0; i < saucers.length(); i++)
            {
                this.mSaucers.add((Saucer) GameObject.build(saucers.getJSONObject(i)));
            }
            for (i = 0; i < cities.length(); i++)
            {
                this.mCities.add((City) GameObject.build(cities.getJSONObject(i)));
            }
            for (i = 0; i < obstacles.length(); i++)
            {
                this.mObstacles.add((Obstacle) GameObject.build(obstacles.getJSONObject(i)));
            }
        } catch (Exception e)
        {
            Log.e("GameSpace.load", e.toString());
        }
    }

    public JSONObject store()
    {
        JSONObject contents = new JSONObject();
        JSONArray saucers = new JSONArray();
        JSONArray cities = new JSONArray();
        JSONArray obstacles = new JSONArray();
        Iterator it = this.mSaucers.iterator();
        while (it.hasNext())
        {
            saucers.put(((Saucer) it.next()).store());
        }
        it = this.mCities.iterator();
        while (it.hasNext())
        {
            cities.put(((City) it.next()).store());
        }
        it = this.mObstacles.iterator();
        while (it.hasNext())
        {
            obstacles.put(((Obstacle) it.next()).store());
        }
        try
        {
            contents.put(SAUCER_KEY, saucers);
            contents.put(CITY_KEY, cities);
            contents.put(OBSTACLE_KEY, obstacles);
        } catch (Exception e)
        {
            Log.e("GameSpace.store", e.toString());
        }
        return contents;
    }

    public void updateState(Vector<Powerup> newPowerups)
    {
        Iterator it = this.mSaucers.iterator();
        while (it.hasNext())
        {
            ((Saucer) it.next()).update();
        }
        it = this.mCities.iterator();
        while (it.hasNext())
        {
            ((City) it.next()).update();
        }
        this.mHitCities.clear();
        removeDead(newPowerups);
    }

    public void removeAll()
    {
        this.mSaucers.clear();
        this.mCities.clear();
        this.mObstacles.clear();
    }

    public void removeAllSaucers()
    {
        this.mSaucers.clear();
    }

    public void addSaucer(Saucer s, boolean relocate)
    {
        if (relocate)
        {
            int tries = 0;
            do
            {
                randomLocation(s.mPosition, (float) s.mRadius);
                tries++;
                if (!collideSaucer(s))
                {
                    break;
                }
            } while (tries < 25);
        }
        s.setState(10);
        this.mSaucers.add(s);
    }

    public void removeSaucer(Saucer s)
    {
        this.mSaucers.remove(s);
    }

    public Vector<Saucer> getSaucers()
    {
        return this.mSaucers;
    }

    public void restoreCities()
    {
        Iterator it = this.mCities.iterator();
        while (it.hasNext())
        {
            ((City) it.next()).restoreHP();
        }
    }

    public boolean collideBeam(Eye eye)
    {
        boolean collide = false;
        Iterator it = this.mSaucers.iterator();
        while (it.hasNext())
        {
            Saucer s = (Saucer) it.next();
            //float densityModifier = GameLogic.mMetrics.density / DisplayMetrics.DENSITY_HIGH;
            if (circleCollides(eye.mBeamX, eye.mBeamY, eye.mRadius, s.mPosition.x, s.mPosition.y, (s.mRadius * GameLogic.mMetrics.density)))
            {
                collide = true;
                s.setState(3);
                s.inflictDamage(GameLogic.mEye.mDamage);
            } else if (s.getState() == 3 && s.mHP > 0.0f)
            {
                Powerup p = GameLogic.mInstance.getActivePowerup();
                if (p == null || p.mType != 6)
                {
                    s.setState(0);
                }
            }
        }
        return collide;
    }

    public void adjustSaucerOffset(float x, float y)
    {
        this.mSaucerXOffset = x / 10.0f;
        this.mSaucerYOffset = y / 10.0f;
    }

    public boolean collideSaucer(Saucer s)
    {
        float x = s.mPosition.x;
        float y = s.mPosition.y;
        int r = s.mRadius;
        if (s.getState() == 1)
        {
            x = s.mNext.x;
            y = s.mNext.y;
        }
        Iterator it = this.mSaucers.iterator();
        while (it.hasNext())
        {
            Saucer s1 = (Saucer) it.next();
            if (s != s1 && circleCollides(
                    x, y, (float) r * GameLogic.mMetrics.density,
                    s1.mPosition.x, s1.mPosition.y, (float) s1.mRadius * GameLogic.mMetrics.density))
            {
                return true;
            }
        }
        return false;
    }

    public Vector<City> getCities()
    {
        return this.mCities;
    }

    public Vector<Obstacle> getObstacles()
    {
        return this.mObstacles;
    }

    public void addCities(Vector<FPoint> cities)
    {
        for (int i = 0; i < cities.size(); i++)
        {
            City p = new City();
            p.mPosition = (FPoint) cities.get(i);
            addCity(p, false);
        }
    }

    public void addExistingCities(Vector<City> cities)
    {
        this.mCities = cities;
    }

    public void addCity(City p, boolean relocate)
    {
        if (relocate)
        {
            FPoint fp = new FPoint();
            boolean tooClose = false;
            do
            {
                randomLocation(fp, (float) p.mRadius);
                Iterator it = this.mCities.iterator();
                while (it.hasNext())
                {
                    City p2 = (City) it.next();
                    if (circleCollides(fp.x, fp.y, BOUNDARY_OFFSET, p2.mPosition.x, p2.mPosition.y, 0.0f))
                    {
                        tooClose = true;
                        break;
                        //continue;
                    }
                }
            } while (tooClose);
            p.mPosition = fp;
        }
        this.mCities.add(p);
    }

    public void removeCity(City p)
    {
        this.mCities.remove(p);
    }

    public void removeAllCities()
    {
        this.mCities.clear();
    }

    public void addObstacle(Obstacle o, boolean relocate)
    {
    }

    public void clearCities()
    {
    }

    public void clearObstacles()
    {
        this.mObstacles.clear();
        this.mObstacles.add(new Obstacle(45, null));
    }

    public boolean areSaucersAttacking()
    {
        Iterator it = this.mSaucers.iterator();
        while (it.hasNext())
        {
            if (((Saucer) it.next()).getState() == 2)
            {
                return true;
            }
        }
        return false;
    }

    void addDeadSaucer(Saucer s)
    {
        this.mDeadSaucers.add(s);
    }

    void addDeadCity(City c)
    {
        this.mDeadCities.add(c);
    }

    void addHitCity(City c)
    {
        this.mHitCities.add(c);
    }

    void removeDead(Vector<Powerup> rewards)
    {
        Iterator it = this.mDeadSaucers.iterator();
        while (it.hasNext())
        {
            Saucer s = (Saucer) it.next();
            removeSaucer(s);
            if (s.hasReward())
            {
                rewards.add(s.getReward());
            }
        }
        this.mDeadSaucers.clear();
        it = this.mDeadCities.iterator();
        while (it.hasNext())
        {
            removeCity((City) it.next());
        }
        this.mDeadCities.clear();
    }

    Vector<City> findTarget(Saucer s)
    {
        Vector<City> targets = null;
        City p = null;
        if (this.mCities.size() == 0)
        {
            return null;
        }
        this.mTargetBuffer.clear();
        this.mTargetBuffer.addAll(this.mCities);
        do
        {
            City p2 = (City) this.mTargetBuffer.remove((int) (Math.random() * ((double) this.mTargetBuffer.size())));
            if (distance(s.mPosition.x, s.mPosition.y, (float) s.mRadius, p2.mPosition.x, p2.mPosition.y, (float) p2.mRadius) <= s.mRange)
            {
                p = p2;
            }
            if (p != null)
            {
                break;
            }
        } while (!this.mTargetBuffer.isEmpty());
        if (p != null)
        {
            targets = new Vector();
            targets.add(p);
        }
        return targets;
    }

    Vector<City> findAllTargets(Saucer s)
    {
        Vector<City> targets = new Vector();
        if (this.mCities.size() == 0)
        {
            return null;
        }
        Iterator it = this.mCities.iterator();
        while (it.hasNext())
        {
            City c = (City) it.next();
            if (distance(s.mPosition.x, s.mPosition.y, (float) s.mRadius, c.mPosition.x, c.mPosition.y, (float) c.mRadius) <= s.mRange)
            {
                targets.add(c);
            }
        }
        return targets;
    }

    void randomLocation(FPoint p, float radius)
    {
        p.x = (float) ((Math.random() * ((double) (this.mWidth - ((radius) * Eye.X_TOLERANCE)))) + ((double) radius));
        p.y = (float) ((Math.random() * ((double) (this.mHeight - ((radius * 3) * Eye.Y_TOLERANCE)))) + ((double) radius)); //??? radius * 3
    }

    static float distance(float x1, float y1, float r1, float x2, float y2, float r2)
    {
        return (float) Math.sqrt(Math.pow((double) (x1 - x2), 2.0d) + Math.pow((double) (y1 - y2), 2.0d));
    }

    static boolean circleCollides(float x1, float y1, float r1, float x2, float y2, float r2)
    {
        return ((float) Math.sqrt(Math.pow((double) (x1 - x2), 2.0d) + Math.pow((double) (y1 - y2), 2.0d))) <= r1 + r2;
    }
}

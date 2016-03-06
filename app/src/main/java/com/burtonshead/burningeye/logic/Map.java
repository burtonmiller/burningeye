package com.burtonshead.burningeye.logic;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.misc.FPoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

public class Map
{
    public static final int MAP_AFRICA = 3;
    public static final int MAP_ANTARCTICA = 7;
    public static final int MAP_ASIA = 5;
    public static final int MAP_AUSTRALIA = 2;
    public static final int MAP_EUROPE = 4;
    public static final int MAP_NORTH_AMERICA = 1;
    public static final int MAP_SOUTH_AMERICA = 6;
    public static final int MAX_MAP_ID = 7;
    private Vector<FPoint> mCities;
    public int mMapID;
    private DisplayMetrics mMetrics;

    public Map(int mapID, Vector<FPoint> cities)
    {
        this.mCities = null;
        this.mMapID = mapID;
        this.mCities = cities;
    }

    public Map(int zone)
    {
        this.mCities = null;
        JSONObject map = null;
        Activity a = GameLogic.mInstance.getActivity();
        try
        {
            map = new JSONObject(a.getResources().getString(a.getResources().getIdentifier("map_" + zone, "string", "com.burtonshead.burningeye")));
        }
        catch (Exception e)
        {
            Exception x = e;
            a.finish();
        }
        try
        {
            this.mMapID = map.getInt("map_id");
            JSONArray cityList = map.getJSONArray("cities");
            int length = cityList.length();
            this.mCities = new Vector();
            for (int i = 0; i < length; i += MAP_NORTH_AMERICA)
            {
                JSONArray jPoint = cityList.getJSONArray(i);
                FPoint p = new FPoint();
                p.x = (float) ((jPoint.getDouble(0) * ((double) GameLogic.mMetrics.widthPixels)) / 480.0d);
                p.y = (float) ((jPoint.getDouble(MAP_NORTH_AMERICA) * ((double) GameLogic.mMetrics.heightPixels)) / 320.0d);
                this.mCities.add(p);
            }
        } catch (Exception e2)
        {
            Log.e("Map.ctr", e2.getMessage());
        }
    }

    public JSONObject store()
    {
        JSONObject j = new JSONObject();
        try
        {
            j.put("map_id", this.mMapID);
            JSONArray cList = new JSONArray();
            for (int i = 0; i < this.mCities.size(); i += MAP_NORTH_AMERICA)
            {
                FPoint p = (FPoint) this.mCities.get(i);
                JSONArray jPoint = new JSONArray();
                jPoint.put(0, (double) p.x);
                jPoint.put(MAP_NORTH_AMERICA, (double) p.y);
                cList.put(i, jPoint);
            }
            j.put("cities", cList);
        } catch (Exception e)
        {
            Log.e("Map.store", e.getMessage());
        }
        return j;
    }

    public int getMapResID()
    {
        switch (this.mMapID)
        {
            case MAP_AUSTRALIA /*2*/:
                return R.drawable.map_2;
            case MAP_AFRICA /*3*/:
                return R.drawable.map_3;
            case MAP_EUROPE /*4*/:
                return R.drawable.map_4;
            case MAP_ASIA /*5*/:
                return R.drawable.map_5;
            case MAP_SOUTH_AMERICA /*6*/:
                return R.drawable.map_6;
            case MAP_ANTARCTICA /*7*/:
                return R.drawable.map_7;
            default:
                return R.drawable.map_1;
        }
    }

    public Vector<FPoint> getCityLocs()
    {
        return this.mCities;
    }
}

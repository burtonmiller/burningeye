package com.burtonshead.burningeye.logic;

/***
 * Author: Burton Miller
 *
 * Copyright 2016
 */

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.ScoreScreen;
import com.burtonshead.burningeye.gamespace.City;
import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.gamespace.GameObject;
import com.burtonshead.burningeye.gamespace.GameSpace;
import com.burtonshead.burningeye.gamespace.Saucer;
import com.burtonshead.burningeye.powerup.Powerup;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

public class GameLogic implements SensorEventListener
{
    public static final int STATE_LEVEL_COMPLETE = 4;
    public static final int STATE_NEW = 0;
    public static final int STATE_OVER = 3;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_RESUME = 1;
    public static final int STATE_UNINIT = -1;

    public static Eye mEye;
    public static GameLogic mInstance;
    public static DisplayMetrics mMetrics;

    public static float mStepMult;
    public static float mTimeDiff;

    public float mOrientX;
    public float mOrientY;
    public float mOrientZ;

    public Activity mContext;
    public Map mMap;
    public Powerup mActivePowerup;


    public GameLogic(Activity c, GameSurface g)
    {
        mInstance = this;

        setContext(c);

        mActivePowerup = null;
        mMainThread = null;

        mGameState = STATE_UNINIT;
        mGameStateChanged = false;
        mGameListeners = new Vector<>();

        mOrientX = 0.0f;
        mOrientY = 0.0f;
        mOrientZ = 0.0f;

        mAccValues = new float[3];
        mMagValues = new float[3];
        mOrientationRotationMatrix = new float[9];
        mOrientationTransformMatrix = new float[9];
        mOrientationResult = new float[3];

        mCurrentTime = 0;
        mLastTime = 0;
        mLastScore = 0;
        mCollide = false;
        mCollideChanged = false;
        mSaucerAttack = false;
        mSaucerAttackChanged = false;
        mPowerups = new Vector<>();
        mNewPowerups = new Vector<>();

        mArchive = new GameArchive(c);
        mArchive.load();

        mTimeDiff = 0.0f;
        mStepMult = 1.0f;
        mSpeedMult = 1.0f;

        initSound();

        Display display = mContext.getWindowManager().getDefaultDisplay();

        mRotation = display.getRotation();

        mMetrics = new DisplayMetrics();
        display.getMetrics(mMetrics);
        int h = mMetrics.heightPixels;
        int w = mMetrics.widthPixels;

        mGameSurface = g;
        mGameSpace = new GameSpace((float) h, (float) w);
        try
        {
            mSensorMgr = (SensorManager) mContext.getSystemService("sensor");
            Sensor accSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor magSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorMgr.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorMgr.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        catch (Exception e)
        {
            Exception x = e;
            noSensorError();
        }

        setMainThread();

        initGameObjects();
    }

    public Activity getActivity()
    {
        return mContext;
    }

    public boolean hasSavedGame()
    {
        return mArchive.isLoaded();
    }

    public void saveGame()
    {
        mArchive.clear();
        mArchive.saveEyePos(mEye);
        mArchive.saveActivePowerup(mActivePowerup);
        mArchive.savePowerups(mPowerups);
        mArchive.saveGameLevel(mGameLevel);
        mArchive.saveGameSpace(mGameSpace);
        mArchive.store();
    }

    public void clearSaved()
    {
        mArchive.erase();
    }

    public boolean restoreGame()
    {
        if (!mArchive.isLoaded())
        {
            return false;
        }

        mGameState = STATE_PAUSE;

        mArchive.restoreEyePos(mEye);
        mArchive.restorePowerups(mPowerups);
        mArchive.restoreGameSpace(mGameSpace);

        loadLevel(mArchive.restoreGameLevel(), false);

        Powerup p = mArchive.restoreActivePowerup();

        if (p != null)
        {
            activatePowerup(p);
        }
        return true;
    }

    public void addScore(long score)
    {
        GameLevel gameLevel = mGameLevel;
        gameLevel.mScore += score;
    }

    public long getScore()
    {
        if (mGameLevel == null)
        {
            return 0;
        }
        return mGameLevel.mScore;
    }

    public void setScore(long score)
    {
        mGameLevel.mScore = score;
    }

    public void resumeSounds()
    {
        mSoundMgr.resumeLoops();
    }

    public void pauseSounds()
    {
        mSoundMgr.pauseLoops();
    }

    public void cleanup()
    {
        mSoundMgr.cleanup();
    }

    public void setContext(Activity c)
    {
        mContext = c;
    }

    public int getWaveCount()
    {
        return mGameLevel.getWaveCount();
    }

    public void addGameListener(GameListener g)
    {
        if (!mGameListeners.contains(g))
        {
            mGameListeners.add(g);
        }
    }

    public void removeGameListener(GameListener g)
    {
        if (mGameListeners.contains(g))
        {
            mGameListeners.remove(g);
        }
    }

    public synchronized void setGameState(int newState)
    {

        int oldState = mGameState;
        boolean noChange = (newState == mGameState);
        mGameStateChanged = noChange;
        mGameState = newState;
        switch (mGameState)
        {
            default:
            {
                processScoreSafe();
                clearSaved();
            }
            case STATE_UNINIT:
            case STATE_PAUSE:
            {
                pauseMainLoopSafe();
                mSoundMgr.pauseLoops();
                System.gc();
                break;
            }
            case STATE_LEVEL_COMPLETE:
            {
                pauseMainLoopSafe();
                mSoundMgr.pauseLoops();
                System.gc();
                break;
            }
            case STATE_NEW:
            {
                resetGame();
                break;
            }
            case STATE_RESUME:
            {
                if (oldState == 4)
                {
                    loadNextLevel();
                }
                resumeMainLoop();
                mSoundMgr.resumeLoops();
            }
        }

        informStateChangeSafe();

        return;
    }


    public synchronized int getGameState()
    {
        return mGameState;
    }

    public Eye getEye()
    {
        return mEye;
    }

    public synchronized Vector<Powerup> getPowerups()
    {
        return mPowerups;
    }

    /**
     * Add a new power
     * @param p Powerup
     */
    public synchronized void addPowerup(Powerup p)
    {
        mPowerups.insertElementAt(p, STATE_NEW);
        while (mPowerups.size() > STATE_LEVEL_COMPLETE)
        {
            mPowerups.remove(STATE_LEVEL_COMPLETE);
        }
        informPowerupsChangedSafe();
    }

    /**
     * Add newly available Powerups, and make sure list is not too long
     */
    public synchronized void addNewPowerups()
    {
        int size = mNewPowerups.size();
        for (int i = STATE_NEW; i < size; i += STATE_RESUME)
        {
            mPowerups.insertElementAt((Powerup) mNewPowerups.get(i), STATE_NEW);
        }
        mNewPowerups.clear();
        while (mPowerups.size() > STATE_LEVEL_COMPLETE)
        {
            mPowerups.remove(STATE_LEVEL_COMPLETE);
        }
        if (size > 0)
        {
            informPowerupsChangedSafe();
        }
    }

    /**
     * Apply current powerup to game state
     * @param powerup
     */
    public synchronized void activatePowerup(Powerup powerup)
    {
        if (mActivePowerup != null)
        {
            deactivatePowerup();
        }

        mPowerups.remove((Object) powerup);
        mActivePowerup = powerup;

        switch (powerup.mType)
        {
            default:
            {
                mEye.setBeamType(Eye.BEAM_NORMAL);
                break;
            }
            case Powerup.POWERUP_DMG_SHOCK:
            {
                mEye.setBeamType(Eye.BEAM_SHOCK);
                break;
            }
            case Powerup.POWERUP_DMG_FIRE:
            {
                mEye.setBeamType(Eye.BEAM_FIRE);
                break;
            }
            case Powerup.POWERUP_DMG_BLACKHOLE:
            {
                mEye.setBeamType(Eye.BEAM_BLACKHOLE);
                break;
            }
            case Powerup.POWERUP_WIDE:
            {
                mEye.setBeamType(Eye.BEAM_WIDE);
                break;
            }
            case Powerup.POWERUP_XWIDE:
            {
                mEye.setBeamType(Eye.BEAM_XWIDE);
                break;
            }
            case Powerup.POWERUP_BOMB:
            {
                mEye.setBeamType(Eye.BEAM_NORMAL);
                break;
            }
            case Powerup.POWERUP_SLOW:
            {
                mEye.setBeamType(Eye.BEAM_NORMAL);
                mSpeedMult = 0.25f;
            }
        }
        informPowerupsChangedSafe();

        return;
    }

    /**
     * Activate the selected Powerup
     * @param index
     */
    public synchronized void activatePowerup(int index)
    {
        activatePowerup((Powerup) mPowerups.get(index));
    }

    /**
     * Deactivate the current Powerup and restore the Beam and Speed states
     */
    public synchronized void deactivatePowerup()
    {
        if (mActivePowerup != null)
        {
            switch (mActivePowerup.mType)
            {
                case Powerup.POWERUP_DMG_SHOCK:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                case Powerup.POWERUP_DMG_FIRE:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                    break;
                case Powerup.POWERUP_DMG_BLACKHOLE:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                    break;
                case Powerup.POWERUP_WIDE:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                    break;
                case Powerup.POWERUP_XWIDE:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                    break;
                case Powerup.POWERUP_BOMB:
                    break;
                case Powerup.POWERUP_SLOW:
                    mSpeedMult = 1.0f;
                    break;
                default:
                    mEye.setBeamType(Eye.BEAM_NORMAL);
                    break;
            }
        }
        mActivePowerup = null;
        informPowerupsChangedSafe();
    }

    public Powerup getActivePowerup()
    {
        return mActivePowerup;
    }

    /**
     * Get current Speed Multiplier - which modifies how fast all the
     * saucers will fly
     * @return
     */
    public float getSpeedMult()
    {
        float mult = 0.8f;
        if (mGameLevel != null)
        {
            mult = mGameLevel.mSpeedBase;
        }
        return mult * mSpeedMult * mMetrics.density;
    }

    public void setMainThread()
    {
        mMainThread = Thread.currentThread();
        mHandler = new Handler();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
    }

    /**
     * Captures Accelerometer and Magnetometer to get values
     * needed to determine device tilt
     * @param event
     */
    public void onSensorChanged(SensorEvent event)
    {
        final String comma = ", ";
        if (event.accuracy != 0)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                System.arraycopy(event.values, 0, mAccValues, 0, event.values.length);
                //Log.i("onSensorChanged.Accelerometer", event.values[0] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                System.arraycopy(event.values, 0, mMagValues, 0, event.values.length);
                //Log.i("onSensorChanged.MagneticField", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

            if (mAccValues != null && mMagValues != null)
            {
                //Log.i("onSensorChanged", "mAccValues = " + mAccValues.toString() + ", mMagValues = " + mMagValues.toString());
                calculateOrientation();
            }
            else
            {
                if (mAccValues == null)
                {
                    //Log.i("GameLogic.onSensorChanged", "\n\n\n ACCELEROMETER NULL \n\n\n");
                }
                if (mMagValues == null)
                {
                    //Log.i("GameLogic.onSensorChanged", "\n\n\n MAGNEMOMETER NULL \n\n\n");
                }
            }
            //Log.i("onSensorChanged", "*** mOrientX = " + mOrientX + ", mOrientY = " + mOrientY + ", mOrientZ = " + mOrientZ + "***");
        }
    }


    //*** NON-PUBLIC ***

    private static final float TICK = 33.0f;
    private static float mSpeedMult;

    private final int mRotation;   // tablets and phones have different rotation settings - which reverses X/Y coordinates
    private float[] mAccValues;    // Acclerometer values for Orientation calculation
    private GameArchive mArchive;  // Storage archive for game state
    private boolean mCollide;      // Collision in progress?
    private boolean mCollideChanged;   // Collision state changing?
    private long mCurrentTime;     // Current time in milliseconds
    private GameLevel mGameLevel;  // Current loaded game level
    private Vector<GameListener> mGameListeners;   // Listeners to game state
    private GameSpace mGameSpace;  // Game space - where objects interact
    private int mGameState;        // Game state (paused, running, etc)
    private boolean mGameStateChanged;   // Did the game state just change?
    private GameSurface mGameSurface;   // Drawing surface for the game
    private Handler mHandler;      // Handler used to process Runnables on the UI thread
    private long mLastScore;       // Most recent score
    private long mLastTime;        // Time before the Current Time (need last two values)
    private float[] mMagValues;    // Magnetometer values for current Orientation calculation
    private MainLoop mMainLoop;    // Main game loop object - runs in its own thread
    private Thread mMainThread;    // Main thread within the game loop
    private Vector<Powerup> mNewPowerups;  // List of newly acquired Powerups
    private float[] mOrientationResult;    // Intermediate values used to determine Orientation - stored once for memory efficiency
    private float[] mOrientationRotationMatrix;    // Intermediate values used to determine Orientation - stored once for memory efficiency
    private float[] mOrientationTransformMatrix;   // Intermediate values used to determine Orientation - stored once for memory efficiency
    private Vector<Powerup> mPowerups;     //List of currently available acquired Powerups
    private boolean mSaucerAttack;         // Is any saucer in a state of attack (to adjust sound)
    private boolean mSaucerAttackChanged;  // Has the state of attack of any saucer just changed
    private SensorManager mSensorMgr;      // Used to calculate Orientation for tilt
    private SoundMgr mSoundMgr;            // Plays sound effects


    private class MainLoop extends Thread
    {
        private boolean mDone;

        public MainLoop()
        {
            mDone = false;
        }

        public void run()
        {
            mDone = false;
            mLastTime = System.currentTimeMillis();
            while (!mDone)
            {
                mCurrentTime = System.currentTimeMillis();
                mTimeDiff = (float) (mCurrentTime - mLastTime);

                if (mTimeDiff >= TICK)
                {
                    mStepMult = mTimeDiff / TICK;
                    //Log.i("MainLoop.run", "*** mStepMult = " + mStepMult + " ***");
                    updateGame();
                    mLastTime = mCurrentTime;
                }
            }
        }

        public void requestExitAndWait()
        {
            mDone = true;
            try
            {
                join(1000);
            }
            catch (Exception x)
            {
                Log.e("GameSurface.DrawingThread.requestExitAndWait", x.getMessage());
            }
        }
    }


    private void initGameObjects()
    {
        mEye = new Eye(mContext);
    }

    private void initSound()
    {
        mSoundMgr = new SoundMgr(mContext);
        mSoundMgr.loadLoop(R.raw.beam_collide_sound, 0.5f);
        mSoundMgr.loadLoop(R.raw.eye_sound, 0.4f);
        mSoundMgr.loadLoop(R.raw.saucer_beam, 0.7f);
        mSoundMgr.loadSound(R.raw.appear_sound, 1.0f);
        mSoundMgr.loadSound(R.raw.explosion_sound, 0.5f);
        mSoundMgr.loadSound(R.raw.power_up_sound, 1.0f);
        mSoundMgr.loadSound(R.raw.scream_sound, 1.0f);
    }

    private void noSensorError()
    {
        new Builder(mContext)
                .setTitle("No Orientation Sensor")
                .setMessage("Your device does not seem to have an orientation sensor.  This game will not function without this technology.")
                .setNegativeButton("Quit", new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mContext.finish();
                    }
                })
                .create()
                .show();
    }

    private void resetGame()
    {
        mGameSpace.removeAll();
        loadNewLevel();
        mSoundMgr.startLoop(R.raw.eye_sound);
    }

    /**
     * Determines the X/Y tilt of the device from a flat position
     */
    private void calculateOrientation()
    {
        try
        {
            SensorManager.getOrientation(mOrientationRotationMatrix, mOrientationResult);
            SensorManager.getRotationMatrix(mOrientationRotationMatrix, null, mAccValues, mMagValues);
            SensorManager.remapCoordinateSystem(mOrientationRotationMatrix, 0, 180, mOrientationTransformMatrix);
            mOrientY = (float) Math.toDegrees((double) mOrientationResult[2]);
            mOrientZ = (float) (-Math.toDegrees((double) mOrientationResult[0]));
            mOrientX = (float) -Math.toDegrees((double) mOrientationResult[1]);

            if (mRotation == Surface.ROTATION_0)
            {
                float x = mOrientY;
                mOrientY = -mOrientX;
                mOrientX = x;
            }

        }
        catch (Exception x)
        {
            Log.e("calcOrientation", "\n\n Problem calculating orientation \n\n", x);
        }
        //Log.i("calculateOrientation", "*** mOrientX = " + mOrientX + ", mOrientY = " + mOrientY + ", mOrientZ = " + mOrientZ + "***");
    }

    /**
      * Informs UI listeners of changes to games state
      * Must be called in UI thread - see informStateChangeSafe()
      */
    private synchronized void informStateChange()
    {
        Iterator it = mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onStateChange();
        }
    }

    /**
     *  Informs UI listeners of changes to game state in the UI thread
     */
    private synchronized void informStateChangeSafe()
    {
        if (Thread.currentThread().equals(mMainThread))
        {
            informStateChange();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    informStateChange();
                }
            });
        }
    }

    /**
      * Informs objects in UI thread of changes to powerup state
      * Must be called in UI thread - see informPowerupsChangesSafe()
      */
    private synchronized void informPowerupsChanged()
    {
        Iterator it = mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onPowerupChange();
        }
        mEye.mReload = true;
    }

    /**
     * Informs objects in UI thread of changes to powerup state in the UI thread
      */
    private synchronized void informPowerupsChangedSafe()
    {
        if (Thread.currentThread().equals(mMainThread))
        {
            informPowerupsChanged();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    informPowerupsChanged();
                }
            });
        }
    }

    /**
     * Informs objects in UI thread of changes to score state
     * Must be called in UI thread - see informScoreChangeSafe()
     */
    private synchronized void informScoreChange()
    {
        Iterator it = mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onScoreChange();
        }
    }

    /**
     * Informs objects in UI thread of changes to score state in UI Thread
      */
    private synchronized void informScoreChangeSafe()
    {
        if (Thread.currentThread().equals(mMainThread))
        {
            informScoreChange();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    informScoreChange();
                }
            });
        }
    }

    // All the update methods are used to manage the game state step-by-step

    private void updateGame()
    {
        updateState();
        updateScore();
        updatePowerups();
        updateInput();
        updateAI();
        updateGraphics();
        updateSound();
        mGameStateChanged = false;
    }

    private synchronized void updateState()
    {
        if (!(mGameState == STATE_OVER || mGameLevel == null))
        {
            if (mGameLevel.saucersComplete() && mGameSpace.getSaucers().size() == 0)
            {
                setGameState(STATE_LEVEL_COMPLETE);
            }
            else if (mGameSpace.getCities().size() == 0)
            {
                setGameState(STATE_OVER);
            }
        }
    }

    private void updateInput()
    {
        mEye.moveBeam(mOrientX, mOrientY, mStepMult);
        mGameSpace.adjustSaucerOffset(mOrientX, mOrientY);
        boolean collide = mGameSpace.collideBeam(mEye);
        mCollideChanged = mCollide ^ collide;
        mCollide = collide;
    }

    private synchronized void updatePowerups()
    {
        if (mActivePowerup != null)
        {
            Powerup powerup = mActivePowerup;
            //Log.i("GameLogic.updatePowerups()", "*** mTimeLeft = " + powerup.mTimeLeft + "***");
            powerup.mTimeLeft = (int) (((float) powerup.mTimeLeft) - mTimeDiff);
            if (mActivePowerup.mTimeLeft <= 0)
            {
                deactivatePowerup();
            }
            else if (mActivePowerup.mType == 6)
            {
                Iterator it = mGameSpace.getSaucers().iterator();
                while (it.hasNext())
                {
                    Saucer s = (Saucer) it.next();
                    s.setState(STATE_OVER);
                    s.inflictDamage(1.2f);
                }
            }
        }
    }

    private void updateAI()
    {
        Vector<Saucer> saucerList = mGameSpace.getSaucers();
        Vector<City> cityList = mGameSpace.getCities();
        if (mGameLevel.mLimit > saucerList.size() && mGameLevel.saucerReady())
        {
            Saucer s = mGameLevel.newSaucer();
            if (s != null)
            {
                mGameSpace.addSaucer(s, true);
            }
        }
        mGameSpace.updateState(mNewPowerups);
        addNewPowerups();
        boolean attack = mGameSpace.areSaucersAttacking();
        mSaucerAttackChanged = mSaucerAttack ^ attack;
        mSaucerAttack = attack;
    }

    /**
     * Draws the current game state
     */
    private void updateGraphics()
    {
        mGameSurface.updateGraphics();
    }

    /**
     * Plays the proper sound depending on state
     */
    private void updateSound()
    {
        if (mCollideChanged || mSaucerAttackChanged)
        {
            if (mCollide)
            {
                mSoundMgr.startLoop(R.raw.beam_collide_sound);
            }
            else if (mSaucerAttack)
            {
                mSoundMgr.startLoop(R.raw.saucer_beam);
            }
            else
            {
                mSoundMgr.startLoop(R.raw.eye_sound);
            }
        }
        int id = GameObject.getNextSound();
        if (id != STATE_UNINIT)
        {
            mSoundMgr.playSound(id);
        }
    }

    /**
     * Tells the UI thread the current score
     */
    private void updateScore()
    {
        if (mGameLevel != null && mLastScore != mGameLevel.mScore)
        {
            mLastScore = mGameLevel.mScore;
            informScoreChangeSafe();
        }
    }

    /**
     * Starts the main game loop, creating if not present
     */
    private void resumeMainLoop()
    {
        if (mMainLoop == null)
        {
            mMainLoop = new MainLoop();
            mMainLoop.start();
        }
    }

    /**
     * Pauses the main game loop
     */
    private void pauseMainLoop()
    {
        if (mMainLoop != null)
        {
            mMainLoop.requestExitAndWait();
            mMainLoop = null;
        }
    }

    /**
     * Pause is often called from the UI thread, because that is where the user commands
     * come from
     */
    private void pauseMainLoopSafe()
    {
        if (Thread.currentThread().equals(mMainThread))
        {
            pauseMainLoop();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    pauseMainLoop();
                }
            });
        }
    }

    /**
     * Load a new Game level
     *
     * @param level
     * @param newLevel  Need to know if this is new
     */
    private void loadLevel(GameLevel level, boolean newLevel)
    {
        mGameLevel = level;
        loadMap(newLevel);
    }

    /**
     * Load a new level base level - higher levels are generated and stored
     */
    private void loadNewLevel()
    {
        try
        {
//            loadLevel(new GameLevel(new JSONObject(mContext.getResources()
//                    .getString(mContext.getResources().getIdentifier("base_level", "string", "com.burtonshead.burningeye")))), true);
            loadLevel(new GameLevel(new JSONObject(mContext.getResources()
                    .getString(R.string.base_level))), true);
        }
        catch (Exception e)
        {
            Exception x = e;
            mContext.finish();
        }
        mEye.recenter();
    }

    /**
     * Makes necessary updates and loads the next level
     */
    private void loadNextLevel()
    {
        mGameSpace.restoreCities();
        mEye.recenter();
        mGameLevel.advanceLevel();
        if (mGameLevel.getZoneChange())
        {
            mGameSpace.removeAll();
            loadMap(true);
            return;
        }
        mGameSpace.removeAllSaucers();
    }

    /**
     * Loads the map for the current level
     * @param newCities
     */
    private void loadMap(boolean newCities)
    {
        mMap = new Map(mGameLevel.getGameZone());
        mGameSurface.setMapImg(mMap.getMapResID());
        if (newCities)
        {
            mGameSpace.removeAllCities();
            mGameSpace.addCities(mMap.getCityLocs());
        }
    }

    /**
     * Sends user to the high score screen.
     * Needs to happen in UI thread.
     *
     * @param score
     */
    private void processScore(long score)
    {
        Vector<HighScore> highScores = App.getSettings().getHighScores();
        if (highScores.size() == 0 || score >= ((HighScore) highScores.lastElement()).score)
        {
            Intent i = new Intent(mContext, ScoreScreen.class);
            i.putExtra(ScoreScreen.NEW_HIGH_SCORE_EXTRA, score);
            mContext.startActivity(i);
        }
    }

    /**
     * Sends user to the high score screen from the UI thread
     */
    private void processScoreSafe()
    {
        final long score = mGameLevel != null ? mGameLevel.mScore : 0;
        if (Thread.currentThread().equals(mMainThread))
        {
            processScore(score);
        }
        else
        {
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    processScore(score);
                }
            });
        }
    }

}

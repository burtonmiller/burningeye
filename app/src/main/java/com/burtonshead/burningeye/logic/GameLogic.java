package com.burtonshead.burningeye.logic;

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

    private static final float TICK = 33.0f;

    public static Eye mEye;
    public static GameLogic mInstance;
    public static DisplayMetrics mMetrics;
    private static float mSpeedMult;
    public static float mStepMult;
    public static float mTimeDiff;
    private final int mRotation;
    private float[] mAccValues;
    public Powerup mActivePowerup;
    private GameArchive mArchive;
    private boolean mCollide;
    private boolean mCollideChanged;
    public Activity mContext;
    private long mCurrentTime;
    private GameLevel mGameLevel;
    private Vector<GameListener> mGameListeners;
    private GameSpace mGameSpace;
    private int mGameState;
    private boolean mGameStateChanged;
    private GameSurface mGameSurface;
    private Handler mHandler;
    private long mLastScore;
    private long mLastTime;
    private float[] mMagValues;
    private MainLoop mMainLoop;
    private Thread mMainThread;
    public Map mMap;
    private Vector<Powerup> mNewPowerups;
    public float mOrientX;
    public float mOrientY;
    public float mOrientZ;
    private float[] mOrientationResult;
    private float[] mOrientationRotationMatrix;
    private float[] mOrientationTransformMatrix;
    private Vector<Powerup> mPowerups;
    private boolean mSaucerAttack;
    private boolean mSaucerAttackChanged;
    private SensorManager mSensorMgr;
    private SoundMgr mSoundMgr;


    class StateChangePoster implements Runnable
    {
        public void run()
        {
            GameLogic.this.informStateChange();
        }
    }

    class PowerupChangePoster implements Runnable
    {
        public void run()
        {
            GameLogic.this.informPowerupsChanged();
        }
    }

    class ScoreInformPoster implements Runnable
    {
        public void run()
        {
            GameLogic.this.informScoreChange();
        }
    }

    class PauseGamePoster implements Runnable
    {
        public void run()
        {
            GameLogic.this.pauseMainLoop();
        }
    }

    class ProcessScorePoster implements Runnable
    {
        private final long mScore;

        ProcessScorePoster(long j)
        {
            mScore = j;
        }

        public void run()
        {
            GameLogic.this.processScore(mScore);
        }
    }

    private class MainLoop extends Thread
    {
        private boolean mDone;

        public MainLoop()
        {
            this.mDone = false;
        }

        public void run()
        {
            this.mDone = false;
            GameLogic.this.mLastTime = System.currentTimeMillis();
            while (!this.mDone)
            {
                GameLogic.this.mCurrentTime = System.currentTimeMillis();
                GameLogic.mTimeDiff = (float) (GameLogic.this.mCurrentTime - GameLogic.this.mLastTime);

                //???
                if (GameLogic.mTimeDiff >= GameLogic.TICK)
                {
                    GameLogic.mStepMult = GameLogic.mTimeDiff / GameLogic.TICK;
                    //Log.i("MainLoop.run", "*** mStepMult = " + GameLogic.mStepMult + " ***");
                    GameLogic.this.updateGame();
                    GameLogic.this.mLastTime = GameLogic.this.mCurrentTime;
                }
            }
        }

        public void requestExitAndWait()
        {
            this.mDone = true;
            try
            {
                join(1000);
            } catch (Exception x)
            {
                Log.e("GameSurface.DrawingThread.requestExitAndWait", x.getMessage());
            }
        }
    }

    public GameLogic(Activity c, GameSurface g)
    {
        mInstance = this;

        setContext(c);

        this.mActivePowerup = null;
        this.mMainThread = null;
        this.mOrientX = 0.0f;
        this.mOrientY = 0.0f;
        this.mOrientZ = 0.0f;
        this.mAccValues = new float[STATE_OVER];
        this.mMagValues = new float[STATE_OVER];
        this.mOrientationRotationMatrix = new float[9];
        this.mOrientationTransformMatrix = new float[9];
        this.mOrientationResult = new float[STATE_OVER];
        this.mGameState = STATE_UNINIT;
        this.mGameStateChanged = false;
        this.mGameListeners = new Vector();
        this.mCurrentTime = 0;
        this.mLastTime = 0;
        this.mLastScore = 0;
        this.mCollide = false;
        this.mCollideChanged = false;
        this.mSaucerAttack = false;
        this.mSaucerAttackChanged = false;
        this.mPowerups = new Vector();
        this.mNewPowerups = new Vector();
        this.mArchive = new GameArchive(c);
        this.mArchive.load();
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

        this.mGameSurface = g;
        this.mGameSpace = new GameSpace((float) h, (float) w);
        // pause 2, resume 1
        try
        {
            this.mSensorMgr = (SensorManager) this.mContext.getSystemService("sensor");
            Sensor accSensor = this.mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor magSensor = this.mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            //Sensor rotationSensor = this.mSensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            this.mSensorMgr.registerListener(this, accSensor, Sensor.REPORTING_MODE_CONTINUOUS);
            this.mSensorMgr.registerListener(this, magSensor, Sensor.REPORTING_MODE_CONTINUOUS);
            //this.mSensorMgr.registerListener(this, rotationSensor, Sensor.REPORTING_MODE_CONTINUOUS);
        }
        catch (Exception e)
        {
            Exception x = e;
            noSensorError();
        }
        setMainThread();
        initGameObjects();
    }

    private void calculateOrientation()
    {
        try
        {
            SensorManager.getOrientation(this.mOrientationRotationMatrix, this.mOrientationResult);
            SensorManager.getRotationMatrix(this.mOrientationRotationMatrix, null, this.mAccValues, this.mMagValues);
            SensorManager.remapCoordinateSystem(this.mOrientationRotationMatrix, 0, 180, this.mOrientationTransformMatrix);
            this.mOrientY = (float) Math.toDegrees((double) this.mOrientationResult[2]);
            this.mOrientZ = (float) (-Math.toDegrees((double) this.mOrientationResult[0]));
            this.mOrientX = (float) -Math.toDegrees((double) this.mOrientationResult[1]);

            if (mRotation == Surface.ROTATION_0)
            {
                float x = mOrientY;
                mOrientY = -mOrientX;
                mOrientX = x;
            }

        } catch (Exception x)
        {
            Log.e("calcOrientation", "\n\n Problem calculating orientation \n\n", x);
        }
        //Log.i("calculateOrientation", "*** mOrientX = " + mOrientX + ", mOrientY = " + mOrientY + ", mOrientZ = " + mOrientZ + "***");
    }

    public Activity getActivity()
    {
        return this.mContext;
    }

    public boolean hasSavedGame()
    {
        return this.mArchive.isLoaded();
    }

    public void saveGame()
    {
        this.mArchive.clear();
        this.mArchive.saveEyePos(mEye);
        this.mArchive.saveActivePowerup(this.mActivePowerup);
        this.mArchive.savePowerups(this.mPowerups);
        this.mArchive.saveGameLevel(this.mGameLevel);
        this.mArchive.saveGameSpace(this.mGameSpace);
        this.mArchive.store();
    }

    public void clearSaved()
    {
        this.mArchive.erase();
    }

    public boolean restoreGame()
    {
        if (!this.mArchive.isLoaded())
        {
            return false;
        }
        this.mGameState = STATE_PAUSE;
        this.mArchive.restoreEyePos(mEye);
        this.mArchive.restorePowerups(this.mPowerups);
        this.mArchive.restoreGameSpace(this.mGameSpace);
        loadLevel(this.mArchive.restoreGameLevel(), false);
        Powerup p = this.mArchive.restoreActivePowerup();
        if (p != null)
        {
            activatePowerup(p);
        }
        return true;
    }

    public void addScore(long score)
    {
        GameLevel gameLevel = this.mGameLevel;
        gameLevel.mScore += score;
    }

    public long getScore()
    {
        if (this.mGameLevel == null)
        {
            return 0;
        }
        return this.mGameLevel.mScore;
    }

    public void setScore(long score)
    {
        this.mGameLevel.mScore = score;
    }

    public void resumeSounds()
    {
        this.mSoundMgr.resumeLoops();
    }

    public void pauseSounds()
    {
        this.mSoundMgr.pauseLoops();
    }

    public void cleanup()
    {
        this.mSoundMgr.cleanup();
    }

    public void setContext(Activity c)
    {
        this.mContext = c;
    }

    public int getWaveCount()
    {
        return this.mGameLevel.getWaveCount();
    }

    public void addGameListener(GameListener g)
    {
        if (!this.mGameListeners.contains(g))
        {
            this.mGameListeners.add(g);
        }
    }

    public void removeGameListener(GameListener g)
    {
        if (this.mGameListeners.contains(g))
        {
            this.mGameListeners.remove(g);
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
                this.clearSaved();
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
        return this.mGameState;
    }

    public Eye getEye()
    {
        return mEye;
    }

    public synchronized Vector<Powerup> getPowerups()
    {
        return this.mPowerups;
    }

    public synchronized void addPowerup(Powerup p)
    {
        this.mPowerups.insertElementAt(p, STATE_NEW);
        while (this.mPowerups.size() > STATE_LEVEL_COMPLETE)
        {
            this.mPowerups.remove(STATE_LEVEL_COMPLETE);
        }
        informPowerupsChangedSafe();
    }

    public synchronized void addNewPowerups()
    {
        int size = this.mNewPowerups.size();
        for (int i = STATE_NEW; i < size; i += STATE_RESUME)
        {
            this.mPowerups.insertElementAt((Powerup) this.mNewPowerups.get(i), STATE_NEW);
        }
        this.mNewPowerups.clear();
        while (this.mPowerups.size() > STATE_LEVEL_COMPLETE)
        {
            this.mPowerups.remove(STATE_LEVEL_COMPLETE);
        }
        if (size > 0)
        {
            informPowerupsChangedSafe();
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public synchronized void activatePowerup(Powerup powerup)
    {
        if (this.mActivePowerup != null)
        {
            this.deactivatePowerup();
        }
        this.mPowerups.remove((Object) powerup);
        this.mActivePowerup = powerup;
        switch (powerup.mType)
        {
            default:
            {
                mEye.setBeamType(0);
                break;
            }
            case 1:
            {
                mEye.setBeamType(1);
                break;
            }
            case 2:
            {
                mEye.setBeamType(2);
                break;
            }
            case 3:
            {
                mEye.setBeamType(3);
                break;
            }
            case 4:
            {
                mEye.setBeamType(4);
                break;
            }
            case 5:
            {
                mEye.setBeamType(5);
                break;
            }
            case 6:
            {
                mEye.setBeamType(0);
                break;
            }
            case 7:
            {
                mEye.setBeamType(0);
                mSpeedMult = 0.25f;
            }
        }
        informPowerupsChangedSafe();

        return;
    }


    public synchronized void activatePowerup(int index)
    {
        activatePowerup((Powerup) this.mPowerups.get(index));
    }

    public synchronized void deactivatePowerup()
    {
        if (this.mActivePowerup != null)
        {
            switch (this.mActivePowerup.mType)
            {
                case STATE_PAUSE /*2*/:
                    mEye.setBeamType(STATE_NEW);
                    break;
                case STATE_OVER /*3*/:
                    mEye.setBeamType(STATE_NEW);
                    break;
                case STATE_LEVEL_COMPLETE /*4*/:
                    mEye.setBeamType(STATE_NEW);
                    break;
                case DateUtils.RANGE_MONTH_SUNDAY /*5*/:
                    mEye.setBeamType(STATE_NEW);
                    break;
                case DateUtils.RANGE_MONTH_MONDAY /*6*/:
                    break;
                case Powerup.POWERUP_SLOW /*7*/:
                    mSpeedMult = 1.0f;
                    break;
                default:
                    mEye.setBeamType(STATE_NEW);
                    break;
            }
        }
        this.mActivePowerup = null;
        informPowerupsChangedSafe();
    }

    public Powerup getActivePowerup()
    {
        return this.mActivePowerup;
    }

    public float getSpeedMult()
    {
        float mult = 0.8f;
        if (this.mGameLevel != null)
        {
            mult = this.mGameLevel.mSpeedBase;
        }
        return mult * mSpeedMult * mMetrics.density;
    }

    public void setMainThread()
    {
        this.mMainThread = Thread.currentThread();
        this.mHandler = new Handler();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
    }

    public float[] mRotationVector = new float[4];

    public void onSensorChanged(SensorEvent event)
    {
        final String comma = ", ";
        if (event.accuracy != 0)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                System.arraycopy(event.values, 0, this.mAccValues, 0, event.values.length);
                //Log.i("onSensorChanged.Accelerometer", event.values[0] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                System.arraycopy(event.values, 0, this.mMagValues, 0, event.values.length);
                //Log.i("onSensorChanged.MagneticField", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }

            if (this.mAccValues != null && this.mMagValues != null)
            {
                //Log.i("onSensorChanged", "mAccValues = " + mAccValues.toString() + ", mMagValues = " + mMagValues.toString());
                calculateOrientation();
            } else
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

    private void initGameObjects()
    {
        mEye = new Eye(this.mContext);
    }

    private void initSound()
    {
        this.mSoundMgr = new SoundMgr(mContext);
        this.mSoundMgr.loadLoop(R.raw.beam_collide_sound, 0.5f);
        this.mSoundMgr.loadLoop(R.raw.eye_sound, Eye.SPEED_FAST);
        this.mSoundMgr.loadLoop(R.raw.saucer_beam, 0.7f);
        this.mSoundMgr.loadSound(R.raw.appear_sound, 1.0f);
        this.mSoundMgr.loadSound(R.raw.explosion_sound, 0.5f);
        this.mSoundMgr.loadSound(R.raw.power_up_sound, 1.0f);
        this.mSoundMgr.loadSound(R.raw.scream_sound, 1.0f);
    }

    private void noSensorError()
    {
        new Builder(this.mContext)
                .setTitle("No Orientation Sensor")
                .setMessage("Your device does not seem to have an orientation sensor.  This game will not function without an technology.")
                .setNegativeButton("Quit", new OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        GameLogic.this.mContext.finish();
                    }
                })
                .create()
                .show();
    }

    private void resetGame()
    {
        this.mGameSpace.removeAll();
        loadNewLevel();
        this.mSoundMgr.startLoop(R.raw.eye_sound);
    }

    private synchronized void informStateChange()
    {
        Iterator it = this.mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onStateChange();
        }
    }

    private synchronized void informStateChangeSafe()
    {
        if (Thread.currentThread().equals(this.mMainThread))
        {
            informStateChange();
        } else
        {
            this.mHandler.post(new StateChangePoster());
        }
    }

    private synchronized void informPowerupsChanged()
    {
        Iterator it = this.mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onPowerupChange();
        }
        mEye.mReload = true;
    }

    private synchronized void informPowerupsChangedSafe()
    {
        if (Thread.currentThread().equals(this.mMainThread))
        {
            informPowerupsChanged();
        } else
        {
            this.mHandler.post(new PowerupChangePoster());
        }
    }

    private synchronized void informScoreChange()
    {
        Iterator it = this.mGameListeners.iterator();
        while (it.hasNext())
        {
            ((GameListener) it.next()).onScoreChange();
        }
    }

    private synchronized void informScoreChangeSafe()
    {
        if (Thread.currentThread().equals(this.mMainThread))
        {
            informScoreChange();
        } else
        {
            this.mHandler.post(new ScoreInformPoster());
        }
    }

    private void updateGame()
    {
        updateState();
        updateScore();
        updatePowerups();
        updateInput();
        updateAI();
        updateGraphics();
        updateSound();
        this.mGameStateChanged = false;
    }

    private synchronized void updateState()
    {
        if (!(this.mGameState == STATE_OVER || this.mGameLevel == null))
        {
            if (this.mGameLevel.saucersComplete() && this.mGameSpace.getSaucers().size() == 0)
            {
                setGameState(STATE_LEVEL_COMPLETE);
            } else if (this.mGameSpace.getCities().size() == 0)
            {
                setGameState(STATE_OVER);
            }
        }
    }

    private void updateScore()
    {
        if (this.mGameLevel != null && this.mLastScore != this.mGameLevel.mScore)
        {
            this.mLastScore = this.mGameLevel.mScore;
            informScoreChangeSafe();
        }
    }

    private void updateInput()
    {
        mEye.moveBeam(this.mOrientX, this.mOrientY, mStepMult);
        this.mGameSpace.adjustSaucerOffset(this.mOrientX, this.mOrientY);
        boolean collide = this.mGameSpace.collideBeam(mEye);
        this.mCollideChanged = this.mCollide ^ collide;
        this.mCollide = collide;
    }

    private synchronized void updatePowerups()
    {
        if (this.mActivePowerup != null)
        {
            Powerup powerup = this.mActivePowerup;
            Log.i("GameLogic.updatePowerups()", "\n***]n***\n*** mTimeLeft = " + powerup.mTimeLeft + "\n" +
                    "***]n***\n" +
                    "***\n");
            powerup.mTimeLeft = (int) (((float) powerup.mTimeLeft) - mTimeDiff);
            if (this.mActivePowerup.mTimeLeft <= 0)
            {
                deactivatePowerup();
            } else if (this.mActivePowerup.mType == 6)
            {
                Iterator it = this.mGameSpace.getSaucers().iterator();
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
        Vector<Saucer> saucerList = this.mGameSpace.getSaucers();
        Vector<City> cityList = this.mGameSpace.getCities();
        if (this.mGameLevel.mLimit > saucerList.size() && this.mGameLevel.saucerReady())
        {
            Saucer s = this.mGameLevel.newSaucer();
            if (s != null)
            {
                this.mGameSpace.addSaucer(s, true);
            }
        }
        this.mGameSpace.updateState(this.mNewPowerups);
        addNewPowerups();
        boolean attack = this.mGameSpace.areSaucersAttacking();
        this.mSaucerAttackChanged = this.mSaucerAttack ^ attack;
        this.mSaucerAttack = attack;
    }

    private void updateGraphics()
    {
        this.mGameSurface.updateGraphics();
    }

    private void updateSound()
    {
        if (this.mCollideChanged || this.mSaucerAttackChanged)
        {
            if (this.mCollide)
            {
                this.mSoundMgr.startLoop(R.raw.beam_collide_sound);
            } else if (this.mSaucerAttack)
            {
                this.mSoundMgr.startLoop(R.raw.saucer_beam);
            } else
            {
                this.mSoundMgr.startLoop(R.raw.eye_sound);
            }
        }
        int id = GameObject.getNextSound();
        if (id != STATE_UNINIT)
        {
            this.mSoundMgr.playSound(id);
        }
    }

    private void resumeMainLoop()
    {
        if (this.mMainLoop == null)
        {
            this.mMainLoop = new MainLoop();
            this.mMainLoop.start();
        }
    }

    private void pauseMainLoop()
    {
        if (this.mMainLoop != null)
        {
            this.mMainLoop.requestExitAndWait();
            this.mMainLoop = null;
        }
    }

    private void pauseMainLoopSafe()
    {
        if (Thread.currentThread().equals(this.mMainThread))
        {
            pauseMainLoop();
        } else
        {
            this.mHandler.post(new PauseGamePoster());
        }
    }

    private void loadLevel(GameLevel level, boolean newLevel)
    {
        this.mGameLevel = level;
        loadMap(newLevel);
    }

    private void loadNewLevel()
    {
        try
        {
            loadLevel(new GameLevel(new JSONObject(this.mContext.getResources().getString(this.mContext.getResources().getIdentifier("base_level", "string", "com.burtonshead.burningeye")))), true);
        } catch (Exception e)
        {
            Exception x = e;
            this.mContext.finish();
        }
        mEye.recenter();
    }

    private void loadNextLevel()
    {
        this.mGameSpace.restoreCities();
        mEye.recenter();
        this.mGameLevel.advanceLevel();
        if (this.mGameLevel.getZoneChange())
        {
            this.mGameSpace.removeAll();
            loadMap(true);
            return;
        }
        this.mGameSpace.removeAllSaucers();
    }

    private void loadMap(boolean newCities)
    {
        this.mMap = new Map(this.mGameLevel.getGameZone());
        this.mGameSurface.setMapImg(this.mMap.getMapResID());
        if (newCities)
        {
            this.mGameSpace.removeAllCities();
            this.mGameSpace.addCities(this.mMap.getCityLocs());
        }
    }

    private void processScoreSafe()
    {
        long score = this.mGameLevel != null ? this.mGameLevel.mScore : 0;
        if (Thread.currentThread().equals(this.mMainThread))
        {
            processScore(score);
        } else
        {
            this.mHandler.post(new ProcessScorePoster(score));
        }
    }

    private void processScore(long score)
    {
        App app = App.mInstance;
        Vector<HighScore> highScores = App.getSettings().getHighScores();
        if (highScores.size() == 0 || score >= ((HighScore) highScores.lastElement()).score)
        {
            Intent i = new Intent(this.mContext, ScoreScreen.class);
            i.putExtra(ScoreScreen.NEW_HIGH_SCORE_EXTRA, score);
            this.mContext.startActivity(i);
        }
    }
}

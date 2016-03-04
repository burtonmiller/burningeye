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
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import com.burtonshead.burningeye.ScoreScreen;
import com.burtonshead.burningeye.gamespace.City;
import com.burtonshead.burningeye.gamespace.Eye;
import com.burtonshead.burningeye.gamespace.GameObject;
import com.burtonshead.burningeye.gamespace.GameSpace;
import com.burtonshead.burningeye.gamespace.Saucer;
import com.burtonshead.burningeye.powerup.Powerup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONObject;

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

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.1 */
    class C00371 implements OnClickListener
    {
        C00371()
        {
        }

        public void onClick(DialogInterface dialog, int which)
        {
            GameLogic.this.mContext.finish();
        }
    }

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.2 */
    class C00382 implements Runnable
    {
        C00382()
        {
        }

        public void run()
        {
            GameLogic.this.informStateChange();
        }
    }

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.3 */
    class C00393 implements Runnable
    {
        C00393()
        {
        }

        public void run()
        {
            GameLogic.this.informPowerupsChanged();
        }
    }

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.4 */
    class C00404 implements Runnable
    {
        C00404()
        {
        }

        public void run()
        {
            GameLogic.this.informScoreChange();
        }
    }

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.5 */
    class C00415 implements Runnable
    {
        C00415()
        {
        }

        public void run()
        {
            GameLogic.this.pauseMainLoop();
        }
    }

    /* renamed from: com.burtonshead.burningeye.logic.GameLogic.6 */
    class C00426 implements Runnable
    {
        private final /* synthetic */ long val$score;

        C00426(long j)
        {
            this.val$score = j;
        }

        public void run()
        {
            GameLogic.this.processScore(this.val$score);
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
                    Log.i("MainLoop.run", "*** mStepMult = " + GameLogic.mStepMult + " ***");
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

    class SoundMgr
    {
        private MediaPlayer mLoopPaused;
        private HashMap<Integer, MediaPlayer> mLoops;
        private boolean mPaused;
        private HashMap<Integer, Integer> mSoundMap;
        private SoundPool mSoundPool;
        private HashMap<Integer, Float> mSoundVol;

        public SoundMgr()
        {
            this.mSoundMap = new HashMap();
            this.mSoundVol = new HashMap();
            this.mLoops = new HashMap();
            this.mPaused = false;
            this.mSoundPool = new SoundPool(50, GameLogic.STATE_OVER, GameLogic.STATE_NEW);
        }

        private void loadSound(int resID, float vol)
        {
            int id = this.mSoundPool.load(GameLogic.this.mContext, resID, GameLogic.STATE_RESUME);
            this.mSoundMap.put(Integer.valueOf(resID), Integer.valueOf(id));
            this.mSoundVol.put(Integer.valueOf(id), Float.valueOf(vol));
        }

        public void playSound(int resID)
        {
            int id = ((Integer) this.mSoundMap.get(Integer.valueOf(resID))).intValue();
            float vol = ((Float) this.mSoundVol.get(Integer.valueOf(id))).floatValue();
            this.mSoundPool.play(id, vol, vol, GameLogic.STATE_RESUME, GameLogic.STATE_NEW, 1.0f);
        }

        private void loadLoop(int resID, float vol)
        {
            MediaPlayer m = MediaPlayer.create(GameLogic.this.mContext, resID);
            m.setLooping(true);
            m.setVolume(vol, vol);
            this.mLoops.put(Integer.valueOf(resID), m);
        }

        public void startLoop(int resID)
        {
            MediaPlayer loop = (MediaPlayer) this.mLoops.get(Integer.valueOf(resID));
            if (this.mPaused)
            {
                this.mLoopPaused = loop;
                return;
            }
            for (MediaPlayer m : this.mLoops.values())
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
            MediaPlayer loop = (MediaPlayer) this.mLoops.get(Integer.valueOf(resID));
            if (this.mLoopPaused.equals(loop))
            {
                this.mLoopPaused = null;
            }
            loop.pause();
        }

        public void pauseLoops()
        {
            this.mPaused = true;
            for (MediaPlayer m : this.mLoops.values())
            {
                if (m.isPlaying())
                {
                    m.pause();
                    this.mLoopPaused = m;
                }
            }
        }

        public void resumeLoops()
        {
            this.mPaused = false;
            if (this.mLoopPaused != null)
            {
                this.mLoopPaused.start();
                this.mLoopPaused = null;
            }
        }

        public void cleanup()
        {
            pauseLoops();
            this.mSoundMap.clear();
            this.mSoundPool.release();
            for (MediaPlayer m : this.mLoops.values())
            {
                m.stop();
                m.release();
            }
            this.mLoops.clear();
            this.mPaused = false;
            this.mLoopPaused = null;
        }
    }

    public GameLogic(Activity c, GameSurface g)
    {
        this.mActivePowerup = null;
        this.mContext = null;
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
        mInstance = this;
        this.mArchive = new GameArchive(c);
        this.mArchive.load();
        mTimeDiff = 0.0f;
        mStepMult = 1.0f;
        mSpeedMult = 1.0f;
        setContext(c);
        initSound();
        mMetrics = new DisplayMetrics();
        this.mContext.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        int h = mMetrics.heightPixels;
        int w = mMetrics.widthPixels;
        this.mGameSurface = g;
        this.mGameSpace = new GameSpace((float) h, (float) w);
        // pause 2, resume 1
        try
        {
            this.mSensorMgr = (SensorManager) this.mContext.getSystemService("sensor");
            Sensor aSensor = this.mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor mSensor = this.mSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            this.mSensorMgr.registerListener(this, aSensor, Sensor.REPORTING_MODE_CONTINUOUS);
            this.mSensorMgr.registerListener(this, mSensor, Sensor.REPORTING_MODE_CONTINUOUS);
        } catch (Exception e)
        {
            Exception x = e;
            noSensorError();
        }
        setMainThread();
        initGameObjects();
    }

    private void calculateOrientation()
    {
        SensorManager.getOrientation(this.mOrientationRotationMatrix, this.mOrientationResult);
        this.mOrientY = (float) Math.toDegrees((double) this.mOrientationResult[STATE_PAUSE]);
        this.mOrientZ = (float) (-Math.toDegrees((double) this.mOrientationResult[STATE_NEW]));
        SensorManager.getRotationMatrix(this.mOrientationRotationMatrix, null, this.mAccValues, this.mMagValues);
        SensorManager.remapCoordinateSystem(this.mOrientationRotationMatrix, STATE_PAUSE, 129, this.mOrientationTransformMatrix);
        SensorManager.getOrientation(this.mOrientationTransformMatrix, this.mOrientationResult);
        this.mOrientX = (float) Math.toDegrees((double) this.mOrientationResult[STATE_PAUSE]);
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


    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
//    public static final int STATE_LEVEL_COMPLETE = 4;
//    public static final int STATE_NEW = 0;
//    public static final int STATE_OVER = 3;
//    public static final int STATE_PAUSE = 2;
//    public static final int STATE_RESUME = 1;
//    public static final int STATE_UNINIT = -1;
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

    public void onSensorChanged(SensorEvent event)
    {
        final String comma = ", ";
        if (event.accuracy != 0)
        {
            if (event.sensor.getType() == STATE_RESUME)
            {
                System.arraycopy(event.values, STATE_NEW, this.mAccValues, STATE_NEW, event.values.length);
                //Log.i("onSensorChanged.Accelerometer", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }
            if (event.sensor.getType() == STATE_PAUSE)
            {
                System.arraycopy(event.values, STATE_NEW, this.mMagValues, STATE_NEW, event.values.length);
                //Log.i("onSensorChanged.MagneticField", event.values[STATE_NEW] + comma + event.values[STATE_RESUME] + comma + event.values[STATE_PAUSE] + ")");
            }
            if (this.mAccValues != null && this.mMagValues != null)
            {
                calculateOrientation();
            }
        }
    }

    private void initGameObjects()
    {
        mEye = new Eye(this.mContext);
    }

    private void initSound()
    {
        this.mSoundMgr = new SoundMgr();
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
        new Builder(this.mContext).setTitle("No Orientation Sensor").setMessage("Your device does not seem to have an orientation sensor.  This game will not function without an technology.").setNegativeButton("Quit", new C00371()).create().show();
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
            this.mHandler.post(new C00382());
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
            this.mHandler.post(new C00393());
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
            this.mHandler.post(new C00404());
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
            this.mHandler.post(new C00415());
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
            this.mHandler.post(new C00426(score));
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

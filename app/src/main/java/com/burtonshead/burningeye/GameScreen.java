package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.burtonshead.burningeye.logic.GameListener;
import com.burtonshead.burningeye.logic.GameLogic;
import com.burtonshead.burningeye.logic.GameSurface;
import com.burtonshead.burningeye.powerup.Powerup;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.util.Vector;


public class GameScreen extends GameActivity
{
    public static GameScreen mInstance;
    private LinearLayout mControls;
    private TextView mExitButton;
    private GameListener mGameListener;
    private GameLogic mGameLogic;
    private GameSurface mGameSurface;
    private LinearLayout mLevelControls;
    private TextView mPlayButton;
    private ImageButton[] mPowerupButtons;
    private LinearLayout mPowerupPanel;
    private TextView mResumeButton;
    private TextView mScore;
    private TextView mWaveExitButton;
    private TextView mWaveResumeButton;
    private TextView mWaveText;

    public GameScreen()
    {
        mPowerupButtons = new ImageButton[4];
        mGameListener = null;
        mGameSurface = null;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_screen);
        init(R.drawable.dim_bkg);
        mInstance = this;
    }

    public void onStart()
    {
        init();
        super.onStart();
    }

    public void onPause()
    {
        showBkgImage(false);
        if (mGameLogic.getGameState() == GameLogic.STATE_RESUME)
        {
            mGameLogic.setGameState(GameLogic.STATE_PAUSE);
        }
        mGameLogic.pauseSounds();
        App.mInstance.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    public void onResume()
    {
        super.onResume();
        showBkgImage(true);
        App.mInstance.playBkgMusic();
        mGameLogic.resumeSounds();
        mGameLogic.setMainThread();
        gameStateChanged();
    }

    public void onStop()
    {
        if (mGameLogic.getGameState() == GameLogic.STATE_PAUSE || mGameLogic.getGameState() == GameLogic.STATE_LEVEL_COMPLETE)
        {
            mGameLogic.saveGame();
            mGameLogic.cleanup();
        }
        mGameLogic = null;
        App.mInstance.releaseAllBitmaps(false);
        System.gc();
        super.onStop();
    }

    private void init()
    {
        mGameSurface = (GameSurface) findViewById(R.id.game_surface);
        mGameLogic = new GameLogic(this, mGameSurface);
        mGameSurface.init(this);
        mGameListener = new BurningEyeListener();
        mGameLogic.addGameListener(mGameListener);
        Typeface smallFont = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        Typeface controlFont = Typeface.createFromAsset(getAssets(), "fonts/add_city_electric.ttf");
        mWaveText = (TextView) findViewById(R.id.wave_text);
        mWaveText.setTypeface(controlFont);
        mScore = (TextView) findViewById(R.id.score);
        mScore.setTypeface(smallFont);
        mPlayButton = (TextView) findViewById(R.id.play_button);
        mPlayButton.setTypeface(controlFont);
        mPlayButton.setOnClickListener(new PlayButtonListener());
        mResumeButton = (TextView) findViewById(R.id.resume_button);
        mResumeButton.setTypeface(controlFont);
        mWaveResumeButton = (TextView) findViewById(R.id.wave_continue_button);
        mWaveResumeButton.setTypeface(controlFont);
        OnClickListener resumeListener = new ResumeListener();
        mResumeButton.setOnClickListener(resumeListener);
        mWaveResumeButton.setOnClickListener(resumeListener);
        mExitButton = (TextView) findViewById(R.id.exit_button);
        mExitButton.setTypeface(controlFont);
        mWaveExitButton = (TextView) findViewById(R.id.wave_exit_button);
        mWaveExitButton.setTypeface(controlFont);
        OnClickListener exitListener = new EndGameListener();
        mExitButton.setOnClickListener(exitListener);
        mWaveExitButton.setOnClickListener(exitListener);
        findViewById(R.id.inner_view).setOnClickListener(new C00175());
        mControls = (LinearLayout) findViewById(R.id.control_panel);
        mLevelControls = (LinearLayout) findViewById(R.id.level_control_panel);
        mPowerupPanel = (LinearLayout) findViewById(R.id.powerup_panel);
        mPowerupButtons[0] = (ImageButton) findViewById(R.id.powerup_button_1);
        mPowerupButtons[0].setOnClickListener(new C00186());
        mPowerupButtons[1] = (ImageButton) findViewById(R.id.powerup_button_2);
        mPowerupButtons[1].setOnClickListener(new C00197());
        mPowerupButtons[2] = (ImageButton) findViewById(R.id.powerup_button_3);
        mPowerupButtons[2].setOnClickListener(new C00208());
        mPowerupButtons[3] = (ImageButton) findViewById(R.id.powerup_button_4);
        mPowerupButtons[3].setOnClickListener(new C00219());
        mGameLogic.setGameState(GameLogic.STATE_UNINIT);
    }

    private void gameStateChanged()
    {
        switch (mGameLogic.getGameState())
        {
            case GameLogic.STATE_RESUME:
                showBkgImage(false);
                App.mInstance.pauseBkgMusic();
                mGameSurface.setVisibility(0);
                mControls.setVisibility(8);
                mLevelControls.setVisibility(8);
                showPowerups(true);
                break;
            case GameLogic.STATE_PAUSE:
                App.mInstance.playBkgMusic();
                mControls.setVisibility(0);
                mPlayButton.setVisibility(8);
                mResumeButton.setVisibility(0);
                mExitButton.setVisibility(0);
                mLevelControls.setVisibility(8);
                showPowerups(false);
                break;
            case GameLogic.STATE_OVER:
                App.mInstance.playBkgMusic();
                mGameSurface.setVisibility(8);
                mControls.setVisibility(0);
                mPlayButton.setVisibility(0);
                mResumeButton.setVisibility(8);
                mExitButton.setVisibility(8);
                mLevelControls.setVisibility(8);
                showPowerups(false);
                break;
            case GameLogic.STATE_LEVEL_COMPLETE:
                if (App.getProps().getGameType() != 0 && mGameLogic.getWaveCount() >= 15)
                {
                    startActivity(new Intent(this, UpgradeScreen.class));
                    finish();
                    break;
                }
                App.mInstance.playBkgMusic();
                mControls.setVisibility(8);
                mLevelControls.setVisibility(0);
                mWaveText.setText("Wave " + mGameLogic.getWaveCount() + " Complete");
                showPowerups(false);
                break;
            default:
                mGameSurface.setVisibility(8);
                mControls.setVisibility(0);
                mLevelControls.setVisibility(8);
                mPlayButton.setVisibility(0);
                mResumeButton.setVisibility(GameLogic.mInstance.hasSavedGame() ? 0 : 8);
                mExitButton.setVisibility(8);
                showPowerups(false);
                break;
        }
        scoreChanged();
    }

    private void showPowerups(boolean s)
    {
        if (s)
        {
            powerupsChanged();
        }
        mPowerupPanel.setVisibility(s ? 0 : 8);
    }

    private void powerupsChanged()
    {
        Vector<Powerup> powerups = mGameLogic.getPowerups();
        int size = powerups.size();
        for (int i = 0; i < 4; i++)
        {
            if (i >= size)
            {
                mPowerupButtons[i].setVisibility(8);
            } else
            {
                mPowerupButtons[i].setBackgroundDrawable(getResources().getDrawable(((Powerup) powerups.get(i)).getDrawableID()));
                mPowerupButtons[i].setVisibility(0);
            }
        }
    }

    private void scoreChanged()
    {
        mScore.setText("" + GameLogic.mInstance.getScore());
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.1 */
    class BurningEyeListener implements GameListener
    {
        BurningEyeListener()
        {
        }

        public void onStateChange()
        {
            GameScreen.this.gameStateChanged();
        }

        public void onPowerupChange()
        {
            GameScreen.this.powerupsChanged();
        }

        public void onScoreChange()
        {
            GameScreen.this.scoreChanged();
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.2 */
    class PlayButtonListener implements OnClickListener
    {
        PlayButtonListener()
        {
        }

        public void onClick(View v)
        {
            GameScreen.this.mGameLogic.setGameState(GameLogic.STATE_NEW );
            GameScreen.this.mGameLogic.setGameState(GameLogic.STATE_RESUME);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.3 */
    class ResumeListener implements OnClickListener
    {
        ResumeListener()
        {
        }

        public void onClick(View v)
        {
            int gameState = GameScreen.this.mGameLogic.getGameState();
            if (gameState == GameLogic.STATE_PAUSE || gameState == GameLogic.STATE_LEVEL_COMPLETE)
            {
                GameScreen.this.mGameLogic.setGameState(GameLogic.STATE_RESUME);
                return;
            }
            GameScreen.this.mGameLogic.restoreGame();
            GameScreen.this.mGameLogic.setGameState(GameLogic.STATE_RESUME);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.4 */
    class EndGameListener implements OnClickListener
    {
        EndGameListener()
        {
        }

        public void onClick(View v)
        {
            mGameLogic.setGameState(GameLogic.STATE_OVER);
            startActivity(new Intent(GameScreen.this.getApplicationContext(), ScoreScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.5 */
    class C00175 implements OnClickListener
    {
        C00175()
        {
        }

        public void onClick(View v)
        {
            if (mGameLogic.getGameState() == GameLogic.STATE_LEVEL_COMPLETE)
            {
                return;
            }

            GameScreen.this.mGameLogic.setGameState(GameLogic.STATE_PAUSE);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.6 */
    class C00186 implements OnClickListener
    {
        C00186()
        {
        }

        public void onClick(View v)
        {
            GameScreen.this.mGameLogic.activatePowerup(0);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.7 */
    class C00197 implements OnClickListener
    {
        C00197()
        {
        }

        public void onClick(View v)
        {
            GameScreen.this.mGameLogic.activatePowerup(1);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.8 */
    class C00208 implements OnClickListener
    {
        C00208()
        {
        }

        public void onClick(View v)
        {
            GameScreen.this.mGameLogic.activatePowerup(2);
        }
    }

    /* renamed from: com.burtonshead.burningeye.GameScreen.9 */
    class C00219 implements OnClickListener
    {
        C00219()
        {
        }

        public void onClick(View v)
        {
            GameScreen.this.mGameLogic.activatePowerup(3);
        }
    }
}

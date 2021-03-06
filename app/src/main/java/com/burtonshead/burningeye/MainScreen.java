package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class MainScreen extends GameActivity
{
    private TextView mAboutButton;
    private TextView mPlayButton;
    private TextView mScoreButton;
    private TextView mStoryButton;
    private TextView mTutorialButton;
    private TextView mUpgradeButton;
    private TextView mSettingsButton;

    /* renamed from: com.burtonshead.burningeye.MainScreen.1 */
    class C00221 implements OnClickListener
    {
        C00221()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), GameScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.2 */
    class C00232 implements OnClickListener
    {
        C00232()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), StoryScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.3 */
    class C00243 implements OnClickListener
    {
        C00243()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), TutorialScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.4 */
    class C00254 implements OnClickListener
    {
        C00254()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), ScoreScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.5 */
    class C00265 implements OnClickListener
    {
        C00265()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), AboutScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.6 */
    class C00276 implements OnClickListener
    {
        C00276()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), UpgradeScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.MainScreen.6 */
    class SettingsListener implements OnClickListener
    {
        SettingsListener()
        {
        }

        public void onClick(View v)
        {
            MainScreen.this.startActivity(new Intent(MainScreen.this.getApplicationContext(), SettingsScreen.class));
        }
    }


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        init(R.drawable.main_bkg);
        ((TextView) findViewById(R.id.title)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/acidic.ttf"));
        mPlayButton = (TextView) findViewById(R.id.play_button);
        mStoryButton = (TextView) findViewById(R.id.story_button);
        mTutorialButton = (TextView) findViewById(R.id.tutorial_button);
        mScoreButton = (TextView) findViewById(R.id.score_button);
        mAboutButton = (TextView) findViewById(R.id.about_button);
        mUpgradeButton = (TextView) findViewById(R.id.upgrade_button);
        mSettingsButton = (TextView) findViewById(R.id.settings_button);
        if (App.getProps().getGameType() == 0)
        {
            mUpgradeButton.setVisibility(View.GONE);
        }
        Typeface tf = Typeface.create(Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf"), 1);
        mPlayButton.setTypeface(tf);
        mStoryButton.setTypeface(tf);
        mTutorialButton.setTypeface(tf);
        mScoreButton.setTypeface(tf);
        mAboutButton.setTypeface(tf);
        mUpgradeButton.setTypeface(tf);
        mPlayButton.setOnClickListener(new C00221());
        mStoryButton.setOnClickListener(new C00232());
        mTutorialButton.setOnClickListener(new C00243());
        mScoreButton.setOnClickListener(new C00254());
        mAboutButton.setOnClickListener(new C00265());
        mUpgradeButton.setOnClickListener(new C00276());
        mSettingsButton.setOnClickListener(new SettingsListener());
    }

    public void onPause()
    {
        showBkgImage(false);
        App.sApp.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    public void onResume()
    {
        showBkgImage(true);
        super.onResume();
        App.sApp.playBkgMusic();
    }
}

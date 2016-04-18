package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsScreen extends GameActivity implements SeekBar.OnSeekBarChangeListener
{
    private TextView mBackButton;
    private SeekBar mMusicLevel;
    private SeekBar mFXLevel;

    class BackListener implements OnClickListener
    {
        public void onClick(View v)
        {
            SettingsScreen.this.startActivity(new Intent(SettingsScreen.this.getApplicationContext(), MainScreen.class));
        }
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_screen);
        init(R.drawable.settings_bkg);

        this.mBackButton = (TextView) findViewById(R.id.settings_back_button);
        this.mBackButton.setOnClickListener(new BackListener());
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf"));

        Typeface settingsFont = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        ((TextView) findViewById(R.id.settings_music_text)).setTypeface(settingsFont);
        ((TextView) findViewById(R.id.settings_soundfx_text)).setTypeface(settingsFont);

        mMusicLevel = (SeekBar) findViewById(R.id.settings_music);
        mFXLevel = (SeekBar) findViewById(R.id.settings_soundfx);

        mMusicLevel.setOnSeekBarChangeListener(this);
        mFXLevel.setOnSeekBarChangeListener(this);

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
        super.onResume();

        mMusicLevel.setProgress(App.sApp.getSettings().getMusicLevel());
        mFXLevel.setProgress(App.sApp.getSettings().getFXLevel());

        showBkgImage(true);
        App.sApp.playBkgMusic();
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
    {
    }

    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    public void onStopTrackingTouch(SeekBar seekBar)
    {
        int progress = seekBar.getProgress();

        if (seekBar == mMusicLevel)
        {
            App.sApp.getSettings().setMusicLevel(progress);
            App.sApp.syncBkgMusicVolume();
        }
        else // FXLevel
        {
            App.sApp.getSettings().setFXLevel(progress);
        }

        App.sApp.getSettings().store();
    }
}

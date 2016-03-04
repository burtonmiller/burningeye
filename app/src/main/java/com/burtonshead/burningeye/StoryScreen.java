package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class StoryScreen extends GameActivity {
    private TextView mBackButton;

    /* renamed from: com.burtonshead.burningeye.StoryScreen.1 */
    class C00321 implements OnClickListener {
        C00321() {
        }

        public void onClick(View v) {
            StoryScreen.this.startActivity(new Intent(StoryScreen.this.getApplicationContext(), MainScreen.class));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_screen);
        init(R.drawable.story_bkg);
        this.mBackButton = (TextView) findViewById(R.id.story_back_button);
        this.mBackButton.setOnClickListener(new C00321());
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/add_city_electric.ttf"));
        Typeface storyFont = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        ((TextView) findViewById(R.id.story_1)).setTypeface(storyFont);
        ((TextView) findViewById(R.id.story_2)).setTypeface(storyFont);
        ((TextView) findViewById(R.id.story_3)).setTypeface(storyFont);
    }

    public void onPause() {
        showBkgImage(false);
        App.mInstance.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        showBkgImage(true);
        App.mInstance.playBkgMusic();
        ((ScrollView) findViewById(R.id.scroller)).scrollTo(0, 0);
    }
}

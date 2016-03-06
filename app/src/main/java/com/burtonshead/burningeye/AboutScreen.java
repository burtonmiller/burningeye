package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutScreen extends GameActivity {
    private TextView mBackButton;

    /* renamed from: com.burtonshead.burningeye.AboutScreen.1 */
    class C00111 implements OnClickListener {
        C00111() {
        }

        public void onClick(View v) {
            AboutScreen.this.startActivity(new Intent(AboutScreen.this.getApplicationContext(), MainScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.AboutScreen.2 */
    class C00122 implements OnClickListener {
        C00122() {
        }

        public void onClick(View v) {
            AboutScreen.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://www.burtonshead.com")));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_screen);
        init(R.drawable.about_bkg);
        this.mBackButton = (TextView) findViewById(R.id.about_back_button);
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/add_city_electric.ttf"));
        this.mBackButton.setOnClickListener(new C00111());
        Typeface smallFont = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        TextView link = (TextView) findViewById(R.id.low_blow_link);
        link.setTypeface(smallFont);
        link.setOnClickListener(new C00122());
        ((TextView) findViewById(R.id.design_credit)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.code_credit)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.graphics_credit)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.sound_credit)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.music_credit)).setTypeface(smallFont);
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
    }
}

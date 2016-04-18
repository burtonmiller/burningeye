package com.burtonshead.burningeye;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class UpgradeScreen extends GameActivity {
    private TextView mBackButton;
    private TextView mUpgradeButton;

    /* renamed from: com.burtonshead.burningeye.UpgradeScreen.1 */
    class C00351 implements OnClickListener {
        C00351() {
        }

        public void onClick(View v) {
            UpgradeScreen.this.startActivity(new Intent(UpgradeScreen.this.getApplicationContext(), MainScreen.class));
            UpgradeScreen.this.finish();
        }
    }

    /* renamed from: com.burtonshead.burningeye.UpgradeScreen.2 */
    class C00362 implements OnClickListener {
        C00362() {
        }

        public void onClick(View v) {
            UpgradeScreen.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.burtonshead.burningeye")));
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        String str = "fonts/white_rabbit.ttf";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_screen);
        init(R.drawable.story_bkg);
        this.mBackButton = (TextView) findViewById(R.id.story_back_button);
        this.mBackButton.setOnClickListener(new C00351());
        String str2 = "fonts/white_rabbit.ttf";
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), str));
        this.mUpgradeButton = (TextView) findViewById(R.id.upgrade_button);
        this.mUpgradeButton.setOnClickListener(new C00362());
        str2 = "fonts/white_rabbit.ttf";
        this.mUpgradeButton.setTypeface(Typeface.createFromAsset(getAssets(), str));
        Typeface smallFont = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        ((TextView) findViewById(R.id.bullet_1)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.bullet_2)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.bullet_3)).setTypeface(smallFont);
        ((TextView) findViewById(R.id.bullet_4)).setTypeface(smallFont);
    }

    public void onPause() {
        showBkgImage(false);
        App.sApp.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        showBkgImage(true);
        App.sApp.playBkgMusic();
    }
}

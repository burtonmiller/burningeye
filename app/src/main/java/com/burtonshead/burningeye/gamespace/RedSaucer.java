package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;
import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import org.apache.commons.lang.time.FastDateFormat;

public class RedSaucer extends Saucer {
    public RedSaucer(float x, float y) {
        super(20, x, y, 3.0f, Eye.Y_TOLERANCE, 120.0f, 1.0f, 0.1f, 30, 250);
        this.mBeamColor = -1426128896;
        this.mPulseStep = 0.2f;
        this.mAttackBitmaps = new Bitmap[6];
        this.mAttackBitmaps[0] = App.sApp.getBitmap(R.drawable.red_saucer_attack_1);
        this.mAttackBitmaps[1] = App.sApp.getBitmap(R.drawable.red_saucer_attack_2);
        this.mAttackBitmaps[2] = App.sApp.getBitmap(R.drawable.red_saucer_attack_3);
        this.mAttackBitmaps[3] = App.sApp.getBitmap(R.drawable.red_saucer_attack_4);
        this.mAttackBitmaps[4] = App.sApp.getBitmap(R.drawable.red_saucer_attack_5);
        this.mAttackBitmaps[5] = App.sApp.getBitmap(R.drawable.red_saucer_attack_6);
        this.mNormalBitmaps = new Bitmap[6];
        this.mNormalBitmaps[0] = App.sApp.getBitmap(R.drawable.red_saucer_normal_1);
        this.mNormalBitmaps[1] = App.sApp.getBitmap(R.drawable.red_saucer_normal_2);
        this.mNormalBitmaps[2] = App.sApp.getBitmap(R.drawable.red_saucer_normal_3);
        this.mNormalBitmaps[3] = App.sApp.getBitmap(R.drawable.red_saucer_normal_4);
        this.mNormalBitmaps[4] = App.sApp.getBitmap(R.drawable.red_saucer_normal_5);
        this.mNormalBitmaps[5] = App.sApp.getBitmap(R.drawable.red_saucer_normal_6);
        this.mHitBitmaps = new Bitmap[3];
        this.mHitBitmaps[0] = App.sApp.getBitmap(R.drawable.red_saucer_dmg_1);
        this.mHitBitmaps[1] = App.sApp.getBitmap(R.drawable.red_saucer_dmg_2);
        this.mHitBitmaps[2] = App.sApp.getBitmap(R.drawable.red_saucer_dmg_3);
    }

    public Bitmap getDimShiftBitmap() {
        switch ((int) (Math.random() * 3.0d)) {
            case FastDateFormat.LONG /*1*/:
                return App.sApp.getBitmap(R.drawable.exp_60_2);
            case FastDateFormat.MEDIUM /*2*/:
                return App.sApp.getBitmap(R.drawable.exp_60_3);
            default:
                return App.sApp.getBitmap(R.drawable.exp_60_1);
        }
    }
}

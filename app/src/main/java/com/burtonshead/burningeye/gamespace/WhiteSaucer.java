package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;
import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import org.apache.commons.lang.time.FastDateFormat;

public class WhiteSaucer extends Saucer {
    public WhiteSaucer(float x, float y) {
        super(15, x, y, 5.0f, 3.0f, 90.0f, 1.5f, Eye.SPEED_SLOW, 40, 400);
        this.mBeamColor = -1426063361;
        this.mPulseStep = Eye.SPEED_FAST;
        this.mAttackBitmaps = new Bitmap[4];
        this.mAttackBitmaps[0] = App.mInstance.getBitmap(R.drawable.white_saucer_attack_1);
        this.mAttackBitmaps[1] = App.mInstance.getBitmap(R.drawable.white_saucer_attack_2);
        this.mAttackBitmaps[2] = App.mInstance.getBitmap(R.drawable.white_saucer_attack_3);
        this.mAttackBitmaps[3] = App.mInstance.getBitmap(R.drawable.white_saucer_attack_4);
        this.mNormalBitmaps = new Bitmap[6];
        this.mNormalBitmaps[0] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_1);
        this.mNormalBitmaps[1] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_2);
        this.mNormalBitmaps[2] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_3);
        this.mNormalBitmaps[3] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_4);
        this.mNormalBitmaps[4] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_5);
        this.mNormalBitmaps[5] = App.mInstance.getBitmap(R.drawable.white_saucer_normal_6);
        this.mHitBitmaps = new Bitmap[3];
        this.mHitBitmaps[0] = App.mInstance.getBitmap(R.drawable.white_saucer_dmg_1);
        this.mHitBitmaps[1] = App.mInstance.getBitmap(R.drawable.white_saucer_dmg_2);
        this.mHitBitmaps[2] = App.mInstance.getBitmap(R.drawable.white_saucer_dmg_3);
    }

    public Bitmap getDimShiftBitmap() {
        switch ((int) (Math.random() * 3.0d)) {
            case FastDateFormat.LONG /*1*/:
                return App.mInstance.getBitmap(R.drawable.exp_45_2);
            case FastDateFormat.MEDIUM /*2*/:
                return App.mInstance.getBitmap(R.drawable.exp_45_3);
            default:
                return App.mInstance.getBitmap(R.drawable.exp_45_1);
        }
    }
}

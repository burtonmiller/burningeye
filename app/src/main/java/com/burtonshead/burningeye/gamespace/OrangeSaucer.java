package com.burtonshead.burningeye.gamespace;

import android.graphics.Bitmap;
import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;
import org.apache.commons.lang.time.FastDateFormat;

public class OrangeSaucer extends Saucer {
    public OrangeSaucer(float x, float y) {
        super(20, x, y, 3.0f, 5.0f, 120.0f, 1.25f, Eye.SPEED_NORMAL, 60, 625);
        this.mBeamColor = -1426229504;
        this.mPulseStep = 0.3f;
        this.mAttackBitmaps = new Bitmap[4];
        this.mAttackBitmaps[0] = App.mInstance.getBitmap(R.drawable.orange_saucer_attack_1);
        this.mAttackBitmaps[1] = App.mInstance.getBitmap(R.drawable.orange_saucer_attack_2);
        this.mAttackBitmaps[2] = App.mInstance.getBitmap(R.drawable.orange_saucer_attack_3);
        this.mAttackBitmaps[3] = App.mInstance.getBitmap(R.drawable.orange_saucer_attack_4);
        this.mNormalBitmaps = new Bitmap[4];
        this.mNormalBitmaps[0] = App.mInstance.getBitmap(R.drawable.orange_saucer_normal_1);
        this.mNormalBitmaps[1] = App.mInstance.getBitmap(R.drawable.orange_saucer_normal_2);
        this.mNormalBitmaps[2] = App.mInstance.getBitmap(R.drawable.orange_saucer_normal_3);
        this.mNormalBitmaps[3] = App.mInstance.getBitmap(R.drawable.orange_saucer_normal_4);
        this.mHitBitmaps = new Bitmap[3];
        this.mHitBitmaps[0] = App.mInstance.getBitmap(R.drawable.orange_saucer_dmg_1);
        this.mHitBitmaps[1] = App.mInstance.getBitmap(R.drawable.orange_saucer_dmg_2);
        this.mHitBitmaps[2] = App.mInstance.getBitmap(R.drawable.orange_saucer_dmg_3);
    }

    public Bitmap getDimShiftBitmap() {
        switch ((int) (Math.random() * 3.0d)) {
            case FastDateFormat.LONG /*1*/:
                return App.mInstance.getBitmap(R.drawable.exp_60_2);
            case FastDateFormat.MEDIUM /*2*/:
                return App.mInstance.getBitmap(R.drawable.exp_60_3);
            default:
                return App.mInstance.getBitmap(R.drawable.exp_60_1);
        }
    }
}

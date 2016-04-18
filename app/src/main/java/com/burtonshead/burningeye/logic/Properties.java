package com.burtonshead.burningeye.logic;

import com.burtonshead.burningeye.App;
import com.burtonshead.burningeye.R;

public class Properties {
    public static final int FULL_VERSION = 0;
    public static final int LITE_VERSION = 1;
    private int mGameType;

    public Properties() {
        if (App.sApp.getResources().getString(R.string.game_type).equals("full")) {
            this.mGameType = FULL_VERSION;
        } else {
            this.mGameType = LITE_VERSION;
        }
    }

    public int getGameType() {
        return this.mGameType;
    }
}

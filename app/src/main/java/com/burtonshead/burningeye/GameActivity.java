package com.burtonshead.burningeye;

import android.app.Activity;
import android.widget.ImageView;

public class GameActivity extends Activity
{
    protected int mBkgImg;

    public GameActivity()
    {
        this.mBkgImg = 0;
    }

    public void init(int bkgImg)
    {
        this.mBkgImg = bkgImg;
    }

    public void showBkgImage(boolean show)
    {
        if (show)
        {
            ((ImageView) findViewById(R.id.bkg_view)).setImageBitmap(App.sApp.getExternalBitmap(this.mBkgImg));
            return;
        }
        ((ImageView) findViewById(R.id.bkg_view)).setImageBitmap(App.sApp.getExternalBitmap(R.drawable.empty_image));
        App.sApp.releaseExternalBitmap(this.mBkgImg);
    }
}

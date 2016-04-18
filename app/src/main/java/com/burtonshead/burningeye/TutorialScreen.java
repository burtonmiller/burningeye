package com.burtonshead.burningeye;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.apache.commons.lang.time.FastDateFormat;

public class TutorialScreen extends Activity {
    private TextView mBackButton;
    private Gallery mGallery;
    private OnClickListener mNavListener;

    /* renamed from: com.burtonshead.burningeye.TutorialScreen.1 */
    class C00331 implements OnClickListener {
        C00331() {
        }

        public void onClick(View v) {
            int pos = TutorialScreen.this.mGallery.getSelectedItemPosition();
            if (pos < TutorialScreen.this.mGallery.getAdapter().getCount() - 1) {
                TutorialScreen.this.mGallery.setSelection(pos + 1, true);
            }
        }
    }

    /* renamed from: com.burtonshead.burningeye.TutorialScreen.2 */
    class C00342 implements OnClickListener {
        C00342() {
        }

        public void onClick(View v) {
            TutorialScreen.this.startActivity(new Intent(TutorialScreen.this.getApplicationContext(), MainScreen.class));
        }
    }

    class TutorialAdapter extends BaseAdapter {
        private Context mContext;
        int mGalleryItemBackground;

        public TutorialAdapter(Context c) {
            this.mContext = c;
        }

        public int getCount() {
            return 4;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout view;
            if (convertView == null || !(convertView instanceof RelativeLayout)) {
                view = (LinearLayout) ((LayoutInflater) TutorialScreen.this.getSystemService("layout_inflater")).inflate(R.layout.tutorial_page, parent, false);
                view.setPadding(4, 4, 4, 4);
                ((TextView) view.findViewById(R.id.tutorial_instr)).setTypeface(Typeface.createFromAsset(TutorialScreen.this.getAssets(), "fonts/white_rabbit.ttf"));
                ((ImageView) view.findViewById(R.id.tutorial_progress)).setOnClickListener(TutorialScreen.this.mNavListener);
            } else {
                view = (LinearLayout) convertView;
            }
            ImageView top = (ImageView) view.findViewById(R.id.tutorial_img);
            TextView instr = (TextView) view.findViewById(R.id.tutorial_instr);
            ImageView bottom = (ImageView) view.findViewById(R.id.tutorial_progress);
            switch (position) {
                case FastDateFormat.FULL /*0*/:
                    top.setImageResource(R.drawable.tutorial_1);
                    instr.setText(TutorialScreen.this.getResources().getString(R.string.tutorial_text_1));
                    bottom.setImageResource(R.drawable.tutorial_bottom_1);
                    break;
                case FastDateFormat.LONG /*1*/:
                    top.setImageResource(R.drawable.tutorial_2);
                    instr.setText(TutorialScreen.this.getResources().getString(R.string.tutorial_text_2));
                    bottom.setImageResource(R.drawable.tutorial_bottom_2);
                    break;
                case FastDateFormat.MEDIUM /*2*/:
                    top.setImageResource(R.drawable.tutorial_3);
                    instr.setText(TutorialScreen.this.getResources().getString(R.string.tutorial_text_3));
                    bottom.setImageResource(R.drawable.tutorial_bottom_3);
                    break;
                case FastDateFormat.SHORT /*3*/:
                    top.setImageResource(R.drawable.tutorial_4);
                    instr.setText(TutorialScreen.this.getResources().getString(R.string.tutorial_text_4));
                    bottom.setImageResource(R.drawable.tutorial_bottom_4);
                    break;
            }
            return view;
        }
    }

    public TutorialScreen() {
        this.mNavListener = new C00331();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_screen);
        this.mBackButton = (TextView) findViewById(R.id.tutorial_back_button);
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf"));
        this.mBackButton.setOnClickListener(new C00342());
        this.mGallery = (Gallery) findViewById(R.id.tutorial_gallery);
        this.mGallery.setAdapter(new TutorialAdapter(this));
    }

    public void onPause() {
        App.sApp.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        App.sApp.playBkgMusic();
    }
}

package com.burtonshead.burningeye;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.burtonshead.burningeye.logic.HighScore;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;

public class ScoreScreen extends GameActivity {
    public static final String NEW_HIGH_SCORE_EXTRA = "new_hs_extra";
    private TextView mBackButton;
    private float mDensity;
    private TableLayout mScoreTable;

    /* renamed from: com.burtonshead.burningeye.ScoreScreen.1 */
    class C00301 implements OnClickListener {
        C00301() {
        }

        public void onClick(View v) {
            ScoreScreen.this.startActivity(new Intent(ScoreScreen.this.getApplicationContext(), MainScreen.class));
        }
    }

    /* renamed from: com.burtonshead.burningeye.ScoreScreen.2 */
    class C00312 implements DialogInterface.OnClickListener {
        private final /* synthetic */ HighScore val$hs;
        private final /* synthetic */ EditText val$textEntryView;

        C00312(HighScore highScore, EditText editText) {
            this.val$hs = highScore;
            this.val$textEntryView = editText;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            this.val$hs.name = this.val$textEntryView.getText().toString();
            App app = App.mInstance;
            App.getSettings().addHighScore(this.val$hs);
            ScoreScreen.this.load();
        }
    }

    public ScoreScreen() {
        this.mDensity = 1.0f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_screen);
        init(R.drawable.score_bkg);
        this.mBackButton = (TextView) findViewById(R.id.score_back_button);
        this.mBackButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/add_city_electric.ttf"));
        this.mBackButton.setOnClickListener(new C00301());
        this.mScoreTable = (TableLayout) findViewById(R.id.score_table);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.mDensity = metrics.density;
    }

    public void onResume() {
        String str = NEW_HIGH_SCORE_EXTRA;
        super.onResume();
        showBkgImage(true);
        App.mInstance.playBkgMusic();
        load();
        Intent intent = getIntent();
        String str2 = NEW_HIGH_SCORE_EXTRA;
        long newScore = intent.getLongExtra(str, -1);
        intent = getIntent();
        str2 = NEW_HIGH_SCORE_EXTRA;
        intent.removeExtra(str);
        if (newScore != -1) {
            processNewHighScore(newScore);
        }
    }

    public void onNewIntent(Intent intent) {
        String str = NEW_HIGH_SCORE_EXTRA;
        super.onNewIntent(intent);
        String str2 = NEW_HIGH_SCORE_EXTRA;
        long score = intent.getLongExtra(str, -1);
        if (score != -1) {
            Intent intent2 = getIntent();
            String str3 = NEW_HIGH_SCORE_EXTRA;
            intent2.removeExtra(str);
            intent2 = getIntent();
            str3 = NEW_HIGH_SCORE_EXTRA;
            intent2.putExtra(str, score);
        }
    }

    public void onPause() {
        showBkgImage(false);
        App.mInstance.pauseBkgMusic();
        System.gc();
        super.onPause();
    }

    private void load() {
        this.mScoreTable.removeAllViews();
        App app = App.mInstance;
        Vector<HighScore> highScores = App.getSettings().getHighScores();
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/white_rabbit.ttf");
        TableRow r = new TableRow(this);
        TextView nameHeader = new TextView(this);
        nameHeader.setLineSpacing(5.0f, 1.0f);
        nameHeader.setText("NAME");
        TextView scoreHeader = new TextView(this);
        scoreHeader.setText("SCORE");
        scoreHeader.setLineSpacing(5.0f, 1.0f);
        nameHeader.setTextAppearance(this, R.style.score_style);
        scoreHeader.setTextAppearance(this, R.style.score_style);
        nameHeader.setTextColor(getResources().getColor(R.color.control_text_sel));
        scoreHeader.setTextColor(getResources().getColor(R.color.control_text_sel));
        nameHeader.setTypeface(font);
        scoreHeader.setTypeface(font);
        r.addView(nameHeader);
        r.addView(scoreHeader);
        this.mScoreTable.addView(r);
        if (highScores != null) {
            Iterator it = highScores.iterator();
            while (it.hasNext()) {
                HighScore s = (HighScore) it.next();
                r = new TableRow(this);
                TextView name = new TextView(this);
                name.setText(s.name);
                TextView score = new TextView(this);
                score.setText("" + s.score);
                name.setTextAppearance(this, R.style.score_style);
                score.setTextAppearance(this, R.style.score_style);
                name.setTextColor(getResources().getColor(R.color.control_text));
                score.setTextColor(getResources().getColor(R.color.control_text));
                name.setTypeface(font);
                name.setLineSpacing(5.0f, 1.0f);
                score.setTypeface(font);
                score.setLineSpacing(5.0f, 1.0f);
                r.addView(name);
                r.addView(score);
                this.mScoreTable.addView(r);
            }
        }
    }

    private void processNewHighScore(long score) {
        EditText textEntryView = (EditText) LayoutInflater.from(this).inflate(R.layout.edit_text, null);
        new Builder(this).setTitle("New High Score!").setView(textEntryView).setPositiveButton("Done", new C00312(new HighScore(score, StringUtils.EMPTY), textEntryView)).show();
    }
}

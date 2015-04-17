package org.roeg.sawroeg;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ChallengeActivity extends Activity {
	
	static SQLiteDatabase db;
	static Cursor c;
	static TextView textView1;
	static Button button1;
	static int refreshCounter = 0;
	static String ans = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_challenge);
		
		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setOnClickListener(new OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
              refreshContent();
          }
      });
		
		button1 = (Button) findViewById(R.id.button1);
		button1.setText("Yienjok");
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContent();
			}
		});
		
		db = MainActivity.db;
		refreshContent();
		
	}
	
	public static void refreshContent() {
		String key = "";
		c = db.rawQuery("SELECT * FROM favs ORDER BY RANDOM() LIMIT 1", null);
		
		if(refreshCounter % 2 != 0) {
			textView1.setText(ans);
			button1.setText("Aen Laeng");
		}
		else {
			if(c.moveToNext() && c != null) {
				ans = c.getString(c.getColumnIndex("item"));
				key = ans.split(" ", 2)[0];
			}
			textView1.setText(key);
			button1.setText("Yienjok");
		}
		refreshCounter++;
	}
}

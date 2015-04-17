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
	static Button button1, button2, button3;
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
              refreshContent("yienjok");
          }
      });
		
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContent("ndirox");
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContent("roxdi");
			}
		});
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContent("roxdoh");
			}
		});
		
		db = MainActivity.db;
		refreshContent("begin");
		
	}
	
	public static void refreshContent(String command) {
		String key = "";
		if(command == "roxdoh" || command == "begin") {
			c = db.rawQuery("SELECT * FROM favs ORDER BY RANDOM() LIMIT 1", null);
			if(c.moveToNext() && c != null) {
				ans = c.getString(c.getColumnIndex("item"));
				key = ans.split(" ", 2)[0];
				textView1.setText(key);
			}
		}
		else if(command == "roxdi") {
			textView1.setText(ans);
		}
		else if(command == "ndirox") {
			textView1.setText(ans);
		}
		else if(command == "yienjok") {
			textView1.setText(ans);
		}
	}
}

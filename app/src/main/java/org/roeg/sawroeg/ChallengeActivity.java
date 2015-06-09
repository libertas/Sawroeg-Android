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
	static String ans = "", key = "";
	static int refreshState = 0;
	// 0 means the keyword is being shown
	// 1 means the entire entry (or + warningMsg2)
	// 2 means the entire entry + warningMsg1

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
		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshContent("roxdoh");
			}
		});
		
		db = MainActivity.datadb;
		refreshContent("begin");
		
	}

	// ATTENTION: This function is bad designed and need to be rewritten
	public static void refreshContent(String command) {
		String warningMsg1 = "\n\n(Naenz dieg neix bae yawj diuz laneg)";
		String warningMsg2 = "\n\n(Youq laj neix genj aen ndeu la)";
		if(command == "roxdoh" || command == "begin") {
			c = db.rawQuery("SELECT * FROM favs ORDER BY RANDOM() LIMIT 1", null);
			if(c.moveToNext() && c != null) {
				ans = c.getString(c.getColumnIndex("item"));
				key = ans.split(" ", 2)[0];
				textView1.setText(key);
				refreshState = 0;
			}
			else {
				textView1.setText("Ndi miz diuzmoeg hoj.");
			}
			if(command == "roxdoh") {
				db.execSQL("UPDATE favs set data=data+2 where item=\"" + ans + "\"");
			}
		}
		else if(command == "roxdi") {
			if(ans != "") {
				if(refreshState == 0) {
					textView1.append(ans + warningMsg1);
					refreshState = 2;
				}
				else if(refreshState == 1) {
					refreshContent("begin");
				}
			}
		}
		else if(command == "ndirox") {
			if(ans != "") {
				if(refreshState == 0) {
					textView1.append(ans + warningMsg1);
					refreshState = 2;
				}
				else if(refreshState == 1) {
					refreshContent("begin");
				}
			}
		}
		else if(command == "yienjok") {
			if(ans != "") {
				if(refreshState == 0) {
					textView1.setText(ans);
					refreshState = 1;
				}
				else if(refreshState == 1) {
					textView1.setText(ans + warningMsg2);
				}
				else if(refreshState == 2) {
					refreshContent("begin");
				}
			}
		}
	}
}

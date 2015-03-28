package org.roeg.sawroeg;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class VocabularyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vocabulary);
		ListView list = (ListView) findViewById(R.id.listView1);
		ArrayList<String> items = new ArrayList<String>();
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				items);
		list.setAdapter(aa);
		SQLiteDatabase db = MainActivity.db;
		Cursor c = db.rawQuery("SELECT * FROM favs", null);
		while(c.moveToNext() && c != null) {
			String i = c.getString(c.getColumnIndex("item"));
			items.add(i);
		}
		aa.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vocabulary, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

package org.roeg.sawroeg;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private ArrayAdapter<String> itemsArray;
	private ArrayList<String> items;

	private ArrayAdapter<String> historyArray;
	private ArrayList<String> historyStrings;


	public static SQLiteDatabase db;
	public static SQLiteDatabase datadb;
	private ListView list;

	AutoCompleteTextView text;

	private void newSearch(String keyword) {
		SharedPreferences settings = getSharedPreferences("org.roeg.sawroeg_preferences", MODE_PRIVATE);
		String length_s = settings.getString("length_edit", "30");
		int limit_length;
		try {
			limit_length = Integer.valueOf(length_s).intValue();
		} catch (Exception e) {
			limit_length = 30;
		}
		Iterator<String> result = Dict.search(keyword, db, limit_length);
		items.clear();
		while (result.hasNext()) {
			String[] tmp = result.next().split(" ", 2);
			String key = tmp[0];
			String value = tmp[1];

			items.add(key + "\n" + value);
		}
		itemsArray.notifyDataSetChanged();
	}

	private void checkDatabase(String dbname) {
		try {
			FileOutputStream out = new FileOutputStream("data/data/org.roeg.sawroeg/databases/" + dbname);
			InputStream in = getResources().getAssets().open(dbname);
			byte[] buffer = new byte[1024];
			int readBytes = 0;
			while ((readBytes = in.read(buffer)) != -1)
				out.write(buffer, 0, readBytes);
			in.close();
			out.close();
		} catch (Exception e1) {
			throw new Error("Unable to create database");
		} finally {
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(MainActivity.this, "Angqcoux Ma Yungh Sawroeg~", Toast.LENGTH_SHORT).show();


		//Copy the database
		db = openOrCreateDatabase(":memory:", MODE_PRIVATE, null);
		datadb = openOrCreateDatabase("data.db", MODE_PRIVATE, null);
		datadb.execSQL("CREATE TABLE IF NOT EXISTS favs (item, data)");
		datadb.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_item ON favs (item)");

		checkDatabase("sawguq.db");
		checkDatabase("newdict.db");

		db.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/newdict.db' AS 'new';");
		db.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/sawguq.db' AS 'old';");
		db.execSQL("CREATE TEMP VIEW sawguq AS SELECT * FROM old.sawguq UNION SELECT * FROM new.sawguq;");


		//Create the UI
		list = (ListView) findViewById(R.id.listView);

		text = (AutoCompleteTextView) findViewById(R.id.editText);
		historyStrings = new ArrayList<String>();
		ArrayList<String> allKeys = Dict.getAll(db);

		Collections.sort(allKeys, new Comparator<String>() {
			@Override
			public int compare(String s, String t1) {
				return s.compareTo(t1);
			}
		});

		historyStrings.addAll(allKeys);
		historyArray = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				historyStrings);
		text.setAdapter(historyArray);

		items = new ArrayList<String>();
		itemsArray = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				items);
		list.setAdapter(itemsArray);
		text.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						text.dismissDropDown();

						String keyword = text.getText().toString();

						newSearch(keyword);
						list.setSelection(0);
						return true;
					}
				return false;
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
				String i = (String) items.get(arg2);
				ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setText(i);
				Toast.makeText(MainActivity.this, "Fukceih diuzmoeg \"" + i + "\"", Toast.LENGTH_SHORT).show();
			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int location, long arg3) {
				String fav_item = (String) items.get(location);
				datadb.execSQL("INSERT OR IGNORE INTO favs VALUES (?, 0)", new Object[]{fav_item});
				Toast.makeText(MainActivity.this, "Gya \"" + fav_item.split(" ", 2)[0] +
						"\" haeuj diuzmoeg hoj bae liux", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		final Button ebutton = (Button) findViewById(R.id.buttonSearch);
		ebutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				text.dismissDropDown();
				
				String keyword = text.getText().toString();

				newSearch(keyword);
				list.setSelection(0);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.diuzmoeg_hoj) {
			Intent intent = new Intent(this, VocabularyActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.diujcienq) {
			Intent intent = new Intent(this, ChallengeActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}

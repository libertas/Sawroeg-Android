package org.roeg.sawroeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.roeg.cytokenizer.BouyeiTokenizer;
import org.roeg.cytokenizer.CuenghTokenizer;


public class MainActivity extends AppCompatActivity {

	private Dict cuenghDict;
	private Dict bouyeiDict;
	private Dict cuenghEngDict;
	private Dict dict;

	private ArrayAdapter<String> itemsArray;
	private ArrayList<String> items;

	private List<String> historyStrings;
	private ArrayAdapter<String> historyArray;


	public static SQLiteDatabase cuenghDb;
	public static SQLiteDatabase bouyeiDb;
	public static SQLiteDatabase cuenghEngDb;
	public static SQLiteDatabase datadb;
	private ListView list;

	private AutoCompleteTextView text;

	private void newSearch(String keyword) {
		keyword = keyword.trim();

		SharedPreferences settings = getSharedPreferences("org.roeg.sawroeg_preferences", MODE_PRIVATE);
		String length_s = settings.getString("length_edit", "500");
		int limit_length;
		try {
			limit_length = Integer.valueOf(length_s).intValue();
		} catch (Exception e) {
			limit_length = 500;
		}

		Iterator<String> result = dict.search(keyword, limit_length);

		items.clear();

		while (result.hasNext()) {
			String[] tmp = result.next().split(" ", 2);
			String key = tmp[0];
			String value = tmp[1];

			items.add(key + "\n" + value);
		}

		itemsArray.notifyDataSetChanged();
	}

	private boolean checkDatabase(String dbname) {
		try {
			InputStream in = getResources().getAssets().open(dbname);
			OutputStream out = new FileOutputStream("data/data/org.roeg.sawroeg/databases/"
					+ dbname);
			DBHelper.copyFile(in, out);
		} catch (Exception e1) {
			System.out.println("Unable to create database");
			return false;
		} finally {
			return true;
		}
	}

	private void copyToClip(String data) {
		ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData cd = ClipData.newPlainText("Sawroeg", data);
		cm.setPrimaryClip(cd);
		Toast.makeText(MainActivity.this, "Guhmoq diuzmoeg \"" + data + "\"", Toast.LENGTH_SHORT).show();
	}

	private void addToFav(String data) {
		datadb.execSQL("INSERT OR IGNORE INTO favs VALUES (?, 0)", new Object[]{data});
		Toast.makeText(MainActivity.this, "Coq \"" + data.split(" ", 2)[0] +
				"\" haeuj diuzmoeg hoj bae liux", Toast.LENGTH_SHORT).show();
	}

	private void makePopMenu(View view, final String data) {

		PopupMenu popup = new PopupMenu(this, view);

		MenuInflater inflater = popup.getMenuInflater();

		inflater.inflate(R.menu.popmenu, popup.getMenu());

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();
				switch(id) {
					case R.id.action_copy:
						copyToClip(data);
						break;
					case R.id.coqhaeuj_diuzmoeg_hoj:
						addToFav(data);
						break;
				}

				return false;
			}
		});

		popup.show();
	}

	protected SQLiteDatabase getDatabase(String dbname_old, String dbname_new) {
		String dbname_new_net = dbname_new + ".net";

		SQLiteDatabase database = openOrCreateDatabase(":memory:", MODE_PRIVATE, null);

		checkDatabase(dbname_old);
		checkDatabase(dbname_new);

		database.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/" + dbname_old + "' AS 'old';");

		File newdict_net = new File("data/data/org.roeg.sawroeg/databases/" + dbname_new_net);
		File newdict = new File("data/data/org.roeg.sawroeg/databases/" + dbname_new);
		if(newdict_net.exists() && newdict.exists()) {
			SQLiteDatabase newdb_net = openOrCreateDatabase(dbname_new_net, MODE_PRIVATE, null);
			SQLiteDatabase newdb = openOrCreateDatabase(dbname_new, MODE_PRIVATE, null);

			int version_net = DBHelper.getDBVersion(newdb_net);
			int version = DBHelper.getDBVersion(newdb);

			if(version < version_net) {
				database.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/" + dbname_new_net + "' AS 'new';");
			} else {
				database.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/" + dbname_new + "' AS 'new';");
			}

			database.execSQL("CREATE TEMP VIEW sawguq AS SELECT * FROM new.sawguq UNION ALL SELECT * FROM old.sawguq;");
		} else if(newdict_net.exists()) {
			database.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/" + dbname_new_net + "' AS 'new';");
			database.execSQL("CREATE TEMP VIEW sawguq AS SELECT * FROM new.sawguq UNION ALL SELECT * FROM old.sawguq;");
		} else if(newdict.exists()) {
			database.execSQL("ATTACH DATABASE 'data/data/org.roeg.sawroeg/databases/" + dbname_new + "' AS 'new';");
			database.execSQL("CREATE TEMP VIEW sawguq AS SELECT * FROM new.sawguq UNION ALL SELECT * FROM old.sawguq;");
		} else {
			database.execSQL("CREATE TEMP VIEW sawguq AS SELECT * FROM old.sawguq;");
		}

		return database;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(MainActivity.this, "Angqcoux Ma Yungh Sawroeg~", Toast.LENGTH_SHORT).show();

		//Check dict update
		DictUpdateIntentService.startActionDictUpdate(this.getApplicationContext(), "newdict.db", "version.txt");
		DictUpdateIntentService.startActionDictUpdate(this.getApplicationContext(), "newdict_bouyei.db", "version_bouyei.txt");
		DictUpdateIntentService.startActionDictUpdate(this.getApplicationContext(), "newdict_cuengh_eng.db", "version_cuengh_eng.txt");


		//Copy the database
		cuenghDb = getDatabase("sawguq.db", "newdict.db");
		bouyeiDb = getDatabase("selgus.db", "newdict_bouyei.db");
		cuenghEngDb = getDatabase("cuengh_eng.db", "newdict_cuengh_eng.db");

		datadb = openOrCreateDatabase("data.db", MODE_PRIVATE, null);
		datadb.execSQL("CREATE TABLE IF NOT EXISTS favs (item, data)");
		datadb.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_item ON favs (item)");

		// Create the Dict object
		cuenghDict = new LatinChineseDict(cuenghDb, new CuenghTokenizer(), "Loeng: Ra Mbouj Ok Saek Yiengh");
		bouyeiDict = new LatinChineseDict(bouyeiDb, new BouyeiTokenizer(), "Longl: Ral Miz Os Sagt Yiangh");
		cuenghEngDict = new LatinChineseDict(cuenghEngDb, new CuenghTokenizer(), "Loeng: Ra Mbouj Ok Saek Yiengh");
		dict = cuenghDict;

		//Create the UI
		final Spinner spinner = (Spinner) findViewById(R.id.spnLanguage);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					default:
					case 0:
						dict = cuenghDict;
						break;
					case 1:
						dict = bouyeiDict;
						break;
					case 2:
						dict = cuenghEngDict;
						break;
				}
				historyStrings = new ArrayList<String>();  // the default list for the adapter
				historyStrings.addAll(dict.getAll());
				historyArray.clear();
				historyArray.addAll(historyStrings);
				historyArray.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				dict = cuenghDict;
			}
		});

		list = (ListView) findViewById(R.id.listView);

		List<String> allKeys = dict.getAll();
		historyStrings = new ArrayList<String>();  // the default list for the adapter
		historyStrings.addAll(allKeys);
		historyArray = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				historyStrings);
		text = (AutoCompleteTextView) findViewById(R.id.editText);
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
				String data = items.get(arg2).toString();

				makePopMenu(arg1, data);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(event.getRepeatCount() == 2 || text.getText().toString().equals("")) {
				moveTaskToBack(false);
				return true;
			}

			text.setText("");

			return true;
		}
		return super.onKeyDown(keyCode, event);
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

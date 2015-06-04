package org.roeg.sawroeg;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private ArrayAdapter<String> aa;
    private ArrayList<String> items;
    public static SQLiteDatabase db;
    private ListView list;
    
    private void newSearch(String keyword) {
    	SharedPreferences settings = getSharedPreferences("org.roeg.sawroeg_preferences", MODE_PRIVATE);
		String length_s = settings.getString("length_edit", "30");
		int limit_length = Integer.valueOf(length_s).intValue();
    	Iterator<String> result = Dict.search(keyword, db, limit_length);
		items.clear();
		int count = 1;
		while(result.hasNext())  {
			items.add(String.valueOf(count) + "." + (String) result.next());
			count++;
		}
		aa.notifyDataSetChanged();
    }
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toast.makeText(MainActivity.this, "Anqcoux Ma Yungh Sawroeg~", Toast.LENGTH_SHORT).show();
		
		//Create the UI
		list = (ListView) findViewById(R.id.listView1);
		
		final EditText text = (EditText) findViewById(R.id.editText1);
		items = new ArrayList<String>();
		aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				items);
		list.setAdapter(aa);
		text.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN)
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						String keyword = text.getText().toString();
						newSearch(keyword);
						list.setSelection(0);
						return true;
					}
				return false;
			}
		});

		list.setOnItemClickListener(new OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView arg0, View arg1, int arg2,long arg3)
          {
              String i = (String)items.get(arg2);
              ClipboardManager cm =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
              i = i.split(".", 2)[1].substring(1);
              cm.setText(i);
              Toast.makeText(MainActivity.this, "Fukceih diuzmoeg \"" + i + "\"", Toast.LENGTH_SHORT).show();
          }
      });

		list.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int location, long arg3) {
				String fav_item = ((String) items.get(location)).split(".", 2)[1].substring(1);
				db.execSQL("INSERT OR IGNORE INTO favs VALUES (?)", new Object[]{fav_item});
				Toast.makeText(MainActivity.this, "Gya \"" + fav_item.split(" ", 2)[0] +
						"\" haeuj diuzmoeg hoj bae liux", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		final Button ebutton = (Button) findViewById(R.id.button1);
		ebutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String keyword = text.getText().toString();
				newSearch(keyword);
				list.setSelection(0);
			}
		});
		
		//Check the database,it might takes very long time
		db = openOrCreateDatabase("sawguq.db", MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS favs (item)");
		db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_item ON favs (item)");
		try {
			Cursor c = db.rawQuery("SELECT * FROM sawguq", null);
		}
		catch(Exception e) {
			final ProgressDialog dialog = ProgressDialog.show(this, "Ancang", "Baez neix dwg baez daih'it mwngz yungh,"
					+ "aen ancang neix yaek yungh bae geij faencung.", true);
			final Handler handler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					dialog.dismiss();
				}
			};
			try {
				DbHelper myDbHelper;
			    myDbHelper = new DbHelper(this);
			    myDbHelper.createDataBase();
			}
			catch(Exception e1) {
				throw new Error("Unable to create database");
				/*new Thread() {
					public void run() {
						CreateDb.create(db);
						handler.sendEmptyMessage(0);
					}
				}.start();*/
			}
			finally {
				handler.sendEmptyMessage(0);
			}
		}
		finally {
			
		}
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
}
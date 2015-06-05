package org.roeg.sawroeg;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class VocabularyActivity extends Activity {
	
	SQLiteDatabase db;
	ArrayList<String> items;
	ArrayAdapter<String> aa;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vocabulary);
		db = MainActivity.datadb;
		items = new ArrayList<String>();
		aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				items);
		ListView list = (ListView) findViewById(R.id.listView1);
		list.setAdapter(aa);
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM favs", null);

			while(c.moveToNext() && c != null) {
				String i = c.getString(c.getColumnIndex("item"));
				items.add(i);
			}
		}
		catch(Exception e){

		}
		finally {
			if (c != null)
				c.close();
			aa.notifyDataSetChanged();
		}

		list.setOnItemClickListener(new OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView arg0, View arg1, int arg2,long arg3)
          {
        	  String i = (String)items.get(arg2);
              ClipboardManager cm =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
              cm.setText(i);
              Toast.makeText(VocabularyActivity.this, "Fukceih \"" + i + "\"", Toast.LENGTH_SHORT).show();
          }
      });
		
		list.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View view, final int location, long arg3) {
				String i = (String)items.get(location);
	            aa.remove(i);
	            db.execSQL("DELETE FROM favs WHERE item IS \"%s\"".replace("%s", i));
	            aa.notifyDataSetChanged();
	            return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.vocabulary, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		//int id = item.getItemId();
		//if (id == R.id.action_settings) {
		//	return true;
		//}
		//return super.onOptionsItemSelected(item);
		return true;
	}
}

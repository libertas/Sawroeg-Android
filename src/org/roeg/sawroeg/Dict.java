package org.roeg.sawroeg;

import java.util.ArrayList;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dict {
	public static Iterator<String> search(String keyword, SQLiteDatabase db) {
		ArrayList<String> result = new ArrayList<String>();
		Cursor c = db.rawQuery("SELECT * FROM sawguq WHERE key like \"%%$s%%\"".replace("$s", keyword), null);
		String i, j;
		while(c.moveToNext()) {
			i = c.getString(c.getColumnIndex("key"));
			j = c.getString(c.getColumnIndex("value"));
        	result.add(i + " " + j);
		}
		return result.iterator();
	}
}

package org.roeg.sawroeg;

import android.database.sqlite.SQLiteDatabase;


public class CreateDb {
	public static int create(SQLiteDatabase db) {
		String[] DictL = DictString.DictStr.split("\n");
		String[] tmp;
		db.execSQL("CREATE TABLE sawguq (key, value)"); 
		for(String i:DictL) {
			tmp = i.split(" ", 2);
			db.execSQL("INSERT INTO sawguq VALUES (?, ?)", new Object[]{tmp[0], tmp[1]});
		}
		return 0;
	}
}

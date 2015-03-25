package org.roeg.sawroeg;

import java.util.ArrayList;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class Dict {
	
	private static boolean isCharChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
        		|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
        		|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
        		|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
        		|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
        		|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
        	return true;
        	}
        return false;
    }
	private static boolean isStringChinese(String s) {
		boolean flag = false;
		for(char i:s.toCharArray()) {
			if(isCharChinese(i))
				flag = true;
		}
		return flag;
	}
	
	public static Iterator<String> search(String keyword, SQLiteDatabase db) {
		ArrayList<String> result = new ArrayList<String>();
		Cursor c;
		if(isStringChinese(keyword))
			c = db.rawQuery("SELECT * FROM sawguq WHERE value like \"%%$s%%\"".replace("$s", keyword), null);
		else
			c = db.rawQuery("SELECT * FROM sawguq WHERE key like \"%%$s%%\"".replace("$s", keyword), null);
		String i, j;
		while(c.moveToNext()) {
			i = c.getString(c.getColumnIndex("key"));
			j = c.getString(c.getColumnIndex("value"));
        	result.add(i + " " + j);
		}
		return result.iterator();
	}
}

package org.roeg.sawroeg;

import java.util.ArrayList;
import java.util.Iterator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



public class Dict {
	
	private static int min(ArrayList a) {
		int tmp = (Integer) a.get(0);
		for(int i = 0; i < a.size(); i++) {
			if((Integer) a.get(i) < tmp)
				tmp = (Integer) a.get(i);
		}
		return tmp;
	}
	
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
	
	public static Iterator<String> search(String keyword, SQLiteDatabase db, int limit_length) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> result1 = new ArrayList<String>();
		ArrayList distances = new ArrayList();
		Cursor c = null;
		if(keyword.length() < 1)
		{
			result.add("Ndi miz");
			return result.iterator();
		}
		try {
			boolean issc = isStringChinese(keyword);
			keyword = keyword.replace(" ", " ");  // replace space with "\xa0"
			if(issc)
				c = db.rawQuery("SELECT * FROM sawguq WHERE value like \"%%$s%%\"".replace("$s", keyword), null);
			else
				c = db.rawQuery("SELECT * FROM sawguq WHERE key like \"%%$s%%\"".replace("$s", keyword), null);
			String i, j;
			int distance;
			while(c.moveToNext()) {
				i = c.getString(c.getColumnIndex("key"));
				j = c.getString(c.getColumnIndex("value"));
				result1.add(j);
				if(issc)
					distance = Levenshtein.distance(j, keyword);
				else
					distance = Levenshtein.distance(i, keyword);
				distances.add(distance);
			}
		}
		catch(Exception e){

		}
		finally {
			if(c != null)
				c.close();
		}
		int m, index, count = 0;
		while(distances.size() != 0 && count != limit_length) {
			m = min(distances);
			index = distances.indexOf(m);
			result.add(result1.get(index));
			result1.remove(index);
			distances.remove(index);
			count++;
		}
		return result.iterator();
	}

	public static ArrayList<String> getAll(SQLiteDatabase db) {
		ArrayList<String> result = new ArrayList<String>();
		Cursor c = null;

		try {
			c = db.rawQuery("SELECT distinct(key) FROM sawguq", null);

			while(c.moveToNext()) {
				// get and key words and replace "\xa0" with space
				result.add(c.getString(c.getColumnIndex("key")).replace(" ", " "));
			}

		} catch (Exception e) {

		} finally {
			if(c != null)
				c.close();
		}

		return result;
	}
}

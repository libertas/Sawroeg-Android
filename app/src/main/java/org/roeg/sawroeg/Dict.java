package org.roeg.sawroeg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import org.roeg.cytokenizer.CYTokenizer;
import org.roeg.cytokenizer.CuenghTokenizer;
import org.roeg.cytokenizer.Word;


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
		for(char i:s.toCharArray()) {
			if(isCharChinese(i))
				return true;
		}
		return false;
	}

	private static int wordsSimilarity(List<Word> a, List<Word> b) {
		int result[] = new int[a.size() + b.size() - 1];

		int results[][] = new int[a.size()][b.size()];

		for(int i = 0; i < a.size(); i++) {
			Word wa = a.get(i);
			String sa = wa.toString();

			for(int j = 0; j < b.size(); j++) {
				Word wb = b.get(j);

				String sb = wb.toString();

				if(sa.equals(sb)) {
					results[i][j] = 0;
				} else if(wa.getMehsing().equals(wb.getMehsing())
						&& wa.getMehyinh().equals(wb.getMehyinh())) {
					results[i][j] = 1;
				} else {
					results[i][j] = 3;
				}
			}
		}

		for (int n = 0; n < b.size(); n++) {
			for(int i = 0, j = n ; i < a.size() && j < b.size(); i++, j++) {
				result[n] += results[i][j];
			}
		}

		for(int n = 0; n < a.size() - 1; n++) {
			for(int i = n + 1, j = 0; i < a.size() && j < b.size(); i++, j++) {
				result[b.size() + n] += results[i][j];
			}
		}

		int ans = Integer.MAX_VALUE;

		for(int i = 0; i < result.length; i++) {
			if(result[i] < ans) {
				ans = result[i];
			}
		}

		return ans;
	}
	
	public static Iterator<String> search(String keyword, SQLiteDatabase db, int limit_length) {
		keyword = keyword.toLowerCase();

		CYTokenizer tokenizer = new CuenghTokenizer();

		List<Word> keys = null;

		ArrayList<String> result = new ArrayList<>();
		ArrayList<String> result1 = new ArrayList<>();
		ArrayList<Integer> distances = new ArrayList<>();
		ArrayList<Integer> wordDistances = new ArrayList<>();

		boolean issc = isStringChinese(keyword);

		Cursor c = null;
		if(keyword.length() < 1)
		{
			result.add("\nNdi miz");
			return result.iterator();
		}

		try {
			keyword = keyword.replace(" ", " ");  // replace space with "\xa0"
			if(issc)
				c = db.rawQuery("SELECT * FROM sawguq WHERE value like \"%%$s%%\"".replace("$s", keyword), null);
			else
				c = db.rawQuery("SELECT * FROM sawguq WHERE key like \"%%$s%%\"".replace("$s", keyword), null);

			String i, j;
			int distance;

			if (!issc) {
				keys = tokenizer.tokenize(keyword);

				for(Word word: keys) {
					System.out.println(word);
				}
			}

			while(c.moveToNext()) {

				i = c.getString(c.getColumnIndex("key"));
				j = c.getString(c.getColumnIndex("value"));

				result1.add(j);

				if(issc) {
                    distance = Levenshtein.distance(j, keyword);
                } else {
                    distance = Levenshtein.distance(i, keyword);

					List<Word> values = tokenizer.tokenize(i);

					int similarity = wordsSimilarity(keys, values);

					wordDistances.add(similarity);

//					System.out.println("Words:\t" + keys.toString() + "\t" + values.toString() +
//							"\t" + String.valueOf(similarity));
                }

				distances.add(distance);
			}

		}
		catch(Exception e){

		}
		finally {
			if(c != null)
				c.close();
		}

		if(issc) {
			int m, index, count = 0;
			while(distances.size() != 0 && count++ < limit_length) {
				m = min(distances);
				index = distances.indexOf(m);
				result.add(result1.get(index));
				result1.remove(index);
				distances.remove(index);
				count++;
			}
		} else {
			ArrayList<Pair<String, Pair<Integer, Integer>>> pair = new ArrayList<>();
			for(int i = 0; i < result1.size(); i++) {
				pair.add(new Pair<String, Pair<Integer, Integer>>(result1.get(i),
						new Pair<Integer, Integer>(wordDistances.get(i), distances.get(i))));
			}

			pair.sort(new Comparator<Pair<String, Pair<Integer, Integer>>>() {
				@Override
				public int compare(Pair<String, Pair<Integer, Integer>> o1,
								   Pair<String, Pair<Integer, Integer>> o2) {
					if(o1.second.first == o2.second.first) {
						return o1.second.second - o2.second.second;
					} else {
						return o1.second.first - o2.second.first;
					}
				}
			});

			for(int i = 0; i < pair.size() && i < limit_length; i++) {
				result.add(pair.get(i).first);
			}
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

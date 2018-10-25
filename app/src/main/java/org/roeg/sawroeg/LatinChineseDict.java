package org.roeg.sawroeg;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import org.roeg.cytokenizer.CYTokenizer;
import org.roeg.cytokenizer.Word;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LatinChineseDict extends Dict{
    protected SQLiteDatabase db;
    protected CYTokenizer tokenizer;
    protected String errMsg;

    LatinChineseDict(SQLiteDatabase db, CYTokenizer tokenizer, String errMsg) {
        this.db = db;
        this.tokenizer = tokenizer;
        this.errMsg = errMsg;
    }

    @Override
    public Iterator<String> search(String keyword, int limit_length) {
        keyword = keyword.toLowerCase();

        List<Word> keys = null;

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> result1 = new ArrayList<>();
        ArrayList<Integer> distances = new ArrayList<>();
        ArrayList<Integer> wordDistances = new ArrayList<>();

        boolean issc = isStringChinese(keyword);

        Cursor c = null;
        if(keyword.length() < 1)
        {
            result.add(errMsg);
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
            System.out.println(e);
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

    @Override
    public List<String> getAll() {
        List<String> result = new ArrayList<String>();
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

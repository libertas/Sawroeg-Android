package org.roeg.sawroeg;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import org.roeg.cytokenizer.CYTokenizer;
import org.roeg.cytokenizer.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LatinChineseDict extends Dict{
    protected final int Max_Ch_Word_Length = 7;
    protected final int Max_Ch_Word_Times = 2;

    protected SQLiteDatabase db;
    protected CYTokenizer tokenizer;
    protected String errMsg;

    LatinChineseDict(SQLiteDatabase db, CYTokenizer tokenizer, String errMsg) {
        this.db = db;
        this.tokenizer = tokenizer;
        this.errMsg = errMsg;


        // Getting allKeys
        this.allKeys = new ArrayList<String>();
        Cursor c = null;

        try {
            c = db.rawQuery("SELECT distinct(key) FROM sawguq", null);

            while(c.moveToNext()) {
                // get and key words and replace "\xa0" with space
                this.allKeys.add(c.getString(c.getColumnIndex("key")).replace(" ", " "));
            }

        } catch (Exception e) {

        } finally {
            if(c != null)
                c.close();
        }

        Collections.sort(this.allKeys, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
    }

    String filter(String s) {
        s = s.replaceAll("\\[[^\\]]+\\]", "");
        s = s.replaceAll("（[^）]*\\）", "");
        s = s.replaceAll("<[^>]*>", "");
        s = s.replaceAll("\\{[^\\}]*\\}", "");
        return s;
    }

    @Override
    public Iterator<String> search(String keyword, int limit_length) {
        keyword = keyword.toLowerCase();

        List<Word> keys = null;

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> result1 = new ArrayList<>();
        ArrayList<Float> distances = new ArrayList<>();
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
            if(issc) {
                c = db.rawQuery("SELECT * FROM sawguq WHERE value like \"%%$s%%\"".replace("$s", keyword), null);
            } else {
                c = db.rawQuery("SELECT * FROM sawguq WHERE key like \"%%$s%%\"".replace("$s", keyword), null);
            }

            String i, j;
            float distance;

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
                    List<Float> disArray = new ArrayList<>();
                    j = languageFilter(filter(j), issc);

                    String[] js = j.split("[ ，；。]+");
                    for(String part: js) {
                        if(part.equals("") || (keyword.length() < Max_Ch_Word_Length
                                && part.length() > Max_Ch_Word_Length)) {
                            continue;
                        }
                        int ldistance = Levenshtein.distance(part, keyword);
                        if(ldistance == 0) {
                            disArray = new ArrayList<>(1);
                            disArray.add((float)ldistance);
                            break;
                        }
                        disArray.add((float)ldistance);
                    }

                    distance = 0;
                    for(Float f: disArray) {
                        distance += f;
                    }
                    distance /= disArray.size();
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
            ArrayList<Pair<String, Float>> pair = new ArrayList<>();
            for(int i = 0; i < result1.size(); i++) {
                pair.add(new Pair<String, Float>(result1.get(i), distances.get(i)));
            }
            Collections.sort(pair, new Comparator<Pair<String, Float>>() {

                @Override
                public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                    return Float.compare(o1.second, o2.second);
                }
            });

            for(int i = 0; i < pair.size() && i < limit_length; i++) {
                result.add(pair.get(i).first);
            }
        } else {
            ArrayList<Pair<String, Pair<Integer, Float>>> pair = new ArrayList<>();
            for(int i = 0; i < result1.size(); i++) {
                pair.add(new Pair<String, Pair<Integer, Float>>(result1.get(i),
                        new Pair<Integer, Float>(wordDistances.get(i), distances.get(i))));
            }

            Collections.sort(pair, new Comparator<Pair<String, Pair<Integer, Float>>>() {
                @Override
                public int compare(Pair<String, Pair<Integer, Float>> o1,
                                   Pair<String, Pair<Integer, Float>> o2) {
                    if(o1.second.first == o2.second.first
                            || o1.second.first == Integer.MAX_VALUE
                            || o2.second.first == Integer.MAX_VALUE) {
                        return Float.compare(o1.second.second, o2.second.second);
                    } else {
                        return Float.compare(o1.second.first, o2.second.first);
                    }
                }
            });

            for(int i = 0; i < pair.size() && i < limit_length; i++) {
                result.add(pair.get(i).first);
            }
        }

        return result.iterator();
    }
}

package org.roeg.sawroeg;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.InputStream;
import java.io.OutputStream;


public class DBHelper {
    public static void copyFile(InputStream from, OutputStream to) {
        try {
            byte[] buffer = new byte[1024];
            int readBytes = 0;
            while ((readBytes = from.read(buffer)) != -1)
                to.write(buffer, 0, readBytes);
            from.close();
            to.close();
        } catch (Exception e1) {
            System.out.println(e1);
        } finally {
        }
    }

    public static int getDBVersion(SQLiteDatabase db) {
        Cursor c;

        try {
            c = db.rawQuery("SELECT value FROM info WHERE key = 'version'", null);
        } catch(SQLiteException e) {
            return Integer.MIN_VALUE;
        }

        if(c.moveToNext()) {
            String version_str = c.getString(c.getColumnIndex("value"));
            int version;
            try {
                version = Integer.valueOf(version_str).intValue();
            } catch(NumberFormatException e) {
                version = Integer.MIN_VALUE;
            }
            return version;
        } else {
            return Integer.MIN_VALUE;
        }
    }
}

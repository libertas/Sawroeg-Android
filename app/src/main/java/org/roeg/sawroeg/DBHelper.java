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
            throw new Error("Unable to create database");
        } finally {
        }
    }

    public static int getDBVersion(SQLiteDatabase db) {
        Cursor c;

        try {
            c = db.rawQuery("SELECT value FROM info WHERE key = 'version'", null);
        } catch(SQLiteException e) {
            return 0;
        }

        if(c.moveToNext()) {
            String version_str = c.getString(c.getColumnIndex("value"));
            int version;
            try {
                version = Integer.valueOf(version_str).intValue();
            } catch(NumberFormatException e) {
                version = 0;
            }
            return version;
        } else {
            return 0;
        }
    }
}

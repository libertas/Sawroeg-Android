package org.roeg.sawroeg;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DictUpdateIntentService extends IntentService {

    private static final String ACTION_DictUpdate = "org.roeg.sawroeg.action.DICTUPDATE";

    private static final String EXTRA_DBNAME = "org.roeg.sawroeg.extra.DBVERSIONTOUPDATE";
    private static final String EXTRA_DBVERSIONFILE = "org.roeg.sawroeg.extra.DBVERSIONFILE";

    private static final String DB_URL = "https://sawroeg.rliber.com/download/db/";

    public DictUpdateIntentService() {
        super("DictUpdateIntentService");
    }

    public static void startActionDictUpdate(Context context, String dbname, String versionFile) {
        Intent intent = new Intent(context, DictUpdateIntentService.class);
        intent.setAction(ACTION_DictUpdate);
        intent.putExtra(EXTRA_DBNAME, dbname);
        intent.putExtra(EXTRA_DBVERSIONFILE, versionFile);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DictUpdate.equals(action)) {
                final String dbname = intent.getStringExtra(EXTRA_DBNAME);
                final String versionFile = intent.getStringExtra(EXTRA_DBVERSIONFILE);
                handleActionDictUpdate(dbname, versionFile);
            }
        }
    }

    private void handleActionDictUpdate(String dbname, String versionFile) {
        int version_net = 0;
        int version = 0;

        try {
            HttpURLConnection connection = null;
            URL versionUrl = new URL(DB_URL + versionFile);
            connection=(HttpURLConnection)versionUrl.openConnection();
            connection.setRequestMethod("GET");

            InputStream in = connection.getInputStream();

            BufferedReader bufr=new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line=null;
            while((line = bufr.readLine())!=null){
                response.append(line);
            }

            if(connection != null) {
                connection.disconnect();
            }

            version_net = Integer.valueOf(response.toString());

            File f_net = new File("data/data/org.roeg.sawroeg/databases/" + dbname + ".net");
            File f = new File("data/data/org.roeg.sawroeg/databases/" + dbname);
            SQLiteDatabase db;
            if(f_net.exists()) {
                db = SQLiteDatabase.openDatabase("data/data/org.roeg.sawroeg/databases/" + dbname + ".net", null, MODE_PRIVATE);
                version = DBHelper.getDBVersion(db);
                db.close();
            } else if(f.exists()) {
                db = SQLiteDatabase.openDatabase("data/data/org.roeg.sawroeg/databases/" + dbname, null, MODE_PRIVATE);
                version = DBHelper.getDBVersion(db);
                db.close();
            } else {
                version = Integer.MIN_VALUE;
            }
        } catch(MalformedURLException e1) {
            System.out.println(e1);
            return;
        } catch(IOException e2) {
            System.out.println(e2);
            return;
        } catch(NumberFormatException e3) {
            System.out.println(e3);
            System.out.println("Exception on version code");
            return;
        }

        if(version_net > version) {
            System.out.println("Updating");

            try {
                HttpURLConnection connection = null;
                URL versionUrl = new URL(DB_URL + dbname);
                connection=(HttpURLConnection)versionUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();

                FileOutputStream out = new FileOutputStream("data/data/org.roeg.sawroeg/databases/"
                        + dbname + ".net.download");

                System.out.println("Downloading");
                DBHelper.copyFile(in, out);
                connection.disconnect();

                SQLiteDatabase db_download = openOrCreateDatabase(dbname + ".net.download", MODE_PRIVATE, null);
                int version_download = DBHelper.getDBVersion(db_download);
                db_download.close();

                System.out.println("Downloaded");

                if(version_download != version_net) {
                    return;
                }

                FileInputStream  fin= new FileInputStream("data/data/org.roeg.sawroeg/databases/"
                        + dbname + ".net.download");
                FileOutputStream fout = new FileOutputStream("data/data/org.roeg.sawroeg/databases/"
                        + dbname + ".net");
                DBHelper.copyFile(fin, fout);
            } catch(MalformedURLException e1) {
                System.out.println(e1);
                return;
            } catch(IOException e2) {
                System.out.println(e2);
                return;
            }
        }
    }
}

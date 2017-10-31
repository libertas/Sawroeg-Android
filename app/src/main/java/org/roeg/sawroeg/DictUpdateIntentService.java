package org.roeg.sawroeg;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DictUpdateIntentService extends IntentService {

    private static final String ACTION_DictUpdate = "org.roeg.sawroeg.action.DICTUPDATE";

    private static final String EXTRA_DBNAME = "org.roeg.sawroeg.extra.DBVERSIONTOUPDATE";

    public DictUpdateIntentService() {
        super("DictUpdateIntentService");
    }

    public static void startActionDictUpdate(Context context, String dbname) {
        Intent intent = new Intent(context, DictUpdateIntentService.class);
        intent.setAction(ACTION_DictUpdate);
        intent.putExtra(EXTRA_DBNAME, dbname);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DictUpdate.equals(action)) {
                final String dbname = intent.getStringExtra(EXTRA_DBNAME);
                handleActionDictUpdate(dbname);
            }
        }
    }

    private void handleActionDictUpdate(String dbname) {
        int version_net = 0;
        int version = 0;

        try {
            HttpURLConnection connection = null;
            URL versionUrl = new URL("http://sawroeg.rliber.com/download/db/version.txt");
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

            File f = new File(dbname + ".net");
            SQLiteDatabase db;
            if(f.exists()) {
                db = openOrCreateDatabase(dbname + ".net", MODE_PRIVATE, null);
            } else {
                db = openOrCreateDatabase(dbname, MODE_PRIVATE, null);
            }

            version = DBHelper.getDBVersion(db);
            db.close();
        } catch(MalformedURLException e1) {
            System.out.println(e1);
        } catch(IOException e2) {
            System.out.println(e2);
        } catch(NumberFormatException e3) {
            System.out.println(e3);
            System.out.println("Exception on version code");
        }

        if(version_net > version) {
            System.out.println("Updating");
        }
    }
}

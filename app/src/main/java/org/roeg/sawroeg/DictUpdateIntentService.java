package org.roeg.sawroeg;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class DictUpdateIntentService extends IntentService {

    private static final String ACTION_DictUpdate = "org.roeg.sawroeg.action.DICTUPDATE";

    private static final String EXTRA_DBNAME = "org.roeg.sawroeg.extra.DBNAMETOUPDATE";

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
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}

package org.roeg.sawroeg;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference); 
	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {  
        String key = preference.getKey();  
        if( key != null ){  
            if(key.equals("length_edit")) {  
            }  
        }  
        return super.onPreferenceTreeClick(preferenceScreen, preference);  
    }
}

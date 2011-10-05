package name.chakimar.sbb;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SimpleBaseBrowser extends Application {

	public static final String DEFAULT_HOMEPAGE = "http://www.google.co.jp";
	public static final String PREF_KEY_HOMEPAGE = "homepage";

	public String getDataString(String key, String defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(key, defaultValue);
	}
	
	public void putDataString(String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
	}
	
	public String getHomepage() {
		return getDataString(PREF_KEY_HOMEPAGE, DEFAULT_HOMEPAGE);
	}
	
	public void setHomePage(String url) {
		putDataString(PREF_KEY_HOMEPAGE, url);
	}
}

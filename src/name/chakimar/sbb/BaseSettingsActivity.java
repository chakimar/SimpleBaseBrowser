package name.chakimar.sbb;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class BaseSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	private EditTextPreference prefHomepage;
	private SimpleBaseBrowser app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.app = (SimpleBaseBrowser) getApplication();
		
		addPreferencesFromResource(R.xml.settings);
		
		this.prefHomepage = (EditTextPreference) findPreference(SimpleBaseBrowser.PREF_KEY_HOMEPAGE);
		prefHomepage.setSummary(app.getHomepage());
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (key.equals(SimpleBaseBrowser.PREF_KEY_HOMEPAGE)) {
			prefHomepage.setSummary(app.getHomepage());
		}
		
	}

}

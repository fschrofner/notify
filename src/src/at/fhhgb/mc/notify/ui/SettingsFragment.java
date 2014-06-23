package at.fhhgb.mc.notify.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import at.fhhgb.mc.notify.R;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
    }
}

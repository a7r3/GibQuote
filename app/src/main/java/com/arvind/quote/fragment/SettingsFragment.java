package com.arvind.quote.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

// A placeholder fragment containing a simple view.
public class SettingsFragment extends PreferenceFragmentCompatDividers {

    private SharedPreferences sharedPreferences;

    // Listen for changes in a SharedPreference
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private ListPreference themePreference;
    private CheckBoxPreference fragSwitcherPreference;

    private String getThemeSummary() {
        String themeKey = sharedPreferences.getString("THEME_KEY", "light");
        return "Current: " + themeKey.substring(0, 1).toUpperCase() + themeKey.substring(1);
    }

    private String getFragSwitcherSummary() {
        String fragSwitcherSummary = sharedPreferences.getBoolean("FRAG_SWITCHER_KEY", true)
                ? "Bottom Navigation Bar"
                : "Navigation Drawer";
        return "Current: " + fragSwitcherSummary;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setActionBarTitle("Settings");
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        themePreference = (ListPreference) findPreference("THEME_KEY");
        themePreference.setSummary(getThemeSummary());

        fragSwitcherPreference = (CheckBoxPreference) findPreference("FRAG_SWITCHER_KEY");
        fragSwitcherPreference.setSummary(getFragSwitcherSummary());

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                Snackbar.make(getActivity().findViewById(R.id.frame_layout),
                        "Restarting Application",
                        Snackbar.LENGTH_LONG)
                        .show();
                switch (s) {
                    case "THEME_KEY":
                        themePreference.setSummary(getThemeSummary());
                        break;
                    case "FRAG_SWITCHER_KEY": // TODO
                        fragSwitcherPreference.setSummary(getFragSwitcherSummary());
                        break;
                }

                // Sleep for a second or maybe two
                // Create a restart intent
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = getActivity().getIntent();
                                getActivity().finish();
                                startActivity(intent);
                            }
                        });
                    }
                }).start();
            }
        };

        // Register the listener (so that it could do its job)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_screen, rootKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Your job's done
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
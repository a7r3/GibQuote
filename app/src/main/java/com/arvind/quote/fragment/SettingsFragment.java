package com.arvind.quote.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.arvind.quote.BuildConfig;
import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.utils.SomeBroadReceiver;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;
import com.takisoft.fix.support.v7.preference.TimePickerPreference;

// A placeholder fragment containing a simple view.
public class SettingsFragment extends PreferenceFragmentCompatDividers {

    private String TAG = "SettingsFragment";
    private SharedPreferences sharedPreferences;

    // Listen for changes in a SharedPreference
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private ListPreference themePreference;
    private CheckBoxPreference fragSwitcherPreference;
    private TimePickerPreference qotdTimePref;

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
        MainActivity.setActionBarTitle("Preferences");
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        themePreference = (ListPreference) findPreference("THEME_KEY");
        themePreference.setSummary(getThemeSummary());

        fragSwitcherPreference = (CheckBoxPreference) findPreference("FRAG_SWITCHER_KEY");
        fragSwitcherPreference.setSummary(getFragSwitcherSummary());

        qotdTimePref = (TimePickerPreference) findPreference("QOTD_TIME");
        qotdTimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Found out that newValue is of the below type.
                // Trial and error :D
                TimePickerPreference.TimeWrapper timeWrapper = (TimePickerPreference.TimeWrapper) newValue;
                sharedPreferences.edit()
                        .putInt("QOTD_HOUR", timeWrapper.hour)
                        .putInt("QOTD_MIN", timeWrapper.minute)
                        .apply();
                Log.i(TAG, timeWrapper.hour + " : " +
                        timeWrapper.minute);
                Intent notifServiceIntent = new Intent(getContext(), SomeBroadReceiver.class);
                notifServiceIntent.setAction(BuildConfig.APPLICATION_ID + ".TIME_SET_BY_USER");
                getContext().sendBroadcast(notifServiceIntent);
                return true;
            }
        });

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                boolean isRestartApplicable = false;

                switch (s) {
                    case "THEME_KEY":
                        isRestartApplicable = true;
                        themePreference.setSummary(getThemeSummary());
                        break;
                    case "FRAG_SWITCHER_KEY":
                        isRestartApplicable = true;
                        fragSwitcherPreference.setSummary(getFragSwitcherSummary());
                        break;
                }

                if (isRestartApplicable) {
                    Snackbar.make(getActivity().findViewById(R.id.frame_layout),
                            "Restarting Application",
                            Snackbar.LENGTH_LONG)
                            .show();
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
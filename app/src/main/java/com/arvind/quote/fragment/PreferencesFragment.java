package com.arvind.quote.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.preference.SwitchPreference;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.util.Log;

import com.arvind.quote.BuildConfig;
import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.arvind.quote.utils.SomeBroadReceiver;
import com.arvind.quote.utils.Updater;
import com.takisoft.fix.support.v7.preference.PreferenceCategory;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;
import com.takisoft.fix.support.v7.preference.TimePickerPreference;

// A placeholder fragment containing a simple view.
public class PreferencesFragment extends PreferenceFragmentCompatDividers {

    private final String TAG = "PreferencesFragment";
    private SharedPreferences sharedPreferences;

    // Listen for changes in a SharedPreference
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private ListPreference themePreference;
    private CheckBoxPreference fragSwitcherPreference;
    private SwitchPreference switchPreference;
    private TimePickerPreference qotdTimePref;
    private CheckBoxPreference updatePreference;

    private String getThemeSummary() {
        String themeKey = sharedPreferences.getString("THEME_KEY", "light");
        Log.d(TAG, "Theme : " + themeKey);
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

        PreferenceCategory lookCategory = (PreferenceCategory) findPreference("LOOK_AND_FEEL");

        themePreference = (ListPreference) lookCategory.findPreference("THEME_KEY");
        themePreference.setSummary(getThemeSummary());

        fragSwitcherPreference = (CheckBoxPreference) lookCategory.findPreference("FRAG_SWITCHER_KEY");
        fragSwitcherPreference.setSummary(getFragSwitcherSummary());

        PreferenceCategory qotdCategory = (PreferenceCategory) findPreference("QOTD_CAT");

        qotdTimePref = (TimePickerPreference) qotdCategory.findPreference("QOTD_TIME");
        qotdTimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Found out that newValue is of the below type.
                // Peeked into TimePickerPreference's source
                // Trial and error :D
                TimePickerPreference.TimeWrapper timeWrapper = (TimePickerPreference.TimeWrapper) newValue;
                sharedPreferences.edit()
                        .putInt("QOTD_HOUR", timeWrapper.hour)
                        .putInt("QOTD_MIN", timeWrapper.minute)
                        .apply();
                Log.d(TAG, "QoTD notification scheduled on " + timeWrapper.hour + " : " +
                        timeWrapper.minute);
                Intent notifServiceIntent = new Intent(getContext(), SomeBroadReceiver.class);
                notifServiceIntent.setAction(BuildConfig.APPLICATION_ID + ".TIME_SET_BY_USER");
                getContext().sendBroadcast(notifServiceIntent);
                return true;
            }
        });

        // Preference to determine whether QoTD TimePicker has to be shown
        switchPreference = (SwitchPreference) qotdCategory.findPreference("QOTD_ENABLE");
        switchPreference.setSummaryOn("You're receiving daily notifications");
        switchPreference.setSummaryOff("You're not receiving daily notifications");

        // Hide the TimePickerPreference if it's unchecked
        if (!switchPreference.isChecked())
            qotdTimePref.setVisible(false);

        switchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isSwitchOn = (Boolean) newValue;
                if (isSwitchOn) // show the TimePicker
                    qotdTimePref.setVisible(true);
                else {
                    // Hide the TimePicker, and Cancel all alarms in the BroadcastReceiver
                    qotdTimePref.setVisible(false);
                    Intent notifServiceIntent = new Intent(getContext(), SomeBroadReceiver.class);
                    notifServiceIntent.setAction(BuildConfig.APPLICATION_ID + ".CANCEL_QOTD");
                    getContext().sendBroadcast(notifServiceIntent);
                }
                return true;
            }
        });

        PreferenceCategory updatePrefCategory = (PreferenceCategory) findPreference("UPDATES");

        updatePreference = (CheckBoxPreference) updatePrefCategory.findPreference("UPDATE_CHECK");
        updatePreference.setSummaryOff("Updates are disabled");
        updatePreference.setSummaryOn("Updates are enabled on startup");

        updatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean isChecked = (Boolean) newValue;
                if (isChecked) {
                    sharedPreferences.edit()
                            .putBoolean("UPDATES_ENABLED", true)
                            .apply();
                    new Updater(getActivity())
                            .setTagsUrl(MainActivity.GIT_TAG_URL)
                            .setRootLayout(R.id.root_layout)
                            .checkForUpdates();
                }
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
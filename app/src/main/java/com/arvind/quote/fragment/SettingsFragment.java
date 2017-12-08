package com.arvind.quote.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import com.arvind.quote.MainActivity;
import com.arvind.quote.R;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends PreferenceFragmentCompatDividers {

    private SharedPreferences sharedPreferences;

    // Listen for changes in a SharedPreference
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setActionBarTitle("Settings");
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                switch (s) {
                    case "THEME_KEY":
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
                        break;
                    case "PRIMARY_KEY": // TODO
                        break;
                    case "ACCENT_KEY": // TODO
                        break;
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
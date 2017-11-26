package com.arvind.quote.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import com.arvind.quote.R;

public class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;
        private String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preference_screen, rootKey);
    }
}
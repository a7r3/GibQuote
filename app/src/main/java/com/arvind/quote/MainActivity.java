package com.arvind.quote;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.arvind.quote.fragment.FavQuoteFragment;
import com.arvind.quote.fragment.GibQuoteFragment;
import com.arvind.quote.fragment.SettingsFragment;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    private final String TAG = "MainActivity";

    // ActionBar for the App
    private static ActionBar actionBar;

    // Layout under which fragments would reside
    private DrawerLayout drawerLayout;

    // Provides toggling action to open the Navigation Drawer
    // Tha Hamburger thingy
    private ActionBarDrawerToggle drawerToggle;

    // Application's Shared Preferences
    private SharedPreferences sharedPreferences;

    // Theme ID - from styles.xml
    private int themeId = R.style.AppTheme;

    // Set ActionBar's title
    public static void setActionBarTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String themeKey = sharedPreferences.getString("THEME_KEY", "light");

        Log.d(TAG, "Theme: " + themeKey);
        switch (themeKey) {
            case "light":
                themeId = R.style.AppTheme;
                break;
            case "dark":
                themeId = R.style.AnotherAppTheme;
                break;
            case "translucent": // TODO: TRANSLUCENT
                themeId = R.style.AppTheme;
                break;
        }

        // Set Activity theme
        setTheme(themeId);

        setContentView(R.layout.activity_main);

        NavigationView navigationView = findViewById(R.id.nav_bar_view);

        int cardViewBackGround = R.color.cardview_light_background;
        switch (themeKey) {
            case "light":
                cardViewBackGround = R.color.cardview_light_background;
                break;
            case "dark":
                cardViewBackGround = R.color.cardview_dark_background;
                break;
            case "translucent": // TODO: TRANSLUCENT
                cardViewBackGround = R.color.cardview_light_background;
                break;
        }

        // Set background of the Navigation drawer view
        navigationView.setBackgroundColor(getResources().getColor(cardViewBackGround));

        // Root Layout of the Application
        // DrawerLayout consists of
        // 1. The Toolbar (ActionBar)
        // 2. The Navigation (Drawer) View
        // 3. RelativeLayout, which occupies rest of the screen
        drawerLayout = findViewById(R.id.drawer_layout);

        // ToolBar Stuffs
        Toolbar toolbar = findViewById(R.id.app_toolbar);

        // Set 'toolbar' as an ActionBar
        setSupportActionBar(toolbar);

        // Required by fragments to setActionBarTitle()
        actionBar = getSupportActionBar();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_layout, new GibQuoteFragment());
            ft.commit();
        }

        setActionBarTitle("GibQuote");

        // Listen for clicks on any MenuItem present in the Navigation Drawer
        // MenuItem is Identified by its position
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switchFragment(item);
                return true;
            }
        });
    }

    public void switchFragment(MenuItem item) {
        Fragment fragment;

        switch (item.getItemId()) {
            case R.id.gib_quotes_item:
                fragment = new GibQuoteFragment();
                break;
            case R.id.settings_item:
                fragment = new SettingsFragment();
                break;
            case R.id.about_item:
                setActionBarTitle("About");
                // Creating AboutLibraries' Fragment :D
                fragment = new LibsBuilder()
                        .withAboutAppName("GibQuote")
                        .withAboutDescription("Random project to fetch random quotes from random providers")
                        .withAboutIconShown(true)
                        .withVersionShown(true)
                        .supportFragment();
                break;
            case R.id.fav_quotes_item:
                fragment = new FavQuoteFragment();
                break;
            default:
                fragment = new GibQuoteFragment();
                break;
        }

        try {
            Log.d(TAG, "Creating new Fragment Instance");
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.frame_layout, fragment, fragment.getClass().getCanonicalName())
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close drawers present in this DrawerLayout
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frame_layout, fragment, preferenceScreen.getKey())
                .addToBackStack(preferenceScreen.getKey())
                .commitAllowingStateLoss();

        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

}

package com.arvind.quote;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.arvind.quote.fragment.FavQuoteFragment;
import com.arvind.quote.fragment.GibQuoteFragment;
import com.arvind.quote.fragment.PreferencesFragment;
import com.arvind.quote.utils.Updater;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    // GitHub Tag Endpoint
    public final static String GIT_TAG_URL = "https://api.github.com/repos/a7r3/GibQuote/git/refs/tags";
    // ActionBar for the App
    private static ActionBar actionBar;
    // Who am I ?
    private final String TAG = "MainActivity";
    // Callback value
    // This would be used to check which set of permissions were asked
    // This is of Storage
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private final String[] requiredPerms = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean isStoragePermissionGranted = false;
    // Layout under which fragments would reside
    private DrawerLayout drawerLayout;
    // Provides toggling action to open the Navigation Drawer
    // Tha Hamburger thingy
    private ActionBarDrawerToggle drawerToggle;
    // Application's Shared Preferences
    private SharedPreferences sharedPreferences;
    // Theme ID - from styles.xml
    public static int themeId = R.style.AppTheme;
    // Keep track of previous selected Drawer Item
    // to make sure the same fragment isn't instantiated again
    private MenuItem previousItem;
    private BottomNavigationView bottomNavigationView;
    // Root Layout
    private RelativeLayout rootLayout;

    // Required by Fragments to ...
    // Set ActionBar's title
    public static void setActionBarTitle(String title) {
        if (actionBar != null)
            actionBar.setTitle(title);
    }

    public static void showActionBar() {
        if (actionBar != null)
            actionBar.show();
    }

    public static void hideActionBar() {
        if (actionBar != null)
            actionBar.hide();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If the granted permission was related to Storage
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            // If there are non-zero grants, and We've got Permission for Storage (there's just one perm)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Yay
                Log.d(TAG, "Storage Permission Granted");
                Snackbar.make(findViewById(R.id.root_layout),
                        "Storage Permission Granted",
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Storage permission NOT Granted");
                // Nothing to do here, yet | We can disable update checks, later
            }
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeKey = sharedPreferences.getString("THEME_KEY", "light");

        int cardViewBackGround = R.color.colorLightCardView;
        int activityBackground = R.color.lightBackground;

        Log.d(TAG, "Theme: " + themeKey);
        switch (themeKey) {
            case "light":
                themeId = R.style.AppTheme;
                break;
            case "dark":
                themeId = R.style.AnotherAppTheme;
                cardViewBackGround = R.color.colorDarkCardView;
                activityBackground = R.color.darkBackground;
                break;
            case "translucent": // TODO: TRANSLUCENT
                themeId = R.style.TranslucentAppTheme;
                cardViewBackGround = R.color.translucentBackground;
                break;
        }

        // Set Activity theme
        setTheme(themeId);

        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.root_layout);

        // If WRITE_EXTERNAL_STORAGE permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If the user has to be provided a reason to grant a permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // then show the reason
                Snackbar.make(rootLayout,
                        "App requires Storage permission to Install Updates",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        requiredPerms,
                                        REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        }).show();
            } else {
                // No reason to be provided, ask directly
                ActivityCompat.requestPermissions(this,
                        requiredPerms,
                        REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else
            isStoragePermissionGranted = true;

        NavigationView navigationView = findViewById(R.id.nav_bar_view);

        // Set background of the Navigation drawer view
        navigationView.setBackgroundColor(getResources().getColor(cardViewBackGround));

        if (themeKey.equals("translucent")) {
            AnimationDrawable anim = (AnimationDrawable) rootLayout.getBackground();
            anim.setEnterFadeDuration(1000);
            anim.setExitFadeDuration(1000);
            // Start the animating background
            anim.start();
        } else
            rootLayout.setBackgroundResource(activityBackground);

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

        // Required by fragments to modify ActionBar
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
                // If the same item isn't selected, then switch fragment
                if (previousItem != item) {
                    switchFragment(item);
                    previousItem = item;
                }
                // Close drawers present in this DrawerLayout
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Since GibQuoteFragment is shown by default
        // Set it as the selected item in NavigationView (Drawer)
        navigationView.getMenu().getItem(0).setChecked(true);

        bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setVisibility(View.VISIBLE);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // If the same item isn't selected, then switch fragment
                if (previousItem != item) {
                    switchFragment(item);
                    previousItem = item;
                }
                return true;
            }
        });

        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        boolean isBottomNavEnabled = sharedPreferences.getBoolean("FRAG_SWITCHER_KEY", false);
        if (isBottomNavEnabled) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.setDrawerIndicatorEnabled(false);
            hideActionBar();
        } else {
            showActionBar();
            bottomNavigationView.setVisibility(View.GONE);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

        if (isStoragePermissionGranted)
            if (sharedPreferences.getBoolean("UPDATE_CHECK", true))
                new Updater(this)
                        .setTagsUrl(GIT_TAG_URL)
                        .setRootLayout(R.id.root_layout)
                        .checkForUpdates();
    }

    private void switchFragment(MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.gib_quotes_item:
                fragment = new GibQuoteFragment();
                break;
            case R.id.preferences_item:
                fragment = new PreferencesFragment();
                break;
            case R.id.licenses_item:
                setActionBarTitle("Licenses");
                // Creating AboutLibraries' Fragment :D
                fragment = new LibsBuilder()
                        .withAboutAppName(getResources().getString(R.string.app_name))
                        .withAboutDescription("Random project to fetch random quotes from random providers")
                        .withAboutIconShown(true)
                        .withVersionShown(true)
                        .withAutoDetect(true)
                        .withLicenseShown(true)
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
            Log.d(TAG, "Switching to Fragment : " + fragment.getClass().getSimpleName());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.frame_layout, fragment, fragment.getClass().getCanonicalName())
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        PreferencesFragment fragment = new PreferencesFragment();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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

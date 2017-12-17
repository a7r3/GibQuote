package com.arvind.quote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.arvind.quote.adapter.Quote;
import com.arvind.quote.fragment.FavQuoteFragment;
import com.arvind.quote.fragment.GibQuoteFragment;
import com.arvind.quote.fragment.SettingsFragment;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    // ActionBar for the App
    private static ActionBar actionBar;
    // Who am I ?
    private final String TAG = "MainActivity";
    // Layout under which fragments would reside
    private DrawerLayout drawerLayout;

    // Provides toggling action to open the Navigation Drawer
    // Tha Hamburger thingy
    private ActionBarDrawerToggle drawerToggle;

    // Application's Shared Preferences
    private SharedPreferences sharedPreferences;

    // Theme ID - from styles.xml
    private int themeId = R.style.AppTheme;

    // Keep track of previous selected Drawer Item
    // to make sure the same fragment isn't instantiated again
    private MenuItem previousItem;

    // Required by Fragments to ...
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

        NavigationView navigationView = findViewById(R.id.nav_bar_view);

        // Set background of the Navigation drawer view
        navigationView.setBackgroundColor(getResources().getColor(cardViewBackGround));

        RelativeLayout rootLayout = findViewById(R.id.root_layout);

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
            Log.d(TAG, "Creating new Fragment Instance");
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
        SettingsFragment fragment = new SettingsFragment();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /* Allows the user to share currently displayed quote */
    public void shareQuote(Context context, Quote quote) {
        Log.d(TAG, "Creating Share Intent");
        // My intention is to send (throw) a piece of Text (ball)
        Intent quoteIntent = new Intent(Intent.ACTION_SEND);
        // Piece of Text (the Ball)
        String quoteMessage = quote.getQuoteText() + "\n\n-- " + quote.getAuthorText();
        // Specify the Text to be thrown
        quoteIntent.putExtra(Intent.EXTRA_TEXT, quoteMessage);
        // Specify the MIME type of the object to be thrown
        quoteIntent.setType("text/plain");
        // Send an Acknowledgement
        Toast.makeText(context, "Select an App to GibQuote", Toast.LENGTH_SHORT).show();
        // Throw the Ball!
        context.startActivity(Intent.createChooser(quoteIntent, "Share this Quote"));
    }


}

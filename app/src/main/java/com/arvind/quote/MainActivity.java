package com.arvind.quote;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.arvind.quote.Fragments.AboutFragment;
import com.arvind.quote.Fragments.FavQuoteFragment;
import com.arvind.quote.Fragments.GibQuoteFragment;
import com.arvind.quote.Fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GibQuote";
    private DrawerLayout drawerLayout;
    private Fragment fragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    private static ActionBar actionBar;


    public static void setActionBarTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }


    // MainActivity Context
    // Required, since shareQuote() is called under the context
    // of RecyclerView
    private final Context mainActivityContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Set Theme unless it is a fresh start
//        if (getIntent().getStringExtra("Theme") != null) {
//            switch (getIntent().getStringExtra("Theme")) {
//                case "Dark":
//                    setTheme(R.style.AnotherAppTheme);
//                    break;
//                case "Light":
//                    setTheme(R.style.AppTheme);
//                    break;
//            }
//        }

        try {
            fragment = GibQuoteFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();

        // ToolBar Stuffs
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);

        // Set 'toolbar' as an ActionBar
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setTitle("GibQuote");

        // Root Layout of the Application
        // DrawerLayout consists of
        // 1. The Toolbar (ActionBar)
        // 2. The Navigation (Drawer) View
        // 3. RelativeLayout, which occupies rest of the screen
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_bar_view);

        // Listen for clicks on any MenuItem present in the Navigation Drawer
        // MenuItem is Identified by its position
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Class fragmentClass = GibQuoteFragment.class;
                Fragment fragment = new GibQuoteFragment();

                switch (item.getItemId()) {
                    case R.id.gib_quotes_item:
                        fragmentClass = GibQuoteFragment.class;
                        break;
                    case R.id.theme_selector_item:
                        fragmentClass = SettingsFragment.class;
                        break;
                    case R.id.about_item:
                        fragmentClass = AboutFragment.class;
                        break;
                    case R.id.fav_quotes_item:
                        fragmentClass = FavQuoteFragment.class;
                        break;
                }

                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .commit();
                // Close drawers present in this DrawerLayout
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // TODO: Hamburger icon Indicator for Navigation Drawer
        // drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        // drawerLayout.addDrawerListener(drawerToggle);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
 }
}
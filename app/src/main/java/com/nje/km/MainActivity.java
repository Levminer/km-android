package com.nje.km;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Material You: Android 12+ rendszeren a színek a felhasználó háttérképéből jönnek
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, 0);
            return windowInsets;
        });
        // Világos módban az app bar világos, ezért ott sötét állapotsor ikonok kellenek
        boolean nightMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(!nightMode);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateUpButton);
        updateUpButton();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            popToRoot();
            showFragment(item.getItemId() == R.id.nav_stops
                    ? new StopsFragment()
                    : new LinesFragment());
            return true;
        });
        // Az aktuális fülre újra koppintva visszaugrik annak első oldalára
        bottomNav.setOnItemReselectedListener(item -> popToRoot());

        // A Vonalak fül már ki van választva, így az újbóli kiválasztása újrakiválasztásnak számítana
        if (savedInstanceState == null) {
            showFragment(new LinesFragment());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    /** A fragmentek hívják meg, amikor láthatóvá válnak. */
    public void setToolbarTitle(CharSequence title, @Nullable CharSequence subtitle) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }

    /** Új oldalt nyit a jelenlegi fölött, így a vissza gomb ide tér vissza. */
    public void openPage(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void popToRoot() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void updateUpButton() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }
}

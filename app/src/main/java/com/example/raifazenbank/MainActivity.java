package com.example.raifazenbank;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.raifazenbank.databinding.ActivityMainBinding;
import com.example.raifazenbank.ui.home.HomeFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private Menu menu; // Меню сохраняем в переменную

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDefaultLocale(); // Сначала устанавливаем язык
        super.onCreate(savedInstanceState); // Потом вызываем жизненный цикл

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Слушатель изменения вкладок
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (menu != null) {
                MenuItem refreshItem = menu.findItem(R.id.action_refresh);
                MenuItem settingsItem = menu.findItem(R.id.action_settings);

                if (refreshItem != null) {
                    // Кнопка "Обновить" только на главной вкладке
                    refreshItem.setVisible(destination.getId() == R.id.navigation_home);
                }

                if (settingsItem != null) {
                    // Кнопка "Настройки" только на главной вкладке
                    settingsItem.setVisible(destination.getId() == R.id.navigation_home);
                }
            }

            // Логика отображения кнопки "Назад" в Toolbar
            boolean isTopLevelDestination = destination.getId() == R.id.navigation_home
                    || destination.getId() == R.id.navigation_dashboard
                    || destination.getId() == R.id.navigation_notifications;

            getSupportActionBar().setDisplayHomeAsUpEnabled(!isTopLevelDestination);
        });

        // Запрос разрешения на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void setDefaultLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "uk"); // Язык по умолчанию — uk
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;

        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);

        if (refreshItem != null) {
            refreshItem.setVisible(navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.navigation_home);
        }
        if (settingsItem != null) {
            settingsItem.setVisible(navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.navigation_home);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.navigation_home) {
                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_main)
                        .getChildFragmentManager()
                        .getFragments()
                        .get(0);
                if (homeFragment != null) {
                    homeFragment.refreshWebView();
                }
            }
            return true;
        } else if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void openSettings() {
        navController.navigate(R.id.navigation_settings);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}

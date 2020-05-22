package com.stho.mobicompass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.main_navigation)
                .build();
        NavController navController = findNavController();
        NavigationUI.setupWithNavController(navView, navController);
    }

    private NavController findNavController() {
        return Navigation.findNavController(this, R.id.nav_host_fragment);
    }
}

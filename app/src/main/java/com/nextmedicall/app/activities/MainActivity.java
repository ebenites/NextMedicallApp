package com.nextmedicall.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nextmedicall.app.R;
import com.nextmedicall.app.fragments.ChartFragment;
import com.nextmedicall.app.fragments.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureButtonNavigationView();

        if (savedInstanceState == null) {
            showHomeFragment();
        }
    }

    private void configureButtonNavigationView() {
        // Bottom Navigation
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        showHomeFragment();
                        break;
                    case R.id.menu_chart:
                        showChartFragment();
                        break;
                }
                return true;
            }
        });
    }

    private void showHomeFragment() {
        showFragment(new HomeFragment());
    }

    private void showChartFragment() {
        showFragment(new ChartFragment());
    }

    private void showFragment(Fragment fragment){
        if(fragment instanceof HomeFragment) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);   // https://stackoverflow.com/questions/6186433/clear-back-stack-using-fragments
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment).commit();
        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_content, fragment).addToBackStack("tag").commit();
        }
    }

}
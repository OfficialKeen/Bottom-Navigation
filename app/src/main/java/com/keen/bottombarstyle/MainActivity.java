package com.keen.bottombarstyle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.keen.bottombarstyle.fragment.HomeFragment;
import com.keen.bottombarstyle.fragment.ProfileFragment;
import com.keen.bottomnavigation.NavBottom;

/**
 * Created by keen on 08/01/18.
 */

public class MainActivity extends AppCompatActivity {


    private NavBottom mNavBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavBottom = findViewById(R.id.mainTabBar);

        mNavBottom.onRestoreInstanceState(savedInstanceState);

        mNavBottom.addTab(HomeFragment.class, new NavBottom.TabParam(R.drawable.world, R.drawable.world1, "Home"));
        mNavBottom.addTab(null, new NavBottom.TabParam(0, 0, "Live"));
        mNavBottom.addTab(ProfileFragment.class, new NavBottom.TabParam(R.drawable.people, R.drawable.people1, "Profile"));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavBottom.onSaveInstanceState(outState);
    }


    public void onClickGoLive(View v) {
        startActivity(new Intent(MainActivity.this, LiveActivity.class));
    }
}

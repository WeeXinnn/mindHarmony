package my.utar.edu.mindharmony;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

import my.utar.edu.mindharmony.aichatbot.chatbot;
import my.utar.edu.mindharmony.meditation.meditation;
import my.utar.edu.mindharmony.profile.profile;
import my.utar.edu.mindharmony.userwellbeingtracking.userwellbeingtracking;
import my.utar.edu.mindharmony.wellnessplan.plan;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navBar = findViewById(R.id.bottom_navigation);

        //home page
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new chatbot())
                .commit();

        Map<Integer, Fragment> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.nav_home, new chatbot());
        fragmentMap.put(R.id.nav_meditation, new meditation());
        fragmentMap.put(R.id.nav_plan, new plan());
        fragmentMap.put(R.id.nav_mood, new userwellbeingtracking());
        fragmentMap.put(R.id.nav_profile, new profile());

        navBar.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}

package my.utar.edu.mindharmony.wellnessplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.utar.edu.mindharmony.R;

public class SingleActivity extends AppCompatActivity {

    private TextView activityNameText, activityDescText;
    private Button completeBtn;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String POINTS_KEY = "points";

    private String activityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        activityNameText = findViewById(R.id.activityName);
        activityDescText = findViewById(R.id.activityDesc);
        completeBtn = findViewById(R.id.completeBtn);

        activityName = getIntent().getStringExtra("activity_name");
        String desc = getIntent().getStringExtra("activity_desc");

        activityNameText.setText(activityName);
        activityDescText.setText(desc);

        if (hasCompletedToday(activityName)) {
            completeBtn.setEnabled(false);
            completeBtn.setText("Completed");
            completeBtn.setBackgroundColor(getResources().getColor(R.color.grey));
        }

        completeBtn.setOnClickListener(v -> {
            if (!hasCompletedToday(activityName)) {
                addPoints(10);
                saveTodayAsCompleted(activityName);
                completeBtn.setEnabled(false);
                completeBtn.setText("Completed");
                Toast.makeText(this, "Great job! +10 points added.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addPoints(int points) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentPoints = prefs.getInt(POINTS_KEY, 0);
        int updatedPoints = currentPoints + points;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(POINTS_KEY, updatedPoints);
        editor.apply();
    }

    private boolean hasCompletedToday(String activityName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedDate = prefs.getString(getCompletionKey(activityName), "");
        return getTodayDate().equals(savedDate);
    }

    private void saveTodayAsCompleted(String activityName) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getCompletionKey(activityName), getTodayDate());
        editor.apply();
    }

    private String getCompletionKey(String activityName) {
        return "last_completed_date_" + activityName.replaceAll("\\s+", "_").toLowerCase();
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}

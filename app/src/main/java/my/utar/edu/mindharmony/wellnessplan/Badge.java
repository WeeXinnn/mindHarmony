package my.utar.edu.mindharmony.wellnessplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.utar.edu.mindharmony.R;

public class Badge extends AppCompatActivity {
    private static final String USER_PREFS = "UserPrefs";
    private static final String WELLNESS_PREFS = "UserWellnessPlan";
    private static final String DAILY_COMPLETION_COUNT_KEY = "daily_completion_count";
    private static final String DAILY_COMPLETION_DATE_KEY = "daily_completion_date";
    private static final String STREAK_COUNT_KEY = "streak_count";
    private static final String LAST_STREAK_DATE_KEY = "last_streak_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView badgeNumber = findViewById(R.id.badge_number);
        ImageView badge1Image = findViewById(R.id.badge1_image);
        ImageView badge2Image = findViewById(R.id.badge2_image);
        ImageView badge3Image = findViewById(R.id.badge3_image);
        TextView achievement1Percentage = findViewById(R.id.achievement1_percentage);
        TextView achievement2Percentage = findViewById(R.id.achievement2_percentage);
        TextView achievement3Percentage = findViewById(R.id.achievement3_percentage);

        SharedPreferences userPrefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        SharedPreferences wellnessPrefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        int points = userPrefs.getInt("points", 0);
        int streak = calculateStreak(userPrefs, wellnessPrefs);

        boolean badge1Unlocked = points >= 50;
        boolean badge2Unlocked = points >= 500;
        boolean badge3Unlocked = streak >= 50;

        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putBoolean("badge1_unlocked", badge1Unlocked);
        editor.putBoolean("badge2_unlocked", badge2Unlocked);
        editor.putBoolean("badge3_unlocked", badge3Unlocked);
        editor.apply();

        // Update badge count
        int unlockedCount = (badge1Unlocked ? 1 : 0) + (badge2Unlocked ? 1 : 0) + (badge3Unlocked ? 1 : 0);
        badgeNumber.setText(String.valueOf(unlockedCount));

        if (!badge1Unlocked) {
            badge1Image.setColorFilter(android.graphics.Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (!badge2Unlocked) {
            badge2Image.setColorFilter(android.graphics.Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (!badge3Unlocked) {
            badge3Image.setColorFilter(android.graphics.Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        int progress1 = Math.min((points * 100) / 50, 100);
        achievement1Percentage.setText(progress1 + "%");

        int progress2 = Math.min((points * 100) / 500, 100);
        achievement2Percentage.setText(progress2 + "%");

        int progress3 = Math.min((streak * 100) / 50, 100);
        achievement3Percentage.setText(progress3 + "%");
    }

    private int getActivitiesPerDay(String time) {
        switch (time) {
            case "5 minutes":
                return 2;
            case "15 minutes":
                return 3;
            case "30 minutes":
                return 5;
            default:
                return 3;
        }
    }

    private int calculateStreak(SharedPreferences userPrefs, SharedPreferences wellnessPrefs) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastCompletionDate = userPrefs.getString(DAILY_COMPLETION_DATE_KEY, "");
        String lastStreakDate = userPrefs.getString(LAST_STREAK_DATE_KEY, "");
        int currentStreak = userPrefs.getInt(STREAK_COUNT_KEY, 0);
        int dailyCompletionCount = userPrefs.getInt(DAILY_COMPLETION_COUNT_KEY, 0);
        String time = wellnessPrefs.getString("time", "15 minutes");
        int activitiesPerDay = getActivitiesPerDay(time);

        try {
            // If no previous completion data, streak is 0
            if (lastCompletionDate.isEmpty()) {
                return 0;
            }

            // Parse dates
            Date lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastCompletionDate);
            Date todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today);
            long diffDays = (todayDate.getTime() - lastDate.getTime()) / (24 * 60 * 60 * 1000);

            // If last completion was today or yesterday, check streak
            if (diffDays == 0 || diffDays == 1) {
                // Check if all activities were completed on the last completion date
                boolean completedAll = dailyCompletionCount >= activitiesPerDay;

                if (lastStreakDate.isEmpty()) {
                    // First streak calculation
                    if (completedAll && diffDays == 0) {
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, 1)
                                .putString(LAST_STREAK_DATE_KEY, today)
                                .apply();
                        return 1;
                    }
                    return 0;
                }

                Date lastStreak = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastStreakDate);
                long streakDiff = (todayDate.getTime() - lastStreak.getTime()) / (24 * 60 * 60 * 1000);

                if (diffDays == 0) {
                    // Same day as last completion
                    if (completedAll && streakDiff == 0) {
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, 1)
                                .putString(LAST_STREAK_DATE_KEY, today)
                                .apply();
                        return 1;
                    }
                    return currentStreak;
                } else if (diffDays == 1) {
                    // Consecutive day
                    if (completedAll) {
                        currentStreak++;
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, currentStreak)
                                .putString(LAST_STREAK_DATE_KEY, lastCompletionDate)
                                .apply();
                        return currentStreak;
                    } else {
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, 0)
                                .putString(LAST_STREAK_DATE_KEY, lastCompletionDate)
                                .apply();
                        return 0;
                    }
                } else {
                    // Missed a day
                    if (completedAll && diffDays <= 1) {
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, 1)
                                .putString(LAST_STREAK_DATE_KEY, lastCompletionDate)
                                .apply();
                        return 1;
                    } else {
                        userPrefs.edit()
                                .putInt(STREAK_COUNT_KEY, 0)
                                .putString(LAST_STREAK_DATE_KEY, lastCompletionDate)
                                .apply();
                        return 0;
                    }
                }
            } else {
                userPrefs.edit()
                        .putInt(STREAK_COUNT_KEY, 0)
                        .putString(LAST_STREAK_DATE_KEY, lastCompletionDate)
                        .apply();
                return 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
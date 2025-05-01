package my.utar.edu.mindharmony.wellnessplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import my.utar.edu.mindharmony.R;
import my.utar.edu.mindharmony.database.AppDatabase;
import my.utar.edu.mindharmony.dao.ActivityDao;
import my.utar.edu.mindharmony.models.Activity;
import my.utar.edu.mindharmony.notification.NotificationReceiver;

public class ViewPlan extends AppCompatActivity {
    private static final String WELLNESS_PREFS = "UserWellnessPlan";
    private static final String USER_PREFS = "UserPrefs";
    private static final String RECENT_ACTIVITIES_KEY = "recent_activities";
    private static final String CURRENT_PLAN_KEY = "current_plan";
    private static final String LAST_PLAN_DATE_KEY = "last_plan_date";
    private static final String LAST_QUESTIONNAIRE_HASH_KEY = "last_questionnaire_hash";
    private static final String DAILY_COMPLETION_COUNT_KEY = "daily_completion_count";
    private static final String DAILY_COMPLETION_DATE_KEY = "daily_completion_date";
    private static final String STREAK_COUNT_KEY = "streak_count";
    private static final String LAST_STREAK_DATE_KEY = "last_streak_date";
    private static final int DAYS_TO_AVOID_REPEATS = 7;

    private ActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView title = findViewById(R.id.categoryTitle);
        title.setText("Your Daily Wellness Plan");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ActivityAdapter();
        recyclerView.setAdapter(adapter);

        setupDailyNotification();
        loadDailyPlan();
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

    @Override
    protected void onResume() {
        super.onResume();
        loadDailyPlan();
    }

    private void setupDailyNotification() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentPlan = userPrefs.getString("current_plan", "");
        boolean notificationsEnabled = userPrefs.getBoolean("notifications_enabled", true);

        if (!currentPlan.isEmpty() && notificationsEnabled) {
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", "Daily Reminder");
            intent.putExtra("message", "Check your wellness plan for today!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (alarmManager != null) {
            }
        }
    }

    private void loadDailyPlan() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences wellnessPrefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        String lastPlanDate = wellnessPrefs.getString(LAST_PLAN_DATE_KEY, "");

        if (today.equals(lastPlanDate) && !hasQuestionnaireChanged()) {
            AsyncTask.execute(() -> {
                List<Activity> currentPlan = loadCurrentPlan();
                if (currentPlan.isEmpty()) {
                    generateNewPlan();
                } else {
                    runOnUiThread(() -> updateUI(currentPlan));
                }
            });
        } else {
            generateNewPlan();
        }
    }

    private boolean hasQuestionnaireChanged() {
        //use hash to check if questionnaire has changed
        SharedPreferences prefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);

        String stressor = prefs.getString("stressor", "");
        String mood = prefs.getString("mood", "");
        String time = prefs.getString("time", "");
        String interests = prefs.getString("interests", "");
        String currentHash = (stressor + mood + time + interests).hashCode() + "";

        String lastHash = prefs.getString(LAST_QUESTIONNAIRE_HASH_KEY, "");
        if (!currentHash.equals(lastHash)) {
            prefs.edit().putString(LAST_QUESTIONNAIRE_HASH_KEY, currentHash).apply();
            return true;
        }
        return false;
    }

    private void generateNewPlan() {
        SharedPreferences wellnessPrefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);

        String stressor = wellnessPrefs.getString("stressor", "");
        String mood = wellnessPrefs.getString("mood", "");
        String time = wellnessPrefs.getString("time", "");
        String interestsStr = wellnessPrefs.getString("interests", "");

        int activitiesPerDay = getActivitiesPerDay(time);

        List<String> interests = new ArrayList<>();
        if (!interestsStr.isEmpty()) {
            Collections.addAll(interests, interestsStr.split(","));
        }

        List<String> categories = new ArrayList<>();
        for (String interest : interests) {
            switch (interest) {
                case "Creativity":
                    categories.add("Boost Your Vibe");
                    break;
                case "Mindfulness":
                case "Reading":
                    categories.add("Unplug & Reset");
                    break;
                case "Sports":
                    categories.add("Move Your Mood");
                    break;
                case "Socializing":
                    categories.add("Connect & Chill");
                    break;
            }
        }

        // Default to all categories if none selected
        if (categories.isEmpty()) {
            categories.add("Move Your Mood");
            categories.add("Connect & Chill");
            categories.add("Boost Your Vibe");
            categories.add("Unplug & Reset");
        }

        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ActivityDao activityDao = db.activityDao();
            List<Activity> allActivities = new ArrayList<>();

            for (String category : categories) {
                List<Activity> categoryActivities = activityDao.getActivitiesByCategory(category);
                allActivities.addAll(categoryActivities);
            }

            List<Activity> filteredActivities = filterActivities(allActivities, stressor, time, mood, activitiesPerDay);

            List<Activity> availableActivities = excludeRecentActivities(filteredActivities);

            List<Activity> dailyPlan = selectRandomActivities(availableActivities, activitiesPerDay);

            saveCurrentPlan(dailyPlan);
            saveRecentActivities(dailyPlan);

            runOnUiThread(() -> updateUI(dailyPlan));
        });
    }

    private void updateUI(List<Activity> dailyPlan) {
        adapter.setActivityList(dailyPlan);

        int completedCount = getCompletedCount(dailyPlan);
        int streak = calculateStreak(completedCount, dailyPlan.size());

    }

    private List<Activity> filterActivities(List<Activity> activities, String stressor, String mood, String time, int activitiesPerDay) {
        List<Activity> filtered = new ArrayList<>();

        for (Activity activity : activities) {
            String name = activity.getName().toLowerCase();
            String desc = activity.getDescription().toLowerCase();
            String category = activity.getCategory();

            boolean matchesStressor = true;
            switch (stressor) {
                case "Friends":
                case "Family":
                    if (mood.equals("Sad") || mood.equals("Stressed")) {
                        matchesStressor = !category.equals("Connect & Chill");
                    }
                    break;
                case "School":
                    matchesStressor = category.equals("Unplug & Reset") || category.equals("Boost Your Vibe");
                    break;
                case "Other":
                    break;
            }

            boolean matchesMood = true;
            switch (mood) {
                case "Sad":
                    matchesMood = name.contains("write") || name.contains("doodle") || name.contains("gratitude") ||
                            name.contains("breathe") || desc.contains("positive") || desc.contains("calm");
                    break;
                case "Stressed":
                    matchesMood = category.equals("Unplug & Reset") ||
                            name.contains("breathe") || name.contains("meditation") || desc.contains("calm");
                    break;
                case "Happy":
                    matchesMood = category.equals("Boost Your Vibe") || category.equals("Connect & Chill") ||
                            name.contains("fun") || desc.contains("enjoy");
                    break;
            }

            if (matchesStressor && matchesMood) {
                filtered.add(activity);
            }
        }

        if (filtered.size() < activitiesPerDay) {
            filtered.addAll(activities);
        }

        return filtered;
    }

    private List<Activity> excludeRecentActivities(List<Activity> activities) {
        SharedPreferences prefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        String recentJson = prefs.getString(RECENT_ACTIVITIES_KEY, "[]");
        List<String> recentActivityNames = new ArrayList<>();
        long sevenDaysAgo = System.currentTimeMillis() - (DAYS_TO_AVOID_REPEATS * 24 * 60 * 60 * 1000);

        try {
            JSONArray recentArray = new JSONArray(recentJson);
            JSONArray updatedArray = new JSONArray();

            for (int i = 0; i < recentArray.length(); i++) {
                JSONObject entry = recentArray.getJSONObject(i);
                String date = entry.getString("date");
                long entryTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date).getTime();

                if (entryTime >= sevenDaysAgo) {
                    recentActivityNames.add(entry.getString("name"));
                    updatedArray.put(entry);
                }
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(RECENT_ACTIVITIES_KEY, updatedArray.toString());
            editor.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Activity> available = new ArrayList<>();
        for (Activity activity : activities) {
            if (!recentActivityNames.contains(activity.getName())) {
                available.add(activity);
            }
        }

        if (available.isEmpty()) {
            available.addAll(activities);
        }

        return available;
    }

    private void saveRecentActivities(List<Activity> activities) {
        SharedPreferences prefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        String recentJson = prefs.getString(RECENT_ACTIVITIES_KEY, "[]");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            JSONArray recentArray = new JSONArray(recentJson);

            for (Activity activity : activities) {
                JSONObject entry = new JSONObject();
                entry.put("name", activity.getName());
                entry.put("date", today);
                recentArray.put(entry);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(RECENT_ACTIVITIES_KEY, recentArray.toString());
            editor.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveCurrentPlan(List<Activity> activities) {
        SharedPreferences prefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            JSONArray planArray = new JSONArray();
            for (Activity activity : activities) {
                JSONObject entry = new JSONObject();
                entry.put("name", activity.getName());
                planArray.put(entry);
            }
            editor.putString(CURRENT_PLAN_KEY, planArray.toString());
            editor.putString(LAST_PLAN_DATE_KEY, today);
            editor.apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Activity> loadCurrentPlan() {
        // Load current plan from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(WELLNESS_PREFS, MODE_PRIVATE);
        String planJson = prefs.getString(CURRENT_PLAN_KEY, "[]");
        List<Activity> plan = new ArrayList<>();

        try {
            JSONArray planArray = new JSONArray(planJson);
            if (planArray.length() == 0) {
                return plan;
            }

            List<String> activityNames = new ArrayList<>();
            for (int i = 0; i < planArray.length(); i++) {
                JSONObject entry = planArray.getJSONObject(i);
                activityNames.add(entry.getString("name"));
            }

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ActivityDao activityDao = db.activityDao();
            List<Activity> allActivities = activityDao.getActivitiesByCategory("Boost Your Vibe");
            allActivities.addAll(activityDao.getActivitiesByCategory("Unplug & Reset"));
            allActivities.addAll(activityDao.getActivitiesByCategory("Move Your Mood"));
            allActivities.addAll(activityDao.getActivitiesByCategory("Connect & Chill"));

            for (String name : activityNames) {
                for (Activity activity : allActivities) {
                    if (activity.getName().equals(name)) {
                        plan.add(activity);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return plan;
    }

    private int getCompletedCount(List<Activity> dailyPlan) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int count = 0;

        for (Activity activity : dailyPlan) {
            String key = "last_completed_date_" + activity.getName().replaceAll("\\s+", "_").toLowerCase();
            String completionDate = prefs.getString(key, "");
            if (today.equals(completionDate)) {
                count++;
            }
        }

        prefs.edit()
                .putInt(DAILY_COMPLETION_COUNT_KEY, count)
                .putString(DAILY_COMPLETION_DATE_KEY, today)
                .apply();

        return count;
    }

    private int calculateStreak(int todayCompletedCount, int activitiesPerDay) {
        SharedPreferences prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastStreakDate = prefs.getString(LAST_STREAK_DATE_KEY, "");
        int currentStreak = prefs.getInt(STREAK_COUNT_KEY, 0);

        try {
            if (lastStreakDate.isEmpty()) {
                if (todayCompletedCount == activitiesPerDay) {
                    prefs.edit()
                            .putInt(STREAK_COUNT_KEY, 1)
                            .putString(LAST_STREAK_DATE_KEY, today)
                            .apply();
                    return 1;
                }
                return 0;
            }

            Date lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastStreakDate);
            Date todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today);
            long diffDays = (todayDate.getTime() - lastDate.getTime()) / (24 * 60 * 60 * 1000);

            if (diffDays == 1) {
                currentStreak = (todayCompletedCount == activitiesPerDay) ? currentStreak + 1 : 0;
            } else if (diffDays > 1) {
                currentStreak = (todayCompletedCount == activitiesPerDay) ? 1 : 0;
            } else {
                if (todayCompletedCount == activitiesPerDay && currentStreak == 0) {
                    currentStreak = 1;
                }
            }

            prefs.edit()
                    .putInt(STREAK_COUNT_KEY, currentStreak)
                    .putString(LAST_STREAK_DATE_KEY, today)
                    .apply();

            return currentStreak;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Activity> selectRandomActivities(List<Activity> activities, int count) {
        List<Activity> selected = new ArrayList<>();
        List<Activity> copy = new ArrayList<>(activities);
        Random random = new Random();

        for (int i = 0; i < Math.min(count, copy.size()); i++) {
            int index = random.nextInt(copy.size());
            selected.add(copy.remove(index));
        }

        if (selected.size() < count && !activities.isEmpty()) {
            selected.addAll(activities.subList(0, Math.min(count - selected.size(), activities.size())));
        }

        return selected;
    }
}
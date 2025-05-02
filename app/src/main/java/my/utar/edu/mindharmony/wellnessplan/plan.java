package my.utar.edu.mindharmony.wellnessplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.utar.edu.mindharmony.R;
import my.utar.edu.mindharmony.notification.NotificationReceiver;

public class plan extends Fragment {

    private static final String TAG = "PlanFragment";
    private static final String USER_PREFS = "UserPrefs";
    private static final String WELLNESS_PREFS = "UserWellnessPlan";
    private static final String DAILY_COMPLETION_COUNT_KEY = "daily_completion_count";
    private static final String DAILY_COMPLETION_DATE_KEY = "daily_completion_date";

    private TextView activitiesCompletion;
    private TextView totalPoints;

    public plan() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_plan, container, false);

            activitiesCompletion = view.findViewById(R.id.activities_completion);
            totalPoints = view.findViewById(R.id.total_points);

            CardView cardMood = view.findViewById(R.id.card_mood);
            CardView cardConnect = view.findViewById(R.id.card_connect);
            CardView cardUnplug = view.findViewById(R.id.card_unplug);
            CardView cardVibe = view.findViewById(R.id.card_vibe);
            Button createPlanBtn = view.findViewById(R.id.btn_create_plan);
            Button viewplan = view.findViewById(R.id.btn_view_plan);
            Button badgeBtn = view.findViewById(R.id.btn_badge);

            if (isAdded() && getActivity() != null) {
                createPlanBtn.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), Questionnaire.class));
                    }
                });

                viewplan.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), ViewPlan.class));
                    }
                });

                badgeBtn.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), Badge.class));
                    }
                });

                cardMood.setOnClickListener(v -> openCategory("Move Your Mood"));
                cardConnect.setOnClickListener(v -> openCategory("Connect & Chill"));
                cardUnplug.setOnClickListener(v -> openCategory("Unplug & Reset"));
                cardVibe.setOnClickListener(v -> openCategory("Boost Your Vibe"));

                safeUpdateUI();
                setupDailyNotification();
            }

            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: ", e);
            return inflater.inflate(R.layout.fragment_plan, container, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        safeUpdateUI();
    }

    private void safeUpdateUI() {
        try {
            if (isAdded() && getActivity() != null && activitiesCompletion != null && totalPoints != null) {
                updateUI(activitiesCompletion, totalPoints);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in safeUpdateUI: ", e);
        }
    }

    private void updateUI(TextView activitiesCompletion, TextView totalPoints) {
        try {
            if (getActivity() == null) return;

            SharedPreferences userPrefs = getActivity().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            SharedPreferences wellnessPrefs = getActivity().getSharedPreferences(WELLNESS_PREFS, Context.MODE_PRIVATE);

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String dailyCompletionDate = userPrefs.getString(DAILY_COMPLETION_DATE_KEY, "");
            int dailyCompletionCount = userPrefs.getInt(DAILY_COMPLETION_COUNT_KEY, 0);
            String time = wellnessPrefs.getString("time", "15 minutes");
            int activitiesPerDay = getActivitiesPerDay(time);

            int completedToday = today.equals(dailyCompletionDate) ? dailyCompletionCount : 0;
            activitiesCompletion.setText(completedToday + "/" + activitiesPerDay);

            int points = userPrefs.getInt("points", 0);
            totalPoints.setText(String.valueOf(points));
        } catch (Exception e) {
            Log.e(TAG, "Error in updateUI: ", e);
        }
    }

    private void setupDailyNotification() {
        try {
            if (!isAdded() || getContext() == null) return;

            Context context = requireContext();

            SharedPreferences planPrefs = context.getSharedPreferences(WELLNESS_PREFS, Context.MODE_PRIVATE);
            SharedPreferences userPrefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);

            String currentPlan = planPrefs.getString("current_plan", "");
            boolean notificationsEnabled = userPrefs.getBoolean("notifications_enabled", true);

            Log.d(TAG, "Current Plan: " + currentPlan);
            Log.d(TAG, "Notifications Enabled: " + notificationsEnabled);

            if (!currentPlan.isEmpty() && notificationsEnabled) {
                Intent intent = new Intent(context, NotificationReceiver.class);
                intent.putExtra("title", "Daily Reminder");
                intent.putExtra("message", "Check your wellness plan for today!");

                int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (alarmManager != null) {
                    Log.d(TAG, "Notification set for: " + calendar.getTime().toString());
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupDailyNotification: ", e);
        }
    }

    private int getActivitiesPerDay(String time) {
        switch (time) {
            case "5 minutes": return 2;
            case "15 minutes": return 3;
            case "30 minutes": return 5;
            default: return 3;
        }
    }

    private void openCategory(String categoryName) {
        try {
            if (!isAdded() || getActivity() == null) return;

            Intent intent = new Intent(getActivity(), CategoryActivity.class);
            intent.putExtra("category_name", categoryName);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error in openCategory: ", e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "Fragment attached");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "Fragment detached");
    }
}
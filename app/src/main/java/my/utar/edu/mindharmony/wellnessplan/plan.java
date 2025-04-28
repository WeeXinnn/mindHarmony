package my.utar.edu.mindharmony.wellnessplan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import my.utar.edu.mindharmony.R;

public class plan extends Fragment {

    private static final String USER_PREFS = "UserPrefs";
    private static final String WELLNESS_PREFS = "UserWellnessPlan";
    private static final String DAILY_COMPLETION_COUNT_KEY = "daily_completion_count";
    private static final String DAILY_COMPLETION_DATE_KEY = "daily_completion_date";

    public plan() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        CardView cardMood = view.findViewById(R.id.card_mood);
        CardView cardConnect = view.findViewById(R.id.card_connect);
        CardView cardUnplug = view.findViewById(R.id.card_unplug);
        CardView cardVibe = view.findViewById(R.id.card_vibe);
        Button createPlanBtn = view.findViewById(R.id.btn_create_plan);
        Button viewplan = view.findViewById(R.id.btn_view_plan);
        Button badgeBtn = view.findViewById(R.id.btn_badge);
        TextView activitiesCompletion = view.findViewById(R.id.activities_completion);
        TextView totalPoints = view.findViewById(R.id.total_points);

        createPlanBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), Questionnaire.class)));
        viewplan.setOnClickListener(v -> startActivity(new Intent(getActivity(), ViewPlan.class)));
        badgeBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), Badge.class)));
        cardMood.setOnClickListener(v -> openCategory("Move Your Mood"));
        cardConnect.setOnClickListener(v -> openCategory("Connect & Chill"));
        cardUnplug.setOnClickListener(v -> openCategory("Unplug & Reset"));
        cardVibe.setOnClickListener(v -> openCategory("Boost Your Vibe"));

        updateUI(activitiesCompletion, totalPoints);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(getView().findViewById(R.id.activities_completion), getView().findViewById(R.id.total_points));
    }

    private void updateUI(TextView activitiesCompletion, TextView totalPoints) {
        SharedPreferences userPrefs = getActivity().getSharedPreferences(USER_PREFS, getActivity().MODE_PRIVATE);
        SharedPreferences wellnessPrefs = getActivity().getSharedPreferences(WELLNESS_PREFS, getActivity().MODE_PRIVATE);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String dailyCompletionDate = userPrefs.getString(DAILY_COMPLETION_DATE_KEY, "");
        int dailyCompletionCount = userPrefs.getInt(DAILY_COMPLETION_COUNT_KEY, 0);
        String time = wellnessPrefs.getString("time", "15 minutes");
        int activitiesPerDay = getActivitiesPerDay(time);

        int completedToday = today.equals(dailyCompletionDate) ? dailyCompletionCount : 0;
        activitiesCompletion.setText(completedToday + "/" + activitiesPerDay);

        int points = userPrefs.getInt("points", 0);
        totalPoints.setText(String.valueOf(points));
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
        Intent intent = new Intent(getActivity(), CategoryActivity.class);
        intent.putExtra("category_name", categoryName);
        startActivity(intent);
    }
}

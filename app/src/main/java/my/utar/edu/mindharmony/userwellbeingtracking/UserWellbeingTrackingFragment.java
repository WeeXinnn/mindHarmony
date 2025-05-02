package my.utar.edu.mindharmony.userwellbeingtracking;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import my.utar.edu.mindharmony.R;

public class UserWellbeingTrackingFragment extends Fragment {

    private static final String USER_ID = "default";
    private static final int MOOD_MIN = 1;
    private static final int MOOD_MAX = 5;

    private MoodViewModel moodViewModel;
    private RecyclerView emojiRv;
    private TextView streakTv;
    private BarChart barChart;
    private MaterialButton historyJournalBtn;
    private MaterialButton calendarButton;
    private LinearLayout streakVisualIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MoodViewModelFactory factory = new MoodViewModelFactory(requireActivity().getApplication());
        moodViewModel = new ViewModelProvider(this, factory).get(MoodViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userwellbeingtracking, container, false);
        initializeViews(view);
        setupEmojiPicker();
        setupObservers();
        setupJournalHistoryButton();
        setupCalendarButton();
        return view;
    }

    private void initializeViews(View view) {
        emojiRv = view.findViewById(R.id.emojiRecyclerView);
        streakTv = view.findViewById(R.id.streakTextView);
        barChart = view.findViewById(R.id.moodBarChart);
        historyJournalBtn = view.findViewById(R.id.historyJournalButton);
        calendarButton = view.findViewById(R.id.calendarButton);
        streakVisualIndicator = view.findViewById(R.id.streakVisualIndicator);
    }

    private void setupEmojiPicker() {
        List<EmojiItem> emojis = Arrays.asList(
                new EmojiItem(R.drawable.angry, getString(R.string.angry), 1),
                new EmojiItem(R.drawable.moderate, getString(R.string.a_bit_angry), 2),
                new EmojiItem(R.drawable.normal, getString(R.string.no_feeling), 3),
                new EmojiItem(R.drawable.abithappy, getString(R.string.a_bit_happy), 4),
                new EmojiItem(R.drawable.veryhappy, getString(R.string.very_happy), 5)
        );

        EmojiAdapter adapter = new EmojiAdapter(emojis, emoji -> {
            Intent intent = new Intent(requireActivity(), NewJournalActivity.class);
            intent.putExtra("emoji_res", emoji.drawableRes);
            intent.putExtra("emoji_name", emoji.name);
            intent.putExtra("emoji_value", emoji.value);
            startActivity(intent);
        });

        emojiRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        emojiRv.setAdapter(adapter);
    }

    private void setupJournalHistoryButton() {
        historyJournalBtn.setOnClickListener(view -> {
            try {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireActivity(), JournalActivity.class);

                    ActivityOptions options = ActivityOptions.makeCustomAnimation(
                            getActivity(),
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    );

                    startActivity(intent, options.toBundle());
                    historyJournalBtn.setEnabled(false);
                    historyJournalBtn.postDelayed(() -> historyJournalBtn.setEnabled(true), 500);
                }
            } catch (ActivityNotFoundException e) {
                Log.e("JournalHistory", "JournalActivity not found", e);
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Could not open journal history", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalStateException e) {
                Log.e("JournalHistory", "Fragment not attached", e);
            } catch (Exception e) {
                Log.e("JournalHistory", "Unexpected error", e);
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupCalendarButton() {
        calendarButton.setOnClickListener(view -> {
            try {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireActivity(), CalendarActivity.class);

                    ActivityOptions options = ActivityOptions.makeCustomAnimation(
                            getActivity(),
                            R.anim.slide_in_right,
                            R.anim.slide_out_left
                    );

                    startActivity(intent, options.toBundle());
                    calendarButton.setEnabled(false);
                    calendarButton.postDelayed(() -> calendarButton.setEnabled(true), 500);
                }
            } catch (ActivityNotFoundException e) {
                Log.e("Calendar", "CalendarActivity not found", e);
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Calendar feature not available", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("Calendar", "Error opening calendar", e);
                if (isAdded() && getActivity() != null) {
                    Toast.makeText(getActivity(), "Error opening calendar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupObservers() {
        moodViewModel.getAllMoodEntries(USER_ID).observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                updateStreakDisplay(entries);
                setupBarChart(entries);
            } else {
                streakTv.setText("Streak: 0 days");
                setupEmptyChart();
                updateStreakVisualIndicator(0);
            }
        });
    }

    private void updateStreakDisplay(List<MoodEntry> entries) {
        int streak = calculateStreak(entries);
        streakTv.setText("Streak: " + streak + " days");
        updateStreakVisualIndicator(streak);
    }

    private void updateStreakVisualIndicator(int streak) {
        streakVisualIndicator.removeAllViews();

        // Define 7 different empty color drawables
        int[] emptyDrawables = {
                R.drawable.circle_border_g,
                R.drawable.circle_border_ly,
                R.drawable.circle_border_grey,
                R.drawable.circle_border_lp,
                R.drawable.circle_border_p,
                R.drawable.circle_border_lb,
                R.drawable.circle_border_lg
        };
        int[] qubyDrawables = {
                R.drawable.quby_1,
                R.drawable.quby_2,
                R.drawable.quby_3,
                R.drawable.quby_4,
                R.drawable.quby_5,
                R.drawable.quby_6,
                R.drawable.quby_7
        };
        Random random = new Random();


        for (int i = 0; i < 7; i++) {
            View dot = new View(getContext());
            int size = getResources().getDimensionPixelSize(R.dimen.streak_dot_size);
            int margin = getResources().getDimensionPixelSize(R.dimen.streak_dot_margin);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);

            if (i < streak) {
                int randomDrawable = qubyDrawables[random.nextInt(qubyDrawables.length)];
                dot.setBackgroundResource(randomDrawable);// filled indicator
            } else {
                dot.setBackgroundResource(emptyDrawables[i]); // colored empty circle
            }

            streakVisualIndicator.addView(dot);
        }
    }


    private int calculateStreak(List<MoodEntry> entries) {
        if (entries.isEmpty()) return 0;

        entries.sort((e1, e2) -> Long.compare(e2.date, e1.date));

        Calendar currentCal = Calendar.getInstance();
        normalizeToStartOfDay(currentCal);

        int streak = 0;
        Calendar entryCal = Calendar.getInstance();

        for (MoodEntry entry : entries) {
            entryCal.setTimeInMillis(entry.date);
            normalizeToStartOfDay(entryCal);

            long diffDays = (currentCal.getTimeInMillis() - entryCal.getTimeInMillis()) / (1000 * 60 * 60 * 24);

            if (diffDays == streak) {
                streak++;
            } else if (diffDays > streak) {
                break;
            }
        }
        return Math.min(streak, 7); // Limit to 7 dots
    }

    private void normalizeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupBarChart(List<MoodEntry> entries) {
        Map<String, List<MoodEntry>> entriesByDay = groupEntriesByDayOfWeek(entries);
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

        for (int i = 0; i < labels.size(); i++) {
            String day = labels.get(i);
            barEntries.add(new BarEntry(i,
                    entriesByDay.containsKey(day) ? calculateAverageRating(entriesByDay.get(day)) : 0));
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Mood Rating");
        dataSet.setColors(getMoodColors(barEntries));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setHighlightPerTapEnabled(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(MOOD_MIN);
        barChart.getAxisLeft().setAxisMaximum(MOOD_MAX);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisLeft().setTextColor(Color.BLACK);
        barChart.getAxisLeft().setTextSize(12f);

        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupEmptyChart() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, 0));
        }

        BarDataSet dataSet = new BarDataSet(entries, "No Data");
        dataSet.setColor(Color.LTGRAY);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private Map<String, List<MoodEntry>> groupEntriesByDayOfWeek(List<MoodEntry> entries) {
        Map<String, List<MoodEntry>> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());

        for (MoodEntry entry : entries) {
            String day = sdf.format(new Date(entry.date));
            if (!map.containsKey(day)) {
                map.put(day, new ArrayList<>());
            }
            map.get(day).add(entry);
        }
        return map;
    }

    private float calculateAverageRating(List<MoodEntry> entries) {
        if (entries.isEmpty()) return 0;
        float sum = 0;
        for (MoodEntry entry : entries) {
            sum += entry.rating;
        }
        return sum / entries.size();
    }

    private List<Integer> getMoodColors(List<BarEntry> entries) {
        List<Integer> colors = new ArrayList<>();
        for (BarEntry entry : entries) {
            colors.add(getMoodColor((int) entry.getY()));
        }
        return colors;
    }

    private int getMoodColor(int rating) {
        switch (rating) {
            case 1: return getResources().getColor(R.color.mood_1);
            case 2: return getResources().getColor(R.color.mood_2);
            case 3: return getResources().getColor(R.color.mood_3);
            case 4: return getResources().getColor(R.color.mood_4);
            case 5: return getResources().getColor(R.color.mood_5);
            default: return Color.LTGRAY;
        }
    }
}
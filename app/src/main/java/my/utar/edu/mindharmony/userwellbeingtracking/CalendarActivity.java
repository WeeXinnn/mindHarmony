package my.utar.edu.mindharmony.userwellbeingtracking;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import my.utar.edu.mindharmony.R;

public class CalendarActivity extends AppCompatActivity {

    private GridLayout calendarGrid;
    private LinearLayout moodSummary;
    private MoodViewModel moodViewModel;
    private List<MoodEntry> moodEntries = new ArrayList<>();
    private Calendar currentCalendar;

    private TextView monthText;
    private ImageView prevMonth, nextMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        MoodViewModelFactory factory = new MoodViewModelFactory(getApplication());
        moodViewModel = new ViewModelProvider(this, factory).get(MoodViewModel.class);

        calendarGrid = findViewById(R.id.calendarGrid);
        moodSummary = findViewById(R.id.mood_summary);
        monthText = findViewById(R.id.monthText);
        prevMonth = findViewById(R.id.prevMonth);
        nextMonth = findViewById(R.id.nextMonth);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        currentCalendar = Calendar.getInstance();

        prevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        moodViewModel.getAllMoodEntries().observe(this, entries -> {
            moodEntries = entries;
            updateCalendar();
        });
    }
    private void clearMoodCounts() {
        ((TextView) findViewById(R.id.mood_angry_count)).setText("0");
        ((TextView) findViewById(R.id.mood_moderate_count)).setText("0");
        ((TextView) findViewById(R.id.mood_normal_count)).setText("0");
        ((TextView) findViewById(R.id.mood_abithappy_count)).setText("0");
        ((TextView) findViewById(R.id.mood_veryhappy_count)).setText("0");
    }


    private void updateCalendar() {
        calendarGrid.removeAllViews();
        clearMoodCounts();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthText.setText(sdf.format(currentCalendar.getTime()));

        calendarGrid.setColumnCount(7);
        calendarGrid.setRowCount(7);

        Map<Integer, Integer> moodCounts = new HashMap<>();

        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        for (String label : dayLabels) {
            calendarGrid.addView(createDayLabel(label));
        }

        Calendar calendar = (Calendar) currentCalendar.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOffset; i++) {
            calendarGrid.addView(createEmptyCell());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            calendarGrid.addView(createDateCell(day, moodCounts));
        }

        createMoodSummary(moodCounts);
    }

    private TextView createDayLabel(String text) {
        TextView dayLabel = new TextView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);

        dayLabel.setLayoutParams(params);
        dayLabel.setText(text);
        dayLabel.setGravity(Gravity.CENTER);
        dayLabel.setTextColor(Color.BLACK);
        dayLabel.setTextSize(14);

        return dayLabel;
    }

    private View createEmptyCell() {
        View emptyView = new View(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        emptyView.setLayoutParams(params);
        return emptyView;
    }

    private FrameLayout createDateCell(int day, Map<Integer, Integer> moodCounts) {
        FrameLayout cell = new FrameLayout(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(4, 4, 4, 4);
        cell.setLayoutParams(params);

        // Array of 7 different circle border color resources
        int[] circleColors = {
                R.drawable.circle_border_g,  // Color 1
                R.drawable.circle_border_ly,  // Color 2
                R.drawable.circle_border_grey,  // Color 3
                R.drawable.circle_border_lp,  // Color 4
                R.drawable.circle_border_p,  // Color 5
                R.drawable.circle_border_lb,  // Color 6
                R.drawable.circle_border_lg  // Color 7
        };

        // Determine the circle color based on mood or other logic
        int colorIndex = day % circleColors.length; // Example: Cycle through the colors based on day

        // Create a container to stack the circle and the date
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        // Create circle container
        LinearLayout circle = new LinearLayout(this);
        FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(dpToPx(40), dpToPx(40));
        circleParams.gravity = Gravity.CENTER;
        circle.setLayoutParams(circleParams);
        circle.setBackgroundResource(circleColors[colorIndex]);  // Apply color
        circle.setGravity(Gravity.CENTER);
        circle.setOrientation(LinearLayout.VERTICAL);

        // Add the date text below the circle
        TextView dateText = new TextView(this);
        dateText.setText(String.valueOf(day));
        dateText.setTextSize(12);
        dateText.setTextColor(Color.BLACK);
        dateText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = 8; // Add some margin between the circle and the text
        dateText.setLayoutParams(dateParams);

        // Add the date text to the container (below the circle)
        container.addView(circle);
        container.addView(dateText);

        // Check if there is a mood entry for this day
        MoodEntry entry = findMoodEntryForDay(day);
        if (entry != null) {
            // Add emoji image based on mood entry rating
            ImageView emoji = new ImageView(this);
            LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
            emoji.setLayoutParams(emojiParams);
            emoji.setImageResource(getEmojiResource(entry.getRating()));  // Get emoji resource based on rating
            circle.addView(emoji);

            // Track mood count
            moodCounts.put(entry.getRating(), moodCounts.getOrDefault(entry.getRating(), 0) + 1);
        }

        // Add the container to the cell
        cell.addView(container);
        return cell;
    }


    private void createMoodSummary(Map<Integer, Integer> moodCounts) {
        updateMoodCount(R.id.mood_angry_count, 1, moodCounts);
        updateMoodCount(R.id.mood_moderate_count, 2, moodCounts);
        updateMoodCount(R.id.mood_normal_count, 3, moodCounts);
        updateMoodCount(R.id.mood_abithappy_count, 4, moodCounts);
        updateMoodCount(R.id.mood_veryhappy_count, 5, moodCounts);
    }

    private void updateMoodCount(int countViewId, int moodLevel, Map<Integer, Integer> moodCounts) {
        TextView countText = findViewById(countViewId);
        int count = moodCounts.getOrDefault(moodLevel, 0);
        countText.setText(String.valueOf(count));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private MoodEntry findMoodEntryForDay(int day) {
        Calendar targetDate = (Calendar) currentCalendar.clone();
        targetDate.set(Calendar.DAY_OF_MONTH, day);
        normalizeToStartOfDay(targetDate);

        for (MoodEntry entry : moodEntries) {
            Calendar entryDate = Calendar.getInstance();
            entryDate.setTimeInMillis(entry.getDate());
            normalizeToStartOfDay(entryDate);

            if (entryDate.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                    entryDate.get(Calendar.MONTH) == targetDate.get(Calendar.MONTH) &&
                    entryDate.get(Calendar.DAY_OF_MONTH) == targetDate.get(Calendar.DAY_OF_MONTH)) {
                return entry;
            }
        }
        return null;
    }

    private void normalizeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private int getEmojiResource(int rating) {
        switch (rating) {
            case 1: return R.drawable.angry;
            case 2: return R.drawable.moderate;
            case 3: return R.drawable.normal;
            case 4: return R.drawable.abithappy;
            case 5: return R.drawable.veryhappy;
            default: return 0;
        }
    }
}

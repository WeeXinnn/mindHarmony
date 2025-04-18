package my.utar.edu.mindharmony.wellnessplan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.utar.edu.mindharmony.R;
import my.utar.edu.mindharmony.database.AppDatabase;
import my.utar.edu.mindharmony.dao.ActivityDao;
import my.utar.edu.mindharmony.models.Activity;

public class CategoryActivity extends AppCompatActivity {
    private static final String TAG = "CategoryActivity";
    public static final String CATEGORY_KEY = "category_name";
    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private TextView title;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryName = getIntent().getStringExtra(CATEGORY_KEY);
        title = findViewById(R.id.categoryTitle);
        title.setText("Category: " + categoryName);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ActivityAdapter();
        recyclerView.setAdapter(adapter);

        loadActivitiesByCategory(categoryName);
    }

    private void loadActivitiesByCategory(String category) {
        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ActivityDao activityDao = db.activityDao();
            List<Activity> activityList = activityDao.getActivitiesByCategory(category);
            Log.d(TAG, "Loaded " + activityList.size() + " activities for category: " + category);
            runOnUiThread(() -> adapter.setActivityList(activityList));
        });
    }
}
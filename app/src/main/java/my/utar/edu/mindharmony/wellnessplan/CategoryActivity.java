package my.utar.edu.mindharmony.wellnessplan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        TextView categoryTitle = findViewById(R.id.categoryTitle);
        String categoryName = getIntent().getStringExtra("category_name");
        if (categoryName != null) {
            categoryTitle.setText(categoryName);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ActivityAdapter();
        recyclerView.setAdapter(adapter);

        loadActivitiesByCategory(categoryName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String categoryName = getIntent().getStringExtra("category_name");
        loadActivitiesByCategory(categoryName);
    }


    private void loadActivitiesByCategory(String category) {
        //load database in background
        AsyncTask.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ActivityDao activityDao = db.activityDao();
            List<Activity> activityList = activityDao.getActivitiesByCategory(category);
            Log.d(TAG, "Loaded " + activityList.size() + " activities for category: " + category);
            runOnUiThread(() -> adapter.setActivityList(activityList));
        });
    }
}
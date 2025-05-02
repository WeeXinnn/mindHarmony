package my.utar.edu.mindharmony.userwellbeingtracking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.utar.edu.mindharmony.R;

public class JournalActivity extends AppCompatActivity {

    private ListView listView;
    private MoodViewModel moodViewModel;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        listView = findViewById(R.id.journalListView);
        emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        MoodDao dao = MoodDatabase.getInstance(this).moodDao();
        MoodViewModel vm = new ViewModelProvider(this, new MoodViewModelFactory(this)).get(MoodViewModel.class);


        vm.getAllEntries().observe(this, allEntries -> {
            if (allEntries == null || allEntries.isEmpty()) {
                emptyView.setText("No journal entries found");
                return;
            }
            Collections.sort(allEntries, (a, b) -> Long.compare(b.date, a.date));

            JournalEntryAdapter adapter = new JournalEntryAdapter(
                    this,
                    R.layout.item_journal_entry,
                    allEntries
            );
            listView.setAdapter(adapter);
        });
    }
}

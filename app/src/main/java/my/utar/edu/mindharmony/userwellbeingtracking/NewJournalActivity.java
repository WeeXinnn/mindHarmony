package my.utar.edu.mindharmony.userwellbeingtracking;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import my.utar.edu.mindharmony.R;

public class NewJournalActivity extends AppCompatActivity {

    private int emojiRes, emojiValue;
    private String emojiName;

    private AppCompatImageView selectedEmojiView;
    private ChipGroup quickTextChips;
    private TextInputEditText noteEt;
    private Button saveBtn;

    private boolean allowEmojiChange = true; // NEW FLAG

    private final int[] emojiImageIds = {
            R.drawable.veryhappy, R.drawable.abithappy, R.drawable.normal, R.drawable.moderate, R.drawable.angry
    };

    private final String[] emojiNames = {
            "Very Happy", "A bit Happy", "Normal", "A bit Angry", "Angry"
    };

    private final int[] emojiValues = {5, 4, 3, 2, 1};

    @SuppressLint({"SetTextI18n", "WrongViewCast"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_journal);

        selectedEmojiView = findViewById(R.id.selectedEmojiView);
        quickTextChips = findViewById(R.id.quickTextChips);
        noteEt = findViewById(R.id.noteEditText);
        saveBtn = findViewById(R.id.saveBtn);

        // Get intent extras
        Intent i = getIntent();
        emojiRes = i.getIntExtra("emoji_res", R.drawable.veryhappy);
        emojiName = i.getStringExtra("emoji_name");
        emojiValue = i.getIntExtra("emoji_value", 5);


        selectedEmojiView.setImageResource(emojiRes);
        selectedEmojiView.setScaleX(1.3f);
        selectedEmojiView.setScaleY(1.3f);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());



        // Set up quick text suggestions
        String[] suggestions = {
                "I feel",
                "Today I",
                "Because I",
                "My mood is",
                "I am grateful for",
                "Something that made me smile today was",
                "I struggled with",
                "I overcame",
                "I learned that",
                "I wish I had",
                "I am proud of",
                "I need to improve",
                "My goals for tomorrow are",
                "I felt supported when",
                "What made today special was"
        };

        for (String txt : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(txt);
            chip.setOnClickListener(v -> {
                String cur = noteEt.getText().toString();
                noteEt.setText(cur + (cur.isEmpty() ? "" : " ") + txt + " ");
                noteEt.setSelection(noteEt.getText().length());
            });
            quickTextChips.addView(chip);
        }

        saveBtn.setOnClickListener(v -> onSaveEntry());
    }







    private void onSaveEntry() {
        String notes = noteEt.getText().toString().trim();
        if (notes.isEmpty()) {
            Toast.makeText(this, "Please write something before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        MoodEntry entry = new MoodEntry();
        entry.setUserId("default");
        entry.setDate(new Date().getTime());
        entry.setRating(emojiValue);
        entry.setEmoji(emojiName);
        entry.setNote(notes);

        new Thread(() -> {
            try {
                MoodDatabase.getInstance(NewJournalActivity.this).moodDao().insert(entry);
                runOnUiThread(() -> {
                    Toast.makeText(NewJournalActivity.this, "Journal saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e("NewJournal", "Error saving entry", e);
                runOnUiThread(() ->
                        Toast.makeText(NewJournalActivity.this, "Error saving journal", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}

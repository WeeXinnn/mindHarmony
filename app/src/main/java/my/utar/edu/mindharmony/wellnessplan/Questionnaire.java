package my.utar.edu.mindharmony.wellnessplan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

import my.utar.edu.mindharmony.R;

public class Questionnaire extends AppCompatActivity {

    RadioGroup stressorGroup, moodGroup, timeGroup;
    CheckBox interestSports, interestSocializing, interestCreativity, interestMindfulness, interestOutdoor;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        stressorGroup = findViewById(R.id.stressorGroup);
        moodGroup = findViewById(R.id.moodGroup);
        timeGroup = findViewById(R.id.timeGroup);

        interestSports = findViewById(R.id.interestSports);
        interestSocializing = findViewById(R.id.interestSocializing);
        interestCreativity = findViewById(R.id.interestCreativity);
        interestMindfulness = findViewById(R.id.interestMindfulness);
        interestOutdoor = findViewById(R.id.interestOutdoor);

        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserPlan();
            }
        });
    }

    private void saveUserPlan() {
        String stressor = getSelectedRadioText(stressorGroup);
        String mood = getSelectedRadioText(moodGroup);
        String time = getSelectedRadioText(timeGroup);

        ArrayList<String> interests = new ArrayList<>();
        if (interestSports.isChecked()) interests.add("Sports");
        if (interestSocializing.isChecked()) interests.add("Socializing");
        if (interestCreativity.isChecked()) interests.add("Creativity");
        if (interestMindfulness.isChecked()) interests.add("Mindfulness");
        if (interestOutdoor.isChecked()) interests.add("Outdoor");

        if (stressor.isEmpty() || mood.isEmpty() || time.isEmpty() || interests.isEmpty()) {
            Toast.makeText(this, "Please complete all the question.", Toast.LENGTH_SHORT).show();
            return;
        }

        String interestStr = android.text.TextUtils.join(",", interests);


        SharedPreferences prefs = getSharedPreferences("UserWellnessPlan", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("stressor", stressor);
        editor.putString("mood", mood);
        editor.putString("interests", interestStr);
        editor.putString("time", time);
        editor.apply();

        Toast.makeText(this, "Plan Saved! Activities will be personalized for you.", Toast.LENGTH_SHORT).show();

        finish();
    }

    private String getSelectedRadioText(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            return selectedButton.getText().toString();
        }
        return "";
    }
}

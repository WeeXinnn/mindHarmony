package my.utar.edu.mindharmony.userwellbeingtracking;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;

public class MoodRepository {

    private MoodDao moodDao;

    public MoodRepository(Application application) {
        MoodDatabase db = MoodDatabase.getInstance(application);
        moodDao = db.moodDao();
    }

    public LiveData<List<MoodEntry>> getAllMoodEntries(String userId) {
        return moodDao.getAll(userId);
    }
}

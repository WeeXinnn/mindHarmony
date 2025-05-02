package my.utar.edu.mindharmony.userwellbeingtracking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;


public class MoodViewModel extends ViewModel {
    private final MoodDao moodDao;

    public MoodViewModel(MoodDao moodDao) {
        this.moodDao = moodDao;
    }

    public LiveData<List<MoodEntry>> getAllEntries() {
        return moodDao.getAllEntries();
    }


    public LiveData<List<MoodEntry>> getAllMoodEntries(String username) {
        return moodDao.getAll(username);
    }
    public LiveData<List<MoodEntry>> getAllMoodEntries() {
        return moodDao.getAllEntries();
    }

}

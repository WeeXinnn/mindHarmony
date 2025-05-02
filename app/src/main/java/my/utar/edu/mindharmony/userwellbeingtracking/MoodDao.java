package my.utar.edu.mindharmony.userwellbeingtracking;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MoodDao {
    @Insert
    void insert(MoodEntry entry);

    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    MoodEntry getByDate(long date);  // Changed to long for timestamp

    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT 1")
    LiveData<MoodEntry> getLatest();

    @Query("SELECT * FROM mood_entries WHERE user_id = :userId ORDER BY date DESC")
    LiveData<List<MoodEntry>> getAll(String userId);

    // Updated to handle long timestamps
    @Query("SELECT * FROM mood_entries WHERE user_id = :userId AND date BETWEEN :start AND :end")
    List<MoodEntry> getForPeriod(String userId, long start, long end); // Changed to long

    @Query("SELECT * FROM mood_entries")
    LiveData<List<MoodEntry>> getAllEntries();

    @Update
    void update(MoodEntry entry);

}

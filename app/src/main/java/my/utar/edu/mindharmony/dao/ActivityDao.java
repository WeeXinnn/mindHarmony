package my.utar.edu.mindharmony.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import my.utar.edu.mindharmony.models.Activity;

@Dao
public interface ActivityDao {
    @Insert
    void insert(Activity activity);

    @Insert
    void insertAll(List<Activity> activities);

    @Query("SELECT * FROM activities WHERE category = :categoryName")
    List<Activity> getActivitiesByCategory(String categoryName);
}
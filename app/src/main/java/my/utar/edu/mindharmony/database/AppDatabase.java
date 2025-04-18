package my.utar.edu.mindharmony.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import my.utar.edu.mindharmony.dao.ActivityDao;
import my.utar.edu.mindharmony.models.Activity;

@Database(entities = {Activity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ActivityDao activityDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "activities.db")
//                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                             .createFromAsset("activities.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
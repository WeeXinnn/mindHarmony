package my.utar.edu.mindharmony.userwellbeingtracking;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

@Database(entities = {MoodEntry.class}, version = 8)
@TypeConverters(Converters.class)
public abstract class MoodDatabase extends RoomDatabase {
    public abstract MoodDao moodDao();

    private static volatile MoodDatabase INSTANCE;
    public static synchronized MoodDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MoodDatabase.class, "mood_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}

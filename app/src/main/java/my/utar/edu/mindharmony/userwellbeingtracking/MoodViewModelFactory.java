package my.utar.edu.mindharmony.userwellbeingtracking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class MoodViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public MoodViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MoodDao dao = MoodDatabase.getInstance(context).moodDao();
        return (T) new MoodViewModel(dao);
    }
}

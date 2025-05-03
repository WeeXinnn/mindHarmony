package my.utar.edu.mindharmony.mindfulness;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import my.utar.edu.mindharmony.R;

public class MoodBoosterFragment extends Fragment {

    public MoodBoosterFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) inflate the layout
        View v = inflater.inflate(R.layout.fragment_mood_booster, container, false);

        // 2) wire up the toolbar back button
        Toolbar toolbar = v.findViewById(R.id.toolbarMood);
        toolbar.setNavigationOnClickListener( click -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        return v;
    }
}

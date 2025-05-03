package my.utar.edu.mindharmony.mindfulness;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import my.utar.edu.mindharmony.R;

public class MeditationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meditation, container, false);

        // Back arrow
        v.findViewById(R.id.btnBack)
                .setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());

        // Card click-listeners…
        int[] cardIds = { R.id.cardHarmony, R.id.cardReflection, R.id.cardPeace, R.id.cardLovingKindness };
        String[] categories = { "Harmony", "Reflection", "Peace", "Loving Kindness" };
        for (int i = 0; i < 4; i++) {
            final String cat = categories[i];
            v.findViewById(cardIds[i]).setOnClickListener(view -> openTimer(cat));
        }

        return v;
    }

    private void openTimer(String category) {
        Bundle b = new Bundle();
        b.putString("category", category);

        TimerFragment tf = new TimerFragment();
        tf.setArguments(b);

        FragmentTransaction ft = requireActivity()
                .getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, tf)
                .addToBackStack(null)
                .commit();
    }
}

package my.utar.edu.mindharmony.mindfulness;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import my.utar.edu.mindharmony.R;

public class FocusFragment extends Fragment {

    public FocusFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_focus, container, false);

        // 1) toolbar back
        Toolbar tb = v.findViewById(R.id.toolbarFocus);
        tb.setNavigationOnClickListener(c ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // 2) for each card, set icon + label + click
        setupCard(v, R.id.cardStudy, "Study / Work", new StudyWorkFragment());
        setupCard(v, R.id.cardMood, "Mood Booster", new MoodBoosterFragment());
        setupCard(v, R.id.cardRelax,"Relax / Unwind", new RelaxFragment());

        return v;
    }

    private void setupCard(View root,
                           int cardId,
                           String label,
                           Fragment target) {
        View card = root.findViewById(cardId);

        // set the text
        TextView tv = card.findViewById(R.id.tvFocusLabel);
        tv.setText(label);

        // click → open fragment
        card.setOnClickListener(c -> {
            FragmentTransaction ft = requireActivity()
                    .getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, target)
                    .addToBackStack(null)
                    .commit();
        });
    }
}

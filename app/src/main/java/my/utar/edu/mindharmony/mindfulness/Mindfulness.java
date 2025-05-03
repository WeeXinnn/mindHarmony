package my.utar.edu.mindharmony.mindfulness;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import my.utar.edu.mindharmony.R;

public class Mindfulness extends Fragment {

    public Mindfulness() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_mindfulness, container, false);

        ((ImageView) v.findViewById(R.id.includeMeditation)
                .findViewById(R.id.imgCard))
                .setImageResource(R.drawable.img_meditation);

        ((ImageView) v.findViewById(R.id.includeFocus)
                .findViewById(R.id.imgCard))
                .setImageResource(R.drawable.img_focus);

        ((ImageView)v.findViewById(R.id.includeMeditation)
                .findViewById(R.id.imgCartoon))
                .setImageResource(R.drawable.meditation_cartoon);

        ((ImageView)v.findViewById(R.id.includeFocus)
                .findViewById(R.id.imgCartoon))
                .setImageResource(R.drawable.focus_cartoon);

        // Card: Meditation
        v.findViewById(R.id.cardMeditation).setOnClickListener(view ->
                openFragment(new MeditationFragment()));

        // Card: Focus Mode
        v.findViewById(R.id.cardFocus).setOnClickListener(view ->
                openFragment(new FocusFragment()));

        return v;
    }

    private void openFragment(Fragment f) {
        FragmentTransaction ft = requireActivity()
                .getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, f)      // <-- your Activity’s FrameLayout id
                .addToBackStack(null)
                .commit();
    }
}

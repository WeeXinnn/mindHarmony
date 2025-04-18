package my.utar.edu.mindharmony.wellnessplan;

import android.content.Intent;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import my.utar.edu.mindharmony.wellnessplan.Questionnaire;

import my.utar.edu.mindharmony.R;

public class plan extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public plan() {
        // Required empty public constructor
    }

    public static plan newInstance(String param1, String param2) {
        plan fragment = new plan();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        // Find CardViews from the layout
        CardView cardMood = view.findViewById(R.id.card_mood);
        CardView cardConnect = view.findViewById(R.id.card_connect);
        CardView cardUnplug = view.findViewById(R.id.card_unplug);
        CardView cardVibe = view.findViewById(R.id.card_vibe);

        Button createPlanBtn = view.findViewById(R.id.btn_create_plan);
        Button viewplan = view.findViewById(R.id.btn_view_plan);
        Button badgeBtn = view.findViewById(R.id.btn_badge);

        // Set onClick listeners
        cardMood.setOnClickListener(v -> openCategory("Move Your Mood"));
        cardConnect.setOnClickListener(v -> openCategory("Connect & Chill"));
        cardUnplug.setOnClickListener(v -> openCategory("Unplug & Reset"));
        cardVibe.setOnClickListener(v -> openCategory("Boost Your Vibe"));

        createPlanBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Questionnaire.class);
            startActivity(intent);
        });

        return view;
    }

    private void openCategory(String categoryName) {
        Intent intent = new Intent(getActivity(), CategoryActivity.class);
        intent.putExtra("category_name", categoryName);
        startActivity(intent);
    }
}

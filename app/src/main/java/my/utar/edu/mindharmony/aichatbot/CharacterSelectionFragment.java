package my.utar.edu.mindharmony.aichatbot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.airbnb.lottie.LottieAnimationView;
import my.utar.edu.mindharmony.R;

public class CharacterSelectionFragment extends Fragment {
    private LottieAnimationView characterAnimation;
    private TextView characterNameText;
    private TextView descriptionText;
    private ImageButton leftArrowButton;
    private ImageButton rightArrowButton;
    private Button letsChatButton;

    private TextView titleText;


    // Available character resources
    private final int[] characterAnimations = {
            R.raw.dog1,
            R.raw.dog2,
            R.raw.dog3
    };

    private final String[] characterNames = {
            "Buddy",
            "Max",
            "Charlie"
    };

    private final String[] characterDescriptions = {
            "Your friendly emotional support companion",
            "A playful buddy to brighten your day",
            "A loyal friend who's always there for you"
    };

    private int currentCharacterIndex = 0;

    public CharacterSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_character_selection, container, false);

        // Initialize UI components
        characterAnimation = view.findViewById(R.id.characterAnimation);
        characterNameText = view.findViewById(R.id.characterNameText);
        descriptionText = view.findViewById(R.id.descriptionText);
        leftArrowButton = view.findViewById(R.id.leftArrowButton);
        rightArrowButton = view.findViewById(R.id.rightArrowButton);
        letsChatButton = view.findViewById(R.id.letsChatButton);

        TextView titleText = view.findViewById(R.id.titleText);
        titleText.setText("👋 Hi, " + "userName");

        // Set the images folder for Lottie
        characterAnimation.setImageAssetsFolder("images/");

        // Set initial character
        updateCharacterDisplay();

        // Set up arrow button listeners
        leftArrowButton.setOnClickListener(v -> {
            currentCharacterIndex = (currentCharacterIndex - 1 + characterAnimations.length) % characterAnimations.length;
            updateCharacterDisplay();
        });

        rightArrowButton.setOnClickListener(v -> {
            currentCharacterIndex = (currentCharacterIndex + 1) % characterAnimations.length;
            updateCharacterDisplay();
        });

        // Set up chat button listener
        letsChatButton.setOnClickListener(v -> {
            // Save the selected character resource ID to use in the chatbot
            ChatbotPreferences.saveSelectedCharacter(requireContext(), characterAnimations[currentCharacterIndex]);

            // Navigate to chatbot fragment
            chatbot chatbotFragment = new chatbot();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, chatbotFragment);
            transaction.commit();
        });

        return view;
    }

    private void updateCharacterDisplay() {
        // Update the animation
        characterAnimation.setAnimation(characterAnimations[currentCharacterIndex]);
        characterAnimation.playAnimation();

        // Update the character name
        characterNameText.setText(characterNames[currentCharacterIndex]);

        // Update description if available
        if (currentCharacterIndex < characterDescriptions.length) {
            descriptionText.setText(characterDescriptions[currentCharacterIndex]);
        }
    }
}
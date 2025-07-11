package my.utar.edu.mindharmony.aichatbot;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.android.material.imageview.ShapeableImageView;
import my.utar.edu.mindharmony.R;
import my.utar.edu.mindharmony.profile.profile;

public class CharacterSelectionFragment extends Fragment {
    private LottieAnimationView characterAnimation;
    private TextView characterNameText;
    private TextView descriptionText;
    private ImageButton leftArrowButton;
    private ImageButton rightArrowButton;
    private Button letsChatButton;
    private TextView titleText;
    private ShapeableImageView profileImage;

    // SharedPreferences constants (same as in profile fragment)
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";

    // Available character resources
    private final int[] characterAnimations = {
            R.raw.dog1,
            R.raw.dog2,
            R.raw.dog3
    };

    private final String[] characterNames = {
            "Bella",
            "Luna",
            "Buddy"
    };

    private final String[] characterDescriptions = {
            "Sweet, gentle, and always encouraging. Bella makes you feel cared for, like you're chatting with someone who truly believes in you.",
            "Calm, supportive, and a great listener. Luna helps you slow down, reflect, and feel understood.",
            "Playful, funny, and full of good vibes. Buddy brings jokes, emojis, and a smile to every chat."
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
        titleText = view.findViewById(R.id.titleText);
        profileImage = view.findViewById(R.id.profile_image);

        // Get the username from SharedPreferences
        // Use the same default value as in the profile class
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "User12345678");

        // Set the greeting with the username
        titleText.setText("👋 Hi, " + username);

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

        // Set up profile image click listener to navigate to profile fragment
        profileImage.setOnClickListener(v -> {
            profile profileFragment = new profile();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Set up chat button listener
        letsChatButton.setOnClickListener(v -> {
            // Save the selected character resource ID to use in the chatbot
            ChatbotPreferences.saveSelectedCharacter(requireContext(), characterAnimations[currentCharacterIndex]);

            // Save the character name and description
            ChatbotPreferences.saveCharacterName(requireContext(), characterNames[currentCharacterIndex]);
            ChatbotPreferences.saveCharacterDescription(requireContext(), characterDescriptions[currentCharacterIndex]);

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

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "User12345678");
        titleText.setText("👋 Hi, " + username);
    }
}
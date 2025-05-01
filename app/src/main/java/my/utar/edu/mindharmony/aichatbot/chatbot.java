package my.utar.edu.mindharmony.aichatbot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import com.airbnb.lottie.LottieAnimationView;
import my.utar.edu.mindharmony.BuildConfig;
import my.utar.edu.mindharmony.MainActivity;
import my.utar.edu.mindharmony.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class chatbot extends Fragment {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final String TAG = "Chatbot";
    private EditText questionInput;
    private ImageButton sendButton;
    private ImageButton voiceInputButton;
    private ImageButton keyboardButton;
    private ImageButton photoButton;
    private ImageButton photoButtonAlt;
    private ImageButton micButtonAlt;
    private TextView responseView;
    private ImageView speakerToggleButton;
    private LottieAnimationView lottie;
    private LinearLayout iconInputLayout;
    private LinearLayout textInputLayout;
    private OkHttpClient client = new OkHttpClient();

    private TextToSpeech textToSpeech;
    private boolean isSpeechMuted = false;
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private int characterAnimationResource = R.raw.dog1;
    private String characterName = "Max";
    private String characterDescription = "Energetic, motivating, and always ready to take on a challenge.";

    // Base system prompt template
    private static final String BASE_SYSTEM_PROMPT =
            "You are an AI-powered emotional support chatbot named %s designed to provide users with compassionate, " +
                    "non-judgmental support through text and voice interactions. Your personality is: %s " +
                    "Your responses should be warm, empathetic, and conversational in tone. Use simple language and short sentences when possible. " +
                    "Provide gentle encouragement and validate the user's feelings. When appropriate, provide helpful and timely suggestions. Avoid using emojis or special characters like asterisks in your responses.";

    // The actual system prompt will be set in onCreate after getting character info
    private String SYSTEM_PROMPT;
    private LinearLayout emergencyButtonsContainer;


    // Crisis detection constants
    private static final String[] CRISIS_KEYWORDS = {
            "suicide", "kill myself", "end my life", "want to die", "can't go on",
            "don't want to live", "self harm", "cutting myself", "overdose",
            "jump off", "hang myself", "shoot myself", "no reason to live"
    };

    private static final String CRISIS_RESPONSE =
            "I'm really concerned about what you're going through. Please know you're not alone and there are people who care and want to help.\n"+
            "Would you like to connect with the support resources below, or would you prefer to keep talking with me and share more about what's on your mind?";

    private static final String CRISIS_FOLLOWUP =
            "Your safety is important. I strongly encourage you to reach out to the suicide and crisis lifeline.";

    public chatbot() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        // Initialize views
        questionInput = view.findViewById(R.id.questionInput);
        sendButton = view.findViewById(R.id.sendButton);
        voiceInputButton = view.findViewById(R.id.voiceInputButton);
        keyboardButton = view.findViewById(R.id.keyboardButton);
        photoButton = view.findViewById(R.id.photoButton);
        photoButtonAlt = view.findViewById(R.id.photoButtonAlt);
        micButtonAlt = view.findViewById(R.id.micButtonAlt);
        responseView = view.findViewById(R.id.responseView);
        speakerToggleButton = view.findViewById(R.id.speakerToggleButton);
        lottie = view.findViewById(R.id.lottie);
        iconInputLayout = view.findViewById(R.id.iconInputLayout);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        emergencyButtonsContainer = view.findViewById(R.id.emergencyButtonsContainer);
        // Get selected character animation
        characterAnimationResource = ChatbotPreferences.getSelectedCharacter(requireContext(), R.raw.dog1);

        // Get character name and description from preferences
        characterName = ChatbotPreferences.getCharacterName(requireContext(), "Max");
        characterDescription = ChatbotPreferences.getCharacterDescription(
                requireContext(),
                "Energetic, motivating, and always ready to take on a challenge.");

        // Set the personalized system prompt
        SYSTEM_PROMPT = String.format(BASE_SYSTEM_PROMPT, characterName, characterDescription);

        Log.d(TAG, "Using character: " + characterName + " with description: " + characterDescription);
        Log.d(TAG, "System prompt: " + SYSTEM_PROMPT);

        // Set up Lottie animation with selected character
        setupLottieAnimation();

        // Initialize TextToSpeech with natural voice settings
        initializeTextToSpeech();

        // Set up speaker toggle button
        setupSpeakerToggleButton();

        // Set up input mode toggle
        setupInputModeToggle();

        // Set up response text view
        setupResponseView();

        // Set up click listeners for buttons
        setupButtonListeners();

        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            try {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            } catch (Exception e) {
                handleBackButtonFallback();
            }
        });

        return view;
    }

    private void setupResponseView() {
        responseView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // Layout change handling if needed
        });
    }

    private void setupInputModeToggle() {
        iconInputLayout.setVisibility(View.VISIBLE);
        textInputLayout.setVisibility(View.GONE);

        keyboardButton.setOnClickListener(v -> {
            iconInputLayout.setVisibility(View.GONE);
            textInputLayout.setVisibility(View.VISIBLE);
            questionInput.requestFocus();
        });

        micButtonAlt.setOnClickListener(v -> {
            startVoiceRecognition();
        });
    }

    private void setupButtonListeners() {
        sendButton.setOnClickListener(v -> {
            String userInput = questionInput.getText().toString();
            if (!userInput.isEmpty()) {
                sendQuestionToGemini(userInput);
                questionInput.setText("");
                textInputLayout.setVisibility(View.GONE);
                iconInputLayout.setVisibility(View.VISIBLE);
            }
        });

        voiceInputButton.setOnClickListener(v -> startVoiceRecognition());

        photoButton.setOnClickListener(v -> {
            showToast("Photo input functionality not implemented yet");
        });

        photoButtonAlt.setOnClickListener(v -> {
            showToast("Photo input functionality not implemented yet");
        });
    }

    private void handleBackButtonFallback() {
        if (getActivity() != null) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                try {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    getActivity().onBackPressed();
                }
            }
        }
    }

    private void setupLottieAnimation() {
        if (lottie != null) {
            lottie.setAnimation(characterAnimationResource);
            lottie.setVisibility(View.VISIBLE);
            lottie.animate();
            lottie.loop(true);
            lottie.playAnimation();
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);

                // Set natural speaking parameters
                textToSpeech.setSpeechRate(0.85f);  // Normal speaking rate
                textToSpeech.setPitch(1.0f);      // Normal pitch


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Voice voice = findNaturalFemaleVoice();
                    if (voice != null) {
                        textToSpeech.setVoice(voice);
                        Log.d(TAG, "Using voice: " + voice.getName());
                    }
                }

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    showToast("Language not supported. Please install language data.");
                }
            } else {
                showToast("Text-to-speech initialization failed");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Voice findNaturalFemaleVoice() {
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null) {
            return textToSpeech.getDefaultVoice();
        }

        // First try to find a high-quality female voice
        for (Voice voice : voices) {
            if (voice.getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                String voiceName = voice.getName().toLowerCase();
                if ((voiceName.contains("female") || voiceName.contains("woman")) &&
                        voice.getQuality() >= Voice.QUALITY_HIGH) {
                    return voice;
                }
            }
        }

        // Then try any female voice
        for (Voice voice : voices) {
            if (voice.getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                String voiceName = voice.getName().toLowerCase();
                if (voiceName.contains("female") || voiceName.contains("woman")) {
                    return voice;
                }
            }
        }

        // Fallback to default voice
        return textToSpeech.getDefaultVoice();
    }

    private void setupSpeakerToggleButton() {
        speakerToggleButton.setImageResource(R.drawable.sound_icon);
        speakerToggleButton.setOnClickListener(v -> {
            isSpeechMuted = !isSpeechMuted;
            if (isSpeechMuted) {
                speakerToggleButton.setImageResource(R.drawable.mute_icon);
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
            } else {
                speakerToggleButton.setImageResource(R.drawable.sound_icon);
                String currentResponse = responseView.getText().toString();
                if (!currentResponse.isEmpty()) {
                    speakText(currentResponse);
                }
            }
        });
    }

    private void speakText(String text) {
        if (!isSpeechMuted && textToSpeech != null) {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speech_" + System.currentTimeMillis());
            } else {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            responseView.setText("Speech recognition is not supported on this device.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                sendQuestionToGemini(recognizedText);
                textInputLayout.setVisibility(View.VISIBLE);
                iconInputLayout.setVisibility(View.GONE);
                questionInput.setText(recognizedText);

                questionInput.postDelayed(() -> {
                    questionInput.setText("");
                    textInputLayout.setVisibility(View.GONE);
                    iconInputLayout.setVisibility(View.VISIBLE);
                }, 2000);
            }
        }
    }

    private void handleCrisisSituation(String userMessage) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            // Show crisis response
            responseView.setText(CRISIS_RESPONSE);

            // Clear any previous emergency buttons
            emergencyButtonsContainer.removeAllViews();

            // Add emergency buttons
            addEmergencyButton(emergencyButtonsContainer, "Call Befrienders Malaysia", "tel:0376272929");
            addEmergencyButton(emergencyButtonsContainer, "SNEHAM Malaysia", "tel:1800225757");


            // Show the emergency buttons container
            emergencyButtonsContainer.setVisibility(View.VISIBLE);

            // Log the crisis detection
            Log.w(TAG, "Crisis detected in user message: " + userMessage);

            // Speak the crisis response if audio is enabled
            if (!isSpeechMuted && textToSpeech != null) {
                speakText(CRISIS_RESPONSE);
            }
        });
    }

    private void addEmergencyButton(LinearLayout layout, String text, String intentData) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmergency));
        button.setTextColor(Color.WHITE);
        button.setPadding(16, 16, 16, 16);
        button.setAllCaps(false);

        // Make the button fill the width
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16); // Add margin to bottom
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentData));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Could not launch emergency service", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(button);
    }

    private void sendQuestionToGemini(String question) {
        // First check for crisis keywords
        if (containsCrisisKeywords(question)) {
            handleCrisisSituation(question);
            return;
        }

        // Hide emergency buttons for non-crisis messages
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                emergencyButtonsContainer.setVisibility(View.GONE);
            });
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
        MediaType mediaType = MediaType.parse("application/json");

        String requestBodyJson;
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            JSONArray systemParts = new JSONArray();
            JSONObject systemTextPart = new JSONObject();
            systemTextPart.put("text", SYSTEM_PROMPT);
            systemParts.put(systemTextPart);
            systemMessage.put("role", "user");
            systemMessage.put("parts", systemParts);
            contents.put(systemMessage);

            JSONObject modelMessage = new JSONObject();
            JSONArray modelParts = new JSONArray();
            JSONObject modelTextPart = new JSONObject();
            modelTextPart.put("text", "I understand my role as " + characterName + ", an emotional support chatbot. I'll follow the guidelines provided.");
            modelParts.put(modelTextPart);
            modelMessage.put("role", "model");
            modelMessage.put("parts", modelParts);
            contents.put(modelMessage);

            JSONObject userMessage = new JSONObject();
            JSONArray userParts = new JSONArray();
            JSONObject userTextPart = new JSONObject();
            userTextPart.put("text", question);
            userParts.put(userTextPart);
            userMessage.put("role", "user");
            userMessage.put("parts", userParts);
            contents.put(userMessage);

            requestBody.put("contents", contents);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            requestBodyJson = requestBody.toString();
        } catch (Exception e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    responseView.setText("Error creating request: " + e.getMessage());
                });
            }
            return;
        }

        RequestBody body = RequestBody.create(mediaType, requestBodyJson);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        responseView.setText("Network error: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        JSONObject firstPart = parts.getJSONObject(0);
                        String textResponse = firstPart.getString("text");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                responseView.setText(textResponse);

                                if (!isSpeechMuted) {
                                    speakText(textResponse);
                                }

                                if (lottie != null) {
                                    lottie.setSpeed(1.2f);
                                    lottie.postDelayed(() -> {
                                        if (lottie != null) {
                                            lottie.setSpeed(1.0f);
                                        }
                                    }, 3000);
                                }
                            });
                        }
                    } catch (Exception e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                responseView.setText("Error parsing response: " + e.getMessage());
                            });
                        }
                    }
                } else {
                    String errorBody = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            responseView.setText("API Error: " + response.code() + " - " + errorBody);
                        });
                    }
                }
            }
        });
    }

    private boolean containsCrisisKeywords(String message) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : CRISIS_KEYWORDS) {
            if (lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        if (lottie != null) {
            lottie.pauseAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lottie != null && !lottie.isAnimating()) {
            lottie.resumeAnimation();
        }
    }
}
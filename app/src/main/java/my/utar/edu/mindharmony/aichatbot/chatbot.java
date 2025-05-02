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

// Add these imports at the top of your file, after the existing imports
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;

import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;
import java.util.Map;

import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

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
            "You are an AI-powered emotional support chatbot named %s designed to provide users with compassionate, non-judgmental support through text and voice interactions." +
                    "Your personality is: %s." +
                    "Your responses should be warm, empathetic, and conversational. Offer brief, supportive replies. When relevant, suggest clear but context-aware actions using basic CBT principles. Avoid vague or generic advice." +
                    "Do not give medical or psychiatric advice.Do not give medical or psychiatric advice. Do not include emojis or special characters like asterisks.";

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

    private static final int REQUEST_CODE_IMAGE_PICK = 200;
    private static final int REQUEST_CODE_READ_STORAGE_PERMISSION = 300;

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
        // Default visibility state
        iconInputLayout.setVisibility(View.VISIBLE);
        textInputLayout.setVisibility(View.GONE);

        // Switch to text input mode when keyboard button is clicked
        keyboardButton.setOnClickListener(v -> {
            iconInputLayout.setVisibility(View.GONE);
            textInputLayout.setVisibility(View.VISIBLE);
            questionInput.requestFocus();

            // Show keyboard automatically
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(questionInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Handle microphone button click in text input mode - switch back to icon mode and start voice recognition
        micButtonAlt.setOnClickListener(v -> {
            // First switch back to icon input mode
            textInputLayout.setVisibility(View.GONE);
            iconInputLayout.setVisibility(View.VISIBLE);

            // Then start voice recognition
            startVoiceRecognition();
        });

        // Listen for text input to show/hide send button based on content
        questionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show send button only when there's text to send
                sendButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

        // Update photo button listeners
        photoButton.setOnClickListener(v -> requestStoragePermission());
        photoButtonAlt.setOnClickListener(v -> requestStoragePermission());
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

                // Enhanced natural speaking parameters
                textToSpeech.setSpeechRate(0.82f);  // Slightly slower for more natural cadence
                textToSpeech.setPitch(1.0f);        // Normal pitch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Voice voice = findBestNaturalVoice();
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
            setupUtteranceProgressListener();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Voice findBestNaturalVoice() {
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null || voices.isEmpty()) {
            return textToSpeech.getDefaultVoice();
        }

        // Define voice quality levels by keywords (higher values = better quality)
        Map<String, Integer> qualityKeywords = new HashMap<>();
        qualityKeywords.put("enhanced", 5);
        qualityKeywords.put("premium", 5);
        qualityKeywords.put("natural", 4);
        qualityKeywords.put("neural", 4);
        qualityKeywords.put("wavenet", 3);
        qualityKeywords.put("high", 2);

        Voice bestVoice = null;
        int bestScore = -1;

        // First try to find a high-quality voice based on our character
        for (Voice voice : voices) {
            if (voice.getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                String voiceName = voice.getName().toLowerCase();

                // Score this voice
                int score = 0;

                // Check if it's the gender we want based on character
                boolean preferFemaleVoice = true; // Change based on character gender
                boolean isFemaleVoice = voiceName.contains("female") || voiceName.contains("woman");

                if (preferFemaleVoice && isFemaleVoice) {
                    score += 3;
                } else if (!preferFemaleVoice && !isFemaleVoice) {
                    score += 3;
                }

                // Add quality based on name keywords
                for (Map.Entry<String, Integer> entry : qualityKeywords.entrySet()) {
                    if (voiceName.contains(entry.getKey())) {
                        score += entry.getValue();
                    }
                }

                // Add quality based on voice quality enum (if available)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    score += voice.getQuality(); // QUALITY_VERY_LOW to QUALITY_VERY_HIGH
                }

                // Update best voice if this one is better
                if (score > bestScore) {
                    bestScore = score;
                    bestVoice = voice;
                }
            }
        }

        return bestVoice != null ? bestVoice : textToSpeech.getDefaultVoice();
    }

    private String preprocessTextForSpeech(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder processedText = new StringBuilder();

        // Replace punctuation with SSML pause markers if using SSML
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return addSpeechMarkup(text);
        } else {
            // For older Android versions, we'll add manual pauses
            String[] sentences = text.split("(?<=[.!?])\\s+");
            for (int i = 0; i < sentences.length; i++) {
                processedText.append(sentences[i]);
                if (i < sentences.length - 1) {
                    processedText.append(" ");
                }
            }
        }

        return processedText.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private String addSpeechMarkup(String text) {
        // Use SSML to improve speech naturalness
        StringBuilder ssml = new StringBuilder();
        ssml.append("<speak>");

        // Replace periods with pauses
        String processed = text
                .replace(". ", ".<break time='500ms'/> ")
                .replace("? ", "?<break time='400ms'/> ")
                .replace("! ", "!<break time='400ms'/> ")
                .replace(", ", ",<break time='200ms'/> ")
                .replace("; ", ";<break time='300ms'/> ")
                .replace("... ", "...<break time='700ms'/> ");

        // Add prosody for more natural speech
        ssml.append("<prosody rate='0.95' pitch='+0%'>");
        ssml.append(processed);
        ssml.append("</prosody>");

        ssml.append("</speak>");
        return ssml.toString();
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

            // Apply emotion-based adjustments
            adjustSpeechForEmotion(text);

            // Preprocess text for more natural speech
            String processedText = preprocessTextForSpeech(text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speech_" + System.currentTimeMillis());

                // If using SSML
                if (processedText.startsWith("<speak>")) {
                    textToSpeech.speak(processedText, TextToSpeech.QUEUE_FLUSH, params, "speech_ssml_" + System.currentTimeMillis());
                } else {
                    textToSpeech.speak(processedText, TextToSpeech.QUEUE_FLUSH, params, "speech_" + System.currentTimeMillis());
                }
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speech_" + System.currentTimeMillis());
                textToSpeech.speak(processedText, TextToSpeech.QUEUE_FLUSH, params);
            }
        }
    }

    private void setupUtteranceProgressListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (lottie != null) {
                                // Make animation more active during speaking
                                lottie.setSpeed(1.2f);
                            }
                        });
                    }
                }

                @Override
                public void onDone(String utteranceId) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (lottie != null) {
                                // Return to normal speed after speaking
                                lottie.setSpeed(1.0f);
                            }
                        });
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    // Handle errors if needed
                }
            });
        }
    }
    private void adjustSpeechForEmotion(String text) {
        // Simple emotion detection based on keywords
        text = text.toLowerCase();

        if (containsAny(text, "happy", "excited", "great", "wonderful", "joy")) {
            textToSpeech.setPitch(1.05f);   // Slightly higher pitch for happiness
            textToSpeech.setSpeechRate(0.9f); // Slightly faster for excitement
        }
        else if (containsAny(text, "sad", "sorry", "unfortunate", "regret")) {
            textToSpeech.setPitch(0.95f);   // Lower pitch for sadness
            textToSpeech.setSpeechRate(0.8f); // Slower for sadness
        }
        else if (containsAny(text, "angry", "frustrated", "annoyed")) {
            textToSpeech.setPitch(1.02f);   // Slightly higher for intensity
            textToSpeech.setSpeechRate(0.85f); // Still controlled pace
        }
        else if (containsAny(text, "calm", "relax", "breathe", "peaceful")) {
            textToSpeech.setPitch(0.98f);   // Slightly lower, soothing
            textToSpeech.setSpeechRate(0.75f); // Slower, calming pace
        }
        else {
            // Default neutral tone
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(0.82f);
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
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
            // Voice recognition handling - process directly without showing text input
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                // Process the voice input directly
                sendQuestionToGemini(recognizedText);

                // Optional: Show a brief toast indicating the recognized text
                showToast("Processing: " + recognizedText);

                // Keep the icon input mode visible
                iconInputLayout.setVisibility(View.VISIBLE);
                textInputLayout.setVisibility(View.GONE);
            }
        }
        else if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                handleImageSelection(imageUri);
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

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
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

    private void requestStoragePermission() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_READ_STORAGE_PERMISSION
                );
            } else {
                openImagePicker();
            }
        }
        // For Android 6-12, we need READ_EXTERNAL_STORAGE
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_STORAGE_PERMISSION
                );
            } else {
                openImagePicker();
            }
        }
        // For Android 5 and below, permissions are granted at install time
        else {
            openImagePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                // Permission denied
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES))) {
                    // Show explanation why we need this permission
                    showToast("We need storage permission to access your images");
                } else {
                    // User denied permission with "Don't ask again" - direct them to settings
                    showPermissionSettingsDialog();
                }
            }
        }
    }

    //  method to direct users to app settings
    private void showPermissionSettingsDialog() {
        // Create an AlertDialog to inform the user they need to enable permissions manually
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Permission Required")
                .setMessage("Storage permission is required to access images. Please enable it in app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    // Direct the user to app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
    }


    // Updated method to handle image selection and conversion to base64
    private void handleImageSelection(Uri imageUri) {
        try {
            // Show loading state
            responseView.setText("Processing image...");

            // Convert image to base64 with proper encoding
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                responseView.setText("Error: Could not open image file");
                return;
            }

            // Resize the image before encoding to avoid large data sizes
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Scale down the image if it's too large
            Bitmap resizedBitmap = resizeImageIfNeeded(originalBitmap, 1024, 1024);

            // Convert to byte array with proper compression
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Use proper Base64 encoding
            String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);

            // Send image to Gemini
            sendImageToGemini(base64Image);

        } catch (Exception e) {
            responseView.setText("Error processing image: " + e.getMessage());
            Log.e(TAG, "Image processing error", e);
        }
    }

    // Helper method to resize large images
    private Bitmap resizeImageIfNeeded(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return image; // No need to resize
        }

        float scale = Math.min((float)maxWidth / width, (float)maxHeight / height);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    // Updated method to send image to Gemini API
    private void sendImageToGemini(String base64Image) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
        MediaType mediaType = MediaType.parse("application/json");

        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();

            // System prompt
            JSONObject systemMessage = new JSONObject();
            JSONArray systemParts = new JSONArray();
            JSONObject systemTextPart = new JSONObject();
            systemTextPart.put("text", SYSTEM_PROMPT +
                    " When responding to images, describe what you see in a friendly, supportive way " +
                    "that matches your personality. If the image contains text, read it carefully and respond appropriately.");
            systemParts.put(systemTextPart);
            systemMessage.put("role", "user");
            systemMessage.put("parts", systemParts);
            contents.put(systemMessage);

            // Model acknowledgment
            JSONObject modelMessage = new JSONObject();
            JSONArray modelParts = new JSONArray();
            JSONObject modelTextPart = new JSONObject();
            modelTextPart.put("text", "I understand my role as " + characterName +
                    ". I'll respond to images with my characteristic personality.");
            modelParts.put(modelTextPart);
            modelMessage.put("role", "model");
            modelMessage.put("parts", modelParts);
            contents.put(modelMessage);

            // User message with image
            JSONObject userMessage = new JSONObject();
            JSONArray userParts = new JSONArray();

            // Text instruction
            JSONObject instructionPart = new JSONObject();
            instructionPart.put("text", "Please look at this image and respond as " + characterName);
            userParts.put(instructionPart);

            // Image part - using correct field names based on Gemini API
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/jpeg"); // Corrected from "mime_type"
            inlineData.put("data", base64Image);
            imagePart.put("inlineData", inlineData); // Corrected from "inline_data"
            userParts.put(imagePart);

            userMessage.put("role", "user");
            userMessage.put("parts", userParts);
            contents.put(userMessage);

            requestBody.put("contents", contents);

            // Generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            // For debugging purposes, log the request
            String requestJson = requestBody.toString();
            Log.d(TAG, "Gemini API Request: " + requestJson);

            RequestBody body = RequestBody.create(mediaType, requestJson);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        responseView.setText("Error sending image: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (getActivity() == null) return;

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
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error: " + e.getMessage());

                            getActivity().runOnUiThread(() -> {
                                responseView.setText("Error parsing image response: " + e.getMessage());
                            });
                        }
                    } else {
                        String errorBody = response.body().string();
                        Log.e(TAG, "API error: " + errorBody);

                        getActivity().runOnUiThread(() -> {
                            responseView.setText("API Error: " + response.code() + " - " + errorBody);
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Request creation error: " + e.getMessage());

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    responseView.setText("Error creating image request: " + e.getMessage());
                });
            }
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
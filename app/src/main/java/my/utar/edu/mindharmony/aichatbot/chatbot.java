package my.utar.edu.mindharmony.aichatbot;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

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
    private static final String TAG = "SoftTTS_Chatbot";
    private EditText questionInput;
    private Button sendButton;
    private ImageButton voiceInputButton;
    private TextView responseView;
    private ImageView speakerToggleButton;
    private LottieAnimationView lottie;
    private OkHttpClient client = new OkHttpClient();

    private TextToSpeech textToSpeech;
    private boolean isSpeechMuted = false;
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    // Default character animation resource
    private int characterAnimationResource = R.raw.dog1;

    // System prompt
    private static final String SYSTEM_PROMPT =
            "You are an AI-powered emotional support chatbot designed to provide users with compassionate, " +
                    "non-judgmental support through text and voice interactions. Your responses should be warm, " +
                    "empathetic, and conversational in tone. Use simple language and short sentences when possible. " +
                    "Provide gentle encouragement and validate the user's feelings.";

    public chatbot() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        questionInput = view.findViewById(R.id.questionInput);
        sendButton = view.findViewById(R.id.sendButton);
        voiceInputButton = view.findViewById(R.id.voiceInputButton);
        responseView = view.findViewById(R.id.responseView);
        speakerToggleButton = view.findViewById(R.id.speakerToggleButton);
        lottie = view.findViewById(R.id.lottie);

        // Get selected character animation
        characterAnimationResource = ChatbotPreferences.getSelectedCharacter(requireContext(), R.raw.dog1);

        // Set up Lottie animation with selected character
        setupLottieAnimation();

        // Initialize TextToSpeech with improved settings
        initializeTextToSpeech();

        // Set up speaker toggle button
        setupSpeakerToggleButton();

        sendButton.setOnClickListener(v -> {
            String userInput = questionInput.getText().toString();
            if (!userInput.isEmpty()) {
                sendQuestionToGemini(userInput);
                questionInput.setText("");
            }
        });

        voiceInputButton.setOnClickListener(v -> startVoiceRecognition());

        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Navigate back using Navigation Component
            try {
                NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            } catch (Exception e) {
                // Fallback if Navigation Component fails
                handleBackButtonFallback();
            }
        });

        return view;
    }

    // Helper method for fallback handling
    private void handleBackButtonFallback() {
        if (getActivity() != null) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                // If there are fragments in the back stack, pop one
                getParentFragmentManager().popBackStack();
            } else {
                // If no fragments in back stack but this isn't the main screen,
                // navigate to main activity or home screen
                try {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } catch (Exception e) {
                    // Last resort: let the system handle the back press
                    getActivity().onBackPressed();
                }
            }
        }
    }



    private void setupLottieAnimation() {
        if (lottie != null) {
            // Set the selected character animation
            lottie.setAnimation(characterAnimationResource);
            // Make sure it's visible
            lottie.setVisibility(View.VISIBLE);
            // Start animation
            lottie.animate();
            // Set animation to loop continuously
            lottie.loop(true);
            // Start playing animation
            lottie.playAnimation();
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);

                // Apply softer base settings
                textToSpeech.setSpeechRate(0.70f);  // Much slower for clearer speech
                textToSpeech.setPitch(0.88f);       // Lower pitch for softer sound

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Voice voice = findSofterVoice();
                    if (voice != null) {
                        textToSpeech.setVoice(voice);
                        Log.d(TAG, "Using voice: " + voice.getName());
                    }

                    // Apply audio enhancements
                    applySofterAudioSettings();
                }

                // Try specific engine configurations
                configureTTSEngine();

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    showToast("Language not supported. Please install language data.");
                } else {
                    // Log available voices for debugging
                    logAvailableVoices();
                }
            } else {
                showToast("Text-to-speech initialization failed");
            }
        });
    }

    private void logAvailableVoices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && textToSpeech != null) {
            Set<Voice> voices = textToSpeech.getVoices();
            Log.d(TAG, "Available TTS voices: " + voices.size());
            for (Voice voice : voices) {
                if (voice.getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                    Log.d(TAG, "Voice: " + voice.getName() +
                            ", Locale: " + voice.getLocale() +
                            ", Quality: " + voice.getQuality());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Voice findSofterVoice() {
        // Prioritize voices that tend to sound softer and more natural
        String[] preferredVoicePatterns = {
                "female",    // Female voices often sound softer
                "wavenet",   // Google's neural voices
                "neural",    // Neural network-based voices
                "premium",   // Higher quality voices
                "enhanced",  // Enhanced quality voices
                "natural"    // Natural sounding voices
        };

        Voice bestVoice = null;
        int highestScore = -1;

        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null) {
            return textToSpeech.getDefaultVoice();
        }

        for (Voice voice : voices) {
            // Skip non-English voices
            if (!voice.getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                continue;
            }

            String voiceName = voice.getName().toLowerCase();
            int score = 0;

            // Score based on preferred voice patterns
            for (String pattern : preferredVoicePatterns) {
                if (voiceName.contains(pattern)) {
                    score += 5;
                }
            }

            // Female voices often sound softer than male voices
            if (voiceName.contains("female")) {
                score += 15;
            }

            // Avoid voices with "robot" or "standard" in their names
            if (voiceName.contains("robot") || voiceName.contains("standard")) {
                score -= 10;
            }

            // High quality voices typically have better audio processing
            if (voice.getQuality() == Voice.QUALITY_VERY_HIGH) {
                score += 15;
            } else if (voice.getQuality() == Voice.QUALITY_HIGH) {
                score += 10;
            }

            // Prefer network voices as they tend to be higher quality
            if (voice.isNetworkConnectionRequired()) {
                score += 5;
            }

            if (score > highestScore) {
                highestScore = score;
                bestVoice = voice;
            }
        }

        if (bestVoice != null) {
            Log.d(TAG, "Selected voice: " + bestVoice.getName() + " with score: " + highestScore);
        }

        return bestVoice != null ? bestVoice : textToSpeech.getDefaultVoice();
    }

    private void applySofterAudioSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Try to access any available audio enhancement features
                Bundle params = new Bundle();

                // Some TTS engines support these parameters
                params.putFloat("volume", 0.85f);  // Slightly lower volume

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // These are manufacturer-specific but worth trying
                    params.putString("audio_attributes", "media");  // Optimize for media playback
                    params.putInt("audio_quality", 100);           // Request highest quality
                    params.putString("emotion", "calm");           // Request calm speaking style
                }

                // Additional settings that might help on some devices
                textToSpeech.setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());

                textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, params, "init_audio_params");
            } catch (Exception e) {
                Log.w(TAG, "Some audio parameters not supported", e);
            }
        }
    }

    private void configureTTSEngine() {
        // Try to configure the specific TTS engine being used
        String engineName = textToSpeech.getDefaultEngine().toLowerCase();
        Log.d(TAG, "Using TTS engine: " + engineName);

        try {
            if (engineName.contains("google")) {
                // Google TTS specific settings
                textToSpeech.setPitch(0.82f);  // Even lower pitch for Google TTS
                textToSpeech.setSpeechRate(0.65f);  // Slower for Google TTS
                Log.d(TAG, "Applied Google TTS settings");
            } else if (engineName.contains("samsung")) {
                // Samsung TTS specific settings
                textToSpeech.setPitch(0.90f);
                textToSpeech.setSpeechRate(0.78f);
                Log.d(TAG, "Applied Samsung TTS settings");
            } else if (engineName.contains("amazon") || engineName.contains("polly")) {
                // Amazon Polly if available
                textToSpeech.setPitch(0.88f);
                textToSpeech.setSpeechRate(0.72f);
                Log.d(TAG, "Applied Amazon TTS settings");
            }
        } catch (Exception e) {
            Log.w(TAG, "Error configuring engine-specific TTS", e);
        }
    }

    private void setupSpeakerToggleButton() {
        speakerToggleButton.setImageResource(R.drawable.sound_icon);
        speakerToggleButton.setOnClickListener(v -> {
            isSpeechMuted = !isSpeechMuted;
            // Update the speaker icon based on mute status
            if (isSpeechMuted) {
                speakerToggleButton.setImageResource(R.drawable.mute_icon);
                // Stop any ongoing speech
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
            } else {
                speakerToggleButton.setImageResource(R.drawable.sound_icon);
                // Read the current response if available
                String currentResponse = responseView.getText().toString();
                if (!currentResponse.isEmpty()) {
                    speakText(currentResponse);
                }
            }
        });
    }

    private void speakText(String text) {
        if (!isSpeechMuted && textToSpeech != null) {
            // Stop any current speech
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }

            // Process the text for softer delivery
            String processedText = processSoftSpeech(text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bundle params = new Bundle();

                // Split into shorter chunks for better processing
                String[] chunks = splitIntoSmallChunks(processedText);
                int queueMode = TextToSpeech.QUEUE_FLUSH;

                for (String chunk : chunks) {
                    String ssmlText = "<speak>" +
                            "<prosody rate=\"0.70\" pitch=\"0.88\" volume=\"0.9\">" +
                            "<amazon:effect name=\"drc\">" +  // Dynamic range compression if available
                            chunk +
                            "</amazon:effect>" +
                            "</prosody>" +
                            "</speak>";

                    // Remove the amazon effect tag if not using Amazon Polly
                    if (!textToSpeech.getDefaultEngine().toLowerCase().contains("amazon")) {
                        ssmlText = ssmlText.replace("<amazon:effect name=\"drc\">", "").replace("</amazon:effect>", "");
                    }

                    textToSpeech.speak(ssmlText, queueMode, params, "soft_speech_" + System.currentTimeMillis());
                    queueMode = TextToSpeech.QUEUE_ADD;

                    // Add a slight delay between chunks for more natural speaking
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Sleep interrupted between TTS chunks", e);
                    }
                }
            } else {
                textToSpeech.speak(processedText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private String processSoftSpeech(String text) {
        // Don't modify if already contains SSML
        if (text.contains("<speak>")) return text;

        // Replace exclamation marks with periods for softer tone
        text = text.replace("!", ".");

        // Soften ALL CAPS text which can sound harsh when spoken
        text = text.replaceAll("([A-Z]{3,})", "<prosody volume=\"soft\">$1</prosody>");

        // Add soft breaks and breathing
        text = text
                // Add breathing pauses for more natural rhythm
                .replace(". ", ".<break time=\"450ms\"/> ")
                .replace("? ", "?<break time=\"500ms\"/> ")
                .replace(", ", ",<break time=\"250ms\"/> ")
                .replace("; ", ";<break time=\"300ms\"/> ")

                // Add slight breathing sounds for longer pauses
                .replace(".<break", ".<break strength=\"medium\"")

                // Use more subtle emphasis - strong emphasis can sound harsh
                .replaceAll("\\b(very|really|extremely)\\b", "<prosody volume=\"soft\">$1</prosody>")

                // Add soft speaking style where appropriate
                .replaceAll("(?i)(thank you|sorry|please|excuse me)", "<prosody pitch=\"low\" rate=\"0.75\">$1</prosody>")

                // Add slight emphasis on important words
                .replaceAll("\\b(important|critical|crucial|essential)\\b", "<emphasis level=\"moderate\">$1</emphasis>")

                // Add breathing for long sentences
                .replaceAll("([^.!?]{60,}?)([,;:])", "$1$2<break time=\"150ms\"/>");

        return text;
    }

    // Split text into smaller chunks (20-30 words) for better TTS processing
    private String[] splitIntoSmallChunks(String text) {
        ArrayList<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;

        for (String sentence : sentences) {
            int sentenceWordCount = sentence.split("\\s+").length;

            if (wordCount + sentenceWordCount > 25) {  // Reduced chunk size for smoother processing
                // This chunk is getting large, save it and start a new one
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    wordCount = 0;
                }
            }

            currentChunk.append(sentence).append(" ");
            wordCount += sentenceWordCount;
        }

        // Add the final chunk if not empty
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks.toArray(new String[0]);
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
                questionInput.setText(result.get(0));
                // Auto-send the recognized speech
                sendQuestionToGemini(result.get(0));
                questionInput.setText("");
            }
        }
    }

    private void sendQuestionToGemini(String question) {
        // No need to show/hide the animation as it should be always visible now

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
            modelTextPart.put("text", "I understand my role as an emotional support chatbot. I'll follow the guidelines provided.");
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
                    // We don't hide the animation anymore
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
                        // Parse the JSON response
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray candidates = jsonResponse.getJSONArray("candidates");
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        JSONObject firstPart = parts.getJSONObject(0);
                        String textResponse = firstPart.getString("text");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Update the UI with the response
                                responseView.setText(textResponse);

                                // Read the response aloud if speech is not muted
                                if (!isSpeechMuted) {
                                    speakText(textResponse);
                                }

                                // Make the animation more expressive during response
                                if (lottie != null) {
                                    // Speed up animation slightly to indicate "talking"
                                    lottie.setSpeed(1.2f);

                                    // Return to normal speed after a delay
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

    // Helper method to show toast messages
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

        // Cleanup TextToSpeech resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        // Cancel any pending API requests
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop any ongoing speech when the fragment is paused
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        // Pause the animation
        if (lottie != null) {
            lottie.pauseAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resume the animation
        if (lottie != null && !lottie.isAnimating()) {
            lottie.resumeAnimation();
        }
    }
}
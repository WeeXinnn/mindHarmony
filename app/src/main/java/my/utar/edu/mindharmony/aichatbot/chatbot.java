package my.utar.edu.mindharmony.aichatbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import com.airbnb.lottie.LottieAnimationView;
import my.utar.edu.mindharmony.BuildConfig;
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
    private EditText questionInput;
    private Button sendButton;
    private ImageButton voiceInputButton;
    private TextView responseView;
    private LottieAnimationView lottie;
    private OkHttpClient client = new OkHttpClient();
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    // System prompt
    private static final String SYSTEM_PROMPT =
            "You are an AI-powered emotional support chatbot designed to provide users with compassionate, " +
                    "non-judgmental support through text and voice interactions..."; // Truncated for brevity

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
        lottie = view.findViewById(R.id.lottie);

        // Set up Lottie animation
        if (lottie != null) {
            lottie.animate();
        }

        sendButton.setOnClickListener(v -> {
            String userInput = questionInput.getText().toString();
            if (!userInput.isEmpty()) {
                sendQuestionToGemini(userInput);
                questionInput.setText("");
            }
        });

        voiceInputButton.setOnClickListener(v -> startVoiceRecognition());

        return view;
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
            }
        }
    }

    private void sendQuestionToGemini(String question) {
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
                getActivity().runOnUiThread(() -> responseView.setText("Error creating request: " + e.getMessage()));
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
                    getActivity().runOnUiThread(() -> responseView.setText("Request failed: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    String reply = extractReplyFromResponse(responseData);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> responseView.setText(reply));
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> responseView.setText("Error: " + response.message() + "\n" + errorBody));
                    }
                }
            }
        });
    }

    private String extractReplyFromResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray candidates = jsonObject.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            JSONObject firstPart = parts.getJSONObject(0);
            return firstPart.getString("text");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
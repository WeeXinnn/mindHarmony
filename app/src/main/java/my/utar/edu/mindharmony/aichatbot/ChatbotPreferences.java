package my.utar.edu.mindharmony.aichatbot;

import android.content.Context;
import android.content.SharedPreferences;

public class ChatbotPreferences {
    private static final String PREF_NAME = "chatbot_preferences";
    private static final String KEY_SELECTED_CHARACTER = "selected_character";

    // Default character animation (dog1)
    private static final int DEFAULT_CHARACTER = 0; // This will be replaced with the actual resource ID

    // Save the selected character resource ID
    public static void saveSelectedCharacter(Context context, int characterResourceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SELECTED_CHARACTER, characterResourceId);
        editor.apply();
    }

    // Get the selected character resource ID
    public static int getSelectedCharacter(Context context, int defaultCharacterResourceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SELECTED_CHARACTER, defaultCharacterResourceId);
    }
}
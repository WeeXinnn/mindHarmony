package my.utar.edu.mindharmony.aichatbot;

import android.content.Context;
import android.content.SharedPreferences;

public class ChatbotPreferences {
    private static final String PREF_NAME = "chatbot_preferences";
    private static final String KEY_SELECTED_CHARACTER = "selected_character";
    private static final String KEY_CHARACTER_NAME = "character_name";
    private static final String KEY_CHARACTER_DESCRIPTION = "character_description";

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

    // Save the character name
    public static void saveCharacterName(Context context, String characterName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CHARACTER_NAME, characterName);
        editor.apply();
    }

    // Get the character name
    public static String getCharacterName(Context context, String defaultName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CHARACTER_NAME, defaultName);
    }

    // Save the character description
    public static void saveCharacterDescription(Context context, String characterDescription) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CHARACTER_DESCRIPTION, characterDescription);
        editor.apply();
    }

    // Get the character description
    public static String getCharacterDescription(Context context, String defaultDescription) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CHARACTER_DESCRIPTION, defaultDescription);
    }
}
package my.utar.edu.mindharmony.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import my.utar.edu.mindharmony.R;

public class profile extends Fragment {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NOTIFICATION = "notification";

    private TextView nameText;
    private EditText nameEdit;
    private Switch notificationSwitch;
    private TextView privacyPolicyText;

    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public profile() {

    }

    public static profile newInstance(String param1, String param2) {
        profile fragment = new profile();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        nameText = view.findViewById(R.id.username);
        nameEdit = view.findViewById(R.id.edit_username);
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        privacyPolicyText = view.findViewById(R.id.privacyPolicy);

        String savedName = sharedPreferences.getString(KEY_USERNAME, "User12345678");
        nameText.setText(savedName);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "Notification permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show();
                        notificationSwitch.setChecked(false); // Reset the switch if permission is denied
                    }
                }
        );

        boolean notificationEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATION, false);
        notificationSwitch.setChecked(notificationEnabled);

        nameText.setOnClickListener(v -> {
            nameText.setVisibility(View.GONE);
            nameEdit.setText(nameText.getText());
            nameEdit.setVisibility(View.VISIBLE);
            nameEdit.requestFocus();
        });

        nameEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveNameAndSwitchToDisplay();
            }
        });

        view.setOnTouchListener((v, event) -> {
            if (nameEdit.isFocused()) {
                saveNameAndSwitchToDisplay();
                return true;
            }
            return false;
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestNotificationPermission();
            }
            saveNotificationSetting(isChecked);
            Toast.makeText(requireContext(), "Notification setting updated", Toast.LENGTH_SHORT).show();
        });

        privacyPolicyText.setOnClickListener(v -> {
            String privacyPolicyContent = loadPrivacyPolicy();

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            builder.setTitle("Privacy Policy")
                    .setMessage(privacyPolicyContent)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(true);

            builder.create().show();
        });
        return view;
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private String loadPrivacyPolicy() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.privacy_policy);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error loading privacy policy.";
        }
        return stringBuilder.toString();
    }

    private void saveNameAndSwitchToDisplay() {
        String newName = nameEdit.getText().toString().trim();
        if (!newName.isEmpty()) {
            nameText.setText(newName);
            saveNameToPreferences(newName);
        }
        nameEdit.setVisibility(View.GONE);
        nameText.setVisibility(View.VISIBLE);
    }

    private void saveNameToPreferences(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, name);
        editor.apply();
    }

    private void saveNotificationSetting(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NOTIFICATION, isEnabled);
        editor.apply();
    }
}

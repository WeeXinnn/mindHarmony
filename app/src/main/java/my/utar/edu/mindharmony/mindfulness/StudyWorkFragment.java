package my.utar.edu.mindharmony.mindfulness;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import my.utar.edu.mindharmony.R;

public class StudyWorkFragment extends Fragment {

    private Toolbar        toolbarStudy;
    private TextView       tvTimerDisplay;
    private ChipGroup      chipScapes;
    private MaterialButton btnStart, btnPause, btnEnd;
    private View           controlsContainer;

    private CountDownTimer timer;
    private MediaPlayer    player;
    private long           timeLeftMs;
    private boolean        isPaused;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_study_work, container, false);

        // 1) Toolbar + Back
        toolbarStudy = v.findViewById(R.id.toolbarStudy);
        toolbarStudy.setNavigationOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // 2) Bind views
        tvTimerDisplay    = v.findViewById(R.id.tvTimerDisplay);
        chipScapes        = v.findViewById(R.id.chipScapes);
        btnStart          = v.findViewById(R.id.btnStart);
        btnPause          = v.findViewById(R.id.btnPause);
        btnEnd            = v.findViewById(R.id.btnEnd);
        controlsContainer = v.findViewById(R.id.controlsContainer);

        // 3) Default to 5 minutes and show
        timeLeftMs = 5 * 60L * 1000L;
        updateTimerDisplay(timeLeftMs);

        // 4) Allow tapping the timer pill to choose duration
        tvTimerDisplay.setClickable(true);
        tvTimerDisplay.setOnClickListener(__ -> showDurationPicker());

        // 5) Start button
        btnStart.setOnClickListener(__ -> {
            btnStart.setVisibility(View.GONE);
            controlsContainer.setVisibility(View.VISIBLE);
            chipScapes.setEnabled(false);
            isPaused = false;

            // begin sound + countdown
            playSoundscape(true);
            startTimer(timeLeftMs);
        });

        // 6) Pause / Resume
        btnPause.setOnClickListener(__ -> {
            if (!isPaused) {
                timer.cancel();
                isPaused = true;
                btnPause.setText("Resume");
            } else {
                isPaused = false;
                btnPause.setText("Pause");
                startTimer(timeLeftMs);
            }
        });

        // 7) End session
        btnEnd.setOnClickListener(__ -> {
            if (timer != null) timer.cancel();
            timeLeftMs = 0;
            updateTimerDisplay(0);
            resetUI();
        });

        return v;
    }

    private void showDurationPicker() {
        // build the 5,10,…,60 array
        String[] values = new String[12];
        for (int i = 0; i < 12; i++) {
            values[i] = String.valueOf((i + 1) * 5);
        }

        NumberPicker np = new NumberPicker(requireContext());
        np.setMinValue(0);
        np.setMaxValue(values.length - 1);
        np.setDisplayedValues(values);
        np.setWrapSelectorWheel(false);

        // set initial index
        int current = (int)(timeLeftMs / 60000 / 5) - 1;
        if (current >= 0 && current < values.length) {
            np.setValue(current);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Duration (mins)")
                .setView(np)
                .setPositiveButton("OK", (dlg, which) -> {
                    int mins = Integer.parseInt(values[np.getValue()]);
                    timeLeftMs = mins * 60L * 1000L;
                    updateTimerDisplay(timeLeftMs);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTimerDisplay(long ms) {
        long m = (ms / 1000) / 60;
        long s = (ms / 1000) % 60;
        tvTimerDisplay.setText(String.format("%02d:%02d", m, s));
    }

    private void startTimer(long duration) {
        timer = new CountDownTimer(duration, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                long m = (ms / 1000) / 60;
                long s = (ms / 1000) % 60;
                tvTimerDisplay.setText(String.format("%02d:%02d", m, s));
            }
            @Override public void onFinish() {
                tvTimerDisplay.setText("Done!");
                resetUI();
            }
        }.start();
    }

    private void playSoundscape(boolean loop) {
        int resId = R.raw.ocean;  // default
        int checked = chipScapes.getCheckedChipId();
        if (checked == R.id.chipWind)   resId = R.raw.wind;
        else if (checked == R.id.chipForest) resId = R.raw.forest;
        else if (checked == R.id.chipMix)    resId = R.raw.mix;

        if (player != null) player.release();
        player = MediaPlayer.create(requireContext(), resId);
        player.setLooping(loop);
        player.start();
    }

    private void resetUI() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        controlsContainer.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        chipScapes.setEnabled(true);
        btnPause.setText("Pause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
        if (player != null) player.release();
    }
}

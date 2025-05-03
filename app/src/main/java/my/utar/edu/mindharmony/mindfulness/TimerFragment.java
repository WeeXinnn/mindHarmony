package my.utar.edu.mindharmony.mindfulness;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import my.utar.edu.mindharmony.R;

public class TimerFragment extends Fragment {

    private TextView tvRemaining, tvCategory;
    private NumberPicker npMinutes;
    private Spinner spSound;
    private Button btnStart, btnPause, btnEnd;

    private CountDownTimer timer;
    private MediaPlayer player;
    private View   controlsContainer;
    private long   timeLeftMs;
    private boolean isPaused;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_timer, container, false);

        tvCategory   = v.findViewById(R.id.tvCategory);
        tvRemaining  = v.findViewById(R.id.tvRemaining);
        npMinutes    = v.findViewById(R.id.npMinutes);
        spSound      = v.findViewById(R.id.spSound);
        btnStart     = v.findViewById(R.id.btnStart);
        btnPause           = v.findViewById(R.id.btnPause);
        btnEnd             = v.findViewById(R.id.btnEnd);
        controlsContainer  = v.findViewById(R.id.controlsContainer);

        String category = getArguments() != null ?
                getArguments().getString("category", "Meditation") : "Meditation";

        tvCategory.setText(category);

        // build a 5-minute increment array: ["5","10",…,"60"]
        String[] minuteSteps = new String[12];
        for (int i = 0; i < 12; i++) {
            minuteSteps[i] = String.valueOf((i + 1) * 5);
        }

        // configure the NumberPicker to use that
        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(minuteSteps.length - 1);
        npMinutes.setDisplayedValues(minuteSteps);
        npMinutes.setWrapSelectorWheel(false);

        // Sound-spinner adapter (put 3 simple options)
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Rain", "Forest", "Waves"});
        spSound.setAdapter(a);

        btnStart.setOnClickListener(view -> {
            // 1) hide Start, show Pause/End
            btnStart.setVisibility(View.GONE);
            controlsContainer.setVisibility(View.VISIBLE);

            // 2) disable inputs
            isPaused = false;
            npMinutes.setEnabled(false);
            spSound.setEnabled(false);

            // 3) compute total milliseconds from the picker
            String[] steps = npMinutes.getDisplayedValues();
            int mins = Integer.parseInt(steps[npMinutes.getValue()]);
            timeLeftMs = mins * 60L * 1000L;

            // 4) start the selected soundscape on loop
            playSoundscape(true);

            // 5) start the countdown
            runTimer(timeLeftMs);
        });

        btnPause.setOnClickListener(vw -> {
            if (!isPaused) {
                timer.cancel();
                isPaused = true;
                btnPause.setText("Resume");
            } else {
                isPaused = false;
                btnPause.setText("Pause");
                runTimer(timeLeftMs);
            }
        });

        btnEnd.setOnClickListener(vw -> {
            if (timer != null) timer.cancel();
            tvRemaining.setText("00:00");
            resetUI();
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Rain", "Forest", "Waves"});
        spSound.setAdapter(adapter);

        return v;
    }

    private void playChime() {
        // TODO: put .mp3 in res/raw (eg. chime.mp3)
        player = MediaPlayer.create(requireContext(), R.raw.chime);
        player.setOnCompletionListener(mp -> mp.release());
        player.start();
    }

    private void resetUI() {
        // stop the looping soundscape
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        controlsContainer.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        npMinutes.setEnabled(true);
        spSound.setEnabled(true);
        btnPause.setText("Pause");
    }



    @Override public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
        if (player != null) player.release();
    }

    private void runTimer(long duration) {
        timer = new CountDownTimer(duration, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                long m = (ms/1000)/60, s = (ms/1000)%60;
                tvRemaining.setText(String.format("%02d:%02d", m, s));
            }
            @Override public void onFinish() {
                tvRemaining.setText("Done!");
                resetUI();
            }
        }.start();
    }

    private void playSoundscape(boolean loop) {
        int pos = spSound.getSelectedItemPosition();
        int res = pos==0 ? R.raw.rain
                : pos==1 ? R.raw.forest
                : R.raw.waves;
        if (player != null) player.release();
        player = MediaPlayer.create(requireContext(), res);
        player.setLooping(loop);
        player.start();
    }

}

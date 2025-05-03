package my.utar.edu.mindharmony.mindfulness;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.material.chip.Chip;
import my.utar.edu.mindharmony.R;

public class RelaxFragment extends Fragment {

    private Toolbar         toolbarRelax;
    private Chip            chipSound;
    private FrameLayout     breathContainer;
    private SeekBar         seekBar;
    private ImageButton     btnPause, btnRepeatLeft, btnRepeatRight;
    private TextView        tvBreathLabel;

    private ObjectAnimator  pulseXL, pulseL, pulseM, pulseS;
    private CountDownTimer  sessionTimer;
    private MediaPlayer     mediaPlayer;

    private final long      sessionDurationMs = 5 * 60_000L;
    private long            timeLeftMs;
    private boolean         isPaused;
    private boolean         exhaling;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_relax_unwind, container, false);

        toolbarRelax     = v.findViewById(R.id.toolbarRelax);
        chipSound        = v.findViewById(R.id.chipSound);
        breathContainer  = v.findViewById(R.id.breathContainer);
        seekBar          = v.findViewById(R.id.seekBar);
        btnPause         = v.findViewById(R.id.btnPause);
        btnRepeatLeft    = v.findViewById(R.id.btnRepeatLeft);
        btnRepeatRight   = v.findViewById(R.id.btnRepeatRight);
        tvBreathLabel    = v.findViewById(R.id.tvBreathLabel);

        toolbarRelax.setNavigationOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        View cXL = v.findViewById(R.id.circle_x_large);
        View cL  = v.findViewById(R.id.circleLarge);
        View cM  = v.findViewById(R.id.circleMedium);
        View cS  = v.findViewById(R.id.circleSmall);

        pulseXL = createPulseAnimator(cXL, 0);
        pulseL  = createPulseAnimator(cL, 500);
        pulseM  = createPulseAnimator(cM, 1000);
        pulseS  = createPulseAnimator(cS, 1500);

        // flip color/text each full cycle
        pulseXL.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationRepeat(Animator animation) {
                exhaling = !exhaling;
                updateRingColors();
                tvBreathLabel.setText(exhaling ? "Breathe Out" : "Breathe In");
            }
        });

        timeLeftMs = sessionDurationMs;
        seekBar.setMax((int) sessionDurationMs);
        sessionTimer = new CountDownTimer(sessionDurationMs, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                seekBar.setProgress((int)(sessionDurationMs - ms));
            }
            @Override public void onFinish() {
                seekBar.setProgress(seekBar.getMax());
                resetSession();
            }
        };

        chipSound.setOnClickListener(chip -> playOrStopSound());
        btnPause.setOnClickListener(b -> {
            if (!isPaused) pauseSession();
            else           resumeSession();
        });
        btnRepeatLeft.setOnClickListener(b -> resetSession());
        btnRepeatRight.setOnClickListener(b -> resetSession());

        startSession();
        return v;
    }

    private ObjectAnimator createPulseAnimator(View target, long delay) {
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(
                target,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f)
        );
        anim.setDuration(4000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setStartDelay(delay);
        return anim;
    }

    private void updateRingColors() {
        @androidx.annotation.DrawableRes int xl = exhaling
                ? R.drawable.circle_x_large_out
                : R.drawable.circle_x_large_in;
        @androidx.annotation.DrawableRes int l  = exhaling
                ? R.drawable.circle_large_out
                : R.drawable.circle_large_in;
        @androidx.annotation.DrawableRes int m  = exhaling
                ? R.drawable.circle_medium_out
                : R.drawable.circle_medium_in;
        @androidx.annotation.DrawableRes int s  = exhaling
                ? R.drawable.circle_small_out
                : R.drawable.circle_small_in;

        breathContainer.findViewById(R.id.circle_x_large).setBackgroundResource(xl);
        breathContainer.findViewById(R.id.circleLarge)    .setBackgroundResource(l);
        breathContainer.findViewById(R.id.circleMedium)   .setBackgroundResource(m);
        breathContainer.findViewById(R.id.circleSmall)    .setBackgroundResource(s);
    }

    private void startSession() {
        isPaused = false;
        exhaling = false;
        updateRingColors();
        tvBreathLabel.setText("Breathe In");
        pulseXL.start(); pulseL.start(); pulseM.start(); pulseS.start();
        sessionTimer.start();
    }

    private void pauseSession() {
        isPaused = true;
        btnPause.setImageResource(R.drawable.ic_resume);
        sessionTimer.cancel();
        pulseXL.pause(); pulseL.pause(); pulseM.pause(); pulseS.pause();
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    private void resumeSession() {
        isPaused = false;
        btnPause.setImageResource(R.drawable.ic_pause);
        pulseXL.resume(); pulseL.resume(); pulseM.resume(); pulseS.resume();
        sessionTimer = new CountDownTimer(timeLeftMs, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                seekBar.setProgress((int)(sessionDurationMs - ms));
            }
            @Override public void onFinish() {
                seekBar.setProgress(seekBar.getMax());
                resetSession();
            }
        }.start();
        if (mediaPlayer != null) mediaPlayer.start();
    }

    private void resetSession() {
        sessionTimer.cancel();
        pulseXL.cancel(); pulseL.cancel(); pulseM.cancel(); pulseS.cancel();

        for (int id : new int[]{
                R.id.circle_x_large, R.id.circleLarge,
                R.id.circleMedium,   R.id.circleSmall
        }) {
            View c = breathContainer.findViewById(id);
            c.setScaleX(1f); c.setScaleY(1f);
        }

        timeLeftMs = sessionDurationMs;
        seekBar.setProgress(0);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        chipSound.setText("Sound: Chirping Birds");
        btnPause.setImageResource(R.drawable.ic_pause);
        startSession();
    }

    private void playOrStopSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            chipSound.setText("Sound: Chirping Birds");
        } else {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.chirping_birds);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            chipSound.setText("Sound: Chirping Birds");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sessionTimer.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

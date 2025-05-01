package my.utar.edu.mindharmony;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Splash screen duration in milliseconds
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get references to the views
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.app_name);

        // Load animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations
        logo.startAnimation(scaleUp);
        appName.startAnimation(slideUp);

        // Start main activity after delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
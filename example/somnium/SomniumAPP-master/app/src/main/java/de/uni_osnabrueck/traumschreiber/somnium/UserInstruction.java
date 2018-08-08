package de.uni_osnabrueck.traumschreiber.somnium;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Generates the Instruction Slides composed of the different slides
 */
public class UserInstruction extends AppIntro {

    // Please DO NOT override onCreate. Use init.
    @Override
    public void init(Bundle savedInstanceState) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isFirstRun = prefs.getBoolean("firstStart", true);

        // possibility to change slides show if first run or not
        if (isFirstRun) {
            // Add your slide's fragments here.
            // AppIntro will automatically generate the dots indicator and buttons.
            addSlide(IntroSlide.newInstance(R.layout.welcome_slide));
            addSlide(IntroSlide.newInstance(R.layout.somnium_slide));
            addSlide(YouTubeSlide.newInstance(R.layout.youtube_slide));
            addSlide(IntroSlide.newInstance(R.layout.charge_slide));
        } else {

            addSlide(IntroSlide.newInstance(R.layout.welcome_slide));
            addSlide(IntroSlide.newInstance(R.layout.somnium_slide));
            addSlide(YouTubeSlide.newInstance(R.layout.youtube_slide));
            addSlide(IntroSlide.newInstance(R.layout.charge_slide));
        }

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(false);
        setVibrateIntensity(30);
    }

    // handle done and skip button
    public void transition() {

        // if user saw intro once disable automatic display
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isFirstRun = prefs.getBoolean("firstStart", true);

        if (isFirstRun) {

            Intent intent = new Intent(this, UserSetupActivity.class);
            startActivity(intent);
            finish();

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finishAfterTransition();
        }

        //  Make a new preferences editor
        SharedPreferences.Editor e = prefs.edit();
        //  Edit preference to make it false because we don't want this to run again
        e.putBoolean("firstStart", false);
        //  Apply changes
        e.apply();

    }

    @Override
    public void onSkipPressed() {
        transition();
    }

    @Override
    public void onDonePressed() {
        transition();
    }

    @Override
    public void onSlideChanged() {
        // Do something when the slide changes.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

}

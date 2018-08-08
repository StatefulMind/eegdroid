package de.uni_osnabrueck.traumschreiber.somnium;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

/**
 * Customised Slide Fragment to show the instructions video during the app instructions
 */
public class YouTubeSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String YoutubeDeveloperKey = "AIzaSyDE-3fx1NhsO__XAsZ55s-1rPvrcJgKbf8";
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private FragmentActivity myContext;
    private int layoutResId;
    private YouTubePlayer YoutubePlayer;

    public YouTubeSlide() {
    }

    // set up for object construction from Myintro class
    public static YouTubeSlide newInstance(int layoutResId) {
        YouTubeSlide sampleSlide = new YouTubeSlide();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);
        return sampleSlide;
    }

    @Override
    public void onAttach(Activity activity) {

        if (activity instanceof FragmentActivity) {
            myContext = (FragmentActivity) activity;
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);


    }

    /**
     * @return boolean if the device has possible internet access necessary for the video play
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //initialise and display youtube video
        final YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        youTubePlayerFragment.initialize(YoutubeDeveloperKey, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

                if (!wasRestored) {
                    YoutubePlayer = player;
                    YoutubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                    YoutubePlayer.cueVideo(getResources().getString(R.string.instructionsVideoId), 1);
                }

                // fallback if no network is available replace the video with some text link
                if (!isNetworkAvailable()) {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.remove(youTubePlayerFragment).commit();
                    // making the link inside the TextView work
                    TextView mLink = (TextView) getView().findViewById(R.id.youTubePlayerPlaceholderText);
                    if (mLink != null) {
                        mLink.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
            }

            /**
             * Called when initialization of the player fails, for example when the device has no
             * Youtube app/api installed. Replace with Text link.
             *
             * @param arg0
             * @param errorReason
             */
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult errorReason) {

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.remove(youTubePlayerFragment).commit();
                // making the link inside the TextView work
                TextView mLink = (TextView) getView().findViewById(R.id.youTubePlayerPlaceholderText);
                if (mLink != null) {
                    mLink.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        });

        // finish up UI
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_fragment, youTubePlayerFragment).commit();
        return inflater.inflate(layoutResId, container, false);
    }


}

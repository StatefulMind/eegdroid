package de.uni_osnabrueck.traumschreiber.somnium;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter that enables the swipe between Fragments in the MainActivity
 */

class SwipeFragmentPagerAdapter extends FragmentPagerAdapter {


    // declare tab titles here, since we use icons declared in the MainActivity keep empty.
    // icons are later set inside MainActivity
    private String tabTitles[] = new String[]{"", ""};
    private Context context;

    SwipeFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    // number of total fragments to create
    @Override
    public int getCount() {
        return tabTitles.length;
    }

    // order of fragments
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ConnectionFragment();
            case 1:
                return new DataFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
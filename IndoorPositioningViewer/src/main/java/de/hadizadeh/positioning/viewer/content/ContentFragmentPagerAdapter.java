package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;
import de.hadizadeh.positioning.viewer.R;

import java.util.List;

/**
 * Manages the tabs for content groups
 */
public class ContentFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragments;
    private Context context;

    /**
     * Creates the pager adapter for handling tabs
     *
     * @param fm        fragment manager
     * @param context   activity context
     * @param fragments list of all fragments which should be displayed
     */
    public ContentFragmentPagerAdapter(FragmentManager fm, Context context, List<Fragment> fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    /**
     * Returns the amount of available fragments
     *
     * @return amount of available fragments
     */
    @Override
    public int getCount() {
        return fragments.size();
    }

    /**
     * Returns a fragment at the defined index
     *
     * @param position index of the fragment
     * @return selected fragment
     */
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    /**
     * Returns the title text of the tab
     *
     * @param position tab index
     * @return title text
     */
    @Override
    public CharSequence getPageTitle(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment instanceof ContentTitleFragment) {
            return context.getString(R.string.content_tab_overview);
        } else if (fragment instanceof ContentUrlFragment) {
            return context.getString(R.string.content_tab_website);
        } else if (fragment instanceof ContentTextFragment) {
            return context.getString(R.string.content_tab_text);
        } else if (fragment instanceof ContentAudioFragment && !(fragment instanceof ContentVideoFragment)) {
            return context.getString(R.string.content_tab_audio);
        } else if (fragment instanceof ContentVideoFragment) {
            return context.getString(R.string.content_tab_video);
        }
        return "";
    }
}
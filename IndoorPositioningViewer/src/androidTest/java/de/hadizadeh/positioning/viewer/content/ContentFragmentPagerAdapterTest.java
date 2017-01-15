package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.test.InstrumentationTestCase;
import de.hadizadeh.positioning.viewer.R;

import java.util.ArrayList;
import java.util.List;

public class ContentFragmentPagerAdapterTest extends InstrumentationTestCase {
    private ContentFragmentPagerAdapter contentFragmentPagerAdapter;
    private List<Fragment> fragments;
    private Fragment contentTitleFragment;

    @Override
    protected void setUp() throws Exception {
        contentTitleFragment = new ContentTitleFragment();
        fragments = new ArrayList<Fragment>();
        fragments.add(contentTitleFragment);

        contentFragmentPagerAdapter = new ContentFragmentPagerAdapter(null, getInstrumentation().getTargetContext(), fragments);
    }

    public void testGetCount() throws Exception {
        assertEquals(1, contentFragmentPagerAdapter.getCount());
    }

    public void testGetItem() throws Exception {
        assertEquals(contentTitleFragment, contentFragmentPagerAdapter.getItem(0));
    }

    public void testGetPageTitle() throws Exception {
        assertEquals(getInstrumentation().getTargetContext().getString(R.string.content_tab_overview), contentFragmentPagerAdapter.getPageTitle(0));
    }
}
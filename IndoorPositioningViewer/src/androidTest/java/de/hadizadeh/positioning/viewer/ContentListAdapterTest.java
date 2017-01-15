package de.hadizadeh.positioning.viewer;

import android.test.InstrumentationTestCase;
import de.hadizadeh.positioning.viewer.content.ContentFragment;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ContentListAdapterTest extends InstrumentationTestCase {
    private ContentListAdapter contentListAdapter;
    private ContentFragment contentFragment;

    @Override
    protected void setUp() throws Exception {
        contentFragment = new ContentFragment();
        List<ContentFragment> contentFragments = new ArrayList<ContentFragment>();
        contentFragments.add(contentFragment);
        contentListAdapter = new ContentListAdapter(getInstrumentation().getContext(), 0, contentFragments);
    }

    public void testGetItem() throws Exception {
        assertEquals(contentFragment, contentListAdapter.getItem(0));

    }
}
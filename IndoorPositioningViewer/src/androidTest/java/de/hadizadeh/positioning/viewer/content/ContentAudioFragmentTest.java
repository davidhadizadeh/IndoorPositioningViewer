package de.hadizadeh.positioning.viewer.content;

import android.test.InstrumentationTestCase;

public class ContentAudioFragmentTest extends InstrumentationTestCase {
    private ContentAudioFragment contentAudioFragment;

    @Override
    protected void setUp() throws Exception {
        contentAudioFragment = new ContentAudioFragment();
    }

    public void testGetFormattedTime() throws Exception {
        assertEquals("01:40", contentAudioFragment.getFormattedTime(100000));
    }
}
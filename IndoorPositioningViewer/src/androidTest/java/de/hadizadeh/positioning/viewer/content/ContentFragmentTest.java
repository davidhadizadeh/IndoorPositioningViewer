package de.hadizadeh.positioning.viewer.content;

import junit.framework.TestCase;

public class ContentFragmentTest extends TestCase {

    private ContentFragment contentFragment;

    @Override
    protected void setUp() throws Exception {
        contentFragment = new ContentFragment();
        contentFragment.setData("title", "description", "image", "url", "fullText", "audioFileName", "videoFileName");
    }

    public void testCompareSame() throws Exception {
        assertTrue(contentFragment.compare("title", "description", "image", "fullText", "audioFileName", "videoFileName"));
    }

    public void testCompareDifferent() throws Exception {
        assertFalse(contentFragment.compare("otherTitle", "description", "image", "fullText", "audioFileName", "videoFileName"));
    }

    public void testGetTitle() throws Exception {
        assertEquals("title", contentFragment.getTitle());
    }

    public void testGetDescription() throws Exception {
        assertEquals("description", contentFragment.getDescription());
    }

    public void testCompareStringSame() throws Exception {
        assertTrue(contentFragment.compareString("text1", "text1"));
    }

    public void testCompareStringDifferent() throws Exception {
        assertFalse(contentFragment.compareString("text1", "text2"));
    }

    public void testCompareStringFirstNull() throws Exception {
        assertFalse(contentFragment.compareString(null, "text2"));
    }

    public void testCompareStringSecondNull() throws Exception {
        assertFalse(contentFragment.compareString("text1", null));
    }

    public void testCompareStringBothNull() throws Exception {
        assertTrue(contentFragment.compareString(null, null));
    }
}
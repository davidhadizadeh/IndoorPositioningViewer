package de.hadizadeh.positioning.viewer;

import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.android.ViewerMap;
import junit.framework.TestCase;

import java.util.ArrayList;

public class ViewerMapFragmentTest extends TestCase {
    private ViewerMapFragment viewerMapFragment;

    @Override
    protected void setUp() throws Exception {
        viewerMapFragment = new ViewerMapFragment();
        viewerMapFragment.setData(new ViewerMap(1,1,1,1, new ArrayList<MappingPoint>()), "floor");
    }

    public void testSetFollowPosition() throws Exception {
        viewerMapFragment.setFollowPosition(true);
        assertTrue(viewerMapFragment.getFollowPosition());
        viewerMapFragment.setFollowPosition(false);
        assertFalse(viewerMapFragment.getFollowPosition());
    }

    public void testChangeFloor() throws Exception {
        assertEquals("6. floor", viewerMapFragment.changeFloor(5));
    }
}
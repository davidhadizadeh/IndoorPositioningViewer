package de.hadizadeh.positioning.viewer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.hadizadeh.positioning.controller.MappedPositionListener;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import de.hadizadeh.positioning.viewer.content.ContentFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for exploring all available content groups
 */
public class ExploreContentFragment extends Fragment implements MappedPositionListener {
    private static List<ContentFragment> contentFragments;

    private List<ContentElement> contents;
    private ListView contentsLv;
    private ArrayAdapter adapter;
    private MappingPoint position;
    private int currentContentIndex = -1;
    private DataLoadedHandler handler;
    private FragmentManager fragmentManager;

    /**
     * Sets the contents and the current position
     *
     * @param contents content groups
     * @param position current position
     */
    public void setData(List<ContentElement> contents, MappingPoint position) {
        this.contents = contents;
        this.position = position;
    }

    /**
     * Creates the fragment view
     *
     * @param inflater           layout inflater
     * @param container          view contrainer
     * @param savedInstanceState saved instances
     * @return created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_explore_content, container, false);
        handler = new DataLoadedHandler(this);
        contentsLv = (ListView) view.findViewById(R.id.fragment_explore_content_lv);
        contentsLv.addHeaderView(new View(getActivity()), null, true);
        contentsLv.addFooterView(new View(getActivity()), null, true);
        if (contentFragments == null) {
            contentFragments = new ArrayList<ContentFragment>();
        }

        fragmentManager = getFragmentManager();
        adapter = new ContentListAdapter(getActivity(), R.layout.fragment_explore_content_lv_item, contentFragments);
        contentsLv.setAdapter(adapter);
        contentsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                currentContentIndex = (int) id;
                ContentFragment currentContentFragment = contentFragments.get(currentContentIndex);
                currentContentFragment.disableScanning();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.activity_main_fragment_fl, currentContentFragment, null);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
        });
        if (contentFragments.size() == 0) {
            loadContentFragments();
        }
        return view;
    }

    private void loadContentFragments() {
        new Thread() {
            public void run() {
                contentFragments = ContentFragment.createContentFragments(contents);
                Message message = ExploreContentFragment.this.handler.obtainMessage();
                handler.sendMessage(message);
            }
        }.start();
    }

    private void contentFragmentsLoaded() {
        adapter.clear();
        // adapter.addAll(contentFragments);
        for (ContentFragment contentFragment : contentFragments) {
            adapter.add(contentFragment);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Forwards the hardware button presses to the current content fragment
     *
     * @param event key event
     * @return true, if the event was handled, else it was not
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (currentContentIndex >= 0) {
            return contentFragments.get(currentContentIndex).dispatchKeyEvent(event);
        }
        return false;
    }

    /**
     * Called if a new position has been received
     *
     * @param mappingPoint new position
     */
    @Override
    public void positionReceived(MappingPoint mappingPoint) {

    }

    /**
     * Called if a new position has been received
     *
     * @param list new position list
     */
    @Override
    public void positionReceived(List<MappingPoint> list) {

    }

    /**
     * Called when the fragments loading finished
     */
    static class DataLoadedHandler extends Handler {
        private final ExploreContentFragment activity;

        DataLoadedHandler(ExploreContentFragment activity) {
            this.activity = activity;
        }

        public void handleMessage(Message msg) {
            this.activity.contentFragmentsLoaded();
        }
    }
}

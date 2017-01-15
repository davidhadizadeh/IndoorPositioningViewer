package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.hadizadeh.positioning.controller.MappedPositionManager;
import de.hadizadeh.positioning.model.MappingPoint;
import de.hadizadeh.positioning.roommodel.model.ContentElement;
import de.hadizadeh.positioning.viewer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for managing multiple content fragments of different types and joining them in a tab view
 */
public class ContentFragment extends Fragment {
    protected String title;
    protected String description;
    protected String image;
    protected boolean scanning;

    private String url;
    private String fullText;
    private String audioFileName;
    private String videoFileName;

    private ContentTitleFragment contentTitleFragment;
    private ContentUrlFragment contentUrlFragment;
    private ContentTextFragment contentTextFragment;
    private ContentAudioFragment contentAudioFragment;
    private ContentVideoFragment contentVideoFragment;

    private List<Fragment> fragments;

    /**
     * Sets the data of the containing fragments
     *
     * @param title         content title
     * @param description   content description
     * @param image         image
     * @param url           url
     * @param fullText      long description text
     * @param audioFileName path to the audio file
     * @param videoFileName path to the video file
     */
    public void setData(String title, String description, String image, String url, String fullText, String audioFileName, String videoFileName) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.scanning = true;
        this.url = url;
        this.fullText = fullText;
        this.audioFileName = audioFileName;
        this.videoFileName = videoFileName;
        contentTitleFragment = new ContentTitleFragment();
        contentTitleFragment.setData(title, description, image);
    }

    /**
     * Enables the scanning animation
     */
    public void enableScanning() {
        this.scanning = true;
    }

    /**
     * Disables the scanning animation
     */
    public void disableScanning() {
        this.scanning = false;
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
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        view.setClickable(true);

        FragmentManager fragmentManager = getFragmentManager();

        //ImageView gearIv = (ImageView) view.findViewById(R.id.fragment_content_gear_iv);
        // TODO: Enable Scanning Animation
//        if (scanning) {
//            RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//            anim.setInterpolator(new LinearInterpolator());
//            anim.setRepeatCount(Animation.INFINITE);
//            anim.setDuration(2000);
//            gearIv.startAnimation(anim);
//            gearIv.setVisibility(View.VISIBLE);
//        } else {
//            gearIv.setVisibility(View.GONE);
//        }

        if (savedInstanceState == null) {
            fragments = new ArrayList<Fragment>();
            fragments.add(contentTitleFragment);

            if (url != null) {
                contentUrlFragment = new ContentUrlFragment();
                contentUrlFragment.setData(url);
                fragments.add(contentUrlFragment);
            }
            if (fullText != null) {
                contentTextFragment = new ContentTextFragment();
                contentTextFragment.setData(fullText);
                fragments.add(contentTextFragment);
            }
            if (audioFileName != null) {
                contentAudioFragment = new ContentAudioFragment();
                contentAudioFragment.setData(audioFileName);
                fragments.add(contentAudioFragment);
            }
            if (videoFileName != null) {
                contentVideoFragment = new ContentVideoFragment();
                contentVideoFragment.setData(videoFileName);
                fragments.add(contentVideoFragment);
            }
        }

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.fragment_content_page_vp);
        viewPager.setAdapter(new ContentFragmentPagerAdapter(fragmentManager, getActivity(), fragments));
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.fragment_content_tabs_tl);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    /**
     * Handles the volume up and down events and adepts them to the internal volume control
     *
     * @param event key event
     * @return true, if the key event got handled, else it was ignored
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = false;
        if (contentAudioFragment != null) {
            result = contentAudioFragment.dispatchKeyEvent(event);
        }
        if (contentVideoFragment != null) {
            result = contentVideoFragment.dispatchKeyEvent(event);
        }
        return result;
    }

    /**
     * Compates the current fragment with data of another fragment
     *
     * @param title         content title
     * @param description   content description
     * @param image         image
     * @param fullText      long description text
     * @param audioFileName path to the audio file
     * @param videoFileName path to the video file
     * @return true, if the data are same, else they are different
     */
    public boolean compare(String title, String description, String image, String fullText, String audioFileName, String videoFileName) {
        return (this.title.equals(title) && this.description.equals(description) && this.image.equals(image));
    }

    /**
     * Returns the title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the description
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Compares the main text with another one, handles null-strings
     *
     * @param mainText    main text
     * @param compareText text to compare
     * @return true if the srings are equal, else they are different or one of them is null
     */
    protected boolean compareString(String mainText, String compareText) {
        if (mainText == null && compareText != null) {
            return false;
        }
        if (mainText != null && compareText == null) {
            return false;
        }
        return !(mainText != null && !mainText.equals(compareText));
    }

    /**
     * Clears the audio and video fragments if they are set (stops the players)
     */
    @Override
    public void onDestroy() {
        if (contentAudioFragment != null) {
            contentAudioFragment.clearPlayer();
        }
        if (contentVideoFragment != null) {
            contentVideoFragment.clearPlayer();
        }
        super.onDestroy();
    }

    /**
     * Returns a thumbnail of the image
     *
     * @return thumbnail
     */
    public Bitmap getThumbnail() {
        return contentTitleFragment.getThumbnail();
    }

    /**
     * Creates a content fragment out of all content data and the positon
     *
     * @param allContents  content data
     * @param mappingPoint position coordinates
     * @return created content fragment
     */
    public static ContentFragment createContentFragment(List<ContentElement> allContents, MappingPoint mappingPoint) {
        ContentElement contentElement = null;
        String mappingPointText = MappedPositionManager.mappingPointToName(mappingPoint);
        if (allContents != null) {
            for (ContentElement currentElement : allContents) {
                if (currentElement.getPositions().contains(mappingPointText)) {
                    contentElement = currentElement;
                    break;
                }
            }
        }
        return createContentFragment(contentElement);
    }

    /**
     * Creates multiple content fragments out of content data (groups content for each content group)
     *
     * @param allContents content data
     * @return created fragments
     */
    public static List<ContentFragment> createContentFragments(List<ContentElement> allContents) {
        List<ContentFragment> contentFragments = new ArrayList<ContentFragment>();
        if (allContents != null) {
            for (ContentElement contentElement : allContents) {
                contentFragments.add(createContentFragment(contentElement));
            }
        }
        return contentFragments;
    }

    /**
     * Creates a content fragment out of a created content element
     *
     * @param contentElement content element
     * @return created content fragment
     */
    public static ContentFragment createContentFragment(ContentElement contentElement) {
        if (contentElement != null) {
            ContentFragment contentFragment = new ContentFragment();
            String descriptionText = null;
            String fullText = null;
            String url = null;
            String imageFileName = null;
            String videoFileName = null;
            String audioFileName = null;
            if (contentElement.getDescription() != null) {
                descriptionText = contentElement.getDescription();
            }
            if (contentElement.getUrl() != null && !"".equals(contentElement.getUrl())) {
                url = contentElement.getUrl();
            }
            if (contentElement.getFullText() != null && !"".equals(contentElement.getFullText())) {
                fullText = contentElement.getFullText();
            }
            if (contentElement.getImageFile() != null && contentElement.getImageFile().exists()) {
                imageFileName = contentElement.getImageFile().getAbsolutePath();
            }
            if (contentElement.getVideoFile() != null && contentElement.getVideoFile().exists()) {
                videoFileName = contentElement.getVideoFile().getAbsolutePath();
            }
            if (contentElement.getAudioFile() != null && contentElement.getAudioFile().exists()) {
                audioFileName = contentElement.getAudioFile().getAbsolutePath();
            }
            contentFragment.setData(contentElement.getTitle(), descriptionText,
                    imageFileName, url, fullText, audioFileName, videoFileName);
            return contentFragment;
        }
        return null;
    }
}

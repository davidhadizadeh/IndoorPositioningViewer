package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hadizadeh.positioning.viewer.R;

/**
 * Fragment for displaying a long content description text
 */
public class ContentTextFragment extends Fragment {
    protected TextView fullTextTv;
    protected String fullText;

    /**
     * Sets the description text
     *
     * @param fullText long description text
     */
    public void setData(String fullText) {
        this.fullText = fullText;
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
        View view = inflater.inflate(R.layout.fragment_content_text, container, false);
        fullTextTv = (TextView) view.findViewById(R.id.fragment_content_text_tv);
        fullTextTv.setText(Html.fromHtml(fullText));
        //fullTextTv.setText(fullText);
        return view;
    }
}

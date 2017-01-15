package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import de.hadizadeh.positioning.viewer.R;

/**
 * Fragment for managing urls and showing them in a webview
 */
public class ContentUrlFragment extends Fragment {
    protected WebView webView;
    protected String url;

    /**
     * Sets the url
     *
     * @param url url
     */
    public void setData(String url) {
        this.url = url;
    }

    /**
     * Creates the fragment view and shows the url in the webview
     *
     * @param inflater           layout inflater
     * @param container          view contrainer
     * @param savedInstanceState saved instances
     * @return created view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_content_url, container, false);
        webView = (WebView) view.findViewById(R.id.fragment_content_url_wv);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.loadUrl(url);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        return view;
    }
}

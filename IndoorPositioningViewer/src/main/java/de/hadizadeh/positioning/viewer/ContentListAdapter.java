package de.hadizadeh.positioning.viewer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.hadizadeh.positioning.viewer.content.ContentFragment;

import java.util.List;

/**
 * List adapter for content elements
 */
public class ContentListAdapter extends ArrayAdapter<ContentFragment> {
    private Context context;
    private int layoutResourceId;
    private List<ContentFragment> data;

    /**
     * Creates the list adapter
     *
     * @param context          activity content
     * @param layoutResourceId layout of the listbox
     * @param data             content fragments to display in the list
     */
    public ContentListAdapter(Context context, int layoutResourceId, List<ContentFragment> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    /**
     * Returns a content group item
     *
     * @param position index of the content group
     * @return selected content group
     */
    @Override
    public ContentFragment getItem(int position) {
        return super.getItem(position);
    }

    /**
     * Returns the view
     *
     * @param position    index
     * @param convertView convert view
     * @param parent      parent view
     * @return content element view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        ContentFragment contentFragment = getItem(position);
        TextView contentTitleTv = (TextView) convertView.findViewById(R.id.fragment_explore_content_lv_item_title_tv);
        TextView contentDescriptionTv = (TextView) convertView.findViewById(R.id.fragment_explore_content_lv_item_description_tv);

        if (contentFragment.getThumbnail() != null) {
            ImageView contentIv = (ImageView) convertView.findViewById(R.id.fragment_explore_content_lv_item_iv);
            contentIv.setImageBitmap(contentFragment.getThumbnail());
        }

        contentTitleTv.setText(contentFragment.getTitle());
        contentDescriptionTv.setText(contentFragment.getDescription());
        return convertView;
    }
}

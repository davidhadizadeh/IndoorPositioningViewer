package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.hadizadeh.positioning.viewer.R;

/**
 * Fragment for displaying the title, a short description and an image of the content
 */
public class ContentTitleFragment extends Fragment {
    protected TextView titleTv;
    protected TextView descriptionTv;
    protected ImageView imageIv;
    protected String title;
    protected String description;
    protected String image;
    protected Bitmap thumbnail;

    /**
     * Sets the data of the fragment
     *
     * @param title       title
     * @param description description
     * @param image       image
     */
    public void setData(String title, String description, String image) {
        this.title = title;
        this.description = description;
        this.image = image;
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(image, new BitmapFactory.Options());
            if(bitmap != null) {
                thumbnail = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            }
        }
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
        View view = inflater.inflate(R.layout.fragment_content_main, container, false);
        view.setClickable(true);

        titleTv = (TextView) view.findViewById(R.id.fragment_content_main_title_tv);
        descriptionTv = (TextView) view.findViewById(R.id.fragment_content_main_description_tv);
        imageIv = (ImageView) view.findViewById(R.id.fragment_content_main_image_iv);
        if (imageIv != null) {
            imageIv.setImageBitmap(thumbnail);
            if (thumbnail == null) {
                imageIv.setVisibility(View.GONE);
            }
        }
        titleTv.setText(title);
        descriptionTv.setText(description);
        if (description == null) {
            descriptionTv.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * Returns the thumbnail of the image
     *
     * @return thumbnail
     */
    public Bitmap getThumbnail() {
        return thumbnail;
    }
}

package de.hadizadeh.positioning.viewer.content;

import android.os.Bundle;
import android.view.*;
import de.hadizadeh.positioning.viewer.R;

/**
 * Fragment for showing a video player
 */
public class ContentVideoFragment extends ContentAudioFragment {
    protected String videoFileName;
    protected SurfaceView videoSv;
    protected SurfaceHolder holder;

    /**
     * Sets the path to the video file
     *
     * @param videoFileName path to the video file
     */
    @Override
    public void setData(String videoFileName) {
        this.videoFileName = videoFileName;
        layoutId = R.layout.fragment_content_video;
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
        View view = super.onCreateView(inflater, container, savedInstanceState);
        videoSv = (SurfaceView) view.findViewById(R.id.fragment_content_video_sv);
        holder = videoSv.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mediaPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        return view;
    }

    /**
     * Clears the media player display when the orientation is changed
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        mediaPlayer.setDisplay(null);
        super.onSaveInstanceState(outState);
    }

    /**
     * Sets the video file
     *
     * @param mediaFile video file
     */
    @Override
    protected void setFile(String mediaFile) {
        super.setFile(videoFileName);
    }

    /**
     * Resets the player
     */
    @Override
    public void clearPlayer() {
        mediaPlayer.setDisplay(null);
        super.clearPlayer();
    }
}

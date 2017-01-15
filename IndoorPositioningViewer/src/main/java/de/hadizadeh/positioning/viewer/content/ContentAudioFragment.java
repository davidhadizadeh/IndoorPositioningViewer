package de.hadizadeh.positioning.viewer.content;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import de.hadizadeh.positioning.viewer.R;

/**
 * Fragment for displaying an audio player for contents which have an audio file
 */
public class ContentAudioFragment extends Fragment {
    /**
     * Defines the speaker where the sound will be played from
     */
    public static final int PLAYER_STREAM_TYPE = AudioManager.STREAM_MUSIC; //AudioManager.STREAM_VOICE_CALL;

    protected ImageView playButtonIv;
    protected SeekBar playerSB;
    protected TextView minutesPlayedTv;
    protected TextView minutesTotalTv;
    protected boolean started;
    protected int seekPosition;
    protected MediaPlayer mediaPlayer;
    protected String audioFileName;
    protected Handler playerProgressHandler;
    protected int layoutId;

    protected AudioManager audioManager;
    protected SeekBar volumeSB;
    protected ImageView volumeMinusIv;
    protected ImageView volumePlusIv;
    protected RelativeLayout volumeBox;

    /**
     * Sets the audio file name
     *
     * @param audioFileName audio file name
     */
    public void setData(String audioFileName) {
        this.audioFileName = audioFileName;
        this.layoutId = R.layout.fragment_content_audio;
        seekPosition = 0;
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
        View view = inflater.inflate(layoutId, container, false);
        playButtonIv = (ImageView) view.findViewById(R.id.fragment_content_play_button_iv);
        playerSB = (SeekBar) view.findViewById(R.id.fragment_content_player_sb);
        minutesPlayedTv = (TextView) view.findViewById(R.id.fragment_content_player_minutes_played_tv);
        minutesTotalTv = (TextView) view.findViewById(R.id.fragment_content_player_minutes_total_tv);

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        volumeBox = (RelativeLayout) view.findViewById(R.id.fragment_content_volume_rl);
        volumeSB = (SeekBar) view.findViewById(R.id.fragment_content_volume_sb);
        volumeMinusIv = (ImageView) view.findViewById(R.id.fragment_content_volume_minus_iv);
        volumePlusIv = (ImageView) view.findViewById(R.id.fragment_content_volume_plus_iv);

        volumeSB.setMax(audioManager.getStreamMaxVolume(ContentAudioFragment.PLAYER_STREAM_TYPE));
        volumeSB.setProgress(audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE));

        volumeSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumeMinusIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE, audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE) - 1, 0);
                volumeSB.setProgress(volumeSB.getProgress() - 1);
            }
        });

        volumePlusIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE, audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE) + 1, 0);
                volumeSB.setProgress(volumeSB.getProgress() + 1);
            }
        });

        if (savedInstanceState == null) {
            setFile(audioFileName);
        }
        initPlayer();
        return view;
    }

    /**
     * Resets the current seek position of the audio player if the fragment resumes
     */
    @Override
    public void onResume() {
        super.onResume();
        playerSB.setProgress(seekPosition);
    }

    /**
     * Initializes the media player and its describing texts
     */
    protected void initPlayer() {
        minutesPlayedTv.setText(getFormattedTime(0));
        minutesTotalTv.setText(getFormattedTime(mediaPlayer.getDuration()));
        playerSB.setProgress(0);
        playerSB.setMax(mediaPlayer.getDuration());

        playerProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Integer currentPosition = (Integer) msg.obj;
                minutesPlayedTv.setText(getFormattedTime(currentPosition));
            }
        };

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                started = false;
                setPlayIcon();
                playerSB.setProgress(0);
                minutesPlayedTv.setText(getFormattedTime(0));
            }
        });

        playButtonIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });

        playerSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekPosition = progress;
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (started) {
            started = false;
            startPlay();
        }

        Thread playerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = 0;
                int total = mediaPlayer.getDuration();
                while (mediaPlayer != null && currentPosition < total) {
                    try {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        if (started) {
                            seekPosition = currentPosition;
                            playerSB.setProgress(currentPosition);
                        }
                        Message message = playerProgressHandler.obtainMessage();
                        message.obj = (Integer) currentPosition;
                        playerProgressHandler.sendMessage(message);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        playerThread.start();
    }

    /**
     * Starts playing the audio file
     */
    protected void startPlay() {
        if (!started) {
            setPauseIcon();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
        } else {
            setPlayIcon();
            mediaPlayer.pause();
        }
        started = !started;
    }

    /**
     * Stops playing
     */
    public void stopPlay() {
        started = false;
        mediaPlayer.stop();
        mediaPlayer.setVolume(0.0f, 0.0f);
        setPlayIcon();
        playerSB.setProgress(0);
        minutesPlayedTv.setText(getFormattedTime(0));
        //mediaPlayer.reset();
    }

    /**
     * Sets the current media file for the player
     *
     * @param mediaFile
     */
    protected void setFile(String mediaFile) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        started = false;
        try {
            mediaPlayer.setAudioStreamType(PLAYER_STREAM_TYPE);
            mediaPlayer.setDataSource(mediaFile);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    //initPlayer();
                }
            });
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the play icon for the player
     */
    protected void setPlayIcon() {
        playButtonIv.setImageResource(R.drawable.audio_play);
    }

    /**
     * Sets the pause icon for the player
     */
    protected void setPauseIcon() {
        playButtonIv.setImageResource(R.drawable.audio_pause);
    }

    /**
     * Stops playing and resets the seek position to the start
     */
    public void clearPlayer() {
        if (started) {
            try {
                stopPlay();
            } catch (Exception e) {
            }
            seekPosition = 0;
        }
    }

    /**
     * Handles the volume up and down events and adepts them to the internal volume control
     *
     * @param event key event
     * @return true, if the key event got handled, else it was ignored
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager.setStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE, audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE) + 1, 0);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager.setStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE, audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE) - 1, 0);
        }
        volumeSB.setProgress(audioManager.getStreamVolume(ContentAudioFragment.PLAYER_STREAM_TYPE));
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return false;
    }

    /**
     * Creates a formatted time text out of milli seconds
     *
     * @param milliseconds milli seconds
     * @return formatted time text
     */
    protected String getFormattedTime(int milliseconds) {
        StringBuffer timeString = new StringBuffer();
        int hours = milliseconds / (1000 * 60 * 60);
        int minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000;
        if (hours > 0) {
            timeString.append(String.format("%02d", hours));
        }
        timeString.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        return timeString.toString();
    }
}

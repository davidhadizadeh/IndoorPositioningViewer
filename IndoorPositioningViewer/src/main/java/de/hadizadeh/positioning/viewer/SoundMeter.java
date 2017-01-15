package de.hadizadeh.positioning.viewer;

import android.media.*;
import android.os.Handler;

/**
 * Possible usage in future for implementing ultrasound
 */
public class SoundMeter {
    private AudioRecord ar = null;
    private int minSize;
    private boolean recording;

    private final int duration = 60; // seconds
    private final int sampleRate = 48000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 440; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private Handler handler = new Handler();

    /**
     * Creates the sound meter
     */
    public SoundMeter() {
        genTone();
    }

    /**
     * Starts logging sound
     */
    public void start() {
        minSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        ar.startRecording();
        recording = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (recording) {
                    System.out.println("Amplitude: " + getAmplitude());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).run();
    }

    /**
     * Stops recording
     */
    public void stop() {
        recording = false;
        if (ar != null) {
            ar.stop();
        }
    }

    /**
     * Returns the amplitude
     *
     * @return amplitude
     */
    public double getAmplitude() {
        // 20000
        short[] buffer = new short[minSize];
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer) {
            if (Math.abs(s) > max) {
                max = Math.abs(s);
            }
        }
        return max;
    }

    /**
     * Generates a sound
     */
    void genTone() {
//        // fill out the array
//        for (int i = 0; i < numSamples; ++i) {
//            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
//        }
//
//        // convert to 16 bit pcm sound array
//        // assumes the sample buffer is normalised.
//        int idx = 0;
//        for (final double dVal : sample) {
//            // scale to maximum amplitude
//            final short val = (short) ((dVal * 32767));
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//
//        }


        int samplingFreq = 48000;
        int f0 = 20000;//ultrasonic tone

        for (int i = 0; i < numSamples; i++) {
            generatedSnd[i] = (byte) Math.cos(2 * Math.PI * f0 / samplingFreq * i);
        }
    }

    /**
     * Plays a sound
     */
    public void playSound() {
        System.out.println("PLAYING SOUND");
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
}

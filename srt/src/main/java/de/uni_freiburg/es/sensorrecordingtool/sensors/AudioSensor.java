package de.uni_freiburg.es.sensorrecordingtool.sensors;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Not wokring at all.
 * <p>
 * Created by phil on 4/26/16.
 */
public class AudioSensor extends Sensor {
    protected static final String TAG = AudioSensor.class.getName();
    protected final Context context;
    private int mRateinMus = 0;


    private static int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private RecorderThread mRecorderThread;

    public AudioSensor(Context c) {
        super(c, 1);
        context = c;
    }

    @Override
    public void prepareSensor() {
        setPrepared();
    }


    @Override
    public String getStringName() {
        return "Audio";
    }

    @Override
    public String getStringType() {
        return "android.hardware.audio";
    }

    @Override
    public void registerListener(SensorEventListener l, int rate_in_mus, int delay, String format, Handler h) {
        //if (!PermissionDialog.microphone(context))
        //    return;

        if (mListeners.size() == 0) {
            mRateinMus = rate_in_mus;
            if(mRecorderThread == null)
                mRecorderThread = new RecorderThread();
            mRecorderThread.startRecording();
        }

        super.registerListener(l, rate_in_mus, delay, format, h);
    }


    @Override
    public void unregisterListener(SensorEventListener l) {
        super.unregisterListener(l);

        if (mListeners.size() == 0)
            mRecorderThread.stopRecording();
    }


    /*
     * Valid Audio Sample rates
     *
     * @see <a
     * href="http://en.wikipedia.org/wiki/Sampling_%28signal_processing%29"
     * >Wikipedia</a>
     */
    private static final int validSampleRates[] = new int[]{8000, 11025, 16000, 22050,
            32000, 37800, 44056, 44100, 47250, 4800, 50000, 50400, 88200,
            96000, 176400, 192000, 352800, 2822400, 5644800};

    public static int getAudioSampleRate() {

    /*
     * Selecting default audio input source for recording since
     * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
     * default encoding format.
     */
        for (int i = 0; i < validSampleRates.length; i++) {
            int result = AudioRecord.getMinBufferSize(validSampleRates[i],
                    mChannelConfig,
                    mAudioFormat);
            if (result > 0) {
                // return the mininum supported audio sample rate
                return validSampleRates[i];
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return -1;

    }

    public static List<Integer> getSupportedAudioSampleRates() {
        List<Integer> list = new ArrayList<>();
    /*
     * Selecting default audio input source for recording since
     * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
     * default encoding format.
     */
        for (int i = 0; i < validSampleRates.length; i++) {
            int result = AudioRecord.getMinBufferSize(validSampleRates[i],
                    mChannelConfig,
                    mAudioFormat);
            if (result > 0) {
                // return the mininum supported audio sample rate
                list.add(validSampleRates[i]);
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return list;

    }

    class RecorderThread extends Thread {

        private int sampleRate =  (int) ((1000 * 1000f) / mRateinMus);
        private AudioRecord mAudioRecorder;

        private int minBufSize = AudioRecord.getMinBufferSize(sampleRate, mChannelConfig, mAudioFormat);

        private boolean status = true;


        RecorderThread() {
            mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, mChannelConfig, mAudioFormat, minBufSize);
        }

        @Override
        public void run() {
            super.run();
            mAudioRecorder.startRecording();

            while (status) {
                byte[] buffer = new byte[minBufSize]; // TODO use short[] and only half of the buffer
                if (mAudioRecorder.read(buffer, 0, buffer.length) > 0) {
                    mEvent.timestamp = System.currentTimeMillis() * 1000 * 1000;
                    mEvent.rawdata = buffer;
                    // Log.e(TAG, mEvent.timestamp + ", " + Arrays.toString(mEvent.rawdata));
                    notifyListeners();
                } else
                    Log.e(TAG, "skipped block");
            }
        }

        public void startRecording() {
            status = true;
            this.start();
        }

        public void stopRecording() {
            status = false;
            mAudioRecorder.stop();
            mAudioRecorder.release();
        }


    }
}

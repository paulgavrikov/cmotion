package de.uni_freiburg.es.sensorrecordingtool.sensors;

import android.content.Context;
import android.os.Handler;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import de.uni_freiburg.es.sensorrecordingtool.FFMpegProcess;

/**
 * Created by phil on 9/1/16.
 */
public class BlockSensorProcess extends SensorProcess {
    public BlockSensorProcess(Context c, String sensor, double rate, String format, double dur,
                              OutputStream bf) throws Exception {
        super(c, sensor, rate, format, dur, bf, new Handler(c.getMainLooper()));
    }

    public BlockSensorProcess(Context c, String sensor, double rate, String format, double dur, OutputStream os, Handler handler) throws Exception {
        super(c, sensor, rate, format, dur, os, handler);
    }

    public BlockSensorProcess(Context c, String sensor, double rate, String format, double dur, FFMpegProcess p, int j, Handler handler) throws Exception {
        super(c,sensor,rate,format,dur,p,j, handler);
    }

    @Override
    public byte[] transfer(SensorEvent sensorEvent) {
        return sensorEvent.rawdata;
    }
}

package nl.elec332.sdr.source.hackrf;

import com.google.common.base.Preconditions;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FunctionPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Cast;
import org.bytedeco.javacpp.annotation.Name;
import org.bytedeco.javacpp.annotation.Opaque;
import org.bytedeco.javacpp.annotation.Platform;

/**
 * Created by Elec332 on 19-3-2020
 */
@Platform(include = "source_hackrf.h", link = "source_hackrf", preload = {"libusb-1.0", "pthreadVC2", "hackrf"})
public class LibHackRF {

    public static final int[] OPTIMAL_SAMPLE_RATES = {2000000, 4000000, 8000000, 10000000, 12500000, 16000000, 20000000};
    public static final int BYTE_BUF_SIZE = 256 * 1024;

    private static final int DEVICE_ID_LENGTH = 32;
    private static final char[][] devices;

    public static native HackRFDevice hrfd_open(int deviceId);

    protected static native boolean hrfd_close(HackRFDevice device);

    protected static native boolean hrfd_setFrequency(HackRFDevice device, @Cast("uint64_t") long frequency);

    protected static native boolean hrfd_setSampleRate(HackRFDevice device, int sampleRate);

    protected static native boolean hrfd_setLNAGain(HackRFDevice device, @Cast("uint32_t") int gain);

    protected static native boolean hrfd_setVGAGain(HackRFDevice device, @Cast("uint32_t") int sampleRate);

    protected static native boolean hrfd_setBiasTeeEnabled(HackRFDevice device, boolean enabled);

    protected static native boolean hrfd_setHWSyncMode(HackRFDevice device, @Cast("uint8_t ") int mode);

    protected static boolean startRx(HackRFDevice device) {
        return hrfd_startRx(device, Preconditions.checkNotNull(device.getCallback()));
    }

    private static native boolean hrfd_startRx(HackRFDevice device, AbstractReceiveCallback ptr);

    protected static native boolean hrfd_stopRx(HackRFDevice device);

    @SuppressWarnings("unused")
    protected static abstract class AbstractReceiveCallback extends FunctionPointer {

        public AbstractReceiveCallback() {
            allocate();
        }

        private native void allocate();

        @Name("runCallback")
        public abstract int call(Transfer data);

    }

    @Opaque
    @Name("hackrf_transfer")
    @SuppressWarnings("unused")
    protected static class Transfer extends Pointer {

        @Cast("uint8_t*")
        public native BytePointer buffer();

        private native Transfer buffer(@Cast("uint8_t*") BytePointer length);

        public native int buffer_length();

        private native Transfer buffer_length(int len);

        public native int valid_length();

        private native Transfer valid_length(int len);

    }

    public static int getDeviceCount() {
        return devices.length;
    }

    public static String[] getDeviceStrings() {
        String[] ret = new String[devices.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = String.copyValueOf(devices[i]);
        }
        return ret;
    }

    public static char[][] getDevices() {
        char[][] ret = new char[devices.length][];
        for (int i = 0; i < devices.length; i++) {
            char[] cp = new char[DEVICE_ID_LENGTH];
            System.arraycopy(devices[i], 0, cp, 0, DEVICE_ID_LENGTH);
            ret[i] = cp;
        }
        return ret;
    }

    private static native int hrfd_getDeviceCountInternal();

    private static native void hrfd_getDevices(int[] link);

    static {
        Loader.load(LibHackRF.class);
        devices = new char[hrfd_getDeviceCountInternal()][];
        int[] buffer = new int[DEVICE_ID_LENGTH * devices.length];
        hrfd_getDevices(buffer);
        for (int i = 0; i < devices.length; i++) {
            char[] mapped = new char[DEVICE_ID_LENGTH];
            for (int j = 0; j < DEVICE_ID_LENGTH; j++) {
                mapped[j] = (char) buffer[DEVICE_ID_LENGTH * i + j];
            }
            devices[i] = mapped;
        }
    }

}

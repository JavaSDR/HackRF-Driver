package nl.elec332.sdr.source.hackrf;

import nl.elec332.sdr.lib.SDRLibrary;
import nl.elec332.sdr.lib.api.datastream.ISampleDataSetter;
import nl.elec332.sdr.lib.api.util.IDataConverter;
import nl.elec332.sdr.lib.source.device.AbstractNativeRFDevice;
import nl.elec332.sdr.lib.util.SourceHelper;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.annotation.Cast;
import org.bytedeco.javacpp.annotation.Name;
import org.bytedeco.javacpp.annotation.Opaque;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 19-3-2020
 */
@Opaque
@Name("hackrf_device")
public class HackRFDevice extends AbstractNativeRFDevice<byte[]> {

    private static final long RESET_FREQUENCY = 200000000;
    private static final IDataConverter converter = SDRLibrary.getInstance().getCachedDataConverterFactory().createConverter(8, false, true);

    private LibHackRF.AbstractReceiveCallback callback;
    private BiConsumer<BytePointer, Integer> listener;

    private int sampleRate = 0;
    private long frequency = 0;
    private int hwSync;

    @Override
    protected long setDeviceFrequency(long freq) {
        this.frequency = freq;
        long offset = freq > RESET_FREQUENCY * 2 ? -RESET_FREQUENCY : RESET_FREQUENCY;
        LibHackRF.hrfd_setFrequency(this, freq + offset); //Bug in HackRF
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LibHackRF.hrfd_setFrequency(this, freq);
        return this.frequency;
    }

    @Override
    protected long setDeviceSampleRate(long sampleRate) {
        this.sampleRate = SourceHelper.getNextHigherOptimalSampleRate(sampleRate, LibHackRF.OPTIMAL_SAMPLE_RATES);
        LibHackRF.hrfd_setSampleRate(this, this.sampleRate);
        return this.sampleRate;
    }

    @Override
    protected int setDeviceLNAGain(int gain) {
        LibHackRF.hrfd_setLNAGain(this, gain);
        return gain;
    }

    public void setVGAGain(int gain) {
        LibHackRF.hrfd_setVGAGain(this, gain);
    }

    public void setBiasTeeEnabled(boolean enabled) {
        LibHackRF.hrfd_setBiasTeeEnabled(this, enabled);
    }

    public void setHWSyncMode(@Cast("int16_t") int mode) {
        this.hwSync = mode;
        LibHackRF.hrfd_setHWSyncMode(this, mode);
    }

    @Override
    public long getFrequency() {
        return this.frequency;
    }

    @Override
    public int getSampleRate() {
        return this.sampleRate;
    }

    @Override
    public int getSamplesPerBuffer() {
        return LibHackRF.BYTE_BUF_SIZE / 2;
    }

    @Override
    public byte[] createBuffer() {
        return new byte[LibHackRF.BYTE_BUF_SIZE];
    }

    @Override
    protected void startDeviceSampling(Consumer<Consumer<byte[]>> bufferGetter) {
        this.clearListeners();
        this.addListener((data, length) ->
                bufferGetter.accept(buf -> {
                            if (length != LibHackRF.BYTE_BUF_SIZE) {
                                throw new RuntimeException("Dropped data");
                            }
                            data.get(buf);
                        }
                ));
        if (hwSync == 0) {
            setHWSyncMode(0);
        }
        LibHackRF.startRx(this);
    }

    @Override
    public void setSampleData(ISampleDataSetter sampleDataSetter, byte[] buffer) {
        converter.readData(buffer, sampleDataSetter);
    }

    @Override
    protected void stopDevice() {
        LibHackRF.hrfd_stopRx(this);
    }

    @Override
    protected void closeDevice() {
        LibHackRF.hrfd_close(this);
    }

    protected LibHackRF.AbstractReceiveCallback getCallback() {
        if (callback == null) {
            this.callback = new LibHackRF.AbstractReceiveCallback() {

                @Override
                public int call(LibHackRF.Transfer data) {
                    try {
                        listener.accept(data.buffer(), data.valid_length());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return 0;
                }

            };
        }
        return this.callback;
    }

    public void addListener(BiConsumer<BytePointer, Integer> listener) {
        Objects.requireNonNull(listener);
        if (this.listener == null) {
            this.listener = (a, b) -> {
            };
        }
        this.listener = this.listener.andThen(listener);
    }

    public void clearListeners() {
        this.listener = null;
    }

}

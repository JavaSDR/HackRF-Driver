package nl.elec332.sdr.source.hackrf;

import nl.elec332.lib.java.swing.IDefaultListCellRenderer;
import nl.elec332.lib.java.swing.LinedGridBagConstraints;
import nl.elec332.sdr.lib.source.AbstractInputHandler;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-3-2020
 */
public class HackRFInputHandler extends AbstractInputHandler<HackRFDevice, byte[]> {

    public HackRFInputHandler() {
        this.deviceId = 0;
        this.gain = 20;
        this.vgaGain = 16;
        this.sampleRate = LibHackRF.OPTIMAL_SAMPLE_RATES[LibHackRF.OPTIMAL_SAMPLE_RATES.length / 2];
        this.biasT = false;
    }

    private int deviceId;
    private int gain, vgaGain;
    private int sampleRate;
    private boolean biasT;

    @Nonnull
    @Override
    public String getDisplayString() {
        return "HackRF";
    }

    @Override
    protected HackRFDevice createNewDevice() {
        return LibHackRF.hrfd_open(deviceId);
    }

    @Override
    protected void modifyNewDevice(HackRFDevice device) {
        device.setSampleRate(sampleRate);
        device.setLNAGain(gain);
        device.setVGAGain(vgaGain);
        device.addListener(this);
        device.setBiasTeeEnabled(biasT);
    }

    @Override
    protected boolean createNewInterface(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        int line = 0;

        JPanel line0 = new JPanel();
        line0.add(new JLabel("Device ID: "));
        JComboBox<String> deviceChooser = new JComboBox<>(LibHackRF.getDeviceStrings());
        deviceChooser.addActionListener(a -> deviceId = deviceChooser.getSelectedIndex());
        deviceChooser.setSelectedIndex(deviceId);
        listeners.add(deviceChooser);
        line0.add(deviceChooser);
        panel.add(line0, new LinedGridBagConstraints(line).alignLeft());

        JPanel line1 = new JPanel();
        line1.add(new JLabel("Sample Rate: "));
        JComboBox<Integer> sampleBox = new JComboBox<>(new Vector<>(Arrays.stream(LibHackRF.OPTIMAL_SAMPLE_RATES).boxed().collect(Collectors.toList())));
        sampleBox.setRenderer(IDefaultListCellRenderer.getCustomName(sr -> sr / 1000000 + " MSPS"));
        sampleBox.addActionListener(a -> {
            int sampleRate = LibHackRF.OPTIMAL_SAMPLE_RATES[sampleBox.getSelectedIndex()];
            this.sampleRate = sampleRate;
            getCurrentDevice().ifPresent(d -> d.setSampleRate(sampleRate));
        });
        sampleBox.setSelectedItem(this.sampleRate);
        listeners.add(sampleBox);
        line1.add(sampleBox);
        panel.add(line1, new LinedGridBagConstraints(++line).alignLeft());

        JPanel line1p5 = new JPanel();
        line1p5.add(new JLabel("Bias-Tee"));
        JCheckBox bt = new JCheckBox();
        bt.setSelected(biasT);
        bt.addActionListener(a -> {
            biasT = bt.isSelected();
            getCurrentDevice().ifPresent(d -> d.setBiasTeeEnabled(biasT));
        });
        line1p5.add(bt);
        panel.add(line1p5, new LinedGridBagConstraints(line).alignRight());

        JPanel line2 = new JPanel();
        line2.add(new JLabel("LNA Gain: "));
        int startVal = 3;
        JLabel lnaLabel = new JLabel(startVal * 8 + " dB  ");
        lnaLabel.setPreferredSize(new Dimension(40, lnaLabel.getPreferredSize().height));
        JSlider lnaSlider = new JSlider(0, 5, 3);
        lnaSlider.setMajorTickSpacing(1);
        lnaSlider.setPaintTicks(true);
        lnaSlider.addChangeListener(a -> {
            int gain = lnaSlider.getValue() * 8;
            lnaLabel.setText(gain + " dB");
            this.gain = gain;
            getCurrentDevice().ifPresent(d -> d.setLNAGain(gain));
        });
        line2.add(lnaSlider);
        line2.add(new JPanel());
        line2.add(lnaLabel);
        panel.add(line2, new LinedGridBagConstraints(++line));

        JPanel line3 = new JPanel();
        line3.add(new JLabel("VGA Gain: "));
        startVal = 4;
        JLabel vgaLabel = new JLabel(startVal * 2 + " dB  ");
        vgaLabel.setPreferredSize(new Dimension(40, vgaLabel.getPreferredSize().height));
        JSlider vgaSlider = new JSlider(0, 31, startVal);
        vgaSlider.setMinorTickSpacing(1);
        vgaSlider.setMajorTickSpacing(4);
        vgaSlider.setPaintTicks(true);
        vgaSlider.addChangeListener(a -> {
            int gain = vgaSlider.getValue() * 2;
            vgaLabel.setText(gain + " dB");
            this.vgaGain = gain;
            getCurrentDevice().ifPresent(d -> d.setVGAGain(gain));
        });
        line3.add(vgaSlider);
        line3.add(new JPanel());
        line3.add(vgaLabel);
        panel.add(line3, new LinedGridBagConstraints(++line));
        return true;
    }

}

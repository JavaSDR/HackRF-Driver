import nl.elec332.sdr.lib.api.source.IInputHandler;
import nl.elec332.sdr.source.hackrf.HackRFInputHandler;

/**
 * Created by Elec332 on 22-4-2020
 */
module nl.elec332.sdr.source.hackrf {

    requires jsr305;
    requires java.desktop;
    requires transitive nl.elec332.sdr.lib;
    requires org.bytedeco.javacpp;

    provides IInputHandler with HackRFInputHandler;

}
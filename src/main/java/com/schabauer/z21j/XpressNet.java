/**
 * This is an XpressNet - Corba Interface. At startup it is only a mock up,
 * filled with default data. Only when it connects to the source XpressNet
 * Object, which is located on an Arduino, it reads and writes real data from
 * the XpressNet bus. It calls remote procedures of the source object and reads
 * back the results by tunneling XpressNet via UDP telegrams.
 *
 * Requirements: XpressNet interface with XpressNet UDP Tunnel (Arduino)
 *
 * @author johannes
 */
package com.schabauer.z21j;

import java.net.InetAddress;

public class XpressNet {
    // certain global XPressnet status indicators

    public static final int csNormal = 0x00; // Normal Operation Resumed ist eingeschaltet
    public static final int csEmergencyStop = 0x01; // Der Nothalt ist eingeschaltet
    public static final int csTrackVoltageOff = 0x02; // Die Gleisspannung ist abgeschaltet
    public static final int csShortCircuit = 0x04; // Kurzschluss
    public static final int csServiceMode = 0x20; // Der Programmiermodus ist aktiv - Service Mode

    public int getPower() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return csNormal;
    }

    public void connect (InetAddress ip, int port) {

    }

    Boolean setPower(int csTrackVoltageOff) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        propagateTel(currentTel);
    }

    void readCVMode(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void writeCVMode(int i, int value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean getTrntInfo(int packetBuffer, int packetBuffer0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean setTrntPos(int packetBuffer, int packetBuffer0, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean getLocoInfo(int i, int packetBuffer) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    boolean getLocoFunc(int i, int packetBuffer) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return true;
    }

    boolean setLocoFunc(int i, int packetBuffer, int i0, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Boolean setLocoDrive(int i, int packetBuffer, int i0, int packetBuffer0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

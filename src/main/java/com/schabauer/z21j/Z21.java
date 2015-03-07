/* 
 * File:   z21.java
 * Author: Johannes Schabauer
 *
 * Created on 7. Februar 2015, 18:54
 */
package com.schabauer.z21j;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.*;

public class Z21 {

    public static int Z21_Port = 21105;
    public static byte XBusVer = 0x30;
    public static byte[] packetBuffer = new byte[64]; //buffer to hold incoming packet,
    //public static TypeActIP[] ActIP = null; //Speicherarray für IPs
    private HashMap<InetAddress, Timestamp> ActIP = new HashMap<InetAddress, Timestamp>();
    private static final long inActInt = 2000;

    private static InetAddress clientIP;           //IP of the currently (last) connected Client
    private static DatagramSocket serverSocket;
    private static int clientPort = 0;

    final static Logger logger = Logger.getLogger(Z21.class);
    private XpressNet xpn = new XpressNet();

    public static void main(String args[]) throws Exception {

        byte[] receiveData = new byte[32];
        byte[] sendData = new byte[32];
        String sentence;
        Z21 z21 = new Z21();

        InetAddress localIP = Utils.getLocalHostLANAddress();

        logger.info(("Starting Z21 at LAN Adress " + localIP + ":" + Z21_Port));

        serverSocket = new DatagramSocket(null);
        InetSocketAddress address = new InetSocketAddress(localIP, Z21_Port);
        serverSocket.bind(address);
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            int receivedDataLen = receivePacket.getLength();
            if (receivedDataLen > 0) {
                clientPort = receivePacket.getPort();
                clientIP = receivePacket.getAddress();
                sentence = new String(receivePacket.getData());
                logger.trace("RECEIVED[" + clientIP.toString() + ":" + clientPort + "]: " + sentence);
                z21.addIPToSlot(clientIP);

                ByteArrayInputStream bis = new ByteArrayInputStream(receiveData);
                //Der Anfang des Telegrams ist mit einem BigEndian Input Stream zu interpretieren,
                //weil der (bigEndian) TimeStamp vom Arduino ohne Interpretation vom Empfangsbuffer in den Sendebuffer
                //kopiert wird.
                DataInputStream dis = new DataInputStream(bis);
                int receivedTel[] = new int[32];
                for (int i = 0; i < receivedDataLen; i++) {
                    receivedTel[i] = dis.readUnsignedByte();  //Bei bytes gibt es ja sowieso keine Probleme
                }
                z21.ParsePacket(receivedTel);
            }

            /*String capitalizedSentence = sentence.toUpperCase();
             sendData = capitalizedSentence.getBytes();
             DatagramPacket sendPacket
             = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
             serverSocket.send(sendPacket);*/
        }
    }

    public void ParsePacket(int[] packetBuffer) {

        int header = (packetBuffer[3] << 8) + packetBuffer[2];
        //    int datalen = (packetBuffer[1]<<8) + packetBuffer[0];
        int[] data = new int[16];
        Boolean ok = false;
        //packetBuffer[packetSize]= 0;
        //debug("z21 packetBuffer: ", (char*) packetBuffer);
        switch (header) {
            case Ct.LAN_GET_SERIAL_NUMBER:
                data[0] = (byte) 0xF5; //Seriennummer 32 Bit (little endian)
                data[1] = 0x0A;
                data[2] = 0x00;
                data[3] = 0x00;
                EthSend(0x08, 0x10, data, false, false);
                //debug("z21 Serial Number: ", (char*) data);
                logger.debug("z21 Serial Number: ");
                break;
            case Ct.LAN_GET_CONFIG:
                logger.debug("Z21-Einstellungen");
                break;
            case Ct.LAN_GET_HWINFO:
                data[0] = 0x00; //HwType 32 Bit
                data[1] = 0x00;
                data[2] = 0x02;
                data[3] = 0x01;
                data[4] = 0x20; //FW Version 32 Bit
                data[5] = 0x01;
                data[6] = 0x00;
                data[7] = 0x00;
                EthSend(0x0C, 0x1A, data, false, false);
                logger.debug("LAN_GET_HWINFO");
                break;
            case Ct.LAN_LOGOFF:
                logger.debug("LAN_LOGOFF");
                //Antwort von Z21: keine
                break;
            case Ct.LAN_XPRESS_NET:
                xPressNetParse(packetBuffer, data);
                break;
            case Ct.LAN_SET_BROADCASTFLAGS:
                logger.warn("!LAN_SET_BROADCASTFLAGS: ");
                // //debug.print(packetBuffer[4], BIN);  // 1=BC Power, Loco INFO, Trnt INFO; B100=BC Sytemstate Datachanged
                break;
            case Ct.LAN_GET_BROADCASTFLAGS:
                logger.warn("!LAN_GET_BROADCASTFLAGS");
                break;
            case Ct.LAN_GET_LOCOMODE:
                logger.warn("!LAN_GET_LOCOMODE");
                break;
            case Ct.LAN_SET_LOCOMODE:
                logger.warn("!LAN_SET_LOCOMODE");
                break;
            case Ct.LAN_GET_TURNOUTMODE:
                logger.warn("!LAN_GET_TURNOUTMODE");
                break;
            case Ct.LAN_SET_TURNOUTMODE:
                logger.warn("!LAN_SET_TURNOUTMODE");
                break;
            case Ct.LAN_RMBUS_GETDATA:
                logger.warn("!LAN_RMBUS_GETDATA");
                break;
            case Ct.LAN_RMBUS_PROGRAMMODULE:
                logger.warn("!LAN_RMBUS_PROGRAMMODULE");
                break;
            case Ct.LAN_SYSTEMSTATE_GETDATA:
                logger.warn("!LAN_SYSTEMSTATE_GETDATA"); //LAN_SYSTEMSTATE_DATACHANGED
                data[0] = 0x00; //MainCurrent mA
                data[1] = 0x00; //MainCurrent mA
                data[2] = 0x00; //ProgCurrent mA
                data[3] = 0x00; //ProgCurrent mA
                data[4] = 0x00; //FilteredMainCurrent
                data[5] = 0x00; //FilteredMainCurrent
                data[6] = 0x00; //Temperature
                data[7] = 0x20; //Temperature
                data[8] = 0x0F; //SupplyVoltage
                data[9] = 0x00; //SupplyVoltage
                data[10] = 0x00; //VCCVoltage
                data[11] = 0x03; //VCCVoltage
                data[12] = xpn.getPower(); //CentralState
                data[13] = 0x00; //CentralStateEx
                data[14] = 0x00; //reserved
                data[15] = 0x00; //reserved
                EthSend(0x14, 0x84, data, false, false);
                break;
            case Ct.LAN_RAILCOM_GETDATA:
                logger.warn("!LAN_RAILCOM_GETDATA");
                break;
            case Ct.LAN_LOCONET_FROM_LAN:
                logger.warn("!LAN_LOCONET_FROM_LAN");
                break;
            case Ct.LAN_LOCONET_DISPATCH_ADDR:
                logger.warn("!LAN_LOCONET_DISPATCH_ADDR");
                break;
            default:
                logger.error("!LAN_X_UNKNOWN_COMMAND");
                data[0] = 0x61;
                data[1] = (byte) 0x82;
                EthSend(0x07, 0x40, data, true, false);
        }

    }

    /**
     * Special Parsing for XPressNet Requires an instance of the XpressNet Corba
     * Interface Object
     *
     * @param packetBuffer
     * @param sendData
     */
    public void xPressNetParse(int[] packetBuffer, int[] sendData) {
        Boolean ok = false;
        int xHeader = packetBuffer[4];
        switch (xHeader) //X-Header
        {
            case Ct.LAN_X_GENERAL:
                switch (packetBuffer[5]) //DB0
                {
                    case Ct.LAN_X_GET_VERSION:
                        sendData[0] = 0x63;
                        sendData[1] = 0x21;
                        sendData[3] = XBusVer; //X-Bus Version
                        sendData[4] = 0x12; //ID der Zentrale
                        sendData[5] = 0;
                        EthSend(0x09, 0x40, sendData, true, false);
                        logger.debug("LAN_X_GET_VERSION");
                        break;
                    case Ct.LAN_X_GET_STATUS:
                        sendData[0] = 0x62;
                        sendData[1] = 0x22;
                        sendData[2] = xpn.getPower();
                        EthSend(0x08, 0x40, sendData, true, false);
                        logger.trace("LAN_X_GET_STATUS"); //This is asked very often ... 
                        break;
                    case Ct.LAN_X_SET_TRACK_POWER_OFF:
                        ok = xpn.setPower(XpressNet.csTrackVoltageOff);
                        logger.debug("LAN_X_SET_TRACK_POWER_OFF");
                        if (ok == false) {
                            logger.warn("Power Send FEHLER");
                        }
                        break;
                    case Ct.LAN_X_SET_TRACK_POWER_ON:
                        ok = xpn.setPower(XpressNet.csNormal);
                        logger.debug("LAN_X_SET_TRACK_POWER_ON");
                        if (ok == false) {
                            logger.warn("Power Send FEHLER");
                        }
                        break;
                }
                break;
            case Ct.LAN_X_CV_READ_0:
                if (packetBuffer[5] == Ct.LAN_X_CV_READ_1) //DB0
                {
                    logger.debug("LAN_X_CV_READ");
                    int CV_MSB = packetBuffer[6];
                    int CV_LSB = packetBuffer[7];
                    xpn.readCVMode(CV_LSB + 1);
                }
                break;
            case Ct.LAN_X_CV_WRITE_0:
                if (packetBuffer[5] == Ct.LAN_X_CV_WRITE_1) //DB0
                {
                    logger.debug("LAN_X_CV_WRITE");
                    int CV_MSB = packetBuffer[6];
                    int CV_LSB = packetBuffer[7];
                    int value = packetBuffer[8];
                    xpn.writeCVMode(CV_LSB + 1, value);
                }
                break;
            case Ct.LAN_X_GET_TURNOUT_INFO: {
                logger.debug("LAN_X_GET_TURNOUT_INFO");
                boolean open = xpn.getTrntInfo(packetBuffer[5], packetBuffer[6]);
                break;
            }
            case Ct.LAN_X_SET_TURNOUT: {
                logger.debug("LAN_X_SET_TURNOUT");
                boolean open;
                open = xpn.setTrntPos(packetBuffer[5], packetBuffer[6], packetBuffer[7] & 0x0F);
                break;
            }
            case Ct.LAN_X_SET_STOP:
                logger.debug("LAN_X_SET_STOP");
                ok = xpn.setPower(XpressNet.csEmergencyStop);
                if (ok == false) {
                    logger.warn("Power Send FEHLER");
                }
                break;
            case Ct.LAN_X_GET_LOCO_INFO_0:
                if (packetBuffer[5] == Ct.LAN_X_GET_LOCO_INFO_1) //DB0
                {
                    logger.debug("LAN_X_GET_LOCO_INFO: ");
                    //Antwort: LAN_X_LOCO_INFO  Adr_MSB - Adr_LSB
                    ////debug(word(packetBuffer[6], packetBuffer[7]));  //mit F1-F12
                    boolean Info= xpn.getLocoInfo(packetBuffer[6] & 0x3F, packetBuffer[7]);
                    boolean FuncOn= xpn.getLocoFunc(packetBuffer[6] & 0x3F, packetBuffer[7]); //F13 bis F28
                }
                break;
            case Ct.LAN_X_SET_LOCO_FUNCTION_0:
                logger.debug("Lok-Adresse");
                if (packetBuffer[5] == Ct.LAN_X_SET_LOCO_FUNCTION_1) //DB0
                {
                    //LAN_X_SET_LOCO_FUNCTION  Adr_MSB        Adr_LSB            Type (EIN/AUS/UM)      Funktion
//C++ TO JAVA CONVERTER WARNING: The right shift operator was replaced by Java's logical right shift operator since the left operand was originally of an unsigned type, but you should confirm this replacement:
                    ok= xpn.setLocoFunc(packetBuffer[6] & 0x3F, packetBuffer[7], packetBuffer[8] >>> 5, packetBuffer[8] & 0b00011111);
                } else {
                    logger.debug("LAN_X_SET_LOCO_DRIVE");
                    //LAN_X_SET_LOCO_DRIVE            Adr_MSB          Adr_LSB      DB0          Dir+Speed
                    ok= xpn.setLocoDrive(packetBuffer[6] & 0x3F, packetBuffer[7], packetBuffer[5] & 0b11, packetBuffer[8]);
                }
                break;
            case Ct.LAN_X_CV_POM:
                if (packetBuffer[5] == Ct.LAN_X_CV_POM_WRITE) //DB0
                {
                    int Option = packetBuffer[8] & 0b11111100; //Option DB3
                    int Adr_MSB = packetBuffer[6] & 0x3F; //DB1
                    int Adr_LSB = packetBuffer[7]; //DB2
                    int CVAdr = packetBuffer[9] | ((packetBuffer[8] & 0b11) << 7);
                    if (Option == Ct.LAN_X_CV_POM_WRITE_BYTE) {
                        logger.debug("LAN_X_CV_POM_WRITE_BYTE");
                        int value = packetBuffer[10]; //DB5
                    }
                    if (Option == Ct.LAN_X_CV_POM_WRITE_BIT) {
                        logger.warn("LAN_X_CV_POM_WRITE_BIT");
                        //Nicht von der APP Unterstützt
                    }
                }
                break;
            case Ct.LAN_X_GET_FIRMWARE_VERSION:
                logger.debug("LAN_X_GET_FIRMWARE_VERSION");
                /*sendData[0] = 0xf3;
                 sendData[1] = 0x0a;
                 sendData[3] = 0x01;   //V_MSB
                 sendData[4] = 0x23;  //V_LSB*/
                sendData[0] = 0xf3;
                sendData[1] = 0x0a;
                sendData[3] = 0x01; //V_MSB
                sendData[4] = 0x23; //V_LSB

                EthSend(0x09, 0x40, sendData, true, false);
                break;
        }
    }

    /**
     *
     * HELPERS
     */
    public void clearIPSlots() {
        ActIP.clear();
    }

    public void z21CheckActiveIP() {
        long now = System.currentTimeMillis();
        for (Entry<InetAddress, Timestamp> e : ActIP.entrySet()) {
            Timestamp lastAct = e.getValue();
            InetAddress ip = e.getKey();

            if (now - lastAct.getTime() > inActInt) {
                logger.info("IP [" + ip + "] kicked out due to inactivity timeout. Last request was " + lastAct.toLocalDateTime());
                ActIP.remove(e.getKey());
            }
        }
    }

    public void addIPToSlot(InetAddress ip) {
        byte Slot = Ct.maxIP;
        long milis = System.currentTimeMillis();
        Timestamp now = new Timestamp(milis);
        if (ActIP.containsKey(ip)) {
            logger.trace("IP[" + ip + "] updated from " + ActIP.get(ip).toString() + " to " + now.toLocalDateTime());
            ActIP.get(ip).setTime(milis);
        } else {
            logger.info("New IP added: " + ip);
            ActIP.put(ip, now);
        }

        //Z21.notifyXNetPower(Z21_main.xpn.getPower());
    }

    //--------------------------------------------------------------------------------------------
    //Senden von Lokdaten via Ethernet
  /*  public static void EthSendOut(int DataLen, int Header, int[] Data, Boolean withXOR) {
     Udp.write(DataLen & 0xFF);
     Udp.write(DataLen & 0xFF00);
     Udp.write(Header & 0xFF);
     Udp.write(Header & 0xFF00);

     //C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
     //ORIGINAL LINE: unsigned char XOR = 0;
     byte XOR = 0;
     byte ldata = DataLen - 5; //Ohne Length und Header und XOR
     if (withXOR == null) //XOR vorhanden?
     {
     ldata++;
     }
     for (int i = 0; i < (ldata); i++) {
     XOR = XOR ^ Data[i];
     Udp.write(Data[i]);
     }
     if (withXOR != null) {
     Udp.write(XOR);
     }
     }*/
    private byte[] prepareCurrentTel(int DataLen, int Header, int[] Data, Boolean withXOR) {
        ByteArrayOutputStream bos = null;
        DataOutputStream dos = null;
        byte[] currentTel = null;
        byte XOR = 0;

        byte ldata = (byte) (DataLen - 5); //Ohne Length und Header und XOR
        if (withXOR == false) //XOR vorhanden?
        {
            ldata += 1;
        }

        try {
            bos = new ByteArrayOutputStream();
            dos = new DataOutputStream(bos);
            int value = DataLen & 0xFF;

            dos.writeByte(value);
            dos.writeByte(DataLen & 0xFF00);
            dos.writeByte(Header & 0xFF);
            dos.writeByte(Header & 0xFF00);

            for (int i = 0; i < ldata; i++) {
                XOR = (byte) (XOR ^ Data[i]);
                dos.writeByte(Data[i]);
            }
            if (withXOR) {
                dos.write(XOR);
            }

            dos.flush();
            currentTel = bos.toByteArray();
            bos.reset();

        } catch (IOException ex) {
            logger.warn(ex);
        }
        return currentTel;
    }

    void EthSendOut(byte[] sendData, InetAddress ip) {
        try {
            //byte[] receiveData = new byte[1024];
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, clientPort);
            serverSocket.send(sendPacket);
            logger.trace("SENT: '" + sendData.toString() + "' to " + ip + ":" + clientPort);

        } catch (IOException ex) {
            logger.warn(ex);
        }
    }

    /**
     *
     * @param DataLen lenght of the whole telegram
     * @param Header Z21 Header
     * @param Data Teletram to send (Unsigend bytes are stored as int, because
     * Java does not have unsigned types)
     * @param withXOR Simple and actually useless Checksum
     * @param broadCast Broadcasts telegram to all connected clients, if set
     */
    public void EthSend(int DataLen, int Header, int[] Data, Boolean withXOR, Boolean broadCast) {
        byte[] sendData = prepareCurrentTel(DataLen, Header, Data, withXOR);

        if (broadCast == true) {
            for (Entry<InetAddress, Timestamp> e : ActIP.entrySet()) {
                InetAddress ip = e.getKey();
                EthSendOut(sendData, ip);
            }
        } else {
            EthSendOut(sendData, clientIP);
        }

        /*if (broadCast == true) {
         IPAddress IPout = Udp.remoteIP();
         for (int i = 0; i < Ct.maxIP; i++) {
         if (ActIP[i].time > 0) //Noch aktiv?
         {
         IPout[0] = ActIP[i].ip0;
         IPout[1] = ActIP[i].ip1;
         IPout[2] = ActIP[i].ip2;
         IPout[3] = ActIP[i].ip3;
         ////debug("z21 sending data to: ", IPout);
         Udp.beginPacket(IPout, Udp.remotePort()); //Broadcast
         Z21.EthSendOut(DataLen, Header, Data, withXOR);
         Udp.endPacket();
         }
         }
         } else {
         Udp.beginPacket(Udp.remoteIP(), Udp.remotePort()); //Broadcast
         Z21.EthSendOut(DataLen, Header, Data, withXOR);
         ////debug(Udp.remoteIP());
         Udp.endPacket();
         }*/
    }

    //--------------------------------------------------------------------------------------------
    //NOTIFIERS
    //--------------------------------------------------------------------------------------------
    /**
     *
     * @param State
     */
    private void notifyXNetPower(byte State) {
        //debug.print("Power: 0x"); 
        ////debug(State, HEX); 
        int[] data = {0x61, 0x00};
        switch (State) {
            case XpressNet.csNormal:
                data[1] = 0x01;
                break;
            case XpressNet.csTrackVoltageOff:
                data[1] = 0x00;
                break;
            case XpressNet.csServiceMode:
                data[1] = 0x02;
                break;
            case XpressNet.csEmergencyStop:
                data[0] = 0x81;
                data[1] = 0x00;
                break;
        }
        EthSend(0x07, 0x40, data, true, true);
    }

    /**
     *
     * @param Adr_High
     * @param Adr_Low
     * @param F2
     * @param F3
     */
    public static void notifyLokFunc(byte Adr_High, byte Adr_Low, byte F2, byte F3) {
        // //debug.print("Loco Fkt: "); 
        // //debug.print(Adr_Low); 
        // //debug.print(", Fkt2: "); 
        // //debug.print(F2, BIN); 
        // //debug.print("; "); 
        // //debug(F3, BIN); 

    }

    /**
     *
     * @param Adr_High
     * @param Adr_Low
     * @param Busy
     * @param Steps
     * @param Speed
     * @param Direction
     * @param F0
     * @param F1
     * @param F2
     * @param F3
     */
    public void notifyLokAll(byte Adr_High, byte Adr_Low, Boolean Busy, byte Steps, byte Speed, byte Direction, byte F0, byte F1, byte F2, byte F3) {

        byte DB2 = Steps;
        if (DB2 == 3) //nicht vorhanden!
        {
            DB2 = 4;
        }
        if (Busy != null) {
            //bitWrite(DB2, 3, 1);
            DB2 |= 0b00000100;
        }
        byte DB3 = Speed;
        if (Direction == 1) {
            //bitWrite(DB3, 7, 1);
            DB3 |= 0b01000000;
        }
        int[] data = new int[9];
        data[0] = 0xEF; //X-HEADER
        data[1] = Adr_High & 0x3F;
        data[2] = Adr_Low;
        data[3] = DB2;
        data[4] = DB3;
        data[5] = F0; //F0, F4, F3, F2, F1
        data[6] = F1; //F5 - F12; Funktion F5 ist bit0 (LSB)
        data[7] = F2; //F13-F20
        data[8] = F3; //F21-F28
        EthSend(14, 0x40, data, true, true); //Send Power und Funktions to all active Apps
    }

    /**
     *
     * @param Adr_High
     * @param Adr_Low
     * @param Pos
     */
    public void notifyTrnt(byte Adr_High, byte Adr_Low, byte Pos) {
        // //debug.print("Weiche: "); 
        // //debug.print(word(Adr_High, Adr_Low)); 
        // //debug.print(", Position: "); 
        // //debug(Pos, BIN); 
        //LAN_X_TURNOUT_INFO
        int[] data = new int[4];
        data[0] = 0x43; //HEADER
        data[1] = Adr_High;
        data[2] = Adr_Low;
        data[3] = Pos;
        EthSend(0x09, 0x40, data, true, false);
    }

    /**
     *
     * @param State
     */
    public void notifyCVInfo(byte State) {
        // //debug.print("CV Prog STATE: "); 
        // //debug(State); 
        if (State == 0x01 || State == 0x02) //Busy or No Data
        {
            //LAN_X_CV_NACK
            int[] data = new int[2];
            data[0] = 0x61; //HEADER
            data[1] = 0x13; //DB0
            EthSend(0x07, 0x40, data, true, false);
        }
    }

    /**
     *
     * @param cvAdr
     * @param cvData
     */
    public void notifyCVResult(byte cvAdr, byte cvData) {
        // //debug.print("CV Prog Read: "); 
        // //debug.print(cvAdr); 
        // //debug.print(", "); 
        // //debug(cvData); 
        //LAN_X_CV_RESULT
        int[] data = new int[5];
        data[0] = 0x64; //HEADER
        data[1] = 0x14; //DB0
        data[2] = 0x00; //CVAdr_MSB
        data[3] = cvAdr; //CVAdr_LSB
        data[4] = cvData; //Value
        EthSend(0x0A, 0x40, data, true, false);
    }

    /**
     *
     * @param Version
     * @param ID
     */
    public static void notifyXNetVersion(byte Version, byte ID) {
        XBusVer = Version;
    }

    /*
     //--------------------------------------------------------------------------------------------
     void notifyXNetStatus(uint8_t LedState )
     {
     }
     */
}

package com.schabauer.z21j;

final class Ct
{
	public static final int LAN_GET_SERIAL_NUMBER = 0x10;
	public static final int LAN_GET_CONFIG = 0x12;
	public static final int LAN_GET_HWINFO = 0x1A;
	public static final int LAN_LOGOFF = 0x30;
	public static final int LAN_XPRESS_NET = 0x40;
	public static final int LAN_X_GENERAL = 0x21;
	public static final int LAN_X_GET_VERSION = 0x21;
	public static final int LAN_X_GET_STATUS = 0x24;
	public static final int LAN_X_SET_TRACK_POWER_OFF = 0x80;
	public static final int LAN_X_SET_TRACK_POWER_ON = 0x81;
	public static final int LAN_X_CV_READ_0 = 0x23;
	public static final int LAN_X_CV_READ_1 = 0x11;
	public static final int LAN_X_CV_WRITE_0 = 0x24;
	public static final int LAN_X_CV_WRITE_1 = 0x12;
	public static final int LAN_X_GET_TURNOUT_INFO = 0x43;
	public static final int LAN_X_SET_TURNOUT = 0x53;
	public static final int LAN_X_SET_STOP = 0x80;
	public static final int LAN_X_GET_LOCO_INFO_0 = 0xE3;
	public static final int LAN_X_GET_LOCO_INFO_1 = 0xF0;
	public static final int LAN_X_SET_LOCO_FUNCTION_0 = 0xE4;
	public static final int LAN_X_SET_LOCO_FUNCTION_1 = 0xF8;
	public static final int LAN_X_CV_POM = 0xE6;
	public static final int LAN_X_CV_POM_WRITE = 0x30;
	public static final int LAN_X_CV_POM_WRITE_BYTE = 0xEC;
	public static final int LAN_X_CV_POM_WRITE_BIT = 0xE8;
	public static final int LAN_X_GET_FIRMWARE_VERSION = 0xF1;
	public static final int LAN_SET_BROADCASTFLAGS = 0x50;
	public static final int LAN_GET_BROADCASTFLAGS = 0x51;
	public static final int LAN_GET_LOCOMODE = 0x60;
	public static final int LAN_SET_LOCOMODE = 0x61;
	public static final int LAN_GET_TURNOUTMODE = 0x70;
	public static final int LAN_SET_TURNOUTMODE = 0x71;
	public static final int LAN_RMBUS_GETDATA = 0x81;
	public static final int LAN_RMBUS_PROGRAMMODULE = 0x82;
	public static final int LAN_SYSTEMSTATE_GETDATA = 0x85;
	public static final int LAN_RAILCOM_GETDATA = 0x89;
	public static final int LAN_LOCONET_FROM_LAN = 0xA2;
	public static final int LAN_LOCONET_DISPATCH_ADDR = 0xA3;
	public static final int maxIP = 10;
	public static final int ActTimeIP = 20;
	public static final int interval = 2000;
	public static final int EEip = 10;
	public static final int EEXNet = 9;
}
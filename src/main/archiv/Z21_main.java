import java.io.*;
import java.net.*;


public class Z21_main
{
	/*
	    Z21 Ethernet Emulation für die App-Steuerung via Smartphone über XpressNet.
	 */

	public static XpressNetClass XpressNet = new XpressNetClass();


	///#include <EthernetUdp.h>         // UDP library
	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if ! Z21_H
	//C++ TO JAVA CONVERTER TODO TASK: #define macros defined in preprocessor conditionals can only be replaced within the scope of the preprocessor conditional:
	///#define Z21_H
	///#endif

	// Enter a MAC address and IP address for your controller below.
	// The IP address will be dependent on your local network:
	public static byte[] mac = {0xFE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};
//C++ TO JAVA CONVERTER TODO TASK: The following statement was not recognized, possibly due to an unrecognized macro:
	IPAddress ip(10, 0, 1, 111);
	public static byte[] loghost = {10, 0, 1, 100};

//C++ TO JAVA CONVERTER TODO TASK: The following statement was not recognized, possibly due to an unrecognized macro:
	EthernetServer server(80);

	//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
	//ORIGINAL LINE: #define ResetPin A1

	// XpressNet address: must be in range of 1-31; must be unique. Note that some IDs
	// are currently used by default, like 2 for a LH90 or LH100 out of the box, or 30
	// for PC interface devices like the XnTCP.
	public static byte XNetAddress = 30; //Adresse im XpressNet


	public static int previousMillis = 0; // will store last time of IP decount updated

	///#include <SoftwareSerial.h>
	//SoftwareSerial //debug(6, 5); // RX, TX

	//--------------------------------------------------------------------------------------------
	public static void setup()
	{
	 // //debug.begin(115200); 
	 // //debug("Z21"); 
	   pinMode(A1, INPUT);
	   digitalWrite(A1, HIGH); //PullUp
	   Thread.sleep(100);

	   if (digitalRead(A1) == LOW || EEPROM.read(Ct.EEXNet) < 32)
	   {
		 XNetAddress = EEPROM.read(Ct.EEXNet);
	   }
	   else
	   {
		  EEPROM.write(Ct.EEXNet, XNetAddress);
		  EEPROM.write(Ct.EEip, ip[0]);
		  EEPROM.write(Ct.EEip + 1, ip[1]);
		  EEPROM.write(Ct.EEip + 2, ip[2]);
		  EEPROM.write(Ct.EEip + 3, ip[3]);
	   }
	  ip[0] = EEPROM.read(Ct.EEip);
	  ip[1] = EEPROM.read(Ct.EEip + 1);
	  ip[2] = EEPROM.read(Ct.EEip + 2);
	  ip[3] = EEPROM.read(Ct.EEip + 3);

	//  //debug(ip);

	  // start the Ethernet and UDP:
	  Ethernet.begin(mac,ip); //IP and MAC Festlegung
	  Syslog.setLoghost(loghost);
	  //  server.begin();    //HTTP Server

	  Z21.debug("Starting XPressNet");
	  XpressNet.start(XNetAddress,3); //Initialisierung XNet
	  Z21.debug("Starting Z21 Emulation");
	  Z21.z21Setup();

	  Z21.debug("Setup finished");
	}


	//--------------------------------------------------------------------------------------------
	public static void loop()
	{

	  XpressNet.receive(); //Check for XpressNet

	  Z21.z21Receive(); //Read Data on UDP Port

	  XpressNet.receive(); //Check for XpressNet

	  Z21_main.Webconfig(); //Webserver for Configuration

	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: unsigned long currentMillis = millis();
	  int currentMillis = millis();
	  if (currentMillis - previousMillis > Ct.interval)
	  {
		previousMillis = currentMillis;
		Z21.z21CheckActiveIP();
	  }
	}



	//--------------------------------------------------------------------------------------------
	public static void Webconfig()
	{
	  EthernetClient client = server.available();
	  if (client != null)
	  {
		String receivedText = 50;
		// an http request ends with a blank line
		Boolean currentLineIsBlank = true;
		while (client.connected())
		{
		  if (client.available())
		  {
			byte c = client.read();
			if (receivedText.length() < 50)
			{
			  receivedText += (char)c;
			}
			// if you've gotten to the end of the line (received a newline
			// character) and the line is blank, the http request has ended,
			// so you can send a reply
			if (c == '\n' && currentLineIsBlank != null)
			{
			  // send a standard http response header
			  client.println("HTTP/1.1 200 OK");
			  client.println("Content-Type: text/html");
			  //client.println("Connection: close");  // the connection will be closed after completion of the response
		  //client.println("Refresh: 5");  // refresh the page automatically every 5 sec
			  client.println();
			  //Website:
			  client.println("<html><head><title>Z21</title></head><body>");
			  client.println("<h1>Z21</h1><br />");
	//----------------------------------------------------------------------------------------------------          
			  int firstPos = receivedText.indexOf("?");
			  if (firstPos > -1)
			  {
				client.println("-> accept changes after RESET!");
				byte lastPos = receivedText.indexOf(" ", firstPos);
				String theText = receivedText.substring(firstPos + 3, firstPos + 3 + lastPos); // 10 is the length of "?A="
				byte XNetPos = theText.indexOf("&XNet=");
				XNetAddress = theText.substring(XNetPos + 6, XNetPos + 6 + theText.length()).toInt();
				byte Aip = theText.indexOf("&B=");
				byte Bip = theText.indexOf("&C=", Aip);
				byte Cip = theText.indexOf("&D=", Bip);
				byte Dip = theText.substring(Cip + 3, Cip + 3 + XNetPos).toInt();
				Cip = theText.substring(Bip + 3, Bip + 3 + Cip).toInt();
				Bip = theText.substring(Aip + 3, Aip + 3 + Bip).toInt();
				Aip = theText.substring(0, Aip).toInt();
				ip[0] = Aip;
				ip[1] = Bip;
				ip[2] = Cip;
				ip[3] = Dip;
				if (EEPROM.read(Ct.EEXNet) != XNetAddress)
				{
				  EEPROM.write(Ct.EEXNet, XNetAddress);
				}
				if (EEPROM.read(Ct.EEip) != Aip)
				{
				  EEPROM.write(Ct.EEip, Aip);
				}
				if (EEPROM.read(Ct.EEip + 1) != Bip)
				{
				  EEPROM.write(Ct.EEip + 1, Bip);
				}
				if (EEPROM.read(Ct.EEip + 2) != Cip)
				{
				  EEPROM.write(Ct.EEip + 2, Cip);
				}
				if (EEPROM.read(Ct.EEip + 3) != Dip)
				{
				  EEPROM.write(Ct.EEip + 3, Dip);
				}
			  }
	//----------------------------------------------------------------------------------------------------          
			  client.print("<form method=get>IP-Adr.: <input type=number min=10 max=254 name=A value=");
			  client.println(ip[0]);
			  client.print(">.<input type=number min=0 max=254 name=B value=");
			  client.println(ip[1]);
			  client.print(">.<input type=number min=0 max=254 name=C value=");
			  client.println(ip[2]);
			  client.print(">.<input type=number min=0 max=254 name=D value=");
			  client.println(ip[3]);
			  client.print("><br /> XBus Adr.: <input type=number min=1 max=31 name=XNet value=");
			  client.print(XNetAddress);
			  client.println("><br /><br />");
			  client.println("<input type=submit></form>");
			  client.println("</body></html>");
			  break;
			}
			if (c == '\n')
			{
			  currentLineIsBlank = true; // you're starting a new line
			}
			else if (c != '\r')
			{
			  currentLineIsBlank = false; // you've gotten a character on the current line
			}
		  }
		}
		client.stop(); // close the connection:
	  }
	}
}
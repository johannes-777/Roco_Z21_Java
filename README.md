# Roco_Z21_Java
Implementation of a Roco Z21 Server Simulator in Java
Based on the excellent work of Philipp Gahtow's C++ XPressNetLib for Arduino
Published with written permission as of 17th of Feb. 2015.

This Z21 Simulator connects to Roco Z21 App (Tests with IOS Z21 App) and allows for an easy way to understand and debug the Roco Z21 Protocol, which is not so easy with an Arduino Project (Breakpoints, Debuginfo, Stacktrace ...)

However, this simulator cannot directly connect to Lenz XPressNet Bus (RS485) and thus control your trains.  In order to do so, it needs to forward the XPressNet telegrams to a real Z21.  

An alternative would be to implement some kind of remote procedure call interface (RPC) or Corba to let this simulator interact with the xPressNetLib Object, which runs on an Arduino :-)

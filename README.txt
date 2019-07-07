This is a demo App about enstablishing a TCP bidirectional connection between an Android phone in "client mode" and a PC in "server mode".
The aim behind this app is to test the "reverse" feature of ADB, which permits to use the USB cable to route all the access requests from the Android device to http://localhost:<PORT> on your computer.

To enstablish the USB connection, type from your PC terminal:

adb reverse tcp:<PORT> tcp:<PORT>

Now when your phone tries to access http://<ADDRESS>:<PORT> the request will be routed to the <ADDRESS> and <PORT> number that are specified inside TcpClientConfig class of the app.
The project also provides a server Windows app to test the communication.
From "SocketTestForWindows" folder launch the .exe and select the "Server" tab; then indicate the <ADDRESS> and the <PORT> accordingly with the app.
Hit "Connect" and then open and try the app.

Copyright 2018 LUCA LORUSSO - http://lukelorusso.com  

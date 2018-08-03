This App is a demo about enstablishing a TCP "client mode" bidirectional connection with a TCP "server".
The aim behind this app is to test the "reverse" feature of ADB, which permit to use the USB cable to route all the access requests from the Android device to http://localhost:<PORT> to the same address on your computer.

To use the USB connection between phone and PC send the command:

adb reverse tcp:<PORT> tcp:<PORT>

Now when your phone tries to access http://<ADDRESS>:<PORT> the request will be routed to the <ADDRESS> and <PORT> number that are specified inside TcpClientConfig class of the app.
The project also provide a server Windows app to test the communication.
From "SocketTest" launch the .exe and select the "Server" tab; then indicate the <ADDRESS> and the <PORT> accordingly with the app.
Hit "Connect" and then open and try the app!

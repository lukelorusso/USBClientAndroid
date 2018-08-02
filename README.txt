To use the USB connection between phone and PC send the command:

adb reverse tcp:<PORT> tcp:<PORT>

<PORT> it's the number specified inside TcpClientConfig.SERVER_PORT in the app.
From SocketTest select the "Server" tab and indicate as IP Address 127.0.0.1 and <PORT> as port.
Hit "Connect" and then open and try the app!
package com.lukelorusso.socketclient.service;

public class TcpClientConfig {

    private TcpClientConfig() {}

    public static final int TIMEOUT_IN_MILLIS = 15000;

    static final String EXCEPTION_LOG_TAG = "TCP";

    static final String SERVER_HOST = "127.0.0.1";

    static final int SERVER_PORT = 4148;
}

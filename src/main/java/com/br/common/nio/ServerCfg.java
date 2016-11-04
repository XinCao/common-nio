package com.br.common.nio;

public class ServerCfg {

    public final String connectionName;
    public final ConnectionFactory factory;
    public final String hostName;
    public final int port;

    public ServerCfg(String hostName, int port, String connectionName, ConnectionFactory factory) {
        this.hostName = hostName;
        this.port = port;
        this.connectionName = connectionName;
        this.factory = factory;
    }
}
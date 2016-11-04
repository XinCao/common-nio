/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

/**
 * 
 * @author 510655387@qq.com
 */
public class ServerCfg {

    public final String hostName;
    public final int port;
    public final String connectionName;
    public final ConnectionFactory factory;

    public ServerCfg(String hostName, int port, String connectionName, ConnectionFactory factory) {
        this.hostName = hostName;
        this.port = port;
        this.connectionName = connectionName;
        this.factory = factory;
    }
}
package com.xincao.common.nio;

import com.xincao.common.nio.core.IOServer;
import com.xincao.common.util.tool.DeadLockDetector;

/**
 *
 * @author caoxin
 */
public class AionNioServer {

    public static void main(String... args) {
        IOServer.getInstance().connect();
        DeadLockDetector.detector(60, DeadLockDetector.Dealt.RESTART);
    }
}
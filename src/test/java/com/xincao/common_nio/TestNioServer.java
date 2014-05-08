package com.xincao.common_nio;

import com.xincao.common_nio.core.IOServer;
import com.xincao.common_nio.util.DeadLockDetector;
import com.xincao.common_nio.util.ThreadPoolManager;

/**
 *
 * @author caoxin
 */
public class TestNioServer {

    public static void main(String... args) {
        ThreadPoolManager.getInstance();
        IOServer.getInstance().connect();
        DeadLockDetector.detector(60, DeadLockDetector.Dealt.RESTART);
    }
}
package com.xincao.common_nio;

import com.xincao.common_nio.core.IOServer;
import com.xincao.common_nio.service.ThreadPoolManager;
import com.xincao.common_util.tool.DeadLockDetector;

/**
 *
 * @author caoxin
 */
public class AionNioServer {

    public static void main(String... args) {
        ThreadPoolManager.getInstance();
        IOServer.getInstance().connect();
        DeadLockDetector.detector(60, DeadLockDetector.Dealt.RESTART);
    }
}
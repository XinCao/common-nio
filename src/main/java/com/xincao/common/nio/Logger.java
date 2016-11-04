package com.xincao.common.nio;

import java.util.Date;

/**
 *
 * @author 510655387@qq.com
 */
public class Logger {

    public static void info(Object message) {
        System.out.println(String.format("[%s-%s] info %s", Thread.currentThread().getName(), new Date().toString(), message));
    }

    public static void error(Object message) {
        System.out.println(String.format("[%s-%s] error %s", Thread.currentThread().getName(), new Date().toString(), message));
    }

    public static void warn(Object message) {
        System.out.println(String.format("[%s-%s] warn %s", Thread.currentThread().getName(), new Date().toString(), message));
    }
}

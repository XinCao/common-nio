/**
 * 欢迎浏览和修改代码，有任何想法可以email我
 */
package com.br.common.nio;

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

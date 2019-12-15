/*
 * The MIT License (MIT)
 * Copyright © 2019-2020 <sky>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sky.meteor.common.config;

/**
 * @author
 */
public class ConfigManager {

    public static boolean tpDiscardSwitch() {
        return getBool(Configs.TP_DISCARD_SWITCH, Configs.TP_DISCARD_SWITCH_DEFAULT);
    }

    public static int tpKeepAliveTime() {
        return getInt(Configs.TP_KEEP_ALIVE_TIME, Configs.TP_KEEP_ALIVE_TIME_DEFAULT);
    }

    public static int TpInitialCapacity() {
        return getInt(Configs.TP_INITIAL_CAPACITY, Configs.TP_INITIAL_CAPACITY_DEFAULT);
    }

    public static int tpMaximumPoolSize() {
        return getInt(Configs.TP_MAXIMUM_POOL_SIZE, Configs.TP_MAXIMUM_POOL_SIZE_DEFAULT);
    }

    public static int tpCorePoolSize() {
        return getInt(Configs.TP_CORE_POOL_SIZE, Configs.TP_CORE_POOL_SIZE_DEFAULT);
    }

    public static int clientPoolMinIdle() {
        return getInt(Configs.CLIENT_POOL_MIN_IDLE, Configs.CLIENT_POOL_MIN_IDLE_DEFAULT);
    }

    public static int clientPoolMaxIdle() {
        return getInt(Configs.CLIENT_POOL_MAX_IDLE, Configs.CLIENT_POOL_MAX_IDLE_DEFAULT);
    }

    public static int clientPoolMaxActive() {
        return getInt(Configs.CLIENT_POOL_MAX_ACTIVE, Configs.CLIENT_POOL_MAX_ACTIVE_DEFAULT);
    }

    public static boolean tcpNodelay() {
        return getBool(Configs.TCP_NODELAY, Configs.TCP_NODELAY_DEFAULT);
    }

    public static boolean metricsSwitch() {
        return getBool(Configs.METRICS_SWITCH, Configs.METRICS_SWITCH_DEFAULT);
    }

    public static boolean tcpSoReuseaddr() {
        return getBool(Configs.TCP_SO_REUSEADDR, Configs.TCP_SO_REUSEADDR_DEFAULT);
    }

    public static int tcpSoBacklog() {
        return getInt(Configs.TCP_SO_BACKLOG, Configs.TCP_SO_BACKLOG_DEFAULT);
    }

    public static boolean nettyEpoll() {
        return getBool(Configs.NETTY_EPOLL_SWITCH, Configs.NETTY_EPOLL_SWITCH_DEFAULT);
    }

    public static boolean getBool(String key, String defaultValue) {
        return Boolean.parseBoolean(System.getProperty(key, defaultValue));
    }

    public static int getInt(String key, String defaultValue) {
        return Integer.parseInt(System.getProperty(key, defaultValue));
    }

    public static byte getByte(String key, String defaultValue) {
        return Byte.parseByte(System.getProperty(key, defaultValue));
    }

    public static long getLong(String key, String defaultValue) {
        return Long.parseLong(System.getProperty(key, defaultValue));
    }
}

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
public class Configs {

    public static final String NETTY_EPOLL_SWITCH = "netty.epoll.switch";

    public static final String NETTY_EPOLL_SWITCH_DEFAULT = "true";

    public static final String TCP_SO_BACKLOG = "tcp.so.backlog";

    public static final String TCP_SO_BACKLOG_DEFAULT = "1024";

    public static final String TCP_NODELAY = "tcp.nodelay";

    public static final String TCP_NODELAY_DEFAULT = "true";

    public static final String TCP_SO_REUSEADDR = "tcp.so.reuseaddr";

    public static final String TCP_SO_REUSEADDR_DEFAULT = "true";

    public static final String METRICS_SWITCH = "metrics.switch";

    public static final String METRICS_SWITCH_DEFAULT = "true";

    public static final String CLIENT_POOL_MAX_ACTIVE = "client.pool.max.active";

    public static final String CLIENT_POOL_MAX_ACTIVE_DEFAULT = "20";

    public static final String CLIENT_POOL_MAX_IDLE = "client.pool.max.idle";

    public static final String CLIENT_POOL_MAX_IDLE_DEFAULT = "10";

    public static final String CLIENT_POOL_MIN_IDLE = "client.pool.min.idle";

    public static final String CLIENT_POOL_MIN_IDLE_DEFAULT = "10";
    /**
     * 核心线程数
     */
    public static final String TP_CORE_POOL_SIZE = "tp.core.pool.size";

    public static final String TP_CORE_POOL_SIZE_DEFAULT = "5";
    /**
     * 线程池最大线程数
     */
    public static final String TP_MAXIMUM_POOL_SIZE = "tp.maximum.pool.size";

    public static final String TP_MAXIMUM_POOL_SIZE_DEFAULT = "200";
    /**
     * 队列大小
     */
    public static final String TP_INITIAL_CAPACITY = "tp.initial.capacity";

    public static final String TP_INITIAL_CAPACITY_DEFAULT = "1000000";
    /**
     * 线程不被使用后存活时间
     */
    public static final String TP_KEEP_ALIVE_TIME = "tp.keep.alive.time";

    public static final String TP_KEEP_ALIVE_TIME_DEFAULT = "120";

    /**
     * 内存大小检测 false:不检测 true:检测
     */
    public static final String TP_DISCARD_SWITCH = "tp.discard.switch";

    public static final String TP_DISCARD_SWITCH_DEFAULT = "true";


}

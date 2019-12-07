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
package com.sky.meteor.spring.annotation;


import com.sky.meteor.common.enums.ClusterEnum;
import com.sky.meteor.common.enums.LoadBalanceEnum;
import com.sky.meteor.common.enums.ProxyEnum;
import com.sky.meteor.common.enums.SerializeEnum;
import com.sky.meteor.spring.AnnotationRegistrar;
import com.sky.meteor.spring.RpcAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * @author
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcAutoConfiguration.class, AnnotationRegistrar.class})
@Documented
public @interface EnableRPC {

    /**
     * 要扫描的包名
     *
     * @return
     */
    String scan();


    /**
     * 代理方式 默认: javassist
     * Consumer端参数
     *
     * @return
     * @see ProxyEnum
     */
    String proxy() default "";

    /**
     * 集群方式 默认: failover
     * Consumer端参数
     *
     * @return
     * @see ClusterEnum
     */
    String cluster() default "";

    /**
     * 序列化方式 默认: fastjson
     *
     * @return
     * @see SerializeEnum
     */
    String serialize() default "";

    /**
     * 负载均衡方式 默认: roundrobin
     *
     * @return
     * @see LoadBalanceEnum
     */
    String loadBalance() default "";
}

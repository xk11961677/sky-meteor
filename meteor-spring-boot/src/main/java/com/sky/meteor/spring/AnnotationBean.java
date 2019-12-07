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
package com.sky.meteor.spring;

import com.sky.meteor.cluster.ClusterInvoker;
import com.sky.meteor.cluster.loadbalance.LoadBalance;
import com.sky.meteor.common.enums.ClusterEnum;
import com.sky.meteor.common.enums.LoadBalanceEnum;
import com.sky.meteor.common.enums.ProxyEnum;
import com.sky.meteor.common.enums.SerializeEnum;
import com.sky.meteor.common.spi.SpiExtensionHolder;
import com.sky.meteor.common.util.ReflectAsmUtils;
import com.sky.meteor.rpc.annotation.Provider;
import com.sky.meteor.rpc.annotation.Reference;
import com.sky.meteor.rpc.consumer.proxy.ProxyFactory;
import com.sky.meteor.serialization.ObjectSerializer;
import com.sky.meteor.spring.util.AopTargetUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 注入客户端代理与加载服务端
 * BeanDefinitionRegistryPostProcessor
 *
 * @author
 */
@Slf4j
public class AnnotationBean implements InitializingBean, BeanPostProcessor {

    static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    static final ConcurrentMap<String, ReferenceBean> referenceConfigs = new ConcurrentHashMap();

    static final ConcurrentMap<Class, Provider> providerConfigs = new ConcurrentHashMap();

    @Setter
    private AnnotationBeanProperties annotationBeanProperties;

    private String annotationPackage;

    private String[] annotationPackages;

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.annotationPackage, "Property 'annotationPackage' is required");
        loadSpiSupport();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Class targetClass = AopTargetUtils.getTarget(bean).getClass();
        this.autowireBeanByMethod(targetClass, bean);
        this.autowireBeanByField(targetClass, bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!isMatchPackage(bean)) {
            return bean;
        }
        Object target = AopTargetUtils.getTarget(bean);
        Provider annotation = AnnotationUtils.findAnnotation(target.getClass(), Provider.class);
        if (annotation != null) {
            ReflectAsmUtils.add(annotation.name(), bean);
            providerConfigs.put(target.getClass(), annotation);
        }
        return bean;
    }

    /**
     * 通过field属性注入bean
     *
     * @param targetClass
     * @param bean
     */
    private void autowireBeanByField(Class targetClass, Object bean) {
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Reference reference = field.getAnnotation(Reference.class);
                if (reference != null) {
                    Object value = refer(reference, field.getType());
                    if (value != null) {
                        field.set(bean, value);
                    }
                }
            } catch (Throwable e) {
                log.error("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 通过set方法注入bean属性
     *
     * @param targetClass
     * @param bean
     */
    private void autowireBeanByMethod(Class targetClass, Object bean) {
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.length() > 3 && name.startsWith("set") && method.getParameterTypes().length == 1
                    && Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                try {
                    Reference reference = method.getAnnotation(Reference.class);
                    if (reference != null) {
                        Object value = refer(reference, method.getParameterTypes()[0]);
                        if (value != null) {
                            method.invoke(bean, new Object[]{});
                        }
                    }
                } catch (Throwable e) {
                    log.error("failed to init remote service reference at method " + name + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 返回接口代理对象
     *
     * @param reference
     * @param referenceClass
     * @return
     */
    private Object refer(Reference reference, Class<?> referenceClass) {
        if (!referenceClass.isInterface()) {
            throw new IllegalStateException("The @Reference undefined interfaceClass, and the property type " + referenceClass.getName() + " is not a interface.");
        }
        String interfaceName = referenceClass.getName();
        String key = interfaceName + ":" + reference.version();
        ReferenceBean referenceBean = referenceConfigs.get(key);
        if (referenceBean == null) {
            referenceBean = new ReferenceBean();
            referenceBean.setReference(reference);
            referenceBean.setInterfaceClass(referenceClass);
            referenceConfigs.putIfAbsent(key, referenceBean);
        }
        return referenceBean.getObject();
    }

    /**
     * 匹配包
     *
     * @param bean
     * @return
     */
    private boolean isMatchPackage(Object bean) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = AopTargetUtils.getTarget(bean).getClass().getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private void loadSpiSupport() {
        SpiExtensionHolder instance = SpiExtensionHolder.getInstance();
        //加载proxy spi
        ProxyEnum proxyEnum = ProxyEnum.acquire(annotationBeanProperties.getProxy());
        instance.loadSpiExtension(ProxyFactory.class, proxyEnum.getKey());

        //加载cluster spi
        ClusterEnum clusterEnum = ClusterEnum.acquire(annotationBeanProperties.getCluster());
        instance.loadSpiExtension(ClusterInvoker.class, clusterEnum.getKey());

        //加载serialize spi
        SerializeEnum serializeEnum = SerializeEnum.acquire(annotationBeanProperties.getSerializer());
        instance.loadSpiExtension(ObjectSerializer.class, serializeEnum.getSerialize());

        //加载loadBalance spi
        LoadBalanceEnum loadBalanceEnum = LoadBalanceEnum.acquire(annotationBeanProperties.getLoadBalance());
        instance.loadSpiExtension(LoadBalance.class, loadBalanceEnum.getKey());
    }

    public void setAnnotationPackage(String annotationPackage) {
        this.annotationPackage = annotationPackage;
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null : COMMA_SPLIT_PATTERN.split(annotationPackage);
    }
}

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
package com.sky.meteor.common.spi;


import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * @author
 */
public class SpiExtensionHolder {

    private static SpiExtensionHolder instance = new SpiExtensionHolder();

    private ConcurrentHashMap<Class, Object> extension = new ConcurrentHashMap();

    private SpiExtensionHolder() {
    }

    public static SpiExtensionHolder getInstance() {
        return instance;
    }

    /**
     * 加载spi extension , 如果未找到则返回默认
     *
     * @param clazz
     * @param name
     * @param <T>
     * @return
     */
    public <T> T loadSpiExtension(Class<T> clazz, String name) {
        ServiceLoader<?> services = SpiLoader.loadAll(clazz);
        T t = (T) StreamSupport.stream(services.spliterator(), true)
                .filter(p -> Objects.equals(p.getClass().getAnnotation(SpiMetadata.class).name(), name))
                .findFirst().orElseGet((Supplier) () -> SpiLoader.loadByName(clazz, clazz.getAnnotation(Spi.class).name()));
        this.loadSpiExtension(clazz, t);
        return t;
    }

    public <T> T get(Class<T> clazz) {
        Object obj = extension.get(clazz);
        if (obj == null) {
            synchronized (instance) {
                obj = extension.get(clazz);
                if (obj == null) {
                    obj = SpiLoader.loadByName(clazz, clazz.getAnnotation(Spi.class).name());
                    this.loadSpiExtension(clazz, obj);
                }
            }
        }
        return (T) obj;
    }

    private void loadSpiExtension(Class clazz, Object obj) {
        extension.put(clazz, obj);
    }
}

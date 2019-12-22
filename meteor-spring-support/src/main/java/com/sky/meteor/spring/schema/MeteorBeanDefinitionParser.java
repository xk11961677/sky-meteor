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
package com.sky.meteor.spring.schema;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sky.meteor.spring.support.MeteorSpringClient;
import com.sky.meteor.spring.support.MeteorSpringConsumerBean;
import com.sky.meteor.spring.support.MeteorSpringProviderBean;
import com.sky.meteor.spring.support.MeteorSpringServer;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
public class MeteorBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> beanClass;

    public MeteorBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if (beanClass == MeteorSpringServer.class) {
            return parseMeteorServer(element, parserContext);
        } else if (beanClass == MeteorSpringClient.class) {
            return parseMeteorClient(element, parserContext);
        } else if (beanClass == MeteorSpringProviderBean.class) {
            return parseMeteorProvider(element, parserContext);
        } else if (beanClass == MeteorSpringConsumerBean.class) {
            return parseMeteorConsumer(element, parserContext);
        } else {
            throw new BeanDefinitionValidationException("Unknown class to definition: " + beanClass.getName());
        }
    }

    private BeanDefinition parseMeteorServer(Element element, ParserContext parserContext) {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(beanClass);

        addProperty(def, element, "registryType", false);
        addProperty(def, element, "port", false);

        List<Pair<String, Object>> options = new ArrayList<>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(def, (Element) item, "registryServerAddresses", false);
                    addProperty(def, (Element) item, "group", false);
                } else if ("options".equals(localName)) {
                    NamedNodeMap attributes = item.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node node = attributes.item(j);
                        options.add(Pair.of(node.getLocalName(), node.getNodeValue()));
                    }
                }
            }
        }
        if (options.size() != 0) {
            def.getPropertyValues().addPropertyValue("options", options);
        }
        return registerBean(def, element, parserContext);
    }

    private BeanDefinition parseMeteorClient(Element element, ParserContext parserContext) {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(beanClass);

        addProperty(def, element, "appName", false);
        addProperty(def, element, "registryType", false);

        List<Pair<String, Object>> options = new ArrayList<>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(def, (Element) item, "registryServerAddresses", false);
                    addProperty(def, (Element) item, "group", false);
                    addProperty(def, (Element) item, "serializerType", false);
                    addProperty(def, (Element) item, "loadBalancerType", false);
                } else if ("options".equals(localName)) {
                    NamedNodeMap attributes = item.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node node = attributes.item(j);
                        options.add(Pair.of(node.getLocalName(), node.getNodeValue()));
                    }
                }
            }
        }
        if (options.size() != 0) {
            def.getPropertyValues().addPropertyValue("options", options);
        }
        return registerBean(def, element, parserContext);
    }

    private BeanDefinition parseMeteorProvider(Element element, ParserContext parserContext) {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(beanClass);

        addPropertyReference(def, element, "server", true);
        addPropertyReference(def, element, "providerImpl", true);
        addProperty(def, element, "name", true);
        addProperty(def, element, "group", true);

        return registerBean(def, element, parserContext);
    }

    private BeanDefinition parseMeteorConsumer(Element element, ParserContext parserContext) {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(beanClass);

        addPropertyReference(def, element, "client", true);
        addProperty(def, element, "interfaceClass", true);
        addProperty(def, element, "group", false);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(def, (Element) item, "version", false);
                    addProperty(def, (Element) item, "extLoadBalancerName", false);
                    addProperty(def, (Element) item, "waitForAvailableTimeoutMillis", false);
                    addProperty(def, (Element) item, "invokeType", false);
                    addProperty(def, (Element) item, "dispatchType", false);
                    addProperty(def, (Element) item, "timeoutMillis", false);
                    addProperty(def, (Element) item, "providerAddresses", false);
                    addProperty(def, (Element) item, "clusterStrategy", false);
                    addProperty(def, (Element) item, "failoverRetries", false);
                }
            }
        }
        return registerBean(def, element, parserContext);
    }

    private BeanDefinition registerBean(RootBeanDefinition definition, Element element, ParserContext parserContext) {
        String id = element.getAttribute("id");
        if (Strings.isNullOrEmpty(id)) {
            id = beanClass.getSimpleName();
        }
        if (parserContext.getRegistry().containsBeanDefinition(id)) {
            throw new IllegalStateException("Duplicate meteor bean id: " + id);
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());

        return definition;
    }

    private static void addProperty(
            RootBeanDefinition definition, Element element, String propertyName, boolean required) {
        String ref = element.getAttribute(propertyName);
        if (required) {
            checkAttribute(propertyName, ref);
        }
        if (!Strings.isNullOrEmpty(ref)) {
            definition.getPropertyValues().addPropertyValue(propertyName, ref);
        }
    }

    private static void addPropertyReference(
            RootBeanDefinition definition, Element element, String propertyName, boolean required) {
        String ref = element.getAttribute(propertyName);
        if (required) {
            checkAttribute(propertyName, ref);
        }
        if (!Strings.isNullOrEmpty(ref)) {
            definition.getPropertyValues().addPropertyValue(propertyName, new RuntimeBeanReference(ref));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void addPropertyReferenceArray(
            RootBeanDefinition definition, Element element, String elementTypeName, String propertyName, boolean required) {
        String[] refArray = StringUtils.split(element.getAttribute(propertyName), ",");
        List<RuntimeBeanReference> refBeanList = Lists.newArrayListWithCapacity(refArray.length);
        for (String ref : refArray) {
            ref = ref.trim();
            if (required) {
                checkAttribute(propertyName, ref);
            }
            if (!Strings.isNullOrEmpty(ref)) {
                refBeanList.add(new RuntimeBeanReference(ref));
            }
        }

        if (!refBeanList.isEmpty()) {
            ManagedArray managedArray = new ManagedArray(elementTypeName, refBeanList.size());
            managedArray.addAll(refBeanList);
            definition.getPropertyValues().addPropertyValue(propertyName, managedArray);
        }
    }

    private static String checkAttribute(String attributeName, String attribute) {
        if (Strings.isNullOrEmpty(attribute)) {
            throw new BeanDefinitionValidationException("Attribute [" + attributeName + "] is required.");
        }
        return attribute;
    }
}

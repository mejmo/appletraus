/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Martin Formanko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mejmo.appletraus.client.domain;

import com.mejmo.appletraus.client.utils.Util;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MFO on 18.10.2015.
 */
public class AppletRausInstanceProxy {

    private static final Logger logger = LoggerFactory.getLogger(AppletRausInstanceProxy.class);

    private Applet applet;
    private MultiKeyMap mappedMethods = new MultiKeyMap();
    private String atmosphereUrl;

    public void assignMethods() {
        List<Method> methods = Util.getPublicMethods(applet.getClass());
        mappedMethods.clear();
        for (Method method : methods) {
            if (mappedMethods.containsKey(method.getName(), method.getParameterTypes().length)) {
                logger.error("Found public method " + method.getName() + " but it was already discovered with this count of " +
                        "parameters. Overloading parameter types not supported yet.");
                continue;
            }
            logger.debug("Found public method "+method.getName()+" with "+method.getParameterTypes().length+" parameter count");
            mappedMethods.put(method.getName(), method.getParameterTypes().length, method);
        }
    }

    public Object invokeMethod(String methodName, List<Object> parameters) throws InvocationTargetException, IllegalAccessException {
        Method method = (Method)mappedMethods.get(methodName, new Integer(parameters.size()));
        return method.invoke(applet, parameters.toArray());
    }

    public void start() {
        logger.info("Starting to initialize applet. Calling init method");
        applet.init();
        logger.info("Applet initialized.");
    }

    public List getMappedMethodNames() {
        List<String> methods = new ArrayList<>();
        for (Object key : mappedMethods.keySet().toArray()) {
            methods.add(((MultiKey) key).getKey(0).toString());
        }
        return methods;
    }

    public Applet getApplet() {
        return applet;
    }

    public void setApplet(Applet applet) {
        this.applet = applet;
    }

    public String getAtmosphereUrl() {
        return this.atmosphereUrl;
    }

    public void setAtmosphereUrl(String atmosphereUrl) {
        this.atmosphereUrl = atmosphereUrl;
    }



}

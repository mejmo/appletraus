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

package com.mejmo.appletraus.client.service;

import com.mejmo.appletraus.client.CookieStore;
import com.mejmo.appletraus.client.config.AppletRausInstanceProxyFactory;
import com.mejmo.appletraus.client.domain.AppletRausContext;
import com.mejmo.appletraus.client.domain.AppletRausInstanceProxy;
import com.mejmo.appletraus.client.domain.AppletRausStub;
import com.mejmo.appletraus.client.exceptions.AppletRausException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.protocol.file.Handler;

import javax.swing.*;
import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MFO on 17.10.2015.
 */
public class AppletExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AppletExecutor.class);

    public AppletRausInstanceProxy createAppletInstance(String tag, String cookies, String baseUrl, String sessionId) {

        Document doc = Jsoup.parse(tag);
        Elements appletElement = doc.select("applet");

        List<URL> appletUrl = new ArrayList<>();
        try {
            for (String jarUrl : appletElement.get(0).attr("archive").split(",")) {
                appletUrl.add(new URL(baseUrl + appletElement.get(0).attr("codebase") + "/" + jarUrl));
            }
        } catch (MalformedURLException e) {
            throw new AppletRausException("Applet URL is malformed. Cannot extract it from applet HTML tag.", e);
        }

        logger.info("Parsed applet URL: "+appletUrl.toString());

        Class startClass;

        //TODO: update the class name and get it from parameter
        startClass = JApplet.class;

        Applet appletObject = null;
        try {
            AppletRausStub stub = new AppletRausStub(
                    new URL(baseUrl + appletElement.get(0).attr("codebase")),
                    new URL(baseUrl + appletElement.get(0).attr("documentbase")),
                    getParams(appletElement.select("param")),
                    new AppletRausContext()
            );
            appletObject = (Applet)startClass.newInstance();
            appletObject.setStub(stub);
            CookieStore.setBrowserCookie(cookies);

        } catch (InstantiationException | IllegalAccessException | MalformedURLException e) {
            throw new AppletRausException("Error occured while initializing applet instance", e);
        }

        AppletRausInstanceProxy proxy = new AppletRausInstanceProxyFactory().getAppletProxy(appletObject, baseUrl);
        proxy.assignMethods();
        new AtmosphereService().connectAtmosphere(baseUrl, proxy, sessionId);
        proxy.start();

        return proxy;

    }

    public Map getParams(Elements params) {
        Map<String, String> map = new HashMap<>();
        for (Element element : params) {
            logger.info("Found parameter "+element.attr("name")+" with value "+element.attr("value"));
            map.put(element.attr("name"), element.attr("value"));
        }
        return map;
    }

}

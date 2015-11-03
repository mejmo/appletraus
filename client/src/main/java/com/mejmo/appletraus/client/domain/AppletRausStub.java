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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.URL;
import java.util.Map;

/**
 * Created by MFO on 17.10.2015.
 */
public class AppletRausStub implements AppletStub {

    private static final Logger logger = LoggerFactory.getLogger(AppletRausStub.class);

    private URL codeBase;
    private URL documentBase;
    private Map<String, String> parameters;
    private AppletContext appletContext;

    public AppletRausStub(URL codeBase, URL documentBase, Map<String, String> parameters, AppletContext appletContext) {
        this.codeBase = codeBase;
        this.documentBase = documentBase;
        this.parameters = parameters;
        this.appletContext = appletContext;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public URL getDocumentBase() {
        return documentBase;
    }

    @Override
    public URL getCodeBase() {
        return codeBase;
    }

    @Override
    public String getParameter(String name) {
        return parameters.containsKey(name) ? parameters.get(name) : null;
    }

    @Override
    public AppletContext getAppletContext() {
        return appletContext;
    }

    @Override
    public void appletResize(int width, int height) {
        logger.info("Applet resize called, but the desired action not known.");
    }

    public void setCodeBase(URL codeBase) {
        this.codeBase = codeBase;
    }

    public void setDocumentBase(URL documentBase) {
        this.documentBase = documentBase;
    }


    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }
}

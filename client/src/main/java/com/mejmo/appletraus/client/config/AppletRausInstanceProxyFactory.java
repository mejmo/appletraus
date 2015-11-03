package com.mejmo.appletraus.client.config;

import com.mejmo.appletraus.client.Constants;
import com.mejmo.appletraus.client.domain.AppletRausInstanceProxy;

import java.applet.Applet;

/**
 * Created by MFO on 17.10.2015.
 */
public class AppletRausInstanceProxyFactory implements Constants {

    public AppletRausInstanceProxy getAppletProxy(Applet applet, String baseUrl) {
        AppletRausInstanceProxy proxy = new AppletRausInstanceProxy();
        proxy.setApplet(applet);
        proxy.setAtmosphereUrl(baseUrl + DEFAULT_ATMOSPHERE_ENDPOINT);
        return proxy;
    }



}

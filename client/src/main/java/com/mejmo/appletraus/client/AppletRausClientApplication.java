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

package com.mejmo.appletraus.client;

import com.mejmo.appletraus.client.service.AppletExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class AppletRausClientApplication implements Constants{

    protected static final Logger logger = LoggerFactory.getLogger(AppletRausClientApplication.class);

    public static void run(String... args) throws Exception {

        if (args.length != 4)
            throw new RuntimeException("Not cookie as argument present");

        String cookies = args[0];
        String sessionId = args[1];
        String tag = args[2];
        String baseUrl = args[3];

        AppletExecutor exec = new AppletExecutor();
        exec.createAppletInstance(tag, cookies, baseUrl, sessionId);

        logger.info("Waiting in background for method calls...");
        CountDownLatch latch = new CountDownLatch(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.warn("Exit command received. Quitting XMLProvider.");
        }

    }

    protected static String getProductionProfileName() {
        return SPRING_PROFILE_PROD;
    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

}

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

package com.mejmo.appletraus.server.controller;

import com.mejmo.appletraus.common.domain.Message;
import com.mejmo.appletraus.common.domain.MessageType;
import com.mejmo.appletraus.common.domain.RESTMethodInvoke;
import com.mejmo.appletraus.server.config.AtmosphereConfig;
import com.mejmo.async2sync.Async2SyncExecutor;
import com.mejmo.async2sync.Async2SyncRunnable;
import com.mejmo.async2sync.AsyncRequestId;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.MetaBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MFO on 18.10.2015.
 */
@RestController
public class MethodInvokeController {

    private static final Logger logger = LoggerFactory.getLogger(MethodInvokeController.class);
    public static Map<String, BroadcasterFactory> broadcasterFactoryMap = new HashMap<>();

//    @Inject
//    private BroadcasterFactory broadcast;

    @RequestMapping(value = "/methodInvoke/{sessionId}", method = RequestMethod.POST)
    public ResponseEntity<?> invokeMethod(@PathVariable final String sessionId,
                                          @RequestBody final RESTMethodInvoke restMethodInvoke) throws Exception {

        if (!getBroadcasterMap().containsKey(sessionId))
            return new ResponseEntity("No related sessionid", HttpStatus.FORBIDDEN);

        logger.info("Method invoke request called. SessionId: " + sessionId);

        final AsyncRequestId asyncId = new AsyncRequestId();
        Async2SyncExecutor async2SyncExecutor = new Async2SyncExecutor();
        String result = async2SyncExecutor.start(String.class, asyncId, new Async2SyncRunnable() {
            @Override
            public void executeAsyncRequest() {
                Message message = new Message();
                message.setMessageType(MessageType.METHOD_CALL);
                message.setMethod(restMethodInvoke.getMethod());
                message.setMethodCallId(asyncId.getId());
                message.setParameters(restMethodInvoke.getParameters());
                getBroadcasterMap().get(sessionId).lookup(sessionId).broadcast(message);
            }
        });

        return new ResponseEntity(result, HttpStatus.OK);

    }

    public static synchronized Map<String, BroadcasterFactory> getBroadcasterMap() {
        return broadcasterFactoryMap;
    }

}

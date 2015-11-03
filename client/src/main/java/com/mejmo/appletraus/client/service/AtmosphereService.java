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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mejmo.appletraus.client.domain.AppletRausInstanceProxy;
import com.mejmo.appletraus.client.exceptions.AppletRausException;
import com.mejmo.appletraus.common.domain.Message;
import com.mejmo.appletraus.common.domain.MessageType;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by MFO on 18.10.2015.
 */
public class AtmosphereService {

    private static final Logger logger = LoggerFactory.getLogger(AtmosphereService.class);

    public void connectAtmosphere(String baseUrl, final AppletRausInstanceProxy applet, String sessionId) {

        AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
        final ObjectMapper mapper = new ObjectMapper();

        RequestBuilder request = client.newRequestBuilder()
                .method(Request.METHOD.GET)
                .uri(baseUrl+"/appletraus-boot/appletraus?sessionId="+sessionId)
                .encoder(new Encoder<Message, String>() {        // Stream the request body
                    @Override
                    public String encode(Message s) {
                        try {
                            return mapper.writeValueAsString(s);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .decoder(new Decoder<String, Message>() {
                    @Override
                    public Message decode(Event type, String s) {
                        Message message;

                        if (type != Event.MESSAGE || s.trim().length() == 0)
                            return null;

                        try {
                            message = mapper.readValue(s, Message.class);
                        } catch (IOException e) {
                            logger.error("Cannot decode JSON message: "+s);
                            return null;
                        }
                        return message;
                    }
                })
                .transport(Request.TRANSPORT.WEBSOCKET)
                .transport(Request.TRANSPORT.LONG_POLLING)
                .trackMessageLength(true);

        final Socket socket = client.create();
        try {
            socket.on(Event.CLOSE.name(), new Function<String>() {
                @Override
                public void on(String t) {
                    logger.info("Atmosphere  resource closed. Existing JNLP application");
                    System.exit(0);
                }
            }).on(Event.REOPENED.name(), new Function<String>() {
                @Override
                public void on(String t) {
                }
            }).on(Event.MESSAGE.name(), new Function<Message>() {
                @Override
                public void on(Message t) {

                    logger.info("Got message");
                    if (t.getMessageType() == MessageType.METHOD_CALL) {
                        try {
                            Object ret = applet.invokeMethod(t.getMethod(), t.getParameters());
                            t.setMessageType(MessageType.METHOD_RESULT);
                            t.setResult(mapper.writeValueAsString(ret));
                        } catch (IllegalAccessException | InvocationTargetException | JsonProcessingException e) {
                            throw new AppletRausException("Error occured while invoking the method", e);
                        }
                        try {
                            socket.fire(t);
                        } catch (IOException e) {
                            throw new AppletRausException("Error occured while sending method result", e);
                        }
                    }
                    if (t.getMessageType() == MessageType.DISCONNECT) {
                        logger.info("DISCONNECT message received. Existing JNLP application");
                        System.exit(0);
                    }

                }
            }).on(new Function<IOException>() {
                @Override
                public void on(IOException ioe) {
                    ioe.printStackTrace();
                }
            }).on(Event.OPEN.name(), new Function<String>() {
                @Override
                public void on(String t) {

                    logger.info("Atmosphere connection established.");
                    Message message = new Message();
                    message.setMessageType(MessageType.GET_AVAILABLE_METHODS);
                    message.getAppletMethodList().addAll(applet.getMappedMethodNames());
                    try {
                        socket.fire(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).open(request.build())
                    .fire("echo")
                    .fire("bong");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}

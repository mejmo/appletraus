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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mejmo.appletraus.common.domain.Message;
import com.mejmo.appletraus.common.domain.MessageType;
import com.mejmo.async2sync.Async2SyncExecutor;
import com.mejmo.async2sync.AsyncRequestId;
import com.mejmo.async2sync.exceptions.RequestNotRegistered;
import org.atmosphere.config.managed.Decoder;
import org.atmosphere.config.managed.Encoder;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Time;

/**
 * Created by MFO on 18.10.2015.
 */
@Component
@ManagedService(path = "/appletraus")
public class AtmosphereController {

    private final Logger logger = LoggerFactory.getLogger(AtmosphereController.class);

    @Ready
    public void onReady(final AtmosphereResource resource) {
        this.logger.info("ApplatRaus atmosphere client connected", resource.uuid());

        String sessionId = resource.getRequest().getParameter("sessionId");
        if (MethodInvokeController.getBroadcasterMap().containsKey(sessionId)) {
            MethodInvokeController.getBroadcasterMap().remove(sessionId);
        }

        MethodInvokeController.getBroadcasterMap().put(sessionId, resource.getAtmosphereConfig().getBroadcasterFactory());

        BroadcasterFactory factory = resource.getAtmosphereConfig().getBroadcasterFactory();
        if (factory.lookup(resource.getRequest().getParameter("sessionId")) == null)
            factory.add(resource.getBroadcaster(), resource.getRequest().getParameter("sessionId"));
        factory.lookup(sessionId).addAtmosphereResource(resource);

    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        this.logger.info("AppletRaus client {} disconnected [{}]", event.getResource().uuid(), (event.isCancelled() ? "cancelled" : "closed"));
        BroadcasterFactory factory = event.getResource().getAtmosphereConfig().getBroadcasterFactory();
        Message message = new Message();
        message.setMessageType(MessageType.DISCONNECT);
        factory.lookup(event.getResource().getRequest().getParameter("sessionId")).broadcast(message);
    }

    @org.atmosphere.config.service.Message(encoders = JacksonEncoderDecoder.class, decoders = JacksonEncoderDecoder.class)
    public Message onMessage(Message message) throws IOException {
        this.logger.info("AppletRaus client sent message method: {}, result: {}", message.getMethod(), message.getResult());

        if (message.getMessageType() == MessageType.METHOD_RESULT) {
            try {
                Async2SyncExecutor.responseReceived(new AsyncRequestId(message.getMethodCallId()), message.getResult());
            } catch (RequestNotRegistered requestNotRegistered) {
                requestNotRegistered.printStackTrace();
            }
        }

        return message;
    }

    public static class JacksonEncoderDecoder implements Encoder<Message, String>, Decoder<String, Message> {

        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String encode(Message m) {
            try {
                return this.mapper.writeValueAsString(m);
            }
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public Message decode(String s) {
            try {
                return this.mapper.readValue(s, Message.class);
            }
            catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

}
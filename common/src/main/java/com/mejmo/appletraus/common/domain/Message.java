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

package com.mejmo.appletraus.common.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message extends RESTMethodInvoke {

	private MessageType messageType;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	private String result;

	public List<AppletMethod> getAppletMethodList() {
		if (appletMethodList == null)
			appletMethodList = new ArrayList<AppletMethod>();
		return appletMethodList;
	}

	public void setAppletMethodList(List<AppletMethod> appletMethodList) {
		this.appletMethodList = appletMethodList;
	}

	private List<AppletMethod> appletMethodList;

	private long time = new Date().getTime();

	public long getTime() {
		return this.time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}

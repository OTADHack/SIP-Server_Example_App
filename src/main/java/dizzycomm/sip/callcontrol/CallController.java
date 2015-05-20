package dizzycomm.sip.callcontrol;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ConvergedHttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is called by the JSP pages to terminate a call that is in
 * progress.
 * 
 * @author Copyright (c) 2006 by BEA Systems, Inc. All Rights Reserved.
 */

public final class CallController {
	public static final String CTRL_ATTR_NAME = "dizzycomm.sip.callcontrol.CallController";

	private final HashMap<String, String> calls = new HashMap<String, String>();
	private final SipSessionsUtil util;
	private final String contextPath;

	public CallController(SipSessionsUtil s, String contextPath) {
		util = s;
		this.contextPath = contextPath;
	}

	public synchronized String[] getAllCalls() {
		Iterator<String> iter = calls.values().iterator();
		String[] result = new String[calls.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (String) iter.next();
		}
		return result;
	}

	public synchronized boolean terminateAll() {
		for (String appSessionId : calls.keySet()) {
			SipApplicationSession as = util.getApplicationSessionById(appSessionId);
			terminateCall(as);
		}
		calls.clear();
		return true;
	}

	public synchronized boolean terminateCall(HttpSession httpSession) {
		SipApplicationSession appSession = ((ConvergedHttpSession) httpSession)
				.getApplicationSession();
		try {
			httpSession.invalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (appSession == null) {
			debug("No application session found for: " + httpSession.getId());
			return false;
		}
		String appSessionId = appSession.getId();
		terminateCall(appSession);
		calls.remove(appSessionId);
		return true;
	}

	private void terminateCall(SipApplicationSession appSession) {
		appSession.setAttribute("CC", "BYE");
		
		Iterator<?> iter = appSession.getSessions("SIP");
	
		while (iter.hasNext()) {
			SipSession sess = (SipSession) iter.next();
			try {
				debug("Sending one BYE message to : " + sess.getAttribute("to"));
				sess.createRequest("BYE").send();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void unregisterCall(SipApplicationSession appSession) {
		calls.remove(appSession.getId());
	}

	public synchronized void registerCall(SipApplicationSession applicationSession) {
		String url = null;
		try {
			// To do in later versions of this course.
			// Use WebLogic JMX to find the listen address of the server.
			URL urlToEncode = new URL("http", "10.0.2.15", 7001, contextPath + "/testPages/terminateCall.jsp");
			url = applicationSession.encodeURL(urlToEncode).toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		debug("registering call for " + applicationSession.getId());
		debug("encoded url=" + url);
		
		// To do in later versions of this course.
		// Get the participants of the call and store that together with the URL.
		// Show the participants of the call on the webpage instead of the URL.
		calls.put(applicationSession.getId(), url);
	}

	private static void debug(String msg) {
		System.out.println("CallController> " + msg);
	}

}

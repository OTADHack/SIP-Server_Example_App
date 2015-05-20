package dizzycomm.sip.callcontrol;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.UAMode;

import dizzycomm.sip.BaseSipServlet;
import dizzycomm.sip.registrar.DCRegistrar;
import dizzycomm.sip.registrar.RegistrationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Back-to-back-user-agent (B2BUA) implementation using the B2buaHelper
 * API introduced in JSR 289.
 * 
 * @author Copyright (c) 2006 by BEA Systems, Inc. All Rights Reserved.
 */

public class DCB2BUA extends BaseSipServlet {
	private static final long serialVersionUID = 4399694080165402412L;
	private static final String ATTR_NAME = "javax.servlet.sip.SipSessionsUtil";

	private static CallController callController;

	private B2buaHelper b2bHelper = null;
	private ServletContext servletContext;

	public void init(ServletConfig sc) throws ServletException {

		super.init(sc);
		trace("%%%% DizzyComm - DCB2BUA Loaded %%%%");

		servletContext = getServletContext();
		SipSessionsUtil util = (SipSessionsUtil) servletContext.getAttribute(ATTR_NAME);

		CallController cc = new CallController(util, servletContext.getContextPath());
		servletContext.setAttribute(CallController.CTRL_ATTR_NAME, cc);
		callController = (CallController) servletContext.getAttribute(CallController.CTRL_ATTR_NAME);
	}

	protected void doInvite(SipServletRequest req1) throws ServletException,
			IOException {

		// Track the requests
		trace("\n" + this.getClass().getName() + ":doInvite()");
		trace(req1);

		// Create a URI for resolving in the registration service
		SipURI myUri = (SipURI) req1.getRequestURI().clone();

		// Remove the user parameter from the SipURI object
		myUri.removeParameter("user");

		// Validate the SipURI by resolving it.
		List<URI> contacts = resolve(myUri);

		// If the contact isn't registered, return "Not Found"
		if (contacts.isEmpty()) {
			SipServletResponse resp = req1
					.createResponse(SipServletResponse.SC_NOT_FOUND);
			trace(resp);
			resp.send();
			return;
		}

		// If the contact is resolved, meaning present in this servers
		// registrar, connect the caller to the FIRST resolved contact.
		// If the contact has several registration we should try several
		// adresses, but that is not implemented in this example.
		if (!contacts.isEmpty()) {
			trace("\n Found contact info locally - " + contacts);
			trace("\n Using the first registered contact: "
					+ ((SipURI) contacts.get(0)).toString());

			// Get the first registered addresses
			SipURI sipUri = (SipURI) contacts.get(0);

			// Get the B2BUA Helper
			b2bHelper = req1.getB2buaHelper();
			
			// implicitly link both requests and sessions
			SipServletRequest req2 = b2bHelper.createRequest(req1, true, null);

			// change the requestURI of req2
			SipURI req2SipURI = (SipURI) req1.getRequestURI().clone();
			req2SipURI.setHost(sipUri.getHost());
			req2SipURI.setPort(sipUri.getPort());
			req2.setRequestURI(req2SipURI);

			// Copy the content of the incoming request to the outgoing request
			contentCpy(req1, req2);
			
			// Send the request
			trace(req2);
			req2.send();
			return;
		}
	}

	protected void doAck(SipServletRequest ack1) throws ServletException,
			IOException {
    	trace(ack1, "doAck()");
		SipSession leg2 = b2bHelper.getLinkedSession(ack1.getSession());

		// get the pending 200 response from the session corresponding to leg2
		List<SipServletMessage> pendingMessages = b2bHelper.getPendingMessages(
				leg2, UAMode.UAC);

		validatePendingMessages(pendingMessages);

		SipServletResponse res = (SipServletResponse) pendingMessages.get(0);
		res.createAck().send();
	}

	protected void doBye(SipServletRequest bye1) throws ServletException,
			IOException {
    	trace(bye1, "doBye()");
		SipSession peerLeg = b2bHelper.getLinkedSession(bye1.getSession());
		SipServletRequest bye2 = b2bHelper.createRequest(peerLeg, bye1, null);
		bye2.send();
	}

	protected void doCancel(SipServletRequest cancel1) throws ServletException,
			IOException {
    	trace(cancel1, "doCancel()");
		SipSession leg2 = b2bHelper.getLinkedSession(cancel1.getSession());
		SipServletRequest cancel = b2bHelper.createCancel(leg2);
		cancel.send();
	}

	protected void doProvisionalResponse(SipServletResponse resp)
			throws ServletException, IOException {
    	trace(resp, "doProvisionalResponse()");
		forward(resp);
	}

	protected void doErrorResponse(SipServletResponse resp)
			throws ServletException, IOException {
    	trace(resp, "doErrorResponse()");
		forward(resp);
	}

	protected void doSuccessResponse(SipServletResponse resp)
			throws ServletException, IOException {
    	trace(resp, "doSuccessResponse()");
    	
    	// Get the linked session for this response
		SipSession leg1 = b2bHelper.getLinkedSession(resp.getSession());

    	// Get the SIP application session
		SipApplicationSession as = leg1.getApplicationSession();
		
    	// Declare a new response that we are going to use to send to the other leg. 
		SipServletResponse resToSend = null;

		if (resp.getMethod().equalsIgnoreCase("INVITE")) {

			// Set attribute to mark that call was started by B2BUA
			as.setAttribute("CC", "B2BUA");
			
			// Register the call in the Call Controller
			trace("Registering call for session: " + as.getId());
			callController.registerCall(as);

			// Create response to send to the other leg.
			resToSend = b2bHelper.createResponseToOriginalRequest(leg1,
					resp.getStatus(), resp.getReasonPhrase());
		} else if (resp.getMethod().equalsIgnoreCase("BYE")) {

			// Check if the CallController is ending this call.
			// If so, the CallController will end both legs and unregister the call.
			// Do not send a BYE message to the other leg, just return.
			String ccCheck = (String) as.getAttribute("CC");
			if (ccCheck.equalsIgnoreCase("BYE"))
				return;
			
			// Unregister the call in the Call Controller
			trace("Unregistering call for session: " + as.getId());
			callController.unregisterCall(as);

			// Get the linked request.
			SipServletRequest origReq = b2bHelper
					.getLinkedSipServletRequest(resp.getRequest());

			// Create response to send to the other leg.
			resToSend = origReq.createResponse(resp.getStatus(),
					resp.getReasonPhrase());
		}

		// Copy the content of the incoming request to the outgoing request
		contentCpy(resp, resToSend);

		// forward response on the other leg
		resToSend.send();
	}

	private void forward(SipServletResponse resp) throws IOException {
    	trace(resp, "forward()");
		SipServletRequest req1 = b2bHelper.getLinkedSipServletRequest(resp
				.getRequest());
		SipServletResponse resToSend = req1.createResponse(resp.getStatus());
		contentCpy(resp, resToSend);
		resToSend.send();
	}

	private void contentCpy(SipServletMessage msg1, SipServletMessage msg2) {
		try {
			if (msg1.getContentType() != null) {
				msg2.setContent(msg1.getRawContent(), msg1.getContentType());
			}
		} catch (IOException e) {
			log("Error in Copying content: " + e);
		}
	}

	private void validatePendingMessages(List<SipServletMessage> pendingMessages) {
		// validation of pending messages
		if (pendingMessages.size() != 1) {
			trace("Pending pendingMessages = " + pendingMessages.size());
			for (SipServletMessage msg : pendingMessages) {
				trace(msg.toString());
			}
			throw new IllegalStateException(
					"More than 1 pending messages on UAC session");
		}

		if (!(pendingMessages.get(0) instanceof SipServletResponse)) {
			trace("Pending message not a SipServletResponse, msg: "
					+ pendingMessages.get(0).toString());
			throw new IllegalStateException(
					"Pending message not a SipServletResponse");
		}

	}

	/**
	 * <b>resolve()</b> - Connect to the registry service through the Registrar
	 * to resolve if the URI is in the registry
	 * 
	 * Method should be public, so to expose it to other servlets
	 * 
	 * @param uri
	 *            URI
	 * 
	 * @return List
	 */
	public List<URI> resolve(URI uri) {
		// Get a handle to the Registration Service
		RegistrationService rs = DCRegistrar.getService(servletContext);

		// Retrieve the array of registered addresses for the sip URI
		Address[] contacts = rs.resolve((SipURI) uri);

		// Declare a list of URIs which we will return to the caller of this
		// method
		List<URI> result = new ArrayList<URI>();

		// Loop through the registered addresses and add the URIs to the list
		for (int i = 0; i < contacts.length; i++)
			result.add(contacts[i].getURI());

		// Return the list of registered URIs
		return result;
	}

}

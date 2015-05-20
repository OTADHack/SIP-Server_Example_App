package dizzycomm.sip.location;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.Address;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import dizzycomm.sip.BaseSipServlet;
import dizzycomm.sip.registrar.*;

/**
 * @author BEA Worldwide Education Services - Copyright (c) 2005/2006
 */
public class DCLocation extends BaseSipServlet {

	private static final long serialVersionUID = 4497920437855310349L;
	
	private ServletContext servletContext;
	
	public void init(ServletConfig sc) throws ServletException {
		
		super.init(sc);
		trace("%%%% DizzyComm - DCLocation Loaded %%%%");
		
		servletContext = getServletContext();
		
		//Make this servlet's service methods available to other servlets.
		servletContext.setAttribute("LOCATION_SVC", this);
	}
	
	/**
	 * Handles initial INVITE requests and attempts to connect the
	 * initiating caller with a UA registered on THIS server. 
	 * If a match is not found, a 404 Not Found should be returned
	 * to the initiator.
	 *
	 * This Servlet is always the first to receive INVITE requests in this
	 * implementation
	 *
	 * @param req SipServletRequest
	 *
	 * @return void
	 *
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doInvite(SipServletRequest req)
    		throws ServletException, IOException {
		
	// If this request is NOT inital, it means it's part of an existing
	// conversation. Call super.doInvite() to log a trace and return.
    if (!req.isInitial()) {
    	super.doInvite(req);
    	return;
    }

    // Track the requests and responses.
    trace("\n %%%%%% Method: " + req.getMethod());

    trace("\n" + this.getClass().getName() + ":doInvite()");
    trace("\n ====req.getRequestURI(): " + req.getRequestURI());
    trace("\n ====req.getTo().getURI(): " + req.getTo().getURI());

    //  Create a SipURI object by cloning the request URI.
    SipURI myUri = (SipURI) req.getRequestURI().clone();

    // Remove the user parameter from the SipURI object
    myUri.removeParameter("user");
    trace("\n ====myUri: " + myUri);

    // Validate the SipURI by resolving it.
    List<URI> contacts = resolve(myUri);
    if (contacts.isEmpty()) {
    	SipServletResponse resp = req.createResponse(SipServletResponse.SC_NOT_FOUND);
		trace(resp);
		resp.send();
		return;
    }

    // If the contact is resolved, meaning present in this servers registrar,
    // connect the caller to the resolved contact.
    if (!contacts.isEmpty()) {
    	trace("\n Found contact info locally - " + contacts );
		Proxy p = req.getProxy();
    	p.proxyTo(contacts);
    	return;
    }
  }

	/**
	 * <b>resolve()</b> - Connect to the registry service through 
	 * the Registrar to resolve if the URI is in the registry
	 * 
	 * Method should be public, so to expose it to other servlets
	 *
	 * @param uri URI
	 *
	 * @return List
	 */
  public List<URI> resolve(URI uri) {
	  Address[] contacts = DCRegistrar.getService(getServletContext()).resolve((SipURI) uri);
	  List <URI> result = new ArrayList<URI>();
	  for (int i = 0; i < contacts.length; i++) result.add(contacts[i].getURI());
	  return result;
  }
}

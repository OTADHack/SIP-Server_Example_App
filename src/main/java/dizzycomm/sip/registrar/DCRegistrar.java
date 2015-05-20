package dizzycomm.sip.registrar;

/**
 * @author BEA Worldwide Education Services - Copyright (c) 2005/2006
 */

import dizzycomm.sip.BaseSipServlet;
import dizzycomm.sip.registrar.LocalRegistrationService;
import dizzycomm.sip.registrar.DBMSRegistrationService;
import dizzycomm.sip.registrar.Registration;
import dizzycomm.sip.registrar.RegistrationService;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

public final class DCRegistrar extends BaseSipServlet {

	private static final long serialVersionUID = 3182099143509979589L;

	// A string that represents the chosen Registration Service. This String is
	// put into the ServletContext as the key for the actual Registration
	// service for the other Servlets to access.
	public static final String SERVICE_ATTR = "dizzycomm.sip.REGISTRATION_SERVICE";

	// The RegistrationService interface along with the variable name for the
	// chosen registration service.
	private RegistrationService registrationService;

	// SipFactory is always app specific, so it cannot be declared static
	private SipFactory factory;

	/**
	 * Sets up reference to SipFactory object as well as access to the Sip
	 * MBeans so that a check can be made to see if the server(s) are running in
	 * a replicated configuration. Finally a handle to the appropriate
	 * RegistrationService class is obtained.
	 * 
	 * The final version of this Servlet also retrieves proxy information from
	 * the database for the Location system to use.
	 * 
	 * @param sc
	 *            ServletConfig
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 */
	public void init(ServletConfig sc) throws ServletException {

		super.init(sc);
		trace("%%%% DizzyComm - DCRegistrar Loaded %%%%");

		// Get the SipFactory using the method provided by the BaseSipServlet
		// class
		factory = (SipFactory) this.getSipFactory();

		// Check sip.xml to determine if a database is being used to store
		// registry data, or an in-memory local registry is used.
		// (No clustering is possible with the local type)
		boolean use_db_for_registry = false;
		if (getContextParam("registry_db_storage").equalsIgnoreCase("true")) {
			use_db_for_registry = true;
		}

		// Based on the param value, load either the in-memory or the RDBMS
		// registry service.
		if (!use_db_for_registry) {
			System.out
					.println("+++ DizzyComm - Registrar Notice: Storing registrations locally - App cannot cluster in this configuration.");
			registrationService = new LocalRegistrationService();
		} else {
			System.out
					.println("+++ DizzyComm - Registrar  Notice: Accessing DBMS based registration store");
			registrationService = new DBMSRegistrationService();
			try {
				registrationService.init();
			} catch (IllegalStateException e) {
				System.out
						.println("+++ DizzyComm - Registrar Notice: Unable to create DBMS RegistrationService, "
								+ "please make sure the connection pool and data source with name: "
								+ "'"
								+ DBMSRegistrationService.DATA_SOURCE_NAME
								+ "'has been defined");
				throw new ServletException(e);
			}
		}

		// Add the configured registration service to the servlet context for
		// the other SIP Servlets to access.
		sc.getServletContext().setAttribute(SERVICE_ATTR, registrationService);
	}

	/**
	 * Processes incoming REGISTER requests, doRegister() in BaseSIPServlet is
	 * called to implement tracing if needed.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws IOException
	 * @throws ServletParseException
	 */
	public void doRegister(SipServletRequest req) throws IOException,
			ServletParseException {

		// Create a new Registration object to hold the REGISTER in progress
		Registration r = new Registration(req, factory);

		// Check to make sure that there is at least one contact in the Contacts
		// header.
		// No Contact -> Returns the current registration information and exit
		if (r.getContacts().size() == 0) {
			trace("\n doRegister: No contacts in the header. Reply with contact list");
			sendSuccess(req, r.getUri());
			return;
		}

		// Check for Wildcards in Contact header.
		// As per the SIP 3261 specification, a wildcard (*) in the contacts
		// header signifies that all related registrations should be canceled.
		// This type of registration action is handled seperately in
		// doWildCardRegister().
		if (r.hasWildcard()) {
			trace("\n doRegister: Wild card registration");
			doWildcardRegister(r, req);
			return;
		}

		// Check to see if a matching callID already exists in the
		// registrationService. Create another Registration object and attempt
		// to retrieve a matching record from the registrationService.
		Registration oldR = registrationService.getByCallID(r.getCallID());

		// If a matching callID is found but the CSeq is greater than what was
		// just received the new request is obsolete and should be ignored.
		if (oldR != null && oldR.getCseq() >= r.getCseq()) {
			trace("\n doRegister: CSeq indicates request is old. Ignore!");
			return;
		}

		// If the new REGISTER request expiration is less than or equal to the
		// current time delete the registration, remove the callID from the
		// registrationService and invalidate the application session.
		if (r.getExpiration() <= System.currentTimeMillis()) {
			trace("\n doRegister: Expires value set to unregister.");
			// delete registration
			if (oldR != null)
				registrationService.removeByCallID(r.getCallID());
			sendSuccess(req, r.getUri());
			req.getApplicationSession().invalidate();
			return;
		}

		// If a matching callId has not been found in the registryService, add
		// it if it has been found BUT the new request is more current, update
		// the callID record in the registryService.
		if (oldR == null) {
			// add registration
			trace("\n doRegister: Add registration");
			registrationService.add(r);
		} else {
			// update registration
			trace("\n doRegister: Update registration");
			oldR.setExpiration(r.getExpiration());
			oldR.setContacts(r.getContacts());
		}

		// Send a success response to the registering user agent
		sendSuccess(req, r.getUri());
	}

	/**
	 * Wildcards (*) in a contact header signify registrations that should be
	 * removed.
	 * 
	 * @param r
	 *            Registration
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws IOException
	 */
	private void doWildcardRegister(Registration r, SipServletRequest req)
			throws IOException {

		// If matching any of the following conditions, the request is deemed
		// illegal.

		// Check if there are any other contacts in the request other than the
		// wildcard. When a wildcard is sent it must be sent without any other
		// contacts. Also check if the expiration on the Registration is over
		// the current system time. If either of these conditions is true,
		// send a 400 response and invalidate the application session.
		if (r.getContacts().size() > 1
				|| r.getExpiration() > System.currentTimeMillis()) {
			trace("\n REGISTER: illegal request. wildcard is used with other contacts,"
					+ " or expires != 0 is specified. aor=[" + r.getUri() + "]");
			createResponse(req, 400).send();
			req.getApplicationSession().invalidate();
			return;
		}

		// Check for a matching CallID in the registration service
		// by using the removeByCallID method. This method will return
		// the object before it removes it from the registry.
		//
		// If one is found and the old Cseq is greater than the new request
		// this REGISTER requests is out of sequence and the new REGISTER
		// should be put back in the registry.
		//
		// This is a RARE condition. An alternate way of dealing with this would
		// be to validate the CSeq and remove it the entry if it were invalid.
		// But a consideration in that case is two trips to the backing store
		// that contains the registrations. This method only requires one.
		Registration oldR = registrationService.removeByCallID(r.getCallID());
		if (oldR != null && oldR.getCseq() > r.getCseq()) {
			// Request was out of sequence. Place the registration back in the
			// registrationService
			registrationService.add(oldR);
			return;
		}

		// Send a success message back to the calling user agent and then
		// invalidate the application session.
		sendSuccess(req, r.getUri());
		req.getApplicationSession().invalidate();
	}

	/**
	 * Sends a 2XX response back to a user agent signifying an OK.
	 * 
	 * @param req
	 *            SipServletRequest
	 * @param uri
	 *            SipURI
	 * 
	 * @return void
	 * 
	 * @throws IOException
	 */
	private void sendSuccess(SipServletRequest req, SipURI uri)
			throws IOException {
		SipServletResponse res = createResponse(req, SipServletResponse.SC_OK);
		Address[] contacts = registrationService.resolve(uri);
		for (int i = 0; i < contacts.length; i++) {
			res.addAddressHeader("Contact", contacts[i], false);
		}

		// Send back an expires field value as RFC 3261 mandates.
		// Server is accepting the value the client is proposing, so
		// no calculations need to be done.
		res.setExpires(req.getExpires());
		res.setHeader("Date", new Date().toString());
		trace(res, "doResponse() - Success");
		res.send();
	}

	/**
	 * Creates a SipServletResponse object to send back respond to a
	 * SipServletRequest
	 * 
	 * @param req
	 *            SipServletRequest
	 * @param code
	 *            int
	 * 
	 * @return SipServletResponse
	 */
	private static SipServletResponse createResponse(SipServletRequest req,
			int code) {
		SipServletResponse res = req.createResponse(code);
		String header = req.getHeader("Event");
		if (header != null)
			res.setHeader("Event", header);
		return res;
	}

	/**
	 * Retrieves a handle to the RegistrationService
	 * 
	 * @param ServletContext
	 *            cs
	 * 
	 * @return RegistrationService
	 */
	public static RegistrationService getService(ServletContext sc) {
		return (RegistrationService) sc.getAttribute(SERVICE_ATTR);
	}

}

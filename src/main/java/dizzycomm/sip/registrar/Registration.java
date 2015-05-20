package dizzycomm.sip.registrar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author BEA Worldwide Education Services - Copyright (c) 2005/2006
 */
public class Registration implements Serializable {

	private static final long serialVersionUID = -6665938727742097045L;

	private final SipURI uri;
	private final String callID;
	private final int cseq;

	private List<Address> contacts;
	private long expiration;

	/**
	 * Object class for creating sip Address object from an inbound REGISTER
	 * request. uri, callID, CSeq, Contacts and Expiration should all be
	 * captured.
	 * 
	 * @param req
	 *            SipServletRequest
	 * @param factory
	 *            SipFactory
	 * 
	 * @throws ServletParseException
	 */
	Registration(SipServletRequest req, SipFactory factory)
			throws ServletParseException {

		uri = canonicalURI((SipURI) req.getTo().getURI(), factory);
		callID = req.getCallId();
		cseq = getCSeq(req);

		// Call the setContacts() method and initialize it with an ArrayList.
		// Get the Expires header from the request and then iterate through all
		// of the contacts by getting the addressHeaders from the request.
		// With each pass create an Address object from each Contact, add it to
		// the contacts List and also get the greater of the xt (expiration) of
		// the request or the Address currently being iterated.
		setContacts(new ArrayList<Address>());
		long xt = req.getExpires();
		
		for (Iterator<Address> i = req.getAddressHeaders("Contact"); i.hasNext();) {
			Address a = (Address) i.next();
			getContacts().add(a);
			// If no Expire value is set in the request, OCCAS sets it to -1.
			// So if neither the "Header Expires" or the "Contact Expires" is
			// set, chapter 10.2.1.1 in RFC3261 stipulates that it is up to the
			// server to choose what this means. This particular code sets the
			// expires value to 3600.
			if (xt < 0 && a.getExpires() < 0) {
				xt = 3600;
			} else {
			xt = Math.max(xt, a.getExpires());
			}
		}

		// Set the expiration by calculating the current time + 1000 * the
		// result of the xt from the Contacts iteration using the setExpiration
		// method.
		setExpiration(System.currentTimeMillis() + 1000 * xt);
	}

	/**
	 * Create a SipURI object in canonical format. format: USER HOST PORT
	 * 
	 * Values must be unescaped before creating the SipURI as mandated by
	 * section 10.3 of RFC 3261
	 * 
	 * @param uri
	 *            SipURI
	 * @param factory
	 *            SipFactory
	 * 
	 * @return SipURI
	 */
	private SipURI canonicalURI(SipURI uri, SipFactory factory) {
		String uUser = StringEscapeUtils.unescapeJava(uri.getUser());
		String uHost = StringEscapeUtils.unescapeJava(uri.getHost());

		SipURI result = factory.createSipURI(uUser, uHost);

		// setPort is not always necessary, but if it is there, set it.
		// This is done seperately becuase factory.createSipURI() does not take
		// port as an argument.
		result.setPort(uri.getPort());

		return result;
	}

	/**
	 * Iterate throught the contact header to detect the presence of any
	 * wildcard values (*).
	 * 
	 * @return boolean
	 */
	boolean hasWildcard() {
		for (Iterator<Address> i = getContacts().iterator(); i.hasNext();) {
			if (((Address) i.next()).isWildcard())
				return true;
		}
		return false;
	}

	/**
	 * Return the CSeq header from a Registration request.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return int
	 */
	private static int getCSeq(SipServletRequest req) {
		String cseq = req.getHeader("CSeq");
		int i = cseq.indexOf(" ");
		int cSeqNum;

		if (i == -1) {
			cSeqNum = Integer.parseInt(cseq);
		} else {
			cSeqNum = Integer.parseInt(cseq.substring(0, i));
		}

		return cSeqNum;
	}

	/**
	 * Utility method to quickly return a single string that contains the URI,
	 * Contacts and Expiration of the current REGISTER request.
	 * 
	 * @return String
	 */
	public String toString() {
		return "uri=" + getUri() + " contacts=" + getContacts() + " expires="
				+ new Date(getExpiration());
	}

	// Getter/Setter Methods

	/**
	 * Returns the current REGISTER URI
	 * 
	 * @return SipURI
	 * @uml.property name="uri"
	 */
	public SipURI getUri() {
		return uri;
	}

	/**
	 * Returns the current REGISTER CallID field.
	 * 
	 * @return String
	 * @uml.property name="callID"
	 */
	public String getCallID() {
		return callID;
	}

	/**
	 * Returns the current REGISTER CSeq field.
	 * 
	 * @return int
	 * @uml.property name="cseq"
	 */
	public int getCseq() {
		return cseq;
	}

	/**
	 * Returns the current REGISTER Contacts in a List.
	 * 
	 * @return List
	 * @uml.property name="contacts"
	 */
	public List<Address> getContacts() {
		return contacts;
	}

	/**
	 * Replaces the contacts of the REGISTER request with a new List of contacts
	 * 
	 * @param contacts
	 *            List
	 * @return void
	 * @uml.property name="contacts"
	 */
	public void setContacts(List<Address> contacts) {
		this.contacts = contacts;
	}

	/**
	 * Returns the Expiration field of the current REGISTER request.
	 * 
	 * @return long
	 * @uml.property name="expiration"
	 */
	public long getExpiration() {
		return expiration;
	}

	/**
	 * Replaces the Expiration field of the current REGISTER request with a new
	 * value.
	 * 
	 * @param expiration
	 *            long
	 * @return void
	 * @uml.property name="expiration"
	 */
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}
}

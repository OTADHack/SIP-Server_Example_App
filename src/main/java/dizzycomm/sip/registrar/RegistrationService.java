package dizzycomm.sip.registrar;

/**
 * @author BEA Worldwide Education Services - Copyright (c) 2005/2006
 */

import javax.servlet.sip.SipURI;
import javax.servlet.sip.Address;

import java.util.Map;

/**
 * An interface for any RegistrationServices that are implemented for this
 * Registrar.
 * 
 */
public interface RegistrationService {

	/**
	 * 
	 * @param callID
	 * @return Registration
	 */
	public Registration getByCallID(String callID);

	/**
	 * 
	 * @param callID
	 * @return Registration
	 */
	public Registration removeByCallID(String callID);

	/**
	 * 
	 * @param registration
	 * @return
	 */
	public void add(Registration registration);

	/**
	 * 
	 * @param uri
	 * @return Address[]
	 */
	public Address[] resolve(SipURI uri);

	/**
	 * 
	 * @return Map
	 */
	public Map<SipURI, Registration> getAllRegistrations();

	/**
	 * 
	 * @return
	 */
	void init();
}

package dizzycomm.sip.registrar;

/**
 * @author Oracle  - Copyright (c) 2011
 */

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;

/*
 * This class implements an in-memory registrar only suited for single server use
 * and only for demonstration. There is no failover or persistence of the registry
 * information. 
 * 
 * Registration information is stored via a TreeMap for sorting byUri, useful for very
 * quick lookups to resolve an address for services like Location.
 * 
 * A Hashmap contains the registrations sorted by callID. Which is needed for checking 
 * for new REGISTER requests that match ones stored here since all REGISTER requests
 * from a UA should be identical.
 */
final class LocalRegistrationService implements RegistrationService {

	/**
	 * Is implemented because RegistrationService Interface requires it. Nothing
	 * is done in this implementation
	 * 
	 * @return void
	 */
	public void init() {
	}
	
	// CallIDs will be stored in a HashMap().
	private final HashMap<String, Registration> byCallID = new HashMap<String, Registration>();

	/**
	 * All URIs are stored in a TreeMap object called byUri, which is built in
	 * conjunction with a Comparator to allow sorting of the incoming URI
	 * components.
	 * 
	 * Comparisons are made on the different parts of the URI, namely the User,
	 * the Host and the Port. If the two versions of host or user do not match
	 * the comparator will return the difference as either a positive or
	 * negative int. If they match, the return is 0 and the comparator continues
	 * to check until it reaches the port in which case it just returns URI1's
	 * port - URI2's port.
	 * 
	 * TreeMap is used because it is a very fast an efficient collection. If
	 * there were hundreds or thousands of URIs registered, speed in the
	 * Registrar queries would be highly important.
	 * 
	 * The logic for the comparator: Two SipURI objects, The first of which is
	 * the new one to be added. The second is SipURI that is already in the
	 * TreeMap. This comparison will iterate through all SipURIs stored in the
	 * TreeMap until the check is complete. This is why speed is essential.
	 * 
	 * 1. get diff between u1 host and u2 host, if they don't match, return the
	 * difference as an int.
	 * 
	 * 2. get diff between u1 user and u2 user, if no match, return the
	 * difference as an int.
	 * 
	 * 3. If both of the previous checks pass, just return the difference of u1
	 * port - u2 port as an int.
	 */
	private final TreeMap<SipURI, Registration> byUri = new TreeMap<SipURI, Registration>(
			new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			SipURI u1 = (SipURI) o1;
			SipURI u2 = (SipURI) o2;
			
			int diff = u1.getHost().compareTo(u2.getHost());
			if (diff != 0){
				return diff;
			}
			
			diff = u1.getUser().compareTo(u2.getUser());
			if (diff != 0) {
				return diff;
			} else {
				return u1.getPort() - u2.getPort();
			}
			
		}
	});

	/**
	 * Creates a Sip Address object array from SipUri. Iterate through the byUri
	 * TreeMap and compare the uri against those in the TreeMap.
	 * 
	 * Method is synchronized TreeMap is not an access to byUri must be kept
	 * synchronized.
	 * 
	 * @param uri
	 *            SipURI
	 * 
	 * @return Address[]
	 */
	public synchronized Address[] resolve(SipURI uri) {
		ArrayList <Address> aList = new ArrayList<Address>();
		Iterator<Registration> i = byUri.tailMap(uri).values().iterator();
		long now = System.currentTimeMillis();
		while (i.hasNext()) {
			Registration reg = (Registration) i.next();
			if (!reg.getUri().equals(uri))
				break; 
			if (reg.getExpiration() < now) {
				i.remove();
				byCallID.remove(reg.getCallID());
				continue;
			}
			for (Iterator<Address> j = reg.getContacts().iterator(); j.hasNext();) {
				Address address = (Address) j.next();
				address.setExpires((int) (reg.getExpiration() - now) / 1000);
				aList.add(address);
			}
		}
		return (Address[]) aList.toArray(new Address[aList.size()]);
	}

	/**
	 * Getter method that returns the all registrations in the byUri Map
	 * 
	 * @return byUri Map 
	 */
	public Map<SipURI, Registration> getAllRegistrations() {
		return byUri;
	}

	/**
	 * Adds a new Registration to the registrar listings of both the byCallID
	 * HashMap and the byUri TreeMap.
	 * 
	 * @param reg
	 *            Registration
	 * 
	 * @return void
	 */
	public synchronized void add(Registration reg) {
		byCallID.put(reg.getCallID(), reg);
		byUri.put(reg.getUri(), reg);
	}

	/**
	 * Retrieve a Registration using the callID as the key.
	 * 
	 * @param callID
	 *            String
	 * 
	 * @return Registration
	 */
	public Registration getByCallID(String callID) {
		return (Registration) byCallID.get(callID);
	}

	/**
	 * Remove the callID passed to this method from the byCallID HashMap.
	 * HashMap.remove() will pull the object out if it is present. If it's
	 * found, pass it back to the method that called this.
	 * 
	 * Also, if it is found, remove the associated URI from the byUri TreeMap.
	 * 
	 * @param callID
	 *            String
	 * 
	 * @return Registration
	 */
	public Registration removeByCallID(String callID) {
		Registration reg = (Registration) byCallID.remove(callID);
		if (reg == null)
			return null;

		Collection<Registration> contacts = byUri.tailMap(reg.getUri()).values();
		for (Iterator<Registration> i = contacts.iterator(); i.hasNext();) {
			if (reg == i.next()) {
				i.remove();
				break;
			}
		}
		return reg;
	}
}

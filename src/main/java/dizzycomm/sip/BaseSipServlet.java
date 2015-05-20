package dizzycomm.sip;

/**
 * @author BEA Worldwide Education Services - Copyright (c) 2005/2006
 */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.ServletException;

import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

/**
 * This class provides utility and tracing methods that other SIP Servlets in
 * the application will find useful. Message tracing can also be set in the SIP
 * Console, but this using these tracing mechanisms you can watch each
 * request/response as it occurs on the WebLogic Server process window.
 */
public abstract class BaseSipServlet extends SipServlet {
	private static final long serialVersionUID = -8030290305522345063L;
	private String msgTrace;
	private final static SimpleDateFormat df = new SimpleDateFormat(
			"MM/dd HH:mm:ss.SSS ");

	/**
	 * Checks for a context parameter in the sip.xml called 'message_trace' that
	 * will activate tracing for a given servlet. Example syntax for the sip.xml
	 * line is:
	 * 
	 * <context-param>
	 * <param-name>dizzycomm.sip.PACKAGENAME.SERVLET_CLASSNAME.message_trace
	 * </param-name> <param-value>all</param-value> </context-param>
	 * 
	 * Valid param-values are none, all, request, response and msg-only.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		msgTrace = getContextParam("message_trace");
		if (msgTrace == null) {
			msgTrace = "";
		} else {
			trace("message_trace=" + msgTrace);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.bea.wcp.sip.WlssSipServlet#doRequest(javax.servlet.sip.SipServletRequest)
	 *      Catch ALL requests
	 */
	protected void doRequest(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doRequest()");
		super.doRequest(req);
	}

	/**
	 * Slight modification to doResponse() that will show trace of response if
	 * enabled
	 * 
	 * @param res
	 *            SipServletResponse
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doResponse(SipServletResponse res) throws ServletException,
			IOException {
		trace(res, "doResponse()");
		super.doResponse(res);
	}

	/**
	 * Slight modification to doErrorResponse() that will show trace of response
	 * if enabled
	 * 
	 * @param res
	 *            SipServletResponse
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doErrorResponse(SipServletResponse res)
			throws ServletException, IOException {
		trace(res, "doErrorResponse()");
		super.doErrorResponse(res);
	}

	/**
	 * Will show trace of all REGISTER requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doRegister(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doRegister()");
		super.doRegister(req);
	}

	/**
	 * Will show trace of all INVITE requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doInvite(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doInvite()");
		super.doInvite(req);
	}

	/**
	 * Will show tracing for all ACK responses that are sent between UAs or UASs
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doAck()");
		super.doAck(req);
	}

	/**
	 * Will show trace of all MESSAGE requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doMessage(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doMessage()");
		super.doMessage(req);
	}

	/**
	 * Will show trace of all SUBSCRIBE requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doSubscribe(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doSubscribe()");
		super.doMessage(req);
	}

	/**
	 * Will show trace of all PUBLISH requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doPublish(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doPublish()");
		super.doPublish(req);
	}

	/**
	 * Will show trace of all NOTIFY requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doNotify(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doNotify()");
		super.doNotify(req);
	}

	/**
	 * Will show trace of all OPTIONS requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doOptions(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doOptions()");
		super.doOptions(req);
	}

	/**
	 * Will show trace of all INFO requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doInfo(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doInfo()");
		super.doInfo(req);
	}

	/**
	 * Will show trace of all REFER requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doRefer(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doRefer()");
		super.doRefer(req);
	}

	/**
	 * Will show trace of all UPDATE requests if tracing is enabled.
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 * 
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doUpdate(SipServletRequest req) throws ServletException,
			IOException {
		trace(req, "doUpdate()");
		super.doUpdate(req);
	}

	/**
	 * Retrieve context-param <param_name> from web.xml
	 * 
	 * @param paramName
	 *            String
	 * 
	 * @return param-value or null
	 */
	protected String getAppContextParam(String paramName) {
		return getServletContext().getInitParameter(paramName);
	}

	/**
	 * Retrieve context-param <ClassName.param_name> from sip.xml
	 * 
	 * @param paramName
	 *            String
	 * 
	 * @return param-value or null
	 */
	protected String getContextParam(String paramName) {
		return getServletContext().getInitParameter(
				getClass().getName() + "." + paramName);
	}

	/**
	 * Retrieves the SipFactory Object which is stored in the Servlet Contect
	 * attribute
	 * 
	 * @return SipFactory
	 */
	public SipFactory getSipFactory() {
		return (SipFactory) getServletContext().getAttribute(
				SipServlet.SIP_FACTORY);
	}

	/**
	 * Utility class to retrieve the initial JNDI context.
	 * 
	 * @return Context
	 * 
	 * @throws ServletException
	 */
	protected Context getInitialContext() throws NamingException {
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY,
					"weblogic.jndi.WLInitialContextFactory");

			return new InitialContext(env);

		} catch (NamingException ne) {
			System.out
					.println("ERROR: Unable to get a connection to the WebLogic Server");
			throw ne;
		}
	}

	/**
	 * Trace Methods. These are all variations that will handle the appropriate
	 * object type.
	 */

	/**
	 * 
	 * @param req
	 *            SipServletRequest
	 * 
	 * @return void
	 */
	public void trace(SipServletRequest req) {
		trace(req, "Request");
	}

	/**
	 * 
	 * @param req
	 *            SipServletRequest
	 * @param String
	 *            text
	 * 
	 * @return void
	 */
	public void trace(SipServletRequest req, String text) {
		if (msgTrace.equals("all") || msgTrace.equals("request")) {
			System.out.println("<<<< " + req.getRemoteAddr() + ":"
					+ req.getRemotePort());
			System.out.println(df.format(new Date()) + getClass().getName()
					+ " - " + text);
			System.out.println(getMsgText(req));
			log(getClass().getName() + " - " + text);
			log(getMsgText(req));
		}
	}

	/**
	 * 
	 * @param res
	 *            SipServletResponse
	 * 
	 * @return void
	 */
	public void trace(SipServletResponse res) {
		trace(res, "Response");
	}

	/**
	 * 
	 * @param res
	 *            SipServletResponse
	 * @param text
	 *            String
	 * 
	 * @return void
	 */
	public void trace(SipServletResponse res, String text) {
		if (msgTrace.equals("all") || msgTrace.equals("response")) {
			System.out.println(">>>>>" + this.getClass().getName());
			System.out.println(">>>> " + res.getRemoteAddr() + ":"
					+ res.getRemotePort());
			System.out.println(df.format(new Date()) + getClass().getName()
					+ " - " + text);
			System.out.println(getMsgText(res));
			log(getClass().getName() + " - " + text);
			log(getMsgText(res));
		}
	}

	/**
	 * 
	 * @param text
	 *            String
	 * 
	 * @return void
	 */
	public void trace(String text) {
		if (msgTrace.equals("all") || msgTrace.equals("msg_only")) {
			System.out.println(df.format(new Date()) + getClass().getName()
					+ " - " + text);
			log(getClass().getName() + " - " + text);
		}
	}

	/**
	 * Method that creates a SipURI with all attribute information (parameter
	 * and header) removed. This can be used for SIP URI normalization. URI
	 * specified by an argument is not modified.
	 * 
	 * @param uri
	 *            Target SIP URI
	 * @return SipURI with all attribute information removed.
	 */
	public SipURI getPlainURI(SipURI uri) {
		SipURI uri2 = getSipFactory()
				.createSipURI(uri.getUser(), uri.getHost());

		int port = uri.getPort();
		if (port > 0)
			uri2.setPort(port);

		return uri2;
	}

	// --- The following are static helper methods.

	/**
	 * Method that parses the numeric value part of the CSeq.
	 * 
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return int - Numeric value part of the CSeq
	 */
	public static int getCSeq(SipServletMessage msg) {
		String cseq = msg.getHeader("CSeq");
		int i = cseq.indexOf(" ");
		return Integer.parseInt(i == -1 ? cseq : cseq.substring(0, i));
	}

	/**
	 * Method that retrieves the method part of the CSeq
	 * 
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return String - Contains the Method of the CSeq
	 */
	public static String getCSeqMethod(SipServletMessage msg) {
		String cseq = msg.getHeader("CSeq");
		int i = cseq.indexOf(" ");

		return i == -1 ? "" : cseq.substring(cseq.indexOf(" ")).trim();
	}

	/**
	 * Method that returnes the message body
	 * 
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return String - Message Content
	 */
	public static String getContent(SipServletMessage msg) throws IOException {
		byte[] raw = msg.getRawContent();
		return (raw == null ? "" : new String(msg.getRawContent()));
	}

	/**
	 * Method that creates simple information about the SIP message. Useful when
	 * creating a log for debugging.
	 * 
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return String - Combined string "method, request URI, From, To header"
	 *         for SIP requests, combined string
	 *         "status code, method name, From, To header" for SIP responses
	 */
	public static String getShortInfo(SipServletMessage msg) {
		if (msg instanceof SipServletRequest) {
			SipServletRequest req = (SipServletRequest) msg;
			return "request=[" + req.getMethod() + " " + req.getRequestURI()
					+ "], from=[" + req.getFrom() + "], to=[" + req.getTo()
					+ "]";
		} else {
			SipServletResponse res = (SipServletResponse) msg;
			return "status=[" + res.getStatus() + " " + res.getReasonPhrase()
					+ "], method=[" + res.getMethod() + "], from=["
					+ res.getFrom() + "], to=[" + res.getTo() + "]";
		}
	}

	/**
	 * Appends additional headers onto SipServletMessage
	 * 
	 * @param sb
	 *            StringBuffer
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return void
	 */
	private static void appendHeaders(StringBuffer sb, SipServletMessage msg) {
		Iterator<String> iter = msg.getHeaderNames();
		while (iter.hasNext()) {
			String header = (String) iter.next();
			ListIterator<String> iter2 = msg.getHeaders(header);

			while (iter2.hasNext()) {
				sb.append(header).append(": ").append(iter2.next())
						.append("\r\n");
			}
		}
	}

	/**
	 * Converts a SipServletMessage Object into a String.
	 * 
	 * @param msg
	 *            SipServletMessage
	 * 
	 * @return String
	 */
	public static String getMsgText(SipServletMessage msg) {
		StringBuffer sb = new StringBuffer(800);
		if (msg instanceof SipServletRequest) {
			SipServletRequest req = (SipServletRequest) msg;
			sb.append(req.getMethod() + " " + req.getRequestURI() + "\r\n");
		} else {
			SipServletResponse res = (SipServletResponse) msg;
			sb.append(res.getStatus() + " " + res.getReasonPhrase() + "\r\n");
		}
		appendHeaders(sb, msg);
		sb.append("\r\n");
		try {
			sb.append(getContent(msg));
		} catch (IOException ex) {
		}

		return sb.toString();
	}

}

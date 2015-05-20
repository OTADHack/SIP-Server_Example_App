<%@ page import="dizzycomm.sip.callcontrol.CallController"%>
<%@ page session="false"%>
<%
	HttpSession sess = request.getSession();
	if (sess == null) {
		out.println("Error: http session was not found!");
		// This example may not work well when cookies are enabled in the browser,
		// since it is relying on the encoded sessionid in the URL to take effect.
		// Try turning them off in your browser. Also restart the browser to kill
		// existing cookies.
		return;
	}
	out.println("<br>Received request to terminate associated call for:" + sess.getId());

	CallController cc = (CallController) application.getAttribute(CallController.CTRL_ATTR_NAME);
	if (cc == null) {
		out.println("error: CallController not found! Please redeploy the webapp");
		return;
	}
	cc.terminateCall(sess);

	String contextPath = request.getContextPath();
	response.sendRedirect(response.encodeRedirectURL(contextPath + "/testPages/admin.jsp"));
%>

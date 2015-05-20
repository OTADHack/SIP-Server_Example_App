<%@ page import="dizzycomm.sip.callcontrol.CallController"%>
<%@ page session="false"%>

<%
	CallController cc = (CallController) application.getAttribute(CallController.CTRL_ATTR_NAME);
	if (cc == null) {
		out.println("error: CallController not found! Please redeploy the webapp");
		return;
	}
	cc.terminateAll();
	
	String contextPath = request.getContextPath();
	response.sendRedirect(response.encodeRedirectURL(contextPath + "/testPages/admin.jsp"));
%>

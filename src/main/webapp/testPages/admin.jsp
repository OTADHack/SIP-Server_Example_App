<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ page import="dizzycomm.sip.callcontrol.CallController"%>
<%@ page session="false" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">


<html>
<head><title>DizzyComm SIP Servlet Test Harness</title>
	<link href="/DizzyCommWeb/pages/css/dizzycomm.css" rel="stylesheet" type="text/css"/>
	</head>
<body>
<table border="0" cellpadding="5" cellspacing="0" width="680">
  <tbody>
    <tr>
      <td valign="top" width="50%">
	<%@ include file="/pages/includes/testHeader.jspf" %>
	<h2>DCCallControl Functionality Test</h2>
<p>This page displays a list of active calls that have been registered using the B2BUA Servlet and the Call Control service. </p>

<%
  CallController cc = (CallController) application.getAttribute(CallController.CTRL_ATTR_NAME);
  if (cc == null) {
    out.println("<h2>error: CallController not found! Please redeploy the webapp</h2>");
    return;
  }
  String[] calls = cc.getAllCalls();
%>

<%
  if (calls != null && calls.length > 0) {
    out.println("\nClick on links to terminate calls:\n");
%>

<table border cellspacing='0' cellpadding='3'>


<tr><th>Participants</th><th>Link for Termination</th></tr>

<%
  for (int i=0; calls != null && i < calls.length; i++) {
    out.println("<tr><td>Get participants</td><td><A HREF=\"" + calls[i] + "\">" +
        calls[i] + "</A></td></tr>");
  }
%>
<%
  if (calls != null && calls.length > 0) {
    out.println("<tr><td>ALL CALLS</td><td><A HREF=\"terminateAll.jsp\">Terminate All</A></td></tr>");
  }
%>

</table>
<%

  } else {
    out.println("<b>No calls are in progress right now.</b>");
  }
%>

<p>
  </p><a href="/DizzyCommWeb/testPages/index.jsp">Go to the Index page for the DizzyComm Test Pages</a>

  <br/>
  <br/>
    <hr align="left" width="670"/>
<br/>
<br>
<%@ include file="/pages/includes/footer.jspf" %>
      </td>
    </tr>
</table>


</body>
</html>

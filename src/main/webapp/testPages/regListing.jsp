<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ page
	import="java.util.*,
                 javax.servlet.sip.Address,
                 dizzycomm.sip.registrar.DCRegistrar,
                 dizzycomm.sip.registrar.Registration,
                 dizzycomm.sip.registrar.RegistrationService"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">


<html>
<head>
<title>DizzyComm SIP Servlet Test Harness</title>
<link href="/DizzyCommWeb/pages/css/dizzycomm.css" rel="stylesheet" type="text/css" />
</head>
<body>
<table border="0" cellpadding="5" cellspacing="0" width="680">
	<tbody>
		<tr>
			<td valign="top" width="50%"><%@ include
				file="/pages/includes/testHeader.jspf"%>
			<h2>DCRegistrar Functionality Test</h2>
			<p>This page displays a list of addresses that have been
			registered using the DCRegistrar Servlet and Registration Services.</p>
			<table border cellspacing='0' cellpadding='3'>
				<tr>
					<th>Registered Address</th>
					<th>Contacts</th>
					<th>Expiration</th>
				</tr>

				<!-- Access DCRegistrar and use one of its static methods to get all registrations. -->
				<%
				RegistrationService regService = DCRegistrar.getService(application);
				Iterator <Registration> i = regService.getAllRegistrations().values().iterator();
				%>

				<!--  Iterate through the registrations, and with each iteration, create a new table row, with table -->
				<!--  data cells to match the headers above.                                                         -->
				<!--  For the contacts, you will have to iterate through multiple possible values because each       -->
				<!--  registration can have multiple addresses-of-record associated with it.                         -->
				<%
				while (i.hasNext()) {
					Registration r = i.next();
  				%>
				<tr>
					<td><%=r.getUri()%></td>
					<td>
					<%
					    String sep = "";
					    for (Iterator <Address> j = r.getContacts().iterator(); j.hasNext(); ) {
					      Address address = (Address) j.next();
					      StringBuffer buf = new StringBuffer();
					      buf.append(sep);
					      if (address.getDisplayName() != null) {
					        buf.append("\"").append(address.getDisplayName()).append("\"");
					      }
					      buf.append(" &lt ").append(address.getURI());
					      buf.append(" &gt");
					      out.print(buf.toString());
					      sep = ", ";
					    }
					%>
					</td>
					<td><%=new Date(r.getExpiration())%></td>
				</tr>
				<% } %>

			</table>
			<p></p>
			<a href="/DizzyCommWeb/testPages/index.jsp">Go to the Index page for the
			DizzyComm Test Pages</a> <br />
			Initiate a Conference Call <br />
			<br />
			<hr align="left" width="670" />
			<br />
			<br>
			<%@ include file="/pages/includes/footer.jspf"%>
			</td>
		</tr>
</table>
</body>
</html>

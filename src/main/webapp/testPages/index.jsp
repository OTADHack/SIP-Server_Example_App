<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
<title>DizzyComm SIP Servlet Test Harness (JSP-only)</title>
<link href="/DizzyCommWeb/pages/css/dizzycomm.css" rel="stylesheet" type="text/css" />
</head>
<body>
<!-- header table begin -->
<!-- header table end -->
<table border="0" cellpadding="5" cellspacing="0" width="680">
	<tbody>
		<tr>
			<td valign="top" width="50%"><%@ include
				file="../pages/includes/testHeader.jspf"%>
			<h3>SIP Servlets Test Pages</h3>
			<p><b>DizzyComm WebLogic SIP Server Test</b></p>
			<ul>
				<li>These JSPs allow you to test the registration and
				conference call capabilities of the DizzyComm SIP Servlets:
				<ul>
					<li><a href="/DizzyCommWeb/testPages/regListing.jsp">List all
					registered users</a></li>
					<!--  <li>Initiate a Conference Call</li>  -->
					<li><a href="/DizzyCommWeb/testPages/admin.jsp">List all
					active calls</a></li>
				</ul>
			</ul>
			<br />
			<hr align="left" width="670" />
			<br>
			</td>
		</tr>
	</tbody>
</table>
<p><%@ include file="../pages/includes/footer.jspf"%>
<!-- FOOTER --></p>
</body>
</html>

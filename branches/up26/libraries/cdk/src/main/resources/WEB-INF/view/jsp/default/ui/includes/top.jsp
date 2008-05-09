<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<title>Academus</title>
		<meta http-equiv="Content-Type"	content="application/xhtml+xml; charset=UTF-8" />
		<meta name="keywords" content="uPortal, Unicon Academus, Unicon" />
		<link rel="stylesheet" href="/portal/media/org/jasig/cas/css/cas.css" type="text/css" media="all" />
	</head>
	<body>
		<div id="casContainer">
			<div id="cas">
				<div id="header">
					<div id="mainLogo"><a href="<spring:message code="screen.logo.url" />"><span></span></a> </div>
				</div>
				
				<%-- This file retained during the removal of CAS server from Toro
				because it is used by the error page JSP. --%>
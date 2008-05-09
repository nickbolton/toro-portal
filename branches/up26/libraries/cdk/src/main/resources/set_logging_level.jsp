<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.apache.log4j.Level" %>
<html>
<head>
<title>Set Logging Level</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<%
    String loggerName = request.getParameter("loggerName");
    String logLevel = request.getParameter("logLevel");

    Logger logger = null;
    if (loggerName != null && !"".equals(loggerName)) {
        logger = Logger.getLogger(loggerName);
    } else {
        logger = Logger.getRootLogger();
    }

    Level level = Level.toLevel(logLevel);
    logger.setLevel(level);
%>

<body>
    <p>Logger <%=(loggerName != null ? loggerName : "RootCategory")%> set to <%=level.toString()%></p>
</body>
</html>

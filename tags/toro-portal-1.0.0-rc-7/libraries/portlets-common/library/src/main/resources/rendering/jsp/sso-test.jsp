<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Single Sign-On Test Page</title>
    </head>
    <body>
        <h1>Single Sign-On Test</h1>

<%
    java.util.Map pmap = request.getParameterMap();
    if (!pmap.isEmpty()) {
%>
        <h2>The following parameters were successfully passed:</h2>
        <table border="1">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Value(s)</th>
                </tr>
            </thead>
            <tbody>
<%
        java.util.Iterator it = pmap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            String[] values = request.getParameterValues(key);
            if (values == null || values.length == 0)
                values = new String[] { "<null>" };
%>
                <tr>
                    <td><%= key %></td>
                    <td>
                        <ul><%
                for (int i = 0; i < values.length; i++) {
                    %><li><%= values[i] %></li><%
                }
                        %></ul></td>
                </tr>
<%
        }
%>
            </tbody>
        </table>
<%
    } else {
%>
        <h2>ERROR: No parameters were given.</h2>
<%
    }
%>
    </body>
</html>

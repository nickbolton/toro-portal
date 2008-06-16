<html>
<head>
    <title></title>
</head>
<body>
    <iframe name="ssologinframe" frameborder="no" width="100%" height="100%"></iframe>
    <form target="ssologinframe" action="<%=request.getParameter("EntryUrl")%>" method="post" id="frmMain" name="frmMain">

<%
    String paramsDelim = request.getParameter("param_list");
    if (paramsDelim != null) {
        String[] paramKeys = paramsDelim.split(",");
        StringBuffer result = new StringBuffer("");
        String key = null;
        String value = null;
        for (int i = 0; i < paramKeys.length; i++) {
            key = paramKeys[i];
            if (key != null) {
                value = request.getParameter(key);
                value = (value != null)? value:"";

                result.append("<input type=\"hidden\"");
                result.append(" name=\"").append(key).append("\"");
                result.append(" id=\"").append(key).append("\"");
                result.append(" value=\"").append(value).append("\"");
                result.append("></input>");
            }
        }
        out.println(result.toString());
    }
%>

    </form>
    <script language="JavaScript">document.frmMain.submit();</script>
</body>
</html>

<html>
	<head>
        <title>Academus Portal</title>
        <link href="/favicon.ico" rel="icon" />
        <link href="/favicon.ico" rel="shortcut icon" />
    </head>

    <frameset rows="60,*" border="0" frameborder="0" frameborder="no" framespacing="0">
	   	<frame src="html/help/helpBanner.htm" name="header" marginwidth="0" marginheight="0" scrolling="no" /><!-- NAME = "Header" -->
        <frame src="<%= request.getParameter( "helpUrl" ) %>" name="main" marginwidth="0" marginheight="0" /><!-- NAME = "Main" -->
	</frameset>
	
</html>


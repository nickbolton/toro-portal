<%@ page session="true" errorPage="error.jsp" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page import="org.jasig.portal.security.*" %>
<%@ page import="org.jasig.portal.security.provider.*" %>
<%@ page import="org.jasig.portal.services.*" %>
<%@ page import="org.jasig.portal.groups.*" %>
<%@ page import="net.unicon.portal.groups.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="javax.naming.*" %>
<%!
private static String fixNull(Object o) {
  return (o == null ? "&nbsp;" : (String)o);
}
  static java.text.NumberFormat s_intFormatter = java.text.NumberFormat.getNumberInstance ();
  static long s_firstInvoked = System.currentTimeMillis ();
  static java.text.DateFormat s_dateFormatter;

  static
  {
    java.util.TimeZone tz = java.util.TimeZone.getDefault ();
//    java.util.TimeZone tz = java.util.TimeZone.getTimeZone ("America/Los_Angeles");
//    java.util.TimeZone tz = java.util.TimeZone.getTimeZone ("Australia/Melbourne");

//    s_dateFormatter = java.text.DateFormat.getDateTimeInstance ();
    s_dateFormatter = java.text.DateFormat.getDateTimeInstance (java.text.DateFormat.LONG, java.text.DateFormat.LONG, new Locale ("en", "AU"));
    s_dateFormatter.setTimeZone (tz);
  }

  public static String getTotalMemory ()
  {
    String totalMemoryString = s_intFormatter.format (Runtime.getRuntime ().totalMemory ());
    return totalMemoryString;
  }

  public static String getFreeMemory ()
  {
    String freeMemoryString = s_intFormatter.format (Runtime.getRuntime ().freeMemory ());
    return freeMemoryString;
  }

  public static String getDate (long lTime)
  {
    java.util.Date timeNow = new java.util.Date (lTime);
    String dateString = s_dateFormatter.format (timeNow);
    return dateString;
  }

  public int getThreadCountInGroup (ThreadGroup threadGroup)
  {
//    System.out.println ("Enumerating threads in group \"" + threadGroup.getName () + "\"");
    return threadGroup.activeCount ();
  }
  
  public String getThreadCount (boolean bVerbose)
  {
    int threadCount = 0;
    StringBuffer sb = new StringBuffer (10240);
    sb.append ("<br>");

    try
    {
      ThreadGroup threadGroup = Thread.currentThread ().getThreadGroup ();
      ThreadGroup parentThread = threadGroup.getParent ();

      // First, find the top group
      while (parentThread != null)
      {
        threadGroup = parentThread;
        parentThread = threadGroup.getParent ();
      }

      // Now, we can enumerate all threads in one move
      int threadGroupCount = threadGroup.activeCount ();
      ThreadGroup[] threadGroupList = new ThreadGroup[threadGroupCount];
      threadGroupCount = threadGroup.enumerate (threadGroupList, true);

      int threadCountInGroup = getThreadCountInGroup (threadGroupList[0]);
      Thread[] threads = new Thread[threadCountInGroup];
      threadCountInGroup = threadGroupList[0].enumerate (threads);

      if (bVerbose)
      {
        sb.append ("Thread details:<br><pre>");

        for (int j = 0;j < threadCountInGroup;j++)
        {
          String threadName = threads[j].getThreadGroup ().getName () + ':' + threads[j].getName ();

          if (threadName == null)
            threadName = "unnamed";

          sb.append (threadName + "\n");
        }
        sb.append ("</pre>");
      }
      threadCount += threadCountInGroup;
    }
    catch (Exception ex)
    {
      ex.printStackTrace ();
    }
    String threadCountString = s_intFormatter.format (threadCount);
    sb.append ("<b>" + threadCountString + "</b>");
    return sb.toString ();
  }
%>
<%
    // Enforce strict authorization: Only authenticated members of the 'Portal Administrators' group may use this page.
    IPerson p = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
    boolean cont = false;
    if (p == null || !p.getSecurityContext().isAuthenticated()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?dest="+request.getRequestURI());
    } else {
        // Check if they are a Portal Administrator
        UniconGroupService groupService = UniconGroupServiceFactory.getService();
        IGroup group = groupService.getGroupByPath(new String[] {/*"Everyone",*/"Staff","Officers","Portal Administrators"});
        if (group != null && group.contains(MemberFactory.getMember((String)p.getAttribute(IPerson.USERNAME)))) {
            cont = true;
        }
    }

    if (!cont) {
%>
<html>
<body>
Permission Denied. Only Portal Administrators are permitted to view this page.
</body>
</html>
<%
    } else {
        // Authorized, continue as planned.
%>

<html>

<%
  String verbFlag = request.getParameter ("VerboseThreads");
  boolean bVerboseThreads, bVerboseUsers;

  if (verbFlag != null && verbFlag.equals ("true"))
  {
    bVerboseThreads = true;
  }
  else
    bVerboseThreads = false;

  verbFlag = request.getParameter ("VerboseUsers");

  if (verbFlag != null && verbFlag.equals ("true"))
  {
    bVerboseUsers = true;
  }
  else
    bVerboseUsers = false;
%>

<head>
<title>uportal.biz Server Statistics</title>
<meta HTTP-EQUIV="Refresh" Content="120; URL=userscount.jsp?VerboseThreads=<%= bVerboseThreads?"true":"false" %>">

<script>

var verboseThreads = <%= bVerboseThreads?"true;":"false;" %>
var verboseUsers = <%= bVerboseUsers?"true;":"false;" %>

function toggleVerboseThreads ()
{
  verboseThreads = document.TogglesForm.VerboseThreads.checked;
  reloadWindow ();
}

function toggleVerboseUsers ()
{
  verboseUsers = document.TogglesForm.VerboseUsers.checked;
  reloadWindow ();
}

function reloadWindow ()
{
  var vThreads, vUsers;

  if (verboseThreads == true)
    vThreads = "VerboseThreads=true";
  else
    vThreads = "VerboseThreads=false";

  if (verboseUsers == true)
    vUsers = "VerboseUsers=true";
  else
    vUsers = "VerboseUsers=false";

  window.location="userscount.jsp?" + vThreads + "&" + vUsers;

}

</script>
</head>

<body bgcolor="white">

<form name=TogglesForm>
Display verbose user information <input type=checkbox

<%
  if (bVerboseUsers)
    out.print (" checked ");
%>

name=VerboseUsers value=<%= bVerboseUsers?"true":"false" %> onClick="toggleVerboseUsers()">
<br>
Display verbose thread information <input type=checkbox

<%
  if (bVerboseThreads)
    out.print (" checked ");
%>

name=VerboseThreads value=<%= bVerboseThreads?"true":"false" %> onClick="toggleVerboseThreads()">
</form>
<%
   // Set up the path
    Hashtable environment = new Hashtable(1);
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
    Context context = new InitialContext(environment);
    NamingEnumeration ne = context.listBindings("sessions");
    
    // Initialize counts of guest users and logged-in users
    int numGuestUsers = 0;
    int numLoggedInUsers = 0;

    if (bVerboseUsers)
    {
%>
Users currently logged in to uportal.biz:<br><br>

<table border="1" cellspacing="3" cellpadding="3">
  <tr bgcolor="#CCCCCC">
    <th>Username</th>
    <th>Name</th>
    <th>Email</th>
    <th>Phone</th>
  </tr>
  
<%
      Connection con = RDBMServices.getConnection();
    
      try {      
        while (ne.hasMore()) {
          // Get the user ID
          Binding b = (Binding)ne.next();
          String sessionId = b.getName();
          String userId = (String)b.getObject();   
        
          // Get user name from database
          String sql = "SELECT USER_NAME FROM UP_USER WHERE USER_ID=?";
          RDBMServices.PreparedStatement pstmt = new RDBMServices.PreparedStatement(con, sql);        
          try {
            pstmt.clearParameters();
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            String userName = null;
            try {
              rs.next();
              userName = rs.getString(1);
            } finally {
              if (rs != null)
                rs.close();
            }
          
            // Create a new person object
            IPerson person = new PersonImpl();
            PersonDirectory pd = PersonDirectory.instance();
            pd.getUserDirectoryInformation(userName, person);
    
            out.print("<tr>");
            out.print("<td>" + userName + "</td>");
            out.print("<td>" + fixNull(person.getAttribute("givenName")) + " " + person.getAttribute("sn") + "</td>");
            out.print("<td>" + fixNull(person.getAttribute("mail")) + "</td>");
            out.print("<td>" + fixNull(person.getAttribute("telephoneNumber")) + "</td>");
            out.print("</tr>");
          
          } catch (Exception e) {
            out.print("<tr><td colspan=\"4\">Problem with userId " + userId + ": " + e + "</td></tr>");
          } finally {
            pstmt.close();
          }

          /*
          // Print out all the person attribute information for debugging
          Enumeration e = person.getAttributeNames();
          while (e.hasMoreElements()) {
            String attName = (String)e.nextElement();
            out.print(attName + "=" + person.getAttribute(attName) + "<br/>");
          }
          */
        
          if (userId.equals("1")) // Guest users have ID=1
            numGuestUsers++;
          else
            numLoggedInUsers++;
        }
        ne.close();
      } finally {
        RDBMServices.releaseConnection(con);
      }
    }
    else
    {
      try
      {      
        while (ne.hasMore())
        {
          numLoggedInUsers++;
          ne.next();
        }
      }
      catch (Exception e) {}
    }
    context.close ();
%>

</table>
<br>
<%
  if (bVerboseUsers)
  {
%>
<b><%= numGuestUsers + numLoggedInUsers %></b> total users 
(<b><%= numGuestUsers %></b> guest, <b><%= numLoggedInUsers %></b> logged in)
<%
  }
  else
  {
%>
<b><%= numLoggedInUsers %></b> total users 
<%
  }
%>
<br>
Current time is: <b><%= getDate (System.currentTimeMillis ()) %></b>
<br>
Running since at least: <b><%= getDate (s_firstInvoked) %></b>
<br>
Current timezone is: <b><%= s_dateFormatter.getTimeZone ().getID () %></b>
<br>
<%= getThreadCount (bVerboseThreads) %> active threads
<br>
Total heap size is: <b><%= getTotalMemory () %></b> bytes
<br>
Free memory: <b><%= getFreeMemory () %></b> bytes
</body>
</html>
<% } %>

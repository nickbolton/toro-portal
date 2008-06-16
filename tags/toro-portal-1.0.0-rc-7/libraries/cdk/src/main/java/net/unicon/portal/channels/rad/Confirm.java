/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */


package net.unicon.portal.channels.rad;

import java.util.*;
import java.text.*;

/*
  Eg: use link in xsl file for confirmation such as
  <a href=" {$goURL}=net.unicon.portal.channels.rad.Confirm
  &amp;methodName=delete&amp;text=Normal Text &lt;b&gt;Bold Text&lt;/b&gt;
  &amp;calid={../@calid}">
  <img src="{$baseImagePath}/rad/trash.gif" border="0" title="Delete"/></a>

  <a href='{$goURL}=net.unicon.portal.channels.rad.Confirm&amp;methodName=delete&amp;
           text=2&amp;distribution-id={@distribution-id}&amp;survey-id={../@survey-id}'>
   <img src="{$baseImagePath}/rad/trash.gif" title="Delete Distribution" border='0'/>
  </a>


  Notes: in your Channel.ssl  must add (absolete)
  <?xml-stylesheet title="Confirm" href="net/unicon/portal/channels/rad/confirm.xsl" type="text/xsl" media="explorer"?>

*/

/**
 * The screen allows user to confirm something.
 */
public class Confirm extends Screen {

  static public final String SID = "Confirm";
  String m_methodSID = null;
  String m_methodName = null;
  String m_back = null;
  String m_text = null;
  Hashtable m_okParams = null;

  /**
   * The Confirm screen can be "stateless", i.e, the sid can be a constant.
   * @return the id of Confirm screen.
   */
  public String sid() {
    return SID;
  }

  /**
   * The version of About screen. It is the RAD version.
   * @return the RAD's version
   */
  public String getVersion() {
    return Channel.VERSION;
  }

  /**
   * Called when Confirm screen is being active (second time).
   * @param params the parameters passed when enter to
   * @throws Exception
   */
  public void reinit(Hashtable params) throws Exception {}

  /**
   * Override the base method. Confirm screen can render together with other channels
   * @return true
   */
  public boolean canRenderAsPeephole() // Tien 0405
  {
    return true;
  }

  /**
   * Called when first entering to Confirm screen
   * @param params the parameters passed when enter to. They must at least contain the keys:
   *  methodSID - together with the methodName key specified what method to be called when finish Confirm screen.
   *  methodName - see above.
   *  back - The sid of back screen.
   *  text - the confirm text (or its id in the local.xml) to be displayed in the Confirm screen.
   *  mfpXX - The parameters of the confirm text
   *  All other pairs key/values will pass to the method (methodSID, methodName) after user selects the confirm action.
   * @throws Exception if invalid input parameters.
   */
  public void init(Hashtable params) throws Exception {
    m_methodSID = (String)params.remove("methodSID");
    m_methodName = (String)params.remove("methodName");
    m_back = (String)params.remove("back");
    m_methodSID = m_methodSID == null ? m_methodSID = m_back : m_methodSID;
    String text = convert(getScreen(m_back).getLocalText((String)params.remove("text"),null));
    params.remove("sid");
    params.remove("go");
    params.remove("do");

    // Save parameters
    int n = -1;
    m_okParams = new Hashtable();
    Enumeration e = params.keys();
    while( e.hasMoreElements()) {
      String key = (String)e.nextElement();
      if( key.startsWith("mfp")) // get max index of parameters
      {
        int pn = Integer.parseInt(key.substring(3));
        if( pn > n)
          n = pn;
      } else
        m_okParams.put(key,params.get(key));
    }

    // Form message text string
    if( n >= 0) {
      Object[] p = new Object[n+1];
      for ( int i = 0; i <= n; i++ )
        p[i] = params.get("mfp" + i);
      m_text = MessageFormat.format(text, p);
    } else
      m_text = text;

    // Check confirmation allowed
    if( Channel.m_confirmation == false) {
      // Get screen being calling method
      Screen scr = getScreen(m_methodSID);

      // call method m_methodName on scr
      if( scr != null)
        m_channel.m_lastScreen = scr.invoke(m_methodName, m_okParams);
    }

  }

  /**
   * Called when user click cancel button on Confirm screen.
   * @param params the parameters when user clicks.
   * @return the next screen after Confirm.
   * @throws Exception
   */
  public Screen cancel(Hashtable params) throws Exception {
    return getScreen(m_back);
  }

  /**
   * Called when user click ok button on Confirm screen.
   * @param params the parameters when user clicks ok
   * @return the next screen after Confirm.
   * @throws Exception
   */
  public Screen clickOK(Hashtable params) throws Exception {
    // Get screen being calling method
    Screen scr = getScreen(m_methodSID);

    // call method m_methodName
    if( scr != null)
      return scr.invoke(m_methodName, m_okParams);
    else
      return this;
  }

  /**
   * Generate XML string for Confirm screen
   * @return xml string
   * @throws Exception
   */
  public String buildXML() throws Exception {
    StringBuffer xml = new StringBuffer();
    xml.append("<?xml version=\"1.0\"?>");
    xml.append("<confirm>");
    xml.append("<text>");
    xml.append(m_text);
    xml.append("</text>");
    xml.append("</confirm>");
    return xml.toString();
  }


  //--------------------------------------------------------------------//

  /**
   * &lt; or &#60; to < , &gt; or &#62; to >
   */
  static String convert(String s) {
    s = replace(s, "&lt;", "<");
    s = replace(s, "&gt;", ">");
    s = replace(s, "&#60;", "<");
    s = replace(s, "&#62;", ">");
    return s;
  }

  static String replace(String s1, String p1, String p2) {
    String s2 = "";
    String s = s1;
    while (s != null) {
      int i = s.indexOf(p1);
      if (i == -1) {
        s2 += s;
        return s2;
      }
      s2 += s.substring(0, i)+p2;
      s = s.substring(i+p1.length());
    }
    return null;
  }
  //-------------------------------------------------------------//
}

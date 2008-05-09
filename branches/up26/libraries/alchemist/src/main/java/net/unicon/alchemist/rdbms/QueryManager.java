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

package net.unicon.alchemist.rdbms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Simplifies query retrieval from an XML document.
 *
 * <p>Allows SQL queries to easily be separated from the codebase. Any
 * parameter orders are still required to remain consistent between the XML
 * document and the codebase.</p>
 *
 * <p>Intended for use at bootstrap time, to statically load queries for each
 * module.</p>
 *
 * @author eandresen
 * @version 2005-02-26
 */
public class QueryManager
{
   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(QueryManager.class);
   private final Map queries;
   private final String modName;

   /**
    * Construct a new QueryManager.
    * @param queryFile Context-relative path to the file containing the queries.
    * @param initModule Initial module to select from the document.
    */
   public QueryManager(String queryFile, String initModule) {
      this.modName = initModule;
      this.queries = Collections.unmodifiableMap(loadQueries(queryFile, initModule));
   }

   /**
    * Get the currently selected module name.
    * @return Currently selected module name
    */
   public String getModule() {
      return this.modName;
   }

   /**
    * Retreive the named query.
    * @param qName Name of the query to find.
    * @return Resolved query.
    */
   public String getQuery(String qName) {
      assert this.modName != null;
      assert qName != null && !"".equals(qName);
      String rslt = (String)this.queries.get(qName);

      assert rslt != null && !"".equals(rslt);
      return rslt;
   }

   private Map loadQueries(String src, String module) {
      Map rslt = new HashMap();
      
      StringBuffer buf = new StringBuffer();
      buf.append("//module[@name='")
         .append(module)
         .append("']");

      if (log.isInfoEnabled()) {
         log.info("Loading query file "+src+" for module "+module);
      }
      InputStream in = null;
      try {
         in = Thread.currentThread().getContextClassLoader().getResourceAsStream(src);
         if (in == null) {
            in = QueryManager.class.getResourceAsStream(src);
         }
         if (in == null) {
            throw new RuntimeException("Unable to locate resource: "+src);
         }
         Document doc = new SAXReader().read(in);
         Element e = (Element)doc.getRootElement().selectSingleNode(buf.toString());
         List q = e.elements("query");
         for (Iterator it = q.iterator(); it.hasNext();) {
            Element el = (Element)it.next();
            String name = el.attribute("name").getValue();
            rslt.put(name, el.getText());
         }
      } catch (Exception ex) {
         throw new RuntimeException(
               "Failed to load queries for module: "+module, ex);
      } finally {
         if (in != null) try { in.close(); } catch (IOException ex) { log.error(ex.getMessage(), ex); }
      }

      return rslt;
   }
}

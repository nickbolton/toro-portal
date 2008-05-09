/*
 *
 * Copyright (c) 2001 - 2002, Interactive Business Solutions, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Interactive Business Solutions, Inc.(IBS)  ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with IBS.
 *
 * IBS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. IBS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 *  $Log: 
 *   2    Channels  1.1         12/20/2001 3:54:39 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:53 AMFreddy Lopez    
 *  $
 *
 */

package com.interactivebusiness.news.data;

import java.util.*;

/** <p>a data object that holds informaiton about a list of topics.
 * The next() and the getxxx() methods return the topics as elements of
 * an ordered list.
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class TopicList implements java.io.Serializable
{
    private java.util.ArrayList  idList;
    private java.util.ArrayList  nameList;
    private java.util.ArrayList  descList;

    private int count = 0;
    public int getCount () { return count; }

    private int currentIndex = -1;
    public void begin() { currentIndex = -1; }


    /**
     * Checks for the existence of the next file in the list.
     * @return true - another file exists, false - no more files.
     */
    public boolean next()
    {
      return ++currentIndex < count;
    }

    public String getTopicID()
    {
      return (hasMore() ? (String) idList.get(currentIndex) : null);
    }

    public String getTopicName()
    {
      return (hasMore() ? (String)nameList.get(currentIndex) : null);
    }

    public String getTopicDescription()
    {
      return (hasMore() ? (String)descList.get(currentIndex) : null);
    }

    public boolean hasMore()
    {
      return (count > 0 && currentIndex < count);
    }

    public TopicList()
    {
      idList     = new ArrayList();
      nameList   = new ArrayList();
      descList   = new ArrayList();
    }

    public void addTopic (String ID, String name, String desc)
    {
      idList.add(ID);
      nameList.add(name);
      descList.add(desc);
      count++;
    }
    
    public void clear(){
        idList     = new ArrayList();
        nameList   = new ArrayList();
        descList   = new ArrayList();
        count = 0;
        currentIndex = -1;
    }
}

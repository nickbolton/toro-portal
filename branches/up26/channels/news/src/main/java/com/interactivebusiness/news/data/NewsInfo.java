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
 *   2    Channels  1.1         12/20/2001 3:54:33 PMFreddy Lopez    Made correction
 *        on copyright; inserted StarTeam log symbol
 *   1    Channels  1.0         12/20/2001 11:05:49 AMFreddy Lopez
 *  $
 *
 */

package com.interactivebusiness.news.data;

import java.sql.Timestamp;

/** <p>a data object that holds information about a given news story in the
 *     news application.</p>
 * @author Sridhar Venkatesh, svenkatesh@interactivebusiness.com
 * @version $LastChangedRevision$
 */

public class NewsInfo implements java.io.Serializable
{
  String newsID;
  public String getID() { return newsID; }
  public void   setID(String id) { newsID = id; }

  String topicID;
  public String getTopicID() { return topicID; }
  public void   setTopicID(String id) { topicID = id; }

  String title;
  public String getTitle() { return title; }
  public void   setTitle(String name) { title = name; }

  String newsAbstract;
  public String getAbstract() { return newsAbstract; }
  public void   setAbstract(String abs) { newsAbstract = abs; }

  String story;
  public String getStory() { return story; }
  public void setStory(String content) { story = content; }

  String author;
  public String getAuthor() { return author; }
  public void setAuthor( String name ) { author = name; }

  Timestamp beginDate;
  public Timestamp getBeginDate() { return beginDate; }
  public void setBeginDate (Timestamp date) { beginDate = date; }

  Timestamp endDate;
  public Timestamp getEndDate() { return endDate; }
  public void setEndDate (Timestamp date) { endDate = date; }

  // new features
  String layoutType;
  public String getLayoutType() { return layoutType; }
  public void setLayoutType( String type ) { layoutType = type; }

  String imageFile;
  public String getImage() { return imageFile; }
  public void setImage ( String file ) { imageFile = file; }

  String imageContentType;
  public String getImageContentType() { return imageContentType; }
  public void setImageContentType ( String contentType ) { imageContentType = contentType; }

  public NewsInfo()
  {
  }

  public NewsInfo(String id)
  {
    this.newsID = id;
  }
}

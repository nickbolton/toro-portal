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


public class UserConfig implements java.io.Serializable
{
    public UserConfig (String userID, UserConfig uc)
    {
      // check if userID has entry in NEWS_USER_CONFIG table

      if (uc != null)
      {
        this.setLayout(uc.getLayout());
        this.setItemsPerTopic(uc.getItemsPerTopic());
      }
    }
    public UserConfig ()
    {
    }

    private String Layout;
    public String getLayout () {return Layout;}
    public void setLayout (String layout) {Layout = layout;}

    private String itemsPerTopic;
    public String getItemsPerTopic () {return itemsPerTopic;}
    public void setItemsPerTopic (String items) {itemsPerTopic = items;}

}

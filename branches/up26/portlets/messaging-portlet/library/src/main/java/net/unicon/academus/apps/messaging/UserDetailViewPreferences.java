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

package net.unicon.academus.apps.messaging;

import net.unicon.alchemist.paging.PagingState;

/**
 * @author ibiswas
 * 
 */
public class UserDetailViewPreferences {
    
    // instance members
    private final PagingState reportPaging;
    private String mode;
    private int curMsgIndex = -1;
    
    public UserDetailViewPreferences(){
        this.reportPaging = new PagingState();
        this.mode = "details";
        this.curMsgIndex = -1;
    }
    
    public void setMode(String mode){
        this.mode = mode;        
    }
    
    public PagingState getReportPageState(){
        return this.reportPaging;
    }
    
    public void reset(){
        this.reportPaging.setCurrentPage(PagingState.FIRST_PAGE);
        this.mode = "details";
        this.curMsgIndex = -1;
    }
    
    public String getMode(){
        return this.mode;
    }
    
    public void setCurrentMessageIndex(int value){
        this.curMsgIndex = value;
    }
    
    public int getCurrentMessageIndex(){
        return this.curMsgIndex;
    }
    
    

}

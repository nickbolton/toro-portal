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
package  net.unicon.portal.util;

import org.jasig.portal.PortalException;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelParameter;

import java.util.Map;
import java.util.HashMap;

public class ChannelDefinitionUtil {

    /**
     * This is the wrapper for the <code>ChannelDefinition</code>
     * getParameter(String) method which seems to be removed from uPortal2.1.x
     * @param cd The <code>ChannelDefinition</code> to which the method
     * will be applied.
     * @param name The name of the parameter to get.
     * @return The value of the desired parameter.
     * @throws PortalException if either of the arguments are null.
     */
    public static String getParameter(ChannelDefinition cd, String name)
    throws PortalException {

        if (name == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("ChannelDefinitionServiceImpl::getParameter() name ");
            sb.append("parameter cannot be null!");
            throw new PortalException(sb.toString());
        }

        if (cd == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("ChannelDefinitionServiceImpl::getParameter() ");
            sb.append("ChannelDefinition parameter cannot be null!");
            throw new PortalException(sb.toString());
        }

        String paramName = null;
        ChannelParameter[] paramArr = cd.getParameters();

        for (int i=0; paramArr != null && i<paramArr.length; i++) {
            paramName = paramArr[i].getName();
            if (name.equals(paramName)) {
                return paramArr[i].getValue();
            }
        }
        return null;
    }

    /**
     * This is the wrapper for the <code>ChannelDefinition</code>
     * getParameters() method.
     * @param cd The <code>ChannelDefinition</code> to which the method
     * will be applied.
     * @return A map containing all the parameters of the given
     * <code>ChannelDefinition</code> object.
     * @throws PortalException if the ChannelDefinition object is null.
     */
    public static Map getParameters(ChannelDefinition cd)
    throws PortalException {
        
        if (cd == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("ChannelDefinitionServiceImpl::getParameters() ");
            sb.append("ChannelDefinition parameter cannot be null!");
            throw new PortalException(sb.toString());
        }
        
        ChannelParameter[] paramArr = cd.getParameters();
        Map params = new HashMap(paramArr.length);

        for (int i=0; paramArr != null && i<paramArr.length; i++) {
            params.put(paramArr[i].getName(), paramArr[i].getValue());
        }   
        return params;
    }
}

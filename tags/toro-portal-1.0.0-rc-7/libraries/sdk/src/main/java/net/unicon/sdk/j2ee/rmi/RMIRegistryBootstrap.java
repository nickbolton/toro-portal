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
package net.unicon.sdk.j2ee.rmi;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

import net.unicon.sdk.properties.UniconProperties;
import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.sdk.properties.RMIPropertiesType;

/**
 * @author Kevin Gary
 * 
 * Provides a facility to fire up an RMI Registry in the current VM
 */
public final class RMIRegistryBootstrap {

    public static final int RMI_DEFAULT_PORT = 1099;

    public static Registry bootstrapRMIRegistry() 
        throws RemoteException {
        
        int port = 0;
        
        // load the props file, look for rmi.registry.port
        UniconProperties props = UniconPropertiesFactory.getManager(RMIPropertiesType.RMI);
        
        try {
            // getPropertyAsInt could throw an exception
            port = props.getPropertyAsInt("rmi.registry.port");
        }
        catch (Throwable t1) {
            // this is a common alternative
            try {            
                port = props.getPropertyAsInt("rmi.port");
            }
            catch (Throwable t2) {
                // well now we're screwed. Set to default and hope
                // for the best
                port = RMI_DEFAULT_PORT;
            }
        }
        // OK, now the real work. This is it!
        return LocateRegistry.createRegistry(port);
    } 
}

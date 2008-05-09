/*

 *******************************************************************************

 *

 * File:       Cache.java

 *

 * Copyright:  ©2002 Unicon, Inc. All Rights Reserved

 *

 * This source code is the confidential and proprietary information of Unicon.

 * No part of this work may be modified or used without the prior written

 * consent of Unicon.

 *

 *******************************************************************************

 */

package net.unicon.portal.channels.campusannouncement.types;

import java.util.*;

import org.jasig.portal.*;

import org.jasig.portal.security.*;

public interface Cache {

    public boolean isValid(IChannel channel, IPerson person);

}


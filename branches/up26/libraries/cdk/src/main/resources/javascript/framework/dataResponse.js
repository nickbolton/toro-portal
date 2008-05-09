//****************************************************************************
//  File:           dataResponse.js
//
//  Description:    JavaScript for page returned by server into Data frame. 
//                  Needed for client-side optimization solution of Academus
//
//  Author:         Shawn Lonas
//
//  Copyright:      2002 UNICON, Inc.
//****************************************************************************
/**
*   Owner:          Shawn Lonas
*
*   Version:        $LastChangedRevision$
*/

/**
*   <!-- UNICOMMENT 
*   The function runs onload.  It analyzes the current environment and sets the
*   appropriate flag in the parent frame, or loads it into a frameset if it is
*   not in one originally.
*
*   Method:     window.onload
*
*   Parameters: 
*       none
*
*   Return: void
*   UNICOMMENT -->
*/
window.onload = function ()
{
    if (window.top != window) 
    {
        if (window.name.indexOf('Data') > -1) 
        {
            window.top.isDataReceived = true;
        } 
        else 
        {
            window.top.isDataInWrongFrame = true;
        }
    }
    else
    {
        var currentHref = window.location.href;
        window.location.href = "main.html?mode=dataOutOfFrame&currentURL="+escape(currentHref);
    }
}

// Validator on the page level
document.writeln('<script language="JavaScript" src="javascript/common/validation.js"></script>');

/*
*   <!-- UNICOMMENT
*
*       Change History:
*
*   $Log: dataResponse.js,v $
*   Revision 1.1  2003/09/20 16:16:29  nbolton
*   adding balder107 code
*
*   Revision 1.4  2003/04/15 06:11:38  nbolton
*   UserAdmin/CSCR development
*
*   Revision 1.3  2003/01/08 18:31:33  shawn
*   TTrack 02881. Added JS include to load validation.js which is expected on the page level.
*
*   Revision 1.2  2002/12/17 22:16:30  shawn
*   TT 2795 - solved situation where data intended for data frame is loaded into window without frameset
*
*   Revision 1.1  2002/12/16 20:50:34  shawn
*   GTR-16.0 client-side optimization - add separate external files to hold styling info and script for the page wrapping the data returned.
*
*
*   UNICOMMENT -->
*/

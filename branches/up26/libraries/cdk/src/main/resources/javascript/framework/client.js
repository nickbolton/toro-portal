//****************************************************************************
/*
    File:           client.js

  Description:    JavaScript for implementing a client-side templating 
                  soultion in Academus Portal.  
                  
  Purpose:        To wrap a web application whose data can be provided and 
                  updated in pieces.  This wrapping enables that dynamic 
                  updating, by controlling the receipt of new Data and  
                  updating of the original Display with the new data when 
                  needed.

  Reqs:           1) This file requires the files that it be loaded within a 
                     frameset containing a frame for Display, a frame for Data, 
                     and a frame for hidden downloads.  
                     
                  2) It requires two sister files: page.js and channel.js.
                     These files add the Page and Channel objects that are used
                     alone with the Frame Object to model the problem space.
                 
  Notes:          1) This controller supports IE5+ and NS6+ (Mozilla 1+).


  Author:         Shawn Lonas

  Copyright:      2002 UNICON, Inc.
*/
//****************************************************************************
/**
*   Owner:          Shawn Lonas
*
*   Version:        $LastChangedRevision$
*/
/*
    Debug Window
*/
/*
var debugWin = window.open("","debugWin");
var debugWinHTML = '<html><body><form><textarea id="text" cols="100" rows="100"></textarea></form></body></html>';
debugWin.document.open();
debugWin.document.writeln(debugWinHTML);
debugWin.document.close();
var logIt = function(message) {
    if (debugWin && !debugWin.closed)
    {
        var outText = debugWin.document.getElementById("text");
        if (outText)
        {
            outText.value += "\n##################\n" + message;
        }
    }
}
*/

/*
    Global Constants, Variables and Flags
*/
var NOT_FOUND = -1; // CONSTANT for testing string searches
var BEGINNING = 0; // CONSTANT for testing string searches
var UPORTAL_START = "index.jsp"; // CONSTANT pointing to main uPortal launching point
var BLANK_PAGE = "space.html"; // CONSTANT pointing to a blank html page
var HIDDEN_PAGE = "hidden.html"; // CONSTANT pointing to a blank html page for hidden frame

// The following flags will be set by other frames.  Setting a default value
// may actually cause a problem with the asynchronos nature of the communication
/*
var window.isDisplayReloaded = false; 
var window.isDataReceived = false;
var window.isDataInWrongFrame = false;
var window.isDisplayInWrongFrame = false;
var detachedChannelURL = "";
var window.isDataOutOfFrame = false;
var window.isDataOutOfFrameWaitingForRedisplay = false;
*/

// default values that should be set by the Display frame
/*
var portalMediaPath = "media/org/jasig/portal/layout/tab-column/nested-tables";
var portalSkin = "academus";
*/

// frame id (from timestamp in milliseconds). Used to give frames unique names.
var timestamp = new Date();
var frameId = timestamp.getTime();

var defaultURL = location.pathname;
defaultURL = defaultURL.substring(0,defaultURL.lastIndexOf("/")+1);
defaultURL = location.protocol + "/" + "/" + location.host + defaultURL + UPORTAL_START;

/**
   <!-- UNICOMMENT
   Class:     TemplatingFramesObject
   Will contain the properties and methods associated with a browser window
   and frames holding the other templating objects.

   Owner:      Shawn Lonas
   Revision:   see above

   Purpose:    Hold status information of the two major windows in the templating
               approach, namely the Display and Data frames/windows.  Be the 
               controller for the whole system.

   Design:     This Object should contain what is necessary to model the Frame
               controller.  It utilizes a Page object, that represents the current
               page that is loaded into the Display frame.  It also uses Channel objects,
               that contain the data and methods related to the channels within
               the page in the Display frame.

               In general, onload the Frames Object is instantiated and the checkStatus
               method is set on a timer.  This is the main event handler for the
               controller and allows it to respond to new pages loaded into the 
               Display, Data, or Template frames.

               In the case of a new document in the Display, a new Page object containing
               zero or more Channel objects are created.

               In the case of new data in the Data frame, the Channels returned are updated
               in the Page object and the Display refreshed to show the new channel content.

               In the case of a template being loaded in the Template frame, data is copied
               to the Frames object for possible use in creating new Page objects that 
               will make use of the template.

   Object Life Cycle:   The Frame object will live as long as the frameset is loaded into
                        the browser.

   UNICOMMENT -->
*/
function TemplatingFramesObject(nameRef)
{
    this.id = "";
    this.ref = window;
    this.displayRef = window.frames[0];
    this.dataRef = window.frames[1];
    this.checkStatusRef = null;

    //focusHTML = this.displayRef.document;
    //top.window.alert(focusHTML);

    // private variable reference to this Object needed for private functions
    // to access this Object.
    var thisRef = this;  

    // last Channel and its Parent where data was requested so that the channel
    // id can be built to allow IE to scroll to the correct channel if a rewrite
    // is needed.
    var lastChannel = ""; 
    var lastChannelParent = "";
    // needed to provide a way to check last command (i.e. GB export)
    var lastChannelCommand = ""; 

    // holds current template in use. For future when multiple templates
    // may be in use (i.e. multi-channel view, tabs, focused)
    var currentTemplate = "main";  
    var templates = new Object(); // page template Hash
    var page; // variable to hold reference to currentTemplate page object

    var hasToRecreateChannels = false; // Flag to indicate whether channels need to be recreated
    var nextDataURL;
    
    /**
    *   <!-- UNICOMMENT 
    *   The function constantly checks the status of various page flags to know
    *   when one or more of the various frames have been reloaded, and then acts
    *   accordingly.
    *
    *   Method:     checkStatus
    *
    *   Parameters: 
    *       none
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.checkStatus = function()
    {
        /* 
            If Display frame has been reloaded, need to re-cache content and 
            re-initialize to work with client templating approach.
        */
        if(thisRef.ref.isDisplayReloaded && page)
        {
            thisRef.displayReloaded();
        }
        /*
            If Display frame has been reloaded, ignore Data received.  Otherwise,
            grab new content and update as appropriate 
        */
        else if(thisRef.ref.isDataReceived)
        {
           thisRef.dataReceived();
        }
        /*
            If Display frame has been loaded with a Data page in error
        */
        else if(thisRef.ref.isDataInWrongFrame)
        {
            thisRef.dataInWrongFrame();
        }
        /*
            If Display frame has been loaded into the Data frame in error
        */
        else if(thisRef.ref.isDisplayInWrongFrame)
        {
            // Reload display frame through normal uportal front door
            // (condition will occur if webserver is restarted)
            thisRef.ref.isDisplayInWrongFrame=false;
            thisRef.dataRef.location.href = HIDDEN_PAGE;
            thisRef.displayRef.location.href = UPORTAL_START;
        }
        
        setTimeout(nameRef+".checkStatus()", 100);
    }
   
    /**
    *   <!-- UNICOMMENT 
    *   The function handles the display frame loaded event.
    *
    *   Method:     displayReloaded
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    this.displayReloaded = function()
    {
        // turn flag off to avoid an endless loop
        thisRef.ref.isDisplayReloaded = false;
        // set temp variable to hold reference to bodyContent <div>
        var bCRef = thisRef.displayRef.document.getElementById("bodyContent");
        if (!bCRef)
        {
            thisRef.displayRef.location.href = UPORTAL_START;
            return;
        }

        // Call page onload handler to set channels
        page.onloadHandler(bCRef.innerHTML);
        
        // set targets on links and forms to the hidden frame as appropriate
        thisRef.setTargets();
            
        // If data out of frame flag is set, reload data into data frame
        if (thisRef.ref.isDataOutOfFrame)
        {
            thisRef.ref.isDataOutOfFrame=false;
            // Get last data URL from the search string for this window
            nextDataURL = thisRef.getDataOutOfFrameURL();
            
            // If URL is not an empty string then load Data frame with the correct URL
            if (nextDataURL != "")
            {
                thisRef.ref.isDataOutOfFrameWaitingForRedisplay=true;
                thisRef.dataRef.location.href = unescape(nextDataURL);
            }
            else
            {
                showDisplay();
            }
        }
        // Bring bodyContent <div> back on the stage now that links have 
        // been changed, unless waiting for additional data to load
        else 
        {
            showDisplay();
        }

        try
        {
            thisRef.displayRef.focusOnForm(); // focus display frame for Accessibility and usability reasons
        }
        catch(e)
        {
            //do nothing (avoids blank screen for Netscape)
        }

    }

    /**   <!-- UNICOMMENT 
    *   The function handles making all of the display layers visible.
    *
    *   Method:     showDisplay
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    var showDisplay = function()
    {
        // set temp variable to hold reference to bodyContent <div>
        var bCRef = thisRef.displayRef.document.getElementById("bodyContent");

        if (bCRef.style)
        {
            bCRef.style.pixelTop = 0; // IE
            bCRef.style.top = "0px"; // Standards
        }
        
    }

    /**   <!-- UNICOMMENT 
    *   The function handles making all of the display layers hidden.
    *
    *   Method:     hideDisplay
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    var hideDisplay = function()
    {
        // set temp variable to hold reference to bodyContent <div>
        var bCRef = thisRef.displayRef.document.getElementById("bodyContent");

        if (bCRef.style)
        {
            bCRef.style.pixelTop = -5000; // IE
            bCRef.style.top = "-5000px"; // Standards
        }
        
    }

    /**   <!-- UNICOMMENT 
    *   The function handles the data frame loaded event.  The data needs to be copied
    *   from the channels within the Data frame into the corresponding channels in the
    *   Display frame.  Any scripts must be copied from the Data
    *   frame and evaluated within the Display frame.
    *
    *   Method:     dataReceived
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    this.dataReceived = function()
    {
        // turn flag off to avoid an endless loop
        thisRef.ref.isDataReceived = false;
        // get Last Channel requested info from the Data frame for auto
        // scroll (needed for IE) and to make channel command available
        getLastChannelRequested(thisRef.dataRef);
        // read from Data frame and capture response to know whether the
        // whole Display frame needs to be rewritten.
        readDataFrame();
        if (hasToRecreateChannels)
        {
            page.onloadHandler(thisRef.displayRef.document.getElementById("bodyContent").innerHTML);
        }

        // set targets on links and forms to the hidden frame as appropriate
        thisRef.setTargets();
        // if recreating channels, make them visible
        if (hasToRecreateChannels)
        {
            showDisplay();
            hasToRecreateChannels = false;
        }

        // set lastChannelCommand in display window
        thisRef.displayRef.lastChannelCommand = lastChannelCommand;

        // replace dynamic content with empty content in case of browser reload
	    thisRef.dataRef.location.href = HIDDEN_PAGE;
        
        // If data was out of frame, can now show content in main
        if (thisRef.ref.isDataOutOfFrameWaitingForRedisplay)
        {
            // turn flag off to avoid an endless loop
            thisRef.ref.isDataOutOfFrameWaitingForRedisplay = false;
            
            showDisplay();   
        }

        try
        {
            thisRef.displayRef.focusOnForm(lastChannel); // focus display frame for Accessibility and usability reasons
        }
        catch(e)
        {
            //do nothing (avoids blank screen for Netscape)
        }
        if (thisRef.displayRef.Academus && thisRef.displayRef.Academus.addSpellCheck)
        {
            thisRef.displayRef.Academus.addSpellCheck(lastChannel);
        }

    }
   
    /**   <!-- UNICOMMENT 
    *   The function handles the data loading in wrong frame event.  I.e. loaded into Display
    *   frame instead of the Date frame.  For now it simply reloads the Display frame with
    *   the default UPORTAL_START page which should display the correct view for the user.
    *
    *   Method:     dataInWrongFrame
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    this.dataInWrongFrame = function()
    {
        thisRef.ref.isDataInWrongFrame = false;

        var dRdoc = thisRef.displayRef.document;
            
        // get Last Channel requested info from the Data frame for auto
        // scdisplayReloadedroll (needed for IE) and to make channel command available
        getLastChannelRequested(thisRef.displayRef);

        // clear display frame before waiting for server to refresh
        dRdoc.open();
        dRdoc.write('<html><body></body></html>');
        dRdoc.close();
        thisRef.displayRef.location.href = UPORTAL_START;
        // set lastChannelCommand in display window
        thisRef.displayRef.lastChannelCommand = lastChannelCommand;
    }

    /**   <!-- UNICOMMENT 
    *   The function initializes the page template data model.
    *
    *   Method:     templateInitialization
    *
    *   Parameters: 
    *       none
    *
    *   Return: none
    *   UNICOMMENT -->
    */
    this.templateInitialization = function()
    {
        // Set "main" template value.  May have other values for "focused", etc
        templates[currentTemplate] = new TemplatingPageObject(currentTemplate, thisRef);
        page = templates[currentTemplate];
    }

    
    /**
    *   <!-- UNICOMMENT 
    *   The function scans the document in the Display frame for all
    *   hrefs and actions that refer to a "worker.cscr" url, and then adds to
    *   them a target pointing to the Data frame.
    *
    *   Method:     setTargets
    *
    *   Parameters: 
    *       none
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.setTargets = function()
    {
        var i; // temp counter
        var dR = thisRef.displayRef; // reference to the Display frame/window
        var aLinks = dR.document.getElementsByTagName("a"); // array of <a> elements
        var aLH; // variable to hold specific links href string
        var formRefs = dR.document.getElementsByTagName("form"); // array of <form> elements
        var fRA; // variable to hold specific form action string
        var frameworkParamExists = false;
        var queryStringSplit;
        var uPRootPos;

        // Loop thru all links to set the target to the hidden frame if it 
        // is going to use the client-side optimization approach
        for (i=0; i< aLinks.length; i++)
        {
            frameworkParamExists = false;
            aLH = aLinks[i].href;

            if (aLH.indexOf && aLH.indexOf("?") >= 0)
            {
                queryStringSplit = aLH.split("?")[1].split("&");
                for (var j=0; j<queryStringSplit.length; j++)
                {
                    if (queryStringSplit[j].indexOf("uP_") == BEGINNING)
                    {
                       frameworkParamExists = true;
                       break;
                    }
                }
            }

            if ((aLH.indexOf("worker.cscr") != NOT_FOUND) &&
                (aLH.indexOf("javascript:") == NOT_FOUND) &&
                (!frameworkParamExists || dR.isDetached))
            {
                aLinks[i].target = "Data"+frameId;
                // Check if in detach to remove the uP_root=me parameter
                if (dR.isDetached)
                {

                    if (aLH.indexOf("&uP_root=me") > -1)
                    {
                        uPRootPos = aLH.indexOf("&uP_root=me");
                        aLinks[i].href = aLH.substr(0,uPRootPos) + aLH.substr(uPRootPos+11);
                    }
                    else if (aLH.indexOf("uP_root=me&") > -1)
                    {
                        uPRootPos = aLH.indexOf("uP_root=me&");
                        aLinks[i].href = aLH.substr(0,uPRootPos) + aLH.substr(uPRootPos+11);
                    }
                    else if (aLH.indexOf("uP_root=me") > -1)
                    {
                        uPRootPos = aLH.indexOf("uP_root=me");
                        aLinks[i].href = aLH.substr(0,uPRootPos) + aLH.substr(uPRootPos+10);
                    }
                }
            }
        } // end for loop

        // Loop thru all forms to set the target to the hidden frame if it 
        // is going to use the client-side optimization approach
        for (i=0; i< formRefs.length; i++)
        {
            frameworkParamExists = false;
            fRA = formRefs[i].action;
            
            if (fRA.indexOf &&
                fRA.indexOf("?") >= 0)
            {
                queryStringSplit = fRA.split("?")[1].split("&");
                for (var j=0; j<queryStringSplit.length; j++)
                {
                    if (queryStringSplit[j].indexOf("uP_") == BEGINNING)
                    {
                        frameworkParamExists = true;
                        break;
                    }
                }
            }

            if (!frameworkParamExists) {
                for (var j=0; j<formRefs[i].elements.length; j++)
                {
                    if (formRefs[i].elements[j].name && formRefs[i].elements[j].name.indexOf("uP_") ==
                        BEGINNING)
                    {
                        frameworkParamExists = true;
                        break;
                    }
                }
            }

            if (fRA.indexOf &&
                (!frameworkParamExists || dR.isDetached)      &&
                fRA.indexOf("worker.cscr") != NOT_FOUND)
            {
                formRefs[i].target = "Data"+frameId;
                // Check if in detach to remove the uP_root=me parameter
                if (dR.isDetached)
                {
                    if (fRA.indexOf("&uP_root=me") > -1)
                    {
                        uPRootPos = fRA.indexOf("&uP_root=me");
                        formRefs[i].action = fRA.substr(0,uPRootPos) + fRA.substr(uPRootPos+11);
                    }
                    else if (fRA.indexOf("uP_root=me&") > -1)
                    {
                        uPRootPos = aLH.indexOf("uP_root=me&");
                        formRefs[i].action = fRA.substr(0,uPRootPos) + fRA.substr(uPRootPos+11);
                    }
                    else if (fRA.indexOf("uP_root=me") > -1)
                    {
                        uPRootPos = aLH.indexOf("uP_root=me");
                        formRefs[i].action = fRA.substr(0,uPRootPos) + fRA.substr(uPRootPos+10);
                    }
                    else if (frameworkParamExists)
                    {
                        for (var j=0; j<formRefs[i].elements.length; j++)
                        {
                            if (formRefs[i].elements[j].name && formRefs[i].elements[j].name.indexOf("uP_root") ==
                                BEGINNING)
                            {
                                formRefs[i].elements[j].name = "";
                                formRefs[i].elements[j].value = "";
                            }
                        }
                    }
                }
            }
        }

    }

    /**
    *   <!-- UNICOMMENT 
    *   The function will return the currentURL value from the page's search
    *   string.
    *
    *   Method:     getDataOutOfFrameURL
    *
    *   Parameters: 
    *       none
    *
    *   Return: String
    *   UNICOMMENT -->
    */
    this.getDataOutOfFrameURL = function()
    {
        var dataURLSearch = location.search; // ?key=value&...
        var dataURLSearchSplit = dataURLSearch.split("&"); // split by &
        var dataURL = "";
        // Loop through all parts looking for cscrHandle and cscrParentId
        for (var i=0; i<dataURLSearchSplit.length; i++)
        {
            if (dataURLSearchSplit[i].indexOf("currentURL") != NOT_FOUND)
            {
                dataURL = dataURLSearchSplit[i].split("=")[1];
            }
        }
        return dataURL;
    }
    
    /**
    *   <!-- UNICOMMENT 
    *   The function reads the <div> layers in the Data frame and stores the
    *   content for those channels that correspond to those in the Display
    *   frame.  It will then simply replace the innerHTML of the 
    *   appropriate <div>s in the Display frame.
    *
    *   Method:     readDataFrame
    *
    *   Parameters: 
    *       none
    *
    *   Return: boolean
    *   UNICOMMENT -->
    */
    var readDataFrame = function()
    {

        var i, ii, iii; // counters
        var dID; // div id
        var divHTML; // div innerHTML
        var channelInDisplayFrame; // specific ref for channel in Display frame
        // flag to indicate if channels need to be recreated from display frame after Data is copied
        hasToRecreateChannels = false; // set flag to default condition, channels don't need to be recreated

        var cA; // temp variable for the channel function array
        var dR = thisRef.displayRef;
        var channelsRefInDisplay;
        
        // ISSUE: On Gradebook export, noticed response was the same channel two times.
        // Use these maps to keep from copying duplicate functions
        var functionMap = new Object(); // Map of functions in the Data frame
        var functionInitMap = new Object(); // Map of init functions in Data frame
        
        // get content from Data frame and replace in Display frame if match is found
        var divArray = thisRef.dataRef.document.getElementsByTagName("div");
        var channelsToRefresh = {}; // Map to hold channel references that may need to refresh in Mozilla if channels have to be recreated
        for (i=0; i<divArray.length; i++)
        {
            dID = divArray[i].id;
            
            channelInDisplayFrame = page.allChannels[dID];
            
            // returned content matches channel in Map
            if (channelInDisplayFrame)
            {
                if (channelInDisplayFrame.hasNoSubChannel())
                {
                    divHTML = divArray[i].innerHTML;

                    // update channel content
                    channelInDisplayFrame.setContent(divHTML);

                    // Replace innerHTML
                    channelInDisplayFrame.refreshContent(); // tell channel to refresh itself
                    // call function in display frame to rewrite image src tags with appropriate skin information
                    if(thisRef.displayRef.imgSrcRewrite) 
                    {
                        thisRef.displayRef.imgSrcRewrite(dID);
                    }
                } // end if layer in Data matches channel
                else // else channel has subchannels
                {
                    hideDisplay(); // hide display until channels copied and recreated.
                    hasToRecreateChannels = true;
                    divHTML = divArray[i].innerHTML;

                    // update channel content
                    channelInDisplayFrame.setContent(divHTML);
                    channelInDisplayFrame.refreshContent(); // tell channel to refresh itself
                    
                } // end else channel has subchannels
            } // end if channel in Data matches channel in Page
        } // end loop thru all layers in Data

        if (hasToRecreateChannels)
        {
            for (i in channelsToRefresh)
            {
                channelsToRefresh[i].refreshContent(true); // make sure all channels refresh content since channels will be recreated
            } // end loop thru channels to refresh
        } // end if channels need to be recreated
        
        if (thisRef.dataRef.channelFunctionsArray)
        {
            // set lastChannelCommand in display window
            dR.lastChannelCommand = lastChannelCommand;
            cA = thisRef.dataRef.channelFunctionsArray;
            for (ii=0; ii<cA.length; ii++)
            {
                for (iii=0; iii<cA[ii].length; iii++)
                {
                    functionName = cA[ii][iii];
                    if (!functionMap[functionName])
                    {
                        functionBody = thisRef.dataRef[functionName].toString();
                        dR.eval("window."+functionName+"="+functionBody);
                        functionMap[functionName] = functionBody;
                    } // end if function not previously copied
                } // end loop thru functions for particular channel
                if (!functionInitMap[cA[ii][0]])
                {
                    functionInitMap[cA[ii][0]] = true; // Only initialize the function one time
                    dR[cA[ii][0]](); // call init function for channel
                } // end if function not already initialized
            } // end loop thru all channels functions
        } // end if IE and channel functions array is defined

        return;
    
    } // end read Data method
   
    /**
    *   <!-- UNICOMMENT 
    *   The function parses the URL in the specified frame to extract certain data concerning 
    *   the last channel requested.
    *
    *   Method:     getLastChannelRequested
    *
    *   Parameters: 
    *       frameRef        Window Object Reference
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    var getLastChannelRequested = function(frameRef)
    {
        var dataURLSearch = frameRef.location.search; // ?key=value&...
        var dataURLSearchSplit = dataURLSearch.split("&"); // split by &
        // clear any previous lastChannel information
        lastChannel = "";
        lastChannelParent = "";
        lastChannelCommand = "";
        // Loop through all parts looking for cscrHandle and cscrParentId

        if (frameRef.location.href.indexOf(".uP") != NOT_FOUND)
        {
            firstURLPart = frameRef.location.href.split(".uP")[0];
            dotURLParts = firstURLPart.split(".");
            lastChannel = dotURLParts[dotURLParts.length-1];
        } // end if key is cscrHandle
        for (var i=0; i<dataURLSearchSplit.length; i++)
        {
            if (dataURLSearchSplit[i].indexOf("cscrParentId") != NOT_FOUND)
            {
                lastChannelParent = dataURLSearchSplit[i].split("=")[1] + "_";
            } // end if key is cscrParentId
            if (dataURLSearchSplit[i].indexOf("command") != NOT_FOUND)
            {
                lastChannelCommand = dataURLSearchSplit[i].split("=")[1];
            } // end if key is command
        } // end loop thru keys in the search string of the URL

    } // end method to get last channel requested information   
    
} // end Templating Frame Object definition

/**
*   <!-- UNICOMMENT 
*   The function defines a listener for the window.onload event.  When triggered
*   it makes sure that all of the frames are initialized properly.
*
*   Method:     onload
*
*   Parameters: 
*       e       Object Ref (Event Obj)
*
*   Return: void
*   UNICOMMENT -->
*/
window.onload = function(e)
{
    // Create object to represent this instance, and start the status checking
    // Set as global variables
    academusTemplating = new TemplatingFramesObject("academusTemplating");
    academusTemplating.templateInitialization();
    academusTemplating.checkStatusRef = setTimeout("academusTemplating.checkStatus()", 100);
    
    // Set the frames' names
    window.frames[0].name = "Display"+frameId;
    window.frames[1].name = "Data"+frameId;

    // ISSUE: Hitting the browser reload or back button can cause problems with
    // frames.  IE and Mozilla handle normal reload differently.  To solve IE
    // reload problem, check the file currently in the Display frame to determine
    // if it is a JS written URL (i.e. IE uses parent URL).  If so, change it 
    // back to the default space.html temporarily.
    if (window.frames[0].location.href.indexOf("/main.html") != NOT_FOUND)
    {
        window.frames[0].location.href = BLANK_PAGE;
    }
    
    // If data page is loaded into a window without this frameset it will call this
    // and pass the last requested URL
    if (location.search.indexOf("mode=dataOutOfFrame")!=NOT_FOUND)
    {
        // set flag so that after UPORTAL_START loads the specific data page can be called
        window.isDataOutOfFrame = true; 
    }
    
    // If loaded in detached mode, then load specified URL into display frame
    if(location.search.indexOf("mode=detachedChannel")!=NOT_FOUND)
    {
        // Get last data URL from the search string for this window
        var nextDataURL = academusTemplating.getDataOutOfFrameURL();
        
        // If URL is not an empty string then load Data frame with the correct URL
        if (nextDataURL != "")
        {
            window.frames[0].location.href = unescape(nextDataURL);
        }
        else
        {
            window.frames[0].location.href = UPORTAL_START;
        }
    }
    else
    {
        window.frames[0].location.href = UPORTAL_START;
    }
}

/*
*   <!-- UNICOMMENT
*
*       Change History:
*
*   $Log: client.js,v $
*   Revision 1.4  2004/03/04 21:02:58  shawn
*   tt3704/3714 Fixed NS 7.1 blank screen issues by trapping errors that were inconsistently appearing
*
*   Revision 1.3  2004/02/20 17:21:23  shawn
*   tt4277 - Fixed NS7.02 bug caused by passing div reference.  Broke out JS from nested-tables.xsl.
*
*   Revision 1.2  2004/02/11 20:51:04  shawn
*   tt3704 - CSCR fix for Mozilla-based browsers.
*
*   Revision 1.1  2003/09/20 16:16:29  nbolton
*   adding balder107 code
*
*   Revision 1.22  2003/07/25 20:19:06  rwalters
*   tt03823 - fixed issues with incorrect skin images loading initially and not being corrected until rollover
*
*   Revision 1.21  2003/07/01 20:48:56  shawn
*   tt03635 Fixed condition that was causing endless loop
*
*   Revision 1.20  2003/05/06 15:57:21  nbolton
*   removed last change
*
*   Revision 1.18  2003/05/05 19:37:14  shawn
*   tt2893 - Added JS to handle case where normal uportal page is loaded into Data frame
*
*   Revision 1.17  2003/04/19 01:34:33  rwalters
*   fixed commenting error
*
*   Revision 1.16  2003/04/16 01:20:23  nbolton
*   TT 03192: UserAdmin/Permissions servant development
*
*   Revision 1.15  2003/04/15 06:11:38  nbolton
*   UserAdmin/CSCR development
*
*   Revision 1.14  2003/04/09 18:12:55  mfreestone
*   OPT1-(1-11), focusing cscr channels outside lms
*
*   Revision 1.13  2003/04/08 05:39:01  mfreestone
*   OPT-(1-11), fix for focusing non lms cscr channels
*
*   Revision 1.12  2003/03/19 00:03:28  shawn
*   tt2792
*
*   Revision 1.11  2003/01/31 16:41:29  shawn
*   ttrack 3011
*
*   Revision 1.10  2003/01/22 20:22:21  shawn
*   TTrack 2976 - changed js to get detach working
*
*   Revision 1.9  2003/01/13 21:01:09  shawn
*   TTrack 2932 - Wrapped JS written html with a span to mark what needs to be deleted by NS with client-side rendering on
*
*   Revision 1.8  2002/12/17 23:59:54  shawn
*   TT 2795 - solved situation where data intended for data frame is loaded into window without frameset
*
*   Revision 1.7  2002/12/17 22:16:30  shawn
*   TT 2795 - solved situation where data intended for data frame is loaded into window without frameset
*
*   Revision 1.6  2002/12/13 15:04:09  shawn
*   GTR-16.0 client-side optimization - fixed Forum link problem for both IE and NS
*
*   Revision 1.5  2002/12/13 00:56:26  shawn
*   GTR-16.0 client-side optimization
*
*   Revision 1.4  2002/12/05 23:09:07  shawn
*   client-side optimization solution for script
*
*   Revision 1.3  2002/11/26 15:49:22  shawn
*   Removed unnecessary comments
*the last channel requested.
*   Revision 1.2  2002/11/21 20:39:56  shawn
*   Added script to provide safety net if loaded in wrong frame
*
*   Revision 1.1  2002/11/20 16:02:24  shawn
*   Added file to support client-side optimizations
*
*
*   UNICOMMENT -->
*/

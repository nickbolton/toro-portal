/**
   <!-- UNICOMMENT
   Class:     TemplatingPageObject
   Will contain the properties and methods associated with a rendered uPortal
   page.

   Owner:      Shawn Lonas
   Revision:   see above

   Purpose:    Hold information relavent to the rendered page to allow it to be
               updated and rendered as needed.  The Page object has a collection
               of channels that it keeps.

   Design:     This object should contain all of the methods and properties 
               particular to the rendered page.  This includes possibly the content
               of the page that wraps the channels, as well as a collection of the
               channels themselves.

               The Page object is invoked from the Frame object through the 
               onloadHandler, resetReferences, and setTemplate methods.  It also makes
               use of the channel Hashes in order to identify and work with the 
               channels on the Page. (Though this should probably be handled through
               a method on this object).

   Object Life Cycle: 
               The Page object is instantiated when the controller loads initially.  
               It continues until the frameset is unloaded.

   UNICOMMENT -->
*/
var TemplatingPageObject = function(id,frameRef)
{
    var thisRef = this;
    this.id = id;
    this.bodyContent = ""; // Body template (read from Display frame)
    this.frameRef = frameRef; // reference back to Frame Object
 
    
    // private variable to allow private functions to access this Object
    var thisRef = this;

    /**
    *   <!-- UNICOMMENT 
    *   The function is used to reset all of the channel references after page is redrawn.
    *
    *   Method:     resetReferences
    *
    *   Parameters: 
    *       windowRef       Window Object Reference
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.resetReferences = function(windowRef)
    {
        for(var i in thisRef.allChannels)
        {
            thisRef.allChannels[i].resetReference(windowRef); // tell each channel on page to reset reference 
        }
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function is returns a reference to a channel, that is somewhere contained on the page, by ID.
    *
    *   Method:     getAllChannelsRef
    *
    *   Parameters: 
    *       id       String
    *
    *   Return: Object Ref
    *   UNICOMMENT -->
    */
    this.getAllChannelsRef = function(id)
    {
        return thisRef.allChannels[id];
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function is used set the channel's reference to its layer (<div>) in the Display frame.
    *
    *   Method:     setChannelRef
    *
    *   Parameters: 
    *       id       String
    *       ref      Object Ref (pointing to HTMLElement of <div> in Display Frame)
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.setChannelRef = function(id,ref)
    {
        thisRef.channels[id] = ref;
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function is used to initialize the page with a new body content String.
    *   This string may contain channels identified by <div> tags with "HTMLBody"
    *   as part of the id.  This function will then create the necessary channel
    *   objects to represent these channels, as well as identify the hierarchy of
    *   channels.
    *
    *   Method:     onloadHandler
    *
    *   Parameters: 
    *       content       String
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.onloadHandler = function(content)
    {
        thisRef.bodyContent = content;
        // clear all previous channels references
        thisRef.allChannels = {}; 
        thisRef.channels = {};

        var d = frameRef.displayRef.document.getElementsByTagName("div");
    
        for(var i=0; i< d.length; i++)
        {
            dID = d[i].id;
            if (dID.indexOf("HTMLBody") != NOT_FOUND)
            {
                thisRef.allChannels[dID] = new ChannelObject(d[i],thisRef);
            } // end if layer is a channel
        } // end loop thru all div tags  
       
        // loop thru all channels to have each identify what subchannels
        // they themselves contain
        for (i in thisRef.allChannels)
        {
            thisRef.allChannels[i].findAllSubChannels();
        }
        // loop thru all channels again to have each channel set itself in
        // its parent's list of subchannels
        for (i in thisRef.allChannels)
        {
            thisRef.allChannels[i].setSubChannelInParent();
        }
        
    } // end onloadHandler method
        
} // end PageObject
    var thisRef = this;


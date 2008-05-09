/**
   <!-- UNICOMMENT
   Class:     ChannelObject
   Will contain the properties and methods associated with a rendered uPortal
   channel.

   Owner:      Shawn Lonas
   Revision:   see above

   Purpose:    Hold information relavent to the rendered channel to allow it to be
               updated and re-rendered as needed.  The Channel object can have a collection
               of subchannels that it keeps.

   Design:     This object should contain all of the methods and properties 
               particular to the rendered channel.  This may include a collection of the
               subchannels themselves.

   Object Life Cycle: 
               The Channel object is instantiated everytime a new document is loaded
               directly into the display frame containing channels, or if a parent channel 
               is replaced with other channels (i.e. a SuperChannel replaces the included channels).

   UNICOMMENT -->
*/
var ChannelObject = function(ref,pageRef)
{
    var thisRef = this;
    this.id = ref.id;
    this.ref = ref; // channel ref
    this.parent = null; // parent channel
    this.page = pageRef; // page reference
    this.content = ref.innerHTML;
    this.subchannels = {};
    this.allSubChannels = {};
    this.currentParentsDescendants = 9999;

    this.numAllSubChannels = 0;
    this.numSubChannels = 0;
    /**
    *   <!-- UNICOMMENT 
    *   The function returns true or false depending on whether the channel has no subchannels.
    *
    *   Method:     hasNoSubChannel
    *
    *   Parameters: 
    *       none
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.hasNoSubChannel = function()
    {
        return (!(thisRef.numSubChannels > 0)); // returns true or false depending on if channel has NO subchannel
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function returns true or false depending on whether the channel has subchannels.
    *
    *   Method:     hasSubChannel
    *
    *   Parameters: 
    *       none
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.hasSubChannel = function()
    {
        return (thisRef.numSubChannels > 0); // returns true or false depending on if channel has subchannels
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function sets the content of the channel to the specified string.
    *
    *   Method:     setContent
    *
    *   Parameters: 
    *       newContent      String
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.setContent = function(newContent)
    {
        thisRef.content = newContent || thisRef.ref.innerHTML;
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function returns the content of the channel, either from the content string directly, or by
    *   filling in the channel's template with subchannel content.  Which is done is controlled by the 
    *   useContentOnly parameter that is passed in.
    *
    *   Method:     getContent
    *
    *   Parameters: 
    *       none
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.getContent = function()
        {
            return thisRef.content;
        }
    /**
    *   <!-- UNICOMMENT 
    *   The function refreshes the content in the Display frame with the content of the
    *   channel.
    *
    *   Method:     refreshContent
    *
    *   Parameters: 
    *       none
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.refreshContent = function()
    {
       thisRef.ref.innerHTML = thisRef.getContent();
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function identifies all channels contained within its content.  It gets a reference to each
    *   subchannels Channel Object and then goes about determining which of the subchannels are direct
    *   children as opposed to descendants.
    *
    *   Method:     findAllSubChannels
    *
    *   Parameters: 
    *       none
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.findAllSubChannels = function ()
    {
        var d = thisRef.ref.getElementsByTagName("div");
        var subChannel;
        
        for(var i=0; i< d.length; i++)
        {
            var did = d[i].id;
            if (did.indexOf("HTMLBody") != NOT_FOUND)
            {
                thisRef.numAllSubChannels++; // increment the number of descendant channels
                thisRef.allSubChannels[did] = thisRef.page.getAllChannelsRef(did); // adds to this channels descendant map
                subChannel = thisRef.allSubChannels[did]; // temp variable ref to subchannel
                // check the subchannels to see if current parents number of descendants is larger than this channels 
                // number of descendants.  If so, this channel is a nearer parent than the previous.
                if (subChannel.currentParentsDescendants > d.length)
                {
                    subChannel.parent = thisRef;
                    subChannel.currentParentsDescendants = d.length;
                } // end if this channel is nearer parent to subchannel
            } // end if <div> is a channel
            
        } // end loop thru all <div>'s within this channel

    } // end find all subchannels method
    /**
    *   <!-- UNICOMMENT 
    *   The function sets a reference to itself in its parent's channel map to complete the tree structure.
    *
    *   Method:     setSubChannelInParent
    *
    *   Parameters: 
    *       none
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.setSubChannelInParent = function ()
    {
        if(thisRef.parent)
        {
            thisRef.parent.subchannels[thisRef.id] = thisRef;
            thisRef.parent.numSubChannels++;
        }
        else if (thisRef.page)
        {
            thisRef.page.setChannelRef(thisRef.id, thisRef);
        }
        
    }
    /**
    *   <!-- UNICOMMENT 
    *   The function is used to reset the channel reference (reference to the <div/>) on the page.
    *
    *   Method:     resetReference
    *
    *   Parameters: 
    *       windowRef       Window Object Reference
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.resetReference = function (windowRef)
    {
        thisRef.ref = windowRef.document.getElementById(thisRef.id);
    }
} // end ChannelObject

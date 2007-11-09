//****************************************************************************
//  File:           expandableTree.js
//
//  Description:    JavaScript for implementing an expandable/collapsible tree
//                  in Academus
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
*   Class:     TreeRowObject
*   Will contain the properties need to describe a row in a collapsible,
*   expandable, tree table.
*
*   Owner:      Shawn Lonas
*   Revision:   see above
*
*   Purpose:    Provide a convenient way to set the properties of a row in the
                table without unnecessary duplication.  This object can be 
                specific to a particular table implementation and modified
                as needed.
*   Design:
*
*   Object Life Cycle:
*
*   UNICOMMENT -->
*/
TreeRowObject = function(ordering,displayedOrdering,text,rowLink,
            isPF,isVisited,isCurrentPage,isExpanded,iconOpen,iconClosed,
            iconSelected,hasEndingImg,trailingImgSrc,trailingImgRolloverSrc,
            trailingImgSelectedSrc,trailingContent,linkTarget,
            isLeadingIconMenuToggle,isTextMenuToggle,isTrailingIconMenuToggle,
            isLeadingIconContentLink,isTextContentLink,isTrailingIconContentLink,
            iconWithChildrenCollapsed,iconWithChildrenExpanded,iconWithoutChildren)
{
    this.ordering = ordering;
    this.displayedOrdering = displayedOrdering;
    this.text = text;
    this.rowLink = rowLink;
    //this.isContentLink = isContentLink;
    this.isPF = isPF;
    this.isVisited = isVisited;
    this.isCurrentPage = isCurrentPage;
    this.isExpanded = isExpanded;
    this.iconOpen = iconOpen;
    this.iconClosed = iconClosed;
    this.iconSelected = iconSelected;
    this.iconWithChildrenCollapsed = iconWithChildrenCollapsed;
    this.iconWithChildrenExpanded = iconWithChildrenExpanded;
    this.iconWithoutChildren = iconWithoutChildren;
    //this.isTextLink = isTextLink;
    this.hasEndingImg = hasEndingImg;
    this.trailingImgSrc = trailingImgSrc;
    this.trailingImgRolloverSrc = trailingImgRolloverSrc;
    this.trailingImgSelectedSrc = trailingImgSelectedSrc;
    this.trailingContent = trailingContent; 
    this.linkTarget = linkTarget; 
    this.isLeadingIconMenuToggle = isLeadingIconMenuToggle;
    this.isLeadingIconContentLink = isLeadingIconContentLink;
    this.isTextMenuToggle = isTextMenuToggle;
    this.isTextContentLink = isTextContentLink;
    this.isTrailingIconMenuToggle = isTrailingIconMenuToggle;
    this.isTrailingIconContentLink = isTrailingIconContentLink;
}

/**
*   <!-- UNICOMMENT
*   Class:     TreeTableObject
*   Will contain the properties and methods for displaying a collapsible,
*   expandable, tree table.
*
*   Owner:      Shawn Lonas
*   Revision:   see above
*
*   Purpose:
*   Design:
*
*   Object Life Cycle:
*
*   UNICOMMENT -->
*/
TreeTableObject = function(tableID)
{
    var colCount = 0;
    var rowCount = 0;
    var rowRef;
    var rowLink = "";
    var tocPath;
    var cols = 0;
    this.child = [];
    this.rows = new Array();

    this.currLocation = "";
    this.currPagePos = 0;
    this.currPageCount = 0;

    this.header = "";
    this.footer = "";
    this.leftMargin = "0";
    this.jsSrcPath = "";
    this.cssSrc = "";
    this.iconSrcPath = "";

    // New properties to extend to HTML elements
    this.frameHeader = ''; // HTML for window header (if applicable)
    this.frameFooter = ''; // HTML for window footer (if applicable)
    this.outputRef = window; // default output to current window
    this.outputType = "window"; // default output type (i.e. "window","HTMLElement")
    this.fromOutputToThis = "parent.pfTree"; // path to this Object from Tree
    this.onrolloverFunctionName = ""; /* name of function to perform rollover functioning
                                         will receive the image id/name, and new image source */
    this.trailingImgSrcPath = ""; // path to be prepended to trailing image sources
    
    /**
    *   <!-- UNICOMMENT
    *   addRow will except the data from the user or server and will set up
    *   the internal structure of the TreeTableObject appropriately.
    *
    *   Method:     addRow
    *
    *   Parameters:
    *           rowRef      Object Reference
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    /*this.addRow = function(ordering,displayedOrdering,text,rowLink,isContentLink,
                    isPF,isVisited,isCurrentPage,isExpanded)*/
    this.addRow = function (rowRef)
    {
        rowRef.parent = this;
        rowRef.child = new Array();
        rowRef.rowNumber = rowCount;
        rowRef.contextPos = 0;
        rowRef.contextLength = 0;
        this.rows[rowCount] = rowRef;
        rowCount++;
    }

    /**
    *   <!-- UNICOMMENT
    *   parseRows creates a DOM-like structure for the Table entries.  It
    *   takes the ordering string and creates parent, children references
    *   to more easily navigate the data.
    *
    *   Method:     parseRows
    *
    *   Parameters: none
    *
    *   Return: String
    *   UNICOMMENT -->
    */
    this.parseRows = function()
    {
        var rowRef;
        // Find the maximum number of columns by splitting each rows toc path (i.e. 1.2.1.3)
        for (var i=0; i<this.rows.length; i++)
        {
            rowRef = this.rows[i];
            tocPath = rowRef.ordering.split(".");
            parentRef = rowRef.parent;
            for (var ii=0; ii< tocPath.length-1; ii++)
            {
                parentRef = parentRef.child[parentRef.child.length-1];
            }
            parentRef.child[parentRef.child.length] = rowRef;
            rowRef.parent = parentRef;
            cols = (tocPath.length > cols)? tocPath.length:cols;
        }
        return;
    }

    /**
    *   <!-- UNICOMMENT
    *   getTree returns the Table as an HTML representation
    *
    *   Method:     getTree
    *
    *   Parameters:
    *       <Name>   <Type>
    *
    *   Return: String
    *   UNICOMMENT -->
    */
    this.getTree = function()
    {
        var results = this.frameHeader;
        results += this.header;
        
        results += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n<tr>\n";
        for (var i=0; i<(2*cols); i++) {
            results += "<td>&nbsp;</td>";
        }
        results += "<td width=\"240\" class=\"uportal-text\">&nbsp;</td><td class=\"uportal-text\">&nbsp;</td><td width=\"100%\" class=\"uportal-text\">&nbsp;</td></tr>";
        results = this.displayChildNodes(this, results);
        results += "</table>";

        results += this.footer;
        results += this.frameFooter;
        return results;
    }

    /**
    *   <!-- UNICOMMENT
    *   drawTree (re)draws the Table.
    *
    *   Method:     drawTree
    *
    *   Parameters: none
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.drawTree = function()
    {
        var stdOut = this.outputRef;
        // If stdOut has document property and it has a write property use it
        if (this.outputType == "window")
        {
            stdOut.document.open();
            stdOut.document.write(this.getTree());
            stdOut.document.close();
        }
        else if (this.outputType == "HTMLElement") // else if innerHTML is property use it
        {
            stdOut.innerHTML = this.getTree();
        }
        else
        {
            alert('Tree output type not recognized');
        }
    }



    /**
    *   <!-- UNICOMMENT
    *   displayChildNodes returns child nodes of the Table as an HTML representation
    *
    *   Method:     displayChildNodes
    *
    *   Parameters:
    *       nodeRef   Object reference
    *
    *   Return: String
    *   UNICOMMENT -->
    */
    this.displayChildNodes = function(nodeRef, results)
    {
        for (var i=0; i<nodeRef.child.length; i++)
        {
            var rowRef = nodeRef.child[i];
            var tocPath = rowRef.ordering.split(".");
            var rowStyle = "level"+tocPath.length;
            rowStyle = (tocPath.length == cols && !rowRef.isPF)? "nonreferenced":rowStyle;
            results += "<tr>\n";
            for (var ii=0; ii<(tocPath.length-1); ii++)
            {
                results += "<td class=\"uportal-text\">&nbsp;</td><td class=\"uportal-text\">&nbsp;</td>"; // children indicator, leading icon
            }
            
            var imgSrc,imgAlt,imgSrcForChildren,imgAltForChildren;
            /*if(rowRef.isExpanded)
            {
                imgSrc = this.iconSrcPath+rowRef.iconOpen;
                imgAlt = "&quot;Opened&quot; Icon: to close";
            }
            else
            {
                imgSrc = this.iconSrcPath+rowRef.iconClosed;
                imgAlt = "&quot;Closed&quot; Icon: to open";
            }*/
            
            if (rowRef.child.length>0)
            {
                if (rowRef.isExpanded)
                {
                    imgSrcForChildren = this.iconSrcPath+rowRef.iconWithChildrenExpanded;
                    imgAltForChildren = "&quot;Expanded&quot; icon: to collapse and hide contents";
                    imgSrc = this.iconSrcPath+rowRef.iconOpen;
                    imgAlt = "&quot;Opened&quot; icon: to close";
                    
                }
                else
                {
                    imgSrcForChildren = this.iconSrcPath+rowRef.iconWithChildrenCollapsed;
                    imgAltForChildren = "&quot;Collapsed&quot; icon: to expand and show contents";
                    imgSrc = this.iconSrcPath+rowRef.iconClosed;
                    imgAlt = "&quot;Closed&quot; icon: to open";
                }
            }
            else
            {
                imgSrcForChildren = this.iconSrcPath+rowRef.iconWithoutChildren;
                imgAltForChildren = "Bullet icon";
                imgSrc = this.iconSrcPath+rowRef.iconClosed;
                imgAlt = "&quot;Content&quot; icon";
                
            }
            var linkStyle = (rowRef.isVisited)? "class=\"viewed\"":"";

            // Children Indicator
            results += "<td style=\"text-align:right; vertical-align:bottom;\" nowrap=\"true\" class=\"uportal-text\">";
            results += this.rowLinkGenerator(rowRef,false,true); // returns <a ...>
            results += "<img src=\""+imgSrcForChildren+"\" alt=\""+imgAltForChildren+"\" vspace=\"1\" border=\"0\"></a>&nbsp;</td>";
            
            // Leading Icon
            results += "<td style=\"text-align:left; vertical-align:top;\" nowrap=\"true\" class=\"uportal-text\">";
            results += this.rowLinkGenerator(rowRef,rowRef.isLeadingIconContentLink,rowRef.isLeadingIconMenuToggle); // returns <a ...>
            results += "<img src=\""+imgSrc+"\" alt=\""+imgAlt+"\" width=\"16\" height=\"16\" border=\"0\"></a>&nbsp;</td>";

            // If Content Link
            if (rowRef.isLeadingIconContentLink || rowRef.isTextContentLink || rowRef.isTrailingIconContentLink) {
                //rowLink = rowRef.rowLink;

                if (rowRef.ordering == this.currLocation)
                {
                   rowStyle = "titlehighlight";
                   this.currPagePos = i + 1;
                   this.currPageCount = rowRef.parent.child.length;
                }
            }
            
            results += "<td valign=\"top\" nowrap=\"true\" colspan=\""+2*(cols-tocPath.length+1)+"\" class=\"uportal-text\"><span class=\""+rowStyle+"\">";
            if (rowRef.isTextMenuToggle || rowRef.isTextContentLink)
            {
                results += this.rowLinkGenerator(rowRef,rowRef.isTextContentLink,rowRef.isTextMenuToggle)+rowRef.text+"</a>";
            }
            else
            {
                results += rowRef.text;
            }
            results += "</span>";
            
            // If Trailing Icon
            if (rowRef.hasEndingImg)
            {
                results += "&nbsp;";
                results += this.rowLinkGenerator(rowRef,rowRef.isTrailingIconContentLink,rowRef.isTrailingIconMenuToggle); // returns <a ...>
                results += '<img border="0" src="'+this.trailingImgSrcPath+rowRef.trailingImgSrc+'" align="absmiddle" name="trailingIcon'+tableID+rowRef.rowNumber+'" id="trailingIcon'+tableID+rowRef.rowNumber+'"/>';
                results += "</a>";
            
            }
            
            results += rowRef.trailingContent;
            results += "<td class=\"uportal-text\">&nbsp;</td></td></tr>\n";

            if (rowRef.child.length>0 && rowRef.isExpanded)
            {
                results = this.displayChildNodes(rowRef, results); // recursive call to display rowRef's child nodes
            }
        }
        return results;
    }

    /**
    *   <!-- UNICOMMENT
    *   rowLinkGenerator returns the opening anchor tag (i.e. <a ...>) with the
    *   appropriate attributes
    *
    *   Method:     rowLinkGenerator
    *
    *   Parameters:
    *       rowRef          Object Reference
    *       isContentLink   Boolean
    *       isMenuToggle    Boolean
    *   Return: String
    *   UNICOMMENT -->
    */
    this.rowLinkGenerator = function (rowRef,isContentLink,isMenuToggle)
    {
        if (isContentLink) {
            var rowLink = rowRef.rowLink;
            var title = "Click here to view &quot;"+rowRef.text+"&quot;"
            if (rowRef.linkTarget != "")
            {
                var targetAttribute = "target=\""+rowRef.linkTarget+"\"";
            }
        }
        else {
            var rowLink = "javascript:void(null);"; 
            var title = "Click here to toggle this item between opened and closed."
            var targetAttribute = "";
        }
        if (isMenuToggle) {
            var onclickCall = "javascript:"+this.fromOutputToThis+".redisplay('"+rowRef.rowNumber+"');";
        }
        else {
            var onclickCall = "";
        }
        var aString = "<a href=\""+rowLink+"\" onclick=\""+onclickCall+"\" title=\""+title+"\" "+targetAttribute+"";
        // If trailing Icon and rollover info is set then add events to anchor to perform rollover
        if (isContentLink && rowRef.hasEndingImg && rowRef.trailingImgRolloverSrc != "" && this.onrolloverFunctionName != "")
        {
            aString += " onmouseover=\""+this.onrolloverFunctionName+"('trailingIcon"+tableID+rowRef.rowNumber+"','"+rowRef.trailingImgRolloverSrc+"')\"";
            aString += " onmouseout=\""+this.onrolloverFunctionName+"('trailingIcon"+tableID+rowRef.rowNumber+"','"+rowRef.trailingImgSrc+"')\"";
        }
        aString += ">";
        return aString;
    }
    
    /**
    *   <!-- UNICOMMENT
    *   addHeader sets the header property.
    *
    *   Method:     addHeader
    *
    *   Parameters:
    *       content   String
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.addHeader = function(content) {
        this.header = content;
    }

    /**
    *   <!-- UNICOMMENT
    *   addFooter sets the footer property.
    *
    *   Method:     addFooter
    *
    *   Parameters:
    *       content   String
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.addFooter = function(content) {
        this.footer = content;
    }

    /**
    *   <!-- UNICOMMENT
    *   redisplay recreates the Table with the appropriate expansion or
    *   contraction of a particular row element.
    *
    *   Method:     redisplay
    *
    *   Parameters:
    *       rowNumber   Integer
    *
    *   Return: String
    *   UNICOMMENT -->
    */
    this.redisplay = function(rowNumber)
    {
        this.rows[rowNumber].isExpanded = !(this.rows[rowNumber].isExpanded);
        this.rows[rowNumber].isVisited = true;

        if (this.rows[rowNumber].isContentLink)
            this.currLocation = this.rows[rowNumber].ordering;
        this.drawTree();
    }

    /**
    *   <!-- UNICOMMENT
    *   expandPath sets the Table to expand to a specified ordering String and
    *   redisplays the Table.
    *
    *   Method:     expandPath
    *
    *   Parameters:
    *       ordering   String
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.expandPath = function(ordering)
    {
        for (var i=0; i<this.rows.length; i++)
        {
            var rowRef = this.rows[i];
            if (this.inTOCPath(rowRef.ordering, ordering))
            {
                rowRef.isExpanded = true;
                rowRef.isVisited = true;
            }
        }
        this.currLocation = ordering;
        this.drawTree();
    }

    /**
    *   <!-- UNICOMMENT
    *   inTOCPath compares two ordering strings to determine if the first
    *   is in the path of the second.
    *
    *   Method:     inTOCPath
    *
    *   Parameters:
    *       currVal         String
    *       compareVal      String
    *
    *   Return: Boolean
    *   UNICOMMENT -->
    */
    this.inTOCPath = function(currVal, compareVal)
    {
        var currPath = new String(currVal);
        var comparePath = new String(compareVal);
    
        if (comparePath.substring(0,currPath.length) == currPath)
        {
            return true;
        }
        return false;
    }

}

/**
*   <!-- UNICOMMENT 
*   Class:     OnLoadRegistryObject  
*   The OnLoadRegistryObject provides a way to safely add additional onload
*   event listeners without knowing what other listeners may have already
*   have been specified.
*
*   Owner:      Shawn Lonas
*   Revision:   1.0
*
*   Purpose:    To safely add a new listener to the window.onload event
*   Design:     When a new object is created, it will copy any previous
*               listener reference into a new array, and replace the main
*               listener reference with a reference to a method that will call
*               the listeners in order.
*
*   Object Life Cycle:  Created as needed on the page.  Becomes obsolete after
*                       the onload event is over.
*
*   UNICOMMENT -->
*/
OnLoadRegistryObject = function ()
{
    var events = new Array();
    
    // If window.onload has an event listener already, add to events array
    if (window.onload != null)
    {
        events[events.length] = window.onload;
    }
    
    /**
    *   <!-- UNICOMMENT 
    *   add places a new listener on the onload event.
    *
    *   Method:     add
    *
    *   Parameters: 
    *       ref   Object Reference 
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.add = function(ref)
    {
        events[events.length] = ref;
        window.onload = this.handleEvent;
    }
    
    /**
    *   <!-- UNICOMMENT 
    *   handleEvent is the method that is called by the browser when the
    *   onload event occurs.  It calls all of the register events in the 
    *   events array.
    *
    *   Method:     handleEvent
    *
    *   Parameters: 
    *       e   Event Object Reference (Browser dependent) 
    *
    *   Return: void()
    *   UNICOMMENT -->
    */
    this.handleEvent = function(e)
    {
        for (var i=0; i< events.length; i++)
        {
            events[i](e);
        }
    }
}

/*
*   <!-- UNICOMMENT
*
*       Change History:
*
*   $Log: expandableTree.js,v $
*   Revision 1.1  2003/09/20 16:16:29  nbolton
*   adding balder107 code
*
*   Revision 1.8  2003/01/16 23:20:36  shawn
*   ADA fixes within expandable table
*
*   Revision 1.7  2002/12/13 00:56:06  shawn
*   GTR-16.0 client-side optimization
*
*   Revision 1.6  2002/11/19 03:02:18  mfreestone
*   no message
*
*   Revision 1.5  2002/11/13 20:34:07  gary
*   Fix font formatting
*
*   Revision 1.4  2002/11/11 22:34:10  shawn
*   Added support for Forum Channel expandable JS menu
*
*   Revision 1.2  2002/11/06 21:20:39  shawn
*   Fixed popup explanation for view and delete options
*
*   Revision 1.1  2002/11/06 18:34:34  shawn
*   JS to now handle the display of the class folders instead of the server.
*
*
*   UNICOMMENT -->
*/

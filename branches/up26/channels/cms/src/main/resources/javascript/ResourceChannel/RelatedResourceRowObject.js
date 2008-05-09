/**
*   <!-- UNICOMMENT
*   Class:     RelatedResourceRowObject
*   Will contain the properties need to describe a row in a collapsible,
*   expandable, tree table for the Related Resource Implementation.
*
*   Owner:      Shawn Lonas
*   Revision:   see above
*
*   Purpose:    A subclass of TreeRowObject that is specific to the Related
                Resource implementation.
*   Design:
*
*   Object Life Cycle:
*
*   UNICOMMENT -->
*/
RelatedResourceRowObject = function (ordering,text,rowLink,linkType,isExpanded,contentHash)
{
    var displayedOrdering = "";
    var isPF = false;
    var isVisited = false;
    var isCurrentPage = false;
    var iconOpen,iconClosed,hasEndingImg,linkTarget,
        trailingImgSrc,trailingImgRolloverSrc,trailingImgSelectedSrc,
        isLeadingIconMenuToggle,isTextMenuToggle,isTrailingIconMenuToggle,
        isLeadingIconContentLink,isTextContentLink,isTrailingIconContentLink;
    var iconSelected = "";
    //var isTextLink = true;
    var trailingContent = "";
    var iconWithChildrenCollapsed = "menu_root_plus.gif";
    var iconWithChildrenExpanded = "menu_root_minus.gif";
    var iconWithoutChildren = "menu_root_empty.gif";
    
    if (linkType == "folder")
    {
        isLeadingIconMenuToggle = true;
        isLeadingIconContentLink = false;
        isTextMenuToggle = true;
        isTextContentLink = false;
        isTrailingIconMenuToggle = false;
        isTrailingIconContentLink = false;
        //isContentLink = false;
        iconOpen = "folder_open_16.gif";
        iconClosed = "folder_closed_16.gif";
        hasEndingImg = false;
        trailingImgSrc = "";
        trailingImgRolloverSrc = "";
        trailingImgSelectedSrc = "";
        linkTarget = "";
    }
    else if (linkType == "file")
    {
        isLeadingIconMenuToggle = false;
        isLeadingIconContentLink = true;
        isTextMenuToggle = false;
        isTextContentLink = true;
        isTrailingIconMenuToggle = false;
        isTrailingIconContentLink = true;
        //isContentLink = true;
        iconOpen = "file.gif";
        iconClosed = "file.gif";
        hasEndingImg = true;
        trailingImgSrc = "channel_view_base.gif";
        trailingImgRolloverSrc = "channel_view_active.gif";
        trailingImgSelectedSrc = "channel_view_selected.gif";
        linkTarget = "_blank";
    }
    else if (linkType == "url")
    {
        isLeadingIconMenuToggle = false;
        isLeadingIconContentLink = true;
        isTextMenuToggle = false;
        isTextContentLink = true;
        isTrailingIconMenuToggle = false;
        isTrailingIconContentLink = true;
        //isContentLink = true;
        iconOpen = "url.gif";
        iconClosed = "url.gif";
        hasEndingImg = true;
        trailingImgSrc = "channel_view_base.gif";
        trailingImgRolloverSrc = "channel_view_active.gif";
        trailingImgSelectedSrc = "channel_view_selected.gif";
        linkTarget = "_blank";
    }

    if (contentHash.get("add"))
    {
        trailingContent += '<img height="1" width="3" src="'+RelatedResourceRowObject.imageSpacer+'" alt="" border="0"/>';
        trailingContent += '<a href="'+contentHash.get("add")+'" onmouseover="swapImage(\'resourceAddElement'+contentHash.get("id")+'\',\'channel_add_active.gif\')"';
        trailingContent += ' onmouseout="swapImage(\'resourceAddElement'+contentHash.get("id")+'\',\'channel_add_base.gif\')"';
        trailingContent += ' title="Click here to add a new element within &quot;'+text+'&quot; '+linkType+'.">';
        trailingContent += '<img border="0" src="'+RelatedResourceRowObject.imagePath+'/channel_add_base.gif" align="absmiddle"';
        trailingContent += ' alt="&quot;Add&quot; icon: to add a new element within &quot;'+text+'&quot; '+linkType+'."';
        trailingContent += ' name="resourceAddElement'+contentHash.get("id")+'" id="resourceAddElement'+contentHash.get("id")+'"/></a>';
    }

    if (contentHash.get("edit"))
    {
        trailingContent += '<img height="1" width="3" src="'+RelatedResourceRowObject.imageSpacer+'" alt="" border="0"/>';
        trailingContent += '<a href="'+contentHash.get("edit")+'" onmouseover="swapImage(\'resourceEditElement'+contentHash.get("id")+'\',\'channel_edit_active.gif\')"';
        trailingContent += ' onmouseout="swapImage(\'resourceEditElement'+contentHash.get("id")+'\',\'channel_edit_base.gif\')"';
        trailingContent += ' title="Click here to edit &quot;'+text+'&quot; '+linkType+'.">';
        trailingContent += '<img border="0" src="'+RelatedResourceRowObject.imagePath+'/channel_edit_base.gif" align="absmiddle"';
        trailingContent += ' alt="&quot;Edit&quot; icon: to edit &quot;'+text+'&quot; '+linkType+'."';
        trailingContent += ' name="resourceEditElement'+contentHash.get("id")+'" id="resourceEditElement'+contentHash.get("id")+'"/></a>';
    }

    if (contentHash.get("delete"))
    {
        trailingContent += '<img height="1" width="3" src="'+RelatedResourceRowObject.imageSpacer+'" alt="" border="0"/>';
        trailingContent += '<a href="'+contentHash.get("delete")+'" onmouseover="swapImage(\'resourceDeleteElement'+contentHash.get("id")+'\',\'channel_delete_active.gif\')"';
        trailingContent += ' onmouseout="swapImage(\'resourceDeleteElement'+contentHash.get("id")+'\',\'channel_delete_base.gif\')"';
        trailingContent += ' title="Click here to delete &quot;'+text+'&quot; '+linkType+'.">';
        trailingContent += '<img border="0" src="'+RelatedResourceRowObject.imagePath+'/channel_delete_base.gif" align="absmiddle"';
        trailingContent += ' alt="&quot;Delete&quot; icon: to delete &quot;'+text+'&quot; '+linkType+'."';
        trailingContent += ' name="resourceDeleteElement'+contentHash.get("id")+'" id="resourceDeleteElement'+contentHash.get("id")+'"/></a>';
    }
    
    var preloadImageArray = new Array();
    preloadImageArray[0] = new Image();
    preloadImageArray[0].src = RelatedResourceRowObject.iconSrcPath + "folder_open_16.gif";
    preloadImageArray[1] = new Image();
    preloadImageArray[1].src = RelatedResourceRowObject.iconSrcPath + "folder_closed_16.gif";
    preloadImageArray[2] = new Image();
    preloadImageArray[2].src = RelatedResourceRowObject.iconSrcPath + "file.gif";
    preloadImageArray[3] = new Image();
    preloadImageArray[3].src = RelatedResourceRowObject.iconSrcPath + "url.gif";
    preloadImageArray[4] = new Image();
    preloadImageArray[4].src = RelatedResourceRowObject.iconSrcPath + iconWithChildrenCollapsed;
    preloadImageArray[5] = new Image();
    preloadImageArray[5].src = RelatedResourceRowObject.iconSrcPath + iconWithChildrenExpanded;
    preloadImageArray[6] = new Image();
    preloadImageArray[6].src = RelatedResourceRowObject.iconSrcPath + iconWithoutChildren;
    
    // Inherit properties from the TreeRowObject
    this.inheritFrom = TreeRowObject;
    this.inheritFrom(ordering,displayedOrdering,text,rowLink,
            isPF,isVisited,isCurrentPage,isExpanded,iconOpen,iconClosed,
            iconSelected,hasEndingImg,trailingImgSrc,trailingImgRolloverSrc,
            trailingImgSelectedSrc,trailingContent,linkTarget,
            isLeadingIconMenuToggle,isTextMenuToggle,isTrailingIconMenuToggle,
            isLeadingIconContentLink,isTextContentLink,isTrailingIconContentLink,
            iconWithChildrenCollapsed,iconWithChildrenExpanded,iconWithoutChildren);

}

setRelatedResourceRowObjectStaticProperties = function()
{
    RelatedResourceRowObject.imagePath = ""; // Static Class Variable needed to build trailingContent
    RelatedResourceRowObject.imageSpacer = "";// Static Class Variable needed to build trailingContent
    RelatedResourceRowObject.iconSrcPath = "";
    RelatedResourceRowObject.trailingImgSrcPath = "";
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

RelatedResourceFunctions = ["initializeRelatedResources","RelatedResourceRowObject",
                            "setRelatedResourceRowObjectStaticProperties",
                            "TreeRowObject","TreeTableObject","postCommand"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = RelatedResourceFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [RelatedResourceFunctions]; // create 2-D array
}


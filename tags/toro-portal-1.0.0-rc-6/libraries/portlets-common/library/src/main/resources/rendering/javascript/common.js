// Function to check all checkboxes within a form
toggleAllCheckBoxes = function(formId,checkboxName)
{
    var ele;
    var anyUnchecked = false;
    var formRef = document.getElementById(formId);

    if (formRef && formRef[checkboxName])
    {
        for (var i=0; i<formRef[checkboxName].length; i++)
        {
            ele = formRef[checkboxName][i];
            if (ele.type == 'checkbox' && !ele.checked && !ele.disabled)
            {
                // If not checked, checked it and mark as having at least 1 unchecked
                anyUnchecked = true;
                ele.checked = true;
            } // end if element is a checkbox
        } // end loop thru checkboxes elements
        // If all were checked, then toggle all off
        if (!anyUnchecked)
        {
            for (i=0; i<formRef[checkboxName].length; i++)
            {
                ele = formRef[checkboxName][i];
                if (ele.type == 'checkbox' && !ele.disabled)
                {
                    ele.checked = false;
                } // end if element is a checkbox
            } // end loop thru checkboxes elements
        }   
        // If value attribute then only 1 checkbox. Toggle it directly.
        if (formRef[checkboxName].value && !formRef[checkboxName].disabled)
        {
            formRef[checkboxName].checked = !formRef[checkboxName].checked; // reverse checked status
        }

    } // end if form ref provided
    
} // end toggleAllCheckBoxes

// Alternate function to check all checkboxes for a column (controlled by checkbox itself)
checkBoxToggleAll = function (controlRef,formId,checkboxName)
{
    var checkedValue = controlRef.checked;
    var formRef = document.getElementById(formId);

    if (formRef && formRef[checkboxName] && formRef[checkboxName].length)
    {
        for (var i=0; i<formRef[checkboxName].length; i++)
        {
            formRef[checkboxName][i].checked = checkedValue;
        } // end loop thru checkboxes elements
    } // end if form ref provided
    else if (formRef[checkboxName]) 
    {
        formRef[checkboxName].checked = checkedValue;
    }
    
}

// Object to provide a namespace for AcademusApps specific JavaScript Functions
AcademusApps = {};

// Static Function that provides Highlight And Select On Click Behavior
// Expects refCss string containing a table className
// Attaches events to each TR of the TABLE to enable: 
//      1) a click of a row to "highlight" via "row-selected" CSS class
//      2) a click of a row to check each radio button within the row, unless it has a "disabled" className
AcademusApps.highlightAndSelectOnClick = function (refCss)
{
    var tables = YAHOO.util.Dom.getElementsByClassName(refCss, 'table'); 
    var onclickHandler = function (e, params)
    {
        var tableRef = params.table;
        var trRef = params.tr;
        var inputs = trRef.getElementsByTagName("input");
        for (var i=0; i<inputs.length; i++)
        {
            if (YAHOO.util.Dom.hasClass(inputs[i],"disabled")) return true;
            if (inputs[i].type='radio')
            {
               inputs[i].checked = true;
            }
        }
        // If current selected TR than just swap classes
        if (tableRef.currentSelectedTR)
        {
            var swapCSS = tableRef.currentSelectedTR.className;
            tableRef.currentSelectedTR.className = trRef.className;
            trRef.className = swapCSS;
            swapCSS = null;
            AcademusApps.replaceClass(trRef, "nohoverhighlight", "hoverhighlight");
            AcademusApps.replaceClass(tableRef.currentSelectedTR, "hoverhighlight", "nohoverhighlight");

        }
        // else add selected class to row
        else
        {
            YAHOO.util.Dom.addClass(trRef,"row-selected");
        }
        tableRef.currentSelectedTR = trRef;
    };
    
    for (var i=0; i<tables.length; i++)
    {
        var trs = tables[i].getElementsByTagName("tr");
        for (var ii=0; ii<trs.length; ii++)
        {
            YAHOO.util.Event.addListener(trs[ii], "click", onclickHandler, {"tr":trs[ii],"table":tables[i]}); 
            if (YAHOO.util.Dom.hasClass(trs[ii],"row-selected"))
            {
                tables[i].currentSelectedTR = trs[ii];
            }
        }
    }
};

// Static Function that only replaces a Class that already exists
// Yahoo default implementation of replaceClass will add newClass and delete oldClass (if it exists)
// This has the undesirable effect of always adding the newClass
AcademusApps.replaceClass = function (ele, oldClass, newClass)
{
    if(YAHOO.util.Dom.hasClass(ele, oldClass)) 
    {
        YAHOO.util.Dom.replaceClass(ele, oldClass, newClass);
    }
};

// Static Function that provides Highlight On Rollover Behavior
// Expects refCss string containing a table className
// Attaches events to each TR of the TABLE to enable a mouseover/mouseout hover effect
// This is necessary for IE as it only supports hover on <a/> elements
// However, for consistency sake, will use for all browsers.
AcademusApps.highlightOnRollover = function (refCss)
{
    //if (navigator.appName.indexOf("Internet Explorer") == -1) return true;

    var tables = YAHOO.util.Dom.getElementsByClassName(refCss, 'table'); 
    var onMouseOverHandler = function (e, tr)
    {
        AcademusApps.replaceClass(tr, "nohoverhighlight", "hoverhighlight");
    };
    
    var onMouseOutHandler = function (e, tr)
    {
        AcademusApps.replaceClass(tr, "hoverhighlight", "nohoverhighlight");
        window.status = tr.className;
    };

    for (var i=0; i<tables.length; i++)
    {
        var trs = tables[i].getElementsByTagName("tr");
        for (var ii=0; ii<trs.length; ii++)
        {
            YAHOO.util.Event.addListener(trs[ii], "mouseover", onMouseOverHandler, trs[ii]); 
            YAHOO.util.Event.addListener(trs[ii], "mouseout", onMouseOutHandler,  trs[ii]); 
        }
    }
    
};

// NOT USED CURRENTLY
// Static Function that provides Scroll To Selected Behavior
// Expects refCss string containing a table className
// The desired behavior is to mimic the anchor behavior.  Currently, unable to use anchor for element
// due to portal URL limitations.
// Unfortunately, without additional work it would have undesirable side-effects.
AcademusApps.scrollToSelected = function (refCss)
{
    var tables = YAHOO.util.Dom.getElementsByClassName(refCss, 'table');
    var lastAnchor = "";
    var onLoadHandler = function (e, lastAnchor)
    {
        window.location.hash = "#"+lastAnchor;
    };
    
    for (var i=0; i<tables.length; i++)
    {
        var as = tables[i].getElementsByTagName("a");
        for (var ii=0; ii<as.length; ii++)
        {
            if (as[ii].className == "hidden-anchor")
            {
                lastAnchor = as[ii].name;
            }
        }
    }
    if (lastAnchor != "")
    {
        YAHOO.util.Event.addListener(window, "load", onLoadHandler, lastAnchor); 
    }
};

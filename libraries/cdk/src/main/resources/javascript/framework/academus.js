function openBrWindow(theURL,winName,features) {
	var winRef = window.open(theURL,winName,features);
    winRef.focus();
}

function currentDateDisplay() {
// renders out the current date based on the client's computer's date information
	var months=new Array(13);
	months[1]='January';
	months[2]='February';
	months[3]='March';
	months[4]='April';
	months[5]='May';
	months[6]='June';
	months[7]='July';
	months[8]='August';
	months[9]='September';
	months[10]='October';
	months[11]='November';
	months[12]='December';
	var time=new Date();
	var lmonth=months[time.getMonth() + 1];
	var date=time.getDate();
	var year=time.getYear();
	if (year < 1000) {year = 1900 + year;}
	var text = lmonth + ' ' + date + ', ' + year;
	document.write(text);
}


function AcademusObject() 
{
    this.toggler = function (toToggleId, class1, class2) 
    {
        var e = document.getElementById(toToggleId);
        if (e && e.className && class1 && class2)
        {
            var classes = e.className.split(" ");
            var className;
            var newClassName = "";
            
            for (var i=0; i< classes.length; i++)
            {
                className = classes[i];
                if (className == class1)
                {
                    newClassName += class2 + " ";
                }
                else if (className == class2)
                {
                    newClassName += class1 + " ";
                }
                else
                {
                    newClassName += className + " ";
                }
            }
            e.className = newClassName;
        }
    }
    if (window.UniconFlyoutObject)
    {
        this.flyouts = new UniconFlyoutObject("Academus.flyouts");
    }
    if (window.openSpellChecker && !window.isAccessibleSkin)
    {
        // textareaIndex for creating Unique Id for textareas without ids
        var textareaIndex = 0;
        this.addSpellCheck = function(targetId)
        {
            targetRef = (targetId)? document.getElementById("HTMLBody_"+targetId):document;
            // If targetRef not found, may be SuperChannel Channel
            if (!targetRef)
            {
                var divs = document.getElementsByTagName("div");
                var targetDiv;
                for (var i=0; i< divs.length; i++)
                {
                    if (divs[i].id.indexOf("HTMLBody_") > -1 && divs[i].id.indexOf("_"+targetId) > -1)
                    {
                        targetRef = divs[i];
                        break;
                    }
                }
                
            }
            // Get all textareas
            if (targetRef && targetRef.getElementsByTagName && document.createElement)
            {
                var textareas = targetRef.getElementsByTagName("textarea");
                var ta, span, id;
                for (var i=0; i< textareas.length; i++)
                {
                    // For each textarea, insert a link to the SpellChecker directly after the textarea
                    ta = textareas[i];
                    // Insert Spell Check link only if: 
                    // 1) display of textarea is not set to "none"
                    // 2) it is a tinyMCE textarea
                    // 3) page has HTMLArea, and other characteristics that match the HTMLArea editor
                    if (ta.style.display != "none" || (window.tinyMCE && ta.className.indexOf("mceEditor") > -1) || (window.HTMLArea && ta.previousSibling && ta.previousSibling.nodeName.toLowerCase() == "div") && ta.previousSibling.getElementsByTagName("iframe")[0])
                    {
                        span = document.createElement('span');
                        id = ta.getAttribute("id");
                        if (!id || id == "")
                        {
                            textareaIndex++;
                            id = "AcademusTextArea"+textareaIndex;
                            ta.id = id;
                        }
                        span.innerHTML = '<a href="javascript:openSpellChecker(\''+id+'\');" title="To check and fix the spelling for this field." class="spellcheck">Spell Check</a>';
                        if (ta.nextSibling)
                        {
                            ta.parentNode.insertBefore(span,ta.nextSibling);
                        }
                        else
                        {
                            ta.parentNode.appendChild(span);
                        }
                    }
                }
            }
        }
    }

}
var Academus = new AcademusObject();

//variables that hold image and skin paths for static html pages in iframes
var skinPathTop = portalMediaPath+'/'+portalSkin+'/css/main.css';
var imagePathTop = portalMediaPath+'/'+portalSkin+'/images/icons/';

function imgSrcRewrite(scopeId){
    if (scopeId && scopeId != "") 
    { 
        scopeRef = window.document.getElementById(scopeId); 
    }
    else 
    { 
        scopeRef = window.document; 
    }
    var imageArray = scopeRef.getElementsByTagName("img");
    var imageArrayLength = imageArray.length;
    for (i=0;i <= (imageArrayLength-1);i++){
        var imgSrc = imageArray[i].src;
        var splitSrc = imgSrc.split('/');
        var img = splitSrc[splitSrc.length-1];
        if (img.search("channel_") != -1) {
            imageArray[i].src = (imagePathTop + img);
        }
    }
    var inputArray = scopeRef.getElementsByTagName("input");
    var inputArrayLength = inputArray.length;
    for (k=0; k <= (inputArrayLength-1); k++) {
        if (inputArray[k].type == "image") {
            var inpImg = inputArray[k].src;
            var splitInpSrc = inpImg.split("/");
            var inp = splitInpSrc[splitInpSrc.length-1];
            if (inp.search("channel_") != -1) {
                inputArray[k].src = (imagePathTop + inp);
            }
        }
    }
}

window.swapImage=function() {}// placeholder to prevent error when page loading
    
function on_load() {
    //initiate image path writer to get specified images to change with skin
    imgSrcRewrite();
    
    window.swapImage=function(imageTarget,imageSrc,useNoImgPath)
    {
        var imagePath = (useNoImgPath)?"":imagePathTop;
        if (document.getElementById && document.getElementById(imageTarget))
        {
            document.getElementById(imageTarget).src = imagePath+imageSrc;
        }
    }

    if (window.top != window) 
    {
        window.top.portalMediaPath = portalMediaPath;
        window.top.portalSkin = portalSkin;
        window.top.isDisplayReloaded = true;
    }
    else 
    {
        focusOnForm();
    }
    if (Academus.flyouts)
    {
        Academus.flyouts.InitializeFlyouts();
    }
    if (Academus.addSpellCheck)
    {
        Academus.addSpellCheck();
    }
}
//window.onload = on_load;

window.focusOnForm = function (channelId)
{
    if (!channelId)
    {
        if (window.location.href.indexOf("userLayoutRootNode.uP") == -1)
        {
            var firstURLPart = window.location.href.split(".uP")[0];
            var dotURLParts = firstURLPart.split(".");
            channelId = dotURLParts[dotURLParts.length-1];
        }
    }
    // If channel specified in URL, focus its text field
    if (channelId)
    {
        
        var targetDiv = document.getElementById("HTMLBody_"+channelId);
        // If targetDiv not found, may be SuperChannel Channel
        if (!targetDiv)
        {
        var divs = document.getElementsByTagName("div");
        for (var i=0; i< divs.length; i++)
        {
                if (divs[i].id.indexOf("HTMLBody_") > -1 && divs[i].id.indexOf("_"+channelId) > -1)
            {
                targetDiv = divs[i];
                break;
            }
        }

        }
        
        if (targetDiv)
        {
            var forms = targetDiv.getElementsByTagName("form");
            if (!findFormFieldToFocus(forms))
            {
                var links = targetDiv.getElementsByTagName("a");
                if (links.length > 0)
                {
                    try
                {
                    links[0].focus();
                }
                    catch(e)
                    {
                        targetDiv.focus();
                    }
                }
                else
                {
                    targetDiv.focus();
                }
            }
        }
        else
        {
            window.focus();
        }
    }
    // Else If Any Forms, Focus Text Field of Login or First Channel
    else if (window.document.forms.length > 0)
    {
        var divs = document.getElementsByTagName("div");
        var targetDiv;
        for (var i=0; i< divs.length; i++)
        {
            if (divs[i].id.indexOf("HTMLBody") > -1 || divs[i].id.indexOf("loginContainer") > -1)
            {
                targetDiv = divs[i];
                break;
            }
        }
       
        if (targetDiv)
        {
            var forms = targetDiv.getElementsByTagName("form");
            if (!findFormFieldToFocus(forms))
            {
                var links = targetDiv.getElementsByTagName("a");
                if (links.length > 0)
                {
                    try
                {
                    links[0].focus();
                }
                    catch(e)
                    {
                        window.focus();
                    }
                }
                else
    {
                    window.focus();
                }
            }
        }
        else
        {
            window.focus();
        }

    }
    // Else Focus Window
    else
    {
        window.focus();
    }
}

window.findFormFieldToFocus = function(forms)
{
    var elements;
    var found = false;
    for (var i=0; !found && i<forms.length; i++ )
    {
        elements = forms[i].elements;
        for (var ii=0; ii<elements.length; ii++ )
        {
            if (elements[ii].type == "text" || elements[ii].type == "textarea" || elements[ii].type == "file" || elements[ii].type == "password")
            {
                elements[ii].focus();
                elements[ii].focus();// Necessary to duplicate to fix IE bug
                found = true;
                break;
            }
        }
    }
    
    return found;

}

function openBrWindow(theURL,winName,features) {
    var newwin;
    if (winName.indexOf("detachedChannel") > -1 && window != window.top)
    {
        newwin = window.open("main.html?mode=detachedChannel&currentURL="+escape(theURL),winName,features);
    }
    else
    {
        newwin = window.open(theURL,winName,features);
        
    }
    newwin.focus();
}


window.checkForNewHref = function ()
{
    if (window.locationhref)
    {
        if (window != window.top && window.locationhref.indexOf("worker.cscr")>-1)
        {
            window.top.frames[1].location.href = window.locationhref;
            window.locationhref = null;
        }
        else
        {
            location.href = window.locationhref;
            window.locationhref = null;
        }
    }
}
checkForNewHrefIntervalRef = setInterval("window.checkForNewHref();",200)

window.loadURL = function(newhref)
{
    if(window != window.top && newhref.indexOf("worker.cscr")>-1)
    {
        window.top.frames[1].location.href = newhref;
    }
    else
    {
        location.href = newhref;
    }
}


// Hide content from view until onLoad is finished if appropriate
if (window.top != window)
{
    var styleOutput = '<style type="text/css">#bodyContent{position:absolute;top:-5000px;}</style>';
    document.writeln(styleOutput);
}

window.appendParameterToAction = function(formRef,parameters)
{
    var action = formRef.action;
    if (action.indexOf("?") > -1)
    {
        var parts = action.split("?");
        var firstpart = parts[0];
        var otherparts = parts.slice(1).join("?");
        action = firstpart + "?" + parameters; 
        if (otherparts != "")
        {
            action += "&" + otherparts;
        }
    }
    else
    {
        action += "?" + parameters;
    }
    formRef.action = action;
    return action;    
}

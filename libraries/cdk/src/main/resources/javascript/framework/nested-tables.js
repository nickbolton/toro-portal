function openBrWindow(theURL,winName,features) {window.open(theURL,winName,features);}

//variables that hold image and skin paths for static html pages in iframes
var skinPathTop = portalMediaPath+'/'+portalSkin+'/skin/'+portalSkin+'.css';
var imagePathTop = portalMediaPath+'/'+portalSkin+'/controls/';

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
    var loc = new String(document.location);
    var targetChannelParam = 'targetChannel=';
    var targetPos = loc.indexOf(targetChannelParam);
    var channelPos;
    var targetChannel;

    if ((loc.indexOf('navigating=true') == -1) && targetPos >= 0) {
        targetChannel = loc.substr(targetPos+targetChannelParam.length);
        channelPos = targetChannel.indexOf('&');
        if (channelPos >= 0) {
            targetChannel = targetChannel.substr(0,channelPos);
        }
        try
        {
        document.location = '#'+targetChannel;
        }
        catch(e)
        {
            //alert("Error encountered!");
        }
    }
    //initiate image path writer to get specified images to change with skin
    imgSrcRewrite();
    
    window.swapImage=function(imageTarget,imageSrc)
    {
        if (document.getElementById && document.getElementById(imageTarget))
        {
            document.getElementById(imageTarget).src = 'media/org/jasig/portal/layout/tab-column/nested-tables/'+portalSkin+'/controls/'+imageSrc;
        }
    }

    if (window.top != window) 
    {
        window.top.portalMediaPath = portalMediaPath;
        window.top.portalSkin = portalSkin;
        window.top.isDisplayReloaded = true;
    }
       
    if(document.forms[0] && document.forms[0].userName)
    {
        document.forms[0].userName.focus();
    }
}

function openBrWindow(theURL,winName,features) {
    if (winName == "detachedChannel" && window != window.top)
    {
        var newwin = window.open("main.html?mode=detachedChannel&currentURL="+escape(theURL),winName,features);
    }
    else
    {
        window.open(theURL,winName,features);
    }
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

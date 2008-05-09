window.loadURL = function(newhref)
{
    location.href = newhref;
}

window.checkForNewHref = function ()
{
    if (window.locationhref)
    {
        location.href = window.locationhref;
        window.locationhref = null;
    }
}
checkForNewHrefIntervalRef = setInterval("window.checkForNewHref();",200);

window.loadURL = function(newhref)
{
    location.href = newhref;
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

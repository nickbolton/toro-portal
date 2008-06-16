//textCount.js
AnnouncementRulesObject = function ()
{
    this.name = "AnnouncementRulesObject";
    // Message rule
    this.messageRules = new Object();
    this.messageRules.field = null;
    this.messageRules.prompt = 'Please limit input to 300 characters.';
    this.messageRules.method = 'checkTextCount';
    this.messageRules.type = '';
    this.messageRules.isEmptyOk = false;
    // rulesRef collection that holds all the field rules
    this.rules = new SimpleObject(
        "message",this.messageRules);
    
    this.checkTextCount = function(fieldRule)
    {
        var addedText = fieldRule.field.value;
        if (!fieldRule.isEmptyOk)
        {
            if (addedText.length > 300)
            {
                alert('Please limit input to 300 characters.');
                fieldRule.field.focus();
                fieldRule.field.select();
                return false;
            }
            else if (addedText.length == 0)
            {
                alert('Please enter a Message.');
                fieldRule.field.focus();
                fieldRule.field.select();
                return false;
            }
        }
        return true;
    }
} // end AnnouncementRulesObject

announceRules = new AnnouncementRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
AnnouncementChannelFunctions = ["initAnnouncementChannel","AnnouncementRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = AnnouncementChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [AnnouncementChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initAnnouncementChannel = function()
{
    window.announceRules = new AnnouncementRulesObject();
}

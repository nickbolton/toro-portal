//autoForm.js
SendNotificationRulesObject = function()
{
    this.name = "SendNotificationRulesObject";
    
    // Message rule 
    this.messageRules = new Object();
    this.messageRules.field = null;
    this.messageRules.prompt = 'Please enter a Message.';
    this.messageRules.type = "text";
    this.messageRules.isEmptyOk = false;
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "message",this.messageRules);

} // end SendNotificationRulesObject

notificationRules = new SendNotificationRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

SendNotificationChannelFunctions = ["initializeSendNotificationChannel",
                                    "SendNotificationRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = SendNotificationChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [SendNotificationChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeSendNotificationChannel = function()
{
    window.notificationRules = new SendNotificationRulesObject();
}


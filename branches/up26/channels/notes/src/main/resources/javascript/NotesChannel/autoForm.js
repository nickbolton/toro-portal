//autoForm.js
NotesRulesObject = function()
{
    this.name = "NotesRulesObject";

    // Message rule 
    this.messageRules = new Object();
    this.messageRules.field = null;
    this.messageRules.prompt = 'Please enter a Message.';
    this.messageRules.method = 'checkTextCount';
    this.messageRules.type = "";
    this.messageRules.isEmptyOk = false;

    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "message",this.messageRules);

    this.checkTextCount = function(fieldRule)
    {
        var addedText = fieldRule.field.value;
        if (!fieldRule.isEmptyOk)
        {
            if (addedText.length > 600)
            {
                alert('Please limit input to 600 characters.');
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

} // end NotesRulesObject

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
NotesChannelFunctions = ["initializeNotesChannel", "NotesRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = NotesChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [NotesChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeNotesChannel = function()
{
    // no initialization needed
}

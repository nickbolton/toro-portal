//autoForm.js
NotePadRulesObject = function()
{
    this.name = "NotePadRulesObject";
    
    // Message rule 
    this.messageRules = new Object();
    this.messageRules.field = null;
    this.messageRules.prompt = 'Please enter a Note.';
    this.messageRules.method = 'checkTextCount';
    this.messageRules.type = "";
    this.messageRules.isEmptyOk = false;
    // Title rule 
    this.titleRules = new Object();
    this.titleRules.field = null;
    this.titleRules.prompt = "Please enter a Title.";
    this.titleRules.type = "text";
    this.titleRules.isEmptyOk= false;
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "message",this.messageRules,
        "title",this.titleRules);

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
                alert('Please enter a Note.');
                fieldRule.field.focus();
                fieldRule.field.select();
                return false;
            }
        }
        return true;
    }

} // end NotePadRulesObject

notePadRules = new NotePadRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

NotepadChannelFunctions = ["initializeNotepadChannel","NotePadRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = NotepadChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [NotepadChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeNotepadChannel = function()
{
    // no initialization needed
}

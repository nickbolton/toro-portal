//autoForm.js
GradebookRulesObject = function()
{
    this.name = "GradebookRulesObject";
    
    // Activation Comment rule 
    this.commentRules = new BaseRulesObject("text",null,'Please enter a Comment.',true);
    // Attempts Permitted rule 
    this.attemptsRules = new BaseRulesObject(null,"checkPositiveInteger","Please enter a non-negative integer for the number of attempts permitted.");
    // entryURL Rules
    this.entryURLRules = new BaseRulesObject(null,"checkLinkTypeForURL");
    this.checkLinkTypeForURL = function(fieldRule)
    {
        // if "url" radio is checked
        if (this.entryLinkRules.field.form.entryLink[1].checked)
        {
            fieldRule.prompt = validator.iURL;
            return validator.checkURL(fieldRule);
        }
        else return true;
    }
    // uploaded Files Rules
    this.uploadedFileRules = new BaseRulesObject(null,"checkLinkTypeForFile");
    this.checkLinkTypeForFile = function(fieldRule)
    {
        // if "file" radio is checked
        if (this.entryLinkRules.field.form.entryLink[2].checked)
        {
            fieldRule.prompt = "Please enter a file to upload.";
            return validator.checkString(fieldRule);
        }
        else return true;
    }
    
    this.entryLinkRules = new BaseRulesObject();
    
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "entryLink",this.entryLinkRules,
        "attempts",this.attemptsRules,
        "comment",this.commentRules,
        "entryURL",this.entryURLRules,
        "uploadedFile",this.uploadedFileRules);
    
    this.checkPositiveInteger = function(fieldRule)
    {
        emptyOK = fieldRule.isEmptyOk;
        if ((emptyOK == true) && 
            (validator.isEmpty(fieldRule.field.value))) return true;
        if (!validator.isInteger(fieldRule.field.value)) 
           return validator.warnInvalid(fieldRule.field, fieldRule.prompt);
        if (!validator.isPositiveInteger(fieldRule.field.value)) 
           return validator.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

} // end GradebookRulesObject 

GradebookChannelFunctions = 
    ["initializeGradebookChannel","GradebookRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = 
        GradebookChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [GradebookChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeGradebookChannel = function()
{
    // no initialization needed
}

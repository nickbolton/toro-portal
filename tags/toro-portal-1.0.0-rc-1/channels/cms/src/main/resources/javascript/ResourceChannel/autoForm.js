//autoForm.js
ResourcesRulesObject = function()
{
    this.name = "ResourcesRulesObject";
    
    // Title rule 
    this.titleRules = new BaseRulesObject("validchars",null,'Please enter a Title.',false);
    this.titleRules.sizeLimit = 254;
    this.titleRules.excludeList = "&><+|}{[]\”:;’?/‘";
    // Description rule 
    this.descRules = new BaseRulesObject("text",null,'Please enter a Description.',false);
    // URL rule 
    this.urlRules = new BaseRulesObject("url",null,'Please enter a valid URL.',false);
    // entryURL Rules
    this.entryURLRules = new BaseRulesObject(null,"checkLinkTypeForURL");
    this.checkLinkTypeForURL = function(fieldRule)
    {
        // if second entryLink radio is checked then it is URL
        if (this.form.type[1].checked)
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
        // if last entryLink radio is checked then it is File
        if (this.form.type[2].checked)
        {
            fieldRule.prompt = "Please enter a file to upload.";
            return validator.checkString(fieldRule);
        }
        else return true;
    }
    
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "name",this.titleRules,
        "description",this.descRules,
        "desc",this.descRules,
        "hyperlink",this.entryURLRules,
        "uploadedFile",this.uploadedFileRules,
        "entryURL",this.urlRules);

} // end ResourcesRulesObject

resourcesRules = new ResourcesRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

RelatedResourceFunctions = ["initializeRelatedResources","ResourcesRulesObject"];

// Need to add to list of functions depending on what other functions may have
// been added (depending on the channel view)                            
if (window.checkFormRRC)
{
    RelatedResourceFunctions[RelatedResourceFunctions.length] = "checkFormRRC";
}                            
if (window.selectRadioRC)
{
    RelatedResourceFunctions[RelatedResourceFunctions.length] = "selectRadioRC";
}
if (window.checkFormEditRRC)
{
    RelatedResourceFunctions[RelatedResourceFunctions.length] = "checkFormEditRRC";
}

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

// create initialize method so that it can be initialized outside of page load
initializeRelatedResources = function()
{
    window.resourcesRules = new ResourcesRulesObject();
}

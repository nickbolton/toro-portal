//autoForm.js
ImportRulesObject = function()
{
    this.name = "ImportRulesObject";
    
    // import-file rule 
    this.importFileRules = new Object();
    this.importFileRules.field = null;
    this.importFileRules.prompt = 'Please enter a file to import.';
    this.importFileRules.type = "text";
    this.importFileRules.isEmptyOk = false;
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "import-file",this.importFileRules);

} // end ImportRulesObject

importRules = new ImportRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

ImportChannelFunctions = ["initializeImportChannel",
                            "ImportRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = ImportChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [ImportChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeImportChannel = function()
{
    window.importRules = new ImportRulesObject();
}


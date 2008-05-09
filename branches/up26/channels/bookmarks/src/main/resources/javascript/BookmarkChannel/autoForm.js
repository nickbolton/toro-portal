//autoForm.js
BookmarkRulesObject = function()
{
    this.name = "BookmarkRulesObject";

    // BookmarkDescription rule 
    //this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description.',false);
    //this.descriptionRules.sizeLimit = 250;
    // BookmarkTitle rule 
    this.bookmarkTitleRules = new BaseRulesObject('text',null,'Please enter a title for this bookmark.',false);
    this.bookmarkTitleRules.sizeLimit = 80;
    // FolderTitle rule 
    this.folderTitleRules = new BaseRulesObject('text',null,'Please enter a name for this folder.',false);
    this.folderTitleRules.sizeLimit = 80;
    // BookmarkURL rule 
    this.bookmarkUrlRules = new BaseRulesObject('url',null,'Please enter a valid URL.',false);
    this.bookmarkUrlRules.sizeLimit = 80;
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(        
        "FolderTitle",this.folderTitleRules,
        "BookmarkURL",this.bookmarkUrlRules,
        "BookmarkTitle",this.bookmarkTitleRules);

		//"BookmarkDescription",this.descriptionRules, ### NOW OPTIONAL
} // end BookmarkRulesObject

checkBookmarkFormSubmit = function (formRef) {
    if(formRef.isCancel) {
        return true;
    } else {
        return validator.applyFormRules(formRef, new BookmarkRulesObject());
    }
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
BookmarkChannelFunctions = ["initializeBookmarkChannel","BookmarkRulesObject","checkBookmarkFormSubmit"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = BookmarkChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [BookmarkChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeBookmarkChannel = function()
{
    // no initialization needed
}

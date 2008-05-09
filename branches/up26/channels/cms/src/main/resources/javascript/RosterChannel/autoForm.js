//autoForm.js
RosterRulesObject = function()
{
    this.name = "RosterRulesObject";

    // searchValue1 rule 
    this.search1Rules = new Object();
    this.search1Rules.field = null;
    this.search1Rules.prompt = 'Please enter a search value.';
    this.search1Rules.type = "text";
    this.search1Rules.isEmptyOk = false;
    // searchValue2 rule 
    this.search2Rules = new Object();
    this.search2Rules.field = null;
    this.search2Rules.prompt = 'Please enter a search value.';
    this.search2Rules.type = "text";
    this.search2Rules.isEmptyOk = false;
    // searchFirstName rule 
    this.searchFNameRules = new Object();
    this.searchFNameRules.field = null;
    this.searchFNameRules.prompt = 'Please enter a First Name.';
    this.searchFNameRules.type = "text";
    this.searchFNameRules.isEmptyOk = true;
    // searchLastName rule 
    this.searchLNameRules = new Object();
    this.searchLNameRules.field = null;
    this.searchLNameRules.prompt = 'Please enter a Last Name.';
    this.searchLNameRules.type = "text";
    this.searchLNameRules.isEmptyOk = true;
    // firstName rule 
    this.fnameRules = new Object();
    this.fnameRules.field = null;
    this.fnameRules.prompt = 'Please enter a First Name.';
    this.fnameRules.type = "text";
    this.fnameRules.isEmptyOk = false;
    // lastName rule 
    this.lnameRules = new Object();
    this.lnameRules.field = null;
    this.lnameRules.prompt = 'Please enter a Last Name.';
    this.lnameRules.type = "text";
    this.lnameRules.isEmptyOk = false;
    // userID rule 
    this.useridRules = new Object();
    this.useridRules.field = null;
    this.useridRules.prompt = 'Please enter a user ID.';
    this.useridRules.type = "text";
    this.useridRules.isEmptyOk = false;
    // import-file rule 
    this.importFileRules = new Object();
    this.importFileRules.field = null;
    this.importFileRules.prompt = 'Please enter a file to import.';
    this.importFileRules.type = "text";
    this.importFileRules.isEmptyOk = false;
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "searchValue1",this.search1Rules,
        "searchValue2",this.search2Rules,
        "import-file",this.importFileRules,
        "searchFirstName",this.searchFNameRules,
        "firstName",this.fnameRules,
        "searchLastName",this.searchLNameRules,
        "lastName",this.lnameRules,
        "userID",this.useridRules);

} // end RosterRulesObject

rosterRules = new RosterRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

RosterChannelFunctions = ["initializeRosterChannel","RosterRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = RosterChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [RosterChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeRosterChannel = function()
{
    window.rosterRules = new RosterRulesObject();
}

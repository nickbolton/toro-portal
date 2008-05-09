//autoForm.js
CommonRulesObject = function()
{
    this.name = "CommonRulesObject";

    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject();

} // end CommonRulesObject

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
CommonFunctions = ["initializeCommon", "CommonRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = CommonFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [CommonFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeCommon = function()
{
}

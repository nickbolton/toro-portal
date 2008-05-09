//autoForm.js
RoleAdminRulesObject = function(ref)
{
    this.name = "RoleAdminRulesObject";

	// roleName rule 
    this.nameRules = new BaseRulesObject('validchars',null,'Please enter a title for this role.',false);
    this.nameRules.sizeLimit = 35;
    this.nameRules.excludeList = "&><+|}{[]\”:;’?,./‘";

	// groupName rule 
    this.groupNameRules = new BaseRulesObject('validchars',null,'Please enter a title for this role.',false);
    this.groupNameRules.sizeLimit = 35;
    this.groupNameRules.excludeList = "&><+|}{[]\”:;’?,./‘";

    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "role_name",this.nameRules,
        "group_name",this.groupNameRules);

     return true;
} // end RoleAdminRulesObject

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
PermissionsChannelFunctions = ["initializePermissionsChannel",
                               "RoleAdminRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = PermissionsChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [PermissionsChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializePermissionsChannel = function()
{
    // no initialization needed
}

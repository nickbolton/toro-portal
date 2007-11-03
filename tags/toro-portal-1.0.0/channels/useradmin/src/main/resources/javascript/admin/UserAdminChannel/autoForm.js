//autoForm.js

UserAdminRulesObject = function()
{
    this.name = "UserAdminRulesObject";
	// user_name rule 
    //this.usernameRules = new BaseRulesObject("text",null,'Please enter a User Name.');
    this.usernameRules = new BaseRulesObject("validchars",null,'Please enter a User Name.');
    this.usernameRules.excludeList = "&><+|}{[]\”:;’?,./‘";
    // entry type rule
    this.entryTypeRules = new BaseRulesObject(); // needed for convenient way to access radio button
    // password rule
    this.passwordRules = new BaseRulesObject(null,"checkEntryTypeForPassword");
    this.otherPasswordRules = new BaseRulesObject("text",null,"Please enter a valid password");
    
    checkEntryTypeForPassword = function (fieldRule)
    {
        // if last radio is checked then it is Password
        if (this.entryTypeRules.field.checked)
        {
            fieldRule.prompt = "Please enter a Password.";
            return validator.checkString(fieldRule);
        }
        else return true;
    }
    // first_name rule 
    this.fnameRules = new BaseRulesObject("validchars",null,'Please enter a First Name.');
    this.fnameRules.excludeList = "&><+|}{[]\”:;’?,./‘";
    // last_name rule 
    this.lnameRules = new BaseRulesObject("validchars",null,'Please enter a Last Name.');
    this.lnameRules.excludeList = "&><+|}{[]\”:;’?,./‘";
    // email rule 
    this.emailRules = new BaseRulesObject("email",null,'Please enter a valid Email Address.');
    this.emailRules.excludeList = "&><+";

    // rulesRef collection that holds all the field rules 
    
    this.rules = new SimpleObject(
        "entry_type",this.entryTypeRules,
        "user_name",this.usernameRules,
        "password",this.passwordRules,
        "first_name",this.fnameRules,
        "last_name",this.lnameRules,
        "email",this.emailRules,
        "currentPassword",this.otherPasswordRules,
        "verifiedPassword",this.otherPasswordRules,
        "newPassword",this.otherPasswordRules
        );

} // end UserAdminRulesObject

userAdminRules = new UserAdminRulesObject();

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
UserAdminChannelFunctions = ["initializeUserAdminChannel",
                            "UserAdminRulesObject", "isNumber"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = UserAdminChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [UserAdminChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeUserAdminChannel = function()
{
    window.userAdminRules = new UserAdminRulesObject();
}

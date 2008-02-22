// autoForm.js
AddressbookRulesObject = function()
{
    this.name = "AddressbookRulesObject";

    // AddressbookValue rule 
    this.invalidCharsRules = new BaseRulesObject('validchars',null,null,true);
    this.invalidCharsRules.excludeList = "|";

    this.nameRules = new BaseRulesObject('validchars',null,null,false);
    this.nameRules.excludeList = "|";

	this.fileTypeRules = new BaseRulesObject('validfiletype',null,null,true);
	this.fileTypeRules.excludeList = ".jpg .gif .bmp .png .mpg .tiff .jpeg .mpeg";

    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "name",this.nameRules,
        "email",this.invalidCharsRules,
        "mobile",this.invalidCharsRules,
        "title",this.invalidCharsRules,
        "company",this.invalidCharsRules,
        "department",this.invalidCharsRules,
        "business-phone",this.invalidCharsRules,
        "fax",this.invalidCharsRules,
        "office-address",this.invalidCharsRules,
        "home-phone",this.invalidCharsRules,
        "home-address",this.invalidCharsRules,
        "notes",this.invalidCharsRules,
        "foldername",this.invalidCharsRules,
		"file",this.fileTypeRules);

} // end AddressbookRulesObject 

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
AddressbookChannelFunctions = ["initializeAddressbookChannel",
                            "AddressbookRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = AddressbookChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [AddressbookChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeAddressbookChannel = function()
{
    // no initialization is needed here
}

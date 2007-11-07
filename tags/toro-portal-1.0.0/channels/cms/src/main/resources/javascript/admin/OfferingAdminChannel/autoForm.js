//autoForm.js
OfferingAdminRulesObject = function()
{
    this.name = "OfferingAdminRulesObject";

    // offeringDescription rule 
    this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description for this offering.',false);
    // offeringName rule 
    this.nameRules = new BaseRulesObject('validchars',null,'Please enter a name for this offering.',false);
    this.nameRules.sizeLimit = 80;
    this.nameRules.excludeList = "&><+|}{[]\”:;’?,./‘";
    // import-file rule 
    this.importFileRules = new BaseRulesObject('text',null,'Please enter a file to import.',false);
    // offeringMonthStartParam rule 
    this.startMonthRules = new BaseRulesObject(null,'checkOfferingDateRange','Please enter a valid date range.',false);

    // rulesRef collection that holds all the field rules 
    /*this.rules =
    {
        "offeringDescription":this.descriptionRules,
        "offeringName":this.nameRules,
        "import-file":this.importFileRules,
        "offeringIdParam":this.optIdRules,
        "offeringMonthStartParam":this.startMonthRules
    };*/
    this.rules = {};
    this.rules.offeringDescription = this.descriptionRules;
    this.rules.offeringName = this.nameRules;
    var tempPropertyName = "import-file";
    this.rules[tempPropertyName] = this.importFileRules;
    this.rules.offeringIdParam = this.optIdRules;
    this.rules.offeringMonthStartParam = this.startMonthRules;

    this.checkOfferingDateRange = function(fieldRule)
    {

        var startMonth = 
            this.form.offeringMonthStartParam[
                this.form.offeringMonthStartParam.selectedIndex].value;

        var startDay = 
            this.form.offeringDayStartParam[
                this.form.offeringDayStartParam.selectedIndex].value;

        var startYear = 
            this.form.offeringYearStartParam[
                this.form.offeringYearStartParam.selectedIndex].value;

        var endMonth = 
            this.form.offeringMonthEndParam[
                this.form.offeringMonthEndParam.selectedIndex].value;

        var endDay = 
            this.form.offeringDayEndParam[
                this.form.offeringDayEndParam.selectedIndex].value;

        var endYear = 
            this.form.offeringYearEndParam[
                this.form.offeringYearEndParam.selectedIndex].value;

        if ( !(validator.isDate(startYear, startMonth, startDay)
            && validator.isDate(endYear, endMonth, endDay)) )
        {
            alert(fieldRule.prompt);
            return false;
        }

        var startDate = new Date();
        startDate.setMonth(parseInt(startMonth - 1));
        startDate.setDate(parseInt(startDay));
        startDate.setFullYear(parseInt(startYear));

        var endDate = new Date();
        endDate.setMonth(parseInt(endMonth - 1));
        endDate.setDate(parseInt(endDay));
        endDate.setFullYear(parseInt(endYear));

        if (!validator.isValidDateRange(startDate,endDate))
        {
           alert(fieldRule.prompt);
           return false;
        }

        return true;

    } // end checkDateRange

} // end OfferingAdminRulesObject

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
OfferingAdminChannelFunctions = ["initializeOfferingAdminChannel",
                            "OfferingAdminRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = OfferingAdminChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [OfferingAdminChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeOfferingAdminChannel = function()
{
    window.offeringAdminRules = new OfferingAdminRulesObject();
}

//autoForm.js
CalendarRulesObject = function()
{
    this.name = "CalendarRulesObject";

    // description rule 
    this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description.',true);
    this.descriptionRules.sizeLimit = 500;

    // event rule 
    this.eventTitleRules = new BaseRulesObject('text',null,'Please enter a title for this Event.',false);
    this.eventTitleRules.sizeLimit = 100;

    // todo rule 
    this.todoTitleRules = new BaseRulesObject('text',null,'Please enter a title for this task.',false);
    this.todoTitleRules.sizeLimit = 100;

    // place rule 
    this.placeRules = new BaseRulesObject('text',null,'Please enter a place.',true);
    this.placeRules.sizeLimit = 500;

    // emails rule 
    this.emailRules = new BaseRulesObject('email',null,'Please enter valid email address(es).',true);
    this.emailRules.sizeLimit = 100;

    // Date Range rule 
    this.dateRangeRules = new BaseRulesObject(null,'checkCalendarDateRange','Please enter a valid date range.',false);

    // Date rule 
    this.dateRules = new BaseRulesObject(null,'checkCalendarDate','Please select a valid date.',false);

    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "description",this.descriptionRules,
        "event",this.eventTitleRules,
        "todo",this.todoTitleRules,
        "place",this.placeRules,
        "emails",this.emailRules,
        "month1",this.dateRangeRules,
        "month",this.dateRules);

    this.checkCalendarDate = function(fieldRule)
    {
        var startMonth = 
            this.form.month[
                this.form.month.selectedIndex].value;
        var startDay = 
            this.form.day[
                this.form.day.selectedIndex].value;
        var startYear = 
            this.form.year[
                this.form.year.selectedIndex].value;

        if ( !(validator.isDate(startYear, startMonth, startDay)) )
        {
            alert(fieldRule.prompt);
            return false;
        }

        return true;
    } // end checkCalendarDate

    this.checkCalendarDateRange = function(fieldRule)
    {
        var startMonth = 
            this.form.month1[
                this.form.month1.selectedIndex].value;
        var startDay = 
            this.form.day1[
                this.form.day1.selectedIndex].value;
        var startYear = 
            this.form.year1[
                this.form.year1.selectedIndex].value;
        var endMonth = 
            this.form.month2[
                this.form.month2.selectedIndex].value;
        var endDay = 
            this.form.day2[
                this.form.day2.selectedIndex].value;
        var endYear = 
            this.form.year2[
                this.form.year2.selectedIndex].value;

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
    } // end checkCalendarDateRange

} // end CalendarRulesObject

function checkFormCalC(formRef){
    submitValue = formRef.submitValue.value;
    if (submitValue == "OK") {
        return validator.applyFormRules(formRef, new CalendarRulesObject());
    } else {
        return true;
    }
} // end checkFormCalC

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
CalendarChannelFunctions = ["initializeCalendarChannel","CalendarRulesObject","checkFormCalC"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = CalendarChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [CalendarChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeCalendarChannel = function()
{
    // no initialization needed
}

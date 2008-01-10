//autoForm.js
NewsAdminRulesObject = function()
{
    this.name = "NewsAdminRulesObject";

    // Content rule 
    this.contentRules = new BaseRulesObject('text',null,'Please enter a news story.',false);
    this.contentRules.sizeLimit = 30000;
    // Abstract rule 
    this.abstractRules = new BaseRulesObject('text',null,'Please enter an abstract.',false);
    this.abstractRules.sizeLimit = 500;
    // Title rule 
    this.titleRules = new BaseRulesObject('text',null,'Please enter a title.',false);
    // Description rule 
    this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description for this topic.',true);
    this.descriptionRules.sizeLimit = 250;
    // newTopicName rule 
    this.nameRules = new BaseRulesObject('text',null,'Please enter a name for this topic.',false);
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "topic_description",this.descriptionRules,
        "topic",this.nameRules,
        "title",this.titleRules,
        "abstract",this.abstractRules,
        "content",this.contentRules);

} // end TopicAdminRulesObject

checkNewsAdminFormSubmit = function (formRef) {
	submitValue = formRef.submitValue.value;
    if (submitValue == "Next") {
        return validator.applyFormRules(formRef, new NewsAdminRulesObject());
    } else {
        return true;
    }
}

/* Toggle News Descriptions/Abstracts on main page */
toggleNewsDescriptions = function (isChecked,formRef)
{
    var searchClassRegEx, replaceClass;
    if (isChecked)
    {
        searchClassRegEx = /hide-description/;
        replaceClass = "show-description";
    }
    else
    {
        searchClassRegEx = /show-description/;
        replaceClass = "hide-description";
    }
    if (formRef.getElementsByTagName)
    {
        var ps = formRef.getElementsByTagName("p");
        for (var i=0; i < ps.length; i++)
        {
            if (ps[i].className)
            {
                ps[i].className = ps[i].className.replace(searchClassRegEx,replaceClass)
            }
        }
    }
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
NewsAdminChannelFunctions = ["initializeNewsAdminChannel","NewsAdminRulesObject","checkNewsAdminFormSubmit","toggleNewsDescriptions"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = NewsAdminChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [NewsAdminChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeNewsAdminChannel = function()
{
    // no initialization needed
}

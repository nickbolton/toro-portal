//autoForm.js
TopicAdminRulesObject = function()
{
    this.name = "TopicAdminRulesObject";

    // topicDescription rule 
    this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description for this topic.',false);
    // topicName rule 
    this.nameRules = new BaseRulesObject('validchars',null,'Please enter a name for this topic.',false);
    this.nameRules.sizeLimit = 80;
    this.nameRules.excludeList = "&><+|}{[]\”:;’?,./‘";
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "topicDescription",this.descriptionRules,
        "topicName",this.nameRules);
} // end TopicAdminRulesObject

checkTopicAdminFormSubmit = function (formRef, selectParentCommand) {
    var command = formRef.command.value;
    if (command == selectParentCommand) {
        return true;
    } else if (formRef.parentGroupId.value == '') {
        alert('Please select a parent topic.');
        return false;
    } else {
        return validator.applyFormRules(formRef, new TopicAdminRulesObject());
    }
}

//stripWhitespace = function(s) { 
//    return s.replace(/[ \t\n\r]+/,'');
//}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
TopicAdminChannelFunctions = ["initializeTopicAdminChannel","TopicAdminRulesObject","checkTopicAdminFormSubmit"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = TopicAdminChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [TopicAdminChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeTopicAdminChannel = function()
{
    window.topicAdminRules = new TopicAdminRulesObject();
}

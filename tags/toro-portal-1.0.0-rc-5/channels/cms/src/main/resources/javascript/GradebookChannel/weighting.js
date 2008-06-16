stripWhitespace = function(s) {
    return s.replace(/[ \t\n\r]+/,'');
}

isNumber = function(val) {
    if (val == null) return false;
    if (val.search(/^[0-9]+$/) != 0) return false;
    return true;
}

recordOnChange = function(ref)
{
    var value = stripWhitespace(ref.value);
    if (value != '') {
        if (!isNumber(value)) {
            alert('Invalid number entered. Please enter a non-negative integer for the weight.');
            return;
        }
    }
    var changeLog = document.gradebookForm.changeLog.value;
    var testString = ref.name+"|";

    if (changeLog.indexOf(testString)==-1)
    {
        changeLog += testString;
    }
    document.gradebookForm.changeLog.value = changeLog;
    updateTotalWeight();
}

updateTotalWeight = function()
{
    var totalWeight=0;
    var fEle = document.gradebookForm.elements;
    for(var i=0; i< fEle.length; i++)
    {
        if (fEle[i].name.indexOf("weight_") != -1)
        {
            totalWeight += parseInt(fEle[i].value);
        }
    }
    document.gradebookForm.totalWeight.value = totalWeight;
}
GradebookWeightingRulesObject = function(formRef)
{
    var fe; // form element ref
    this.name = "GradebookWeightingRulesObject";
    // rules map 
    this.rules = {}; 
    
    // Weight rule (must be a non-negative Integer)
    this.checkWeight = function(fieldRule)
    {
        // return true if it is a nonnegative integer, else false.  No messaging is required.
        var isScoreOK = validator.isNonnegativeInteger(fieldRule.field.value,fieldRule.isEmptyOk);
        if (!isScoreOK)
        {
            fieldRule.field.focus(); // focus failing element
            fieldRule.field.select(); // select text in failing element
        }
        return isScoreOK;
    }
    
    for(var i=0; i<formRef.elements.length; i++)
    {
        fe = formRef.elements[i];
        if (fe.name.indexOf("weight_")!=-1)
        {
            this.rules[fe.name]= new BaseRulesObject(null,"checkWeight","",true);
        }
    }
} // end GradebookWeightingRulesObject 


// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

GradebookChannelFunctions = ["initializeGradebookChannel","stripWhitespace",
                             "recordOnChange","isNumber","updateTotalWeight",
                             "GradebookWeightingRulesObject"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = GradebookChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [GradebookChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeGradebookChannel = function()
{
    // no initialization necessary
}

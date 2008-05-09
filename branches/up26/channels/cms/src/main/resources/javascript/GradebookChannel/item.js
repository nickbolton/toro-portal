stripWhitespace = function(s) 
{
    return s.replace(/[ \t\n\r]+/,'');
}

checkSubmit = function() 
{
    var formElement;
    var name;
    var value;

    for (var i=0; i < document.gradebookForm.elements.length; i++) {
        formElement = document.gradebookForm.elements[i];
        if (!formElement.name || formElement.name.indexOf("SCORE_") < 0) continue;
        value = stripWhitespace(formElement.value);
        if (formElement.value == '') continue;
        if (isNaN(parseInt(value)) || (parseInt(value).toString() != value) || value < 0) {
            alert('Invalid number entered.  Please enter only positive integers.');
            formElement.focus();
            return false;
        }
    }
    return true;
}

isNumber = function(val) 
{
    if (val == null) return false;
    if (val.search(/^[0-9]+$/) != 0) return false;
    return true;
}

recordOnChange = function(ref)
{
    var value = stripWhitespace(ref.value);
    if (value != '') {
        if (isNaN(parseInt(value)) || (parseInt(value).toString() != value) || value < 0) {
            alert('Invalid number entered.  Please enter only positive integers.');
            ref.focus();
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
    updateMeanMedian(ref);
}



updateMeanMedian = function(ref)
{
    var totalScore = 0;
    var testVal = 0;
    var scoreArray = new Array();
    var splitArray = ref.name.split("_"); // temp array to hold names split by _
    var colnum = parseInt(splitArray[splitArray.length-1]);
    for (var i=0; i < document.gradebookForm.elements.length; i++)
    {
        var formElement = document.gradebookForm.elements[i];
        if (formElement.name && formElement.name.indexOf("SCORE_") != -1)
        {
            splitArray = formElement.name.split("_");
            if (colnum == parseInt(splitArray[splitArray.length-1]))
            {
                testVal = formElement.value;
                if ((testVal != "") || !isNaN(parseInt(testVal)))
                {
                    totalScore += parseInt(testVal);
                    scoreArray[scoreArray.length] = parseInt(testVal);
                }
            }
        }
    }
    if (scoreArray.length != 0)
    {
        document.gradebookForm["Mean_"+colnum].value = totalScore/(scoreArray.length);
        scoreArray.sort(numericalSort);
        var mod = scoreArray.length % 2;
        var medianValue = 0;
        if (mod == 0)
        {
            //even number of elements in the array
            var lowmiddle = (scoreArray.length / 2) - 1;
            var highmiddle = scoreArray.length / 2;
            var low = scoreArray[lowmiddle];
            var high = scoreArray[highmiddle];
            medianValue = (low + high) / 2;
           
        }
        else
        {
            //odd number of elements in the array
            var middle = (scoreArray.length / 2) - .5;
            medianValue = scoreArray[middle];
        }
        document.gradebookForm["Median_"+colnum].value = medianValue;

    }
    else
    {
        document.gradebookForm["Mean_"+colnum].value = "";
        document.gradebookForm["Median_"+colnum].value = "";
    }
}

numericalSort = function(a,b)
{
    return a - b;
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

GradebookChannelFunctions = ["initializeGradebookChannel","stripWhitespace",
                            "checkSubmit","isNumber","recordOnChange",
                            "updateMeanMedian","numericalSort"];

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

// initialize method is specified in the XSL with dynamic parameters
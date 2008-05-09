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
            alert('Invalid number entered.  Please enter only positive integers.');
            return;
        }
    }
    if(document.gradebookForm.changeLog)
    {
    var changeLog = document.gradebookForm.changeLog.value;
    var testString = ref.name+"|";

    if (changeLog.indexOf(testString)==-1)
    {
        changeLog += testString;
    }
    document.gradebookForm.changeLog.value = changeLog;
    updateMeanMedian(ref);
    }
}

changeTotalScore = function(ref)
{
    var value = stripWhitespace(ref.value);
    var origScoreRef = document.gradebookForm.original_score;
    var modifierRef = document.gradebookForm.modifier;
    if (value != '') {
        if (!isNumber(value)) {
            alert('Invalid number entered.  Please enter only positive integers.');
            return;
        }
        else if(value < 0) {
            alert('Negative number entered.  Please enter only positive integers.');
            return;
        }
    }
    modifierRef.value = value - origScoreRef.value;
    recordOnChange(ref);    
}

changeScoreModifier = function(ref)
{
    var value = parseInt(stripWhitespace(ref.value));
    var origScoreRef = document.gradebookForm.original_score;
    var totalScoreRef = document.gradebookForm.score;
    if (value != '') {
        if (isNaN(value)) {
            alert('Invalid number entered.  Please enter only integers.');
            return;
        }
        else if ((parseInt(origScoreRef.value) + parseInt(value))<0)
        {
            alert('A negative total score is not permitted.  Please enter a different modifier.');
            return;
        }
    }
    totalScoreRef.value = parseInt(origScoreRef.value) + parseInt(value);
    recordOnChange(totalScoreRef);    
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
        document.gradebookForm["Mean_"+colnum].value = parseInt((totalScore/(scoreArray.length))*10)/10;
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

GradebookScoresRulesObject = function(formRef)
{
    var fe; // form element ref
    this.name = "GradebookScoresRulesObject";
    
    // Submission/Feedback comment Rules
    this.commentRules = new BaseRulesObject("text",null,"",true);
    
    // rules map with entry for single score (Details View)
    this.rules = new SimpleObject(
        "score",new BaseRulesObject(null,"checkScore","",true),
        "submission-comment",this.commentRules,
        "feedback-comment",this.commentRules);
    
    // Scores rule (must be a non-negative Integer)
    this.checkScore = function(fieldRule)
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
        if (fe.name.indexOf("SCORE_")!=-1)
        {
            this.rules[fe.name]= new BaseRulesObject(null,"checkScore","",true);
        }
    }
} // end GradebookScoresRulesObject 

// another so that client-side optimizations would work

GradebookChannelFunctions = ["initializeGradebookChannel","stripWhitespace",
                            "isNumber","recordOnChange","updateMeanMedian",
                            "numericalSort","GradebookScoresRulesObject",
                            "changeTotalScore","changeScoreModifier"];

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

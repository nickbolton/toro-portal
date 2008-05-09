stripWhitespace = function(s) 
{
    return s.replace(/[ \t\n\r]+/,'');
}

checkSubmit = function() 
{
    var form = document.gradebookForm;
    if (form.item_name.value == null || stripWhitespace(form.item_name.value) == '') {
        alert('Please enter a name for this item.');
        return false;
    }
    if (form.min_score.value == null || stripWhitespace(form.min_score.value) == '') {
        form.min_score.value = '0';
    }
    if (!isNumber(form.min_score.value)) {
        alert('Invalid minimum score entered.');
        return false;
    }
    if (form.max_score.value == null || stripWhitespace(form.max_score.value) == '') {
        form.max_score.value = '0';
    }
    if (!isNumber(form.max_score.value)) {
        alert('Invalid maximum score entered.');
        return false;
    }
    if (form.weight.value == null || stripWhitespace(form.weight.value) == '') {
        form.weight.value = '0';
    } else if (!isNumber(form.weight.value)) {
        alert('Invalid weight entered.');
        return false;
    }
    var diff = form.max_score.value - form.min_score.value;
    if (diff < 0) {
        alert('The max value cannot be less than the min value.');
        return false;
    }
    return true;
}

isNumber = function(val) 
{
    if (val == null) return false;
    if (val.search(/^[0-9]+$/) != 0) return false;
    return true;
}

onlineAsmtAssociationCheck = function(ref)
{
    var dgbf=document.gradebookForm;
    if(ref.selectedIndex > 0)
    {
        if(dgbf.item_name.value=="")
        {
            dgbf.item_name.value=ref.options[ref.selectedIndex].text;
        }
        dgbf.max_score.value='';
        dgbf.max_score.onfocus=onlineAsmtMaxScoreBlur;
        dgbf.max_score.className='text-disabled';
    }
    else
    {
        dgbf.max_score.onfocus=null;
        dgbf.max_score.className='text';
    }
}

onlineAsmtMaxScoreBlur = function()
{
    document.gradebookForm.max_score.blur();
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

GradebookChannelFunctions = ["initializeGradebookChannel","stripWhitespace",
                             "checkSubmit","isNumber","onlineAsmtAssociationCheck",
                             "onlineAsmtMaxScoreBlur"];

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
    // no initialization needed
}



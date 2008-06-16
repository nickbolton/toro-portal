//autoForm.js

selectRadioCC = function(ref){
    //alert(ref);
    var inputTypeNum = document.curriculumForm.type.length;
    //alert(inputTypeNum);
    if (inputTypeNum > 2)
    {
        if(ref==document.curriculumForm.curriculumURL){
            document.curriculumForm.type[1].checked = true;
            ref.focus();
            return true;
        }
        if(ref==document.curriculumForm.uploadedFile){
            document.curriculumForm.type[2].checked = true;
            document.curriculumForm.curriculumURL.value='http://';
            ref.focus();
            return true;
        }
    }
    else if(inputTypeNum <= 2){
        if(ref==document.curriculumForm.curriculumURL){
            document.curriculumForm.type[0].checked = true;
            ref.focus();
            return true;
        }
        if(ref==document.curriculumForm.uploadedFile){
            document.curriculumForm.type[1].checked = true;
            document.curriculumForm.curriculumURL.value='http://';
            ref.focus();
            return true;
        }
    }
    return true;
}


checkFormCC = function(ref)
{    
    var hyperlinkValue = ref.curriculumURL.value;
    var uploadedFileValue = ref.uploadedFile.value;
    var titleValue = ref.title.value;
    var typeValue;
    //alert('old');
    
    //find the type of element selected
    for(i=0;i < ref.type.length;i++){
        if(ref.type[i].checked){
            typeValue = ref.type[i].value;
        }
    }
        
    //display directions
    if (typeValue == null)
    {
        alert('Please select the type of reference for the added curriculum.');
        return false;
    }
    if (typeValue=='url' && !validator.isURL(hyperlinkValue, false))
    {
        alert('Please fill in the "Link to URL" field.');
        return false;
    }
    
    if (typeValue=='file' && uploadedFileValue == "") {
        alert('Please fill in the "Uploaded File" field.');
        return false;
    }

    var invalidChars = validator.stripCharsNotInBag(uploadedFileValue, '&'); 
    if (typeValue=='file' && invalidChars != "") {
        alert('The file name contains the following invalid characters: '
            + invalidChars);
        return false;
    }

    if (titleValue == "")
    {
        alert('Please fill in the "Name" field.');
        return false;
    }    
    
    return true;
}

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

CurriculumChannelFunctions = ["initializeCurriculumChannel","selectRadioCC","checkFormCC"];

// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = CurriculumChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [CurriculumChannelFunctions]; // create 2-D array
}

// create initialize method so that it can be initialized outside of page load
initializeCurriculumChannel = function()
{
    // no initialization needed
}


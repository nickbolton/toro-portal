//checkFormRRC.js

checkFormRRC = function(ref)
{    
    var hyperlinkValue = ref.hyperlink.value;
    var uploadedFileValue = ref.uploadedFile.value;
    var titleValue = ref.name.value;
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
        if(document.ResourcesForm.hyperlink != '' || document.ResourcesForm.hyperlink !='http://'){
            document.ResourcesForm.type[1].checked = true;
               return true;
        }
       else if(document.ResourcesForm.uploadedFile != ''){
            document.ResourcesForm.type[2].checked = true;
            document.ResourcesForm.hyperlink.value='http://';
               return true;
        }
        
        document.ResourcesForm.type[1].checked = true;
        
    }
    if (typeValue=='url' && hyperlinkValue == "http://")
    {
        alert('Please fill in the "Link to URL" field.');
        document.ResourcesForm.hyperlink.focus();
        return false;
    }
    
    if (typeValue=='file' && uploadedFileValue == "")
    {
        alert('Please fill in the "Uploaded File" field.');
        return false;
    }
    if (typeValue=='folder')
    {
        document.ResourcesForm.hyperlink.value='http://';
    }
    if (titleValue == "")
    {
        alert('Please fill in the "Title" field.');
        document.ResourcesForm.name.focus();
        return false;
    }    
    
    return true;
}

selectRadioRC = function(ref){
    //alert(ref);
    if(ref==document.ResourcesForm.hyperlink){
        document.ResourcesForm.type[1].checked = true;
        ref.focus();
           return true;
    }
       if(ref==document.ResourcesForm.uploadedFile){
        document.ResourcesForm.type[2].checked = true;
        document.ResourcesForm.hyperlink.value='http://';
        ref.focus();
           return true;
    }
    return true;
}

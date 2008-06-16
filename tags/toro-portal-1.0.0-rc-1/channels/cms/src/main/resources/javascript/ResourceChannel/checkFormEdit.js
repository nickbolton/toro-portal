//checkFormEditRRC.js

checkFormEditRRC = function(ref,typeValue)
{    
    var titleValue = ref.name.value;
    var typeValue;
    //alert('new');
    
    
    if (typeValue=='url' && ref.entryURL.value == "")
    {
        alert('Please fill in the "Link to URL" field. Remember to begin with "http://"');
        return false;
    }

    if (titleValue == "")
    {
        alert('Please fill in the "Title" field.');
        return false;
    }    
    
    return true;
}

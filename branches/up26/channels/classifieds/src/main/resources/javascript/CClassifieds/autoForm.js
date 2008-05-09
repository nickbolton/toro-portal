//autoForm.js
CClassifiedsRulesObject = function()
{
    this.name = "CClassifiedsRulesObject";

    // Message rule 
    this.messageRules = new BaseRulesObject('text',null,'Please enter a message for this classified.',false);
    this.messageRules.sizeLimit = 2500;
    // Phone rule 
    //this.phoneRules = new BaseRulesObject('phone',null,validator.iUSPhone,false);
    this.phoneRules = new BaseRulesObject(null,"phoneRulesTypeCheck");
    this.phoneRulesTypeCheck = function(fieldRule)
    {
        /* Alternate version to validate some elements of phone number field */
        /*
        if (this.phoneRules.field.value != "")
        {
            var intlphn = /^[+]?[\d .()-]*([xX]|[eE][xX][tT]|[eE][xX][tT][.])?[\d]{0,5}$/;
            var isValid = intlphn.exec(this.phoneRules.field.value);
            if (!isValid)
            {
                alert("Please format the phone number to contain only the following characters:\n.-() 0123456789\n\nNOTE:\nThe number may start with a '+', and may have up to a five digit extension at the end of the number preceded by an 'x', 'ext', or 'ext.' (regardless of case).\n\nEXAMPLES:\n+1 352 669-9999 x12345\n1.352.669.9999 X1234\n669 9999 Ext.1\next12345");
                this.phoneRules.field.focus();
                return false;                
            }
        }
        */
        return true;
    }
    // Email rule 
    //this.emailRules = new BaseRulesObject('email',null,validator.iEmail,false);
    this.emailRules = new BaseRulesObject(null,"emailRulesTypeCheck");
    this.emailRulesTypeCheck = function(fieldRule)
    {
        var isSomethingFilledIn = false;
        if (this.phoneRules.field.value != "")
        {
            isSomethingFilledIn = true;
        }
        if (this.emailRules.field.value != "")
        {
            isSomethingFilledIn = true;
            fieldRule.prompt = validator.iEmail;
            return validator.checkEmail(fieldRule);
        }
        if (!isSomethingFilledIn)
        {
            alert("Please enter either a Phone Number or an Email Address.");
            return false;
        }
        return true;
        
    }
    // Description rule 
    this.descriptionRules = new BaseRulesObject('text',null,'Please enter a description for this topic.',false);
    this.descriptionRules.sizeLimit = 100;
    // newTopicName rule 
    this.nameRules = new BaseRulesObject('text',null,'Please enter a name for this topic.',false);
    // Image Rules
    this.imageRules = new BaseRulesObject(null,"imageRulesTypeCheck");
    this.imageRulesTypeCheck = function(fieldRule)
    {
        var fileExtPattern = /[.](jpg|JPG|jpeg|JPEG|gif|GIF|png|PNG|bmp|BMP)$/;
        var whiteSpacePattern = /^(\s)*$/;
        var value = fieldRule.field.value;
        if(value=="" || fileExtPattern.test(value) || whiteSpacePattern.test(value))
        {
            return true;
        }
        else
        {
            alert("Please limit image formats to .jpg, .gif, .png, or .bmp files");
            return false;
        }
    }
    // Icon image rules (uses normal image rules)
    this.iconImageRules = new BaseRulesObject(null,"imageRulesTypeCheck");
    
    // rulesRef collection that holds all the field rules 
    this.rules = new SimpleObject(
        "message",this.messageRules,
        "phone",this.phoneRules,
        "email", this.emailRules,
        "description",this.descriptionRules,
        "classified_image",this.imageRules,
        "icon_image",this.iconImageRules,
        "newTopicName",this.nameRules);

} // end TopicAdminRulesObject
CClassifiedsRulesObject.prototype.isCancel=false;

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
CClassifiedsChannelFunctions = ["initializeCClassifiedsChannel","CClassifiedsRulesObject"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = CClassifiedsChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [CClassifiedsChannelFunctions]; // create 2-D array
}
// create initialize method so that it can be initialized outside of page load
initializeCClassifiedsChannel = function()
{
    // Set prototype flag to specify whether Cancel button was pressed
    // Will be set dynamically to true when Cancel button is pressed and
    // form is submitted still
    CClassifiedsRulesObject.prototype.isCancel=false;
}

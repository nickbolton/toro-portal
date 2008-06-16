modValidate = function (formRef,isPassOnly)
{
    var buttonpressRef = window.buttonpress;
    var defaultButton;

    if (window.buttonpress && window.buttonpress.value=="Cancel")
    {
        var defaultActionId = formRef.id+"_defaultAction";
        var defaultActionRef = document.getElementById(defaultActionId);
        defaultActionRef.name = buttonpressRef.name;
        defaultActionRef.value = buttonpressRef.value;
        return true;
    }
    else
    {
        window.buttonpress = null;
    }
    
	if (!formRef.validatorInitialized)
	{
		for (var i=0; i< formRef.elements.length; i++)
		{
			var e = formRef.elements[i];
			e.validator = new FormElementValidationObject(e,formRef);
		}
		formRef.validatorInitialized = true;
	}
    // If tinyMCE HTML WYSIWYG editor is initialized then make sure values are copied 1st, if applicable
    var hasTinyMCE = false;
	if (window.tinyMCE)
    {
        for (var i=0; i< formRef.elements.length; i++)
        {
            if(formRef.elements[i].className.indexOf("mceEditor") > -1)
            {
                hasTinyMCE = true;
                break;
            }
        }
        if (hasTinyMCE)
        {
            tinyMCE.triggerSave();
        }
    }
    
    // Check all Implied Values First
	for (var i=0; i< formRef.elements.length; i++)
	{
		var e = formRef.elements[i];
		var v = e.validator;
		if (v.isImplied)
		{
			var impliedRefName = e.name + "_" + e.value;
			var impliedRef = formRef[impliedRefName];
			if (impliedRef.value == "")
			{
		    	e.checked = false;
			}
			else
			{
				e.checked = true;
			}
		}
	}
    // If is Pass (Values) Only (i.e. no validation), then return true
    if (isPassOnly)
    {
        return true;
    }
    // If no-validate class (function like Pass Only) on button pressed then set default Action and return true
    if (buttonpressRef && buttonpressRef.className && buttonpressRef.className.indexOf("no-validate") > -1)
    {
        var defaultActionId = formRef.id+"_defaultAction";
        var defaultActionRef = document.getElementById(defaultActionId);
        defaultActionRef.name = buttonpressRef.name;
        defaultActionRef.value = buttonpressRef.value;
        return true;
    }
    // Loop thru all elements to validate as needed
	for (var i=0; i< formRef.elements.length; i++)
	{
		var e = formRef.elements[i];
		var v = e.validator;
        var isValid = true;
        var targetElement;
        // If Required
		if (v.isRequired)
		{
			isValid = v.validate();
            if (!isValid)
            {
                return false;
            }
		}
        // If Minimum is needed
		if (v.minimum > 0)
		{
			var optionsRef = formRef[e.name];
            var selectedCount = 0;
            if (e.type == "checkbox")
            {
                for (var ii=0; ii< optionsRef.length; ii++)
                {
                    if (optionsRef[ii].checked)
                    {
                        selectedCount++;
                    }
                }
            }
            // If Minimum is not met
            if (selectedCount < v.minimum)
            {
                if (v.isImplied)
                {
                    var impliedRefName = e.name + "_" + e.value;
                    var impliedRef = formRef[impliedRefName];
                    if (impliedRef.type="File")
                    {
                        alert("You must select at least "+v.minimum+" file(s) to upload.");
                    }
                    else
                    {
                        alert("You must fill in at least "+v.minimum+" of these options.");
                    }
                    impliedRef.focus();
                    return false;
                }
                else
                {
                    alert("You must select at least "+v.minimum+" options.");
                    e.focus();
                    return false;
                }
            }
		}
        // If Optional (i.e. not required but must be in correct format and size if supplied)
        if (v.isOptional && e.value!="")
        {
            isValid = v.validate();
            if (!isValid)
            {
                return false;
            }
        }
        // If toValidate populated (i.e. triggers other fields to validate)
        if (v.toValidate.length > 0 && (e.checked || ((e.type=="text" || e.type=="file" || e.type=="password" || e.type=="textarea") && e.value != "")))
        {
            for (var ii=0; ii<v.toValidate.length; ii++)
            {
                targetElement = v.toValidate[ii];
                if (targetElement && targetElement.validator)
                {
                    isValid = targetElement.validator.validate();
                    if (!isValid)
                    {
                        break;
                    }
                }
            }
            if (!isValid)
            {
                return false;
            }
            
        }
 
	}
   
    // If no buttonpressed, then identify the default button action to use 
    if (!buttonpressRef)
    {
        for (var i=0; i< formRef.elements.length; i++)
        {
            var e = formRef.elements[i];
            if (e.type == "button" && (!defaultButton || e.className.indexOf("form-button-emphasis")>-1))
            {
                defaultButton = e;
            }
        }
        // If valid default button found, add to form action the name-value of the default button
        if (defaultButton)
        {
            var defaultActionId = formRef.id+"_defaultAction";
            var defaultActionRef = document.getElementById(defaultActionId);
            if (defaultActionRef)
            {
                defaultActionRef.name = defaultButton.name;
                defaultActionRef.value = defaultButton.value;
                //formRef.action += "&"+defaultButton.name+"="+defaultButton.value;
            }
        }
    }
    else
    {
        var defaultActionId = formRef.id+"_defaultAction";
        var defaultActionRef = document.getElementById(defaultActionId);
        defaultActionRef.name = buttonpressRef.name;
        defaultActionRef.value = buttonpressRef.value;
        //formRef.action += "&"+defaultButton.name+"="+defaultButton.value;
    }

    return true;
	
}

nonblank = function(e)
{
	if (e.value == "")
	{
		alert("This field may not be blank.  Please fill it in.");
		e.focus();
		return false;
	}
    else
    {
        return true;
    }
}

TypeText = function(e)
{
    var maxsize = e.validator.maxsize;
	if (e.value == "")
	{
        var labels = document.getElementsByTagName("label");
        var labelRef;
        for (var i=0; i< labels.length; i++)
        {
            if (labels[i].htmlFor == e.id)
            {
                labelRef = labels[i];
            }
        }
        if (labelRef)
        {
            alert("The field '"+labelRef.innerHTML+"' may not be blank. Please fill it in.");
        }
        else
        {
		    alert("This field may not be blank.  Please fill it in.");
        }
		e.focus();
		return false;
	}
    else if (e.value.length > maxsize)
    {
		alert("The text for this field may not exceed "+maxsize+" characters.  Please constrain your answer to that limit.");
		e.focus();
		return false;
    }
    else
    {
        return true;
    }
}

TypeHTMLFilter = function(e)
{
	if (e.value == "")
	{
        var labels = document.getElementsByTagName("label");
        var labelRef;
        for (var i=0; i< labels.length; i++)
        {
            if (labels[i].htmlFor == e.id)
            {
                labelRef = labels[i];
            }
        }
        if (labelRef)
        {
            alert("The field '"+labelRef.innerHTML+"' may not be blank. Please fill it in.");
        }
        else
        {
		    alert("This field may not be blank.  Please fill it in.");
        }
		e.focus();
		return false;
	}
    else
    {
        return true;
    }
}

modValidationFormats = {
"nonblank":nonblank,
"TypeText":TypeText,
"TypePassword":TypeText,
"TypeHTMLFilter":TypeHTMLFilter,
"TypeFileUpload":nonblank
};

FormElementValidationObject = function(e,formRef)
{
    this.e = e;
	this.isRequired = false;
	this.isOptional = false;
	this.isTrigger = false;
	this.isImplied = false;
	this.minimum = 0;
	this.format = "nonblank";
    this.maxsize = 0;
    this.toValidate = [];
	var thisRef = this;

    var classes = e.className.split(" ");
    var className;
	
	for (var ii=0; ii< classes.length; ii++)
    {
        className = classes[ii];
        if (className == "required")
        {
			this.isRequired = true;
        }
		else if (className == "optional")
		{
			this.isOptional = true;
		}
		else if (className == "trigger")
		{
			this.isTrigger = true;
		}
		else if (className == "implied")
		{
			this.isImplied = true;
		}
		else if (className.indexOf("format_") > -1)
		{
			this.format = className.substring(7);
		}
		else if (className.indexOf("maxsize_") > -1)
		{
			this.maxsize = parseInt(className.substring(8));
		}
		else if (className.indexOf("minimum_") > -1)
		{
			this.minimum = parseInt(className.substring(8));
		}
		else if (className.indexOf("validate_") > -1)
		{
			this.toValidate[this.toValidate.length] = formRef[className.substring(9)];
		}
    }
	
    this.validate = function()
	{
		if (modValidationFormats[thisRef.format])
		{
			return modValidationFormats[thisRef.format](e);
		}
        else
        {
            alert("Validation not found!");
            return true;
        }
	}
}

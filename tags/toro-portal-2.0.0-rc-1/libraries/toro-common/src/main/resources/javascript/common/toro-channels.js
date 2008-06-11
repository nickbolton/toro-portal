window.swapImage=function() {};

function openBrWindow(theURL,winName,features) {
    var winRef = window.open(theURL,winName,features);
    winRef.focus();
}

window.checkForNewHref = function () {
    if (window.locationhref)
    {
        location.href = window.locationhref;
        window.locationhref = null;
    }
};

checkForNewHrefIntervalRef = setInterval("window.checkForNewHref();",200);

// Used for Forum Channel (still need??)
window.loadURL = function(newhref) {
    location.href = newhref;
};

// Used for WebMail (still need??)
window.appendParameterToAction = function(formRef,parameters) {
    var action = formRef.action;
    if (action.indexOf("?") > -1)
    {
        var parts = action.split("?");
        var firstpart = parts[0];
        var otherparts = parts.slice(1).join("?");
        action = firstpart + "?" + parameters; 
        if (otherparts != "")
        {
            action += "&" + otherparts;
        }
    }
    else
    {
        action += "?" + parameters;
    }
    formRef.action = action;
    return action;    
};

// validation.js
function BaseValidationObject() {
    this.name = "BaseValidationObject";

    this.digits = "0123456789";
    this.lowercaseLetters = "abcdefghijklmnopqrstuvwxyz"
    this.uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    // whitespace characters
    this.whitespace = " \t\n\r";
    // decimal point character differs by language and culture
    this.decimalPointDelimiter = "."
    // non-digit characters which are allowed in phone numbers
    this.phoneNumberDelimiters = "()- ";
    // characters which are allowed in US phone numbers
    this.validUSPhoneChars = this.digits + this.phoneNumberDelimiters;
    // characters which are allowed in international phone numbers
    // (a leading + is OK)
    this.validWorldPhoneChars = this.digits + this.phoneNumberDelimiters + "+";
    // non-digit characters which are allowed in 
    // Social Security Numbers
    this.SSNDelimiters = "- ";
    // characters which are allowed in Social Security Numbers
    this.validSSNChars = this.digits + this.SSNDelimiters;
    // U.S. Social Security Numbers have 9 this.digits.
    // They are formatted as 123-45-6789.
    this.digitsInSocialSecurityNumber = 9;
    // U.S. phone numbers have 10 this.digits.
    // They are formatted as 123 456 7890 or (123) 456-7890.
    this.digitsInUSPhoneNumber = 10;
    // non-digit characters which are allowed in ZIP Codes
    this.ZIPCodeDelimiters = "-";
    // our preferred delimiter for reformatting ZIP Codes
    this.ZIPCodeDelimeter = "-"
    // characters which are allowed in Social Security Numbers
    this.validZIPCodeChars = this.digits + this.ZIPCodeDelimiters
    // U.S. ZIP codes have 5 or 9 digits.
    // They are formatted as 12345 or 12345-6789.
    this.digitsInZIPCode1 = 5
    this.digitsInZIPCode2 = 9

    // CONSTANT STRING DECLARATIONS
    // (grouped for ease of translation and localization)

    // m is an abbreviation for "missing"
    this.mPrefix = "You did not enter a value into the "
    this.mSuffix = " field. This is a required field. Please enter it now."
    this.mGeneric = "You must complete all required fields."

    // s is an abbreviation for "string"
    this.sUSLastName = "Last Name"
    this.sUSFirstName = "First Name"
    this.sWorldLastName = "Family Name"
    this.sWorldFirstName = "Given Name"
    this.sTitle = "Title"
    this.sCompanyName = "Company Name"
    this.sUSAddress = "Street Address"
    this.sWorldAddress = "Address"
    this.sCity = "City"
    this.sStateCode = "State Code"
    this.sWorldState = "State, Province, or Prefecture"
    this.sCountry = "Country"
    this.sZIPCode = "ZIP Code"
    this.sWorldPostalCode = "Postal Code"
    this.sPhone = "Phone Number"
    this.sFax = "Fax Number"
    this.sDateOfBirth = "Date of Birth"
    this.sExpirationDate = "Expiration Date"
    this.sEmail = "Email"
    this.sURL = "URL"
    this.sSSN = "Social Security Number"
    this.sOtherInfo = "Other Information"

    // i is an abbreviation for "invalid"
    this.iSizelimit = "This field has exceeded the max character limit of "
    this.iInvalidChars = "You cannot use the following character(s) in this field: "
        this.iInvalidFileType = "This field contains the following invalid file type: "
        this.iInvalidWhitespace = "This field does not accept whitespace characters."
    this.iTitle = "Please enter a Title."
    this.iAlphabetic = "This field only accepts a string of letters (between a and z). Please reenter it now."
    this.iAlphanumeric = "This field only accepts a string of letters (between a and b) and/or non negative numbers (between 0 and 9). Please reenter it now."
    this.iInteger = "This field only accepts numbers (Like -123 or 123). Please reenter it now."
    this.iNonnegativeInteger = "This field only accepts non negative numbers (Like 123, but not -123). Please reenter it now."
    this.iNegativeInteger = "This field only accepts negative numbers (Like -123, but not 123). Please reenter it now."
    this.iFloat = "This field only accepts decimals (Like -1.23 or 1.23). Please reenter it now."
    this.iNonnegativeFloat= "This field only accepts non negative decimals (Like 1.23, but not -1.23). Please reenter it now."

    this.iStateCode = "This field must be a valid two character U.S. state abbreviation (like CA for California). Please reenter it now."
    this.iZIPCode = "This field must be a 5 or 9 digit U.S. ZIP Code (like 94043). Please reenter it now."
    this.iUSPhone = "This field must be a 10 digit U.S. phone number (like 415 555 1212). Please reenter it now."
    this.iWorldPhone = "This field must be a valid international phone number (may include digits and seperators [+,-,(),space]).\nPlease reenter it now."
    this.iSSN = "This field must be a 9 digit U.S. social security number (like 123 45 6789). Please reenter it now."
    this.iEmail = "This field must be a valid email address (like person@domain.com). Please reenter it now."
    this.iURL = "This field must be a valid web address (like http://www.unicon.net). Please reenter it now."
    this.iDay = "This field must be a day number between 1 and 31.  Please reenter it now."
    this.iMonth = "This field must be a month number between 1 and 12.  Please reenter it now."
    this.iYear = "This field must be a 2 or 4 digit year number.  Please reenter it now."
    this.iDatePrefix = "The Day, Month, and Year for "
    this.iDateSuffix = " do not form a valid date.  Please reenter them now."

    // p is an abbreviation for "prompt"
    this.pEntryPrompt = "Please enter a "
    this.pStateCode = "2 character code (like CA)."
    this.pZIPCode = "5 or 9 digit U.S. ZIP Code (like 94043)."
    this.pUSPhone = "10 digit U.S. phone number (like 415 555 1212)."
    this.pWorldPhone = "international phone number [like +1 (234) 567-890-1234]."
    this.pSSN = "9 digit U.S. social security number (like 123 45 6789)."
    this.pEmail = "valid email address (like person@domain.com)."
    this.pURL = "valid web address (like http://www.unicon.net)."
    this.pDay = "day number between 1 and 31."
    this.pMonth = "month number between 1 and 12."
    this.pYear = "2 or 4 digit year number."

    this.daysInMonth = new Array(13);
    this.daysInMonth[1] = 31;
    this.daysInMonth[2] = 29;
    this.daysInMonth[3] = 31;
    this.daysInMonth[4] = 30;
    this.daysInMonth[5] = 31;
    this.daysInMonth[6] = 30;
    this.daysInMonth[7] = 31;
    this.daysInMonth[8] = 31;
    this.daysInMonth[9] = 30;
    this.daysInMonth[10] = 31;
    this.daysInMonth[11] = 30;
    this.daysInMonth[12] = 31;

    // Valid U.S. Postal Codes for states, territories, armed forces, 
    // etc. See http://www.usps.gov/ncsc/lookups/abbr_state.txt.
    this.USStateCodeDelimiter = "|";
    this.USStateCodes = "AL|AK|AS|AZ|AR|CA|CO|CT|DE|DC|FM|FL|GA|GU|HI|ID|IL|IN|IA|KS|KY|LA|ME|MH|MD|MA|MI|MN|MS|MO|MT|NE|NV|NH|NJ|NM|NY|NC|ND|MP|OH|OK|OR|PW|PA|PR|RI|SC|SD|TN|TX|UT|VT|VI|VA|WA|WV|WI|WY|AE|AA|AE|AE|AP";

    // START - COMMON FIELD RULES 
    // Text rule 
    this.textRules = new Object();
    this.textRules.field = null;
    this.textRules.prompt = this.mGeneric;
    this.textRules.method = "checkString";
    this.textRules.isEmptyOk = false;
    // Valid Characters rule 
    this.validCharsRules = new Object();
    this.validCharsRules.field = null;
    this.validCharsRules.prompt = this.mGeneric;
    this.validCharsRules.method = "checkValidCharacters";
    this.validCharsRules.isEmptyOk = false;
    // Valid File Type rule 
    this.validFileTypeRules = new Object();
    this.validFileTypeRules.field = null;
    this.validFileTypeRules.prompt = this.mGeneric;
    this.validFileTypeRules.method = "checkValidFileType";
    this.validFileTypeRules.isEmptyOk = false;
    // Whitespace rule 
    this.validWhitespaceRules = new Object();
    this.validWhitespaceRules.field = null;
    this.validWhitespaceRules.prompt = this.mGeneric;
    this.validWhitespaceRules.method = "checkValidWhitespace";
    this.validWhitespaceRules.isEmptyOk = false;
        // Integer in Range rule 
    this.intrangeRules = new Object();
    this.intrangeRules.field = null;
    this.intrangeRules.prompt = this.mGeneric;
    this.intrangeRules.method = "checkIntegerInRange";
    this.intrangeRules.low = 0;
    this.intrangeRules.high = 0;
    this.intrangeRules.isEmptyOk = false;
    // Positive Integer rule 
    this.positiveIntRules = new Object();
    this.positiveIntRules.field = null;
    this.positiveIntRules.prompt = this.iNonnegativeInteger;
    this.positiveIntRules.method = "checkPositiveInteger";
    this.positiveIntRules.isEmptyOk = false;
    // Negative Integer rule 
    this.negativeIntRules = new Object();
    this.negativeIntRules.field = null;
    this.negativeIntRules.prompt = this.iNegativeInteger;
    this.negativeIntRules.method = "checkNegativeInteger";
    this.negativeIntRules.isEmptyOk = false;
    // Email rule 
    this.emailRules = new Object();
    this.emailRules.field = null;
    this.emailRules.prompt = this.iEmail;
    this.emailRules.method = "checkEmail";
    this.emailRules.isEmptyOk = false;
    // ZIPCode rule 
    this.zipcodeRules = new Object();
    this.zipcodeRules.field = null;
    this.zipcodeRules.prompt = this.iZIPCode; 
    this.zipcodeRules.method = "checkZIPCode";
    this.zipcodeRules.isEmptyOk = false;
    // USPhone field
    this.usPhoneRules = new Object();
    this.usPhoneRules.field = null;
    this.usPhoneRules.prompt = this.iUSPhone; 
    this.usPhoneRules.method = "checkUSPhone";
    this.usPhoneRules.isEmptyOk = false;
    // InternationalPhone field
    this.intlPhoneRules = new Object();
    this.intlPhoneRules.field = null;
    this.intlPhoneRules.prompt = this.iWorldPhone;
    this.intlPhoneRules.method = "checkInternationalPhone";
    this.intlPhoneRules.isEmptyOk = false;
    // SSN field
    this.ssnRules = new Object();
    this.ssnRules.field = null;
    this.ssnRules.prompt = this.iSSN;
    this.ssnRules.method = "checkSSN";
    this.ssnRules.isEmptyOk = false;
    // Year field
    this.yearRules = new Object();
    this.yearRules.field = null;
    this.yearRules.prompt = this.iYear;
    this.yearRules.method = "checkYear";
    this.yearRules.isEmptyOk = false;
    // Month field
    this.monthRules = new Object();
    this.monthRules.field = null;
    this.monthRules.prompt = this.iMonth;
    this.monthRules.method = "checkMonth";
    this.monthRules.isEmptyOk = false;
    // Day field
    this.dayRules = new Object();
    this.dayRules.field = null;
    this.dayRules.prompt = this.iDay;
    this.dayRules.method = "checkDay";
    this.dayRules.isEmptyOk = false;
    // URL field
    this.urlRules = new Object();
    this.urlRules.field = null;
    this.urlRules.prompt = this.iURL;
    this.urlRules.method = "checkURL";
    this.urlRules.isEmptyOk = false;
    // Escape characters field
    this.escapeCharsRules = new Object();
    this.escapeCharsRules.field = null;
    this.escapeCharsRules.prompt = this.iURL;
    this.escapeCharsRules.method = "checkChars";
    this.escapeCharsRules.isEmptyOk = false;
    
    // rulesRef collection that holds all the field rule arrays
    this.rules =
    {
        "text":this.textRules,
        "validchars":this.validCharsRules,
                "validfiletype":this.validFileTypeRules,
                "validwhitespace":this.validWhitespaceRules,
        "intrange":this.intrangeRules,
        "positiveint":this.positiveIntRules,
        "negativeint":this.negativeIntRules,
        "email":this.emailRules,
        "zip":this.zipcodeRules,
        "phone":this.usPhoneRules,
        "fax":this.usPhoneRules,
        "intlphone":this.intlPhoneRules,
        "ssn":this.ssnRules,
        "year":this.yearRules,
        "month":this.monthRules,
        "day":this.dayRules,
        "url":this.urlRules,
        "escapechars":this.escapeCharsRules
    };
    // END- COMMON FIELD RULES 

    // Global variable defaultEmptyOK defines default return value 
    // for many functions when they are passed the empty string. 
    // By default, they will return defaultEmptyOK.
    this.defaultEmptyOK = true; 

    // Check whether string s is empty.
    this.isEmpty = function(s)
    {   
        return ((s == null) || (s.length == 0))
    }

    // Returns true if string s is empty or 
    // whitespace characters only.
    this.isWhitespace = function(s)
    {   
        var i;
        
        // Is s empty?
        if (this.isEmpty(s)) return true;

        // Search through string's characters one by one
        // until we find a non-whitespace character.
        // When we do, return false; if we don't, return true.
        for (i = 0; i < s.length; i++)
        {   
            // Check that current character isn't whitespace.
            var c = s.charAt(i);

            if (this.whitespace.indexOf(c) == -1) return false;
        }

        // All characters are whitespace.
        return true;
    }

    // Removes all characters which appear in string bag from string s.
    this.stripCharsInBag = function(s, bag)
    {   
        var i;
        var returnString = "";

        // Search through string's characters one by one.
        // If character is not in bag, append to returnString.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character isn't whitespace.
            var c = s.charAt(i);
            if (bag.indexOf(c) == -1) returnString += c;
        }

        return returnString;
    }

    // Removes all characters which do NOT appear in string bag 
    // from string s.
    this.stripCharsNotInBag = function(s, bag)
    {   
        var i;
        var returnString = "";

        // Search through string's characters one by one.
        // If character is in bag, append to returnString.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character isn't whitespace.
            var c = s.charAt(i);
            if (bag.indexOf(c) != -1) returnString += c;
        }

        return returnString;
    }

    // Removes all file types which appear in string bag 
    // from string s.
    this.stripFileTypeInBag = function(s, bag)
    {   
        var i, r, arrBag;  
        var returnString = "";
        
        // send string to lowercase and check for a match of each
        // excluded file type possibility
        s = s.toLowerCase();
        arrBag = bag.split(" ");
        for (r = 0; r<arrBag.length; r++) {
            i = s.match(arrBag[r]);
            if (i != null) {
                returnString = i;
                break;
            }
        }

        return returnString;
    }

    // Checks for any whitespace characters from s.
    // Global variable whitespace (see above)
    // defines which characters are considered whitespace.
    this.invalidWhitespace = function(s)
    {   
        var i;
        var returnString = "";

        i = s.match(this.whitespace);
        if (i != null) {
            this.warnInvalidWhitespace();
        }
    }

    // Removes all whitespace characters from s.
    // Global variable whitespace (see above)
    // defines which characters are considered whitespace.
    this.stripWhitespace = function(s)
    {   
        return this.stripCharsInBag(s, this.whitespace);
    }

    // Removes initial (leading) whitespace characters from s.
    // Global variable whitespace (see above)
    // defines which characters are considered whitespace.
    this.stripInitialWhitespace = function(s)
    {   
        var i = 0;

        while ((i < s.length) && (this.whitespace.indexOf(
            s.charAt(i)) != -1))
           i++;
        return s.substring (i, s.length);
    }

    // Returns true if character c is an English letter 
    // (A .. Z, a..z).
    //
    // NOTE: Need i18n version to support European characters.
    // This could be tricky due to different character
    // sets and orderings for various languages and platforms.
    this.isLetter = function(c)
    {   
        return ( ((c >= "a") && (c <= "z")) || ((c >= "A") && 
            (c <= "Z")) );
    }

    // Returns true if character c is a digit 
    // (0 .. 9).
    this.isDigit = function(c)
    {   
        return ((c >= "0") && (c <= "9"))
    }

    // Returns true if character c is a letter or digit.
    this.isLetterOrDigit = function(c)
    {   
        return (this.isLetter(c) || this.isDigit(c))
    }

    // isInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if all characters in string s are numbers.
    //
    // Accepts non-signed integers only. Does not accept floating 
    // point, exponential notation, etc.
    //
    // We don't use parseInt because that would accept a string
    // with trailing non-numeric characters.
    //
    // By default, returns defaultEmptyOK if s is empty.
    // There is an optional second argument called emptyOK.
    // emptyOK is used to override for a single function call
    //      the default behavior which is specified globally by
    //      defaultEmptyOK.
    // If emptyOK is false (or any value other than true), 
    //      the function will return false if s is empty.
    // If emptyOK is true, the function will return true if s is empty.
    //
    // EXAMPLE FUNCTION CALL:     RESULT:
    // isInteger ("5")            true 
    // isInteger ("")             defaultEmptyOK
    // isInteger ("-5")           false
    // isInteger ("", true)       true
    // isInteger ("", false)      false
    // isInteger ("5", false)     true
    this.isInteger = function(s)
    {   
        var i;

        if (this.isEmpty(s)) 
           if (this.isInteger.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isInteger.arguments[1] == true);

        // Search through string's characters one by one
        // until we find a non-numeric character.
        // When we do, return false; if we don't, return true.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character is number.
            var c = s.charAt(i);

            if (!this.isDigit(c)) return false;
        }

        // All characters are numbers.
        return true;
    }

    // isSignedInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if all characters are numbers; 
    // first character is allowed to be + or - as well.
    //
    // Does not accept floating point, exponential notation, etc.
    //
    // We don't use parseInt because that would accept a string
    // with trailing non-numeric characters.
    //
    // For explanation of optional argument emptyOK,
    // see comments of function isInteger.
    //
    // EXAMPLE FUNCTION CALL:          RESULT:
    // isSignedInteger ("5")           true 
    // isSignedInteger ("")            defaultEmptyOK
    // isSignedInteger ("-5")          true
    // isSignedInteger ("+5")          true
    // isSignedInteger ("", false)     false
    // isSignedInteger ("", true)      true
    this.isSignedInteger = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isSignedInteger.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isSignedInteger.arguments[1] == true);

        else {
            var startPos = 0;
            var secondArg = this.defaultEmptyOK;

            if (this.isSignedInteger.arguments.length > 1)
                secondArg = this.isSignedInteger.arguments[1];

            // skip leading + or -
            if ( (s.charAt(0) == "-") || (s.charAt(0) == "+") )
               startPos = 1;    
            return (this.isInteger(s.substring(startPos, s.length), secondArg))
        }
    }

    // isPositiveInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is an integer > 0.
    //
    // For explanation of optional argument emptyOK,
    // see comments of function isInteger.
    this.isPositiveInteger = function(s)
    {   
        var secondArg = this.defaultEmptyOK;

        if (this.isPositiveInteger.arguments.length > 1)
            secondArg = this.isPositiveInteger.arguments[1];

        // The next line is a bit byzantine.  What it means is:
        // a) s must be a signed integer, AND
        // b) one of the following must be true:
        //    i)  s is empty and we are supposed to return true for
        //        empty strings
        //    ii) this is a positive, not negative, number

        return (this.isSignedInteger(s, secondArg)
             && ( (this.isEmpty(s) && secondArg)  || (parseInt (s) > 0) ) );
    }

    // isNonnegativeInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is an integer >= 0.
    //
    // For explanation of optional argument emptyOK,
    // see comments of function isInteger.
    this.isNonnegativeInteger = function(s)
    {   
        var secondArg = this.defaultEmptyOK;

        if (this.isNonnegativeInteger.arguments.length > 1)
            secondArg = this.isNonnegativeInteger.arguments[1];

        // The next line is a bit byzantine.  What it means is:
        // a) s must be a signed integer, AND
        // b) one of the following must be true:
        //    i)  s is empty and we are supposed to return true for
        //        empty strings
        //    ii) this is a number >= 0

        return (this.isSignedInteger(s, secondArg)
             && ( (this.isEmpty(s) && secondArg)  || (parseInt (s) >= 0) ) );
    }

    // isNegativeInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is an integer < 0.
    //
    // For explanation of optional argument emptyOK,
    // see comments of function isInteger.
    this.isNegativeInteger = function(s)
    {   
        var secondArg = this.defaultEmptyOK;

        if (this.isNegativeInteger.arguments.length > 1)
            secondArg = this.isNegativeInteger.arguments[1];

        // The next line is a bit byzantine.  What it means is:
        // a) s must be a signed integer, AND
        // b) one of the following must be true:
        //    i)  s is empty and we are supposed to return true for
        //        empty strings
        //    ii) this is a negative, not positive, number

        return (this.isSignedInteger(s, secondArg)
             && ( (this.isEmpty(s) && secondArg)  || (parseInt (s) < 0) ) );
    }

    // isNonpositiveInteger (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is an integer <= 0.
    this.isNonpositiveInteger = function(s)
    {   
        var secondArg = this.defaultEmptyOK;

        if (this.isNonpositiveInteger.arguments.length > 1)
            secondArg = this.isNonpositiveInteger.arguments[1];

        // The next line is a bit byzantine.  What it means is:
        // a) s must be a signed integer, AND
        // b) one of the following must be true:
        //    i)  s is empty and we are supposed to return true for
        //        empty strings
        //    ii) this is a number <= 0

        return (this.isSignedInteger(s, secondArg)
             && ( (this.isEmpty(s) && secondArg)  || (parseInt (s) <= 0) ) );
    }

    // isFloat (STRING s [, BOOLEAN emptyOK])
    // 
    // True if string s is an unsigned floating point (real) number. 
    //
    // Also returns true for unsigned integers. If you wish
    // to distinguish between integers and floating point numbers,
    // first call isInteger, then call isFloat.
    //
    // Does not accept exponential notation.
    this.isFloat = function(s)
    {   
        var i;
        var seenDecimalPoint = false;

        if (this.isEmpty(s)) 
           if (this.isFloat.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isFloat.arguments[1] == true);

        if (s == this.decimalPointDelimiter) return false;

        // Search through string's characters one by one
        // until we find a non-numeric character.
        // When we do, return false; if we don't, return true.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character is number.
            var c = s.charAt(i);

            if ((c == this.decimalPointDelimiter) && !seenDecimalPoint) seenDecimalPoint = true;
            else if (!this.isDigit(c)) return false;
        }

        // All characters are numbers.
        return true;
    }

    // isSignedFloat (STRING s [, BOOLEAN emptyOK])
    // 
    // True if string s is a signed or unsigned floating point 
    // (real) number. First character is allowed to be + or -.
    //
    // Also returns true for unsigned integers. If you wish
    // to distinguish between integers and floating point numbers,
    // first call isSignedInteger, then call isSignedFloat.
    //
    // Does not accept exponential notation.
    this.isSignedFloat = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isSignedFloat.arguments.length == 1) 
               return this.defaultEmptyOK;
           else return (this.isSignedFloat.arguments[1] == true);

        else {
            var startPos = 0;
            var secondArg = this.defaultEmptyOK;

            if (this.isSignedFloat.arguments.length > 1)
                secondArg = this.isSignedFloat.arguments[1];

            // skip leading + or -
            if ( (s.charAt(0) == "-") || (s.charAt(0) == "+") )
               startPos = 1;    
            return (this.isFloat(s.substring(startPos, s.length), secondArg))
        }
    }

    // isAlphabetic (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is English letters 
    // (A .. Z, a..z) only.
    //
    // NOTE: Need i18n version to support European characters.
    // This could be tricky due to different character
    // sets and orderings for various languages and platforms.
    this.isAlphabetic = function(s)
    {   
        var i;

        if (this.isEmpty(s)) 
           if (this.isAlphabetic.arguments.length == 1) 
               return this.defaultEmptyOK;
           else return (this.isAlphabetic.arguments[1] == true);

        // Search through string's characters one by one
        // until we find a non-alphabetic character.
        // When we do, return false; if we don't, return true.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character is letter.
            var c = s.charAt(i);

            if (!this.isLetter(c))
            return false;
        }

        // All characters are letters.
        return true;
    }

    // isAlphanumeric (STRING s [, BOOLEAN emptyOK])
    // 
    // Returns true if string s is English letters 
    // (A .. Z, a..z) and numbers only.
    //
    // NOTE: Need i18n version to support European characters.
    // This could be tricky due to different character
    // sets and orderings for various languages and platforms.
    this.isAlphanumeric = function(s)
    {   
        var i;

        if (this.isEmpty(s)) 
           if (this.isAlphanumeric.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isAlphanumeric.arguments[1] == true);

        // Search through string's characters one by one
        // until we find a non-alphanumeric character.
        // When we do, return false; if we don't, return true.

        for (i = 0; i < s.length; i++)
        {   
            // Check that current character is number or letter.
            var c = s.charAt(i);

            if (! (this.isLetter(c) || this.isDigit(c) ) )
            return false;
        }

        // All characters are numbers or letters.
        return true;
    }

    // reformat (TARGETSTRING, STRING, INTEGER, STRING, INTEGER ... )       
    //
    // Handy function for arbitrarily inserting formatting characters
    // or delimiters of various kinds within TARGETSTRING.
    //
    // reformat takes one named argument, a string s, and any number
    // of other arguments.  The other arguments must be integers or
    // strings.  These other arguments specify how string s is to be
    // reformatted and how and where other strings are to be inserted
    // into it.
    //
    // reformat processes the other arguments in order one by one.
    // * If the argument is an integer, reformat appends that number 
    //   of sequential characters from s to the resultString.
    // * If the argument is a string, reformat appends the string
    //   to the resultString.
    //
    // NOTE: The first argument after TARGETSTRING must be a string.
    // (It can be empty.)  The second argument must be an integer.
    // Thereafter, integers and strings must alternate.  This is to
    // provide backward compatibility to Navigator 2.0.2 JavaScript
    // by avoiding use of the typeof operator.
    //
    // It is the caller's responsibility to make sure that we do not
    // try to copy more characters from s than s.length.
    //
    // EXAMPLES:
    //
    // * To reformat a 10-digit U.S. phone number from "1234567890"
    //   to "(123) 456-7890" make this function call:
    //   reformat("1234567890", "(", 3, ") ", 3, "-", 4)
    //
    // * To reformat a 9-digit U.S. Social Security number from
    //   "123456789" to "123-45-6789" make this function call:
    //   reformat("123456789", "", 3, "-", 2, "-", 4)
    //
    // HINT:
    //
    // If you have a string which is already delimited in one way
    // (example: a phone number delimited with spaces as "123 456 7890")
    // and you want to delimit it in another way using function reformat,
    // call function stripCharsNotInBag to remove the unwanted 
    // characters, THEN call function reformat to delimit as desired.
    //
    // EXAMPLE:
    //
    // reformat (stripCharsNotInBag ("123 456 7890", digits),
    //           "(", 3, ") ", 3, "-", 4)
    this.reformat = function(s)
    {   
        var arg;
        var sPos = 0;
        var resultString = "";

        for (var i = 1; i < this.reformat.arguments.length; i++) {
           arg = this.reformat.arguments[i];
           if (i % 2 == 1) resultString += arg;
           else {
               resultString += s.substring(sPos, sPos + arg);
               sPos += arg;
           }
        }
        return resultString;
    }

    // isSSN (STRING s [, BOOLEAN emptyOK])
    // 
    // isSSN returns true if string s is a valid U.S. Social
    // Security Number.  Must be 9 digits.
    //
    // NOTE: Strip out any delimiters (spaces, hyphens, etc.)
    // from string s before calling this function.
    this.isSSN = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isSSN.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isSSN.arguments[1] == true);
        return (this.isInteger(s) && s.length == this.digitsInSocialSecurityNumber)
    }

    // isUSPhoneNumber (STRING s [, BOOLEAN emptyOK])
    // 
    // isUSPhoneNumber returns true if string s is a valid U.S. Phone
    // Number.  Must be 10 digits.
    //
    // NOTE: Strip out any delimiters (spaces, hyphens, parentheses, etc.)
    // from string s before calling this function.
    this.isUSPhoneNumber = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isUSPhoneNumber.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isUSPhoneNumber.arguments[1] == true);
        return (this.isInteger(s) && s.length == this.digitsInUSPhoneNumber)
    }

    // isInternationalPhoneNumber (STRING s [, BOOLEAN emptyOK])
    // 
    // isInternationalPhoneNumber returns true if string s is a valid 
    // international phone number.  Must be digits only; any length OK.
    // May be prefixed by + character.
    //
    // NOTE: A phone number of all zeros would not be accepted.
    // I don't think that is a valid phone number anyway.
    //
    // NOTE: Strip out any delimiters (spaces, hyphens, parentheses, etc.)
    // from string s before calling this function.  You may leave in 
    // leading + character if you wish.
    this.isInternationalPhoneNumber = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isInternationalPhoneNumber.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isInternationalPhoneNumber.arguments[1] == true);
        var strippedChars=this.stripCharsInBag(s,"()- +");
        return (this.isPositiveInteger(strippedChars))
    }

    // isZIPCode (STRING s [, BOOLEAN emptyOK])
    // 
    // isZIPCode returns true if string s is a valid 
    // U.S. ZIP code.  Must be 5 or 9 digits only.
    //
    // NOTE: Strip out any delimiters (spaces, hyphens, etc.)
    // from string s before calling this function.  
    this.isZIPCode = function(s)
    {  
        if (this.isEmpty(s)) 
           if (this.isZIPCode.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isZIPCode.arguments[1] == true);
       return (this.isInteger(s) && 
                ((s.length == this.digitsInZIPCode1) ||
                 (s.length == this.digitsInZIPCode2)))
    }

    // isStateCode (STRING s [, BOOLEAN emptyOK])
    // 
    // Return true if s is a valid U.S. Postal Code 
    // (abbreviation for state).
    this.isStateCode = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isStateCode.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isStateCode.arguments[1] == true);
        return ( (USStateCodes.indexOf(s) != -1) &&
                 (s.indexOf(USStateCodeDelimiter) == -1) )
    }

    // isURL (STRING s [, BOOLEAN emptyOK])
    this.isURL = function(s)
    {
        if (this.isEmpty(s)) 
           if (this.isURL.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isURL.arguments[1] == true);

        // is s valid URL?
        if (s.search(/^https?:\/\/.+$/) != 0) return false;
        return true;
    }

    // isEmail (STRING s [, BOOLEAN emptyOK])
    // 
    // Email address must be of form a@b.c -- in other words:
    // * there must be at least one character before the @
    // * there must be at least one character before and after the .
    // * the characters @ and . are both required
    this.isEmail = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isEmail.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isEmail.arguments[1] == true);
       
        // is s whitespace?
        if (this.isWhitespace(s)) return false;
        
        // there must be >= 1 character before @, so we
        // start looking at character position 1 
        // (i.e. second character)
        var i = 1;
        var sLength = s.length;

        // look for @
        while ((i < sLength) && (s.charAt(i) != "@"))
        { 
            i++
        }

        if ((i >= sLength) || (s.charAt(i) != "@")) return false;
        else i += 2;

        // look for .
        while ((i < sLength) && (s.charAt(i) != "."))
        { 
            i++
        }

        // there must be at least one character after the .
        if ((i >= sLength - 1) || (s.charAt(i) != ".")) return false;
        else return true;
    }

    // isYear (STRING s [, BOOLEAN emptyOK])
    // 
    // isYear returns true if string s is a valid 
    // Year number.  Must be 2 or 4 digits only.
    // 
    // For Year 2000 compliance, you are advised
    // to use 4-digit year numbers everywhere.
    //
    // And yes, this function is not Year 10000 compliant, but 
    // because I am giving you 8003 years of advance notice,
    // I don't feel very guilty about this ...
    //
    // For B.C. compliance, write your own function. ;->
    this.isYear = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isYear.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isYear.arguments[1] == true);
        if (!this.isNonnegativeInteger(s)) return false;
        return ((s.length == 2) || (s.length == 4));
    }

    // isIntegerInRange (STRING s, INTEGER a, INTEGER b [, BOOLEAN emptyOK])
    // 
    // isIntegerInRange returns true if string s is an integer 
    // within the range of integer arguments a and b, inclusive.
    this.isIntegerInRange = function(s, a, b)
    {   
        if (this.isEmpty(s)) 
           if (this.isIntegerInRange.arguments.length == 1) 
               return this.defaultEmptyOK;
           else return (this.isIntegerInRange.arguments[1] == true);

        // Catch non-integer strings to avoid creating a NaN below,
        // which isn't available on JavaScript 1.0 for Windows.
        if (!this.isInteger(s, false)) return false;

        // Now, explicitly change the type to integer via parseInt
        // so that the comparison code below will work both on 
        // JavaScript 1.2 (which typechecks in equality comparisons)
        // and JavaScript 1.1 and before (which doesn't).
        var num = parseInt (s);
        return ((num >= a) && (num <= b));
    }

    // isMonth (STRING s [, BOOLEAN emptyOK])
    // 
    // isMonth returns true if string s is a valid 
    // month number between 1 and 12.
    this.isMonth = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isMonth.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isMonth.arguments[1] == true);
        return this.isIntegerInRange (s, 1, 12);
    }

    // isDay (STRING s [, BOOLEAN emptyOK])
    // 
    // isDay returns true if string s is a valid 
    // day number between 1 and 31.
    this.isDay = function(s)
    {   
        if (this.isEmpty(s)) 
           if (this.isDay.arguments.length == 1) return this.defaultEmptyOK;
           else return (this.isDay.arguments[1] == true);   
        return this.isIntegerInRange (s, 1, 31);
    }

    // daysInFebruary (INTEGER year)
    // 
    // Given integer argument year,
    // returns number of days in February of that year.
    this.daysInFebruary = function(year)
    {   
        // February has 29 days in any year evenly divisible by four,
        // EXCEPT for centurial years which are not also divisible by 400.
        return (  ((year % 4 == 0) && ( (!(year % 100 == 0)) || 
            (year % 400 == 0) ) ) ? 29 : 28 );
    }

    // isDate (STRING year, STRING month, STRING day)
    //
    // isDate returns true if string arguments year, month, and day 
    // form a valid date.
    this.isDate = function(year, month, day)
    {   
        // catch invalid years (not 2- or 4-digit) and invalid months and days.
        if (! (this.isYear(year, false) && this.isMonth(month, false) && 
            this.isDay(day, false))) return false;

        // Explicitly change type to integer to make code work in both
        // JavaScript 1.1 and JavaScript 1.2.
        var intYear = parseInt(year);
        var intMonth = parseInt(month);
        var intDay = parseInt(day);

        // catch invalid days, except for February
        if (intDay > this.daysInMonth[intMonth]) return false; 

        if ((intMonth == 2) && (intDay > this.daysInFebruary(intYear))) return false;

        return true;
    }

    // isValidDateRangeByValue (String startYear, String startMonth,
    // startDay, String endYear, String endMonth, String endDay)
    //
    // isValidDateRangeByValue returns true if start date occurs before end date
    this.isValidDateRangeByValue = function(
        startYear, startMonth, startDay, endYear, endMonth, endDay)
    {   
        if ( !(this.isDate(startYear, startMonth, startDay)
            && this.isDate(endYear, endMonth, endDay)) )
        {
            return false;
        }

        var startDate = new Date();
        startDate.setMonth(parseInt(startMonth));
        startDate.setDate(parseInt(startDay));
        startDate.setFullYear(parseInt(startYear));

        var endDate = new Date();
        endDate.setMonth(parseInt(endMonth));
        endDate.setDate(parseInt(endDay));
        endDate.setFullYear(parseInt(endYear));

        if (!this.isValidDateRange(startDate,endDate)) return false;

        return true;
    }

    // isValidDateRange (DATE startDate, DATE endDate)
    //
    // isValidDateRange returns true if startDate occurs before endDate
    this.isValidDateRange = function(startDate, endDate)
    {   
        var startMonth = startDate.getMonth();
        var startDay = startDate.getDate();
        var startYear = startDate.getFullYear();

        var endMonth = endDate.getMonth();
        var endDay = endDate.getDate();
        var endYear = endDate.getFullYear();

        if (startYear > endYear) return false;
        if (startYear < endYear) return true; 

        if (startMonth > endMonth) return false;
        if (startMonth < endMonth) return true;

        if (startDay == endDay) return false;
        if (startDay > endDay) return false;
        if (startDay < endDay) return true;
    }

    /* FUNCTIONS TO NOTIFY USER OF INPUT REQUIREMENTS OR MISTAKES. */

    // Display prompt string s in status bar.
    this.prompt = function(s)
    {   
        window.status = s
    }

    // Display data entry prompt string s in status bar.
    this.promptEntry = function(s)
    {   
        window.status = this.pEntryPrompt + s
    }

    // Notify user that theField exceeds max character limit.
    // String s is the size limit of theField.
    // Put focus in theField and return false.
    this.warnSizelimit = function(theField, s)
    {   
        alert(this.iSizelimit + s);
        theField.focus();
        theField.select();
        return false;
    }

    // Notify user that theField is has invalid characters.
    // String s is a list of the invalid characters found in theField.value.
    // Put focus in theField and return false.
    this.warnInvalidChars = function(theField, s)
    {   
        alert(this.iInvalidChars + s);
        theField.focus();
        theField.select();
        return false;
    }

    // Notify user that theField is has invalid file type.
    // String s is a list of the invalid file type found in theField.value.
    // Put focus in theField and return false.
    this.warnInvalidFileType = function(theField, s)
    {   
        alert(this.iInvalidFileType + s);
        theField.focus();
        theField.select();
        return false;
    }

    // Notify user that theField is has invalid use of whitespace.
    // String s is a list of the invalid whitespace found in theField.value.
    // Put focus in theField and return false.
    this.warnInvalidWhitespace = function(theField, s)
    {   
        alert(this.iInvalidWhitespace);
        theField.focus();
        theField.select();
        return false;
    }


    // Notify user that required field theField is empty.
    // String s describes expected contents of theField.value.
    // Put focus in theField and return false.
    this.warnEmpty = function(theField, s)
    {   
        if (this.warnEmpty.arguments.length == 2)
        {
            alert(s);
        }
        else
        {
            alert(this.mGeneric);
        }
        theField.focus();
        theField.select();
        return false;
    }

    // Notify user that contents of field theField are invalid.
    // String s describes expected contents of theField.value.
    // Put select theField, put focus in it, and return false.
    this.warnInvalid = function(theField, s)
    {   
        alert(s);
        theField.focus();
        theField.select();
        return false;
    }

    /* FUNCTIONS TO INTERACTIVELY CHECK VARIOUS FIELDS. */

    // Check that string fieldRule.field.value is not all whitespace.
    this.checkString = function(fieldRule)
    {   
        emptyOK = fieldRule.isEmptyOk;
        if ((emptyOK == true) && (this.isEmpty(fieldRule.field.value))) return true;
        if (fieldRule.sizeLimit && 
            (fieldRule.field.value.length > fieldRule.sizeLimit))
               return this.warnSizelimit(fieldRule.field, fieldRule.sizeLimit);

        if (this.isWhitespace(fieldRule.field.value)) 
           return this.warnEmpty(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value does not have invalid characters.
    this.checkValidCharacters = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        var invalidChars = "";
        if (fieldRule.excludeList)
        {
            invalidChars = this.stripCharsNotInBag(
                fieldRule.field.value, fieldRule.excludeList);
        }
        if (invalidChars != "")
            return this.warnInvalidChars(fieldRule.field, invalidChars);
        else return true;
    }

    // Check that string fieldRule.field.value does not have an invalid file type.
    this.checkValidFileType = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        var invalidFileType = "";
        if (fieldRule.excludeList)
        {
            invalidFileType = this.stripFileTypeInBag(
                fieldRule.field.value, fieldRule.excludeList);
        }
        if (invalidFileType != "")
            return this.warnInvalidFileType(fieldRule.field, invalidFileType);
        else return true;
    }

    // Check that string fieldRule.field.value does not have invalid whitespace usage.
    this.checkValidWhitespace = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        var invalidWhitespace = " ";
        invalidWhitespace = fieldRule.field.value.match(" ");
        if (invalidWhitespace)
            return this.warnInvalidWhitespace(fieldRule.field, invalidWhitespace);
        else return true;
    }

    // Check that string fieldRule.field.value is alphabetic
    this.checkAlphabetic = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isAlphabetic(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is alphanumeric
    this.checkAlphanumeric = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isAlphanumeric(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is is an integer.
    this.checkInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a non negative integer.
    this.checkNonnegativeInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isNonnegativeInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a negative integer.
    this.checkNegativeInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isNegativeInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a positive integer.
    this.checkPositiveInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isPositiveInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a signed integer.
    this.checkSignedInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isSignedInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a signed integer.
    this.checkNonpositiveInteger = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isNonpositiveInteger(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a float.
    this.checkFloat = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isFloat(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a signed float.
    this.checkSignedFloat = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isSignedFloat(fieldRule.field.value, emptyOK)) 
           return this.warnInvalid(fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a valid U.S. state code.
    this.checkStateCode = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
        {  fieldRule.field.value = fieldRule.field.value.toUpperCase();
           if (!this.isStateCode(fieldRule.field.value, false)) 
              return this.warnInvalid (fieldRule.field, fieldRule.prompt);
           else return true;
        }
    }

    // takes ZIPString, a string of 5 or 9 digits;
    // if 9 digits, inserts separator hyphen
    this.reformatZIPCode = function(ZIPString)
    {   
        if (ZIPString.length == 5) return ZIPString;
        else return (this.reformat (ZIPString, "", 5, "-", 4));
    }

    // Check that string fieldRule.field.value is a valid ZIP code.
    this.checkZIPCode = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
        { 
            var normalizedZIP = this.stripCharsInBag(fieldRule.field.value, this.ZIPCodeDelimiters)
          if (!this.isZIPCode(normalizedZIP, false)) 
             return this.warnInvalid (fieldRule.field, fieldRule.prompt);
          else 
          {  // if you don't want to insert a hyphen, comment next line out
             //fieldRule.field.value = this.reformatZIPCode(normalizedZIP)
             return true;
          }
        }
    }

    // takes USPhone, a string of 10 digits
    // and reformats as (123) 456-789
    this.reformatUSPhone = function(USPhone)
    {   
        return (this.reformat(USPhone, "(", 3, ") ", 3, "-", 4))
    }

    // Check that string fieldRule.field.value is a valid US Phone.
    this.checkUSPhone = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
        {  
            var normalizedPhone = 
            this.stripCharsInBag(
                fieldRule.field.value, this.phoneNumberDelimiters)
           if (!this.isUSPhoneNumber(normalizedPhone, false)) 
              return this.warnInvalid (fieldRule.field, fieldRule.prompt);
           else 
           {  // if you don't want to reformat as (123) 456-789, comment next line out
              //fieldRule.field.value = this.reformatUSPhone(normalizedPhone)
              return true;
           }
        }
    }

    // Check that string fieldRule.field.value is a valid International Phone.
    this.checkInternationalPhone = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
        {  if (!this.isInternationalPhoneNumber(fieldRule.field.value, false)) 
              return this.warnInvalid (fieldRule.field, fieldRule.prompt);
           else return true;
        }
    }

    // Check that string fieldRule.field.value is a valid URL.
    this.checkURL= function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else if (!this.isURL(fieldRule.field.value, false)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
        else return true;
    }
    
    // Check string fieldRule.field.value for offending characters, replace with escaped chars.
    this.checkChars= function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
            {
                //alert ("Not Null Test Message!");
                alert (fieldRule.field.value);
                b = new Array();
                        b[0] = /&/gi;
                        b[1] = /\>/gi;
                        b[2] = /\</gi;
                        b[3] = /\+/gi;
                        
                        c = new Array();
                        c[0] = "&amp";
                        c[1] = "&gt;";
                        c[2] = "&lt;";
                        c[3] = "&#43;";
                        
                        for (var i = 0 ; i < b.length ; i ++)
                        {
                            if (fieldRule.field.value.search(b[i]) != -1) 
                            {
                                fieldRule.field.value = fieldRule.field.value.replace(b[i],c[i]);
                            }
                        }
                        alert (fieldRule.field.value);
                return true;
            }
        /*
        this was an external function I brought in for experimenting
        function isLegal(txt) {
                    var invalids = "!@#$%^&*()-~,'<.>/?;:\|"
                    for(i=0; i<invalids.length; i++) {
                        if(txt.indexOf(invalids.charAt(i)) >= 0 ) {
                            return false;
                        }
                    }
                    return true;
                }
        if (!this.checkString(fieldRule)) return false;
        else if (!this.isURL(fieldRule.field.value, false)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
        else return true;
        */
    }

    // Check that string fieldRule.field.value is a valid Email.
    this.checkEmail = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isEmail(fieldRule.field.value, fieldRule.isEmptyOk)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
                var invalidChars = "";
        if (fieldRule.excludeList)
        {
            invalidChars = this.stripCharsNotInBag(
                fieldRule.field.value, fieldRule.excludeList);
        }
        if (invalidChars != "")
            return this.warnInvalidChars(fieldRule.field, invalidChars);
        else return true;
    }
    
    




    // takes SSN, a string of 9 digits
    // and reformats as 123-45-6789
    this.reformatSSN = function(SSN)
    {   
        return (this.reformat(SSN, "", 3, "-", 2, "-", 4))
    }

    // Check that string fieldRule.field.value is a valid SSN.
    this.checkSSN = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        else
        {  
            var normalizedSSN = this.stripCharsInBag(fieldRule.field.value, this.SSNDelimiters)
           if (!this.isSSN(normalizedSSN, false)) 
              return this.warnInvalid (fieldRule.field, fieldRule.prompt);
           else 
           {  // if you don't want to reformat as 123-456-7890, comment next line out
              //fieldRule.field.value = this.reformatSSN(normalizedSSN)
              return true;
           }
        }
    }

    // Check that string fieldRule.field.value is a valid Year.
    this.checkYear = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isYear(fieldRule.field.value, false)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Check that string fieldRule.field.value is a valid Month.
    this.checkMonth = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isMonth(fieldRule.field.value, false)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
        else return true;
    }


    // Check that string fieldRule.field.value is a valid Day.
    this.checkDay = function(fieldRule)
    {   
        if (!this.checkString(fieldRule)) return false;
        if (!this.isDay(fieldRule.field.value, false)) 
           return this.warnInvalid (fieldRule.field, fieldRule.prompt);
        else return true;
    }

    // Get checked value from radio button.
    this.getRadioButtonValue = function(radio)
    {   
        for (var i = 0; i < radio.length; i++)
        {   
            if (radio[i].checked) { break; }
        }
        return radio[i].value;
    }

    // This is called when multiple rules are checked against fields
    // contained within a form.
    this.applyFormRules = function(formRef, rulesRef)
    {
        var invalidCount = 0;
        if (this.applyFormRules.arguments.length == 1) 
        {
            rulesRef = this;
        }
        else // set field property on rule for use by dependent fields
        {
            rulesRef.form = formRef;
            for (var i = 0; i < formRef.elements.length; i++)
            {
                var fieldRef = formRef.elements[i];
                if (rulesRef.rules[fieldRef.name] != null)
                {
                    rulesRef.rules[fieldRef.name].field = fieldRef;
                }
            }
        }
        // loop and apply field rules
        for (var i = 0; i < formRef.elements.length; i++)
        {
            var fieldRef = formRef.elements[i];
            if (fieldRef.type == 'text' 
                || fieldRef.type == 'textarea'
                || fieldRef.type == 'password'
                || fieldRef.type == 'select-one'
                || fieldRef.type == 'select-multiple'
                || fieldRef.type == 'file')
            {
                if (!this.applyFieldRules(fieldRef, rulesRef))
                {
                    invalidCount++;
                    break;
                }
            }
            /*else
            {
                // this is where multi-selection stuff will be handled.
            }*/
        }
        return (invalidCount == 0);
    }

    // This is called when rules are checked against a field.
    this.applyFieldRules = function(fieldRef, rulesRef)
    {
        var isCommon = true;
        var implRule = null;
        var baseRule = null;
        var prompt = "";
        var method = "";
        var emptyOk = "";
        if (rulesRef.name == "BaseValidationObject")
        {
            baseRule = this.rules["text"];
            baseRule.field = fieldRef;
            prompt = baseRule.prompt;
            method = baseRule.method; 
            emptyOk = baseRule.isEmptyOk;
        }
        else if (!rulesRef.rules[fieldRef.name])
        {
            return true; // if no rule is defined ignore it
        }
        else
        {
            var implRule = rulesRef.rules[fieldRef.name];
            var type = implRule.type;
            prompt = implRule.prompt;
            emptyOk = implRule.isEmptyOk;
            //alert("type: "+type+"\nprompt: "+prompt+"\nemptyOk: "+emptyOk);
            if (!this.isEmpty(type))
            {
                baseRule = this.rules[type];
                baseRule.prompt = 
                    (this.isEmpty(prompt))? baseRule.prompt:prompt;
                baseRule.field = fieldRef;
                baseRule.isEmptyOk = emptyOk;
                method = baseRule.method;
                // set max character size limit
                baseRule.sizeLimit = 
                    (implRule.sizeLimit)? implRule.sizeLimit:255;
                // set list of characters that are not valid 
                baseRule.excludeList = 
                    (implRule.excludeList)? implRule.excludeList:"";
            }
            else
            {
                implRule.field = 
                    (implRule.field != null)? implRule.field:fieldRef;
                method = implRule.method; 
                // set max character size limit
                implRule.sizeLimit = 
                    (implRule.sizeLimit)? implRule.sizeLimit:255;
                // set list of characters that are not valid 
                implRule.excludeList = 
                    (implRule.excludeList)? implRule.excludeList:"";
                isCommon = false;
            }
        } // end if
        if (isCommon)
        {
            if (!this[method](baseRule))
            {
                return false;
            }
        } 
        else
        {
            if (!rulesRef[method](implRule))
            {
                return false;
            }
        } // end if
        return true;
    } // end applyFieldRules
 
} // end BaseValidationObject

validator = new BaseValidationObject();

BaseRulesObject = function(type,method,promptMsg,isEmptyOk) {
    this.type = type;
    this.method = method;
    if (isEmptyOk == null) 
    {
        isEmptyOk = false;
    }
    this.isEmptyOk = isEmptyOk;
    this.prompt = promptMsg;
    this.field = null;
}; // end BaseRulesObject

// Create a basic object that can be made to replace object literal {property:value} syntax 
// which is failing in Netscape for CSCR
SimpleObject = function() {
    for(var i=0; i<arguments.length; i+=2)
    {
        if((i+1)<arguments.length)
        {
            this[arguments[i]] = arguments[i+1];
        }
    }
    this.get = function(prop)
    {
        return this[prop];
    }
    this.set = function(prop,val)
    {
        this[prop] = val;
    }
}; // end SimpleObject


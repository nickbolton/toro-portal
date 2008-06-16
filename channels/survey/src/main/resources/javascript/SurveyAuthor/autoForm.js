//autoForm.js
/*  
This function is used to reveal/hide the recipient selection inputs based on which radio button is selected (poll or recipient)
*/
var ToggleDistribution = function (type) {
    var type = (type);
    if (type == "recipient") {
        document.getElementById("recipientOptions").style.display = "block";
    } else {
        document.getElementById("recipientOptions").style.display = "none";
    }
}
// create initialize method so that it can be initialized outside of page load
initializeSurveyAuthorChannel = function()
{
    // no initialization needed
}
// This will offer a warning to survey creators that no title was given to the distribution.  This only warns the user, does not force them to use this.
var WarnUntitledDistribution = function (distForm) {
	if(distForm.SDTitle.value == "") {
		if(confirm("This distribution has no title. Do you wish to continue?")) {
			return true;
		}
		else {
			return false;	
		}
	}
}


// Everything below was added to support the copying of functions from one window to
// another so that client-side optimizations would work
SurveyAuthorChannelFunctions = ["initializeSurveyAuthorChannel","ToggleDistribution","WarnUntitledDistribution"];
// if channelFunctionsArray is already defined, just add to it
if (window.channelFunctionsArray)
{
    channelFunctionsArray[channelFunctionsArray.length] = SurveyAuthorChannelFunctions;
}
// else create channelFunctionsArray with this as first entry
else
{
    channelFunctionsArray = [SurveyAuthorChannelFunctions]; // create 2-D array
}
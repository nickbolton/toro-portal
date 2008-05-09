// UniconURLSequencer used to Single Sign Onto another app 
// It handles sequential URL requests
// @param guid (globally unique id) that is used to reference this Object instance within UniconSSOSequences
// @param doneFunction optional parameter that is a reference to a Function to evoke when loading of the Sequence is complete
function UniconURLSequencer (guid,doneFunction)
{
    var thisRef = this;
    var sequence = [];
    var seqPos = 0;
    var lastTime = 0;
    var resetForm;
    var resetFormGuid;
    
    
    /* set to stillWaitingAsOf 0 initially, will be updated by other window when loading a page */
    this.stillWaitingAsOf = 0; 

    // Property Flag to determine whether or not Form(s) are ready to be submitted
    this.isReady = false;
    
    // Property Flag to determine if a start is requested once Ready
    this.isStartRequested = false;

    // To add another form to submit in sequence
    this.add = function(formId,sendId)
    {
        var position = sequence.length;
        sequence[position] = {"formId":formId,"sendId":sendId}; 
    }

    // To retrieve a Form Reference by Send Id
    this.getFormRefBySendId = function(sendId)
    {
        var formRef;
        for (var i=0; i<sequence.length; i++)
        {
            if (sequence[i].sendId == sendId)
            {
                formRef = document.getElementById(sequence[i].formId);
                break;
            }
        }
        return formRef;
    }

    // Return array of send Ids for this Sequence
    this.getSendIds = function()
    {
        var sendIdArray = [];
        for (var i=0; i<sequence.length; i++)
        {
            sendIdArray[sendIdArray.length] = sequence[i].sendId;
        }
        return sendIdArray;
    }

    // To start running the sequence, beginning with the first added form
    this.start = function()
    {
        if (!thisRef.isReady) 
        {
            //window.status='Start requested flag set';
            thisRef.isStartRequested = true;
            return;
        }
        // If first time sequence is run, store initial values of reset form
        if (!resetFormGuid)
        {
            resetForm = document.getElementById(guid+"_resetForm");
            resetFormGuid = document.getElementById(guid+"_resetForm_guid");            
        }
        seqPos = 0;
        thisRef.next();
    }

    // To submit the next form in the sequence.  Actually submits the reset form and waits for onreset
    this.next = function()
    {
        if (seqPos == sequence.length-1 && !doneFunction)
        {
            // If last form, don't need to reset 1st, just submit
            var nextFormRef = document.getElementById(sequence[seqPos].formId);
            nextFormRef.submit();
        }
        else if (seqPos < sequence.length)
        {
            // Load reset file into Window/Iframe pointing to this sequence
            resetFormGuid.value = guid;
            resetForm.submit();
        }
        else if (doneFunction)
        {
            doneFunction();
        }
    }

    // When reset page has loaded, submit next request
    this.onreset = function()
    {
        var nextFormRef = document.getElementById(sequence[seqPos].formId);
        sequence[seqPos].startTime = (new Date()).getTime();
        window.setTimeout("UniconSSOSequences['"+guid+"'].checkLoadingStatus()",300);
        nextFormRef.submit();
    }

    // Method to check the loading status of a sequence URL is being loaded
    // When loaded call onload()
    this.checkLoadingStatus = function()
    {
        if (thisRef.stillWaitingAsOf > 0)
        {
            if(lastTime > 0 && lastTime == thisRef.stillWaitingAsOf)
            {
                lastTime = 0;
                thisRef.onload();
            }
            else
            {
                lastTime = thisRef.stillWaitingAsOf;
                window.setTimeout("UniconSSOSequences['"+guid+"'].checkLoadingStatus()",300);
            }

        }
        else
        {
            window.setTimeout("UniconSSOSequences['"+guid+"'].checkLoadingStatus()",300);
        }
        

    }
    
    // When sequence form has loaded, this is called.  Update sequence position and call next()
    this.onload = function()
    {
        sequence[seqPos].stopTime = (new Date()).getTime();
        seqPos++;
        thisRef.next();
    }

}

// Class Method
// Set form parameters so they are ready for submission
// @param listOfSeq         - Array of Strings representing UniconSSOSequences ids involved
// @param paramSourceType   - String identifying the security type ("basic","ajax")
// @param callbackUrl       - Url to use for a callback to the server
UniconURLSequencer.setFormParameters = function (listOfSeq,securityType,callbackUrl)
{
   var verifiedListOfSeq = [];
   for (var i=0; i<listOfSeq.length; i++)
   {
       var seqId = listOfSeq[i];
       if (UniconSSOSequences[seqId])
       {
           verifiedListOfSeq[verifiedListOfSeq.length] = seqId;
       }
   }
   if (securityType == "ajax" && callbackUrl)
   {
       // Set success handler for parsing Form Parameters out of response and inserting them into the appropriate form
       //     setting isReady = true for each in listOfSeq when complete.
       // Set failure handler??? - perhaps use static html error screen to populate the desired send window
       // Make Ajax callback to get Form Parameters
       var callbackObj = new UniconCallback(callbackUrl,verifiedListOfSeq,securityType);
       callbackObj.makeAjaxCall();
   }   
   else
   {
       // isReady = true for each send
       for (var i=0; i<verifiedListOfSeq.length; i++)
       {
           var seqId = verifiedListOfSeq[i];
           var seqRef = UniconSSOSequences[seqId];
           seqRef.isReady = true;
           if (seqRef.isStartRequested)
           {
               seqRef.start();
           }
       }
   }
}

// UniconCallback used to manage Ajax callbacks (one per each callback needed)
// @param verifiedListOfSeq - Array of Strings representing UniconSSOSequences ids involved
// @param securityType      - String identifying the security type ("basic","ajax")
// @param callbackUrl       - Url to use for a callback to the server
UniconCallback = function(callbackUrl,verifiedListOfSeq,securityType)
{
    var thisRef = this;

    var transaction;
    var tries = 0; // counter to keep track of retries
    
    // Instance Method
    // Starts an Ajax transaction and increments the number of tries
    this.makeAjaxCall = function()
    {
       tries++;
       var callback =
       {
          success: thisRef.handleAjaxResponse,
          failure: thisRef.handleAjaxFailure,
          argument: verifiedListOfSeq
       }
       transaction = YAHOO.util.Connect.asyncRequest('POST', callbackUrl, callback, "dummy=value"); // dummy=value sent for Firefox request to IIS

    }
    // Instance Method
    // Handle the Ajax Success Response by advising of error, or filling in form parameter values
    // @param o - Yahoo connection object reference to provide results of the Ajax transaction 
    this.handleAjaxResponse = function(o)
    {
        var response = o.responseText; // response as text
        var errorRedirectRE = /<error[^>]*redirect="([^"]*)"/;
        
        // Redirect page if error response indicates redirect is needed (i.e. session expired)
        if (response.search(errorRedirectRE) > -1)
        {
            window.location.href=RegExp.$1;
            return;
        }
        // Show alert if an error code is returned by the callback
        else if (response.indexOf("<error") > -1)
        {
            alert('NOTE: An error has occurred while trying to sign into another application.\n\nPlease contact your Portal Administrator for help.');
            return;
        }

        var sends = response.split('</send>'); // array of strings containing roughly contents of <send /> element of response
        var paramNameRE = /name="([^"]*)"/; // Regular Expression for obtaining name attribute value
        var valueRE = />([^<]*)<\/value>/; // Regular Expression for obtaining value text
        
        // Dependent variables set below
        var seqId; // sequence Id
        var seqRef; // reference to the sequence object
        var sendIds; // array of send Ids registered with the sequence object
        var formRef; // reference to a form object
        var params; // array of strings containing roughly contents of <parameter /> element of response
        var paramName; // form parameter name - String
        var value; // form value - String

        // Index variables used in for-loops
        var sId; // index for s(end)Id within sendIds array
        var rS; // index for r(esponse)S(end) within sends array of text
        var pI; // p(arameter)I(ndex) within params array
        
        // Loop thru each Sequence Id identified in callback
        for (var i=0; i<o.argument.length; i++)
        {
            seqId = o.argument[i];
            seqRef = UniconSSOSequences[seqId];
            sendIds = seqRef.getSendIds();
            // Loop thru each send Id known for this Sequence
            for (sId=0; sId< sendIds.length; sId++)
            {
                formRef = seqRef.getFormRefBySendId(sendIds[sId]);
                // Loop thru response text by each section delineated by </send>
                for (rS = 0; rS<sends.length-1; rS++)
                {
                    // If send Id is found within text section, this must contain info for that form
                    if (sends[rS].indexOf('"'+sendIds[sId]+'"') > -1)
                    {
                        params = sends[rS].split('</parameter>');
                        // Loop thru the text section further delineated by </parameter>
                        for (pI = 0; pI< params.length-1; pI++)
                        {
                            // Grab parameter name and value and set values in form
                            paramName = (params[pI].search(paramNameRE) > -1)? RegExp.$1:null;
                            value = (params[pI].search(valueRE) > -1)? RegExp.$1:null;
                            if (formRef[paramName])
                            {
                                formRef[paramName].value = value;
                            }
                        }
                    }
                }
            }
            seqRef.isReady = true; // set flag to indicate form is now ready to be submitted as values should be set
        }
        // Loop thru again, and start any Sequences that have been requested to be started
        for (var i=0; i<o.argument.length; i++)
        {
            seqId = o.argument[i];
            seqRef = UniconSSOSequences[seqId];
            if (seqRef.isStartRequested)
            {
                seqRef.isStartRequested = false;
                seqRef.start();
            }
        }
             
    }

    // Instance Method
    // Handle the Ajax Failure Response by advising of error, or if no failure info to retry until try attempts exceeded
    // @param o - Yahoo connection object reference to provide results of the Ajax transaction 
    this.handleAjaxFailure = function(o)
    {
        // How could it fail?
        // * bad callback URL in configuration or servlet not available - AJAX failure
        // * dropped internet connection from either client or server - AJAX failure
        // * callback response could not be generated for some reason (i.e. invalid session) - AJAX success with <error />
        // * Firefox mysterious fails callback with no info - AJAX failure that seems to get resolved with a retry
        if (!o.status && tries < 10)
        {
            thisRef.makeAjaxCall();
        }
        else
        {
            // Show alert if an Ajax failure occurs during the callback
            alert('NOTE: An error has occurred while trying to sign into another application.\n\nThis may be caused by an interrupted Internet connection, or a configuration issue.\n\nPlease contact your Portal Administrator for help.');
        }
        return;
    }
}

if (!window.UniconSSOSequences)
{
    window.UniconSSOSequences = {};
}


/*
  SAMPLE HTML IMPLEMENTATION CODE
*/
/*
<div id="HTMLBody_275" class="portlet-container-content">
<!--Comment to Fix Serializer Bug?-->
<script src="/toro-portlets-common/rendering/javascript/uniconURLSequencer.js" language="JavaScript" type="text/javascript">
</script>

<!-- Yahoo Ajax Lib -->
<script src="/toro-portlets-common/rendering/javascript/yui/yahoo.js">
</script>

<script src="/toro-portlets-common/rendering/javascript/yui/connection.js">
</script>

         <form action="/toro-portlets-common/rendering/html/sso/resetPage.html" id="sequencer_PortletInstanceID_SequenceID_resetForm" target="YahooPopUpWindow" method="get" enctype="application/x-www-form-urlencoded">
            <input type="hidden" name="guid" id="sequencer_PortletInstanceID_SequenceID_resetForm_guid" value="" />
         </form>

         <form name="PortletInstanceID_SendId884_form" id="PortletInstanceID_SendId884_form" action="http://login.yahoo.com/config/login" method="GET" target="YahooPopUpWindow" style="display:inline;">
            <input type="hidden" name=".partner" value="" />
            <input type="hidden" name=".intl" value="" />
            <input type="hidden" name=".src" value="" />
            <input type="hidden" name="logout" value="" />
            <input type="hidden" name=".done" value="" />
         </form>

         <form name="PortletInstanceID_SendId906_form" id="PortletInstanceID_SendId906_form" action="https://login.yahoo.com/config/login" method="POST" target="YahooPopUpWindow" style="display:inline;">
            <input type="hidden" name=".intl" value="" />
            <input type="hidden" name=".challenge" value="" />
            <input type="hidden" name="login" value="" />
            <input type="hidden" name="passwd" value="" />
            <input type="hidden" name=".src" value="" />
            <input type="hidden" name=".done" value="" />
            <input type="hidden" name="sb" value="" />
         </form>

         <div class="portlet-font portlet GatewayPortlet GatewayPortlet_gateway_main">
            <h2>Gateway Portlet Test</h2>

            <a title="My yahoo in a popup" href="javascript:UniconSSOSequences['sequencer_PortletInstanceID_SequenceID'].start();" onclick="window.open('','YahooPopUpWindow','width=600,height=600,location=yes,toolbar=yes,status=yes,resizable=yes,scrollbars=yes')" class="largeLink"><span class="largeLink">My Yahoo</span></a>

            <script language="JavaScript" type="text/javascript">
                UniconSSOSequences["sequencer_PortletInstanceID_SequenceID"] = new UniconURLSequencer("sequencer_PortletInstanceID_SequenceID");

                UniconSSOSequences["sequencer_PortletInstanceID_SequenceID"].add("PortletInstanceID_SendId884_form","abc123");
                
                UniconSSOSequences["sequencer_PortletInstanceID_SequenceID"].add("PortletInstanceID_SendId906_form","abc124");
                UniconURLSequencer.setFormParameters(["sequencer_PortletInstanceID_SequenceID"],"ajax","ssoResponseSample.xml"); // DONE defining sequences, now load parameters for them
            </script>

            <p>My yahoo in a popup</p>
         </div>
      </div>
*/


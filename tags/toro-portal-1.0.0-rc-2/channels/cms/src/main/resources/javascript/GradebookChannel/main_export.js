/**
*   <!-- UNICOMMENT 
*   Class:     OnLoadRegistryObject  
*   The OnLoadRegistryObject provides a way to safely add additional onload
*   event listeners without knowing what other listeners may have already
*   have been specified.
*
*   Owner:      Shawn Lonas
*   Revision:   1.0
*
*   Purpose:    To safely add a new listener to the window.onload event
*   Design:     When a new object is created, it will copy any previous
*               listener reference into a new array, and replace the main
*               listener reference with a reference to a method that will call
*               the listeners in order.
*
*   Object Life Cycle:  Created as needed on the page.  Becomes obsolete after
*                       the onload event is over.
*
*   UNICOMMENT -->
*/
OnLoadRegistryObject = function()
{
    var events = new Array();
    
    // If window.onload has an event listener already, add to events array
    if (window.onload != null)
    {
        events[events.length] = window.onload;
    }
    
    /**
    *   <!-- UNICOMMENT 
    *   add places a new listener on the onload event.
    *
    *   Method:     add
    *
    *   Parameters: 
    *       ref   Object Reference 
    *
    *   Return: void
    *   UNICOMMENT -->
    */
    this.add = function(ref)
    {
        events[events.length] = ref;
        window.onload = this.handleEvent;
    }
    
    /**
    *   <!-- UNICOMMENT 
    *   handleEvent is the method that is called by the browser when the
    *   onload event occurs.  It calls all of the register events in the 
    *   events array.
    *
    *   Method:     handleEvent
    *
    *   Parameters: 
    *       e   Event Object Reference (Browser dependent) 
    *
    *   Return: void()
    *   UNICOMMENT -->
    */
    this.handleEvent = function(e)
    {
        for (var i=0; i< events.length; i++)
        {
            events[i](e);
        }
    }
}

// Function to set onload event to launch export window
GBExportOnloadHandler = function()
{
    // Only generate export file if the current command is to export
    if ((window.location.href.indexOf("command=export") > -1 && window.name.indexOf("Data") == -1) || 
         (window.lastChannelCommand && window.lastChannelCommand == "export"))
    {
        generateExportFile();
    }
}

var GBExportOnload = new OnLoadRegistryObject();
GBExportOnload.add(GBExportOnloadHandler);

// Function to pop-up a window containing the delimited file, for export
// Will automatically trigger Save As in IE (on a PC), NS will need to save manually
// Input: No parameters, but requires global variable gradeBookCols to contain the
//  number of columns in the gradebook.
generateExportFile = function ()
{
    var fEle = document.gradebookForm.elements;
    // headerCol0, weightingCol0, percentCol0, user0Col0, meanCol0, medianCol0
    var dataString = "";
    var counter = 0;
    for (var i=0; i< fEle.length; i++)
    {
        if (fEle[i].type == "hidden" && fEle[i].name.indexOf("Col") != -1)
        {
            if (isNaN(parseFloat(fEle[i].value)))
            {
                dataString += '"' + fEle[i].value + '"|';
            }
            else
            {
                dataString += fEle[i].value + "|";
            }
            counter += 1;
            if (counter == gradeBookCols)
            {
                dataString += "\n";
                counter = 0;
            }                        
        }
    }
    
    if (window.ActiveXObject)
    {
        var outputWin = window.open();
        outputWin.document.open("text/plain");
        outputWin.document.write(dataString);
        outputWin.document.close();
        outputWin.document.execCommand('SaveAs',null,'gradebook.txt');
        outputWin.close();
    }
    else if (false && document.layers && java.awt)
    {
        /*
            Currently not functioning as expected for NS4, therefore ignore for now
        */
        // turn on privileges for writing   
        netscape.security.PrivilegeManager.enablePrivilege("UniversalFileWrite")
        // create frame and dialog   
        var frame = new java.awt.Frame()    
        var dlog = new java.awt.FileDialog(frame, "Save As...", java.awt.FileDialog.SAVE)    
        // bring dialog to front and show it   
        dlog.toFront()    
        dlog.show()    
        // capture path selected by user and file name entered into dialog field   
        var filename = dlog.getDirectory() + dlog.getFile()    
        // do the Java stuff for writing the data to that file   
        var outputStream = new java.io.FileOutputStream(filename)
        var writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputStream))
        writer.write(dataString)    
        // flush the queue and close everything up   
        writer.flush()    
        writer.close()    
        outputStream.close()    
        netscape.security.PrivilegeManager.disablePrivilege("UniversalFileWrite")
    }
    else
    {    
        var outputWin = window.open();
        outputWin.document.open();
        outputWin.document.writeln("<textarea rows='4' cols='80'>");
        outputWin.document.write(dataString);
        outputWin.document.writeln("</textarea>");
        outputWin.document.close();
        outputWin.alert("Copy the information above and paste it into a text editor (i.e. Notepad).\n\nSave the file as a text file (i.e. txt).");
    }
}

stripWhitespace = function(s) {
    return s.replace(/[ \t\n\r]+/,'');
}

checkSubmit = function() {

    var formElement;
    var name;
    var value;

    for (var i=0; i < document.gradebookForm.elements.length; i++) {

        formElement = document.gradebookForm.elements[i];

        if (!formElement.name || formElement.name.indexOf("SCORE_") < 0) continue;

        value = stripWhitespace(formElement.value);

        if (formElement.value == '') continue;

        if (!isNumber(value)) {
            alert('Invalid number entered.');
            return false;
        }
    }
    return true;
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
            alert('Invalid number entered.');
            return;
        }
    }
    var changeLog = document.gradebookForm.changeLog.value;
    var testString = ref.name+"|";

    if (changeLog.indexOf(testString)==-1)
    {
        changeLog += testString;
    }
    document.gradebookForm.changeLog.value = changeLog;
    updateMeanMedian(ref);
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
        document.gradebookForm["Mean_"+colnum].value = totalScore/(scoreArray.length);
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

// Every below was added to support the copying of functions from one window to
// another so that client-side optimizations would work

GradebookChannelFunctions = ["initializeGradebookChannel","stripWhitespace",
                            "checkSubmit","isNumber","recordOnChange",
                            "updateMeanMedian","numericalSort",
                            "generateExportFile","GBExportOnloadHandler",
                            "OnLoadRegistryObject"];

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
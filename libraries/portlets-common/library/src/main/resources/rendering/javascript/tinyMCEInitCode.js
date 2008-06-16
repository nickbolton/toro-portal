// Code to initialize tinyMCE to work within AcademusApps

// Run only if all of the following are true: 
// 1) tinyMCE is found (i.e. JS code loaded correctly)
// 2) It has not not already been initialized
// 3) If not Safari
if(window.tinyMCE && !window.tinyMCE.isInitialized && !window.isAccessibleSkin && navigator.userAgent.indexOf('Safari') == -1)
{
   window.tinyMCE.init({
        mode : "specific_textareas",
        editor_selector : "mceEditor",
        theme : "advanced",
        add_form_submit_trigger : false,
        plugins : "table,advhr,advimage,advlink,emotions,insertdatetime,preview,searchreplace,print",
        theme_advanced_buttons1 : "undo,redo,separator,cut,copy,paste,separator,justifyleft,justifycenter,justifyright,justifyfull,separator,bullist,numlist,separator,outdent,indent,separator,visualaid,cleanup,code,separator,print,preview,help",
        theme_advanced_buttons2 : "bold,italic,underline,strikethrough,sub,sup,forecolor,separator,formatselect,fontselect,fontsizeselect",
        theme_advanced_buttons3 : "removeformat,separator,link,unlink,anchor,separator,image,separator,advhr,charmap,insertdate,inserttime,separator,tablecontrols",
        theme_advanced_toolbar_location : "top",
        theme_advanced_toolbar_align : "left",
        theme_advanced_path_location : "bottom",
        plugin_insertdate_dateFormat : "%Y-%m-%d",
        plugin_insertdate_timeFormat : "%H:%M:%S",
        extended_valid_elements : "a[name|href|target|title|onclick],img[class|src|border=0|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],hr[class|width|size|noshade],font[face|size|color|style],span[class|align|style]"
   });
   // Set an initialization flag so if another portal also called it will not duplicate the initialization
   window.tinyMCE.isInitialized = true;    
}



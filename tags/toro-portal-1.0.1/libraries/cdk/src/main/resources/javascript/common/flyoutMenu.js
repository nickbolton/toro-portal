/* 
    NOTE: File has been marked up to generate documentation through JSDoc (http://jsdoc.sourceforge.net/)
    However, it does not correctly identify and represent inner "classes" as such.
*/

/** 
 * @fileoverview This file is necessary to create and run flyout menus
 *
 * @author Shawn Lonas <a href="mailto:shawn@unicon.net">shawn@unicon.net</a>
 * @version 1.0
 */

/**
 *  Returns a Unicon Flyout Object that when initialized will handle the creation
 *  and handling of flyout menus on the page.
 *  @class                      This is the base encapsulation class for flyout menus.
 *  @constructor
 *  @param UniconFlyoutName     a unique name that will be used for this Flyout in the global namespace.
 *  @param hideMenuClass        a CSS class that will handle the hiding of menus.  A default will be used if not provided.
 *  @param showMenuClass        a CSS class that will handle the showing of menus.  A default will be used if not provided.
 *  @param baseOptionClass      a CSS class that is used for the base (i.e. non-rollover) state of a menu option. A default is provided.
 *  @param rolloverOptionClass  a CSS class that is used for the rollover state of a menu option. A default is provided.
 *  @return                      a flyout object that can be initialized to create and run menus
 */
function UniconFlyoutObject (UniconFlyoutName,hideMenuClass,showMenuClass,baseOptionClass,rolloverOptionClass)
{
    var allMenus = {};
    var topMenus = {};

    // Classes for styling and controlling Menus
    hideMenuClass = hideMenuClass || "hide-flyout-menu";
    showMenuClass = showMenuClass || "show-flyout-menu";
    baseOptionClass = baseOptionClass || "flyout-menu-option";
    rolloverOptionClass = rolloverOptionClass || "flyout-menu-option-over";

    var thisRef = this;

    /**
     *  Returns a Menu Object that represents a container for menu options.
     *  
     *  @class          This is the menu class for flyout menus.
     *  @constructor
     *  @param ref      a ref to the DOM element that represents the menu container (i.e. ul or div)
     *  @return         a Menu Object that can be initialized and will be used to represent the Menu data structure.
     *  @type MenuObject
     */
    this.MenuObject = function (ref)
    {
        this.ref = ref;
        this.parentMenu = null;
        this.parentOption = null;
        this.options = new Array();
        this.isOverMenu = false;
        this.isMenuOpen = false;
        this.currentOptionRef = null;
        this.top = null;
        this.currentExpandedPath = [this];
        this.thisMenuPath = new Array();
        
        var thisRef = this;
        var offMenuCounter = 0;
        
        // Calculate the Rollover className once (assuming it won't being changing elsewhere)
        var closedClassName = "";
        var openedClassName = "";
        if (ref.className)
        {
            closedClassName = ref.className;
            var classes = ref.className.split(" ");
            var className;
            
            for (var i=0; i< classes.length; i++)
            {
                className = classes[i];
                if (className == hideMenuClass)
                {
                    openedClassName += showMenuClass + " ";
                }
                else
                {
                    openedClassName += className + " ";
                }
            }
        }

        /**
         *  Initializes this Menu Object, and all submenus, making sure it is ready to function by 
         *  setting the global path to each.
         *  
         *  @param path     an array representing the path to this Menu Object (from the top)
         *  @return         None
         */
        this.initialize = function (path)
        {
            var newPath = new Array();
            if (!path)
            {
                thisRef.top = thisRef;
            }
            else
            {  
                thisRef.top = path[0];
                for (var i=0; i< path.length; i++)
                {
                    newPath[newPath.length] = path[i];      
                }
            }
            newPath[newPath.length] = thisRef;
            thisRef.thisMenuPath = newPath;
            for (i=0; i< thisRef.options.length; i++)
            {
                thisRef.options[i].initialize(newPath);
            }
        };
        
        /**
         *  Checks the Menu Status to determine if submenus need to be closed or left open.
         *
         *  @return         None
         */
        this.checkMenuStatus = function ()
        {
            // If not over any submenu
            if (!thisRef.isOverMenu && thisRef.isMenuOpen)
            {
                // If counter has reached 7 or about 1.4 secs
                if (offMenuCounter>6)
                {
                    thisRef.closeSubMenus();
                    offMenuCounter = 0;
                    thisRef.isMenuOpen = false;
                } 
                else // else increment counter
                {
                    offMenuCounter++;
                }
            }
            else
            {
                offMenuCounter = 0;
            }
        };
        
        /**
         *  Adds another Menu option to the end of the options
         *
         *  @param  optionRef   a reference to the MenuOptionObject that is an option
         *  @return             None
         */
        this.addOption = function (optionRef)
        {
            var optionsLength = this.options.length;
            this.options[optionsLength] = optionRef;
            optionRef.indexInParent = optionsLength;
            optionRef.parent = thisRef;
        };
        
        /**
         *  Call to cause Menu to show itself only
         *
         *  @return             None
         */
        this.open = function ()
        {
            thisRef.ref.className = openedClassName;
        };
        
        /**
         *  Call to cause Menu to hide itself
         *
         *  @return             None
         */
        this.close = function ()
        {
            thisRef.ref.className = closedClassName;
        };
        
        /**
         *  This methods closes all subMenus
         *
         *  @return             None
         */
        this.closeSubMenus = function ()
        {
            
            // close this menu if not top menu
            if (thisRef.parentMenu)
            {
                thisRef.close(); 
            }
            // loop thru all menu options and close all options
            for (var i=0; i< thisRef.options.length; i++)
            {
                thisRef.options[i].close();
            }
        };
        
    };

    /**
     *  Returns a Menu Option Object that represents a choice within the Menu, specifically an anchor/link.
     *  
     *  @class          This is the menu option class for flyout menus.
     *  @constructor
     *  @param aRef     a ref to the DOM element (<a/> that represents the menu option)
     *  @return         Menu Option Object
     *  @type MenuOptionObject
     */
    this.MenuOptionObject = function (aRef)
    {
        this.parent = null;
        this.child = null;
        this.indexInParent = 0;
        this.top = null;
        this.thisMenuPath = new Array();
        
        var thisRef = this;

        // Calculate the Rollover className once (assuming it won't being changing elsewhere)
        var baseClassName = "";
        var rolloverClassName = "";
        if (aRef.className)
        {
            baseClassName = aRef.className;
            var classes = aRef.className.split(" ");
            var className;
            
            for (var i=0; i< classes.length; i++)
            {
                className = classes[i];
                if (className == baseOptionClass)
                {
                    rolloverClassName += rolloverOptionClass + " ";
                }
                else
                {
                    rolloverClassName += className + " ";
                }
            }
        }
        
        /**
         *  Initializes this Menu Option Object and a child submenu (if applicable) making sure it is ready to function by 
         *  setting the global path to each.
         *  
         *  @param path     an array representing the path to this Menu Object (from the top)
         *  @return         None
         */
        this.initialize = function (path)
        {
            thisRef.top = path[0];
            var newPath = new Array();
            for (var i=0; i< path.length; i++)
            {
                newPath[newPath.length] = path[i];      
            }
            newPath[newPath.length] = thisRef;
            thisRef.thisMenuPath = newPath;
            if(thisRef.child)
            {
                thisRef.child.initialize(newPath);
            }
        };
        
        /**
         *  Adds a menu as a child of this option (i.e. submenu)
         *  
         *  @param menuRef  a reference to the child submenu (Menu Object)
         *  @return         None
         */
        this.addSubMenu = function (menuRef)
        {
            this.child = menuRef;
            menuRef.parentOption = thisRef;
            menuRef.parentMenu = thisRef.parent;
        };

        /**
         *  Opens childs submenu (if applicable)
         *  
         *  @return         None
         */
        this.open = function ()
        {
            if (thisRef.child)
            {
                thisRef.child.open();
            }
        };
        
        /**
         *  Closes all descendant menus (invoked from parent menu)
         *  
         *  @return         None
         */
        this.close = function ()
        {
            aRef.className = baseClassName;
            if (thisRef.child)
            {
                thisRef.child.closeSubMenus();
            }
        };
        
        /**
         *  Handles mouseOver events by swapping image, opening subMenu, and sending 
         *  message to parent notifying it that mouse is over this option
         *  
         *  @return         None
         */
        this.over = function ()
        {
            aRef.className = rolloverClassName;
            thisRef.open();
            
            var currentExpandedPath = thisRef.top.currentExpandedPath;
            var isDone = false;
            for (var i=0; (i<currentExpandedPath.length) && (i<thisRef.thisMenuPath.length) && (!isDone); i++)
            {
                if (currentExpandedPath[i] != thisRef.thisMenuPath[i])
                {
                    currentExpandedPath[i].close();
                    thisRef.top.currentExpandedPath = thisRef.thisMenuPath;
                    isDone = true;
                }
            }
            if (!isDone)
            {
                thisRef.top.currentExpandedPath = thisRef.thisMenuPath;
            }
            thisRef.top.isOverMenu = true;
            thisRef.top.isMenuOpen = true;
            thisRef.top.currentOptionRef = thisRef;
        };
        
        /**
         *  Handles mouseOut events by swapping image back and sending message to parent
         *  notifying it that mouse is no longer over subMenu
         *  
         *  @return         None
         */
        this.out = function ()
        {
            thisRef.top.isOverMenu = false;
            thisRef.top.currentOptionRef = null;
        };
        
    };

    /**
     *  Initializes All Flyouts on web page by identifying those that have been marked with
     *  special CSS classes, and creating Menu and Menu Option Objects to handle them.
     *  
     *  @return         None
     */
    this.InitializeFlyouts = function ()
    {
        var anchors = document.getElementsByTagName("a");
        var a, anchorId, menuId, submenuId, menuOption, menuRef, submenuRef, parentRef;
        var parentMenuRegEx = /parent-menu_([^ ]*)/;
        var childMenuRegEx = /child-menu_([^ ]*)/;
        // Loop thru all anchors to look for flyout menu options
        for (var i=0; i<anchors.length; i++)
        {
            a = anchors[i];
            if (a.className.indexOf("flyout-menu-option") > -1)
            {
                // If valid parent menu described with class names, then add MenuObject for parent
                // and option to parent
                if (a.className.indexOf("parent-menu") > -1 && parentMenuRegEx.test(a.className))
                {
                    menuId = RegExp.$1;
                    menuRef = document.getElementById(menuId);
                    if (menuRef)
                    {
                    if (!allMenus[menuId])
                    {
                            allMenus[menuId] = new thisRef.MenuObject(menuRef);
                    }
                    anchorId = a.id || "flyout-anchor-"+i;
                    menuOption = new thisRef.MenuOptionObject(a);
                    
                    a.optionRef = menuOption; // add ref from anchor to menuOption
                    if (a.addEventListener)
                    {
                        a.addEventListener("mouseover",menuOption.over,false);
                        a.addEventListener("mouseout",menuOption.out,false);
                        a.addEventListener("focus",menuOption.over,false);
                        a.addEventListener("blur",menuOption.out,false);
                    }
                    else if (a.attachEvent)
                    {
                        a.attachEvent("onmouseover",menuOption.over);
                        a.attachEvent("onmouseout",menuOption.out);
                        a.attachEvent("onfocus",menuOption.over);
                        a.attachEvent("onblur",menuOption.out);
                    }
                    else
                    {
                        a.onmouseover = menuOption.over;
                        a.onmouseout = menuOption.out;
                        a.onfocus = menuOption.over;
                        a.onblur = menuOption.out;
                    }
                    allMenus[menuId].addOption(menuOption);

                    // If Child Menu defined Hook up Sub-menu
                    if (a.className.indexOf("child-menu") > -1 && childMenuRegEx.test(a.className))
                    {
                        submenuId = RegExp.$1;
                            submenuRef = document.getElementById(submenuId);
                            if (submenuRef)
                                {
                        if (!allMenus[submenuId])
                        {
                                    allMenus[submenuId] = new thisRef.MenuObject(submenuRef);
                        }
                        menuOption.addSubMenu(allMenus[submenuId]);
                            }
                        }
                    }
                }
            }
        }
        // Loop thru all menus to determine if top level menu or submenu.  If submenu attach as submenu.
        for (i in allMenus)
        {
            if (!allMenus[i].parentMenu)
            {
                topMenus[i] = allMenus[i];
            }
        }
        
        // Initialize each of the top menus
        for (i in topMenus)
        {
            topMenus[i].initialize();
        }

        // Set to close menus when clicking on document    
        if (document.addEventListener)
        {
            document.addEventListener("click",thisRef.closeMenus,false);
        }
        else if (document.attachEvent)
        {
            document.attachEvent("onclick",thisRef.closeMenus);
        }
        
        // Start check for roll-off of navigation (to close after a time delay when rolling mouse off of menu)
        setInterval("window."+UniconFlyoutName+".checkMenuStatuses()",100);

    };

    /**
     *  Checks the Menu Statuses for all top level menus.
     *  
     *  @return         None
     */
    this.checkMenuStatuses = function()
    {
        for (var i in topMenus)
        {
            topMenus[i].checkMenuStatus();
        }
    };

    /**
     *  Closes all top level menus (and all their submenus)
     *  
     *  @return         None
     */
    this.closeMenus = function()
    {
        for (var i in topMenus)
        {
            topMenus[i].closeSubMenus();
        }
    };

};
// END Unicon Flyout Object


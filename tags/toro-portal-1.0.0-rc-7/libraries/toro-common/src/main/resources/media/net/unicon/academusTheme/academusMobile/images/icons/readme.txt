Icon Readme Notes 1.21.04

Nathan Pearson (npearson@unicon.net)
--------------------------------------------

A set of icons representing each relevant state has been created for the "admin.gif" icon.  These icons are titled:

channel_admin_base.gif
channel_admin_selected.gif
channel_admin_active.gif
channel_admin_inactive.gif

In order to use this set in place of the existing admin.gif icon, the channel XSL that points to that icon will need to be changed.



Icon Readme Notes 1.19.04

Nathan Pearson (npearson@unicon.net)
--------------------------------------------

Icons are broken up into two main categories:

1. Skin dependant icons
2. Non-skin dependent icons

Non-skin dependent icons are icons that are located within channel specific directories or elsewhere in the system.  These icons, though somewhat important to understand in relation to skin dependent icons will not be discussed in these notes at this time.

Skin dependent icons are the icons that are included in each customer or deployable skin.  Though these icons may be changed from skin to skin to accommodate a visual style, in general they are the same set of icons used recurrently for each skin.

Skin dependent icons are located in the following directory:

../media/org/jasig/portal/layout/tab-column/nested-tables/<skin name>/controls


In addition to the actual icon files located in this directory, there is also a sub-directory titled "psd".

This sub-directory contains the source files for each icon.  Source files can be used to make style changes to the icons or to add additional icons to the set.

Important:  If style changes are made to the source files per customer request, those changes should be maintained for that customer only.  If the icon changes are global changes or effect all customers, those files should be updated system-wide in the repository (currently CVS).

The "psd" sub-directory contains the following directories:

../psd/channel-content-icons
../psd/channel-header-icons


The "channel-content-icons" director currently includes the following sub-directories:

../psd/channel-content-icons/calendar
../psd/channel-content-icons/main
../psd/channel-content-icons/misc
../psd/channel-content-icons/page
../psd/channel-content-icons/peephole

Some of these sub-directories may contain additional directories to support the various states of icons within the given set.  But in genetal, these are all the skin dependent icon source files that create the icons that live inside a channel.

The "channel-header-icons" directory currently includes all icon source files that create the icons used in the channel header to control the channel itself.


----

Additional Notes:

In the root "controls" directory, there is currently an icon titled "admin.gif".  This icon is used for the Campus Announcements channel and is not a skin dependent icon.  Since this icon is inconsistent with the architecture of how icons are currently organized, it will need to be addressed at some point in the future by making changes to the XSL files that reference it.

However, at the time that this Readme file was written, there was a realization of related organizational improvements that could also be made to the Academus icon architecture which may require more time than currently available.

To that end, this icon has been left "as is" for now.


It also stands to mention that the larger "peephole icons" were designed in Adobe Illustrator and then imported into Photoshop where they were prepped as a set, including "on" and "off" states.  The original Illustrator sources files have not been included in any skin directories in order to protect sensitive work product.  However, these files are available to Unicon designers when needed.


----

Important Notes:

The main set of channel content icons includes several icons with filenames too long for Adobe ImageReady to support, thereby forcing ImageReady to cut-off one or more of the last letters in the filename.  This requires a manual filename adjustment to be made each time the icons are exported from ImageReady.

The icons which have been identified as having this problem are:

channel_xdistribute_selecte.gif > channel_xdistribute_selected.gif
channel_xdistribute_inactiv.gif > channel_xdistribute_inactive.gif

*This ImageReady specific issue may be related to the current 3.0 version.  Future versions of ImageReady may work fine.
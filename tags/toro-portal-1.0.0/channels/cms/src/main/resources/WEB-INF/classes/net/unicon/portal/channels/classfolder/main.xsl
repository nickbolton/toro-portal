<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <!-- Include Files -->
    <xsl:include href="common.xsl"/>
    <!--Catch the root node and set outer table -->
    <xsl:template match="/">
        <!--<textarea rows="4" cols="40">
                <xsl:copy-of select = "*"/>
                <parameter name="addResource"><xsl:value-of select="$addResource" /></parameter>
                <parameter name="editResource"><xsl:value-of select="$editResource" /></parameter>
                <parameter name="deleteResource"><xsl:value-of select="$deleteResource" /></parameter>
        </textarea> -->
        <xsl:call-template name="links"/>
        <xsl:call-template name="autoFormJS"/>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="class-folders">
        <!-- UniAcc: Data Table -->
        <table cellspacing="0" width="100%">
            <tr>
                <th class="th-top-single" id="ClassFolders">
                    Folders
                </th>
            </tr>
            <tr>
                <td class="table-content-single-bottom" style="text-align:left;padding-left:15;padding-bottom:15;padding-top:10" headers="ClassFolders">
                    <!-- Class Folder form data -->
                    <form name="ResourcesForm" action="{$baseActionURL}" method="post">
                        <input name="command" type="hidden" value="" />
                        <input name="nodeid" type="hidden" value="" />
                        <input name="nodename" type="hidden" value="" />
                        <input name="nodetype" type="hidden" value="" />
                        <input name="nodedesc" type="hidden" value="" />
                        <input name="nodeurl" type="hidden" value="" />
                    </form>
                    <!-- Start new menu body -->
                    <div id="relatedResourceMenuBody" name="relatedResourceMenuBody">&#160;</div>

                    <!-- Start JS for new menu -->
                    <script language="JavaScript" src="javascript/common/expandableTree.js"></script>
                    <script language="JavaScript" src="javascript/ResourceChannel/RelatedResourceRowObject.js"></script>
                    <script language="JavaScript" type="text/javascript">
                        postCommand = function(command, nodeid, nodetype,
                            nodename, nodedesc, nodeurl) {
                            document.ResourcesForm.command.value = command;
                            document.ResourcesForm.nodeid.value = nodeid;
                            document.ResourcesForm.nodetype.value = nodetype;
                            document.ResourcesForm.nodename.value = nodename;
                            document.ResourcesForm.nodedesc.value = nodedesc;
                            document.ResourcesForm.nodeurl.value = nodeurl;
                            document.ResourcesForm.submit();
                        }

                        initializeRelatedResources = function()
                        {
                            window.relatedResourceTable = new TreeTableObject("relatedResourceTable");
                            setRelatedResourceRowObjectStaticProperties();
                            RelatedResourceRowObject.imagePath = "<xsl:value-of select="$CONTROLS_IMAGE_PATH" />/";
                            RelatedResourceRowObject.imageSpacer = "<xsl:value-of select="$SPACER" />";
                            RelatedResourceRowObject.iconSrcPath = "<xsl:value-of select="$NAV_IMAGE_PATH" />/";
                            RelatedResourceRowObject.trailingImgSrcPath = "<xsl:value-of select="$CONTROLS_IMAGE_PATH" />/";

                            <!-- Apply templates for all children folders, urls, and files -->
                            <xsl:apply-templates select="(child::folder | child::url-element | child::file)">
                                <xsl:with-param name="path"></xsl:with-param>
                            </xsl:apply-templates>

                            relatedResourceTable.outputRef = document.getElementById("relatedResourceMenuBody");
                            relatedResourceTable.outputType = "HTMLElement";
                            relatedResourceTable.fromOutputToThis = "relatedResourceTable";
                            relatedResourceTable.iconSrcPath = "<xsl:value-of select="$NAV_IMAGE_PATH"/>/";
                            relatedResourceTable.trailingImgSrcPath = "<xsl:value-of select="$CONTROLS_IMAGE_PATH"/>/";
                            relatedResourceTable.onrolloverFunctionName = "swapImage";
                            relatedResourceTable.parseRows(); // prepare Tree to be drawn
                            relatedResourceTable.drawTree(); // draw Tree
                        }

                        var relatedResourceOnload = new OnLoadRegistryObject();
                        relatedResourceOnload.add(initializeRelatedResources);

                    </script>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!-- FOLDERs -->
    <xsl:template match="folder">
        <xsl:param name="path"/>
        <!-- Decide whether a . needs to be added before the position in the ordering (i.e. not needed before the first number)  -->
        <xsl:variable name="ORDERING">
            <xsl:choose>
                <xsl:when test="count(ancestor::*) = 2">
                    <xsl:value-of select="position()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$path"/>.<xsl:value-of select="position()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--<xsl:value-of select="name" />(<xsl:value-of select="$ORDERING" />)<br/> -->
        <xsl:variable name="URL">javascript:void(null);</xsl:variable>
        <!-- NAME avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="NAME">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="name"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- DESCRIPTION avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="DESCRIPTION">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="description"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- Build Content Hash used by JS to generate the control buttons for this row -->
        <!-- NOTE: choose was used instead of if to make it a little more readable (i.e. it all needs to be output on 1 line) -->
        <xsl:variable name="CONTENT_HASH">"id","<xsl:value-of select="generate-id()"/>"<xsl:choose>
                <xsl:when test="$addResource='Y'">,"add","javascript:postCommand('add', '<xsl:value-of select="@oid"/>', 'folder', '<xsl:value-of select="$NAME"/>', '', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$editResource='Y'">,"edit","javascript:postCommand('edit', '<xsl:value-of select="@oid"/>', 'folder', '<xsl:value-of select="$NAME"/>', '<xsl:value-of select="$DESCRIPTION"/>', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$deleteResource='Y'">,"delete","javascript:postCommand('delete', '<xsl:value-of select="@oid"/>', 'folder', '<xsl:value-of select="$NAME"/>', '', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:variable>

        relatedResourceTable.addRow(new RelatedResourceRowObject ("<xsl:value-of select="$ORDERING"/>",'<xsl:value-of select="$NAME"/>',"<xsl:value-of select="$URL"/>","folder",false,new SimpleObject(<xsl:value-of select="$CONTENT_HASH"/>)));
        <!-- Apply templates for all children folders, urls, and files -->
        <xsl:apply-templates select="(child::folder | child::url-element | child::file)">
            <xsl:with-param name="path">
                <xsl:value-of select="$ORDERING"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- URLs -->
    <xsl:template match="url-element">
        <xsl:param name="path"/>
        <!-- Decide whether a . needs to be added before the position in the ordering (i.e. not needed before the first number)  -->
        <xsl:variable name="ORDERING">
            <xsl:choose>
                <xsl:when test="count(ancestor::*) = 2">
                    <xsl:value-of select="position()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$path"/>.<xsl:value-of select="position()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--<xsl:value-of select="name" />(<xsl:value-of select="$ORDERING" />)<br/> -->
        <xsl:variable name="URL">
            <xsl:value-of select="hyperlink"/>
        </xsl:variable>
        <!-- NAME avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="NAME">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="name"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- DESCRIPTION avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="DESCRIPTION">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="description"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- Build Content Hash used by JS to generate the control buttons for this row -->
        <!-- NOTE: choose was used instead of if to make it a little more readable (i.e. it all needs to be output on 1 line) -->
        <xsl:variable name="CONTENT_HASH">"id","<xsl:value-of select="generate-id()"/>"<xsl:choose>
                <xsl:when test="$editResource='Y'">,"edit","javascript:postCommand('edit', '<xsl:value-of select="@oid"/>', 'url-element', '<xsl:value-of select="$NAME"/>', '<xsl:value-of select="$DESCRIPTION"/>', '<xsl:value-of select="hyperlink"/>');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$deleteResource='Y'">,"delete","javascript:postCommand('delete', '<xsl:value-of select="@oid"/>', 'url-element', '<xsl:value-of select="$NAME"/>', '', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:variable>

        relatedResourceTable.addRow(new RelatedResourceRowObject ("<xsl:value-of select="$ORDERING"/>","<xsl:value-of select="$NAME"/>","<xsl:value-of select="$URL"/>","url",true,new SimpleObject(<xsl:value-of select="$CONTENT_HASH"/>)));
        <!-- Apply templates for all children folders, urls, and files -->
        <xsl:apply-templates select="(child::folder | child::url-element | child::file)">
            <xsl:with-param name="path">
                <xsl:value-of select="$ORDERING"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    <!-- FILEs -->
    <xsl:template match="file">
        <xsl:param name="path"/>
        <!-- Decide whether a . needs to be added before the position in the ordering (i.e. not needed before the first number)  -->
        <xsl:variable name="ORDERING">
            <xsl:choose>
                <xsl:when test="count(ancestor::*) = 2">
                    <xsl:value-of select="position()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$path"/>.<xsl:value-of select="position()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--<xsl:value-of select="name" />(<xsl:value-of select="$ORDERING" />)<br/> -->
        <xsl:variable name="URL">
            <xsl:value-of select="$workerActionURL"/>&amp;oid=<xsl:value-of select="@oid"/>&amp;filetype=<xsl:value-of select="filetype"/>
        </xsl:variable>
        <!-- NAME avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="NAME">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="name"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- DESCRIPTION avoids javascript errors due
            to having single quotes escaped within the string -->
        <xsl:variable name="DESCRIPTION">
            <xsl:call-template name="translateQuotes">
                <xsl:with-param name="TEXTVALUE" select="description"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- Build Content Hash used by JS to generate the control buttons for this row -->
        <!-- NOTE: choose was used instead of if to make it a little more readable (i.e. it all needs to be output on 1 line) -->
        <xsl:variable name="CONTENT_HASH">"id","<xsl:value-of select="generate-id()"/>"<xsl:choose>
                <xsl:when test="$editResource='Y'">,"edit","javascript:postCommand('edit', '<xsl:value-of select="@oid"/>', 'file', '<xsl:value-of select="$NAME"/>', '<xsl:value-of select="$DESCRIPTION"/>', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$deleteResource='Y'">,"delete","javascript:postCommand('delete', '<xsl:value-of select="@oid"/>', 'file', '<xsl:value-of select="$NAME"/>', '', '');"</xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
        </xsl:variable>

        relatedResourceTable.addRow(new RelatedResourceRowObject ("<xsl:value-of select="$ORDERING"/>","<xsl:value-of select="$NAME"/>","<xsl:value-of select="$URL"/>","file",true,new SimpleObject(<xsl:value-of select="$CONTENT_HASH"/>)));
        <!-- Apply templates for all children folders, urls, and files -->
        <xsl:apply-templates select="(child::folder | child::url-element | child::file)">
            <xsl:with-param name="path">
                <xsl:value-of select="$ORDERING"/>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>

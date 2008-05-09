<?xml version='1.0' encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>

<xsl:template match="/">

  <xsl:for-each select="schema/tables/table/constraints/foreign-key">
      <xsl:call-template name="drop_constraint">
          <xsl:with-param name="CONSTYPE">FOREIGN KEY</xsl:with-param>
      </xsl:call-template>
  </xsl:for-each>

  <xsl:apply-templates select="schema/tables/table" mode="drop"/>

  <xsl:apply-templates select="schema/tables/table" mode="create"/>

  <xsl:for-each select="schema/tables/table/constraints/primary-key">
      <xsl:call-template name="create_constraint">
          <xsl:with-param name="CONSTYPE">PRIMARY KEY</xsl:with-param>
      </xsl:call-template>
  </xsl:for-each>

  <xsl:for-each select="schema/tables/table/constraints/unique-key">
      <xsl:call-template name="create_constraint">
          <xsl:with-param name="CONSTYPE">UNIQUE</xsl:with-param>
      </xsl:call-template>
  </xsl:for-each>

  <xsl:for-each select="schema/tables/table/constraints/foreign-key">
      <xsl:call-template name="create_constraint">
          <xsl:with-param name="CONSTYPE">FOREIGN KEY</xsl:with-param>
      </xsl:call-template>
  </xsl:for-each>

</xsl:template>

<xsl:template match="table" mode="drop">
<statement type="drop">
<xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
DROP TABLE <xsl:value-of select="name"/>
</statement><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="table" mode="create">
<statement type="create">
    <xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
    <xsl:text>CREATE TABLE </xsl:text>
       <xsl:value-of select="name"/>
    <xsl:text>(</xsl:text>
    <xsl:apply-templates select="columns/column"/>
    <xsl:text>)</xsl:text>
</statement><xsl:text>
</xsl:text>
<xsl:apply-templates select="indexes/index" mode="create"/><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template match="column">
  <xsl:text>  </xsl:text>
  <xsl:value-of select="name"/>
  <xsl:text> </xsl:text>
  <xsl:value-of select="type"/>
  <xsl:if test="param">(<xsl:value-of select="param"/>)</xsl:if>
  <xsl:if test="default/*">
      <xsl:if test="default/sequence">
        <xsl:text> IDENTITY </xsl:text>
      </xsl:if>
      <xsl:if test="default/integer or default/varchar">
          <xsl:text> DEFAULT </xsl:text>
          <xsl:value-of select="default"/>
      </xsl:if>
  </xsl:if>
  <xsl:if test="../../constraints/not-null/columns/column = node()">
      <xsl:text> NOT NULL </xsl:text>
  </xsl:if>
  <xsl:if test="position() != last()"><xsl:text>,
</xsl:text></xsl:if>
</xsl:template>

<xsl:template match="index" mode="create">
<xsl:if test="name != concat(../../constraints/primary-key/name,'') and
              name != concat(../../constraints/unique-key/name,'')"> 
    <statement type="create">
        <xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
        <xsl:text>CREATE </xsl:text>
        <xsl:if test="true() = starts-with(unique,'TRUE')">
            <xsl:text>UNIQUE </xsl:text>
        </xsl:if>
        <xsl:text>INDEX </xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text> ON </xsl:text>
        <xsl:value-of select="../../name"/>
        <xsl:text> (</xsl:text>
            <xsl:for-each select="columns/column">
                <xsl:value-of select="."/>
                <xsl:if test="position() != last()">
                    <xsl:text>, </xsl:text>
                </xsl:if>
            </xsl:for-each>
        <xsl:text>)</xsl:text>
    </statement><xsl:text>
    </xsl:text>
</xsl:if> 
</xsl:template>

<xsl:template name="drop_constraint" mode="drop">
    <xsl:param name="CONSTYPE"/>
<statement type="drop">
    <xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
    <xsl:text>ALTER TABLE </xsl:text>
    <xsl:value-of select="../../name"/>
    <xsl:text> DROP CONSTRAINT </xsl:text>
    <xsl:value-of select="name"/>
</statement><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template name="create_constraint" mode="create">
    <xsl:param name="CONSTYPE"/>
<statement type="create">
    <xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
    <xsl:text>ALTER TABLE </xsl:text>
    <xsl:value-of select="../../name"/>
    <xsl:text> ADD CONSTRAINT </xsl:text>
    <xsl:value-of select="name"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$CONSTYPE"/> 
    <xsl:text> (</xsl:text>
        <xsl:for-each select="columns/column">
            <xsl:value-of select="."/>
            <xsl:if test="position() != last()">
                <xsl:text>, </xsl:text>
            </xsl:if>
        </xsl:for-each>
    <xsl:text>)</xsl:text>
    <xsl:if test="name()='foreign-key'">
        <xsl:text> REFERENCES </xsl:text>
        <xsl:value-of select="parent-table/name"/>
        <xsl:text> (</xsl:text>
            <xsl:for-each select="parent-table/columns/key-column">
                <xsl:value-of select="."/>
                <xsl:if test="position() != last()">
                    <xsl:text>, </xsl:text>
                </xsl:if>
            </xsl:for-each>
        <xsl:text>)</xsl:text>
    </xsl:if>
</statement><xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>

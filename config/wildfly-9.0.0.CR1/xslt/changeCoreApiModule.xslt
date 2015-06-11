<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:module="urn:jboss:module:1.3" version="1.0">

  <xsl:param name="version.picketlink"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="//module:module[@name='org.picketlink.core.api']/module:resources"/>

  <xsl:template match="//module:module[@name='org.picketlink.core.api']/module:resources">
      <resources>
        <resource-root>
          <xsl:attribute name="path">picketlink-api-<xsl:value-of select="$version.picketlink"/>.jar</xsl:attribute>
        </resource-root>
      </resources>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
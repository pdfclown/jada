<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheet transforms bibliographic data (XML) into a bibliographic list (HTML).
-->
<x:stylesheet
  version="1.0"
  xmlns:b="https://pdfclown.org/ns/biblio"
  xmlns:x="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="b">
  <x:output
    method="html"
    indent="no"/>
  <x:strip-space elements="*"/>

  <x:template match="text()"/>

  <x:template match="b:biblio">
    <h1>Bibliography</h1>
    <x:call-template name="section">
      <x:with-param
        name="items"
        select="b:spec"/>
      <x:with-param
        name="title"
        select="'Specifications'"/>
    </x:call-template>
    <x:call-template name="section">
      <x:with-param
        name="items"
        select="b:doc"/>
      <x:with-param
        name="title"
        select="'Other publications'"/>
    </x:call-template>
    <x:call-template name="section">
      <x:with-param
        name="items"
        select="b:ref"/>
      <x:with-param
        name="title"
        select="'Other references'"/>
    </x:call-template>
  </x:template>

  <x:template match="b:spec|b:doc|b:ref|b:part|b:version">
    <x:param name="id-path"/>
    <x:param name="label"/>
    <x:param name="publisher"/>

    <x:variable name="version">
      <x:choose>
        <x:when test="@version">
          <x:value-of select="@version"/>
        </x:when>
        <x:when test="local-name() = 'version'">
          <x:value-of select="@id"/>
        </x:when>
      </x:choose>
    </x:variable>
    <x:variable name="cur-id-path">
      <x:if test="$id-path">
        <x:value-of select="$id-path"/>
        <x:choose>
          <x:when test="local-name() = 'part'">
            <x:text>/</x:text>
          </x:when>
          <x:when test="local-name() = 'version'">
            <x:text>:</x:text>
          </x:when>
        </x:choose>
      </x:if>
      <x:value-of select="@id"/>
    </x:variable>
    <x:variable name="cur-label">
      <x:choose>
        <x:when test="@title">
          <x:value-of select="@title"/>
        </x:when>
        <x:when test="$label">
          <x:value-of select="$label"/>
          <x:text> </x:text>
          <x:value-of select="$version"/>
        </x:when>
        <x:otherwise>
          <x:value-of select="@url"/>
        </x:otherwise>
      </x:choose>
    </x:variable>
    <x:variable name="cur-publisher">
      <x:choose>
        <x:when test="@publisher">
          <x:value-of select="@publisher"/>
        </x:when>
        <x:otherwise>
          <x:value-of select="$publisher"/>
        </x:otherwise>
      </x:choose>
    </x:variable>

    <dt id="{$cur-id-path}">
      <x:text>[</x:text>
      <x:value-of select="$cur-id-path"/>
      <x:text>]</x:text>
    </dt>
    <dd>
      <x:if test="@name">
        <code>
          <x:apply-templates select="@name"/>
        </code>
        <x:text> - </x:text>
      </x:if>
      <i>
        <x:call-template name="link">
          <x:with-param
            name="href"
            select="@src"/>
          <x:with-param
            name="text"
            select="$cur-label"/>
        </x:call-template>
      </i>
      <x:if test="@edition or @year or string($version)">
        <x:text> (</x:text>
        <x:if test="@year">
          <x:value-of select="@year"/>
        </x:if>
        <x:if test="@edition">
          <x:if test="@year">
            <x:text>, </x:text>
          </x:if>
          <x:text>edition </x:text>
          <x:value-of select="@edition"/>
        </x:if>
        <x:if test="string($version)">
          <x:if test="@edition|@year">
            <x:text>, </x:text>
          </x:if>
          <x:text>version </x:text>
          <x:choose>
            <x:when test="contains($version,'~')">
              <x:value-of select="substring-before($version,'~')"/>
            </x:when>
            <x:otherwise>
              <x:value-of select="$version"/>
            </x:otherwise>
          </x:choose>
        </x:if>
        <x:text>)</x:text>
      </x:if>
      <x:text>. </x:text>
      <x:if test="@author">
        <span class="biblio-author">
          <x:apply-templates select="@author"/>
        </span>
        <x:text>. </x:text>
      </x:if>
      <x:if test="$cur-publisher != ''">
        <x:value-of select="$cur-publisher"/>
        <x:text>. </x:text>
      </x:if>
      <div class="biblio-resources-extra">
        <x:if test="@url">
          <x:text>Home: </x:text>
          <x:call-template name="link">
            <x:with-param
              name="href"
              select="@url"/>
          </x:call-template>
        </x:if>
        <x:if test="b:see">
          <x:if test="@url">
            <br/>
          </x:if>
          <x:text>Related resources:</x:text>
          <ul>
            <x:apply-templates select="b:see"/>
          </ul>
        </x:if>
      </div>
      <x:if test="b:part|b:version">
        <dl>
          <x:apply-templates select="b:part|b:version">
            <x:with-param
              name="id-path"
              select="$cur-id-path"/>
            <x:with-param
              name="label"
              select="$cur-label"/>
            <x:with-param
              name="publisher"
              select="$cur-publisher"/>
          </x:apply-templates>
        </dl>
      </x:if>
    </dd>
  </x:template>

  <x:template match="b:see">
    <li>
      <x:call-template name="link">
        <x:with-param
          name="href"
          select="@url"/>
        <x:with-param
          name="text"
          select="@title"/>
      </x:call-template>
    </li>
  </x:template>

  <x:template name="link">
    <x:param name="href"/>
    <x:param
      name="text"
      select="$href"/>
    <x:variable name="content">
      <x:choose>
        <x:when test="$text">
          <x:value-of select="$text"/>
        </x:when>
        <x:otherwise>
          <x:value-of select="$href"/>
        </x:otherwise>
      </x:choose>
    </x:variable>

    <x:choose>
      <x:when test="$href">
        <a href="{$href}">
          <x:value-of select="$content"/>
        </a>
      </x:when>
      <x:otherwise>
        <x:value-of select="$content"/>
      </x:otherwise>
    </x:choose>
  </x:template>

  <x:template name="section">
    <x:param name="items"/>
    <x:param name="title"/>

    <x:if test="$items">
      <h2>
        <x:value-of select="$title"/>
      </h2>
      <dl>
        <x:apply-templates select="$items">
          <x:sort select="@id"/>
        </x:apply-templates>
      </dl>
    </x:if>
  </x:template>
</x:stylesheet>

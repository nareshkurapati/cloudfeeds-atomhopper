package com.rackspace.feeds.filter

import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.only
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.times

/**
 * Created by rona6028 on 9/25/14.
 */
class PrivateAttrsFilterTest extends Specification {


    @Unroll
    def "Should throw ServletException when xsltFile is not specified"() {

        when:
        PrivateAttrsFilter filter = new PrivateAttrsFilter()
        FilterConfig config = mock(FilterConfig)
        when(config.getInitParameter("xsltFile")).thenReturn(null)
        filter.init(config)

        then:
        thrown ServletException
    }

    @Unroll
    def "Should throw ServletException when xsltFile is specified, but doesn't exist"() {

        when:
        PrivateAttrsFilter filter = new PrivateAttrsFilter()
        FilterConfig config = mock(FilterConfig)
        when(config.getInitParameter("xsltFile")).thenReturn("/some/nonexistent/path")
        filter.init(config)

        then:
        thrown ServletException
    }

    @Unroll
    def "Should pass through when xsltFile exists & x-roles header contains cloudfeeds:service-admin"() {

        when:
        PrivateAttrsFilter filter = new PrivateAttrsFilter()
        FilterConfig config = mock(FilterConfig)
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        FilterChain chain = mock(FilterChain)
        File tempFile = File.createTempFile(this.getClass().canonicalName, "tmp")
        PrintWriter writer = new PrintWriter(tempFile)
        writer.append("""<xsl:stylesheet xmlns:event="http://docs.rackspace.com/core/event"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:httpx="http://openrepose.org/repose/httpx/v1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://wadl.dev.java.net/2009/02"
                version="2.0">
                <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
                <xsl:template match="@*|node()">
                </xsl:template>
                </xsl:stylesheet>""")
        when(config.getInitParameter("xsltFile")).thenReturn(tempFile.absolutePath)
        when(request.getHeaders("x-roles")).thenReturn( (new Vector( [ PrivateAttrsFilter.CF_ADMIN ].asList() ) ).elements() )
        filter.init(config)
        filter.doFilter(request, response, chain)

        then:
        verify(chain, only()).doFilter(request, response)
        tempFile.deleteOnExit()

    }
}

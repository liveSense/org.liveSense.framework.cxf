//$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/AbstractJaxWsServer.java $
//$Id: AbstractJaxWsServer.java 680 2011-09-12 16:57:25Z PRO-VISION\SSeifert $
package org.liveSense.framework.cxf.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * Abstract servlet-based implementation for CXF-based SOAP services. Ensures
 * that correct class loader is used is during initialization and invoking
 * phases. Via getCurrentRequest() and getCurrentResponse() it is possible to
 * access these objects from SOAP method implementations.
 */
public abstract class AbstractJaxWsServer extends CXFNonSpringServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Extension for SOAP requests
     */
    public static final String SOAP_EXTENSION = "soap";

    @Override
    public void init(ServletConfig pServletConfig) throws ServletException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // set classloader to CXF bundle class loader to avoid OSGI classloader problems
            Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

            super.init(pServletConfig);

            // register SOAP service
            ServerFactoryBean factory = new JaxWsServerFactoryBean();
            factory.setBus(getBus());
            factory.setAddress(RequestWrapper.VIRTUAL_PATH);
            factory.setServiceClass(getServerInterfaceType());
            factory.setServiceBean(this);
            factory.create();

        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    @Override
    protected void invoke(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException {
        RequestContext.getThreadLocal().set(new RequestContext(pRequest, pResponse));
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // set classloader to CXF bundle class loader to avoid OSGI classloader problems
            Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

            super.invoke(new RequestWrapper(pRequest), pResponse);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            RequestContext.getThreadLocal().remove();
        }
    }

    /**
     * @return Servlet request for current threads SOAP request
     */
    protected SlingHttpServletRequest getCurrentRequest() {
        RequestContext requestContext = RequestContext.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("No current soap request context available.");
        }
        return (SlingHttpServletRequest)requestContext.getRequest();
    }

    /**
     * @return Servlet response for current threads SOAP request
     */
    protected SlingHttpServletResponse getCurrentResponse() {
        RequestContext requestContext = RequestContext.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("No current soap request context available.");
        }
        return (SlingHttpServletResponse)requestContext.getResponse();
    }

    /**
     * @return Interface of SOAP service
     */
    protected abstract Class getServerInterfaceType();

}

//$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/AbstractJaxWsServer.java $
//$Id: AbstractJaxWsServer.java 680 2011-09-12 16:57:25Z PRO-VISION\SSeifert $
package org.liveSense.framework.cxf.util;

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.apache.sling.commons.classloader.DynamicClassLoader;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.apache.sling.commons.classloader.DynamicClassLoaderProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract servlet-based implementation for CXF-based SOAP services. Ensures
 * that correct class loader is used is during initialization and invoking
 * phases. Via getCurrentRequest() and getCurrentResponse() it is possible to
 * access these objects from SOAP method implementations.
 */
@Component(componentAbstract=true)
public abstract class AbstractWsServer extends CXFNonSpringServlet {
    private static final long serialVersionUID = 1L;

	@Reference(bind="bindAuth", unbind="unbindAuth")
	AuthenticationSupport auth = null;

	@Reference
	PackageAdmin packageAdmin;

	@Reference
	DynamicClassLoaderManager dynamicClassLoader = null;
		
    /**
     * Extension for SOAP requests
     */
	private final Logger log = LoggerFactory.getLogger("SOAP");

	public abstract void callInit() throws Throwable;

	public abstract void callFinal() throws Throwable;
	
	//private HashMap<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();
	private ClassLoader classLoader = null;
	
   /**
    *
    * Allows the extending OSGi service to set its classloader.
    *
    * @param classLoader The classloader to provide to the SlingRemoteServiceServlet.
    */
   protected void setClassLoader(ClassLoader classLoader) {
       this.classLoader = classLoader;
   }
   
   /*
	
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
	*/
   public abstract void init(ServletConfig pServletConfig) throws ServletException;

   public void initCxfServlet(ServletConfig sc) throws ServletException {
	   super.init(sc);
   }
   
    @Override
    protected void invoke(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException {
        RequestContext.getThreadLocal().set(new RequestContext(pRequest, pResponse));
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

        try {
        	if (classLoader != null) {
                // Set contextClassLoader
            	Thread.currentThread().setContextClassLoader(classLoader);
            } else {
            	ClassLoader dcl = null;
            	if (dynamicClassLoader != null) {
            		dcl = dynamicClassLoader.getDynamicClassLoader();
            	}
            	Thread.currentThread().setContextClassLoader(dynamicClassLoader.getDynamicClassLoader());
            }
            // Authenticating - OSGi context
            if (auth != null) {
            	auth.handleSecurity(pRequest, pResponse);
            }
            try {
            	callInit();
            } catch (Throwable e) {
            	throw new ServletException("Error on callInit", e);
			}
            super.invoke(new RequestWrapper(pRequest), pResponse);
        } finally {
        	try {
        		callFinal();
        	} catch (Throwable e) {
            	log.error("callFinal: ", e);
        	}
           	Thread.currentThread().setContextClassLoader(oldClassLoader);
            RequestContext.getThreadLocal().remove();        		
        }
    }

    /**
     * @return Servlet request for current threads SOAP request
     */
    protected HttpServletRequest getThreadLocalRequest() {
        RequestContext requestContext = RequestContext.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("No current soap request context available.");
        }
        return (SlingHttpServletRequest)requestContext.getRequest();
    }

    /**
     * @return Servlet response for current threads SOAP request
     */
    protected HttpServletResponse getThreadLocalResponse() {
        RequestContext requestContext = RequestContext.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("No current soap request context available.");
        }
        return (SlingHttpServletResponse)requestContext.getResponse();
    }

	protected String getUser() {
		return (String)this.getThreadLocalRequest().getAttribute("org.osgi.service.http.authentication.remote.user");
	}

    /**
     * @return Interface of SOAP service
     */
    protected abstract Class getServerInterfaceType();

	public Bundle getBundleByName(String name) {
		 Bundle[] ret = packageAdmin.getBundles(name, null);
		 if (ret != null && ret.length > 0) {
			 return ret[0];
		 }
		 return null;
	}

	protected void bindAuth(AuthenticationSupport auth) {
		this.auth = auth;
	}
	
	protected void unbindAuth(AuthenticationSupport auth) {
		this.auth = null;
	}
}

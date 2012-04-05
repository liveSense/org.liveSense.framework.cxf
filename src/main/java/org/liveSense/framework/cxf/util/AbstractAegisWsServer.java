package org.liveSense.framework.cxf.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.apache.cxf.BusFactory;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.type.XMLTypeCreator;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.felix.scr.annotations.Component;

/**
 * Abstract servlet-based implementation for CXF-based SOAP services. Ensures
 * that correct class loader is used is during initialization and invoking
 * phases. Via getCurrentRequest() and getCurrentResponse() it is possible to
 * access these objects from SOAP method implementations.
 */
@Component(componentAbstract=true)
public abstract class AbstractAegisWsServer extends AbstractWsServer {
    private static final long serialVersionUID = 1L;
    

    @Override
    public void init(ServletConfig pServletConfig) throws ServletException {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // set classloader to CXF bundle class loader to avoid OSGI classloader problems
          Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());
                    	
            super.initCxfServlet(pServletConfig);

            ServerFactoryBean factory = new ServerFactoryBean();
            factory.setBus(getBus());
            factory.setAddress(RequestWrapper.VIRTUAL_PATH);
            factory.setServiceClass(getServerInterfaceType());
            factory.getServiceFactory().setDataBinding(new AegisDatabinding());
            factory.setServiceBean(this);
            factory.create();

		} finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

}

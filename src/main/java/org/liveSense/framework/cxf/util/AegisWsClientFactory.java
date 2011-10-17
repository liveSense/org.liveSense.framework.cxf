package org.liveSense.framework.cxf.util;

import org.apache.cxf.BusFactory;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;

/**
 * Helper methods for initializing webservice clients.
 */
public class AegisWsClientFactory {

    private AegisWsClientFactory() {
        // static methods only
    }

    /**
     * Create webservice port via JAXWS proxy factory.
     * 
     * This method fixes numerous problems with 3rdparty libs used by CXF and
     * CXF itself and classloader issues with OSGI. Using this method the
     * initialization phase of JAXB mapping is wrapped in an OSGI-aware
     * classloader. Furthermore each client instances is wrapped in an
     * OSGI-aware subclass (see {@link OsgiAwareClientImpl}), which ensures that
     * each invoke call on a webservice method is itself executed within an
     * OSGI-aware classloader context.
     * 
     * @param <T> Port class
     * @param pClass Port class
     * @param pPortUrl Port url (this is not the WSDL location)
     * @return Port object
     */
    public static <T> T create(Class<T> pClass, String pPortUrl) {
        return AegisWsClientFactory.create(pClass, pPortUrl, null, null);
    }
    
    /**
     * Create webservice port via JAXWS proxy factory.
     * 
     * This method fixes numerous problems with 3rdparty libs used by CXF and
     * CXF itself and classloader issues with OSGI. Using this method the
     * initialization phase of JAXB mapping is wrapped in an OSGI-aware
     * classloader. Furthermore each client instances is wrapped in an
     * OSGI-aware subclass (see {@link OsgiAwareClientImpl}), which ensures that
     * each invoke call on a webservice method is itself executed within an
     * OSGI-aware classloader context.
     * 
     * @param <T> Port class
     * @param pClass Port class
     * @param pPortUrl Port url (this is not the WSDL location)
     * @param pUsername Username for port authentication
     * @param pPassword Password for port authentication
     * @return Port object
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> pClass, String pPortUrl, String pUsername, String pPassword) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // set classloader to CXF bundle class loader to avoid OSGI classloader problems
            Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

            ClientProxyFactoryBean factory = new ClientProxyFactoryBean(new OsgiAwareAegisWsClientFactoryBean());
            factory.setAddress(pPortUrl);
            factory.getServiceFactory().setDataBinding(new AegisDatabinding());
            factory.setServiceClass(pClass);
            factory.setUsername(pUsername);
            factory.setPassword(pPassword);
            return (T) factory.create();
            
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

}

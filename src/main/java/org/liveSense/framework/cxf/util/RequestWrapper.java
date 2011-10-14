//$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/RequestWrapper.java $
//$Id: RequestWrapper.java 677 2011-09-09 15:26:10Z PRO-VISION\SSeifert $
package org.liveSense.framework.cxf.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper that maps all pathinfo to a virtual path, to whom the SOAP
 * services are registered to.
 */
class RequestWrapper extends HttpServletRequestWrapper {

    public static final String VIRTUAL_PATH = "/soaprequest";

    public RequestWrapper(HttpServletRequest pRequest) {
        super(pRequest);
    }

    @Override
    public String getPathInfo() {
        return VIRTUAL_PATH;
    }

}

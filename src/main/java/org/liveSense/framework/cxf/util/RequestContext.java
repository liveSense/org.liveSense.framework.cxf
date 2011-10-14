//$URL: http://feanor:8050/svn/test/trunk/DevTest/apache-sling/adaptto/sling-cxf-integration/helloworld-application/src/main/java/adaptto/slingcxf/server/util/RequestContext.java $
//$Id: RequestContext.java 677 2011-09-09 15:26:10Z PRO-VISION\SSeifert $
package org.liveSense.framework.cxf.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages request/response context of incoming SOAP server HTTP requests using
 * a ThreadLocal.
 */
class RequestContext {

    private static ThreadLocal<RequestContext> mThreadLocal = new ThreadLocal<RequestContext>();

    private final HttpServletRequest mRequest;
    private final HttpServletResponse mResponse;

    RequestContext(HttpServletRequest pRequest, HttpServletResponse pResponse) {
        mRequest = pRequest;
        mResponse = pResponse;
    }

    /**
     * @return Request
     */
    public HttpServletRequest getRequest() {
        return mRequest;
    }

    /**
     * @return Response
     */
    public HttpServletResponse getResponse() {
        return mResponse;
    }

    /**
     * @return Context for current SOAP server request
     */
    public static RequestContext getRequestContext() {
        return mThreadLocal.get();
    }

    static ThreadLocal<RequestContext> getThreadLocal() {
        return mThreadLocal;
    }

}

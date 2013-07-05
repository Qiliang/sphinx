package com.nikoyo.sphinx;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.browser.token.SimpleTokenHandler;
import org.apache.chemistry.opencmis.server.impl.browser.token.SimpleTokenHandlerSessionHelper;
import org.apache.chemistry.opencmis.server.impl.browser.token.TokenHandler;
import org.apache.chemistry.opencmis.server.shared.BasicAuthCallContextHandler;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;

public class TokenCallContextHandler extends BasicAuthCallContextHandler implements TokenHandler {

    private static final long serialVersionUID = 1L;

    private final TokenHandler tokenHandler;

    /**
     * Constructor.
     */
    public TokenCallContextHandler() {
        tokenHandler = new SimpleTokenHandler();
    }

    public Map<String, String> getCallContextMap(HttpServletRequest request) {
        Map<String, String> result = new HashMap<String, String>();

        Map<String, String> basicAuthMap = super.getCallContextMap(request);
        if (basicAuthMap != null && !basicAuthMap.isEmpty()) {
            result.putAll(basicAuthMap);
        }

        // lastResult must always provide an old token
        // -> don't check the token
        boolean isLastResultRequest = "lastresult".equalsIgnoreCase(HttpUtils.getStringParameter(request,
                Constants.PARAM_SELECTOR));

        if (!isLastResultRequest) {
            // if a token is provided, check it
            if (request.getParameter(Constants.PARAM_TOKEN) != null) {
                if (SimpleTokenHandlerSessionHelper.testAndInvalidateToken(request)) {
                    String token = SimpleTokenHandlerSessionHelper.getToken(request);
                    String appId = SimpleTokenHandlerSessionHelper.getApplicationIdFromKey(token);
                    result.put(CallContext.USERNAME, SimpleTokenHandlerSessionHelper.getUser(request, appId));
                    result.put(CallContext.PASSWORD, null);
                } else {
                    throw new CmisPermissionDeniedException("Invalid token!");
                }
            }

            if (!result.containsKey(CallContext.USERNAME)) {
                // neither basic authentication nor token authentication have
                // returned a username -> reject request
                throw new CmisPermissionDeniedException("No authentication!");
            }
        }

        return result;
    }

    public void service(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
        tokenHandler.service(servletContext, request, response);
    }
}

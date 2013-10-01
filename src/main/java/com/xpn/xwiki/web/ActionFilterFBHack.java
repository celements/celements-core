package com.xpn.xwiki.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.web.ActionFilter;
import com.xpn.xwiki.web.XWikiConfigurationService;

public class ActionFilterFBHack implements Filter {
    /** Logging helper. */
    private static final Log LOG = LogFactory.getLog(ActionFilter.class);

    /** The query property name prefix that indicates the target action. */
    private static final String ACTION_PREFIX = "action_";

    /** URL path separator. */
    private static final String PATH_SEPARATOR = "/";

    /**
     * The name of the request attribute that specifies if the action has been already dispatched. This flag is required
     * to prevent recursive dispatch loop and allows us to map this filter to INCLUDE and FORWARD. The value of this
     * request attribute is a string. The associated boolean value is determined using {@link Boolean#valueOf(String)}.
     */
    private static final String ATTRIBUTE_ACTION_DISPATCHED = ActionFilter.class.getName() + ".actionDispatched";

    /**
     * Use to access resources.
     */
    private ServletContext servletContext;

    /**
     * {@inheritDoc}
     * 
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
      this.servletContext = filterConfig.getServletContext();
    }

  /**
   * {@inheritDoc}
   * 
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  @SuppressWarnings("unchecked")
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain
      ) throws IOException, ServletException {
    // Only HTTP requests can be dispatched.
    if (request instanceof HttpServletRequest
        && !Boolean.valueOf((String) request.getAttribute(ATTRIBUTE_ACTION_DISPATCHED))) {
      HttpServletRequest hrequest = (HttpServletRequest) request;
      Enumeration<String> parameterNames = hrequest.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String parameter = parameterNames.nextElement();
        if(needsDispatch(parameter)) {
          String targetURL = getTargetURL(hrequest, parameter);
          RequestDispatcher dispatcher = hrequest.getRequestDispatcher(targetURL);
          if (dispatcher != null) {
            LOG.debug("Forwarding request to " + targetURL);
            request.setAttribute(ATTRIBUTE_ACTION_DISPATCHED, "true");
            dispatcher.forward(hrequest, response);
            // Allow multiple calls to this filter as long as they are not nested.
            request.removeAttribute(ATTRIBUTE_ACTION_DISPATCHED);
            // If the request was forwarder to another path, don't continue the normal processing chain.
            return;
          }
        }
      }
    }
    // Let the request pass through unchanged.
    chain.doFilter(request, response);
  }

  boolean needsDispatch(String parameter) {
    String action = parameter.replaceFirst(ACTION_PREFIX, "").trim();
    //TODO find a way to get all valid actions instead
    boolean isValidAction = (action.length() > 0) && !action.endsWith("_map");
    return parameter.startsWith(ACTION_PREFIX) && isValidAction;
  }

  /**
   * {@inheritDoc}
   * 
   * @see Filter#destroy()
   */
  public void destroy() {
    // No finalization needed.
  }

  /**
   * Compose a new URL path based on the original request and the specified action. The result is relative to the
   * application context, so that it can be used with {@link HttpServletRequest#getRequestDispatcher(String)}. For
   * example, calling this method with a request for <tt>/xwiki/bin/edit/Some/Document</tt> and <tt>action_save</tt>,
   * the result is <tt>/bin/save/Some/Document</tt>.
   * 
   * @param request the original request
   * @param action the action parameter, starting with <tt>action_</tt>
   * @return The rebuilt URL path, with the specified action in place of the original Struts action. Note that unlike
   *         the HTTP path, this does not contain the application context part.
   */
  private String getTargetURL(HttpServletRequest request, String action) {
    String newAction = PATH_SEPARATOR + action.substring(ACTION_PREFIX.length());

    // Extract the document name from the requested path. We don't use getPathInfo() since it is decoded
    // by the container, thus it will not work when XWiki uses a non-UTF-8 encoding.
    String path = request.getRequestURI();

    // First step, remove the context path, if any.
    path = XWiki.stripSegmentFromPath(path, request.getContextPath());

    // Second step, remove the servlet path, if any.
    String servletPath = request.getServletPath();
    path = XWiki.stripSegmentFromPath(path, servletPath);

    // Third step, remove the struts mapping. This step is mandatory, so this filter will fail if the
    // requested action was a hidden (default) 'view', like in '/bin/Main/'. This is OK, since forms
    // don't use 'view' as a target.
    int index = path.indexOf(PATH_SEPARATOR, 1);

    // We need to also get rid of the wiki name in case of a XEM in usepath mode
    if ("1".equals(XWikiConfigurationService.getProperty("xwiki.virtual.usepath", "0", this.servletContext))) {
      if (servletPath.equals(PATH_SEPARATOR
        + XWikiConfigurationService.getProperty("xwiki.virtual.usepath.servletpath", "wiki",
            this.servletContext))) {
        // Move the wiki name together with the servlet path
        servletPath += path.substring(0, index);
        index = path.indexOf(PATH_SEPARATOR, index + 1);
      }
    }

    String document = path.substring(index);

    // Compose the target URL starting with the servlet path.
    return servletPath + newAction + document;
  }
}

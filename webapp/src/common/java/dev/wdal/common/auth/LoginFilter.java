package dev.wdal.common.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static dev.wdal.common.auth.JwtUtil.getCookieValue;
import static dev.wdal.common.auth.JwtUtil.validateToken;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter
{
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private String contextPath = "";
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        this.contextPath = httpRequest.getContextPath();

//        System.out.println("LoginFilter processing: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI()))
        {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (validateToken(getCookieValue(httpRequest, "jwtToken")) == null) // httpRequest.getSession().getAttribute("user") == null
        {
            System.out.println("LoginFilter blocking: " + httpRequest.getRequestURI());
            httpResponse.sendRedirect(this.contextPath + "/");
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI)
    {
        /*
         Set up your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc...)
         You might also want to allow some CSS files, etc...
         */
        String fullURIString = requestURI.toLowerCase();
        // System.out.println("isUrlAllowedWithoutLogin: " + fullURIString);
        String partialURIString = fullURIString.replaceFirst(this.contextPath.toLowerCase() + "/", "");
        // System.out.println("partialURIString: " + partialURIString);
        return allowedURIs.stream().anyMatch(partialURIString::equals) || partialURIString.startsWith("_dashboard/");
    }

    public void init(FilterConfig fConfig)
    {
        allowedURIs.add("");
        allowedURIs.add("images/favicon.ico");
        allowedURIs.add("css/styles.css");
        allowedURIs.add("js/login.js");
        allowedURIs.add("api/login");
    }

    public void destroy()
    {
        // ignored.
    }
}
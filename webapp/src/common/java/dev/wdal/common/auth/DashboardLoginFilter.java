package dev.wdal.common.auth;

import io.jsonwebtoken.Claims;
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
@WebFilter(filterName = "DashboardLoginFilter", urlPatterns = "/_dashboard/*")
public class DashboardLoginFilter implements Filter
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

//        System.out.println("DashboardLoginFilter processing: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI()))
        {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        Claims claims = validateToken(getCookieValue(httpRequest, "jwtToken"));
        if (claims == null || !claims.get("userType").equals("employee"))
        {
            System.out.println("DashboardLoginFilter blocking: " + httpRequest.getRequestURI());
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
        // System.out.println("dash isUrlAllowedWithoutLogin: " + fullURIString);
        String partialURIString = fullURIString.replaceFirst(this.contextPath.toLowerCase()+ "/_dashboard/", "");
        // System.out.println("dash partialURIString: " + partialURIString);
        return allowedURIs.stream().anyMatch(partialURIString::equals);
    }

    public void init(FilterConfig fConfig)
    {
        allowedURIs.add("");
        allowedURIs.add("js/login.js");
        allowedURIs.add("api/login");
    }

    public void destroy()
    {
        // ignored.
    }
}
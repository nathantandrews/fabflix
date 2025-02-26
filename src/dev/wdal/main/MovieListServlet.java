package dev.wdal.main;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends AdvancedSearchServlet
{
    private static final String SERVICE_NAME = "movies";

    protected String getServiceName()
    {
        return SERVICE_NAME;
    }

    protected void setSearchConds(HttpServletRequest request)
    {
        clearConstraints();
        // addTitleConstraint(request);
    }
}

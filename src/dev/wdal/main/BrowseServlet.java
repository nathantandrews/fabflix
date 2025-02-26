package dev.wdal.main;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends AdvancedSearchServlet
{
    private static final String SERVICE_NAME = "browse";

    @Override
    protected String getServiceName()
    {
        return SERVICE_NAME;
    }

    @Override
    protected void setConstraints(HttpServletRequest request)
    {
        clearConstraints();

        String genre = request.getParameter("genre");
        if (genre != null && !genre.isEmpty())
        {
            addConstraint("gm.genreId = ?", Integer.valueOf(genre));
        }

        addTitleConstraint(request);
    }
}

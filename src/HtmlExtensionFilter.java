import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HtmlExtensionFilter implements Filter
{
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        path = path.replaceFirst(req.getContextPath(), "");

        if (path.startsWith("/api") || path.startsWith("/images") || path.startsWith("/css") || path.startsWith("/js"))
        {
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/_dashboard"))
        {
            request.getRequestDispatcher("/pages" + path + ".html").forward(request, response);
        }
        else if (path.startsWith("/pages"))
        {
            request.getRequestDispatcher(path + ".html").forward(request, response);
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) {}

    public void destroy() {}
}
